package ru.finess.finess.identity.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public record UserFullName(
    @Column(name = "first_name", nullable = false) String firstName,
    @Column(name = "last_name", nullable = false) String lastName,
    @Column(name = "middle_name") String middleName) {}
