package com.danny.ticket.services.impl;

import com.danny.ticket.domain.entities.Ticket;
import com.danny.ticket.domain.entities.TicketStatusEnum;
import com.danny.ticket.domain.entities.TicketType;
import com.danny.ticket.domain.entities.User;
import com.danny.ticket.exceptions.TicketSoldOutException;
import com.danny.ticket.exceptions.TicketTypeNotFoundException;
import com.danny.ticket.exceptions.UserNotFoundException;
import com.danny.ticket.repositories.QrCodeRepository;
import com.danny.ticket.repositories.TicketRepository;
import com.danny.ticket.repositories.TicketTypeRepository;
import com.danny.ticket.repositories.UserRepository;
import com.danny.ticket.services.QrCodeService;
import com.danny.ticket.services.TicketTypeService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TicketTypeServiceImpl implements TicketTypeService {

    private final UserRepository userRepository;
    private final TicketTypeRepository ticketTypeRepository;
    private final TicketRepository ticketRepository;
    private final QrCodeRepository qrCodeRepository;
    private final QrCodeService qrCodeService;

    @Override
    @Transactional
    public Ticket purchaseTicket(UUID userId, UUID ticketTypeId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(
                String.format("user with ID %s was not found", userId)
        ));

        TicketType ticketType = ticketTypeRepository.findByIdWithLock(ticketTypeId)
                .orElseThrow(() -> new TicketTypeNotFoundException(
                String.format("ticket type with ID %s not found", ticketTypeId)
        ));

        int purchasedTickets = ticketRepository.countByTicketTypeId(ticketType.getId());
        Integer totalAvailable = ticketType.getTotalAvailable();

        if(purchasedTickets + 1 > totalAvailable){
            throw new TicketSoldOutException();
        }

        Ticket ticket = new Ticket();
        ticket.setStatus(TicketStatusEnum.PURCHASED);
        ticket.setTicketType(ticketType);
        ticket.setPurchaser(user);

        Ticket savedTicket = ticketRepository.save(ticket);
        qrCodeService.generateQrCode(savedTicket);

        return ticketRepository.save(savedTicket);

    }
}
