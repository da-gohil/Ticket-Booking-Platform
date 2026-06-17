package com.danny.ticket.domain.dtos;

import com.danny.ticket.domain.entities.TicketStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ListTicketTicketTypeResponseDTO {

    private UUID id;
    private TicketStatusEnum status;
    private Double price;

}
