package com.danny.ticket.domain.dtos;

import com.danny.ticket.domain.entities.TicketStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ListTicketTicketTypeResponseDTO {

    private UUID id;
    private TicketStatusEnum status;
    private BigDecimal price;

}
