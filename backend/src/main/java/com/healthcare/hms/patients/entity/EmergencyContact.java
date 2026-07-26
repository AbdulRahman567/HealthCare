package com.healthcare.hms.patients.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.Size;

/**
 * Next-of-kin / emergency contact stored on the patient registration row.
 *
 * <p>Embedded (not a separate table) for Phase 5.1 — one primary contact is
 * sufficient for registration. Additional contacts can become a child table later
 * without changing the Patient aggregate root.
 */
@Embeddable
public class EmergencyContact {

    @Size(max = 150)
    @Column(name = "emergency_contact_name", length = 150)
    private String name;

    @Size(max = 30)
    @Column(name = "emergency_contact_phone", length = 30)
    private String phone;

    @Size(max = 50)
    @Column(name = "emergency_contact_relation", length = 50)
    private String relation;

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(final String phone) {
        this.phone = phone;
    }

    public String getRelation() {
        return relation;
    }

    public void setRelation(final String relation) {
        this.relation = relation;
    }
}
