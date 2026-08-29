package com.csci318.libraryservice.domain.valueobject;

import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class Isbn implements Serializable {

    private String value;

    protected Isbn() {
    }

    public Isbn(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("ISBN is required");
        }
        this.value = value;
    }

    public static Isbn of(String value) {
        return new Isbn(value);
    }

    public String value() {
        return value;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Isbn isbn)) {
            return false;
        }
        return Objects.equals(value, isbn.value);
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
