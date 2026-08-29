package com.csci318.libraryservice.persistence.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "inventory_read_model")
public class InventoryReadModelEntity {

    @Id
    private String inventoryId;
    private String libraryId;
    private String isbn;
    private int totalCopies;
    private int availableCopies;
    private long version;

    protected InventoryReadModelEntity() {
    }

    public InventoryReadModelEntity(String inventoryId, String libraryId, String isbn, int totalCopies, int availableCopies, long version) {
        this.inventoryId = inventoryId;
        this.libraryId = libraryId;
        this.isbn = isbn;
        this.totalCopies = totalCopies;
        this.availableCopies = availableCopies;
        this.version = version;
    }

    public String getInventoryId() {
        return inventoryId;
    }

    public String getLibraryId() {
        return libraryId;
    }

    public String getIsbn() {
        return isbn;
    }

    public int getTotalCopies() {
        return totalCopies;
    }

    public void setTotalCopies(int totalCopies) {
        this.totalCopies = totalCopies;
    }

    public int getAvailableCopies() {
        return availableCopies;
    }

    public void setAvailableCopies(int availableCopies) {
        this.availableCopies = availableCopies;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }
}
