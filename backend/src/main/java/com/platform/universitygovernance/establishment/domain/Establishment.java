package com.platform.universitygovernance.establishment.domain;

import java.time.Instant;
import java.util.UUID;

import com.platform.universitygovernance.university.domain.University;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name="establishment")
public class Establishment {

    @Id
    private  UUID id;

    @ManyToOne(optional = false)
    @JoinColumn(name="university_id", nullable = false)
    private  University university;

    @Column(name="name" , nullable = false)
    private  String name;


    @Enumerated(EnumType.STRING)
    @Column(name="type" , nullable = false)
    private EstablishmentType establishmentType;
    

    @Enumerated(EnumType.STRING)
    @Column(name="status" , nullable = false)
    private EstablishmentStatus establishmentStatus;

    @Column(name="created_at" , nullable = false , updatable = false)
    private Instant createdAt;
    
    @Column(name="updated_at" , nullable = false )
    private Instant updatedAt;


    public Establishment(){}

    @PrePersist
    void onCreate(){
        Instant now= Instant.now();
        if(id==null){
            id=UUID.randomUUID();
        }

        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    } 

    public void setName(String name){ this.name = name;}
    public void setEstablishmentType(EstablishmentType establishmentType){ this.establishmentType=establishmentType;}
    public void setUniversity(University university){ this.university=university;}
    public void setEstablishmentStatus(EstablishmentStatus establishmentStatus){this.establishmentStatus=establishmentStatus;}

    public UUID getId(){return this.id;}
    public String getName(){ return this.name;}
    public University getUniversity(){return this.university;}
    public EstablishmentStatus getEstablishmentStatus(){return this.establishmentStatus;}
    public EstablishmentType getEstablishmentType(){return this.establishmentType;}
    public Instant getCreatedAt(){ return this.createdAt;}
    public Instant getUpdatedAt(){ return this.updatedAt;}


}
