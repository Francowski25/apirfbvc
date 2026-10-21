package com.epiis.apirfbvc.entity;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "tinventorymovement")
@Setter
@Getter
public class EntityInventoryMovement {

    @Id
    @Column(name = "idMovement")
    private String idMovement;

    @Column(name = "type")
    private String type;

    @Column(name = "observation")
    private String observation;

    @Column(name = "movementDate")
    private Date movementDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idUser")
    private EntityUser user;
}