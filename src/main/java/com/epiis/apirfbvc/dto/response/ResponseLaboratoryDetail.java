package com.epiis.apirfbvc.dto.response;

import com.epiis.apirfbvc.generic.ResponseGeneric;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResponseLaboratoryDetail extends ResponseGeneric {
	private String idLaboratory;
	private String image;
	private String name;
	private String status;
	private String totalProducts;
	private String createdAt;
	private String updatedAt;
}