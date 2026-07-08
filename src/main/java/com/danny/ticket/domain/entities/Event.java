package com.danny.ticket.domain.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.BatchSize;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name="events")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Event {

    @Id
    @Column(name="id", updatable = false, nullable = false)
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "\"start\"", nullable = false)
    private LocalDateTime start;

    @Column(name = "\"end\"", nullable = false)
    private LocalDateTime end;

    @Column(name = "venue", nullable = false)
    private String venue;

    @Column(name="sales_start")
    private LocalDateTime salesStart;

    @Column(name="sales_end")
    private LocalDateTime salesEnd;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private EventStatusEnum status = EventStatusEnum.DRAFT;

    //One organizer can host multiple events
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organizer_id")
    private User organizer;

    //An attendee can attend many events and each event can also have multiple attendees
    @ManyToMany(mappedBy = "attendingEvents")
    private List<User> attendees = new ArrayList<>();

    @ManyToMany(mappedBy = "staffingEvents")
    private List<User> staff = new ArrayList<>();

    // Paginated organizer listing serializes ticketTypes for many events at once.
    // A fetch-join with Pageable would paginate in memory, so batch-fetch the
    // collection instead: N+1 queries collapse into ~2 batched IN queries.
    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
    @BatchSize(size = 20)
    private List<TicketType> ticketTypes = new ArrayList<>();

    @CreatedDate
    @Column(name="created_at", updatable= false, nullable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name="updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // Identity-based equality: JPA entities must be equal on their persistent
    // identity only. Using mutable business fields breaks Set membership once an
    // entity is mutated, and touching lazy fields here can trigger extra queries.
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Event event)) return false;
        return id != null && id.equals(event.id);
    }

    @Override
    public int hashCode() {
        // Constant so the hash is stable across the transient -> persistent
        // transition (before an id is assigned).
        return getClass().hashCode();
    }
}
