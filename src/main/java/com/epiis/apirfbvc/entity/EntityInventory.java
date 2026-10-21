package com.epiis.apirfbvc.entity;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "tinventory")
@Setter
@Getter
public class EntityInventory {

    @Id
    @Column(name = "idProduct")
    private String idProduct;

    @OneToOne
    @MapsId
    @JoinColumn(name = "idProduct")
    private EntityProduct product;

    @Column(name = "stock")
    private int stock;

    @Column(name = "lastUpdated")
    private Date lastUpdated;
}