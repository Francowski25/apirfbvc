package com.epiis.apirfbvc.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;

@RestController
@RequestMapping("/api/reniec")
public class ReniecController {

    private final RestClient restClient;

    @Value("${reniec.token}")
    private String reniecToken;

    public ReniecController() {
        this.restClient = RestClient.builder()
                .baseUrl("https://api.decolecta.com")
                .build();
    }

    @GetMapping("/{dni}")
    public ResponseEntity<?> consultarDni(@PathVariable String dni) {
        try {
            String response = restClient.get()
                    .uri("/v1/reniec/dni?numero={dni}", dni)
                    .header("Authorization", "Bearer " + reniecToken)
                    .retrieve()
                    .body(String.class);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
        	return ResponseEntity.status(HttpStatus.NOT_FOUND).body("DNI no encontrado");        }
    }
}
