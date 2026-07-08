package com.danny.ticket.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateTicketTypeRequest {

    private String name;
    private BigDecimal price;
    private String description;
    private Integer totalAvailable;

}
