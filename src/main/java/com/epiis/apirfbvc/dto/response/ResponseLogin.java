package com.epiis.apirfbvc.dto.response;

import java.util.Date;

import com.epiis.apirfbvc.generic.ResponseGeneric;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResponseLogin extends ResponseGeneric {
    private String idUser;
	private String token;
	private String refreshToken;
    private String firstName;
    private String surName;
    private String cellPhone;
    private String dni;
    private String image;
    private String email;
    private String password;
    private String role;
    private String status;    
    private Date createdAt;
    private Date updatedAt;
}