package com.danny.ticket.domain.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ListEventTicketTypeResponseDTO {

    private UUID id;
    private String name;
    private BigDecimal price;
    private String description;
    private Integer totalAvailable;



}
