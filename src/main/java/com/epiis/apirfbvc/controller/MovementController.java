package com.epiis.apirfbvc.controller;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.epiis.apirfbvc.business.BusinessMovement;
import com.epiis.apirfbvc.dto.request.RequestMovementCreate;
import com.epiis.apirfbvc.dto.request.RequestMovementGetAll;
import com.epiis.apirfbvc.dto.response.ResponseKardex;
import com.epiis.apirfbvc.dto.response.ResponseMovementCreate;
import com.epiis.apirfbvc.dto.response.ResponseMovementDetail;
import com.epiis.apirfbvc.dto.response.ResponseMovementGetAll;
import com.epiis.apirfbvc.dto.response.ResponseProductSearch;

@RestController
@RequestMapping(path = "movement")
public class MovementController {
	private final BusinessMovement businessMovement;

	public MovementController(BusinessMovement businessMovement) {
		this.businessMovement = businessMovement;
	}

	@GetMapping(path = "getall")
	public ResponseEntity<ResponseMovementGetAll> actionGetAll(@ModelAttribute RequestMovementGetAll request) {
		try {
			ResponseMovementGetAll response = businessMovement.getAll(request);
			return ResponseEntity.ok(response);
		} catch (Exception e) {
			ResponseMovementGetAll response = new ResponseMovementGetAll();
			response.exception();
			response.listMessage.add(e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
		}
	}

	@GetMapping(path = "detail/{idMovement}")
	public ResponseEntity<ResponseMovementDetail> actionDetail(@PathVariable String idMovement) {
		try {
			ResponseMovementDetail response = businessMovement.getDetail(idMovement);
			return ResponseEntity.ok(response);
		} catch (Exception e) {
			ResponseMovementDetail response = new ResponseMovementDetail();
			response.exception();
			response.listMessage.add(e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
		}
	}

	@GetMapping(path = "product/{idProduct}")
	public ResponseEntity<ResponseKardex> actionGetKardex(@PathVariable String idProduct) {
		try {
			ResponseKardex response = businessMovement.getKardexByProduct(idProduct);
			return ResponseEntity.ok(response);
		} catch (Exception e) {
			ResponseKardex response = new ResponseKardex();
			response.exception();
			response.listMessage.add(e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
		}
	}

	@GetMapping(path = "search/{q}")
	public ResponseEntity<ResponseProductSearch> actionSearch(@PathVariable String q) {
		try {
			ResponseProductSearch response = businessMovement.search(q);
			return ResponseEntity.ok(response);
		} catch (Exception e) {
			ResponseProductSearch response = new ResponseProductSearch();
			response.exception();
			response.listMessage.add(e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
		}
	}

	@PostMapping(path = "create", consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ResponseMovementCreate> actionCreate(
			@Valid @RequestBody RequestMovementCreate request, BindingResult bindingResult) {
		try {
			ResponseMovementCreate response;

			if (bindingResult.hasErrors()) {
				response = new ResponseMovementCreate();
				bindingResult.getAllErrors()
						.forEach(error -> response.listMessage.add(error.getDefaultMessage()));
				return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
			}

			response = businessMovement.create(request);

			return ResponseEntity.status(HttpStatus.CREATED).body(response);
		} catch (Exception e) {
			ResponseMovementCreate response = new ResponseMovementCreate();
			response.exception();
			response.listMessage.add(e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
		}
	}
}