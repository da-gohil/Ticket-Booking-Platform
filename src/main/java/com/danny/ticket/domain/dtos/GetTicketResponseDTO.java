package com.danny.ticket.domain.dtos;

import com.danny.ticket.domain.entities.TicketStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GetTicketResponseDTO {

    private UUID id;
    private TicketStatusEnum status;
    private Double price;
    private String description;

    private String eventName;
    private String eventVenue;

    private LocalDateTime eventStart;
    private LocalDateTime eventEnd;

}
