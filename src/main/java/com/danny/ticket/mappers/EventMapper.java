package com.danny.ticket.mappers;

import com.danny.ticket.domain.CreateEventRequest;
import com.danny.ticket.domain.CreateTicketTypeRequest;
import com.danny.ticket.domain.UpdateEventRequest;
import com.danny.ticket.domain.UpdateTicketTypeRequest;
import com.danny.ticket.domain.dtos.*;
import com.danny.ticket.domain.entities.Event;
import com.danny.ticket.domain.entities.TicketType;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface EventMapper {

    CreateTicketTypeRequest fromDto(CreateTicketTypeRequestDTO dto);

    CreateEventRequest fromDto(CreateEventRequestDTO dto);

    CreateEventResponseDTO toDto(Event event);

    ListEventTicketTypeResponseDTO toDto(TicketType ticketType);

    ListEventResponseDTO toListEventResponseDto(Event event);

    GetEventTicketTypesResponseDTO toGetEventTicketTypesResponseDTO(TicketType ticketType);

    GetEventDetailsResponseDTO toGetEventDetailsResponseDTO(Event event);

    UpdateTicketTypeRequest fromDto(UpdateTicketTypeRequestDTO dto);

    UpdateEventRequest fromDto(UpdateEventRequestDTO dto);

    UpdateTicketTypeResponseDTO toUpdateTicketTypeResponseDto(TicketType ticketType);

    UpdateEventResponseDTO toUpdateEventResponseDTO(Event event);

    ListPublishedEventResponseDTO toListPublishedEventResponseDTO(Event event);

    GetPublishedEventDetailsResponseDTO toGetPublishedEventDetailsResponseDTO(Event event);

    GetPublishedEventDetailsTicketTypeResponseDTO toGetPublishedEventDetailsTicketTypeResponseDTO(TicketType ticketType);

}
