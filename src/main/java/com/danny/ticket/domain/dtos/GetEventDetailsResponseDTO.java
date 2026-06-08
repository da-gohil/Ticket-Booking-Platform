package com.danny.ticket.domain.dtos;

import com.danny.ticket.domain.entities.EventStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SecondaryRow;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GetEventDetailsResponseDTO {

    private UUID id;

    private String name;

    private LocalDateTime start;

    private LocalDateTime end;

    private String venue;

    private LocalDateTime salesStart;

    private LocalDateTime salesEnd;

    private EventStatusEnum status;

    private List<GetEventTicketTypesResponseDTO> ticketTypes = new ArrayList<>();

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}
