package com.danny.ticket.domain.dtos;

import com.danny.ticket.domain.entities.EventStatusEnum;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateEventResponseDTO {

    private UUID uuid;
    private String name;
    private LocalDateTime start;
    private LocalDateTime end;
    private String venue;
    private LocalDateTime salesStart;
    private LocalDateTime salesEnd;
    private EventStatusEnum status;

    private List<UpdateTicketTypeResponseDTO> ticketTypes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}
