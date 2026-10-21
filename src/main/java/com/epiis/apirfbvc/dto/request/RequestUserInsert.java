package com.epiis.apirfbvc.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RequestUserInsert {

    @NotBlank(message = "El campo \"firstName\" es requerido.")
    private String firstName;

    @NotBlank(message = "El campo \"surName\" es requerido.")
    private String surName;
    
    @NotBlank(message = "El campo \"dni\" es requerido.")
    private String dni;
    
    @NotBlank(message = "El campo \"cellPhone\" es requerido.")
    private String cellPhone;

    @NotBlank(message = "El campo \"email\" es requerido.")
    @Email(message = "El correo electrónico no es válido.")
    private String email;

    @NotBlank(message = "El campo \"password\" es requerido.")
    private String password;

    @NotBlank(message = "El campo \"role\" es requerido.")
    private String role;

    private String image;
}