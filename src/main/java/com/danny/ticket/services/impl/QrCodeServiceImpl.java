package com.danny.ticket.services.impl;

import com.danny.ticket.domain.entities.QrCode;
import com.danny.ticket.domain.entities.Ticket;
import com.danny.ticket.services.QrCodeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class QrCodeServiceImpl implements QrCodeService {

    @Override
    public QrCode generateQrCode(Ticket ticket) {
        return null;
    }
}
