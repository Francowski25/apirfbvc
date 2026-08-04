package com.epiis.apirfbvc.dto.request;

import java.util.List;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RequestMovementCreate {
	@NotBlank(message = "El type es obligatorio.")
	private String type;

	private String observation;

	@NotBlank(message = "El idUser es obligatorio.")
	private String idUser;

	@NotEmpty(message = "Debe incluir al menos un producto en el movimiento.")
	private List<RequestMovementDetailItem> details;
}