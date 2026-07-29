package com.epiis.apirfbvc.controller;

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
	final private BusinessLaboratory businessLaboratory;

	public LaboratoryController(BusinessLaboratory businessLaboratory) {
		this.businessLaboratory = businessLaboratory;
	}
	
	@PostMapping(path = "insert", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<ResponseLaboratoryInsert> actionInsert(@Valid @ModelAttribute RequestLaboratoryInsert request, BindingResult bindingResult) {
		try {
			ResponseLaboratoryInsert response;
			
			if (bindingResult.hasErrors()) {
				response = new ResponseLaboratoryInsert();
				
				bindingResult.getAllErrors()
                .forEach(error -> response.listMessage.add(error.getDefaultMessage()));
				
				return ResponseEntity.ok(response);
			}
			
			response = businessLaboratory.insert(request);
			
			return ResponseEntity.ok(response);
		} catch(Exception e) {
			ResponseLaboratoryInsert response = new ResponseLaboratoryInsert();
	        response.exception();
	        response.listMessage.add(e.getMessage());
	        return ResponseEntity.ok(response);
		}
	}
	
	@GetMapping(path = "getall")
	public ResponseEntity<ResponseLaboratoryGetAll> listCategories(){
		return ResponseEntity.ok(businessLaboratory.getAll());
	}

	@GetMapping(path = "detail")
	public ResponseEntity<ResponseLaboratoryDetail> actionDetail(
			@Valid @ModelAttribute RequestLaboratoryDetail request, BindingResult bindingResult) {
		try {
			ResponseLaboratoryDetail response;

			if (bindingResult.hasErrors()) {
				response = new ResponseLaboratoryDetail();
				bindingResult.getAllErrors()
					.forEach(error -> response.listMessage.add(error.getDefaultMessage()));
				return ResponseEntity.ok(response);
			}

			response = businessLaboratory.getDetail(request);

			return ResponseEntity.ok(response);
		} catch (Exception e) {
			ResponseLaboratoryDetail response = new ResponseLaboratoryDetail();
			response.exception();
			response.listMessage.add(e.getMessage());
			return ResponseEntity.ok(response);
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
			return ResponseEntity.ok(response);
		}
	}
	
}
