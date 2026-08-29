package com.csci318.libraryservice.domain.entity;

import com.csci318.libraryservice.domain.valueobject.Contact;
import com.csci318.libraryservice.domain.valueobject.CustomerId;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "customers")
public class Customer {

    @EmbeddedId
    @AttributeOverride(name = "value", column = @Column(name = "id"))
    private CustomerId id;
    private String name;

    @Embedded
    private Contact contact;

    protected Customer() {
    }

    public Customer(CustomerId id, String name, Contact contact) {
        this.id = id;
        this.name = name;
        this.contact = contact;
    }

    public Customer(String id, String name, String email, String phone) {
        this(CustomerId.of(id), name, new Contact(email, phone));
    }

    public CustomerId getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Contact getContact() {
        return contact;
    }
}
