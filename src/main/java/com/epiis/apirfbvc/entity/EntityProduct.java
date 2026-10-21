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
@Table(name = "tproduct")
@Setter
@Getter
public class EntityProduct {
	@Id
    @Column(name = "idProduct")
	private String idProduct;
	
    @Column(name = "name")
    private String name;
    
    @Column(name = "description")
    private String description;
    
    @Column(name = "barcode")
    private String barcode;
    
    @Column(name = "image")
	private String image;
    
    @Column(name = "priceSale")
    private BigDecimal priceSale;
    
    @Column(name = "requiresPrescription")
    private boolean requiresPrescription;
    
    @Column(name = "stockMinimum")
    private int stockMinimum;
    
    @Column(name = "status")
    private String status;
    
    @Column(name = "createdAt")
    private Date createdAt;

    @Column(name = "updatedAt")
    private Date updatedAt;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idCategory")
    private EntityCategory category;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idLaboratory")
    private EntityLaboratory laboratory;
}
