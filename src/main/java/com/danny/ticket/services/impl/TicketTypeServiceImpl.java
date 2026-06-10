package com.danny.ticket.services.impl;

import com.danny.ticket.domain.entities.Ticket;
import com.danny.ticket.repositories.UserRepository;
import com.danny.ticket.services.TicketTypeService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TicketTypeServiceImpl implements TicketTypeService {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public Ticket purchaseTicket(UUID userId, UUID ticketTypeId) {
        return null;
    }
}
