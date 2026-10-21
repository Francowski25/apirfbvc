package com.epiis.apirfbvc.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.epiis.apirfbvc.business.BusinessLaboratory;
import com.epiis.apirfbvc.dto.request.RequestLaboratoryDetail;
import com.epiis.apirfbvc.dto.request.RequestLaboratoryInsert;
import com.epiis.apirfbvc.dto.response.ResponseLaboratoryDetail;
import com.epiis.apirfbvc.dto.response.ResponseLaboratoryGetAll;
import com.epiis.apirfbvc.dto.response.ResponseLaboratoryInsert;
import com.epiis.apirfbvc.dto.response.ResponseLaboratoryStatus;

import jakarta.validation.Valid;

@RestController
@RequestMapping(path = "laboratories")
public class LaboratoryController {

	private final BusinessLaboratory businessLaboratory;

	public LaboratoryController(BusinessLaboratory businessLaboratory) {
		this.businessLaboratory = businessLaboratory;
	}

	@PostMapping(path = "insert", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<ResponseLaboratoryInsert> actionInsert(
			@Valid @ModelAttribute RequestLaboratoryInsert request, BindingResult bindingResult) {
		try {
			if (bindingResult.hasErrors()) {
				ResponseLaboratoryInsert response = new ResponseLaboratoryInsert();
				bindingResult.getAllErrors()
						.forEach(error -> response.listMessage.add(error.getDefaultMessage()));
				return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
			}

			ResponseLaboratoryInsert response = businessLaboratory.insert(request);
			return ResponseEntity.status(HttpStatus.CREATED).body(response);

		} catch (Exception e) {
			ResponseLaboratoryInsert response = new ResponseLaboratoryInsert();
			response.exception();
			response.listMessage.add(e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
		}
	}

	@GetMapping(path = "getall")
	public ResponseEntity<ResponseLaboratoryGetAll> listCategories() {
		try {
			ResponseLaboratoryGetAll response = businessLaboratory.getAll();
			return ResponseEntity.ok(response);
		} catch (Exception e) {
			ResponseLaboratoryGetAll response = new ResponseLaboratoryGetAll();
			response.exception();
			response.listMessage.add(e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
		}
	}

	@GetMapping(path = "detail")
	public ResponseEntity<ResponseLaboratoryDetail> actionDetail(
			@Valid @ModelAttribute RequestLaboratoryDetail request, BindingResult bindingResult) {
		try {
			if (bindingResult.hasErrors()) {
				ResponseLaboratoryDetail response = new ResponseLaboratoryDetail();
				bindingResult.getAllErrors()
						.forEach(error -> response.listMessage.add(error.getDefaultMessage()));
				return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
			}

			ResponseLaboratoryDetail response = businessLaboratory.getDetail(request);
			return ResponseEntity.ok(response);

		} catch (Exception e) {
			ResponseLaboratoryDetail response = new ResponseLaboratoryDetail();
			response.exception();
			response.listMessage.add(e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
		}
	}

	@PutMapping(path = "status/{id}/{newStatus}")
	public ResponseEntity<ResponseLaboratoryStatus> actionUpdateStatus(
			@PathVariable String id, @PathVariable String newStatus) {
		try {
			ResponseLaboratoryStatus response = businessLaboratory.updateLaboratoryStatus(id, newStatus);
			return ResponseEntity.ok(response);
		} catch (Exception e) {
			ResponseLaboratoryStatus response = new ResponseLaboratoryStatus();
			response.exception();
			response.listMessage.add(e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
		}
	}
}