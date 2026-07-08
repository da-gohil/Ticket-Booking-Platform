package com.danny.ticket.repositories;

import com.danny.ticket.domain.entities.Ticket;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, UUID> {

    int countByTicketTypeId(UUID ticketTypeId);

    // ToOne fetch-join is safe with pagination and removes the per-row lookup
    // of ticketType during response mapping.
    @EntityGraph(attributePaths = "ticketType")
    Page<Ticket> findByPurchaserId(UUID purchaserId, Pageable pageable);

    // Detail view maps ticketType and its parent event, so fetch both up front.
    @EntityGraph(attributePaths = {"ticketType", "ticketType.event"})
    Optional<Ticket> findByIdAndPurchaserId(UUID id, UUID purchaserId);

}
