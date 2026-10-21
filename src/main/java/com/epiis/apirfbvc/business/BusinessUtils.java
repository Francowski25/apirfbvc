package com.epiis.apirfbvc.business;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Date;

import com.epiis.apirfbvc.entity.EntityLot;
import com.epiis.apirfbvc.entity.EntityProduct;
import com.epiis.apirfbvc.entity.EntityUser;

import lombok.experimental.UtilityClass;

@UtilityClass
public class BusinessUtils {

    public static final String DEFAULT_VALUE = "—";
    public static final String TYPE_ENTRADA = "Entrada";
    public static final String TYPE_SALIDA = "Salida";
    public static final String TYPE_AJUSTE_POSITIVO = "Ajuste_Positivo";
    public static final String TYPE_AJUSTE_NEGATIVO = "Ajuste_Negativo";
    public static final String STATUS_ACTIVE = "activo";
    public static final String COST_ZERO = "0";
    public static final String ERROR_PREFIX = "Error al ";
    public static final String MSG_ERROR_GENERAR_REPORTE = "Error al generar reporte: ";
    public static final String MSG_ERROR_REGISTRAR_VENTA = "Error al registrar la venta: ";
    public static final String MSG_CLIENTE_NO_ENCONTRADO = "Cliente no encontrado.";
    public static final String MSG_VENTA_REGISTRADA = "Venta registrada correctamente. N°: ";
    public static final String SIN_CLIENTE = "Sin cliente";
    public static final String SIN_PRODUCTO = "Sin producto";
    public static final String SIN_ID = "sin-";

    public static final String DATE_PATTERN = "yyyy-MM-dd";
    public static final String LOT_DATE_PATTERN = "yyyyMMdd";
    public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern(DATE_PATTERN);
    public static final DateTimeFormatter LOT_DATE_FORMATTER = DateTimeFormatter.ofPattern(LOT_DATE_PATTERN);

    public static final String KEY_ID_MOVEMENT = "idMovement";
    public static final String KEY_MOVEMENT_DATE = "movementDate";
    public static final String KEY_TYPE = "type";
    public static final String KEY_OBSERVATION = "observation";
    public static final String KEY_USER_NAME = "userName";
    public static final String KEY_QUANTITY = "quantity";
    public static final String KEY_PRODUCT_NAME = "productName";
    public static final String KEY_LOT_CODE = "lotCode";
    public static final String KEY_UNIT_COST = "unitCost";
    public static final String KEY_ID_DETAIL = "idDetail";
    public static final String KEY_SUPPLIER_NAME = "supplierName";
    public static final String KEY_TOTAL_UNIDADES = "totalUnidades";
    public static final String KEY_COSTO_TOTAL = "costoTotal";
    public static final String KEY_SUBTOTAL = "subtotal";
    public static final String KEY_DETALLES = "detalles";
    public static final String KEY_ID_LOT = "idLot";
    public static final String KEY_CODE = "code";
    public static final String KEY_CURRENT_STOCK = "currentStock";
    public static final String KEY_STOCK_MINIMUM = "stockMinimum";
    public static final String KEY_PURCHASE_PRICE = "purchasePrice";
    public static final String KEY_EXPIRATION_DATE = "expirationDate";
    public static final String KEY_TOTAL_MOVEMENTS = "totalMovimientos";
    public static final String KEY_TOTAL_ENTRADAS = "totalEntradas";
    public static final String KEY_TOTAL_SALIDAS = "totalSalidas";
    public static final String KEY_TOTAL_AJUSTES = "totalAjustes";
    public static final String KEY_CANTIDAD_MOVIDA = "cantidadMovida";
    public static final String KEY_PRODUCTOS_CRITICOS = "productosCriticos";
    public static final String KEY_STOCK_TOTAL = "stockTotal";
    public static final String KEY_LOTES_POR_VENCER = "lotesPorVencer";
    public static final String KEY_STOCK_COMPROMETIDO = "stockComprometido";
    public static final String KEY_TOTAL_COMPRAS = "totalCompras";
    public static final String KEY_INVERSION_TOTAL = "inversionTotal";
    public static final String KEY_ID_SALE = "idSale";
    public static final String KEY_SALE_NUMBER = "saleNumber";
    public static final String KEY_SALE_DATE = "saleDate";
    public static final String KEY_CUSTOMER_NAME = "customerName";
    public static final String KEY_TOTAL = "total";
    public static final String KEY_PAYMENT_METHOD = "paymentMethod";
    public static final String KEY_DISCOUNT = "discount";
    public static final String KEY_IGV = "igv";
    public static final String KEY_ID_SALE_DETAIL = "idSaleDetail";
    public static final String KEY_ID_USER = "idUser";
    public static final String KEY_ROLE = "role";
    public static final String KEY_ID_PRODUCT = "idProduct";
    public static final String KEY_VECES_VENDIDO = "vecesVendido";
    public static final String KEY_TOTAL_QTY = "totalQty";
    public static final String KEY_TOTAL_MONTO = "totalMonto";
    public static final String KEY_TOTAL_VENTAS = "totalVentas";
    public static final String KEY_TICKET_PROMEDIO = "ticketPromedio";
    public static final String KEY_TOTAL_DESCUENTO = "totalDescuento";
    public static final String KEY_TOTAL_IGV = "totalIgv";
    public static final String KEY_TOTAL_USUARIOS = "totalUsuarios";
    public static final String KEY_TOTAL_PRODUCTOS = "totalProductos";
    public static final String KEY_VENTAS_HOY = "ventasHoy";
    public static final String KEY_VENTAS_AYER = "ventasAyer";
    public static final String KEY_TRANSACCIONES_HOY = "transaccionesHoy";
    public static final String KEY_DIA = "dia";
    public static final String KEY_FECHA = "fecha";

    public static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    public static boolean isNotBlank(String value) {
        return !isBlank(value);
    }

    public static String getValueOrDefault(String value) {
        return value != null ? value : "";
    }

    public static String buildFullName(EntityUser user) {
        if (user == null) {
            return DEFAULT_VALUE;
        }
        return user.getFirstName() + " " + user.getSurName();
    }

    public static String getRole(EntityUser user) {
        return user != null ? user.getRole() : DEFAULT_VALUE;
    }

    public static String getDni(EntityUser user) {
        return user != null ? user.getDni() : DEFAULT_VALUE;
    }

    public static String getProductName(EntityProduct product) {
        return product != null ? product.getName() : DEFAULT_VALUE;
    }

    public static String getLotCode(EntityLot lot) {
        return lot != null ? lot.getCode() : DEFAULT_VALUE;
    }

    public static String getLotExpirationDate(EntityLot lot) {
        return lot != null && lot.getExpirationDate() != null ? lot.getExpirationDate().toString() : DEFAULT_VALUE;
    }

    public static String getSupplierName(EntityLot lot) {
        return lot != null && lot.getSupplier() != null ? lot.getSupplier().getName() : DEFAULT_VALUE;
    }

    public static String formatBigDecimal(BigDecimal value) {
        return value != null ? value.toString() : COST_ZERO;
    }

    public static String formatDate(Date date) {
        return date != null ? date.toString() : "";
    }

    public static Date parseDate(String dateStr, boolean endOfDay) {
        try {
            if (isBlank(dateStr)) {
                return endOfDay ? new Date() : new Date(0);
            }
            LocalDate localDate = LocalDate.parse(dateStr.trim(), DATE_FORMATTER);
            if (endOfDay) {
                return Date.from(localDate.atTime(LocalTime.MAX).atZone(ZoneId.systemDefault()).toInstant());
            }
            return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        } catch (DateTimeParseException e) {
            return endOfDay ? new Date() : new Date(0);
        }
    }

    public static LocalDate toLocalDate(Date date) {
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    public static String buildErrorMessage(String operation, RuntimeException e) {
        return ERROR_PREFIX + operation + ": " + e.getMessage();
    }

    public static String buildReportErrorMessage(RuntimeException e) {
        return MSG_ERROR_GENERAR_REPORTE + e.getMessage();
    }
}