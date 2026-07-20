package com.danny.ticket;

import com.danny.ticket.domain.entities.Event;
import com.danny.ticket.domain.entities.EventStatusEnum;
import com.danny.ticket.domain.entities.Ticket;
import com.danny.ticket.domain.entities.TicketStatusEnum;
import com.danny.ticket.domain.entities.TicketType;
import com.danny.ticket.domain.entities.User;
import com.danny.ticket.repositories.EventRepository;
import com.danny.ticket.repositories.TicketRepository;
import com.danny.ticket.repositories.UserRepository;
import jakarta.persistence.EntityManagerFactory;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies the N+1 fixes by counting JDBC prepared statements per read path via
 * Hibernate statistics. Each measured read runs inside its own (fresh) transaction
 * so the persistence context is empty — this both forces real SQL to fire and
 * faithfully models the running app, where open-in-view keeps the session open
 * while the controller maps lazy associations into DTOs.
 *
 * Runs against the live docker stack DB (localhost:5432/ticketDB). Uses randomly
 * generated ids per run, so it neither depends on nor pollutes existing data.
 */
@SpringBootTest
class QueryCountVerificationTest {

    @Autowired EntityManagerFactory emf;
    @Autowired PlatformTransactionManager txManager;
    @Autowired UserRepository userRepository;
    @Autowired EventRepository eventRepository;
    @Autowired TicketRepository ticketRepository;

    private Statistics stats;
    private TransactionTemplate tx;

    @BeforeEach
    void setUp() {
        stats = emf.unwrap(SessionFactory.class).getStatistics();
        stats.setStatisticsEnabled(true);
        tx = new TransactionTemplate(txManager);
    }

    /** Runs the read in a fresh transaction and returns the number of statements it issued. */
    private long countStatements(Consumer<Object> read) {
        return tx.execute(status -> {
            stats.clear();
            read.accept(null);
            return stats.getPrepareStatementCount();
        });
    }

    private User newUser() {
        User u = new User();
        u.setId(UUID.randomUUID());
        u.setName("user-" + UUID.randomUUID());
        u.setEmail(UUID.randomUUID() + "@example.com");
        return u;
    }

    private TicketType newTicketType(String name, Event event) {
        TicketType tt = new TicketType();
        tt.setName(name);
        tt.setPrice(BigDecimal.valueOf(25.00));
        tt.setDescription("desc");
        tt.setTotalAvailable(100);
        tt.setEvent(event);
        return tt;
    }

    private Event newEvent(User organizer, int ticketTypeCount) {
        Event e = new Event();
        e.setName("event-" + UUID.randomUUID());
        e.setStart(LocalDateTime.now());
        e.setEnd(LocalDateTime.now().plusHours(2));
        e.setVenue("venue");
        e.setStatus(EventStatusEnum.PUBLISHED);
        e.setOrganizer(organizer);
        for (int i = 0; i < ticketTypeCount; i++) {
            e.getTicketTypes().add(newTicketType("tt-" + i, e));
        }
        return e;
    }

    @Test
    void listTicketsForUser_isConstant_regardlessOfRowCount() {
        UUID[] userId = new UUID[1];
        tx.executeWithoutResult(s -> {
            User organizer = userRepository.save(newUser());
            User purchaser = userRepository.save(newUser());
            userId[0] = purchaser.getId();
            Event event = eventRepository.save(newEvent(organizer, 1));
            TicketType tt = event.getTicketTypes().get(0);
            for (int i = 0; i < 5; i++) {
                Ticket t = new Ticket();
                t.setStatus(TicketStatusEnum.PURCHASED);
                t.setTicketType(tt);
                t.setPurchaser(purchaser);
                ticketRepository.save(t);
            }
        });

        long queries = countStatements(ignored -> {
            var page = ticketRepository.findByPurchaserId(userId[0], PageRequest.of(0, 50));
            assertThat(page.getContent()).hasSize(5);
            // Simulate the mapper touching the ToOne ticketType on every row.
            page.getContent().forEach(t -> {
                t.getTicketType().getPrice();
                t.getTicketType().getName();
            });
        });

        // Single content SELECT that joins ticket_types (the ToOne @EntityGraph);
        // Spring Data skips the count query because the first page isn't full.
        // The pre-fix lazy behaviour would be 1 + 5 (one ticketType per row).
        assertThat(queries)
                .as("list 5 tickets + touch ticketType per row")
                .isEqualTo(1);
    }

    @Test
    void getTicketForUser_fetchesTicketTypeAndEventInOneQuery() {
        UUID[] ids = new UUID[2]; // [userId, ticketId]
        tx.executeWithoutResult(s -> {
            User organizer = userRepository.save(newUser());
            User purchaser = userRepository.save(newUser());
            Event event = eventRepository.save(newEvent(organizer, 1));
            Ticket t = new Ticket();
            t.setStatus(TicketStatusEnum.PURCHASED);
            t.setTicketType(event.getTicketTypes().get(0));
            t.setPurchaser(purchaser);
            Ticket saved = ticketRepository.save(t);
            ids[0] = purchaser.getId();
            ids[1] = saved.getId();
        });

        long queries = countStatements(ignored -> {
            Ticket t = ticketRepository.findByIdAndPurchaserId(ids[1], ids[0]).orElseThrow();
            // Mapper walks ticket -> ticketType -> event.
            t.getTicketType().getPrice();
            t.getTicketType().getEvent().getName();
            t.getTicketType().getEvent().getVenue();
        });

        // Single SELECT joining ticket_types and events. NOT 3 separate queries.
        assertThat(queries)
                .as("get ticket detail + walk ticketType.event")
                .isEqualTo(1);
    }

    @Test
    void getEventForOrganizer_fetchesTicketTypesInOneQuery() {
        UUID[] ids = new UUID[2]; // [organizerId, eventId]
        tx.executeWithoutResult(s -> {
            User organizer = userRepository.save(newUser());
            Event event = eventRepository.save(newEvent(organizer, 3));
            ids[0] = organizer.getId();
            ids[1] = event.getId();
        });

        long queries = countStatements(ignored -> {
            Event e = eventRepository.findByIdAndOrganizerId(ids[1], ids[0]).orElseThrow();
            assertThat(e.getTicketTypes()).hasSize(3);
            e.getTicketTypes().forEach(TicketType::getName);
        });

        // Single SELECT with the ticketTypes graph. NOT 1 + 1.
        assertThat(queries)
                .as("get event detail + touch 3 ticket types")
                .isEqualTo(1);
    }

    @Test
    void listEventsForOrganizer_batchesTicketTypes_insteadOfNPlusOne() {
        UUID[] organizerId = new UUID[1];
        tx.executeWithoutResult(s -> {
            User organizer = userRepository.save(newUser());
            organizerId[0] = organizer.getId();
            for (int i = 0; i < 3; i++) {
                eventRepository.save(newEvent(organizer, 2));
            }
        });

        long queries = countStatements(ignored -> {
            var page = eventRepository.findByOrganizerId(organizerId[0], PageRequest.of(0, 50));
            assertThat(page.getContent()).hasSize(3);
            // Touch every event's collection — @BatchSize loads them all in one IN query.
            page.getContent().forEach(e -> e.getTicketTypes().forEach(TicketType::getName));
        });

        // 1 content SELECT for events + 1 batched IN query loading all three
        // events' ticket_types at once (@BatchSize). Count query is skipped
        // because the first page isn't full. The pre-fix lazy behaviour would
        // be 1 + 3 (one ticket_types query per event).
        assertThat(queries)
                .as("list 3 events + touch each event's ticket types")
                .isEqualTo(2);
    }
}
