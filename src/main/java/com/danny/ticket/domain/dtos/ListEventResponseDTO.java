package com.danny.ticket.domain.dtos;

import com.danny.ticket.domain.entities.EventStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ListEventResponseDTO {

    private UUID id;

    private String name;


    private LocalDateTime start;
    private LocalDateTime end;

    private String venue;

    private LocalDateTime salesStart;
    private LocalDateTime salesEnd;

    private EventStatusEnum status;

    private List<ListEventTicketTypeResponseDTO> ticketTypes = new ArrayList<>();

}
