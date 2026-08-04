package com.epiis.apirfbvc.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RequestRefreshToken {
	@NotBlank(message = "El refreshToken es obligatorio.")
	private String refreshToken;
}