package com.epiis.apirfbvc.business;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.epiis.apirfbvc.dto.request.RequestMovementCreate;
import com.epiis.apirfbvc.dto.request.RequestMovementDetailItem;
import com.epiis.apirfbvc.dto.request.RequestMovementGetAll;
import com.epiis.apirfbvc.dto.response.ResponseKardex;
import com.epiis.apirfbvc.dto.response.ResponseMovementCreate;
import com.epiis.apirfbvc.dto.response.ResponseMovementDetail;
import com.epiis.apirfbvc.dto.response.ResponseMovementGetAll;
import com.epiis.apirfbvc.dto.response.ResponseProductSearch;
import com.epiis.apirfbvc.entity.EntityInventory;
import com.epiis.apirfbvc.entity.EntityInventoryMovement;
import com.epiis.apirfbvc.entity.EntityInventoryMovementDetail;
import com.epiis.apirfbvc.entity.EntityLot;
import com.epiis.apirfbvc.entity.EntityProduct;
import com.epiis.apirfbvc.repository.RepositoryInventory;
import com.epiis.apirfbvc.repository.RepositoryInventoryMovement;
import com.epiis.apirfbvc.repository.RepositoryInventoryMovementDetail;
import com.epiis.apirfbvc.repository.RepositoryLot;
import com.epiis.apirfbvc.repository.RepositoryProduct;
import com.epiis.apirfbvc.repository.RepositorySupplier;
import com.epiis.apirfbvc.repository.RepositoryUser;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BusinessMovement {

    private final RepositoryInventoryMovement repositoryMovement;
    private final RepositoryInventoryMovementDetail repositoryMovementDetail;
    private final RepositoryProduct repositoryProduct;
    private final RepositoryLot repositoryLot;
    private final RepositoryInventory repositoryInventory;
    private final RepositoryUser repositoryUser;
    private final RepositorySupplier repositorySupplier;

    public ResponseMovementGetAll getAll(RequestMovementGetAll request) {
        ResponseMovementGetAll response = new ResponseMovementGetAll();
        try {
            String safeType = BusinessUtils.isBlank(request.getType()) ? "Todos" : request.getType();

            int safePage = (request.getPage() != null && request.getPage() >= 0) ? request.getPage() : 0;
            int safeLimit = (request.getLimit() != null && request.getLimit() > 0) ? request.getLimit() : 20;

            Date safeDateFrom = BusinessUtils.parseDate(request.getDateFrom(), false);
            Date safeDateTo = BusinessUtils.parseDate(request.getDateTo(), true);

            List<EntityInventoryMovement> movimientos = "Todos".equalsIgnoreCase(safeType)
                    ? repositoryMovement.findAll()
                    : repositoryMovement.findByType(safeType);

            List<EntityInventoryMovement> filtrados = movimientos.stream()
                    .filter(m -> m.getMovementDate() != null
                            && !m.getMovementDate().before(safeDateFrom)
                            && !m.getMovementDate().after(safeDateTo))
                    .sorted(Comparator.comparing(EntityInventoryMovement::getMovementDate).reversed())
                    .toList();

            int totalElements = filtrados.size();
            int totalPages = (int) Math.ceil((double) totalElements / safeLimit);

            int fromIndex = Math.min(safePage * safeLimit, totalElements);
            int toIndex = Math.min(fromIndex + safeLimit, totalElements);

            List<EntityInventoryMovement> pagina = filtrados.subList(fromIndex, toIndex);

            for (EntityInventoryMovement m : pagina) {
                response.getListMovements().add(buildMovementSummaryMap(m));
            }

            response.setTotalElements(String.valueOf(totalElements));
            response.setTotalPages(String.valueOf(totalPages));
            response.setCurrentPage(String.valueOf(safePage));

            response.success();
        } catch (RuntimeException e) {
            handleException(response, "obtener los movimientos", e);
        }

        return response;
    }

    private Map<String, Object> buildMovementSummaryMap(EntityInventoryMovement m) {
        List<EntityInventoryMovementDetail> detalles = repositoryMovementDetail
                .findByMovement_IdMovement(m.getIdMovement());

        BigDecimal costoTotal = detalles.stream()
                .map(this::calculateSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, Object> data = new HashMap<>();
        data.put(BusinessUtils.KEY_ID_MOVEMENT, m.getIdMovement());
        data.put(BusinessUtils.KEY_TYPE, m.getType());
        data.put(BusinessUtils.KEY_MOVEMENT_DATE, m.getMovementDate().toString());
        data.put(BusinessUtils.KEY_OBSERVATION, BusinessUtils.getValueOrDefault(m.getObservation()));
        data.put(BusinessUtils.KEY_USER_NAME, BusinessUtils.buildFullName(m.getUser()));
        data.put(BusinessUtils.KEY_ROLE, BusinessUtils.getRole(m.getUser()));
        data.put("totalProductos", detalles.size());
        data.put("costoTotal", costoTotal.toString());

        return data;
    }

    public ResponseMovementDetail getDetail(String idMovement) {
        ResponseMovementDetail response = new ResponseMovementDetail();
        try {
            Optional<EntityInventoryMovement> optMovement = repositoryMovement.findById(idMovement);

            if (optMovement.isEmpty()) {
                response.error();
                response.listMessage.add("No se encontró el movimiento con el ID proporcionado.");
                return response;
            }

            EntityInventoryMovement movement = optMovement.get();

            List<EntityInventoryMovementDetail> detalles = repositoryMovementDetail
                    .findByMovement_IdMovement(movement.getIdMovement());

            List<Map<String, Object>> listDetail = detalles.stream()
                    .map(this::mapToDetailItem)
                    .toList();

            BigDecimal costoTotal = detalles.stream()
                    .map(this::calculateSubtotal)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            populateMovementUserData(response, movement);
            response.setIdMovement(movement.getIdMovement());
            response.setType(movement.getType());
            response.setMovementDate(movement.getMovementDate().toString());
            response.setObservation(BusinessUtils.getValueOrDefault(movement.getObservation()));
            response.setCostoTotal(costoTotal.toString());
            response.setListDetail(listDetail);

            response.success();
        } catch (RuntimeException e) {
            handleException(response, "obtener el detalle", e);
        }

        return response;
    }

    private Map<String, Object> mapToDetailItem(EntityInventoryMovementDetail d) {
        Map<String, Object> item = new HashMap<>();
        item.put(BusinessUtils.KEY_ID_DETAIL, d.getIdDetail());
        item.put(BusinessUtils.KEY_PRODUCT_NAME, BusinessUtils.getProductName(d.getProduct()));
        item.put(BusinessUtils.KEY_LOT_CODE, BusinessUtils.getLotCode(d.getLot()));
        item.put(BusinessUtils.KEY_EXPIRATION_DATE, BusinessUtils.getLotExpirationDate(d.getLot()));
        item.put(BusinessUtils.KEY_QUANTITY, d.getQuantity());
        item.put(BusinessUtils.KEY_UNIT_COST, BusinessUtils.formatBigDecimal(d.getUnitCost()));

        BigDecimal subtotal = calculateSubtotal(d);
        item.put(BusinessUtils.KEY_SUBTOTAL, subtotal.toString());
        return item;
    }

    private BigDecimal calculateSubtotal(EntityInventoryMovementDetail d) {
        return d.getUnitCost() != null
                ? d.getUnitCost().multiply(BigDecimal.valueOf(d.getQuantity()))
                : BigDecimal.ZERO;
    }

    private void populateMovementUserData(ResponseMovementDetail response, EntityInventoryMovement movement) {
        response.setUserName(BusinessUtils.buildFullName(movement.getUser()));
        response.setRole(BusinessUtils.getRole(movement.getUser()));
        response.setDni(BusinessUtils.getDni(movement.getUser()));
    }

    public ResponseKardex getKardexByProduct(String idProduct) {
        ResponseKardex response = new ResponseKardex();
        try {
            Optional<EntityProduct> optProduct = repositoryProduct.findById(idProduct);

            if (optProduct.isEmpty()) {
                response.error();
                response.listMessage.add("El producto no existe.");
                return response;
            }

            List<EntityInventoryMovementDetail> detalles = repositoryMovementDetail.findByProduct_IdProduct(idProduct);

            List<EntityInventoryMovementDetail> ordenados = detalles.stream()
                    .filter(d -> d.getMovement() != null && d.getMovement().getMovementDate() != null)
                    .sorted(Comparator.comparing(d -> d.getMovement().getMovementDate()))
                    .toList();

            List<Map<String, Object>> listKardex = new ArrayList<>();
            int saldo = 0;

            for (EntityInventoryMovementDetail d : ordenados) {
                int movimiento = calculateMovementQuantity(d.getMovement().getType(), d.getQuantity());
                saldo += movimiento;
                listKardex.add(mapToKardexItem(d, movimiento, saldo));
            }

            response.setIdProduct(idProduct);
            response.setProductName(optProduct.get().getName());
            response.setSaldoActual(String.valueOf(saldo));
            response.setListKardex(listKardex);

            response.success();
        } catch (RuntimeException e) {
            handleException(response, "generar el kardex", e);
        }

        return response;
    }

    private int calculateMovementQuantity(String type, int quantity) {
        boolean isPositive = BusinessUtils.TYPE_ENTRADA.equals(type)
                || BusinessUtils.TYPE_AJUSTE_POSITIVO.equals(type);
        return isPositive ? quantity : -quantity;
    }

    private Map<String, Object> mapToKardexItem(EntityInventoryMovementDetail d, int movimiento, int saldoResultante) {
        Map<String, Object> item = new HashMap<>();
        item.put(BusinessUtils.KEY_ID_DETAIL, d.getIdDetail());
        item.put(BusinessUtils.KEY_MOVEMENT_DATE, d.getMovement().getMovementDate().toString());
        item.put(BusinessUtils.KEY_TYPE, d.getMovement().getType());
        item.put(BusinessUtils.KEY_OBSERVATION, BusinessUtils.getValueOrDefault(d.getMovement().getObservation()));
        item.put(BusinessUtils.KEY_LOT_CODE, BusinessUtils.getLotCode(d.getLot()));
        item.put(BusinessUtils.KEY_EXPIRATION_DATE, BusinessUtils.getLotExpirationDate(d.getLot()));
        item.put(BusinessUtils.KEY_QUANTITY, d.getQuantity());
        item.put("movimiento", movimiento);
        item.put("saldoResultante", saldoResultante);
        item.put("responsable", BusinessUtils.buildFullName(d.getMovement().getUser()));
        item.put(BusinessUtils.KEY_UNIT_COST, BusinessUtils.formatBigDecimal(d.getUnitCost()));
        return item;
    }

    public ResponseProductSearch search(String q) {
        ResponseProductSearch response = new ResponseProductSearch();
        try {
            String safeQuery = (q != null ? q.trim().toLowerCase() : "");

            if (safeQuery.length() < 2) {
                response.success();
                return response;
            }

            List<EntityProduct> coincidencias = repositoryProduct.findAll().stream()
                    .filter(p -> BusinessUtils.STATUS_ACTIVE.equalsIgnoreCase(p.getStatus()))
                    .filter(p -> p.getName() != null && p.getName().toLowerCase().contains(safeQuery))
                    .limit(10)
                    .toList();

            for (EntityProduct p : coincidencias) {
                Map<String, Object> data = new HashMap<>();

                int stock = repositoryInventory.findById(p.getIdProduct())
                        .map(EntityInventory::getStock)
                        .orElse(0);

                data.put("idProduct", p.getIdProduct());
                data.put("name", p.getName());
                data.put("barcode", p.getBarcode());
                data.put("stock", stock);
                data.put("stockMinimum", p.getStockMinimum());
                data.put("priceSale", p.getPriceSale());

                response.getListProducts().add(data);
            }

            response.success();
        } catch (RuntimeException e) {
            handleException(response, "buscar productos", e);
        }

        return response;
    }

    private String generateLotCode() {
        LocalDate hoy = LocalDate.now();
        String prefix = "LOT-" + hoy.format(BusinessUtils.LOT_DATE_FORMATTER);
        long totalHoy = repositoryLot.findAll().stream()
                .filter(l -> l.getCreatedAt() != null)
                .filter(l -> BusinessUtils.toLocalDate(l.getCreatedAt()).isEqual(hoy))
                .count();

        String correlativo = String.format("%03d", totalHoy + 1);

        return prefix + correlativo;
    }

    public ResponseMovementCreate create(RequestMovementCreate request) {
        ResponseMovementCreate response = new ResponseMovementCreate();

        try {
            List<String> tiposValidos = List.of(
                    BusinessUtils.TYPE_ENTRADA,
                    BusinessUtils.TYPE_SALIDA,
                    BusinessUtils.TYPE_AJUSTE_POSITIVO,
                    BusinessUtils.TYPE_AJUSTE_NEGATIVO);

            if (!tiposValidos.contains(request.getType())) {
                response.error();
                response.listMessage.add("El tipo de movimiento no es válido.");
                return response;
            }

            boolean esEntrada = BusinessUtils.TYPE_ENTRADA.equals(request.getType())
                    || BusinessUtils.TYPE_AJUSTE_POSITIVO.equals(request.getType());
            boolean esSalida = BusinessUtils.TYPE_SALIDA.equals(request.getType())
                    || BusinessUtils.TYPE_AJUSTE_NEGATIVO.equals(request.getType());

            for (RequestMovementDetailItem item : request.getDetails()) {
                String error = validateMovementItem(item, esEntrada, esSalida);
                if (error != null) {
                    response.error();
                    response.listMessage.add(error);
                    return response;
                }
            }

            EntityInventoryMovement entityMovement = new EntityInventoryMovement();
            entityMovement.setIdMovement(UUID.randomUUID().toString());
            entityMovement.setType(request.getType());
            entityMovement.setObservation(request.getObservation());
            entityMovement.setMovementDate(new Date());
            entityMovement.setUser(repositoryUser.findById(request.getIdUser()).orElse(null));

            repositoryMovement.save(entityMovement);

            for (RequestMovementDetailItem item : request.getDetails()) {
                String processError = processMovementItem(item, entityMovement, esEntrada);
                if (processError != null) {
                    response.exception();
                    response.listMessage.add(processError);
                    return response;
                }
            }

            response.setIdMovement(entityMovement.getIdMovement());
            response.success();
            response.listMessage.add("Movimiento registrado correctamente.");

        } catch (RuntimeException e) {
            handleException(response, "registrar el movimiento", e);
        }

        return response;
    }

    private String validateMovementItem(RequestMovementDetailItem item, boolean esEntrada, boolean esSalida) {
        Optional<EntityProduct> optProduct = repositoryProduct.findById(item.getIdProduct());
        if (optProduct.isEmpty()) {
            return "El producto " + item.getIdProduct() + " no existe.";
        }

        EntityProduct product = optProduct.get();

        if (item.getQuantity() == null || item.getQuantity() <= 0) {
            return "La cantidad debe ser mayor a cero para el producto " + product.getName() + ".";
        }

        if (esEntrada) {
            return validateEntradaItem(item, product);
        }

        if (esSalida) {
            return validateSalidaItem(item, product);
        }

        return null;
    }

    private String validateEntradaItem(RequestMovementDetailItem item, EntityProduct product) {
        if (item.getUnitCost() == null || item.getUnitCost() <= 0) {
            return "El costo unitario es obligatorio para el producto " + product.getName() + ".";
        }

        boolean lotMissing = BusinessUtils.isBlank(item.getIdLot());
        boolean expirationMissing = BusinessUtils.isBlank(item.getExpirationDate());
        boolean supplierMissing = BusinessUtils.isBlank(item.getIdSupplier());

        if (lotMissing && (expirationMissing || supplierMissing)) {
            return "Para crear un lote nuevo de " + product.getName()
                    + " se requiere fecha de vencimiento y proveedor.";
        }

        return null;
    }

    private String validateSalidaItem(RequestMovementDetailItem item, EntityProduct product) {
        if (BusinessUtils.isBlank(item.getIdLot())) {
            return "Debe seleccionar un lote para descontar stock de " + product.getName() + ".";
        }

        Optional<EntityLot> optLot = repositoryLot.findById(item.getIdLot());
        if (optLot.isEmpty()) {
            return "El lote seleccionado para " + product.getName() + " no existe.";
        }

        if (optLot.get().getCurrentStock() < item.getQuantity()) {
            return "Stock insuficiente en el lote " + optLot.get().getCode()
                    + " para " + product.getName() + ". Disponible: " + optLot.get().getCurrentStock();
        }

        return null;
    }

    private String processMovementItem(RequestMovementDetailItem item, EntityInventoryMovement entityMovement,
            boolean esEntrada) {
        EntityProduct product = repositoryProduct.findById(item.getIdProduct()).orElseThrow();
        EntityLot lot;

        if (esEntrada && BusinessUtils.isBlank(item.getIdLot())) {
            if (item.getUnitCost() == null) {
                return "El costo unitario llegó nulo al momento de crear el lote para "
                        + product.getName() + ". Verifique el campo 'unitCost' en el request.";
            }
            lot = createLot(item, product, entityMovement.getMovementDate());
        } else {
            lot = updateLotStock(item, esEntrada, entityMovement.getMovementDate());
        }

        saveMovementDetail(item, entityMovement, product, lot, esEntrada);
        updateInventoryStock(item, esEntrada, entityMovement.getMovementDate());

        return null;
    }

    private EntityLot createLot(RequestMovementDetailItem item, EntityProduct product, Date movementDate) {
        EntityLot lot = new EntityLot();
        lot.setIdLot(UUID.randomUUID().toString());
        lot.setCode(generateLotCode());
        lot.setExpirationDate(LocalDate.parse(item.getExpirationDate()));
        lot.setPurchasePrice(BigDecimal.valueOf(item.getUnitCost()));
        lot.setCurrentStock(item.getQuantity());
        lot.setProduct(product);
        lot.setSupplier(repositorySupplier.findById(item.getIdSupplier()).orElse(null));
        lot.setCreatedAt(movementDate);
        lot.setUpdatedAt(movementDate);

        return repositoryLot.save(lot);
    }

    private EntityLot updateLotStock(RequestMovementDetailItem item, boolean esEntrada, Date movementDate) {
        EntityLot lot = repositoryLot.findById(item.getIdLot()).orElseThrow();

        int nuevoStockLote = esEntrada
                ? lot.getCurrentStock() + item.getQuantity()
                : lot.getCurrentStock() - item.getQuantity();

        lot.setCurrentStock(nuevoStockLote);
        lot.setUpdatedAt(movementDate);

        return repositoryLot.save(lot);
    }

    private void saveMovementDetail(RequestMovementDetailItem item, EntityInventoryMovement entityMovement,
            EntityProduct product, EntityLot lot, boolean esEntrada) {
        EntityInventoryMovementDetail detail = new EntityInventoryMovementDetail();
        detail.setIdDetail(UUID.randomUUID().toString());
        detail.setQuantity(item.getQuantity());
        detail.setUnitCost(esEntrada
                ? BigDecimal.valueOf(item.getUnitCost())
                : lot.getPurchasePrice());
        detail.setMovement(entityMovement);
        detail.setProduct(product);
        detail.setLot(lot);

        repositoryMovementDetail.save(detail);
    }

    private void updateInventoryStock(RequestMovementDetailItem item, boolean esEntrada, Date movementDate) {
        EntityInventory inventory = repositoryInventory.findById(item.getIdProduct())
                .orElseGet(() -> {
                    EntityInventory nuevo = new EntityInventory();
                    nuevo.setIdProduct(item.getIdProduct());
                    nuevo.setStock(0);
                    return nuevo;
                });

        int nuevoStockTotal = esEntrada
                ? inventory.getStock() + item.getQuantity()
                : inventory.getStock() - item.getQuantity();

        inventory.setStock(nuevoStockTotal);
        inventory.setLastUpdated(movementDate);

        repositoryInventory.save(inventory);
    }

    private static void handleException(Object response, String operation, RuntimeException e) {
        String message = BusinessUtils.buildErrorMessage(operation, e);
        if (response instanceof ResponseMovementGetAll r) {
            r.exception();
            r.listMessage.add(message);
        } else if (response instanceof ResponseMovementDetail r) {
            r.exception();
            r.listMessage.add(message);
        } else if (response instanceof ResponseKardex r) {
            r.exception();
            r.listMessage.add(message);
        } else if (response instanceof ResponseProductSearch r) {
            r.exception();
            r.listMessage.add(message);
        } else if (response instanceof ResponseMovementCreate r) {
            r.exception();
            r.listMessage.add(message);
        }
    }
}