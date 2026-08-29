package com.csci318.libraryservice.domain.valueobject;

import jakarta.persistence.Embeddable;

@Embeddable
public class Contact {

    private String email;
    private String phone;

    protected Contact() {
    }

    public Contact(String email, String phone) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Customer email is required");
        }
        if (phone == null || phone.isBlank()) {
            throw new IllegalArgumentException("Customer phone is required");
        }
        this.email = email;
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }
}
