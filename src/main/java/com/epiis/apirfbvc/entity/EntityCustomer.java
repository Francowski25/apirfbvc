package com.epiis.apirfbvc.entity;

import java.util.Date;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "tcustomer")
@Setter
@Getter
public class EntityCustomer {
    @Id
    @Column(name = "idCustomer")
    private String idCustomer;

    @Column(name = "documentType")
    private String documentType;
    
    @Column(name = "documentNumber")
    private String documentNumber;

    @Column(name = "name")
    private String name;

    @Column(name = "createdAt")
    private Date createdAt;
}