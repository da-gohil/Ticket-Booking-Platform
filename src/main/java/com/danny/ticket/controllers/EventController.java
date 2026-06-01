package com.danny.ticket.controllers;

import com.danny.ticket.domain.CreateEventRequest;
import com.danny.ticket.domain.dtos.CreateEventRequestDTO;
import com.danny.ticket.domain.dtos.CreateEventResponseDTO;
import com.danny.ticket.domain.entities.Event;
import com.danny.ticket.mappers.EventMapper;
import com.danny.ticket.services.EventService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping(params = "/api/v1/events")
@RequiredArgsConstructor
public class EventController {

    private final EventMapper eventMapper;
    private final EventService eventService;

    @PostMapping
    public ResponseEntity<CreateEventResponseDTO> createEvent(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody CreateEventRequestDTO createEventRequestDTO){
        CreateEventRequest createEventRequest = eventMapper.fromDto(createEventRequestDTO);
        UUID userId = UUID.fromString(jwt.getSubject());
        Event createdEvent = eventService.createEvent(userId, createEventRequest);

        CreateEventResponseDTO createEventResponseDTO = eventMapper.toDto(createdEvent);
        return new ResponseEntity<>(createEventResponseDTO, HttpStatus.CREATED);
    }
}
