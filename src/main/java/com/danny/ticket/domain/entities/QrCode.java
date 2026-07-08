package com.danny.ticket.domain.entities;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@Table(name = "qr_codes")
public class QrCode {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private QrCodeStatusEnum status;

    //may need to change based on how QR code works
    @Column(name = "value", columnDefinition = "TEXT", nullable = false)
    private String value;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id")
    private Ticket ticket;

    @CreatedDate
    @Column(name="created_at", updatable= false, nullable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name="updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // Identity-based equality (see Event).
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof QrCode qrCode)) return false;
        return id != null && id.equals(qrCode.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
