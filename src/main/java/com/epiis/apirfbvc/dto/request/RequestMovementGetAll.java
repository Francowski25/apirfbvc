package com.epiis.apirfbvc.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RequestMovementGetAll {
	private String type;       
	private String dateFrom;
	private String dateTo;
	private Integer page;
	private Integer limit;
}