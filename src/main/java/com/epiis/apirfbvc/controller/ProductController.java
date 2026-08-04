package com.epiis.apirfbvc.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.epiis.apirfbvc.business.BusinessProduct;
import com.epiis.apirfbvc.dto.request.RequestProductInsert;
import com.epiis.apirfbvc.dto.response.ResponseProductGetAll;
import com.epiis.apirfbvc.dto.response.ResponseProductInsert;
import com.epiis.apirfbvc.dto.response.ResponseProductStockAlert;

import jakarta.validation.Valid;

@RestController
@RequestMapping(path = "products")
public class ProductController {
	private final BusinessProduct businessProduct;

	public ProductController(BusinessProduct businessProduct) {
		this.businessProduct = businessProduct;
	}

	@PostMapping(path = "insert", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<ResponseProductInsert> actionInsert(@Valid @ModelAttribute RequestProductInsert request,
			BindingResult bindingResult) {
		try {
			ResponseProductInsert response;

			if (bindingResult.hasErrors()) {
				response = new ResponseProductInsert();

				bindingResult.getAllErrors()
						.forEach(error -> response.listMessage.add(error.getDefaultMessage()));

				return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
			}

			response = businessProduct.insert(request);

			return ResponseEntity.status(HttpStatus.CREATED).body(response);
		} catch (Exception e) {
			ResponseProductInsert response = new ResponseProductInsert();
			response.exception();
			response.listMessage.add(e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
		}
	}

	@GetMapping(path = "getall")
	public ResponseEntity<ResponseProductGetAll> listProduct() {
		return ResponseEntity.ok(businessProduct.getAll());
	}

	@GetMapping(path = "stock-alert")
	public ResponseEntity<ResponseProductStockAlert> getStockAlert() {
		return ResponseEntity.ok(businessProduct.getStockAlert());
	}
}