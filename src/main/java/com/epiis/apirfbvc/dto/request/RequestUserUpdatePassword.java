package com.epiis.apirfbvc.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RequestUserUpdatePassword {
	@NotBlank(message = "El idUser es obligatorio.")
	private String idUser;

	@NotBlank(message = "La contraseña es obligatoria.")
	@Size(min = 6, message = "La contraseña debe tener al menos 6 caracteres.")
	private String password;
}