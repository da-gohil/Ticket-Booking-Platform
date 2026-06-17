package com.danny.ticket.domain.entities;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "tickets")
public class Ticket {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private TicketStatusEnum status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_type_id")
    private TicketType ticketType;

    //User can purchase multiple tickets but a ticket can only have one purchaser
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "purchaser_id")
    private User purchaser;

    //Ticket validation
    @OneToMany(mappedBy = "ticket", cascade = CascadeType.ALL)
    private List<TicketValidation> validations = new ArrayList<>();

    //QR Code
    @OneToMany(mappedBy = "ticket", cascade = CascadeType.ALL)
    private List<QrCode> qrCodes = new ArrayList<>();

    //Audit fields
    @CreatedDate
    @Column(name="created_at", updatable= false, nullable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name="updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Ticket ticket = (Ticket) o;
        return Objects.equals(id, ticket.id) && status == ticket.status && Objects.equals(createdAt, ticket.createdAt) && Objects.equals(updatedAt, ticket.updatedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, status, createdAt, updatedAt);
    }
}
