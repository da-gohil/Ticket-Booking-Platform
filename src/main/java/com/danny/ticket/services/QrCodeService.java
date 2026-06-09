package com.danny.ticket.services;

import com.danny.ticket.domain.entities.QrCode;
import com.danny.ticket.domain.entities.Ticket;

public interface QrCodeService {

    QrCode generateQrCode(Ticket ticket);
    
}
