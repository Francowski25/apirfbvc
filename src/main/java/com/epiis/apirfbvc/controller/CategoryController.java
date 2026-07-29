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

import com.epiis.apirfbvc.business.BusinessCategory;
import com.epiis.apirfbvc.dto.request.RequestCategoryDetail;
import com.epiis.apirfbvc.dto.request.RequestCategoryDisable;
import com.epiis.apirfbvc.dto.request.RequestCategoryInsert;
import com.epiis.apirfbvc.dto.response.ResponseCategoryDetail;
import com.epiis.apirfbvc.dto.response.ResponseCategoryDisable;
import com.epiis.apirfbvc.dto.response.ResponseCategoryGetAll;
import com.epiis.apirfbvc.dto.response.ResponseCategoryInsert;

import jakarta.validation.Valid;

@RestController
@RequestMapping(path = "categories")
public class CategoryController {
	private final BusinessCategory businessCategory;

	public CategoryController(BusinessCategory businessCategory) {
		this.businessCategory = businessCategory;
	}
	
	@PostMapping(path = "insert", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<ResponseCategoryInsert> actionInsert(@Valid @ModelAttribute RequestCategoryInsert request, BindingResult bindingResult) {
		try {
			ResponseCategoryInsert response;
			
			if (bindingResult.hasErrors()) {
				response = new ResponseCategoryInsert();
				
				bindingResult.getAllErrors()
                .forEach(error -> response.listMessage.add(error.getDefaultMessage()));
				
				return ResponseEntity.ok(response);
			}
			
			response = businessCategory.insert(request);
			
			return ResponseEntity.ok(response);
		} catch(Exception e) {
			ResponseCategoryInsert response = new ResponseCategoryInsert();
	        response.exception();
	        response.listMessage.add(e.getMessage());
	        return ResponseEntity.ok(response);
		}
	}
	
	@GetMapping(path = "getall")
	public ResponseEntity<ResponseCategoryGetAll> listCategories(){
		return ResponseEntity.ok(businessCategory.getAll());
	}

	@PutMapping(path = "status/{id}/{newStatus}")
	public ResponseEntity<ResponseCategoryDisable> actionUpdateStatus(
	        @PathVariable String id, @PathVariable String newStatus) {
	    try {
	        ResponseCategoryDisable response = businessCategory.updateCategoryStatus(id, newStatus);
	        return ResponseEntity.ok(response);
	    } catch (Exception e) {
	        ResponseCategoryDisable response = new ResponseCategoryDisable();
	        response.exception();
	        response.listMessage.add(e.getMessage());
	        return ResponseEntity.ok(response);
	    }
	}

	@GetMapping(path = "detail")
	public ResponseEntity<ResponseCategoryDetail> actionDetail(
	        @Valid @ModelAttribute RequestCategoryDetail request, BindingResult bindingResult) {
	    try {
	        ResponseCategoryDetail response;

	        if (bindingResult.hasErrors()) {
	            response = new ResponseCategoryDetail();
	            bindingResult.getAllErrors()
	                .forEach(error -> response.listMessage.add(error.getDefaultMessage()));
	            return ResponseEntity.ok(response);
	        }

	        response = businessCategory.getDetail(request);

	        return ResponseEntity.ok(response);
	    } catch (Exception e) {
	        ResponseCategoryDetail response = new ResponseCategoryDetail();
	        response.exception();
	        response.listMessage.add(e.getMessage());
	        return ResponseEntity.ok(response);
	    }
	}
}
