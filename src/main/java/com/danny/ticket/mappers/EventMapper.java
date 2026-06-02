package com.danny.ticket.mappers;

import com.danny.ticket.domain.CreateEventRequest;
import com.danny.ticket.domain.CreateTicketTypeRequest;
import com.danny.ticket.domain.dtos.CreateEventRequestDTO;
import com.danny.ticket.domain.dtos.CreateEventResponseDTO;
import com.danny.ticket.domain.dtos.CreateTicketTypeRequestDTO;
import com.danny.ticket.domain.entities.Event;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface EventMapper {

    CreateTicketTypeRequest fromDto(CreateTicketTypeRequestDTO dto);

    CreateEventRequest fromDto(CreateEventRequestDTO dto);

    CreateEventResponseDTO toDto(Event event);
}
