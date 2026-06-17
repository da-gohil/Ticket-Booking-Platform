package com.danny.ticket.mappers;

import com.danny.ticket.domain.dtos.GetTicketResponseDTO;
import com.danny.ticket.domain.dtos.ListTicketResponseDTO;
import com.danny.ticket.domain.dtos.ListTicketTicketTypeResponseDTO;
import com.danny.ticket.domain.entities.Ticket;
import com.danny.ticket.domain.entities.TicketType;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TicketMapper {

    ListTicketTicketTypeResponseDTO toListTicketTicketTypeResponseDTO(TicketType ticketType);

    ListTicketResponseDTO toListTicketResponseDTO(Ticket ticket);

    @Mapping(target = "price", source = "ticket.ticketType.price")
    @Mapping(target = "description", source = "ticket.ticketType.description")
    @Mapping(target = "eventName", source = "ticket.ticketType.event.name")
    @Mapping(target = "eventVenue", source = "ticket.ticketType.event.venue")
    @Mapping(target = "eventStart", source = "ticket.ticketType.event.start")
    @Mapping(target = "eventEnd", source = "ticket.ticketType.event.end")
    GetTicketResponseDTO toGetTicketResponseDTO(Ticket ticket);
}
