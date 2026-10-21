package com.epiis.apirfbvc.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RequestLaboratoryInsert {
	@NotBlank(message = "El campo \"name\" es requerido.")
    private String name;

    private String image;
}
