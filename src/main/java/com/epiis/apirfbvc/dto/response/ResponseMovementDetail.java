package com.epiis.apirfbvc.dto.response;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import com.epiis.apirfbvc.generic.ResponseGeneric;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResponseMovementDetail extends ResponseGeneric {
	private String idMovement;
	private String type;
	private String movementDate;
	private String observation;
	private String userName;
	private String role;
	private String dni;
	private String costoTotal;
	private List<Map<String, Object>> listDetail = new ArrayList<>();
}