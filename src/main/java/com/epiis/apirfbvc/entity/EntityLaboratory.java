package com.epiis.apirfbvc.entity;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "tlaboratory")
@Getter
@Setter
public class EntityLaboratory {
    @Id
    @Column(name = "idLaboratory")
    private String idLaboratory;

    @Column(name = "image")
    private String image;

    @Column(name = "name")
    private String name;
    
    @Column(name = "status")
    private String status;
    
    @Column(name = "createdAt")
    private Date createdAt;

    @Column(name = "updatedAt")
    private Date updatedAt;
}
