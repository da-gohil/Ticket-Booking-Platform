package com.danny.ticket.controllers;

import com.danny.ticket.domain.CreateEventRequest;
import com.danny.ticket.domain.UpdateEventRequest;
import com.danny.ticket.domain.dtos.*;
import com.danny.ticket.domain.entities.Event;
import com.danny.ticket.mappers.EventMapper;
import com.danny.ticket.services.EventService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping(path = "/api/v1/events")
@RequiredArgsConstructor
public class EventController {

    private final EventMapper eventMapper;
    private final EventService eventService;

    @PostMapping
    public ResponseEntity<CreateEventResponseDTO> createEvent(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody CreateEventRequestDTO createEventRequestDTO){
        CreateEventRequest createEventRequest = eventMapper.fromDto(createEventRequestDTO);
        UUID userId = parseUserID(jwt);
        Event createdEvent = eventService.createEvent(userId, createEventRequest);

        CreateEventResponseDTO createEventResponseDTO = eventMapper.toDto(createdEvent);
        return new ResponseEntity<>(createEventResponseDTO, HttpStatus.CREATED);
    }

    @PutMapping(path="/{eventId}")
    public ResponseEntity<UpdateEventResponseDTO> updateEvent(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID eventId,
            @Valid @RequestBody UpdateEventRequestDTO updateEventRequestDTO){
        UpdateEventRequest updateEventRequest = eventMapper.fromDto(updateEventRequestDTO);
        UUID userId = parseUserID(jwt);
        Event createdEvent = eventService.updateEventForOrganizer(
                userId, eventId, updateEventRequest
        );

        UpdateEventResponseDTO updateEventResponseDTO = eventMapper.toUpdateEventResponseDTO(createdEvent);
        return ResponseEntity.ok(updateEventResponseDTO);
    }

    @DeleteMapping("/{event_id}")
    public ResponseEntity<Void> deleteEvent(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID eventId
    ){
        UUID userId = parseUserID(jwt);
        eventService.deleteEventForOrganizer(userId, eventId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<Page<ListEventResponseDTO>> listEvents(
            @AuthenticationPrincipal Jwt jwt, Pageable pageable
    ){
        UUID userId = parseUserID(jwt);
        Page<Event> events =  eventService.listEventForOrganizer(userId,pageable);
        return ResponseEntity.ok(
                events.map(eventMapper::toListEventResponseDto)
        );
    }

    @GetMapping(path = "/{eventId}")
    public ResponseEntity<GetEventDetailsResponseDTO> getEvent(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID eventId
    ){
        UUID userId = parseUserID(jwt);
        return eventService.getEventForOrganizer(userId, eventId)
                .map(eventMapper::toGetEventDetailsResponseDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    private UUID parseUserID(Jwt jwt){
        return UUID.fromString(jwt.getSubject());
    }
}
