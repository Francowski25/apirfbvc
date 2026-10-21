package com.epiis.apirfbvc.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RequestLaboratoryDetail {
	@NotBlank(message = "El idLaboratory es obligatorio.")
	private String idLaboratory;
}