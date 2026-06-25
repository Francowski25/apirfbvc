package com.epiis.apirfbvc.business;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.epiis.apirfbvc.dto.response.ResponsePurchaseGetAll;
import com.epiis.apirfbvc.entity.EntityInventoryMovement;
import com.epiis.apirfbvc.entity.EntityInventoryMovementDetail;
import com.epiis.apirfbvc.repository.RepositoryInventoryMovement;
import com.epiis.apirfbvc.repository.RepositoryInventoryMovementDetail;

@Service
public class BusinessPurchase {

    private final RepositoryInventoryMovement repositoryMovement;
    private final RepositoryInventoryMovementDetail repositoryMovementDetail;

    public BusinessPurchase(RepositoryInventoryMovement repositoryMovement,
                            RepositoryInventoryMovementDetail repositoryMovementDetail) {
        this.repositoryMovement = repositoryMovement;
        this.repositoryMovementDetail = repositoryMovementDetail;
    }

    public ResponsePurchaseGetAll getAll() {
        ResponsePurchaseGetAll response = new ResponsePurchaseGetAll();

        List<EntityInventoryMovement> movimientos =
            repositoryMovement.findByType("Entrada");

        List<Map<String, Object>> items = movimientos.stream()
            .map(this::toMap)
            .collect(Collectors.toList());

        response.setListPurchases(items);
        response.success();
        return response;
    }

    private Map<String, Object> toMap(EntityInventoryMovement m) {
        List<EntityInventoryMovementDetail> detalles =
            repositoryMovementDetail.findByMovement_IdMovement(m.getIdMovement());

        int totalUnidades = detalles.stream()
            .mapToInt(EntityInventoryMovementDetail::getQuantity)
            .sum();

        double costoTotal = detalles.stream()
            .mapToDouble(d -> d.getQuantity() *
                (d.getUnitCost() != null ? d.getUnitCost().doubleValue() : 0))
            .sum();

        String supplierName = detalles.stream()
            .filter(d -> d.getLot() != null && d.getLot().getSupplier() != null)
            .map(d -> d.getLot().getSupplier().getName())
            .findFirst()
            .orElse("—");

        List<Map<String, String>> detallesMap = detalles.stream().map(d -> {
            Map<String, String> det = new HashMap<>();
            det.put("idDetail", d.getIdDetail());
            det.put("productName", d.getProduct() != null ? d.getProduct().getName() : "—");
            det.put("lotCode", d.getLot() != null ? d.getLot().getCode() : "—");
            det.put("expirationDate", d.getLot() != null && d.getLot().getExpirationDate() != null
                ? d.getLot().getExpirationDate().toString() : "—");
            det.put("quantity", String.valueOf(d.getQuantity()));
            det.put("unitCost", d.getUnitCost() != null ? d.getUnitCost().toString() : "0");
            det.put("subtotal", String.valueOf(
                d.getQuantity() * (d.getUnitCost() != null ? d.getUnitCost().doubleValue() : 0)));
            return det;
        }).collect(Collectors.toList());

        Map<String, Object> data = new HashMap<>();
        data.put("idMovement", m.getIdMovement());
        data.put("movementDate", m.getMovementDate().toString());
        data.put("observation", m.getObservation() != null ? m.getObservation() : "");
        data.put("userName", m.getUser() != null
            ? m.getUser().getFirstName() + " " + m.getUser().getSurName() : "—");
        data.put("supplierName", supplierName);
        data.put("totalUnidades", totalUnidades);
        data.put("costoTotal", costoTotal);
        data.put("detalles", detallesMap);

        return data;
    }
}