package com.csci318.libraryservice.domain.aggregateroot;

import com.csci318.libraryservice.domain.domainevent.InventoryAdjusted;
import com.csci318.libraryservice.domain.domainevent.InventoryCopyReleased;
import com.csci318.libraryservice.domain.domainevent.InventoryCopyReserved;
import com.csci318.libraryservice.domain.domainevent.InventoryInitialized;
import com.csci318.libraryservice.domain.domainevent.InventoryTransferredIn;
import com.csci318.libraryservice.domain.domainevent.InventoryTransferredOut;
import com.csci318.libraryservice.domain.valueobject.CopyCount;
import com.csci318.libraryservice.domain.valueobject.Isbn;
import com.csci318.libraryservice.domain.valueobject.LibraryId;
import com.csci318.libraryservice.exception.InventoryInvariantViolationException;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.time.LocalDateTime;

@Entity
@Table(name = "inventory_items")
public class InventoryItem {

    @Id
    private String inventoryId;

    @Embedded
    @AttributeOverride(name = "value", column = @jakarta.persistence.Column(name = "library_id"))
    private LibraryId libraryId;

    @Embedded
    @AttributeOverride(name = "value", column = @jakarta.persistence.Column(name = "isbn"))
    private Isbn isbn;

    @Embedded
    @AttributeOverride(name = "value", column = @jakarta.persistence.Column(name = "total_copies"))
    private CopyCount totalCopies;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "value", column = @jakarta.persistence.Column(name = "available_copies"))
    })
    private CopyCount availableCopies;

    @Version
    private long version;

    protected InventoryItem() {
    }

    public InventoryItem(LibraryId libraryId, Isbn isbn) {
        this.inventoryId = inventoryId(libraryId, isbn);
        this.libraryId = libraryId;
        this.isbn = isbn;
        this.totalCopies = new CopyCount(0);
        this.availableCopies = new CopyCount(0);
    }

    public static String inventoryId(LibraryId libraryId, Isbn isbn) {
        return libraryId.value() + ":" + isbn.value();
    }

    public InventoryInitialized initialize(int totalCopies, LocalDateTime occurredAt) {
        this.totalCopies = new CopyCount(totalCopies);
        this.availableCopies = new CopyCount(totalCopies);
        return new InventoryInitialized(libraryId, isbn, totalCopies, totalCopies, occurredAt);
    }

    public InventoryCopyReserved reserveCopyForLoan(String loanId, LocalDateTime occurredAt) {
        if (availableCopies.value() <= 0) {
            throw new InventoryInvariantViolationException("No copies available for ISBN " + isbn.value() + " in library " + libraryId.value());
        }
        availableCopies = availableCopies.minus(1);
        return new InventoryCopyReserved(libraryId, isbn, loanId, occurredAt);
    }

    public InventoryCopyReleased releaseCopyFromReturn(String loanId, LocalDateTime occurredAt) {
        if (availableCopies.value() >= totalCopies.value()) {
            throw new InventoryInvariantViolationException("Inventory has no borrowed copy to release for ISBN " + isbn.value());
        }
        availableCopies = availableCopies.plus(1);
        return new InventoryCopyReleased(libraryId, isbn, loanId, occurredAt);
    }

    public InventoryAdjusted adjustStock(String reasonCode, int delta, String operatorId, LocalDateTime occurredAt) {
        int borrowedCopies = totalCopies.value() - availableCopies.value();
        int newTotal = totalCopies.value() + delta;
        if (newTotal < 0 || newTotal < borrowedCopies) {
            throw new InventoryInvariantViolationException("Inventory adjustment would violate copy-count invariants");
        }
        totalCopies = new CopyCount(newTotal);
        availableCopies = new CopyCount(newTotal - borrowedCopies);
        return new InventoryAdjusted(libraryId, isbn, delta, reasonCode, operatorId, occurredAt);
    }

    public InventoryTransferredOut transferOut(int quantity, LibraryId targetLibraryId, String transferId, LocalDateTime occurredAt) {
        if (quantity <= 0 || availableCopies.value() < quantity) {
            throw new InventoryInvariantViolationException("Source library does not have enough transferable copies");
        }
        totalCopies = totalCopies.minus(quantity);
        availableCopies = availableCopies.minus(quantity);
        return new InventoryTransferredOut(transferId, libraryId, targetLibraryId, isbn, quantity, occurredAt);
    }

    public InventoryTransferredIn transferIn(int quantity, LibraryId sourceLibraryId, String transferId, LocalDateTime occurredAt) {
        if (quantity <= 0) {
            throw new InventoryInvariantViolationException("Transfer quantity must be positive");
        }
        totalCopies = totalCopies.plus(quantity);
        availableCopies = availableCopies.plus(quantity);
        return new InventoryTransferredIn(transferId, sourceLibraryId, libraryId, isbn, quantity, occurredAt);
    }

    public String getInventoryId() {
        return inventoryId;
    }

    public LibraryId getLibraryId() {
        return libraryId;
    }

    public Isbn getIsbn() {
        return isbn;
    }

    public int getTotalCopies() {
        return totalCopies.value();
    }

    public int getAvailableCopies() {
        return availableCopies.value();
    }

    public long getVersion() {
        return version;
    }
}
