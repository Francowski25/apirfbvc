package com.epiis.apirfbvc.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.epiis.apirfbvc.business.BusinessSale;
import com.epiis.apirfbvc.dto.request.RequestSaleSave;
import com.epiis.apirfbvc.dto.response.ResponseSaleGetAll;
import com.epiis.apirfbvc.dto.response.ResponseSaleKpi;
import com.epiis.apirfbvc.dto.response.ResponseSaleRecent;
import com.epiis.apirfbvc.dto.response.ResponseSaleReport;
import com.epiis.apirfbvc.dto.response.ResponseSaleSave;
import com.epiis.apirfbvc.dto.response.ResponseSaleTopProducts;
import com.epiis.apirfbvc.dto.response.ResponseSaleWeek;

@RestController
@RequestMapping(path = "sale")
public class SaleController {
    private final BusinessSale businessSale;

    public SaleController(BusinessSale businessSale) {
        this.businessSale = businessSale;
    }
    
    @GetMapping(path = "getall")
    public ResponseEntity<ResponseSaleGetAll> getAll() {
        return ResponseEntity.ok(businessSale.getAll());
    }

    @PostMapping(path = "save")
    public ResponseEntity<ResponseSaleSave> save(@RequestBody RequestSaleSave request) {
        return ResponseEntity.ok(businessSale.save(request));
    }

    @GetMapping(path = "kpi")
    public ResponseEntity<ResponseSaleKpi> getKpi() {
        return ResponseEntity.ok(businessSale.getKpi());
    }

    @GetMapping(path = "week")
    public ResponseEntity<ResponseSaleWeek> getSalesWeek() {
        return ResponseEntity.ok(businessSale.getSalesWeek());
    }

    @GetMapping(path = "top-products")
    public ResponseEntity<ResponseSaleTopProducts> getTopProducts(@RequestParam(defaultValue = "5") int limit) {
        return ResponseEntity.ok(businessSale.getTopProducts(limit));
    }

    @GetMapping(path = "recent")
    public ResponseEntity<ResponseSaleRecent> getRecent(@RequestParam(defaultValue = "5") int limit) {
        return ResponseEntity.ok(businessSale.getRecent(limit));
    }
    
    @GetMapping("report/{from}/{to}")
    public ResponseEntity<ResponseSaleReport> getReport(
            @PathVariable String from,
            @PathVariable String to) {

        return ResponseEntity.ok(businessSale.getReport(from, to));
    }

    @GetMapping(path = "report/by-user/{from}/{to}")
    public ResponseEntity<ResponseSaleReport> getReportByUser(
            @PathVariable String from,
            @PathVariable String to) {
        return ResponseEntity.ok(businessSale.getReportByUser(from, to));
    }

    @GetMapping(path = "report/by-product/{from}/{to}")
    public ResponseEntity<ResponseSaleReport> getReportByProduct(
    		@PathVariable String from,
            @PathVariable String to) {
        return ResponseEntity.ok(businessSale.getReportByProduct(from, to));
    }
}