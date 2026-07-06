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

import com.epiis.apirfbvc.business.BusinessUser;
import com.epiis.apirfbvc.dto.request.RequestUserInsert;
import com.epiis.apirfbvc.dto.response.ResponseUserDashboardKpi;
import com.epiis.apirfbvc.dto.response.ResponseUserGetAll;
import com.epiis.apirfbvc.dto.response.ResponseUserInsert;
import jakarta.validation.Valid;

@RestController
@RequestMapping(path = "users")
public class UserController {
	private final BusinessUser businessUser;
	
	public UserController(
			BusinessUser businessUser
	) {
		this.businessUser = businessUser;
	}
	
	@PostMapping(path = "insert", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<ResponseUserInsert> actionInsert(@Valid @ModelAttribute RequestUserInsert request, BindingResult bindingResult) {
		try {
			ResponseUserInsert response;
			
			if (bindingResult.hasErrors()) {
				response = new ResponseUserInsert();
				
				bindingResult.getAllErrors()
                .forEach(error -> response.listMessage.add(error.getDefaultMessage()));
				
				return ResponseEntity.ok(response);
			}
			
			response = businessUser.insert(request);
			
			return ResponseEntity.ok(response);
		} catch(Exception e) {
			ResponseUserInsert response = new ResponseUserInsert();
	        response.exception();
	        response.listMessage.add(e.getMessage());
	        return ResponseEntity.ok(response);
		}
	}
	
	@GetMapping(path = "getall")
	public ResponseEntity<ResponseUserGetAll> listUsers() {
		return ResponseEntity.ok(businessUser.getAll());
	}
	
	@PutMapping(path = "status/{id}/{newStatus}")
	public ResponseEntity<ResponseUserInsert> updateStatus(@PathVariable String id, @PathVariable String newStatus) {
	    return ResponseEntity.ok(businessUser.updateUserStatus(id, newStatus));
	}
	
	@GetMapping(path = "dashboard/kpi/{idUser}")
	public ResponseEntity<ResponseUserDashboardKpi> getDashboardKpi(
	        @PathVariable String idUser) {

	    return ResponseEntity.ok(
	            businessUser.getDashboardKpi(idUser));
	}
}