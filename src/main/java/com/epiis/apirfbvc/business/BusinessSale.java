package com.epiis.apirfbvc.business;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.epiis.apirfbvc.dto.request.RequestMovementCreate;
import com.epiis.apirfbvc.dto.request.RequestMovementDetailItem;
import com.epiis.apirfbvc.dto.request.RequestSaleSave;
import com.epiis.apirfbvc.dto.response.ResponseMovementCreate;
import com.epiis.apirfbvc.dto.response.ResponseSaleGetAll;
import com.epiis.apirfbvc.dto.response.ResponseSaleKpi;
import com.epiis.apirfbvc.dto.response.ResponseSaleRecent;
import com.epiis.apirfbvc.dto.response.ResponseSaleReport;
import com.epiis.apirfbvc.dto.response.ResponseSaleSave;
import com.epiis.apirfbvc.dto.response.ResponseSaleTopProducts;
import com.epiis.apirfbvc.dto.response.ResponseSaleWeek;
import com.epiis.apirfbvc.entity.EntityCustomer;
import com.epiis.apirfbvc.entity.EntityLot;
import com.epiis.apirfbvc.entity.EntityProduct;
import com.epiis.apirfbvc.entity.EntitySale;
import com.epiis.apirfbvc.entity.EntitySaleDetail;
import com.epiis.apirfbvc.entity.EntityUser;
import com.epiis.apirfbvc.repository.RepositoryCustomer;
import com.epiis.apirfbvc.repository.RepositorySale;
import com.epiis.apirfbvc.repository.RepositorySaleDetail;
import com.epiis.apirfbvc.repository.RepositoryUser;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BusinessSale {

    private static final String STATUS_COMPLETADA = "Completada";
    private static final String SUCCESS = "success";
    private static final BigDecimal IGV_RATE = BigDecimal.valueOf(0.18);
    private static final int SCALE = 2;

    private final RepositorySale repositorySale;
    private final RepositorySaleDetail repositorySaleDetail;
    private final RepositoryCustomer repositoryCustomer;
    private final RepositoryUser repositoryUser;
    private final BusinessMovement businessMovement;

    public ResponseSaleGetAll getAll() {
        ResponseSaleGetAll response = new ResponseSaleGetAll();

        List<EntitySale> ventas = repositorySale.findAll()
                .stream()
                .sorted(Comparator.comparing(EntitySale::getSaleDate).reversed())
                .toList();

        for (EntitySale s : ventas) {
            List<EntitySaleDetail> detalles = repositorySaleDetail.findBySale_IdSale(s.getIdSale());
            List<Map<String, String>> detallesMap = mapDetailsToMap(detalles);

            Map<String, Object> data = buildSaleMap(s, detallesMap);
            response.getListSales().add(data);
        }

        response.success();
        return response;
    }

    private List<Map<String, String>> mapDetailsToMap(List<EntitySaleDetail> detalles) {
        return detalles.stream().map(d -> {
            Map<String, String> det = new HashMap<>();
            det.put(BusinessUtils.KEY_ID_SALE_DETAIL, d.getIdSaleDetail());
            det.put(BusinessUtils.KEY_PRODUCT_NAME, getNameOrDefault(d.getProduct()));
            det.put(BusinessUtils.KEY_LOT_CODE, d.getLot() != null ? d.getLot().getCode() : BusinessUtils.DEFAULT_VALUE);
            det.put(BusinessUtils.KEY_QUANTITY, String.valueOf(d.getQuantity()));
            det.put(BusinessUtils.KEY_UNIT_COST, BusinessUtils.formatBigDecimal(d.getUnitPrice()));
            det.put(BusinessUtils.KEY_SUBTOTAL, BusinessUtils.formatBigDecimal(d.getSubtotal()));
            return det;
        }).toList();
    }

    private Map<String, Object> buildSaleMap(EntitySale s, List<Map<String, String>> detallesMap) {
        Map<String, Object> data = new HashMap<>();
        data.put(BusinessUtils.KEY_ID_SALE, s.getIdSale());
        data.put(BusinessUtils.KEY_SALE_NUMBER, s.getSaleNumber());
        data.put(BusinessUtils.KEY_SALE_DATE, BusinessUtils.formatDate(s.getSaleDate()));
        data.put(BusinessUtils.KEY_CUSTOMER_NAME, getCustomerName(s));
        data.put("customerDocument", s.getCustomer() != null ? s.getCustomer().getDocumentNumber() : BusinessUtils.DEFAULT_VALUE);
        data.put(BusinessUtils.KEY_USER_NAME, BusinessUtils.buildFullName(s.getUser()));
        data.put(BusinessUtils.KEY_SUBTOTAL, BusinessUtils.formatBigDecimal(s.getSubtotal()));
        data.put(BusinessUtils.KEY_DISCOUNT, BusinessUtils.formatBigDecimal(s.getDiscount()));
        data.put(BusinessUtils.KEY_IGV, BusinessUtils.formatBigDecimal(s.getIgv()));
        data.put(BusinessUtils.KEY_TOTAL, BusinessUtils.formatBigDecimal(s.getTotal()));
        data.put(BusinessUtils.KEY_PAYMENT_METHOD, s.getPaymentMethod());
        data.put("status", s.getStatus());
        data.put(BusinessUtils.KEY_DETALLES, detallesMap);
        return data;
    }

    private String getNameOrDefault(EntityProduct product) {
        return product != null ? product.getName() : BusinessUtils.DEFAULT_VALUE;
    }

    private String getCustomerName(EntitySale sale) {
        return sale.getCustomer() != null ? sale.getCustomer().getName() : BusinessUtils.DEFAULT_VALUE;
    }

    public ResponseSaleSave save(RequestSaleSave request) {
        ResponseSaleSave response = new ResponseSaleSave();

        try {
            ResponseSaleSave validation = validateSaveRequest(request);
            if (validation != null) {
                return validation;
            }

            EntityCustomer customerRef = resolveCustomer(request.getIdCustomer());
            if (customerRef == null && BusinessUtils.isNotBlank(request.getIdCustomer())) {
                response.error();
                response.listMessage.add(BusinessUtils.MSG_CLIENTE_NO_ENCONTRADO);
                return response;
            }

            BigDecimal subtotal = calculateSubtotal(request.getItems());
            BigDecimal discount = BigDecimal.valueOf(request.getDiscount() != null ? request.getDiscount() : 0);
            BigDecimal[] totals = calculateTotals(subtotal, discount);

            String saleNumber = generateSaleNumber();

            ResponseMovementCreate movementResponse = processMovement(request, saleNumber);
            if (!SUCCESS.equals(movementResponse.getType())) {
                response.error();
                response.listMessage.addAll(movementResponse.listMessage);
                return response;
            }

            persistSale(request, saleNumber, subtotal, discount, totals[0], totals[1], customerRef);

            response.success();
            response.listMessage.add(BusinessUtils.MSG_VENTA_REGISTRADA + saleNumber);

        } catch (RuntimeException e) {
            response.exception();
            response.listMessage.add(BusinessUtils.MSG_ERROR_REGISTRAR_VENTA + e.getMessage());
        }

        return response;
    }

    private ResponseSaleSave validateSaveRequest(RequestSaleSave request) {
        ResponseSaleSave response = new ResponseSaleSave();

        if (request.getItems() == null || request.getItems().isEmpty()) {
            response.error();
            response.listMessage.add("Debe agregar al menos un producto.");
            return response;
        }

        if (repositoryUser.findById(request.getIdUser()).isEmpty()) {
            response.error();
            response.listMessage.add("Usuario no encontrado.");
            return response;
        }

        return null;
    }

    private EntityCustomer resolveCustomer(String idCustomer) {
        if (BusinessUtils.isNotBlank(idCustomer)) {
            return repositoryCustomer.findById(idCustomer).orElse(null);
        }
        return null;
    }

    private BigDecimal calculateSubtotal(List<RequestSaleSave.SaleItem> items) {
        BigDecimal subtotal = BigDecimal.ZERO;
        for (RequestSaleSave.SaleItem item : items) {
            subtotal = subtotal.add(BigDecimal.valueOf(item.getUnitPrice())
                    .multiply(BigDecimal.valueOf(item.getQuantity())));
        }
        return subtotal;
    }

    private BigDecimal[] calculateTotals(BigDecimal subtotal, BigDecimal discount) {
        BigDecimal base = subtotal.subtract(discount);
        BigDecimal igv = base.multiply(IGV_RATE).setScale(SCALE, RoundingMode.HALF_UP);
        BigDecimal total = base.add(igv).setScale(SCALE, RoundingMode.HALF_UP);
        return new BigDecimal[] { igv, total };
    }

    private ResponseMovementCreate processMovement(RequestSaleSave request, String saleNumber) {
        RequestMovementCreate movementRequest = new RequestMovementCreate();
        movementRequest.setType(BusinessUtils.TYPE_SALIDA);
        movementRequest.setObservation("Venta N° " + saleNumber);
        movementRequest.setIdUser(request.getIdUser());

        List<RequestMovementDetailItem> movementDetails = request.getItems().stream().map(item -> {
            RequestMovementDetailItem detailItem = new RequestMovementDetailItem();
            detailItem.setIdProduct(item.getIdProduct());
            detailItem.setIdLot(item.getIdLot());
            detailItem.setQuantity(item.getQuantity());
            return detailItem;
        }).toList();

        movementRequest.setDetails(movementDetails);
        return businessMovement.create(movementRequest);
    }

    private void persistSale(RequestSaleSave request, String saleNumber, BigDecimal subtotal,
            BigDecimal discount, BigDecimal igv, BigDecimal total, EntityCustomer customerRef) {
        EntitySale sale = buildSaleEntity(request, saleNumber, subtotal, discount, igv, total, customerRef);
        repositorySale.save(sale);
        saveSaleDetails(sale, request.getItems());
    }

    private EntitySale buildSaleEntity(RequestSaleSave request, String saleNumber, BigDecimal subtotal,
            BigDecimal discount, BigDecimal igv, BigDecimal total, EntityCustomer customerRef) {
        EntitySale sale = new EntitySale();
        sale.setIdSale(UUID.randomUUID().toString());
        sale.setSaleNumber(saleNumber);
        sale.setSaleDate(new Date());
        sale.setSubtotal(subtotal.setScale(SCALE, RoundingMode.HALF_UP));
        sale.setDiscount(discount.setScale(SCALE, RoundingMode.HALF_UP));
        sale.setIgv(igv);
        sale.setTotal(total);
        sale.setPaymentMethod(request.getPaymentMethod());
        sale.setStatus(STATUS_COMPLETADA);
        sale.setCreatedAt(new java.sql.Date(new Date().getTime()));
        sale.setUpdatedAt(sale.getCreatedAt());
        sale.setCustomer(customerRef);

        EntityUser userRef = new EntityUser();
        userRef.setIdUser(request.getIdUser());
        sale.setUser(userRef);

        return sale;
    }

    private void saveSaleDetails(EntitySale sale, List<RequestSaleSave.SaleItem> items) {
        for (RequestSaleSave.SaleItem item : items) {
            EntitySaleDetail detail = new EntitySaleDetail();
            detail.setIdSaleDetail(UUID.randomUUID().toString());
            detail.setQuantity(item.getQuantity());
            detail.setUnitPrice(BigDecimal.valueOf(item.getUnitPrice()));
            detail.setSubtotal(BigDecimal.valueOf(item.getUnitPrice())
                    .multiply(BigDecimal.valueOf(item.getQuantity()))
                    .setScale(SCALE, RoundingMode.HALF_UP));
            detail.setSale(sale);

            EntityProduct productRef = new EntityProduct();
            productRef.setIdProduct(item.getIdProduct());
            detail.setProduct(productRef);

            EntityLot lotRef = new EntityLot();
            lotRef.setIdLot(item.getIdLot());
            detail.setLot(lotRef);

            repositorySaleDetail.save(detail);
        }
    }

    private String generateSaleNumber() {
        long count = repositorySale.count() + 1;

        while (true) {
            String candidate = String.format("V%06d", count);

            boolean exists = repositorySale.findAll().stream()
                    .anyMatch(s -> s.getSaleNumber().equals(candidate));

            if (!exists) {
                return candidate;
            }

            count++;
        }
    }

    public ResponseSaleKpi getKpi() {
        ResponseSaleKpi response = new ResponseSaleKpi();

        Date inicioHoy = getStartOfDay(0);
        Date inicioAyer = getStartOfDay(1);
        Date finAyer = inicioHoy;

        List<EntitySale> ventasHoy = repositorySale.findBySaleDateGreaterThanEqualAndStatus(inicioHoy,
                STATUS_COMPLETADA);
        List<EntitySale> ventasAyer = repositorySale.findBySaleDateBetweenAndStatus(inicioAyer, finAyer,
                STATUS_COMPLETADA);

        double totalHoy = sumTotals(ventasHoy);
        double totalAyer = sumTotals(ventasAyer);

        Map<String, Object> kpi = new HashMap<>();
        kpi.put(BusinessUtils.KEY_VENTAS_HOY, totalHoy);
        kpi.put(BusinessUtils.KEY_VENTAS_AYER, totalAyer);
        kpi.put(BusinessUtils.KEY_TRANSACCIONES_HOY, ventasHoy.size());

        response.setKpi(kpi);
        response.success();
        return response;
    }

    private double sumTotals(List<EntitySale> sales) {
        return sales.stream().mapToDouble(s -> s.getTotal().doubleValue()).sum();
    }

    public ResponseSaleWeek getSalesWeek() {
        ResponseSaleWeek response = new ResponseSaleWeek();
        List<Map<String, Object>> resultado = new ArrayList<>();

        for (int i = 6; i >= 0; i--) {
            Date inicio = getStartOfDay(i);
            Date fin = getStartOfDay(i - 1);

            List<EntitySale> ventasDia = repositorySale.findBySaleDateBetweenAndStatus(inicio, fin, STATUS_COMPLETADA);
            double total = sumTotals(ventasDia);

            LocalDate fecha = LocalDate.now().minusDays(i);
            String diaNombre = fecha.getDayOfWeek()
                    .getDisplayName(TextStyle.SHORT, Locale.of("es", "ES"));

            Map<String, Object> item = new HashMap<>();
            item.put(BusinessUtils.KEY_DIA, capitalize(diaNombre));
            item.put(BusinessUtils.KEY_FECHA, fecha.toString());
            item.put(BusinessUtils.KEY_TOTAL, total);
            resultado.add(item);
        }

        response.setListSalesWeek(resultado);
        response.success();
        return response;
    }

    public ResponseSaleTopProducts getTopProducts(int limit) {
        ResponseSaleTopProducts response = new ResponseSaleTopProducts();

        LocalDate inicioMes = LocalDate.now().withDayOfMonth(1);
        Date fechaInicio = java.sql.Date.valueOf(inicioMes);

        List<EntitySaleDetail> detalles = repositorySaleDetail.findBySale_SaleDateGreaterThanEqual(fechaInicio);

        Map<String, Integer> acumulado = new HashMap<>();
        Map<String, String> nombres = new HashMap<>();

        for (EntitySaleDetail d : detalles) {
            String idProduct = d.getProduct().getIdProduct();
            acumulado.merge(idProduct, d.getQuantity(), Integer::sum);
            nombres.put(idProduct, d.getProduct().getName());
        }

        List<Map<String, Object>> resultado = acumulado.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(limit)
                .map(e -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put(BusinessUtils.KEY_PRODUCT_NAME, nombres.get(e.getKey()));
                    item.put(BusinessUtils.KEY_TOTAL_QTY, e.getValue());
                    return item;
                })
                .toList();

        response.setListTopProducts(resultado);
        response.success();
        return response;
    }

    public ResponseSaleRecent getRecent(int limit) {
        ResponseSaleRecent response = new ResponseSaleRecent();

        List<EntitySale> ventas = repositorySale.findByStatusOrderBySaleDateDesc(STATUS_COMPLETADA)
                .stream()
                .limit(limit)
                .toList();

        List<Map<String, Object>> items = ventas.stream()
                .map(this::mapRecentSale)
                .toList();

        response.setListSales(items);
        response.success();
        return response;
    }

    private Map<String, Object> mapRecentSale(EntitySale s) {
        Map<String, Object> data = new HashMap<>();
        data.put(BusinessUtils.KEY_SALE_NUMBER, s.getSaleNumber());
        data.put(BusinessUtils.KEY_CUSTOMER_NAME, getCustomerName(s));
        data.put(BusinessUtils.KEY_TOTAL, s.getTotal());
        data.put(BusinessUtils.KEY_PAYMENT_METHOD, s.getPaymentMethod());
        data.put(BusinessUtils.KEY_SALE_DATE, s.getSaleDate().toString());
        return data;
    }

    private Date getStartOfDay(int daysAgo) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, -daysAgo);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    private String capitalize(String str) {
        if (str == null || str.isEmpty())
            return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    public ResponseSaleReport getReport(String from, String to) {
        ResponseSaleReport response = new ResponseSaleReport();
        try {
            Date fechaFrom = BusinessUtils.parseDate(from, false);
            Date fechaTo = BusinessUtils.parseDate(to, true);

            List<EntitySale> ventas = filterSalesByDateRange(fechaFrom, fechaTo);

            double totalMonto = sumTotals(ventas);
            double totalDescuento = sumDiscounts(ventas);
            double totalIgv = sumIgv(ventas);

            List<Map<String, Object>> detalle = ventas.stream().map(this::mapReportSale).toList();

            Map<String, Object> resumen = buildReportSummary(ventas.size(), totalMonto, totalDescuento, totalIgv);

            response.setResumen(resumen);
            response.setDetalle(detalle);
            response.success();

        } catch (RuntimeException e) {
            response.listMessage.add(BusinessUtils.MSG_ERROR_GENERAR_REPORTE + e.getMessage());
        }
        return response;
    }

    private List<EntitySale> filterSalesByDateRange(Date from, Date to) {
        return repositorySale.findAll().stream()
                .filter(s -> STATUS_COMPLETADA.equals(s.getStatus()))
                .filter(s -> s.getSaleDate() != null
                        && !s.getSaleDate().before(from)
                        && !s.getSaleDate().after(to))
                .sorted(Comparator.comparing(EntitySale::getSaleDate))
                .toList();
    }

    private double sumDiscounts(List<EntitySale> sales) {
        return sales.stream()
                .mapToDouble(s -> s.getDiscount() != null ? s.getDiscount().doubleValue() : 0).sum();
    }

    private double sumIgv(List<EntitySale> sales) {
        return sales.stream().mapToDouble(s -> s.getIgv().doubleValue()).sum();
    }

    private Map<String, Object> mapReportSale(EntitySale s) {
        Map<String, Object> data = new HashMap<>();
        data.put(BusinessUtils.KEY_ID_SALE, s.getIdSale());
        data.put(BusinessUtils.KEY_SALE_NUMBER, s.getSaleNumber());
        data.put(BusinessUtils.KEY_SALE_DATE, s.getSaleDate().toString());
        data.put(BusinessUtils.KEY_CUSTOMER_NAME, s.getCustomer() != null ? s.getCustomer().getName() : BusinessUtils.SIN_CLIENTE);
        data.put(BusinessUtils.KEY_USER_NAME, BusinessUtils.buildFullName(s.getUser()));
        data.put(BusinessUtils.KEY_PAYMENT_METHOD, s.getPaymentMethod());
        data.put(BusinessUtils.KEY_SUBTOTAL, BusinessUtils.formatBigDecimal(s.getSubtotal()));
        data.put(BusinessUtils.KEY_DISCOUNT, BusinessUtils.formatBigDecimal(s.getDiscount()));
        data.put(BusinessUtils.KEY_IGV, s.getIgv().toString());
        data.put(BusinessUtils.KEY_TOTAL, s.getTotal().toString());
        return data;
    }

    private Map<String, Object> buildReportSummary(int totalVentas, double totalMonto,
            double totalDescuento, double totalIgv) {
        Map<String, Object> resumen = new HashMap<>();
        resumen.put(BusinessUtils.KEY_TOTAL_VENTAS, totalVentas);
        resumen.put(BusinessUtils.KEY_TOTAL_MONTO, totalMonto);
        resumen.put(BusinessUtils.KEY_TOTAL_DESCUENTO, totalDescuento);
        resumen.put(BusinessUtils.KEY_TOTAL_IGV, totalIgv);
        resumen.put(BusinessUtils.KEY_TICKET_PROMEDIO, totalVentas == 0 ? 0 : totalMonto / totalVentas);
        return resumen;
    }

    public ResponseSaleReport getReportByUser(String from, String to) {
        ResponseSaleReport response = new ResponseSaleReport();
        try {
            Date fechaFrom = BusinessUtils.parseDate(from, false);
            Date fechaTo = BusinessUtils.parseDate(to, true);

            List<EntitySale> ventas = filterSalesByDateRange(fechaFrom, fechaTo);

            Map<String, Map<String, Object>> porUsuario = new LinkedHashMap<>();

            for (EntitySale s : ventas) {
                String idUser = s.getUser() != null ? s.getUser().getIdUser() : BusinessUtils.SIN_ID + "usuario";
                String userName = BusinessUtils.buildFullName(s.getUser());
                String role = s.getUser() != null ? s.getUser().getRole() : BusinessUtils.DEFAULT_VALUE;

                porUsuario.computeIfAbsent(idUser, k -> buildUserReportMap(idUser, userName, role));

                Map<String, Object> u = porUsuario.get(idUser);
                u.put(BusinessUtils.KEY_TOTAL_VENTAS, (int) u.get(BusinessUtils.KEY_TOTAL_VENTAS) + 1);
                u.put(BusinessUtils.KEY_TOTAL_MONTO, (double) u.get(BusinessUtils.KEY_TOTAL_MONTO) + s.getTotal().doubleValue());
            }

            List<Map<String, Object>> detalle = sortByTotalMontoDesc(new ArrayList<>(porUsuario.values()));
            double totalMonto = sumTotalMontoFromList(detalle);

            Map<String, Object> resumen = new HashMap<>();
            resumen.put(BusinessUtils.KEY_TOTAL_VENTAS, ventas.size());
            resumen.put(BusinessUtils.KEY_TOTAL_MONTO, totalMonto);
            resumen.put(BusinessUtils.KEY_TOTAL_USUARIOS, detalle.size());

            response.setResumen(resumen);
            response.setDetalle(detalle);
            response.success();

        } catch (RuntimeException e) {
            response.listMessage.add(BusinessUtils.MSG_ERROR_GENERAR_REPORTE + e.getMessage());
        }
        return response;
    }

    private Map<String, Object> buildUserReportMap(String idUser, String userName, String role) {
        Map<String, Object> m = new HashMap<>();
        m.put(BusinessUtils.KEY_ID_USER, idUser);
        m.put(BusinessUtils.KEY_USER_NAME, userName);
        m.put(BusinessUtils.KEY_ROLE, role);
        m.put(BusinessUtils.KEY_TOTAL_VENTAS, 0);
        m.put(BusinessUtils.KEY_TOTAL_MONTO, 0.0);
        return m;
    }

    private List<Map<String, Object>> sortByTotalMontoDesc(List<Map<String, Object>> list) {
        list.sort((a, b) -> Double.compare((double) b.get(BusinessUtils.KEY_TOTAL_MONTO), (double) a.get(BusinessUtils.KEY_TOTAL_MONTO)));
        return list;
    }

    private double sumTotalMontoFromList(List<Map<String, Object>> list) {
        return list.stream().mapToDouble(d -> (double) d.get(BusinessUtils.KEY_TOTAL_MONTO)).sum();
    }

    public ResponseSaleReport getReportByProduct(String from, String to) {
        ResponseSaleReport response = new ResponseSaleReport();
        try {
            Date fechaFrom = BusinessUtils.parseDate(from, false);
            Date fechaTo = BusinessUtils.parseDate(to, true);

            List<EntitySale> ventas = filterSalesByDateRange(fechaFrom, fechaTo);

            Map<String, Map<String, Object>> porProducto = new LinkedHashMap<>();

            for (EntitySale s : ventas) {
                List<EntitySaleDetail> detalles = repositorySaleDetail.findBySale_IdSale(s.getIdSale());
                for (EntitySaleDetail d : detalles) {
                    String idProduct = d.getProduct() != null ? d.getProduct().getIdProduct() : BusinessUtils.SIN_ID + "producto";
                    String productName = d.getProduct() != null ? d.getProduct().getName() : BusinessUtils.SIN_PRODUCTO;

                    porProducto.computeIfAbsent(idProduct, k -> buildProductReportMap(idProduct, productName));

                    Map<String, Object> p = porProducto.get(idProduct);
                    p.put(BusinessUtils.KEY_TOTAL_QTY, (int) p.get(BusinessUtils.KEY_TOTAL_QTY) + d.getQuantity());
                    p.put(BusinessUtils.KEY_TOTAL_MONTO, (double) p.get(BusinessUtils.KEY_TOTAL_MONTO) + d.getSubtotal().doubleValue());
                    p.put(BusinessUtils.KEY_VECES_VENDIDO, (int) p.get(BusinessUtils.KEY_VECES_VENDIDO) + 1);
                }
            }

            List<Map<String, Object>> detalle = sortByTotalQtyDesc(new ArrayList<>(porProducto.values()));
            int totalUnidades = sumTotalQty(detalle);
            double totalMonto = sumTotalMontoFromList(detalle);

            Map<String, Object> resumen = new HashMap<>();
            resumen.put(BusinessUtils.KEY_TOTAL_PRODUCTOS, detalle.size());
            resumen.put(BusinessUtils.KEY_TOTAL_UNIDADES, totalUnidades);
            resumen.put(BusinessUtils.KEY_TOTAL_MONTO, totalMonto);

            response.setResumen(resumen);
            response.setDetalle(detalle);
            response.success();

        } catch (RuntimeException e) {
            response.listMessage.add(BusinessUtils.MSG_ERROR_GENERAR_REPORTE + e.getMessage());
        }
        return response;
    }

    private Map<String, Object> buildProductReportMap(String idProduct, String productName) {
        Map<String, Object> m = new HashMap<>();
        m.put(BusinessUtils.KEY_ID_PRODUCT, idProduct);
        m.put(BusinessUtils.KEY_PRODUCT_NAME, productName);
        m.put(BusinessUtils.KEY_TOTAL_QTY, 0);
        m.put(BusinessUtils.KEY_TOTAL_MONTO, 0.0);
        m.put(BusinessUtils.KEY_VECES_VENDIDO, 0);
        return m;
    }

    private List<Map<String, Object>> sortByTotalQtyDesc(List<Map<String, Object>> list) {
        list.sort((a, b) -> Integer.compare((int) b.get(BusinessUtils.KEY_TOTAL_QTY), (int) a.get(BusinessUtils.KEY_TOTAL_QTY)));
        return list;
    }

    private int sumTotalQty(List<Map<String, Object>> list) {
        return list.stream().mapToInt(d -> (int) d.get(BusinessUtils.KEY_TOTAL_QTY)).sum();
    }
}