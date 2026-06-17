package com.danny.ticket.services;

import com.danny.ticket.domain.entities.QrCode;
import com.danny.ticket.domain.entities.Ticket;

import java.util.UUID;

public interface QrCodeService {

    QrCode generateQrCode(Ticket ticket);

    byte[] getQrCodeImageForUserAndTicket(UUID userId, UUID ticketId);
}
