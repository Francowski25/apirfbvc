package com.epiis.apirfbvc.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
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
@Table(name = "tlot")
@Getter
@Setter
public class EntityLot {

    @Id
    @Column(name = "idLot")
    private String idLot;

    @Column(name = "code")
    private String code;

    @Column(name = "expirationDate")
    private LocalDate expirationDate;

    @Column(name = "purchasePrice")
    private BigDecimal purchasePrice;

    @Column(name = "currentStock")
    private Integer currentStock;
    
    @Column(name = "createdAt")
    private Date createdAt;

    @Column(name = "updatedAt")
    private Date updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idProduct")
    private EntityProduct product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idSupplier")
    private EntitySupplier supplier;
}