package com.epiis.apirfbvc.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.epiis.apirfbvc.business.BusinessPurchase;
import com.epiis.apirfbvc.dto.response.ResponsePurchaseGetAll;
import com.epiis.apirfbvc.dto.response.ResponsePurchaseRecent;
import com.epiis.apirfbvc.dto.response.ResponsePurchaseReport;

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
	
	@GetMapping(path = "recent")
    public ResponseEntity<ResponsePurchaseRecent> getRecent(@RequestParam(defaultValue = "5") int limit) {
        return ResponseEntity.ok(businessPurchase.getRecent(limit));
    }
	
	@GetMapping(path = "report/{from}/{to}")
	public ResponseEntity<ResponsePurchaseReport> getReport(
			@PathVariable String from,
			@PathVariable String to) {
	    return ResponseEntity.ok(businessPurchase.getReport(from, to));
	}

	@GetMapping(path = "report/by-supplier/{from}/{to}")
	public ResponseEntity<ResponsePurchaseReport> getReportBySupplier(
	        @PathVariable String from,
	        @PathVariable String to) {
	    return ResponseEntity.ok(businessPurchase.getReportBySupplier(from, to));
	}
}
