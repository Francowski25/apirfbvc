package com.epiis.apirfbvc.business;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.epiis.apirfbvc.dto.request.RequestSaleSave;
import com.epiis.apirfbvc.dto.response.ResponseSaleGetAll;
import com.epiis.apirfbvc.dto.response.ResponseSaleKpi;
import com.epiis.apirfbvc.dto.response.ResponseSaleRecent;
import com.epiis.apirfbvc.dto.response.ResponseSaleRecentUser;
import com.epiis.apirfbvc.dto.response.ResponseSaleReport;
import com.epiis.apirfbvc.dto.response.ResponseSaleSave;
import com.epiis.apirfbvc.dto.response.ResponseSaleTopProducts;
import com.epiis.apirfbvc.dto.response.ResponseSaleWeek;
import com.epiis.apirfbvc.dto.response.ResponseSaleWeekUser;
import com.epiis.apirfbvc.entity.EntityCustomer;
import com.epiis.apirfbvc.entity.EntityLot;
import com.epiis.apirfbvc.entity.EntityProduct;
import com.epiis.apirfbvc.entity.EntitySale;
import com.epiis.apirfbvc.entity.EntitySaleDetail;
import com.epiis.apirfbvc.entity.EntityUser;
import com.epiis.apirfbvc.repository.RepositoryCustomer;
import com.epiis.apirfbvc.repository.RepositoryLot;
import com.epiis.apirfbvc.repository.RepositorySale;
import com.epiis.apirfbvc.repository.RepositorySaleDetail;

@Service
public class BusinessSale {

    private final RepositorySale repositorySale;
    private final RepositorySaleDetail repositorySaleDetail;
    private final RepositoryLot repositoryLot;
    private final RepositoryCustomer repositoryCustomer;
 
    public BusinessSale(RepositorySale repositorySale, RepositorySaleDetail repositorySaleDetail,
			RepositoryLot repositoryLot, RepositoryCustomer repositoryCustomer) {
		this.repositorySale = repositorySale;
		this.repositorySaleDetail = repositorySaleDetail;
		this.repositoryLot = repositoryLot;
		this.repositoryCustomer = repositoryCustomer;
	}

	public ResponseSaleGetAll getAll() {
        ResponseSaleGetAll response = new ResponseSaleGetAll();

        List<EntitySale> ventas = repositorySale.findAll()
            .stream()
            .sorted((a, b) -> b.getSaleDate().compareTo(a.getSaleDate()))
            .collect(Collectors.toList());

        for (EntitySale s : ventas) {
            List<EntitySaleDetail> detalles = repositorySaleDetail.findBySale_IdSale(s.getIdSale());

            List<Map<String, String>> detallesMap = detalles.stream().map(d -> {
                Map<String, String> det = new HashMap<>();
                det.put("idSaleDetail", d.getIdSaleDetail());
                det.put("productName", d.getProduct() != null ? d.getProduct().getName() : "—");
                det.put("lotCode", d.getLot() != null ? d.getLot().getCode() : "—");
                det.put("quantity", String.valueOf(d.getQuantity()));
                det.put("unitPrice", d.getUnitPrice() != null ? d.getUnitPrice().toString() : "0");
                det.put("subtotal", d.getSubtotal() != null ? d.getSubtotal().toString() : "0");
                return det;
            }).collect(Collectors.toList());

            Map<String, Object> data = new HashMap<>();
            data.put("idSale", s.getIdSale());
            data.put("saleNumber", s.getSaleNumber());
            data.put("saleDate", s.getSaleDate() != null ? s.getSaleDate().toString() : "");
            data.put("customerName", s.getCustomer() != null ? s.getCustomer().getName() : "—");
            data.put("customerDocument", s.getCustomer() != null ? s.getCustomer().getDocumentNumber() : "—");
            data.put("userName", s.getUser() != null
                ? s.getUser().getFirstName() + " " + s.getUser().getSurName() : "—");
            data.put("subtotal", s.getSubtotal() != null ? s.getSubtotal().toString() : "0");
            data.put("discount", s.getDiscount() != null ? s.getDiscount().toString() : "0");
            data.put("igv", s.getIgv() != null ? s.getIgv().toString() : "0");
            data.put("total", s.getTotal() != null ? s.getTotal().toString() : "0");
            data.put("paymentMethod", s.getPaymentMethod());
            data.put("status", s.getStatus());
            data.put("detalles", detallesMap);

            response.getListSales().add(data);
        }

        response.success();
        return response;
    }

    public ResponseSaleSave save(RequestSaleSave request) {
        ResponseSaleSave response = new ResponseSaleSave();

        if (request.getItems() == null || request.getItems().isEmpty()) {
            response.listMessage.add("Debe agregar al menos un producto.");
            return response;
        }

        for (RequestSaleSave.SaleItem item : request.getItems()) {
        	String idLot = item.getIdLot();

        	if (idLot == null || idLot.isBlank()) {
        	    response.listMessage.add("El lote es obligatorio.");
        	    return response;
        	}

        	EntityLot lot = repositoryLot.findById(idLot).orElse(null);

        	if (lot == null) {
        	    response.listMessage.add("Lote no encontrado: " + idLot);
        	    return response;
        	}
        }

        BigDecimal subtotal = BigDecimal.ZERO;
        for (RequestSaleSave.SaleItem item : request.getItems()) {
            BigDecimal itemSubtotal = BigDecimal.valueOf(item.getUnitPrice())
                .multiply(BigDecimal.valueOf(item.getQuantity()));
            subtotal = subtotal.add(itemSubtotal);
        }

        BigDecimal discount = BigDecimal.valueOf(request.getDiscount() != null ? request.getDiscount() : 0);
        BigDecimal base = subtotal.subtract(discount);
        BigDecimal igv = base.multiply(BigDecimal.valueOf(0.18)).setScale(2, RoundingMode.HALF_UP);
        BigDecimal total = base.add(igv).setScale(2, RoundingMode.HALF_UP);

        long count = repositorySale.count() + 1;
        String saleNumber = String.format("V%06d", count);

        EntitySale sale = new EntitySale();
        sale.setIdSale(UUID.randomUUID().toString());
        sale.setSaleNumber(saleNumber);
        sale.setSaleDate(new Date());
        sale.setSubtotal(subtotal.setScale(2, RoundingMode.HALF_UP));
        sale.setDiscount(discount.setScale(2, RoundingMode.HALF_UP));
        sale.setIgv(igv);
        sale.setTotal(total);
        sale.setPaymentMethod(request.getPaymentMethod());
        sale.setStatus("Completada");
        sale.setCreatedAt(new java.sql.Date(new Date().getTime()));
        sale.setUpdatedAt(sale.getCreatedAt());

        String idCustomer = request.getIdCustomer();

        if (idCustomer != null && !idCustomer.isBlank()) {

            EntityCustomer customerRef = repositoryCustomer
                    .findById(idCustomer)
                    .orElseThrow(() -> new RuntimeException("Cliente no encontrado."));

            sale.setCustomer(customerRef);

        } else {
            sale.setCustomer(null);
        }

        EntityUser userRef = new EntityUser();
        userRef.setIdUser(request.getIdUser());
        sale.setUser(userRef);

        repositorySale.save(sale);

        for (RequestSaleSave.SaleItem item : request.getItems()) {
            BigDecimal itemSubtotal = BigDecimal.valueOf(item.getUnitPrice())
                .multiply(BigDecimal.valueOf(item.getQuantity()))
                .setScale(2, RoundingMode.HALF_UP);

            EntitySaleDetail detail = new EntitySaleDetail();
            detail.setIdSaleDetail(UUID.randomUUID().toString());
            detail.setQuantity(item.getQuantity());
            detail.setUnitPrice(BigDecimal.valueOf(item.getUnitPrice()));
            detail.setSubtotal(itemSubtotal);
            detail.setSale(sale);

            EntityProduct productRef = new EntityProduct();
            productRef.setIdProduct(item.getIdProduct());
            detail.setProduct(productRef);

            EntityLot lotRef = new EntityLot();
            lotRef.setIdLot(item.getIdLot());
            detail.setLot(lotRef);

            repositorySaleDetail.save(detail);
            
            String idLot = item.getIdLot();

            if (idLot == null || idLot.isBlank()) {
                continue;
            }

            EntityLot lot = repositoryLot.findById(idLot).orElse(null);

            if (lot == null) {
                continue;
            }

            lot.setCurrentStock(lot.getCurrentStock() - item.getQuantity());
            lot.setUpdatedAt(new java.sql.Date(new Date().getTime()));
            repositoryLot.save(lot);
        }

        response.success();
        response.listMessage.add("Venta registrada correctamente. N°: " + saleNumber);
        return response;
    }

    public ResponseSaleKpi getKpi() {
        ResponseSaleKpi response = new ResponseSaleKpi();

        Date inicioHoy = getStartOfDay(0);
        Date inicioAyer = getStartOfDay(1);
        Date finAyer = inicioHoy;

        List<EntitySale> ventasHoy = repositorySale.findBySaleDateGreaterThanEqualAndStatus(inicioHoy, "Completada");
        List<EntitySale> ventasAyer = repositorySale.findBySaleDateBetweenAndStatus(inicioAyer, finAyer, "Completada");

        double totalHoy = ventasHoy.stream().mapToDouble(s -> s.getTotal().doubleValue()).sum();
        double totalAyer = ventasAyer.stream().mapToDouble(s -> s.getTotal().doubleValue()).sum();

        Map<String, Object> kpi = new HashMap<>();
        kpi.put("ventasHoy", totalHoy);
        kpi.put("ventasAyer", totalAyer);
        kpi.put("transaccionesHoy", ventasHoy.size());

        response.setKpi(kpi);
        response.success();
        return response;
    }

    public ResponseSaleWeek getSalesWeek() {
        ResponseSaleWeek response = new ResponseSaleWeek();
        List<Map<String, Object>> resultado = new ArrayList<>();

        for (int i = 6; i >= 0; i--) {
            Date inicio = getStartOfDay(i);
            Date fin = getStartOfDay(i - 1);

            List<EntitySale> ventasDia = repositorySale.findBySaleDateBetweenAndStatus(inicio, fin, "Completada");
            double total = ventasDia.stream().mapToDouble(s -> s.getTotal().doubleValue()).sum();

            LocalDate fecha = LocalDate.now().minusDays(i);
            String diaNombre = fecha.getDayOfWeek()
                    .getDisplayName(TextStyle.SHORT, Locale.of("es", "ES"));
            
            Map<String, Object> item = new HashMap<>();
            item.put("dia", capitalize(diaNombre));
            item.put("fecha", fecha.toString());
            item.put("total", total);
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
            acumulado.merge(
            	    idProduct,
            	    d.getQuantity(),
            	    (a, b) -> a + b
            	);
            nombres.put(idProduct, d.getProduct().getName());
        }

        List<Map<String, Object>> resultado = acumulado.entrySet().stream()
            .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
            .limit(limit)
            .map(e -> {
                Map<String, Object> item = new HashMap<>();
                item.put("productName", nombres.get(e.getKey()));
                item.put("totalQty", e.getValue());
                return item;
            })
            .collect(Collectors.toList());

        response.setListTopProducts(resultado);
        response.success();
        return response;
    }

    public ResponseSaleRecent getRecent(int limit) {
        ResponseSaleRecent response = new ResponseSaleRecent();

        List<EntitySale> ventas = repositorySale.findByStatusOrderBySaleDateDesc("Completada")
            .stream()
            .limit(limit)
            .collect(Collectors.toList());

        List<Map<String, Object>> items = ventas.stream()
            .map(s -> {
                Map<String, Object> data = new HashMap<>();
                data.put("saleNumber", s.getSaleNumber());
                data.put("customerName", s.getCustomer() != null ? s.getCustomer().getName() : "—");
                data.put("total", s.getTotal());
                data.put("paymentMethod", s.getPaymentMethod());
                data.put("saleDate", s.getSaleDate().toString());
                return data;
            })
            .collect(Collectors.toList());

        response.setListSales(items);
        response.success();
        return response;
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
        if (str == null || str.isEmpty()) return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
    
    public ResponseSaleReport getReport(String from, String to) {
        ResponseSaleReport response = new ResponseSaleReport();
        try {
            Date fechaFrom = parseDate(from, false);
            Date fechaTo = parseDate(to, true);

            List<EntitySale> ventas = repositorySale.findAll().stream()
                .filter(s -> "Completada".equals(s.getStatus()))
                .filter(s -> s.getSaleDate() != null
                    && !s.getSaleDate().before(fechaFrom)
                    && !s.getSaleDate().after(fechaTo))
                .sorted((a, b) -> a.getSaleDate().compareTo(b.getSaleDate()))
                .collect(Collectors.toList());

            double totalMonto = ventas.stream()
                .mapToDouble(s -> s.getTotal().doubleValue()).sum();

            double totalDescuento = ventas.stream()
                .mapToDouble(s -> s.getDiscount() != null ? s.getDiscount().doubleValue() : 0).sum();

            double totalIgv = ventas.stream()
                .mapToDouble(s -> s.getIgv().doubleValue()).sum();

            List<Map<String, Object>> detalle = ventas.stream().map(s -> {
                Map<String, Object> data = new HashMap<>();
                data.put("idSale", s.getIdSale());
                data.put("saleNumber", s.getSaleNumber());
                data.put("saleDate", s.getSaleDate().toString());
                data.put("customerName", s.getCustomer() != null ? s.getCustomer().getName() : "Sin cliente");
                data.put("userName", s.getUser() != null
                    ? s.getUser().getFirstName() + " " + s.getUser().getSurName() : "—");
                data.put("paymentMethod", s.getPaymentMethod());
                data.put("subtotal", s.getSubtotal().toString());
                data.put("discount", s.getDiscount() != null ? s.getDiscount().toString() : "0");
                data.put("igv", s.getIgv().toString());
                data.put("total", s.getTotal().toString());
                return data;
            }).collect(Collectors.toList());

            Map<String, Object> resumen = new HashMap<>();
            resumen.put("totalVentas", ventas.size());
            resumen.put("totalMonto", totalMonto);
            resumen.put("totalDescuento", totalDescuento);
            resumen.put("totalIgv", totalIgv);
            resumen.put("ticketPromedio", ventas.isEmpty() ? 0 : totalMonto / ventas.size());

            response.setResumen(resumen);
            response.setDetalle(detalle);
            response.success();

        } catch (Exception e) {
            response.listMessage.add("Error al generar reporte: " + e.getMessage());
        }
        return response;
    }

    public ResponseSaleReport getReportByUser(String from, String to) {
        ResponseSaleReport response = new ResponseSaleReport();
        try {
            Date fechaFrom = parseDate(from, false);
            Date fechaTo = parseDate(to, true);

            List<EntitySale> ventas = repositorySale.findAll().stream()
                .filter(s -> "Completada".equals(s.getStatus()))
                .filter(s -> s.getSaleDate() != null
                    && !s.getSaleDate().before(fechaFrom)
                    && !s.getSaleDate().after(fechaTo))
                .collect(Collectors.toList());

            Map<String, Map<String, Object>> porUsuario = new LinkedHashMap<>();

            for (EntitySale s : ventas) {
                String idUser = s.getUser() != null ? s.getUser().getIdUser() : "sin-usuario";
                String userName = s.getUser() != null
                    ? s.getUser().getFirstName() + " " + s.getUser().getSurName() : "Sin usuario";
                String role = s.getUser() != null ? s.getUser().getRole() : "—";

                porUsuario.computeIfAbsent(idUser, k -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("idUser", idUser);
                    m.put("userName", userName);
                    m.put("role", role);
                    m.put("totalVentas", 0);
                    m.put("totalMonto", 0.0);
                    return m;
                });

                Map<String, Object> u = porUsuario.get(idUser);
                u.put("totalVentas", (int) u.get("totalVentas") + 1);
                u.put("totalMonto", (double) u.get("totalMonto") + s.getTotal().doubleValue());
            }

            List<Map<String, Object>> detalle = new ArrayList<>(porUsuario.values());
            detalle.sort((a, b) -> Double.compare((double) b.get("totalMonto"), (double) a.get("totalMonto")));

            double totalMonto = detalle.stream().mapToDouble(d -> (double) d.get("totalMonto")).sum();

            Map<String, Object> resumen = new HashMap<>();
            resumen.put("totalVentas", ventas.size());
            resumen.put("totalMonto", totalMonto);
            resumen.put("totalUsuarios", detalle.size());

            response.setResumen(resumen);
            response.setDetalle(detalle);
            response.success();

        } catch (Exception e) {
            response.listMessage.add("Error al generar reporte: " + e.getMessage());
        }
        return response;
    }

    public ResponseSaleReport getReportByProduct(String from, String to) {
        ResponseSaleReport response = new ResponseSaleReport();
        try {
            Date fechaFrom = parseDate(from, false);
            Date fechaTo = parseDate(to, true);

            List<EntitySale> ventas = repositorySale.findAll().stream()
                .filter(s -> "Completada".equals(s.getStatus()))
                .filter(s -> s.getSaleDate() != null
                    && !s.getSaleDate().before(fechaFrom)
                    && !s.getSaleDate().after(fechaTo))
                .collect(Collectors.toList());

            Map<String, Map<String, Object>> porProducto = new LinkedHashMap<>();

            for (EntitySale s : ventas) {
                List<EntitySaleDetail> detalles = repositorySaleDetail.findBySale_IdSale(s.getIdSale());
                for (EntitySaleDetail d : detalles) {
                    String idProduct = d.getProduct() != null ? d.getProduct().getIdProduct() : "sin-producto";
                    String productName = d.getProduct() != null ? d.getProduct().getName() : "Sin producto";

                    porProducto.computeIfAbsent(idProduct, k -> {
                        Map<String, Object> m = new HashMap<>();
                        m.put("idProduct", idProduct);
                        m.put("productName", productName);
                        m.put("totalQty", 0);
                        m.put("totalMonto", 0.0);
                        m.put("vecesVendido", 0);
                        return m;
                    });

                    Map<String, Object> p = porProducto.get(idProduct);
                    p.put("totalQty", (int) p.get("totalQty") + d.getQuantity());
                    p.put("totalMonto", (double) p.get("totalMonto") + d.getSubtotal().doubleValue());
                    p.put("vecesVendido", (int) p.get("vecesVendido") + 1);
                }
            }

            List<Map<String, Object>> detalle = new ArrayList<>(porProducto.values());
            detalle.sort((a, b) -> Integer.compare((int) b.get("totalQty"), (int) a.get("totalQty")));

            int totalUnidades = detalle.stream().mapToInt(d -> (int) d.get("totalQty")).sum();
            double totalMonto = detalle.stream().mapToDouble(d -> (double) d.get("totalMonto")).sum();

            Map<String, Object> resumen = new HashMap<>();
            resumen.put("totalProductos", detalle.size());
            resumen.put("totalUnidades", totalUnidades);
            resumen.put("totalMonto", totalMonto);

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
    
    public ResponseSaleWeekUser getDashboardSalesWeek(String idUser) {

        ResponseSaleWeekUser response = new ResponseSaleWeekUser();

        try {

            LocalDate hoy = LocalDate.now();
            LocalDate inicioSemana = hoy.with(DayOfWeek.MONDAY);

            String[] dias = { "Lun", "Mar", "Mié", "Jue", "Vie", "Sáb", "Dom" };
            double[] valores = new double[7];

            List<EntitySale> ventas = repositorySale.findAll().stream()
                    .filter(s -> "Completada".equals(s.getStatus()))
                    .filter(s -> s.getUser() != null)
                    .filter(s -> s.getUser().getIdUser().equals(idUser))
                    .filter(s -> s.getSaleDate() != null)
                    .collect(Collectors.toList());

            for (EntitySale venta : ventas) {

                LocalDate fecha = venta.getSaleDate().toInstant()
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate();

                if (!fecha.isBefore(inicioSemana) && !fecha.isAfter(hoy)) {

                    int index = fecha.getDayOfWeek().getValue() - 1;

                    valores[index] += venta.getTotal().doubleValue();
                }
            }

            response.setLabels(Arrays.asList(dias));

            response.setValues(Arrays.stream(valores)
                    .boxed()
                    .collect(Collectors.toList()));

            response.success();

        } catch (Exception e) {
            response.listMessage.add("Error al obtener ventas semanales: " + e.getMessage());
        }

        return response;
    }

    public ResponseSaleRecentUser getRecent(String idUser, int limit) {

        ResponseSaleRecentUser response = new ResponseSaleRecentUser();

        List<EntitySale> ventas = repositorySale.findAll().stream()
                .filter(s -> "Completada".equals(s.getStatus()))
                .filter(s -> s.getUser() != null)
                .filter(s -> s.getUser().getIdUser().equals(idUser))
                .sorted((a, b) -> b.getSaleDate().compareTo(a.getSaleDate()))
                .limit(limit)
                .collect(Collectors.toList());

        for (EntitySale item : ventas) {

            Map<String, Object> data = new HashMap<>();

            data.put("idSale", item.getIdSale());
            data.put("saleNumber", item.getSaleNumber());
            data.put("saleDate", item.getSaleDate().toString());
            data.put("customerName",
                    item.getCustomer() != null
                            ? item.getCustomer().getName()
                            : "Sin cliente");
            data.put("paymentMethod", item.getPaymentMethod());
            data.put("total", item.getTotal().toString());

            response.getListSales().add(data);
        }

        response.success();

        return response;
    }
}