package com.epiis.apirfbvc.business;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.epiis.apirfbvc.dto.response.ResponseInventoryIncome;
import com.epiis.apirfbvc.dto.response.ResponseInventoryReport;
import com.epiis.apirfbvc.entity.EntityInventoryMovement;
import com.epiis.apirfbvc.entity.EntityInventoryMovementDetail;
import com.epiis.apirfbvc.entity.EntityLot;
import com.epiis.apirfbvc.repository.RepositoryInventoryMovement;
import com.epiis.apirfbvc.repository.RepositoryInventoryMovementDetail;
import com.epiis.apirfbvc.repository.RepositoryLot;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BusinessInventory {

    private final RepositoryInventoryMovement repositoryMovement;
    private final RepositoryInventoryMovementDetail repositoryMovementDetail;
    private final RepositoryLot repositoryLot;

    public ResponseInventoryIncome getIncomes() {
        ResponseInventoryIncome response = new ResponseInventoryIncome();

        List<EntityInventoryMovement> movimientos = repositoryMovement.findByType(BusinessUtils.TYPE_ENTRADA);

        List<Map<String, Object>> items = movimientos.stream()
                .map(this::toIncomeMap)
                .toList();

        response.setListMovements(items);
        response.success();
        return response;
    }

    private Map<String, Object> toIncomeMap(EntityInventoryMovement m) {
        List<EntityInventoryMovementDetail> detalles = repositoryMovementDetail
                .findByMovement_IdMovement(m.getIdMovement());

        List<Map<String, String>> detallesMap = detalles.stream().map(d -> {
            Map<String, String> det = new HashMap<>();
            det.put(BusinessUtils.KEY_ID_DETAIL, d.getIdDetail());
            det.put(BusinessUtils.KEY_PRODUCT_NAME, BusinessUtils.getProductName(d.getProduct()));
            det.put(BusinessUtils.KEY_LOT_CODE, BusinessUtils.getLotCode(d.getLot()));
            det.put(BusinessUtils.KEY_QUANTITY, String.valueOf(d.getQuantity()));
            det.put(BusinessUtils.KEY_UNIT_COST, BusinessUtils.formatBigDecimal(d.getUnitCost()));
            return det;
        }).toList();

        Map<String, Object> data = new HashMap<>();
        data.put(BusinessUtils.KEY_ID_MOVEMENT, m.getIdMovement());
        data.put(BusinessUtils.KEY_MOVEMENT_DATE, m.getMovementDate().toString());
        data.put(BusinessUtils.KEY_OBSERVATION, BusinessUtils.getValueOrDefault(m.getObservation()));
        data.put(BusinessUtils.KEY_USER_NAME, BusinessUtils.buildFullName(m.getUser()));
        data.put(BusinessUtils.KEY_DETALLES, detallesMap);

        return data;
    }

    public ResponseInventoryReport getReportMovements(String from, String to) {
        ResponseInventoryReport response = new ResponseInventoryReport();
        try {
            Date fechaFrom = BusinessUtils.parseDate(from, false);
            Date fechaTo = BusinessUtils.parseDate(to, true);

            List<EntityInventoryMovement> movimientos = repositoryMovement.findAll().stream()
                    .filter(m -> m.getMovementDate() != null
                            && !m.getMovementDate().before(fechaFrom)
                            && !m.getMovementDate().after(fechaTo))
                    .sorted((a, b) -> a.getMovementDate().compareTo(b.getMovementDate()))
                    .toList();

            int entradas = 0;
            int salidas = 0;
            int ajustes = 0;
            int totalCantidad = 0;

            List<Map<String, Object>> detalle = new ArrayList<>();

            for (EntityInventoryMovement m : movimientos) {

                List<EntityInventoryMovementDetail> detalles = repositoryMovementDetail
                        .findByMovement_IdMovement(m.getIdMovement());

                int cantidad = detalles.stream()
                        .mapToInt(EntityInventoryMovementDetail::getQuantity)
                        .sum();

                totalCantidad += cantidad;

                if (BusinessUtils.TYPE_ENTRADA.equals(m.getType())) {
                    entradas++;
                } else if (BusinessUtils.TYPE_SALIDA.equals(m.getType())) {
                    salidas++;
                } else {
                    ajustes++;
                }

                Map<String, Object> data = new HashMap<>();
                data.put(BusinessUtils.KEY_ID_MOVEMENT, m.getIdMovement());
                data.put(BusinessUtils.KEY_MOVEMENT_DATE, m.getMovementDate().toString());
                data.put(BusinessUtils.KEY_TYPE, m.getType());
                data.put(BusinessUtils.KEY_OBSERVATION, m.getObservation());
                data.put(BusinessUtils.KEY_USER_NAME, BusinessUtils.buildFullName(m.getUser()));
                data.put(BusinessUtils.KEY_QUANTITY, cantidad);

                detalle.add(data);
            }

            Map<String, Object> resumen = new HashMap<>();
            resumen.put(BusinessUtils.KEY_TOTAL_MOVEMENTS, movimientos.size());
            resumen.put(BusinessUtils.KEY_TOTAL_ENTRADAS, entradas);
            resumen.put(BusinessUtils.KEY_TOTAL_SALIDAS, salidas);
            resumen.put(BusinessUtils.KEY_TOTAL_AJUSTES, ajustes);
            resumen.put(BusinessUtils.KEY_CANTIDAD_MOVIDA, totalCantidad);

            response.setResumen(resumen);
            response.setDetalle(detalle);
            response.success();

        } catch (RuntimeException e) {
            handleReportException(response, e);
        }

        return response;
    }

    public ResponseInventoryReport getReportLowStock() {
        ResponseInventoryReport response = new ResponseInventoryReport();

        try {
            List<EntityLot> lotes = repositoryLot.findAll().stream()
                    .filter(l -> l.getProduct() != null)
                    .filter(l -> l.getCurrentStock() <= l.getProduct().getStockMinimum())
                    .toList();

            List<Map<String, Object>> detalle = lotes.stream().map(l -> {
                Map<String, Object> data = new HashMap<>();
                data.put(BusinessUtils.KEY_ID_LOT, l.getIdLot());
                data.put(BusinessUtils.KEY_CODE, l.getCode());
                data.put(BusinessUtils.KEY_PRODUCT_NAME, BusinessUtils.getProductName(l.getProduct()));
                data.put(BusinessUtils.KEY_CURRENT_STOCK, l.getCurrentStock());
                data.put(BusinessUtils.KEY_STOCK_MINIMUM, l.getProduct().getStockMinimum());
                data.put(BusinessUtils.KEY_SUPPLIER_NAME, BusinessUtils.getSupplierName(l));
                return data;
            }).toList();

            Map<String, Object> resumen = new HashMap<>();
            resumen.put(BusinessUtils.KEY_PRODUCTOS_CRITICOS, detalle.size());
            resumen.put(BusinessUtils.KEY_STOCK_TOTAL, lotes.stream()
                    .mapToInt(EntityLot::getCurrentStock)
                    .sum());

            response.setResumen(resumen);
            response.setDetalle(detalle);
            response.success();

        } catch (RuntimeException e) {
            handleReportException(response, e);
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
                data.put(BusinessUtils.KEY_ID_LOT, l.getIdLot());
                data.put(BusinessUtils.KEY_CODE, l.getCode());
                data.put(BusinessUtils.KEY_PRODUCT_NAME, BusinessUtils.getProductName(l.getProduct()));
                data.put(BusinessUtils.KEY_EXPIRATION_DATE, l.getExpirationDate().toString());
                data.put(BusinessUtils.KEY_CURRENT_STOCK, l.getCurrentStock());
                data.put(BusinessUtils.KEY_SUPPLIER_NAME, BusinessUtils.getSupplierName(l));
                data.put(BusinessUtils.KEY_PURCHASE_PRICE, l.getPurchasePrice().toString());
                return data;
            }).toList();

            Map<String, Object> resumen = new HashMap<>();
            resumen.put(BusinessUtils.KEY_LOTES_POR_VENCER, detalle.size());
            resumen.put(BusinessUtils.KEY_STOCK_COMPROMETIDO, lotes.stream()
                    .mapToInt(EntityLot::getCurrentStock)
                    .sum());

            response.setResumen(resumen);
            response.setDetalle(detalle);
            response.success();

        } catch (RuntimeException e) {
            handleReportException(response, e);
        }

        return response;
    }

    private static void handleReportException(ResponseInventoryReport response, RuntimeException e) {
        response.listMessage.add(BusinessUtils.buildReportErrorMessage(e));
    }
}