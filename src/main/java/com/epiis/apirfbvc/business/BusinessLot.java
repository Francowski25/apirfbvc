package com.epiis.apirfbvc.business;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.epiis.apirfbvc.dto.response.ResponseLotGetAll;
import com.epiis.apirfbvc.entity.EntityLot;
import com.epiis.apirfbvc.repository.RepositoryLot;

@Service
public class BusinessLot {

    private static final String DEFAULT_VALUE = "—";
    private static final String KEY_ID_LOT = "idLot";
    private static final String KEY_CODE = "code";
    private static final String KEY_EXPIRATION_DATE = "expirationDate";
    private static final String KEY_PURCHASE_PRICE = "purchasePrice";
    private static final String KEY_CURRENT_STOCK = "currentStock";

    private final RepositoryLot repositoryLot;

    public BusinessLot(RepositoryLot repositoryLot) {
        this.repositoryLot = repositoryLot;
    }

    public ResponseLotGetAll getAll() {
        ResponseLotGetAll response = new ResponseLotGetAll();

        List<EntityLot> list = repositoryLot.findAll();

        if (list.isEmpty()) {
            response.warning();
            response.listMessage.add("No se encontraron lotes registrados en el sistema.");
            return response;
        }

        List<Map<String, String>> items = list.stream()
                .map(this::toMap)
                .toList();

        response.setListLots(items);
        response.success();
        return response;
    }

    private Map<String, String> toMap(EntityLot lot) {
        Map<String, String> data = new HashMap<>();

        data.put(KEY_ID_LOT, lot.getIdLot());
        data.put(KEY_CODE, lot.getCode());
        data.put(KEY_EXPIRATION_DATE, lot.getExpirationDate() != null
                ? lot.getExpirationDate().toString()
                : "");
        data.put(KEY_PURCHASE_PRICE, lot.getPurchasePrice() != null
                ? lot.getPurchasePrice().toString()
                : "0");
        data.put(KEY_CURRENT_STOCK, String.valueOf(lot.getCurrentStock()));
        data.put("createdAt", lot.getCreatedAt() != null
                ? lot.getCreatedAt().toString()
                : "");

        data.put("idProduct", lot.getProduct() != null ? lot.getProduct().getIdProduct() : "");
        data.put("productName", lot.getProduct() != null ? lot.getProduct().getName() : DEFAULT_VALUE);

        data.put("idSupplier", lot.getSupplier() != null ? lot.getSupplier().getIdSupplier() : "");
        data.put("supplierName", lot.getSupplier() != null ? lot.getSupplier().getName() : DEFAULT_VALUE);

        data.put("expirationStatus", calcularEstadoVencimiento(lot.getExpirationDate()));

        return data;
    }

    private String calcularEstadoVencimiento(LocalDate expirationDate) {
        if (expirationDate == null)
            return "Sin fecha";

        LocalDate hoy = LocalDate.now();
        LocalDate limite = hoy.plusDays(30);

        if (expirationDate.isBefore(hoy)) {
            return "Vencido";
        } else if (!expirationDate.isAfter(limite)) {
            return "Por vencer";
        } else {
            return "Vigente";
        }
    }

    public ResponseLotGetAll getByProduct(String idProduct) {
        ResponseLotGetAll response = new ResponseLotGetAll();

        List<EntityLot> lots = repositoryLot.findByProduct_IdProduct(idProduct);

        if (lots.isEmpty()) {
            response.warning();
            response.listMessage.add("El producto no tiene ningún lote registrado.");
            return response;
        }

        List<Map<String, String>> items = lots.stream()
                .filter(l -> l.getCurrentStock() > 0)
                .map(l -> {
                    Map<String, String> data = new HashMap<>();
                    data.put(KEY_ID_LOT, l.getIdLot());
                    data.put(KEY_CODE, l.getCode());
                    data.put(KEY_CURRENT_STOCK, String.valueOf(l.getCurrentStock()));
                    data.put(KEY_EXPIRATION_DATE, l.getExpirationDate() != null
                            ? l.getExpirationDate().toString()
                            : "");
                    data.put(KEY_PURCHASE_PRICE, l.getPurchasePrice() != null
                            ? l.getPurchasePrice().toString()
                            : "0");
                    return data;
                })
                .toList();

        if (items.isEmpty()) {
            response.warning();
            response.listMessage.add("El producto tiene lotes registrados, pero todos están agotados (stock 0).");
            return response;
        }

        response.setListLots(items);
        response.success();
        return response;
    }
}