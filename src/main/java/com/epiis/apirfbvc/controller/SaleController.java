package com.epiis.apirfbvc.controller;

import org.springframework.http.HttpStatus;
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
        try {
            return ResponseEntity.ok(businessSale.getAll());
        } catch (Exception e) {
            ResponseSaleGetAll response = new ResponseSaleGetAll();
            response.exception();
            response.listMessage.add(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping(path = "save")
    public ResponseEntity<ResponseSaleSave> save(@RequestBody RequestSaleSave request) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(businessSale.save(request));
        } catch (Exception e) {
            ResponseSaleSave response = new ResponseSaleSave();
            response.exception();
            response.listMessage.add(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping(path = "kpi")
    public ResponseEntity<ResponseSaleKpi> getKpi() {
        try {
            return ResponseEntity.ok(businessSale.getKpi());
        } catch (Exception e) {
            ResponseSaleKpi response = new ResponseSaleKpi();
            response.exception();
            response.listMessage.add(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping(path = "week")
    public ResponseEntity<ResponseSaleWeek> getSalesWeek() {
        try {
            return ResponseEntity.ok(businessSale.getSalesWeek());
        } catch (Exception e) {
            ResponseSaleWeek response = new ResponseSaleWeek();
            response.exception();
            response.listMessage.add(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping(path = "top-products")
    public ResponseEntity<ResponseSaleTopProducts> getTopProducts(@RequestParam(defaultValue = "5") int limit) {
        try {
            return ResponseEntity.ok(businessSale.getTopProducts(limit));
        } catch (Exception e) {
            ResponseSaleTopProducts response = new ResponseSaleTopProducts();
            response.exception();
            response.listMessage.add(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping(path = "recent")
    public ResponseEntity<ResponseSaleRecent> getRecent(@RequestParam(defaultValue = "4") int limit) {
        try {
            return ResponseEntity.ok(businessSale.getRecent(limit));
        } catch (Exception e) {
            ResponseSaleRecent response = new ResponseSaleRecent();
            response.exception();
            response.listMessage.add(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping(path = "report/{from}/{to}")
    public ResponseEntity<ResponseSaleReport> getReport(
            @PathVariable String from,
            @PathVariable String to) {
        try {
            return ResponseEntity.ok(businessSale.getReport(from, to));
        } catch (Exception e) {
            ResponseSaleReport response = new ResponseSaleReport();
            response.exception();
            response.listMessage.add(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping(path = "report/by-user/{from}/{to}")
    public ResponseEntity<ResponseSaleReport> getReportByUser(
            @PathVariable String from,
            @PathVariable String to) {
        try {
            return ResponseEntity.ok(businessSale.getReportByUser(from, to));
        } catch (Exception e) {
            ResponseSaleReport response = new ResponseSaleReport();
            response.exception();
            response.listMessage.add(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping(path = "report/by-product/{from}/{to}")
    public ResponseEntity<ResponseSaleReport> getReportByProduct(
            @PathVariable String from,
            @PathVariable String to) {
        try {
            return ResponseEntity.ok(businessSale.getReportByProduct(from, to));
        } catch (Exception e) {
            ResponseSaleReport response = new ResponseSaleReport();
            response.exception();
            response.listMessage.add(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}