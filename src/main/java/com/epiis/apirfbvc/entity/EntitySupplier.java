package com.epiis.apirfbvc.entity;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "tsupplier")
@Getter
@Setter
public class EntitySupplier {

    @Id
    @Column(name = "idSupplier")
    private String idSupplier;

    @Column(name = "name")
    private String name;

    @Column(name = "ruc")
    private String ruc;

    @Column(name = "phone")
    private String phone;

    @Column(name = "address")
    private String address;

    @Column(name = "email")
    private String email;

    @Column(name = "status")
    private String status;
    
    @Column(name = "createdAt")
    private Date createdAt;

    @Column(name = "updatedAt")
    private Date updatedAt;
}