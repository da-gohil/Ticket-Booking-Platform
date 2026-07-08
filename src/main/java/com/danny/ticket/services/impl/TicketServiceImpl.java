package com.danny.ticket.services.impl;

import com.danny.ticket.domain.entities.Ticket;
import com.danny.ticket.domain.entities.TicketStatusEnum;
import com.danny.ticket.domain.entities.TicketType;
import com.danny.ticket.domain.entities.User;
import com.danny.ticket.exceptions.TicketSoldOutException;
import com.danny.ticket.exceptions.TicketTypeNotFoundException;
import com.danny.ticket.exceptions.UserNotFoundException;
import com.danny.ticket.repositories.TicketRepository;
import com.danny.ticket.repositories.TicketTypeRepository;
import com.danny.ticket.repositories.UserRepository;
import com.danny.ticket.services.QrCodeService;
import com.danny.ticket.services.TicketService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TicketServiceImpl implements TicketService {

    private final UserRepository userRepository;
    private final TicketTypeRepository ticketTypeRepository;
    private final TicketRepository ticketRepository;
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
        int totalAvailable = ticketType.getTotalAvailable() == null ? 0 : ticketType.getTotalAvailable();

        if(purchasedTickets + 1 > totalAvailable){
            throw new TicketSoldOutException();
        }

        Ticket ticket = new Ticket();
        ticket.setStatus(TicketStatusEnum.PURCHASED);
        ticket.setTicketType(ticketType);
        ticket.setPurchaser(user);

        Ticket savedTicket = ticketRepository.save(ticket);
        // The QR code links back to this ticket; the ticket itself is unchanged
        // afterwards, so a second explicit save is unnecessary — the persistence
        // context flushes the managed entity at commit.
        qrCodeService.generateQrCode(savedTicket);

        return savedTicket;
    }

    @Override
    public Page<Ticket> listTicketsForUser(UUID userId, Pageable pageable) {
        return ticketRepository.findByPurchaserId(userId, pageable);
    }

    @Override
    public Optional<Ticket> getTicketForUser(UUID userId, UUID ticketId) {
        return ticketRepository.findByIdAndPurchaserId(ticketId, userId);
    }
}
