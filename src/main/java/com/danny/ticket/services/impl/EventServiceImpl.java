package com.danny.ticket.services.impl;

import com.danny.ticket.domain.CreateEventRequest;
import com.danny.ticket.domain.UpdateEventRequest;
import com.danny.ticket.domain.UpdateTicketTypeRequest;
import com.danny.ticket.domain.entities.Event;
import com.danny.ticket.domain.entities.EventStatusEnum;
import com.danny.ticket.domain.entities.TicketType;
import com.danny.ticket.domain.entities.User;
import com.danny.ticket.exceptions.EventUpdateException;
import com.danny.ticket.exceptions.TicketTypeNotFoundException;
import com.danny.ticket.exceptions.EventNotFoundException;
import com.danny.ticket.exceptions.UserNotFoundException;
import com.danny.ticket.repositories.EventRepository;
import com.danny.ticket.repositories.UserRepository;
import com.danny.ticket.services.EventService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {

    private final UserRepository userRepository;
    private final EventRepository eventRepository;

    @Override
    @Transactional
    public Event createEvent(UUID organizerId, CreateEventRequest event) {
        User organizer = userRepository.findById(organizerId)
                .orElseThrow(() -> new UserNotFoundException(
                        String.format("User with ID '%s' not found", organizerId)
                ));

        Event eventToCreate = new Event();

        List<TicketType> ticketTypesToCreate = event.getTicketTypes().stream()
                .map(ticketType -> newTicketType(
                        ticketType.getName(),
                        ticketType.getPrice(),
                        ticketType.getDescription(),
                        ticketType.getTotalAvailable(),
                        eventToCreate))
                .toList();

        eventToCreate.setName(event.getName());
        eventToCreate.setStart(event.getStart());
        eventToCreate.setEnd(event.getEnd());
        eventToCreate.setVenue(event.getVenue());
        eventToCreate.setSalesStart(event.getSalesStart());
        eventToCreate.setSalesEnd(event.getSalesEnd());
        eventToCreate.setStatus(event.getStatus());
        eventToCreate.setOrganizer(organizer);
        eventToCreate.setTicketTypes(ticketTypesToCreate);

        return eventRepository.save(eventToCreate);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Event> listEventForOrganizer(UUID organizerId, Pageable pageable) {
        Page<Event> events = eventRepository.findByOrganizerId(organizerId, pageable);
        // Initialise ticketTypes inside this transaction so the controller can map
        // them after the session closes (open-in-view is disabled). @BatchSize
        // collapses this into a single batched query, not N lazy loads.
        events.forEach(event -> event.getTicketTypes().size());
        return events;
    }

    @Override
    public Optional<Event> getEventForOrganizer(UUID organizerId, UUID id) {
        return eventRepository.findByIdAndOrganizerId(id, organizerId);
    }

    @Override
    @Transactional
    public Event updateEventForOrganizer(UUID organizerId, UUID id, UpdateEventRequest event) {
        if(null == event.getId()){
            throw new EventUpdateException("Event ID cannot be null");
        }

        if(!id.equals(event.getId())){
            throw new EventUpdateException("Cannot update ID of the event");
        }

        Event existingEvent = eventRepository
                .findByIdAndOrganizerId(id, organizerId)
                .orElseThrow(() -> new EventNotFoundException(
                        String.format("Event with ID '%s' does not exist", id)
                ));

        existingEvent.setName(event.getName());
        existingEvent.setStart(event.getStart());
        existingEvent.setEnd(event.getEnd());
        existingEvent.setVenue(event.getVenue());
        existingEvent.setSalesStart(event.getSalesStart());
        existingEvent.setSalesEnd(event.getSalesEnd());
        existingEvent.setStatus(event.getStatus());

        Set<UUID> requestTicketTypeIds = event.getTicketTypes()
                .stream()
                .map(UpdateTicketTypeRequest::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        existingEvent.getTicketTypes().removeIf(existingTicketType ->
                !requestTicketTypeIds.contains(existingTicketType.getId())
        );

        //index to for events to access them by IDs
        Map<UUID, TicketType> existingTicketTypesIndex = existingEvent.getTicketTypes().stream()
                .collect(Collectors.toMap(TicketType::getId, Function.identity()));

        for(UpdateTicketTypeRequest ticketType : event.getTicketTypes()){
            if(null == ticketType.getId()){
                //Create
                existingEvent.getTicketTypes().add(newTicketType(
                        ticketType.getName(),
                        ticketType.getPrice(),
                        ticketType.getDescription(),
                        ticketType.getTotalAvailable(),
                        existingEvent));
            }else if (existingTicketTypesIndex.containsKey(ticketType.getId())) {
                //Update
                TicketType existingTicketType = existingTicketTypesIndex.get(ticketType.getId());
                existingTicketType.setName(ticketType.getName());
                existingTicketType.setPrice(ticketType.getPrice());
                existingTicketType.setDescription(ticketType.getDescription());
                existingTicketType.setTotalAvailable(ticketType.getTotalAvailable());
            }else{
                throw new TicketTypeNotFoundException(String.format(
                        "Ticket type with ID '%s' does not exist", ticketType.getId()
                ));
            }
        }
        return eventRepository.save(existingEvent);
    }

    @Override
    @Transactional
    public void deleteEventForOrganizer(UUID organizerId, UUID id) {
        getEventForOrganizer(organizerId, id).ifPresent(eventRepository::delete);
    }

    @Override
    public Page<Event> listPublishedEvents(Pageable pageable) {
        return eventRepository.findByStatus(EventStatusEnum.PUBLISHED, pageable);
    }

    @Override
    public Page<Event> searchPublishedEvents(String query, Pageable pageable) {
        return eventRepository.searchEvents(query, pageable);
    }

    @Override
    public Optional<Event> getPublishedEvent(UUID id) {
        return eventRepository.findByIdAndStatus(id, EventStatusEnum.PUBLISHED);
    }

    // Single place that builds a TicketType from request fields, shared by the
    // create-event and update-event (new ticket type) paths.
    private TicketType newTicketType(String name, BigDecimal price, String description,
                                     Integer totalAvailable, Event event) {
        TicketType ticketType = new TicketType();
        ticketType.setName(name);
        ticketType.setPrice(price);
        ticketType.setDescription(description);
        ticketType.setTotalAvailable(totalAvailable);
        ticketType.setEvent(event);
        return ticketType;
    }
}
