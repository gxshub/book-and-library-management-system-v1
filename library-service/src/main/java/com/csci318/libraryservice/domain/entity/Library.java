package com.csci318.libraryservice.domain.entity;

import com.csci318.libraryservice.domain.valueobject.LibraryId;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "libraries")
public class Library {

    @EmbeddedId
    @AttributeOverride(name = "value", column = @Column(name = "id"))
    private LibraryId id;
    private String name;
    private String location;

    protected Library() {
    }

    public Library(LibraryId id, String name, String location) {
        this.id = id;
        this.name = name;
        this.location = location;
    }

    public Library(String id, String name, String location) {
        this(LibraryId.of(id), name, location);
    }

    public LibraryId getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getLocation() {
        return location;
    }
}
