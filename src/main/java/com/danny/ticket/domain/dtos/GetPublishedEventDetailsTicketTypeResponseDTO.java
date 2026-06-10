package com.danny.ticket.domain.dtos;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GetPublishedEventDetailsTicketTypeResponseDTO {

    private UUID id;
    private String name;
    private Double price;
    private String description;

}
