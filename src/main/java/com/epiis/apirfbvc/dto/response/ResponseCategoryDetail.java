package com.epiis.apirfbvc.dto.response;

import com.epiis.apirfbvc.generic.ResponseGeneric;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResponseCategoryDetail extends ResponseGeneric {
	private String idCategory;
	private String image;
	private String name;
	private String status;
	private String totalProducts;
	private String createdAt;
	private String updatedAt;
}