package com.epiis.apirfbvc.entity;

import java.math.BigDecimal;

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
@Table(name = "tinventorymovementdetail")
@Setter
@Getter
public class EntityInventoryMovementDetail {

    @Id
    @Column(name = "idDetail")
    private String idDetail;

    @Column(name = "quantity")
    private int quantity;

    @Column(name = "unitCost")
    private BigDecimal unitCost;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idMovement")
    private EntityInventoryMovement movement;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idProduct")
    private EntityProduct product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idLot")
    private EntityLot lot;
}