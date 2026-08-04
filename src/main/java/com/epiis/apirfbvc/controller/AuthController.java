package com.epiis.apirfbvc.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.epiis.apirfbvc.business.BusinessAuth;
import com.epiis.apirfbvc.dto.request.RequestLogin;
import com.epiis.apirfbvc.dto.request.RequestRefreshToken;
import com.epiis.apirfbvc.dto.response.ResponseLogin;
import com.epiis.apirfbvc.dto.response.ResponseRefreshToken;

import jakarta.validation.Valid;

@RestController
@RequestMapping(path = "auth")
public class AuthController {
	private final BusinessAuth businessAuth;

	public AuthController(BusinessAuth businessAuth) {
		this.businessAuth = businessAuth;
	}

	@PostMapping(path = "login", consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ResponseLogin> actionLogin(
			@Valid @RequestBody RequestLogin request, BindingResult bindingResult) {
		try {
			if (bindingResult.hasErrors()) {
				ResponseLogin response = new ResponseLogin();
				bindingResult.getAllErrors()
						.forEach(error -> response.listMessage.add(error.getDefaultMessage()));
				return ResponseEntity.badRequest().body(response);
			}

			ResponseLogin response = businessAuth.login(request);
			return ResponseEntity.ok(response);
		} catch (Exception e) {
			ResponseLogin response = new ResponseLogin();
			response.exception();
			response.listMessage.add(e.getMessage());
			return ResponseEntity.internalServerError().body(response);
		}
	}

	@PostMapping(path = "refresh", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<ResponseRefreshToken> actionRefresh(
			@Valid @ModelAttribute RequestRefreshToken request, BindingResult bindingResult) {
		try {
			ResponseRefreshToken response;

			if (bindingResult.hasErrors()) {
				response = new ResponseRefreshToken();
				bindingResult.getAllErrors()
						.forEach(error -> response.listMessage.add(error.getDefaultMessage()));
				return ResponseEntity.badRequest().body(response);
			}

			response = businessAuth.refresh(request);
			return ResponseEntity.ok(response);

		} catch (Exception e) {
			ResponseRefreshToken response = new ResponseRefreshToken();
			response.exception();
			response.listMessage.add(e.getMessage());
			return ResponseEntity.internalServerError().body(response);
		}
	}
}