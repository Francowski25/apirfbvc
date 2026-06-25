package com.epiis.apirfbvc.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.epiis.apirfbvc.business.BusinessPurchase;
import com.epiis.apirfbvc.dto.response.ResponsePurchaseGetAll;

@RestController
@RequestMapping(path = "purchase")
public class PurchaseController {

	private final BusinessPurchase businessPurchase;
	
	public PurchaseController(BusinessPurchase businessPurchase) {
		this.businessPurchase = businessPurchase;
	}

	@GetMapping(path =  "getall")
	public ResponseEntity<ResponsePurchaseGetAll> getAll() {
	    return ResponseEntity.ok(businessPurchase.getAll());
	}
}
