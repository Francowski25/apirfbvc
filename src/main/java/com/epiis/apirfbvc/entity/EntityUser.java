package com.epiis.apirfbvc.entity;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "tuser")
@Setter
@Getter
public class EntityUser {

    @Id
    @Column(name = "idUser")
    private String idUser;
    
    @Column(name = "image")
	private String image;

    @Column(name = "dni")
	private String dni;
    
    @Column(name = "firstName")
    private String firstName;

    @Column(name = "surName")
    private String surName;

    @Column(name = "email")
    private String email;
    
    @Column(name = "cellPhone")
    private String cellPhone;

    @Column(name = "password")
    private String password;

    @Column(name = "role")
    private String role;

    @Column(name = "status")
    private String status;    

    @Column(name = "createdAt")
    private Date createdAt;

    @Column(name = "updatedAt")
    private Date updatedAt;
}