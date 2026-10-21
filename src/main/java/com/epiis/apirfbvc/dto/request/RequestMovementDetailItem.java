package com.epiis.apirfbvc.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RequestMovementDetailItem {
	@NotBlank(message = "El idProduct es obligatorio.")
	private String idProduct;

	private String idLot;

	@NotNull(message = "La cantidad es obligatoria.")
	private Integer quantity;

	private Double unitCost;

	private String expirationDate;
	private String idSupplier;
}