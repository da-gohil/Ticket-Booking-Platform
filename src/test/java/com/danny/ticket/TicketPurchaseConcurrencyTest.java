package com.danny.ticket;

import com.danny.ticket.domain.entities.Event;
import com.danny.ticket.domain.entities.EventStatusEnum;
import com.danny.ticket.domain.entities.TicketType;
import com.danny.ticket.domain.entities.User;
import com.danny.ticket.exceptions.TicketSoldOutException;
import com.danny.ticket.repositories.EventRepository;
import com.danny.ticket.repositories.TicketRepository;
import com.danny.ticket.repositories.UserRepository;
import com.danny.ticket.services.TicketService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Behavioural tests for {@link TicketService#purchaseTicket}. Runs against the
 * live docker stack DB so the PESSIMISTIC_WRITE lock on the ticket_type row is
 * exercised for real — an in-memory H2 test would not reproduce the race.
 */
@SpringBootTest
class TicketPurchaseConcurrencyTest {

    @Autowired TicketService ticketService;
    @Autowired UserRepository userRepository;
    @Autowired EventRepository eventRepository;
    @Autowired TicketRepository ticketRepository;
    @Autowired PlatformTransactionManager txManager;

    private record Seed(UUID userId, UUID ticketTypeId) {}

    /** Persists a user + event + single ticket type with the given capacity, committed. */
    private Seed seed(int capacity) {
        TransactionTemplate tx = new TransactionTemplate(txManager);
        return tx.execute(status -> {
            User user = new User();
            user.setId(UUID.randomUUID());
            user.setName("buyer-" + UUID.randomUUID());
            user.setEmail(UUID.randomUUID() + "@example.com");
            user = userRepository.save(user);

            Event event = new Event();
            event.setName("event-" + UUID.randomUUID());
            event.setStart(LocalDateTime.now());
            event.setEnd(LocalDateTime.now().plusHours(2));
            event.setVenue("venue");
            event.setStatus(EventStatusEnum.PUBLISHED);
            event.setOrganizer(user);

            TicketType tt = new TicketType();
            tt.setName("GA");
            tt.setPrice(BigDecimal.valueOf(50.00));
            tt.setDescription("general admission");
            tt.setTotalAvailable(capacity);
            tt.setEvent(event);
            event.getTicketTypes().add(tt);

            event = eventRepository.save(event);
            return new Seed(user.getId(), event.getTicketTypes().get(0).getId());
        });
    }

    @Test
    void purchase_throwsWhenSoldOut() {
        Seed seed = seed(1);

        // First purchase consumes the only ticket.
        ticketService.purchaseTicket(seed.userId(), seed.ticketTypeId());

        // Second purchase must be rejected.
        assertThatThrownBy(() ->
                ticketService.purchaseTicket(seed.userId(), seed.ticketTypeId()))
                .isInstanceOf(TicketSoldOutException.class);

        assertThat(ticketRepository.countByTicketTypeId(seed.ticketTypeId())).isEqualTo(1);
    }

    @Test
    void concurrentPurchases_doNotOversell() throws InterruptedException {
        int capacity = 3;
        int threads = 8;
        Seed seed = seed(capacity);

        ExecutorService pool = Executors.newFixedThreadPool(threads);
        CountDownLatch startGate = new CountDownLatch(1);
        CountDownLatch doneGate = new CountDownLatch(threads);
        AtomicInteger success = new AtomicInteger();
        AtomicInteger soldOut = new AtomicInteger();
        AtomicInteger other = new AtomicInteger();

        for (int i = 0; i < threads; i++) {
            pool.submit(() -> {
                try {
                    startGate.await(); // release all threads at once for maximum contention
                    ticketService.purchaseTicket(seed.userId(), seed.ticketTypeId());
                    success.incrementAndGet();
                } catch (TicketSoldOutException e) {
                    soldOut.incrementAndGet();
                } catch (Exception e) {
                    other.incrementAndGet();
                } finally {
                    doneGate.countDown();
                }
            });
        }

        startGate.countDown();
        boolean finished = doneGate.await(30, TimeUnit.SECONDS);
        pool.shutdownNow();

        assertThat(finished).as("all purchase attempts completed").isTrue();
        assertThat(other.get()).as("no unexpected exceptions").isZero();
        assertThat(success.get()).as("exactly capacity succeed").isEqualTo(capacity);
        assertThat(soldOut.get()).as("the rest are rejected").isEqualTo(threads - capacity);
        // The lock guarantees the DB never exceeds capacity.
        assertThat(ticketRepository.countByTicketTypeId(seed.ticketTypeId()))
                .as("persisted tickets never exceed capacity")
                .isEqualTo(capacity);
    }
}
