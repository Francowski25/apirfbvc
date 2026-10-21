package com.epiis.apirfbvc.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RequestCategoryDetail {
	@NotBlank(message = "El idCategory es obligatorio.")
	private String idCategory;
}