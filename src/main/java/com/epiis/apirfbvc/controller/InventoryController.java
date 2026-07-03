package com.epiis.apirfbvc.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.epiis.apirfbvc.business.BusinessInventory;
import com.epiis.apirfbvc.dto.response.ResponseInventoryIncome;
import com.epiis.apirfbvc.dto.response.ResponseInventoryReport;

@RestController
@RequestMapping(path = "inventory")
public class InventoryController {
	private final BusinessInventory businessInventory;

	public InventoryController(BusinessInventory businessInventory) {
		this.businessInventory = businessInventory;
	}
	
	@GetMapping(path = "getall")
	public ResponseEntity<ResponseInventoryIncome> listIncome(){
		return ResponseEntity.ok(businessInventory.getIncomes());
	}
	
	@GetMapping(path = "report/movements/{from}/{to}")
	public ResponseEntity<ResponseInventoryReport> getReportMovements(
	        @PathVariable String from,
	        @PathVariable String to) {
	    return ResponseEntity.ok(businessInventory.getReportMovements(from, to));
	}

	@GetMapping(path = "report/low-stock")
	public ResponseEntity<ResponseInventoryReport> getReportLowStock() {
	    return ResponseEntity.ok(businessInventory.getReportLowStock());
	}

	@GetMapping(path = "report/expiring")
	public ResponseEntity<ResponseInventoryReport> getReportExpiring() {
	    return ResponseEntity.ok(businessInventory.getReportExpiring());
	}
}
