package com.epiis.apirfbvc.business;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
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

        if (request.getIdCustomer() == null || request.getIdCustomer().isBlank()) {
            response.listMessage.add("El cliente es obligatorio.");
            return response;
        }

        if (request.getItems() == null || request.getItems().isEmpty()) {
            response.listMessage.add("Debe agregar al menos un producto.");
            return response;
        }

        EntityCustomer customer = repositoryCustomer.findById(request.getIdCustomer()).orElse(null);
        if (customer == null) {
            response.listMessage.add("Cliente no encontrado.");
            return response;
        }

        for (RequestSaleSave.SaleItem item : request.getItems()) {
            EntityLot lot = repositoryLot.findById(item.getIdLot()).orElse(null);
            if (lot == null) {
                response.listMessage.add("Lote no encontrado: " + item.getIdLot());
                return response;
            }
            if (lot.getCurrentStock() < item.getQuantity()) {
                response.listMessage.add("Stock insuficiente en lote " + lot.getCode()
                    + ". Disponible: " + lot.getCurrentStock());
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

        EntityCustomer customerRef = new EntityCustomer();
        customerRef.setIdCustomer(request.getIdCustomer());
        sale.setCustomer(customerRef);

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

            EntityLot lot = repositoryLot.findById(item.getIdLot()).orElse(null);
            if (lot == null) continue;

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
            String diaNombre = fecha.getDayOfWeek().getDisplayName(TextStyle.SHORT, new Locale("es", "ES"));

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
            acumulado.merge(idProduct, d.getQuantity(), Integer::sum);
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
}