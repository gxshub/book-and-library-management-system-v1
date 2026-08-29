package com.csci318.libraryservice.domain.valueobject;

import jakarta.persistence.Embeddable;

@Embeddable
public class CopyCount {

    private int value;

    protected CopyCount() {
    }

    public CopyCount(int value) {
        if (value < 0) {
            throw new IllegalArgumentException("Copy count must be non-negative");
        }
        this.value = value;
    }

    public int value() {
        return value;
    }

    public CopyCount plus(int delta) {
        return new CopyCount(value + delta);
    }

    public CopyCount minus(int delta) {
        return new CopyCount(value - delta);
    }
}
