package com.epiis.apirfbvc.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

@RestController
@RequestMapping("/api/reniec")
public class ReniecController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReniecController.class);
    private static final String AUTH_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final RestClient restClient;

    @Value("${reniec.token}")
    private String reniecToken;

    public ReniecController() {
        this.restClient = RestClient.builder()
                .baseUrl("https://api.decolecta.com")
                .build();
    }

    @GetMapping("/{dni}")
    public ResponseEntity<String> consultarDni(@PathVariable String dni) {
        try {
            String response = restClient.get()
                    .uri("/v1/reniec/dni?numero={dni}", dni)
                    .header(AUTH_HEADER, BEARER_PREFIX + reniecToken)
                    .retrieve()
                    .body(String.class);
            return ResponseEntity.ok(response);
        } catch (RestClientResponseException e) {
            LOGGER.error("Error en consulta RENIEC para DNI {}: {} - {}", dni, e.getStatusCode(), e.getMessage());
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("DNI no encontrado");
            }
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body("Error en servicio externo");
        } catch (RuntimeException e) {
            LOGGER.error("Error inesperado en consulta RENIEC para DNI {}: {}", dni, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error interno del servidor");
        }
    }
}