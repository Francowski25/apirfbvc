package com.epiis.apirfbvc.entity;

import java.math.BigDecimal;
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
@Table(name = "tsale")
@Setter
@Getter
public class EntitySale {
    @Id
    @Column(name = "idSale")
    private String idSale;

    @Column(name = "saleNumber")
    private String saleNumber;
    @Column(name = "saleDate")
    private Date saleDate;

    @Column(name = "subtotal")
    private BigDecimal subtotal;
    @Column(name = "discount")
    private BigDecimal discount;
    @Column(name = "igv")
    private BigDecimal igv;
    @Column(name = "total")
    private BigDecimal total;

    @Column(name = "paymentMethod")
    private String paymentMethod;
    @Column(name = "status")
    private String status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idUser")
    private EntityUser user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idCustomer")
    private EntityCustomer customer;

    @Column(name = "createdAt")
    private Date createdAt;
    @Column(name = "updatedAt")
    private Date updatedAt;
}