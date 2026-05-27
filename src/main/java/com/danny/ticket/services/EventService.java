package com.danny.ticket.services;

import com.danny.ticket.domain.CreateEventRequest;
import com.danny.ticket.domain.entities.Event;

import java.util.UUID;

public interface EventService {
    Event createEvent(UUID organizerId, CreateEventRequest event);

}
