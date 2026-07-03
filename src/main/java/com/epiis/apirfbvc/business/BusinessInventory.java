package com.epiis.apirfbvc.business;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.epiis.apirfbvc.dto.response.ResponseInventoryIncome;
import com.epiis.apirfbvc.dto.response.ResponseInventoryReport;
import com.epiis.apirfbvc.entity.EntityInventoryMovement;
import com.epiis.apirfbvc.entity.EntityInventoryMovementDetail;
import com.epiis.apirfbvc.entity.EntityLot;
import com.epiis.apirfbvc.repository.RepositoryInventoryMovement;
import com.epiis.apirfbvc.repository.RepositoryInventoryMovementDetail;
import com.epiis.apirfbvc.repository.RepositoryLot;

@Service
public class BusinessInventory {
    private final RepositoryInventoryMovement repositoryMovement;
    private final RepositoryInventoryMovementDetail repositoryMovementDetail;
    private final RepositoryLot repositoryLot;

    public BusinessInventory(RepositoryLot repositoryLot,
                             RepositoryInventoryMovement repositoryMovement,
                             RepositoryInventoryMovementDetail repositoryMovementDetail) {
        this.repositoryMovement = repositoryMovement;
        this.repositoryMovementDetail = repositoryMovementDetail;
        this.repositoryLot = repositoryLot;
    }

    public ResponseInventoryIncome getIncomes() {
        ResponseInventoryIncome response = new ResponseInventoryIncome();

        List<EntityInventoryMovement> movimientos = repositoryMovement.findByType("Entrada");

        List<Map<String, Object>> items = movimientos.stream()
            .map(this::toIncomeMap)
            .collect(Collectors.toList());

        response.setListMovements(items);
        response.success();
        return response;
    }

    private Map<String, Object> toIncomeMap(EntityInventoryMovement m) {
        List<EntityInventoryMovementDetail> detalles =
            repositoryMovementDetail.findByMovement_IdMovement(m.getIdMovement());

        List<Map<String, String>> detallesMap = detalles.stream().map(d -> {
            Map<String, String> det = new HashMap<>();
            det.put("idDetail", d.getIdDetail());
            det.put("productName", d.getProduct() != null ? d.getProduct().getName() : "—");
            det.put("lotCode", d.getLot() != null ? d.getLot().getCode() : "—");
            det.put("quantity", String.valueOf(d.getQuantity()));
            det.put("unitCost", d.getUnitCost() != null ? d.getUnitCost().toString() : "0");
            return det;
        }).collect(Collectors.toList());

        Map<String, Object> data = new HashMap<>();
        data.put("idMovement", m.getIdMovement());
        data.put("movementDate", m.getMovementDate().toString());
        data.put("observation", m.getObservation() != null ? m.getObservation() : "");
        data.put("userName", m.getUser() != null
            ? m.getUser().getFirstName() + " " + m.getUser().getSurName()
            : "—");
        data.put("detalles", detallesMap);

        return data;
    }
    
    public ResponseInventoryReport getReportMovements(String from, String to) {
        ResponseInventoryReport response = new ResponseInventoryReport();
        try {
            Date fechaFrom = parseDate(from, false);
            Date fechaTo = parseDate(to, true);

            List<EntityInventoryMovement> movimientos = repositoryMovement.findAll().stream()
                    .filter(m -> m.getMovementDate() != null
                            && !m.getMovementDate().before(fechaFrom)
                            && !m.getMovementDate().after(fechaTo))
                    .sorted((a, b) -> a.getMovementDate().compareTo(b.getMovementDate()))
                    .collect(Collectors.toList());

            int entradas = 0;
            int salidas = 0;
            int ajustes = 0;
            int totalCantidad = 0;

            List<Map<String, Object>> detalle = new ArrayList<>();

            for (EntityInventoryMovement m : movimientos) {

                List<EntityInventoryMovementDetail> detalles =
                        repositoryMovementDetail.findByMovement_IdMovement(m.getIdMovement());

                int cantidad = detalles.stream()
                        .mapToInt(EntityInventoryMovementDetail::getQuantity)
                        .sum();

                totalCantidad += cantidad;

                switch (m.getType()) {
                    case "Entrada" -> entradas++;
                    case "Salida" -> salidas++;
                    default -> ajustes++;
                }

                Map<String, Object> data = new HashMap<>();
                data.put("idMovement", m.getIdMovement());
                data.put("movementDate", m.getMovementDate().toString());
                data.put("type", m.getType());
                data.put("observation", m.getObservation());
                data.put("userName", m.getUser() != null
                        ? m.getUser().getFirstName() + " " + m.getUser().getSurName()
                        : "—");
                data.put("quantity", cantidad);

                detalle.add(data);
            }

            Map<String, Object> resumen = new HashMap<>();
            resumen.put("totalMovimientos", movimientos.size());
            resumen.put("totalEntradas", entradas);
            resumen.put("totalSalidas", salidas);
            resumen.put("totalAjustes", ajustes);
            resumen.put("cantidadMovida", totalCantidad);

            response.setResumen(resumen);
            response.setDetalle(detalle);
            response.success();

        } catch (Exception e) {
            response.listMessage.add("Error al generar reporte: " + e.getMessage());
        }

        return response;
    }

    public ResponseInventoryReport getReportLowStock() {

        ResponseInventoryReport response = new ResponseInventoryReport();

        try {

            List<EntityLot> lotes = repositoryLot.findAll().stream()
                    .filter(l -> l.getProduct() != null)
                    .filter(l -> l.getCurrentStock() <= l.getProduct().getStockMinimum())
                    .collect(Collectors.toList());

            List<Map<String, Object>> detalle = lotes.stream().map(l -> {

                Map<String, Object> data = new HashMap<>();
                data.put("idLot", l.getIdLot());
                data.put("code", l.getCode());
                data.put("productName", l.getProduct().getName());
                data.put("currentStock", l.getCurrentStock());
                data.put("stockMinimum", l.getProduct().getStockMinimum());
                data.put("supplier", l.getSupplier() != null ? l.getSupplier().getName() : "—");

                return data;

            }).collect(Collectors.toList());

            Map<String, Object> resumen = new HashMap<>();
            resumen.put("productosCriticos", detalle.size());
            resumen.put("stockTotal", lotes.stream()
                    .mapToInt(EntityLot::getCurrentStock)
                    .sum());

            response.setResumen(resumen);
            response.setDetalle(detalle);
            response.success();

        } catch (Exception e) {
            response.listMessage.add("Error al generar reporte: " + e.getMessage());
        }

        return response;
    }

    public ResponseInventoryReport getReportExpiring() {

        ResponseInventoryReport response = new ResponseInventoryReport();

        try {

            LocalDate hoy = LocalDate.now();
            LocalDate limite = hoy.plusDays(30);

            List<EntityLot> lotes = repositoryLot.findByExpirationDateBetween(hoy, limite);

            List<Map<String, Object>> detalle = lotes.stream().map(l -> {

                Map<String, Object> data = new HashMap<>();

                data.put("idLot", l.getIdLot());
                data.put("code", l.getCode());
                data.put("productName", l.getProduct() != null ? l.getProduct().getName() : "—");
                data.put("expirationDate", l.getExpirationDate().toString());
                data.put("currentStock", l.getCurrentStock());
                data.put("supplier", l.getSupplier() != null ? l.getSupplier().getName() : "—");
                data.put("purchasePrice", l.getPurchasePrice().toString());

                return data;

            }).collect(Collectors.toList());

            Map<String, Object> resumen = new HashMap<>();
            resumen.put("lotesPorVencer", detalle.size());
            resumen.put("stockComprometido", lotes.stream()
                    .mapToInt(EntityLot::getCurrentStock)
                    .sum());

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