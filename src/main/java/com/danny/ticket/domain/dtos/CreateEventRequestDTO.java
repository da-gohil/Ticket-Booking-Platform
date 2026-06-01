package com.danny.ticket.domain.dtos;

import com.danny.ticket.domain.CreateTicketTypeRequest;
import com.danny.ticket.domain.entities.EventStatusEnum;
import jakarta.validation.MessageInterpolator;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateEventRequestDTO {

    @NotBlank(message = "Event name is required")
    private String name;

    private LocalDateTime start;
    private LocalDateTime end;

    @NotBlank(message = "venue information is required")
    private String venue;
    private LocalDateTime salesStart;
    private LocalDateTime salesEnd;

    @NotNull(message = "Event Status must be provided")
    private EventStatusEnum status;
    @NotEmpty(message = "Atleast one ticket type is required")
    @Valid
    private List<CreateTicketTypeRequestDTO> ticketTypes;

}
