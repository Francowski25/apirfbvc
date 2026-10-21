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
@Table(name = "tsaledetail")
@Setter
@Getter
public class EntitySaleDetail {
    @Id
    @Column(name = "idSaleDetail")
    private String idSaleDetail;

    @Column(name = "quantity")
    private Integer quantity;
    @Column(name = "unitPrice")
    private BigDecimal unitPrice;
    @Column(name = "subtotal")
    private BigDecimal subtotal;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idSale")
    private EntitySale sale;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idProduct")
    private EntityProduct product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idLot")
    private EntityLot lot;
}