package com.csci318.libraryservice.domain.valueobject;

import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class LibraryId implements Serializable {

    private String value;

    protected LibraryId() {
    }

    public LibraryId(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Library ID is required");
        }
        this.value = value;
    }

    public static LibraryId of(String value) {
        return new LibraryId(value);
    }

    public String value() {
        return value;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof LibraryId libraryId)) {
            return false;
        }
        return Objects.equals(value, libraryId.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
