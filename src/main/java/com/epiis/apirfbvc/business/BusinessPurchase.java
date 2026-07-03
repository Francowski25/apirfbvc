package com.epiis.apirfbvc.business;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.epiis.apirfbvc.dto.response.ResponsePurchaseGetAll;
import com.epiis.apirfbvc.dto.response.ResponsePurchaseRecent;
import com.epiis.apirfbvc.dto.response.ResponsePurchaseReport;
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
    
    public ResponsePurchaseRecent getRecent(int limit) {
        ResponsePurchaseRecent response = new ResponsePurchaseRecent();

        List<EntityInventoryMovement> movimientos = repositoryMovement
            .findByTypeOrderByMovementDateDesc("Entrada")
            .stream()
            .limit(limit)
            .collect(Collectors.toList());

        List<Map<String, Object>> items = movimientos.stream()
            .map(m -> {
                List<EntityInventoryMovementDetail> detalles =
                    repositoryMovementDetail.findByMovement_IdMovement(m.getIdMovement());

                double costoTotal = detalles.stream()
                    .mapToDouble(d -> d.getQuantity() *
                        (d.getUnitCost() != null ? d.getUnitCost().doubleValue() : 0))
                    .sum();

                String supplierName = detalles.stream()
                    .filter(d -> d.getLot() != null && d.getLot().getSupplier() != null)
                    .map(d -> d.getLot().getSupplier().getName())
                    .findFirst()
                    .orElse("—");

                Map<String, Object> data = new HashMap<>();
                data.put("supplierName", supplierName);
                data.put("costoTotal", costoTotal);
                data.put("totalItems", detalles.size());
                data.put("movementDate", m.getMovementDate().toString());
                return data;
            })
            .collect(Collectors.toList());

        response.setListPurchases(items);
        response.success();
        return response;
    }
    
    public ResponsePurchaseReport getReport(String from, String to) {
        ResponsePurchaseReport response = new ResponsePurchaseReport();

        try {
            Date fechaFrom = parseDate(from, false);
            Date fechaTo = parseDate(to, true);

            List<EntityInventoryMovement> compras = repositoryMovement.findByType("Entrada")
                    .stream()
                    .filter(m -> m.getMovementDate() != null
                            && !m.getMovementDate().before(fechaFrom)
                            && !m.getMovementDate().after(fechaTo))
                    .sorted((a, b) -> a.getMovementDate().compareTo(b.getMovementDate()))
                    .collect(Collectors.toList());

            double inversionTotal = 0;

            List<Map<String, Object>> detalle = new ArrayList<>();

            for (EntityInventoryMovement m : compras) {

                List<EntityInventoryMovementDetail> detalles =
                        repositoryMovementDetail.findByMovement_IdMovement(m.getIdMovement());

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
                        .orElse("—");

                Map<String, Object> data = new HashMap<>();
                data.put("idMovement", m.getIdMovement());
                data.put("movementDate", m.getMovementDate().toString());
                data.put("supplierName", supplierName);
                data.put("userName", m.getUser() != null
                        ? m.getUser().getFirstName() + " " + m.getUser().getSurName()
                        : "—");
                data.put("totalUnidades", totalUnidades);
                data.put("costoTotal", costoTotal);

                detalle.add(data);
            }

            Map<String, Object> resumen = new HashMap<>();
            resumen.put("totalCompras", compras.size());
            resumen.put("totalUnidades", detalle.stream()
                    .mapToInt(d -> (int) d.get("totalUnidades"))
                    .sum());
            resumen.put("inversionTotal", inversionTotal);

            response.setResumen(resumen);
            response.setDetalle(detalle);
            response.success();

        } catch (Exception e) {
            response.listMessage.add("Error al generar reporte: " + e.getMessage());
        }

        return response;
    }

    public ResponsePurchaseReport getReportBySupplier(String from, String to) {
        ResponsePurchaseReport response = new ResponsePurchaseReport();

        try {
            Date fechaFrom = parseDate(from, false);
            Date fechaTo = parseDate(to, true);

            List<EntityInventoryMovement> compras = repositoryMovement.findByType("Entrada")
                    .stream()
                    .filter(m -> m.getMovementDate() != null
                            && !m.getMovementDate().before(fechaFrom)
                            && !m.getMovementDate().after(fechaTo))
                    .collect(Collectors.toList());

            Map<String, Map<String, Object>> porProveedor = new LinkedHashMap<>();

            for (EntityInventoryMovement m : compras) {

                List<EntityInventoryMovementDetail> detalles =
                        repositoryMovementDetail.findByMovement_IdMovement(m.getIdMovement());

                for (EntityInventoryMovementDetail d : detalles) {

                    String idSupplier = d.getLot() != null && d.getLot().getSupplier() != null
                            ? d.getLot().getSupplier().getIdSupplier()
                            : "sin-proveedor";

                    String supplierName = d.getLot() != null && d.getLot().getSupplier() != null
                            ? d.getLot().getSupplier().getName()
                            : "Sin proveedor";

                    porProveedor.computeIfAbsent(idSupplier, k -> {
                        Map<String, Object> map = new HashMap<>();
                        map.put("idSupplier", idSupplier);
                        map.put("supplierName", supplierName);
                        map.put("totalCompras", 0);
                        map.put("totalUnidades", 0);
                        map.put("inversionTotal", 0.0);
                        return map;
                    });

                    Map<String, Object> proveedor = porProveedor.get(idSupplier);

                    proveedor.put("totalCompras", (int) proveedor.get("totalCompras") + 1);
                    proveedor.put("totalUnidades",
                            (int) proveedor.get("totalUnidades") + d.getQuantity());

                    proveedor.put("inversionTotal",
                            (double) proveedor.get("inversionTotal")
                                    + d.getQuantity()
                                    * (d.getUnitCost() != null ? d.getUnitCost().doubleValue() : 0));
                }
            }

            List<Map<String, Object>> detalle = new ArrayList<>(porProveedor.values());

            detalle.sort((a, b) -> Double.compare(
                    (double) b.get("inversionTotal"),
                    (double) a.get("inversionTotal")));

            double inversionTotal = detalle.stream()
                    .mapToDouble(d -> (double) d.get("inversionTotal"))
                    .sum();

            Map<String, Object> resumen = new HashMap<>();
            resumen.put("totalProveedores", detalle.size());
            resumen.put("totalCompras", compras.size());
            resumen.put("inversionTotal", inversionTotal);

            response.setResumen(resumen);
            response.setDetalle(detalle);
            response.success();

        } catch (Exception e) {
            response.listMessage.add("Error al generar reporte: " + e.getMessage());
        }

        return response;
    }

    private Date parseDate(String dateStr, boolean endOfDay) {
        try {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd");
            Date date = sdf.parse(dateStr);

            if (endOfDay) {
                Calendar cal = Calendar.getInstance();
                cal.setTime(date);
                cal.set(Calendar.HOUR_OF_DAY, 23);
                cal.set(Calendar.MINUTE, 59);
                cal.set(Calendar.SECOND, 59);
                return cal.getTime();
            }

            return date;

        } catch (Exception e) {
            return endOfDay ? new Date() : new Date(0);
        }
    }
}