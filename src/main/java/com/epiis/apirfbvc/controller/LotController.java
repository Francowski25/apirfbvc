package com.epiis.apirfbvc.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.epiis.apirfbvc.business.BusinessLot;
import com.epiis.apirfbvc.dto.response.ResponseLotGetAll;

@RestController
@RequestMapping(path = "lot")
public class LotController {
	private final BusinessLot businessLot;
		
	public LotController(BusinessLot businessLot) {
		this.businessLot = businessLot;
	}

	@GetMapping(path = "getall")
	public ResponseEntity<ResponseLotGetAll> getAll() {
	    return ResponseEntity.ok(businessLot.getAll());
	}
	
	@GetMapping(path = "byProduct/{idProduct}")
	public ResponseEntity<ResponseLotGetAll> getByProduct(@PathVariable String idProduct) {
	    return ResponseEntity.ok(businessLot.getByProduct(idProduct));
	}
}
