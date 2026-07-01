package com.epiis.apirfbvc.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RequestCustomerInsert {
    @NotBlank(message = "El campo \"documentType\" es requerido.")
    private String documentType;
    
    @NotBlank(message = "El campo \"documentNumber\" es requerido.")
    private String documentNumber;
   
    @NotBlank(message = "El campo \"name\" es requerido.")
    private String name;
}