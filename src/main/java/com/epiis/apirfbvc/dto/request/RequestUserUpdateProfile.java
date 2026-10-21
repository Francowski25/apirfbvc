package com.epiis.apirfbvc.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RequestUserUpdateProfile {
	@NotBlank(message = "El idUser es obligatorio.")
	private String idUser;

	private String image;

	@NotBlank(message = "El nombre es obligatorio.")
	private String firstName;

	@NotBlank(message = "El apellido es obligatorio.")
	private String surName;

	@NotBlank(message = "El correo es obligatorio.")
	@Email(message = "El correo no tiene un formato válido.")
	private String email;

	private String cellPhone;
}