package com.danny.ticket;

import com.danny.ticket.domain.CreateEventRequest;
import com.danny.ticket.domain.CreateTicketTypeRequest;
import com.danny.ticket.domain.UpdateEventRequest;
import com.danny.ticket.domain.UpdateTicketTypeRequest;
import com.danny.ticket.domain.entities.Event;
import com.danny.ticket.domain.entities.EventStatusEnum;
import com.danny.ticket.domain.entities.TicketType;
import com.danny.ticket.domain.entities.User;
import com.danny.ticket.exceptions.EventUpdateException;
import com.danny.ticket.exceptions.TicketTypeNotFoundException;
import com.danny.ticket.repositories.UserRepository;
import com.danny.ticket.services.EventService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Behavioural tests for {@link EventService#updateEventForOrganizer}'s ticket-type
 * diffing: existing types are updated in place, omitted ones are orphan-removed,
 * and null-id ones are created. Runs against the live docker stack DB.
 */
@SpringBootTest
class EventUpdateDiffingTest {

    @Autowired EventService eventService;
    @Autowired UserRepository userRepository;
    @Autowired PlatformTransactionManager txManager;

    private UUID seedOrganizer() {
        TransactionTemplate tx = new TransactionTemplate(txManager);
        return tx.execute(s -> {
            User u = new User();
            u.setId(UUID.randomUUID());
            u.setName("organizer-" + UUID.randomUUID());
            u.setEmail(UUID.randomUUID() + "@example.com");
            return userRepository.save(u).getId();
        });
    }

    private CreateTicketTypeRequest ticketType(String name, double price) {
        CreateTicketTypeRequest tt = new CreateTicketTypeRequest();
        tt.setName(name);
        tt.setPrice(BigDecimal.valueOf(price));
        tt.setDescription(name + " desc");
        tt.setTotalAvailable(100);
        return tt;
    }

    /** Creates an event with a "VIP" and a "GA" ticket type; returns the persisted event. */
    private Event createEventWithTwoTypes(UUID organizerId) {
        CreateEventRequest req = new CreateEventRequest();
        req.setName("Original");
        req.setStart(LocalDateTime.now());
        req.setEnd(LocalDateTime.now().plusHours(2));
        req.setVenue("Original Venue");
        req.setStatus(EventStatusEnum.DRAFT);
        req.setTicketTypes(List.of(ticketType("VIP", 150.0), ticketType("GA", 50.0)));
        return eventService.createEvent(organizerId, req);
    }

    private UUID idOfType(Event event, String name) {
        return event.getTicketTypes().stream()
                .filter(tt -> name.equals(tt.getName()))
                .map(TicketType::getId)
                .findFirst()
                .orElseThrow();
    }

    private UpdateTicketTypeRequest updateType(UUID id, String name, double price) {
        UpdateTicketTypeRequest tt = new UpdateTicketTypeRequest();
        tt.setId(id);
        tt.setName(name);
        tt.setPrice(BigDecimal.valueOf(price));
        tt.setDescription(name + " desc");
        tt.setTotalAvailable(100);
        return tt;
    }

    @Test
    void update_modifiesKept_removesOmitted_andAddsNew() {
        UUID organizerId = seedOrganizer();
        Event created = createEventWithTwoTypes(organizerId);
        UUID vipId = idOfType(created, "VIP");

        UpdateEventRequest req = new UpdateEventRequest();
        req.setId(created.getId());
        req.setName("Updated");
        req.setStart(created.getStart());
        req.setEnd(created.getEnd());
        req.setVenue("New Venue");
        req.setStatus(EventStatusEnum.PUBLISHED);
        req.setTicketTypes(List.of(
                updateType(vipId, "VIP Plus", 200.0), // keep + modify
                updateType(null, "Balcony", 40.0)      // create (GA is omitted -> removed)
        ));

        eventService.updateEventForOrganizer(organizerId, created.getId(), req);

        Event reloaded = eventService.getEventForOrganizer(organizerId, created.getId()).orElseThrow();
        assertThat(reloaded.getName()).isEqualTo("Updated");
        assertThat(reloaded.getVenue()).isEqualTo("New Venue");
        assertThat(reloaded.getStatus()).isEqualTo(EventStatusEnum.PUBLISHED);

        assertThat(reloaded.getTicketTypes())
                .as("GA orphan-removed; VIP kept; Balcony added")
                .hasSize(2)
                .extracting(TicketType::getName)
                .containsExactlyInAnyOrder("VIP Plus", "Balcony");

        TicketType vip = reloaded.getTicketTypes().stream()
                .filter(tt -> "VIP Plus".equals(tt.getName())).findFirst().orElseThrow();
        assertThat(vip.getId()).as("kept type retains its identity").isEqualTo(vipId);
        assertThat(vip.getPrice()).isEqualByComparingTo(BigDecimal.valueOf(200.0));
    }

    @Test
    void update_withUnknownTicketTypeId_throwsAndPersistsNothing() {
        UUID organizerId = seedOrganizer();
        Event created = createEventWithTwoTypes(organizerId);

        UpdateEventRequest req = new UpdateEventRequest();
        req.setId(created.getId());
        req.setName("Should not persist");
        req.setStart(created.getStart());
        req.setEnd(created.getEnd());
        req.setVenue("Original Venue");
        req.setStatus(EventStatusEnum.DRAFT);
        // An id that doesn't belong to this event.
        req.setTicketTypes(List.of(updateType(UUID.randomUUID(), "Ghost", 10.0)));

        assertThatThrownBy(() ->
                eventService.updateEventForOrganizer(organizerId, created.getId(), req))
                .isInstanceOf(TicketTypeNotFoundException.class);

        // Rolled back: original name and both ticket types intact.
        Event reloaded = eventService.getEventForOrganizer(organizerId, created.getId()).orElseThrow();
        assertThat(reloaded.getName()).isEqualTo("Original");
        assertThat(reloaded.getTicketTypes())
                .extracting(TicketType::getName)
                .containsExactlyInAnyOrder("VIP", "GA");
    }

    @Test
    void listForOrganizer_ticketTypesUsableWithOpenInViewDisabled() {
        UUID organizerId = seedOrganizer();
        createEventWithTwoTypes(organizerId);

        // Called outside any transaction, exactly as a controller does with
        // open-in-view off. The service must have initialised ticketTypes, so
        // touching them here must not raise LazyInitializationException.
        var page = eventService.listEventForOrganizer(organizerId, PageRequest.of(0, 50));
        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getContent().get(0).getTicketTypes())
                .extracting(TicketType::getName)
                .containsExactlyInAnyOrder("VIP", "GA");
    }

    @Test
    void update_withNullId_throwsEventUpdateException() {
        UUID organizerId = seedOrganizer();
        UpdateEventRequest req = new UpdateEventRequest();
        req.setId(null);

        assertThatThrownBy(() ->
                eventService.updateEventForOrganizer(organizerId, UUID.randomUUID(), req))
                .isInstanceOf(EventUpdateException.class);
    }

    @Test
    void update_withMismatchedId_throwsEventUpdateException() {
        UUID organizerId = seedOrganizer();
        UpdateEventRequest req = new UpdateEventRequest();
        req.setId(UUID.randomUUID()); // body id

        assertThatThrownBy(() ->
                eventService.updateEventForOrganizer(organizerId, UUID.randomUUID(), req)) // different path id
                .isInstanceOf(EventUpdateException.class);
    }
}
