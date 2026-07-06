package com.epiis.apirfbvc.business;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.epiis.apirfbvc.dto.request.RequestProductInsert;
import com.epiis.apirfbvc.dto.response.ResponseProductGetAll;
import com.epiis.apirfbvc.dto.response.ResponseProductInsert;
import com.epiis.apirfbvc.dto.response.ResponseProductStockAlert;
import com.epiis.apirfbvc.entity.EntityCategory;
import com.epiis.apirfbvc.entity.EntityLaboratory;
import com.epiis.apirfbvc.entity.EntityLot;
import com.epiis.apirfbvc.entity.EntityProduct;
import com.epiis.apirfbvc.repository.RepositoryLot;
import com.epiis.apirfbvc.repository.RepositoryProduct;

@Service
public class BusinessProduct {

    private final RepositoryProduct repositoryProduct;
    private final RepositoryLot repositoryLot;

    public BusinessProduct(RepositoryProduct repositoryProduct, RepositoryLot repositoryLot) {
        this.repositoryProduct = repositoryProduct;
        this.repositoryLot = repositoryLot;
    }

    public ResponseProductInsert insert(RequestProductInsert request) {
        ResponseProductInsert response = new ResponseProductInsert();

        EntityProduct product = new EntityProduct();
        product.setIdProduct(UUID.randomUUID().toString());
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setBarcode(request.getBarcode());
        product.setImage(request.getImage());
        product.setPriceSale(request.getPriceSale());
        product.setRequiresPrescription(request.isRequiresPrescription());
        product.setStockMinimum(request.getStockMinimum());
        product.setStatus("activo");
		product.setCreatedAt(new java.sql.Date(new Date().getTime()));
		product.setUpdatedAt(product.getCreatedAt());
        
        EntityCategory category = new EntityCategory();
        category.setIdCategory(request.getIdCategory());
        product.setCategory(category);

        EntityLaboratory laboratory = new EntityLaboratory();
        laboratory.setIdLaboratory(request.getIdLaboratory());
        product.setLaboratory(laboratory);

        repositoryProduct.save(product);

        EntityLot lot = new EntityLot();
        lot.setIdLot(UUID.randomUUID().toString());
        lot.setCode("LOT-" + System.currentTimeMillis());
        lot.setCurrentStock(request.getTotalStock());
        lot.setPurchasePrice(BigDecimal.ZERO);
        lot.setProduct(product);
        lot.setSupplier(null);
        lot.setCreatedAt(new java.sql.Date(new Date().getTime()));
		lot.setUpdatedAt(lot.getCreatedAt());

        if (request.getNextExpiration() != null && !request.getNextExpiration().isBlank()) {
            lot.setExpirationDate(LocalDate.parse(request.getNextExpiration()));

        } else {
            lot.setExpirationDate(LocalDate.of(2099, 12, 31));
        }

        repositoryLot.save(lot);

        response.success();
		response.listMessage.add("Registro realizado correctamente.");

        return response;
    }
    
    public ResponseProductGetAll getAll() {
        ResponseProductGetAll response = new ResponseProductGetAll();
        List<EntityProduct> list = repositoryProduct.findAll();
        List<Map<String, String>> items = list.stream()
            .map(this::toMap)
            .collect(Collectors.toList());
        response.setListProducts(items);
        response.success();
        return response;
    }

    private Map<String, String> toMap(EntityProduct p) {
        List<EntityLot> lots = repositoryLot.findByProduct_IdProduct(p.getIdProduct());

        int totalStock = lots.stream()
            .mapToInt(EntityLot::getCurrentStock)
            .sum();

        String nextExpiration = lots.stream()
            .map(EntityLot::getExpirationDate)
            .min(LocalDate::compareTo)
            .map(LocalDate::toString)
            .orElse("");

        Map<String, String> data = new HashMap<>();
        data.put("idProduct", p.getIdProduct());
        data.put("name", p.getName());
        data.put("description", p.getDescription());
        data.put("barcode", p.getBarcode());
        data.put("image", p.getImage());
        data.put("priceSale", p.getPriceSale() != null ? p.getPriceSale().toString() : "0");
        data.put("stockMinimum", String.valueOf(p.getStockMinimum()));
        data.put("requiresPrescription", String.valueOf(p.isRequiresPrescription()));
        data.put("status", p.getStatus());
        data.put("totalStock", String.valueOf(totalStock));
        data.put("nextExpiration", nextExpiration);
        data.put("laboratory", p.getLaboratory() != null ? p.getLaboratory().getName() : "");
        data.put("category", p.getCategory() != null ? p.getCategory().getName() : "");
        data.put("createdAt", p.getCreatedAt().toString());
        
        return data;
    }
    
    public ResponseProductStockAlert getStockAlert() {
        ResponseProductStockAlert response = new ResponseProductStockAlert();

        List<EntityProduct> productos = repositoryProduct.findAll();

        int stockCritico = 0;
        for (EntityProduct p : productos) {
            int totalStock = repositoryLot.findByProduct_IdProduct(p.getIdProduct())
                .stream()
                .mapToInt(EntityLot::getCurrentStock)
                .sum();
            if (totalStock <= p.getStockMinimum()) {
                stockCritico++;
            }
        }

        LocalDate hoy = LocalDate.now();
        LocalDate limite = hoy.plusDays(30);
        long porVencer = repositoryLot.findByExpirationDateBetween(hoy, limite).size();

        response.setStockCritico(stockCritico);
        response.setPorVencer30Dias(porVencer);
        response.success();
        return response;
    }
    
}