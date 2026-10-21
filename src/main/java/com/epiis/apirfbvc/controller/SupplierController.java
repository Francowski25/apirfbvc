package com.epiis.apirfbvc.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.epiis.apirfbvc.business.BusinessSupplier;
import com.epiis.apirfbvc.dto.request.RequestSupplierInsert;
import com.epiis.apirfbvc.dto.request.RequestSupplierUpdate;
import com.epiis.apirfbvc.dto.response.ResponseSupplierGetAll;
import com.epiis.apirfbvc.dto.response.ResponseSupplierInsert;
import com.epiis.apirfbvc.dto.response.ResponseSupplierUpdate;

@RestController
@RequestMapping(path = "supplier")
public class SupplierController {

    private final BusinessSupplier businessSupplier;

    public SupplierController(BusinessSupplier businessSupplier) {
        this.businessSupplier = businessSupplier;
    }

    @GetMapping(path = "getall")
    public ResponseEntity<ResponseSupplierGetAll> getAll() {
        return ResponseEntity.ok(businessSupplier.getAll());
    }

    @PostMapping(path = "insert", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ResponseSupplierInsert> insert(@ModelAttribute RequestSupplierInsert request) {
        return ResponseEntity.ok(businessSupplier.insert(request));
    }

    @PutMapping(path = "update", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ResponseSupplierUpdate> update(@ModelAttribute RequestSupplierUpdate request) {
        return ResponseEntity.ok(businessSupplier.update(request));
    }

    @PutMapping(path = "status/{id}/{newStatus}")
    public ResponseEntity<ResponseSupplierInsert> toggleStatus(@PathVariable String id, @PathVariable String newStatus) {
        return ResponseEntity.ok(businessSupplier.toggleStatus(id, newStatus));
    }
}