package com.csci318.libraryservice.service;

import com.csci318.libraryservice.domain.aggregateroot.InventoryItem;
import com.csci318.libraryservice.domain.domainevent.InventoryAdjusted;
import com.csci318.libraryservice.domain.domainevent.InventoryCopyReleased;
import com.csci318.libraryservice.domain.domainevent.InventoryCopyReserved;
import com.csci318.libraryservice.domain.domainevent.InventoryInitialized;
import com.csci318.libraryservice.domain.domainevent.InventoryTransferredIn;
import com.csci318.libraryservice.domain.domainevent.InventoryTransferredOut;
import com.csci318.libraryservice.domain.valueobject.Isbn;
import com.csci318.libraryservice.domain.valueobject.LibraryId;
import com.csci318.libraryservice.dto.InventoryAdjustmentRequest;
import com.csci318.libraryservice.dto.InventoryTransferRequest;
import com.csci318.libraryservice.exception.InventoryInvariantViolationException;
import com.csci318.libraryservice.exception.ResourceNotFoundException;
import com.csci318.libraryservice.exception.ValidationFailedException;
import com.csci318.libraryservice.persistence.entity.InventoryEventEntity;
import com.csci318.libraryservice.persistence.entity.InventoryReadModelEntity;
import com.csci318.libraryservice.persistence.repository.InventoryEventRepository;
import com.csci318.libraryservice.persistence.repository.InventoryItemRepository;
import com.csci318.libraryservice.persistence.repository.InventoryReadModelRepository;
import com.csci318.libraryservice.persistence.repository.LibraryRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Transactional
public class InventoryEventSourcingService {

    private final InventoryEventRepository inventoryEventRepository;
    private final InventoryItemRepository inventoryItemRepository;
    private final InventoryReadModelRepository inventoryReadModelRepository;
    private final LibraryRepository libraryRepository;
    private final ObjectMapper objectMapper;
    private final Clock clock = Clock.systemDefaultZone();

    public InventoryEventSourcingService(InventoryEventRepository inventoryEventRepository,
                                         InventoryItemRepository inventoryItemRepository,
                                         InventoryReadModelRepository inventoryReadModelRepository,
                                         LibraryRepository libraryRepository,
                                         ObjectMapper objectMapper) {
        this.inventoryEventRepository = inventoryEventRepository;
        this.inventoryItemRepository = inventoryItemRepository;
        this.inventoryReadModelRepository = inventoryReadModelRepository;
        this.libraryRepository = libraryRepository;
        this.objectMapper = objectMapper;
    }

    public InventoryReadModelEntity reserveCopy(String libraryId, String isbn, String loanId) {
        InventoryItem aggregate = requireInventory(libraryId, isbn);
        InventoryCopyReserved event = aggregate.reserveCopyForLoan(loanId, LocalDateTime.now(clock));
        return persistAndProject(aggregate, event);
    }

    public InventoryReadModelEntity releaseCopy(String libraryId, String isbn, String loanId) {
        InventoryItem aggregate = requireInventory(libraryId, isbn);
        InventoryCopyReleased event = aggregate.releaseCopyFromReturn(loanId, LocalDateTime.now(clock));
        return persistAndProject(aggregate, event);
    }

    public InventoryReadModelEntity adjustInventory(String libraryId, String isbn, InventoryAdjustmentRequest request) {
        LibraryId library = LibraryId.of(libraryId);
        Isbn bookIsbn = Isbn.of(isbn);
        ensureLibraryExists(library);
        if (request.delta() == 0) {
            throw new ValidationFailedException("Inventory adjustment delta must not be zero");
        }

        InventoryItem aggregate = inventoryItemRepository.findById(InventoryItem.inventoryId(library, bookIsbn)).orElse(null);
        if (aggregate == null) {
            if (request.delta() < 0) {
                throw new InventoryInvariantViolationException("Cannot reduce stock for inventory that does not exist");
            }
            aggregate = new InventoryItem(library, bookIsbn);
            InventoryInitialized initialized = aggregate.initialize(0, LocalDateTime.now(clock));
            persistAndProject(aggregate, initialized);
        }
        InventoryAdjusted event = aggregate.adjustStock(request.reasonCode(), request.delta(), request.operatorId(), LocalDateTime.now(clock));
        return persistAndProject(aggregate, event);
    }

    public InventoryTransferResult transferInventory(InventoryTransferRequest request) {
        LibraryId sourceLibraryId = LibraryId.of(request.sourceLibraryId());
        LibraryId targetLibraryId = LibraryId.of(request.targetLibraryId());
        Isbn isbn = Isbn.of(request.isbn());
        ensureLibraryExists(sourceLibraryId);
        ensureLibraryExists(targetLibraryId);
        if (request.sourceLibraryId().equals(request.targetLibraryId())) {
            throw new ValidationFailedException("Source and target libraries must be different");
        }

        InventoryItem source = requireInventory(request.sourceLibraryId(), request.isbn());
        String transferId = "TRF-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        InventoryTransferredOut transferredOut = source.transferOut(request.quantity(), targetLibraryId, transferId, LocalDateTime.now(clock));
        persistAndProject(source, transferredOut);

        InventoryItem target = inventoryItemRepository.findById(InventoryItem.inventoryId(targetLibraryId, isbn))
                .orElseGet(() -> new InventoryItem(targetLibraryId, isbn));
        if (target.getVersion() == 0 && target.getTotalCopies() == 0 && target.getAvailableCopies() == 0) {
            InventoryInitialized initialized = target.initialize(0, LocalDateTime.now(clock));
            persistAndProject(target, initialized);
        }
        InventoryTransferredIn transferredIn = target.transferIn(request.quantity(), sourceLibraryId, transferId, LocalDateTime.now(clock));
        persistAndProject(target, transferredIn);

        return new InventoryTransferResult(transferId, "ACCEPTED");
    }

    public InventoryReadModelEntity applyLossOrDamage(String libraryId, String isbn, String reasonCode, String operatorId) {
        InventoryItem aggregate = requireInventory(libraryId, isbn);
        InventoryAdjusted event = aggregate.adjustStock(reasonCode, -1, operatorId, LocalDateTime.now(clock));
        return persistAndProject(aggregate, event);
    }

    private InventoryItem requireInventory(String libraryId, String isbn) {
        LibraryId library = LibraryId.of(libraryId);
        Isbn bookIsbn = Isbn.of(isbn);
        ensureLibraryExists(library);
        return inventoryItemRepository.findById(InventoryItem.inventoryId(library, bookIsbn))
                .orElseThrow(() -> new ResourceNotFoundException("Book inventory not found for library " + libraryId + " and ISBN " + isbn));
    }

    private void ensureLibraryExists(LibraryId libraryId) {
        if (!libraryRepository.existsById(libraryId)) {
            throw new ResourceNotFoundException("Library " + libraryId.value() + " was not found");
        }
    }

    private InventoryReadModelEntity persistAndProject(InventoryItem aggregate, Object domainEvent) {
        InventoryItem savedAggregate = inventoryItemRepository.save(aggregate);
        InventoryEventEntity event = new InventoryEventEntity(
                UUID.randomUUID().toString(),
                savedAggregate.getInventoryId(),
                "InventoryAggregate",
                domainEvent.getClass().getSimpleName(),
                savedAggregate.getVersion(),
                LocalDateTime.now(clock),
                toJson(domainEvent));
        inventoryEventRepository.save(event);
        InventoryReadModelEntity projection = new InventoryReadModelEntity(
                savedAggregate.getInventoryId(),
                savedAggregate.getLibraryId().value(),
                savedAggregate.getIsbn().value(),
                savedAggregate.getTotalCopies(),
                savedAggregate.getAvailableCopies(),
                savedAggregate.getVersion());
        return inventoryReadModelRepository.save(projection);
    }

    private String toJson(Object payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Failed to serialize inventory event payload", ex);
        }
    }

    public record InventoryTransferResult(String transferId, String status) {
    }
}
