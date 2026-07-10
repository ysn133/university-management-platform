package com.platform.universitygovernance.university.domain;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name="university")
public class University {

    @Id
    private UUID id;

    @Column(name="name" , nullable=false , unique=true)
    private String name;

    @Column(name="created_at" , nullable=false , updatable = false)
    private Instant createdAt;

    @Column(name="updated_at" , nullable=false)
    private Instant updatedAt;

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

    public void setName(String name){
        this.name = name;
    }


    public UUID getId(){ return this.id;}
    public String getName(){ return this.name;}
    public Instant getUpdatedAt(){ return this.updatedAt;}
    public Instant getCreatedAt(){return this.createdAt;}


    

    
}
