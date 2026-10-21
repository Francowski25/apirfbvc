package com.epiis.apirfbvc.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.epiis.apirfbvc.business.BusinessCustomer;
import com.epiis.apirfbvc.dto.request.RequestCustomerInsert;
import com.epiis.apirfbvc.dto.response.ResponseCustomerGetAll;
import com.epiis.apirfbvc.dto.response.ResponseCustomerInsert;
import com.epiis.apirfbvc.dto.response.ResponseCustomerReport;

import jakarta.validation.Valid;

@RestController
@RequestMapping(path = "customer")
public class CustomerController {
	private final BusinessCustomer businessCustomer;

	public CustomerController(BusinessCustomer businessCustomer) {
		this.businessCustomer = businessCustomer;
	}

	@PostMapping(path = "insert")
	public ResponseEntity<ResponseCustomerInsert> actionInsert(@Valid @RequestBody RequestCustomerInsert request) {
		try {
			ResponseCustomerInsert response = businessCustomer.insert(request);
			return ResponseEntity.status(HttpStatus.CREATED).body(response);

		} catch (Exception e) {
			ResponseCustomerInsert response = new ResponseCustomerInsert();
			response.exception();
			response.listMessage.add(e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
		}
	}

	@GetMapping(path = "getall")
	public ResponseEntity<ResponseCustomerGetAll> listUsers() {
		return ResponseEntity.ok(businessCustomer.getAll());
	}

	@GetMapping(path = "report/frequent")
	public ResponseEntity<ResponseCustomerReport> getReportFrequent() {
		return ResponseEntity.ok(businessCustomer.getReportFrequent());
	}
}