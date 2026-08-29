package com.csci318.libraryservice.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "inventory_events")
public class InventoryEventEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true, nullable = false)
    private String eventId;
    private String aggregateId;
    private String aggregateType;
    private String eventType;
    private long aggregateVersion;
    private LocalDateTime occurredAt;
    @Lob
    @Column(columnDefinition = "CLOB")
    private String payload;

    protected InventoryEventEntity() {
    }

    public InventoryEventEntity(String eventId, String aggregateId, String aggregateType, String eventType,
                                long aggregateVersion, LocalDateTime occurredAt, String payload) {
        this.eventId = eventId;
        this.aggregateId = aggregateId;
        this.aggregateType = aggregateType;
        this.eventType = eventType;
        this.aggregateVersion = aggregateVersion;
        this.occurredAt = occurredAt;
        this.payload = payload;
    }
}
