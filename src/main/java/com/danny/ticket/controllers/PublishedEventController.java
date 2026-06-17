package com.danny.ticket.controllers;

import com.danny.ticket.domain.dtos.GetPublishedEventDetailsResponseDTO;
import com.danny.ticket.domain.dtos.ListPublishedEventResponseDTO;
import com.danny.ticket.domain.entities.Event;
import com.danny.ticket.mappers.EventMapper;
import com.danny.ticket.services.EventService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping(path = "api/v1/published-events")
@RequiredArgsConstructor
public class PublishedEventController {

    private final EventService eventService;
    private final EventMapper eventMapper;

    @GetMapping
    public ResponseEntity<Page<ListPublishedEventResponseDTO>> listPublishedEvents(
            @RequestParam(required = false) String q,
            Pageable pageable){

        //When the user specifies a query we can either search or list all the events
        Page<Event> events;
        if(null != q && !q.trim().isEmpty()){
            events = eventService.searchPublishedEvents(q, pageable);
        }else {
            events = eventService.listPublishedEvents(pageable);
        }
        return ResponseEntity.ok(
                events.map(eventMapper::toListPublishedEventResponseDTO)
        );
    }

    @GetMapping(path="/{eventId}")
    public ResponseEntity<GetPublishedEventDetailsResponseDTO> getPublishedEventDetails(
            @PathVariable UUID eventId
    ){
        return eventService.getPublishedEvent(eventId)
                .map(eventMapper::toGetPublishedEventDetailsResponseDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
