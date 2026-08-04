package com.epiis.apirfbvc.business;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.epiis.apirfbvc.dto.response.ResponsePurchaseGetAll;
import com.epiis.apirfbvc.dto.response.ResponsePurchaseRecent;
import com.epiis.apirfbvc.dto.response.ResponsePurchaseReport;
import com.epiis.apirfbvc.entity.EntityInventoryMovement;
import com.epiis.apirfbvc.entity.EntityInventoryMovementDetail;
import com.epiis.apirfbvc.repository.RepositoryInventoryMovement;
import com.epiis.apirfbvc.repository.RepositoryInventoryMovementDetail;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BusinessPurchase {

    private final RepositoryInventoryMovement repositoryMovement;
    private final RepositoryInventoryMovementDetail repositoryMovementDetail;

    public ResponsePurchaseGetAll getAll() {
        ResponsePurchaseGetAll response = new ResponsePurchaseGetAll();

        List<EntityInventoryMovement> movimientos = repositoryMovement.findByType(BusinessUtils.TYPE_ENTRADA);

        List<Map<String, Object>> items = movimientos.stream()
                .map(this::toMap)
                .toList();

        response.setListPurchases(items);
        response.success();
        return response;
    }

    private Map<String, Object> toMap(EntityInventoryMovement m) {
        List<EntityInventoryMovementDetail> detalles = repositoryMovementDetail
                .findByMovement_IdMovement(m.getIdMovement());

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
                .orElse(BusinessUtils.DEFAULT_VALUE);

        List<Map<String, String>> detallesMap = detalles.stream()
                .map(this::mapToPurchaseDetailMap)
                .toList();

        Map<String, Object> data = new HashMap<>();
        data.put(BusinessUtils.KEY_ID_MOVEMENT, m.getIdMovement());
        data.put(BusinessUtils.KEY_MOVEMENT_DATE, m.getMovementDate().toString());
        data.put(BusinessUtils.KEY_OBSERVATION, BusinessUtils.getValueOrDefault(m.getObservation()));
        data.put(BusinessUtils.KEY_USER_NAME, BusinessUtils.buildFullName(m.getUser()));
        data.put(BusinessUtils.KEY_SUPPLIER_NAME, supplierName);
        data.put(BusinessUtils.KEY_TOTAL_UNIDADES, totalUnidades);
        data.put(BusinessUtils.KEY_COSTO_TOTAL, costoTotal);
        data.put(BusinessUtils.KEY_DETALLES, detallesMap);

        return data;
    }

    private Map<String, String> mapToPurchaseDetailMap(EntityInventoryMovementDetail d) {
        Map<String, String> det = new HashMap<>();
        det.put(BusinessUtils.KEY_ID_DETAIL, d.getIdDetail());
        det.put(BusinessUtils.KEY_PRODUCT_NAME, BusinessUtils.getProductName(d.getProduct()));
        det.put(BusinessUtils.KEY_LOT_CODE, BusinessUtils.getLotCode(d.getLot()));
        det.put(BusinessUtils.KEY_EXPIRATION_DATE, BusinessUtils.getLotExpirationDate(d.getLot()));
        det.put(BusinessUtils.KEY_QUANTITY, String.valueOf(d.getQuantity()));
        det.put(BusinessUtils.KEY_UNIT_COST, BusinessUtils.formatBigDecimal(d.getUnitCost()));
        det.put(BusinessUtils.KEY_SUBTOTAL, String.valueOf(
                d.getQuantity() * (d.getUnitCost() != null ? d.getUnitCost().doubleValue() : 0)));
        return det;
    }

    public ResponsePurchaseRecent getRecent(int limit) {
        ResponsePurchaseRecent response = new ResponsePurchaseRecent();

        List<EntityInventoryMovement> movimientos = repositoryMovement
                .findByTypeOrderByMovementDateDesc(BusinessUtils.TYPE_ENTRADA)
                .stream()
                .limit(limit)
                .toList();

        List<Map<String, Object>> items = movimientos.stream()
                .map(m -> {
                    List<EntityInventoryMovementDetail> detalles = repositoryMovementDetail
                            .findByMovement_IdMovement(m.getIdMovement());

                    double costoTotal = detalles.stream()
                            .mapToDouble(d -> d.getQuantity() *
                                    (d.getUnitCost() != null ? d.getUnitCost().doubleValue() : 0))
                            .sum();

                    String supplierName = detalles.stream()
                            .filter(d -> d.getLot() != null && d.getLot().getSupplier() != null)
                            .map(d -> d.getLot().getSupplier().getName())
                            .findFirst()
                            .orElse(BusinessUtils.DEFAULT_VALUE);

                    Map<String, Object> data = new HashMap<>();
                    data.put(BusinessUtils.KEY_SUPPLIER_NAME, supplierName);
                    data.put(BusinessUtils.KEY_COSTO_TOTAL, costoTotal);
                    data.put("totalItems", detalles.size());
                    data.put(BusinessUtils.KEY_MOVEMENT_DATE, m.getMovementDate().toString());
                    return data;
                })
                .toList();

        response.setListPurchases(items);
        response.success();
        return response;
    }

    public ResponsePurchaseReport getReport(String from, String to) {
        ResponsePurchaseReport response = new ResponsePurchaseReport();

        try {
            Date fechaFrom = BusinessUtils.parseDate(from, false);
            Date fechaTo = BusinessUtils.parseDate(to, true);

            List<EntityInventoryMovement> compras = repositoryMovement.findByType(BusinessUtils.TYPE_ENTRADA)
                    .stream()
                    .filter(m -> m.getMovementDate() != null
                            && !m.getMovementDate().before(fechaFrom)
                            && !m.getMovementDate().after(fechaTo))
                    .sorted(Comparator.comparing(EntityInventoryMovement::getMovementDate))
                    .toList();

            double inversionTotal = 0;

            List<Map<String, Object>> detalle = new ArrayList<>();

            for (EntityInventoryMovement m : compras) {

                List<EntityInventoryMovementDetail> detalles = repositoryMovementDetail
                        .findByMovement_IdMovement(m.getIdMovement());

                int totalUnidades = detalles.stream()
                        .mapToInt(EntityInventoryMovementDetail::getQuantity)
                        .sum();

                double costoTotal = detalles.stream()
                        .mapToDouble(d -> d.getQuantity()
                                * (d.getUnitCost() != null ? d.getUnitCost().doubleValue() : 0))
                        .sum();

                inversionTotal += costoTotal;

                String supplierName = detalles.stream()
                        .filter(d -> d.getLot() != null && d.getLot().getSupplier() != null)
                        .map(d -> d.getLot().getSupplier().getName())
                        .findFirst()
                        .orElse(BusinessUtils.DEFAULT_VALUE);

                Map<String, Object> data = new HashMap<>();
                data.put(BusinessUtils.KEY_ID_MOVEMENT, m.getIdMovement());
                data.put(BusinessUtils.KEY_MOVEMENT_DATE, m.getMovementDate().toString());
                data.put(BusinessUtils.KEY_SUPPLIER_NAME, supplierName);
                data.put(BusinessUtils.KEY_USER_NAME, BusinessUtils.buildFullName(m.getUser()));
                data.put(BusinessUtils.KEY_TOTAL_UNIDADES, totalUnidades);
                data.put(BusinessUtils.KEY_COSTO_TOTAL, costoTotal);

                detalle.add(data);
            }

            Map<String, Object> resumen = new HashMap<>();
            resumen.put(BusinessUtils.KEY_TOTAL_COMPRAS, compras.size());
            resumen.put(BusinessUtils.KEY_TOTAL_UNIDADES, detalle.stream()
                    .mapToInt(d -> (int) d.get(BusinessUtils.KEY_TOTAL_UNIDADES))
                    .sum());
            resumen.put(BusinessUtils.KEY_INVERSION_TOTAL, inversionTotal);

            response.setResumen(resumen);
            response.setDetalle(detalle);
            response.success();

        } catch (RuntimeException e) {
            handleReportException(response, e);
        }

        return response;
    }

    public ResponsePurchaseReport getReportBySupplier(String from, String to) {
        ResponsePurchaseReport response = new ResponsePurchaseReport();

        try {
            Date fechaFrom = BusinessUtils.parseDate(from, false);
            Date fechaTo = BusinessUtils.parseDate(to, true);

            List<EntityInventoryMovement> compras = repositoryMovement.findByType(BusinessUtils.TYPE_ENTRADA)
                    .stream()
                    .filter(m -> m.getMovementDate() != null
                            && !m.getMovementDate().before(fechaFrom)
                            && !m.getMovementDate().after(fechaTo))
                    .toList();

            Map<String, Map<String, Object>> porProveedor = new LinkedHashMap<>();

            compras.forEach(m -> processMovementDetails(m, porProveedor));

            List<Map<String, Object>> detalle = new ArrayList<>(porProveedor.values());
            detalle.sort((a, b) -> Double.compare(
                    (double) b.get(BusinessUtils.KEY_INVERSION_TOTAL),
                    (double) a.get(BusinessUtils.KEY_INVERSION_TOTAL)));

            double inversionTotal = detalle.stream()
                    .mapToDouble(d -> (double) d.get(BusinessUtils.KEY_INVERSION_TOTAL))
                    .sum();

            Map<String, Object> resumen = new HashMap<>();
            resumen.put("totalProveedores", detalle.size());
            resumen.put(BusinessUtils.KEY_TOTAL_COMPRAS, compras.size());
            resumen.put(BusinessUtils.KEY_INVERSION_TOTAL, inversionTotal);

            response.setResumen(resumen);
            response.setDetalle(detalle);
            response.success();

        } catch (RuntimeException e) {
            handleReportException(response, e);
        }

        return response;
    }

    private void processMovementDetails(EntityInventoryMovement m, Map<String, Map<String, Object>> porProveedor) {
        List<EntityInventoryMovementDetail> detalles = repositoryMovementDetail
                .findByMovement_IdMovement(m.getIdMovement());

        for (EntityInventoryMovementDetail d : detalles) {
            updateSupplierMap(d, porProveedor);
        }
    }

    private void updateSupplierMap(EntityInventoryMovementDetail d, Map<String, Map<String, Object>> porProveedor) {
        boolean hasSupplier = d.getLot() != null && d.getLot().getSupplier() != null;
        String idSupplier = hasSupplier ? d.getLot().getSupplier().getIdSupplier() : BusinessUtils.SIN_ID + "proveedor";
        String supplierName = hasSupplier ? d.getLot().getSupplier().getName() : "Sin proveedor";

        Map<String, Object> proveedor = porProveedor.computeIfAbsent(idSupplier, k -> {
            Map<String, Object> map = new HashMap<>();
            map.put("idSupplier", idSupplier);
            map.put(BusinessUtils.KEY_SUPPLIER_NAME, supplierName);
            map.put(BusinessUtils.KEY_TOTAL_COMPRAS, 0);
            map.put(BusinessUtils.KEY_TOTAL_UNIDADES, 0);
            map.put(BusinessUtils.KEY_INVERSION_TOTAL, 0.0);
            return map;
        });

        double unitCost = d.getUnitCost() != null ? d.getUnitCost().doubleValue() : 0.0;
        double subtotal = d.getQuantity() * unitCost;

        proveedor.put(BusinessUtils.KEY_TOTAL_COMPRAS, (int) proveedor.get(BusinessUtils.KEY_TOTAL_COMPRAS) + 1);
        proveedor.put(BusinessUtils.KEY_TOTAL_UNIDADES, (int) proveedor.get(BusinessUtils.KEY_TOTAL_UNIDADES) + d.getQuantity());
        proveedor.put(BusinessUtils.KEY_INVERSION_TOTAL, (double) proveedor.get(BusinessUtils.KEY_INVERSION_TOTAL) + subtotal);
    }

    private static void handleReportException(ResponsePurchaseReport response, RuntimeException e) {
        response.listMessage.add(BusinessUtils.buildReportErrorMessage(e));
    }
}