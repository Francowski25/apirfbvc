package com.epiis.apirfbvc.business;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.epiis.apirfbvc.dto.request.RequestSupplierInsert;
import com.epiis.apirfbvc.dto.request.RequestSupplierUpdate;
import com.epiis.apirfbvc.dto.response.ResponseSupplierGetAll;
import com.epiis.apirfbvc.dto.response.ResponseSupplierInsert;
import com.epiis.apirfbvc.dto.response.ResponseSupplierUpdate;
import com.epiis.apirfbvc.entity.EntitySupplier;
import com.epiis.apirfbvc.repository.RepositorySupplier;

@Service
public class BusinessSupplier {

    private static final String STATUS_ACTIVO = "activo";
    private static final String EMPTY = "";

    private static final String KEY_ID_SUPPLIER = "idSupplier";
    private static final String KEY_NAME = "name";
    private static final String KEY_RUC = "ruc";
    private static final String KEY_PHONE = "phone";
    private static final String KEY_ADDRESS = "address";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_STATUS = "status";
    private static final String KEY_CREATED_AT = "createdAt";

    private static final String MSG_YA_EXISTE_RUC = "Ya existe un proveedor con ese RUC.";
    private static final String MSG_REGISTRADO = "Proveedor registrado correctamente.";
    private static final String MSG_ID_OBLIGATORIO = "El id del proveedor es obligatorio.";
    private static final String MSG_NO_ENCONTRADO = "Proveedor no encontrado.";
    private static final String MSG_ACTUALIZADO = "Proveedor actualizado correctamente.";
    private static final String MSG_ESTADO_ACTUALIZADO = "Estado del proveedor actualizado a '%s' correctamente.";
    private static final String MSG_PROVEEDOR_NO_ENCONTRADO_ID = "No se encontró el proveedor con el ID proporcionado.";
    private static final String MSG_ERROR_ESTADO = "Error al actualizar el estado: ";

    private final RepositorySupplier repositorySupplier;

    public BusinessSupplier(RepositorySupplier repositorySupplier) {
        this.repositorySupplier = repositorySupplier;
    }

    public ResponseSupplierGetAll getAll() {
        ResponseSupplierGetAll response = new ResponseSupplierGetAll();
        List<Map<String, String>> items = repositorySupplier.findAll()
                .stream()
                .map(this::toMap)
                .toList();
        response.setListSuppliers(items);
        response.success();
        return response;
    }

    public ResponseSupplierInsert insert(RequestSupplierInsert request) {
        ResponseSupplierInsert response = new ResponseSupplierInsert();

        if (hasRuc(request.getRuc()) && repositorySupplier.existsByRuc(request.getRuc())) {
            response.listMessage.add(MSG_YA_EXISTE_RUC);
            return response;
        }

        EntitySupplier supplier = buildSupplier(request);
        repositorySupplier.save(supplier);

        response.success();
        response.listMessage.add(MSG_REGISTRADO);
        return response;
    }

    public ResponseSupplierUpdate update(RequestSupplierUpdate request) {
        ResponseSupplierUpdate response = new ResponseSupplierUpdate();

        if (isBlank(request.getIdSupplier())) {
            response.listMessage.add(MSG_ID_OBLIGATORIO);
            return response;
        }

        EntitySupplier supplier = repositorySupplier.findById(request.getIdSupplier()).orElse(null);
        if (supplier == null) {
            response.listMessage.add(MSG_NO_ENCONTRADO);
            return response;
        }

        if (hasRuc(request.getRuc())
                && repositorySupplier.existsByRucAndIdSupplierNot(request.getRuc(), request.getIdSupplier())) {
            response.listMessage.add(MSG_YA_EXISTE_RUC);
            return response;
        }

        updateEntity(supplier, request);
        repositorySupplier.save(supplier);

        response.success();
        response.listMessage.add(MSG_ACTUALIZADO);
        return response;
    }

    public ResponseSupplierInsert toggleStatus(String id, String newStatus) {
        ResponseSupplierInsert response = new ResponseSupplierInsert();
        try {
            String safeId = id != null ? id : EMPTY;
            String safeStatus = newStatus != null ? newStatus.toLowerCase() : STATUS_ACTIVO;

            Optional<EntitySupplier> optionalSupplier = repositorySupplier.findById(safeId);

            if (optionalSupplier.isPresent()) {
                EntitySupplier supplier = optionalSupplier.get();
                supplier.setStatus(safeStatus);
                supplier.setUpdatedAt(new java.sql.Date(new Date().getTime()));
                repositorySupplier.save(supplier);

                response.success();
                response.listMessage.add(String.format(MSG_ESTADO_ACTUALIZADO, safeStatus));
            } else {
                response.error();
                response.listMessage.add(MSG_PROVEEDOR_NO_ENCONTRADO_ID);
            }
        } catch (Exception e) {
            response.exception();
            response.listMessage.add(MSG_ERROR_ESTADO + e.getMessage());
        }

        return response;
    }

    private EntitySupplier buildSupplier(RequestSupplierInsert request) {
        EntitySupplier supplier = new EntitySupplier();
        supplier.setIdSupplier(UUID.randomUUID().toString());
        supplier.setName(request.getName());
        supplier.setRuc(request.getRuc());
        supplier.setPhone(request.getPhone());
        supplier.setAddress(request.getAddress());
        supplier.setEmail(request.getEmail());
        supplier.setStatus(STATUS_ACTIVO);
        supplier.setCreatedAt(new java.sql.Date(new Date().getTime()));
        supplier.setUpdatedAt(supplier.getCreatedAt());
        return supplier;
    }

    private void updateEntity(EntitySupplier supplier, RequestSupplierUpdate request) {
        supplier.setName(request.getName());
        supplier.setRuc(request.getRuc());
        supplier.setPhone(request.getPhone());
        supplier.setAddress(request.getAddress());
        supplier.setEmail(request.getEmail());
        supplier.setUpdatedAt(new java.sql.Date(new Date().getTime()));
    }

    private boolean hasRuc(String ruc) {
        return ruc != null && !ruc.isBlank();
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private Map<String, String> toMap(EntitySupplier s) {
        Map<String, String> data = new HashMap<>();
        data.put(KEY_ID_SUPPLIER, s.getIdSupplier());
        data.put(KEY_NAME, s.getName());
        data.put(KEY_RUC, defaultString(s.getRuc()));
        data.put(KEY_PHONE, defaultString(s.getPhone()));
        data.put(KEY_ADDRESS, defaultString(s.getAddress()));
        data.put(KEY_EMAIL, defaultString(s.getEmail()));
        data.put(KEY_STATUS, s.getStatus());
        data.put(KEY_CREATED_AT, s.getCreatedAt() != null ? s.getCreatedAt().toString() : EMPTY);
        return data;
    }

    private String defaultString(String value) {
        return value != null ? value : EMPTY;
    }
}