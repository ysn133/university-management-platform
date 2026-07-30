package com.platform.identityaccess.domain;

import com.platform.universitygovernance.establishment.domain.Establishment;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "student")
public class Student {

    @Id
    private UUID id;

    @OneToOne(optional = false)
    @JoinColumn(name = "user_account_id", nullable = false, unique = true)
    private UserAccount userAccount;

    @ManyToOne(optional = false)
    @JoinColumn(name = "establishment_id", nullable = false)
    private Establishment establishment;

    @Column(name = "apogee_code", nullable = false, unique = true, length = 50)
    private String apogeeCode;

    @Column(name = "national_student_code", unique = true, length = 50)
    private String nationalStudentCode;

    @Column(name = "initial_enrollment_date")
    private LocalDate initialEnrollmentDate;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public Student() {
    }

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        if (id == null) {
            id = UUID.randomUUID();
        }
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public UserAccount getUserAccount() {
        return userAccount;
    }

    public void setUserAccount(UserAccount userAccount) {
        this.userAccount = userAccount;
    }

    public Establishment getEstablishment() {
        return establishment;
    }

    public void setEstablishment(Establishment establishment) {
        this.establishment = establishment;
    }

    public String getApogeeCode() {
        return apogeeCode;
    }

    public void setApogeeCode(String apogeeCode) {
        this.apogeeCode = apogeeCode;
    }

    public String getNationalStudentCode() {
        return nationalStudentCode;
    }

    public void setNationalStudentCode(String nationalStudentCode) {
        this.nationalStudentCode = nationalStudentCode;
    }

    public LocalDate getInitialEnrollmentDate() {
        return initialEnrollmentDate;
    }

    public void setInitialEnrollmentDate(LocalDate initialEnrollmentDate) {
        this.initialEnrollmentDate = initialEnrollmentDate;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
