package com.epiis.apirfbvc.business;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

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

    private final RepositorySupplier repositorySupplier;

    public BusinessSupplier(RepositorySupplier repositorySupplier) {
        this.repositorySupplier = repositorySupplier;
    }

    public ResponseSupplierGetAll getAll() {
        ResponseSupplierGetAll response = new ResponseSupplierGetAll();
        List<EntitySupplier> list = repositorySupplier.findAll();
        List<Map<String, String>> items = list.stream()
            .map(this::toMap)
            .collect(Collectors.toList());
        response.setListSuppliers(items);
        response.success();
        return response;
    }

    public ResponseSupplierInsert insert(RequestSupplierInsert request) {
        ResponseSupplierInsert response = new ResponseSupplierInsert();

        if (request.getRuc() != null && !request.getRuc().isBlank()) {
            if (repositorySupplier.existsByRuc(request.getRuc())) {
                response.listMessage.add("Ya existe un proveedor con ese RUC.");
                return response;
            }
        }

        EntitySupplier supplier = new EntitySupplier();
        supplier.setIdSupplier(UUID.randomUUID().toString());
        supplier.setName(request.getName());
        supplier.setRuc(request.getRuc());
        supplier.setPhone(request.getPhone());
        supplier.setAddress(request.getAddress());
        supplier.setEmail(request.getEmail());
        supplier.setStatus("activo");
        supplier.setCreatedAt(new java.sql.Date(new Date().getTime()));
        supplier.setUpdatedAt(supplier.getCreatedAt());

        repositorySupplier.save(supplier);
        response.success();
        response.listMessage.add("Proveedor registrado correctamente.");
        return response;
    }

    public ResponseSupplierUpdate update(RequestSupplierUpdate request) {
        ResponseSupplierUpdate response = new ResponseSupplierUpdate();

        String idSupplier = request.getIdSupplier();

        if (idSupplier == null || idSupplier.isBlank()) {
            response.listMessage.add("El id del proveedor es obligatorio.");
            return response;
        }

        EntitySupplier supplier = repositorySupplier.findById(idSupplier)
                .orElse(null);

        if (supplier == null) {
            response.listMessage.add("Proveedor no encontrado.");
            return response;
        }
        
        if (request.getRuc() != null && !request.getRuc().isBlank()) {
            if (repositorySupplier.existsByRucAndIdSupplierNot(request.getRuc(), request.getIdSupplier())) {
                response.listMessage.add("Ya existe un proveedor con ese RUC.");
                return response;
            }
        }

        supplier.setName(request.getName());
        supplier.setRuc(request.getRuc());
        supplier.setPhone(request.getPhone());
        supplier.setAddress(request.getAddress());
        supplier.setEmail(request.getEmail());
        supplier.setUpdatedAt(new java.sql.Date(new Date().getTime()));

        repositorySupplier.save(supplier);
        response.success();
        response.listMessage.add("Proveedor actualizado correctamente.");
        return response;
    }

    public ResponseSupplierInsert toggleStatus(String id, String newStatus) {
    	ResponseSupplierInsert response = new ResponseSupplierInsert();
	    try {
	    	String safeId = id != null ? id : "";
	        String safeStatus = newStatus != null ? newStatus.toLowerCase() : "activo";
	        
	        Optional<EntitySupplier> optionalUser = repositorySupplier.findById(safeId);
	        
	        if (optionalUser.isPresent()) {
	            EntitySupplier user = optionalUser.get();
	            
	            user.setStatus(safeStatus);
	            user.setUpdatedAt(new java.sql.Date(new java.util.Date().getTime()));
	            
	            repositorySupplier.save(user);
	            
	            response.success();
	            response.listMessage.add("Estado del proveedor actualizado a '" + safeStatus + "' correctamente.");
	        } else {
	            response.error();
	            response.listMessage.add("No se encontró el proveedor con el ID proporcionado.");
	        }
	    } catch (Exception e) {
	        response.exception();
	        response.listMessage.add("Error al actualizar el estado: " + e.getMessage());
	    }
	    
	    return response;
    }

    private Map<String, String> toMap(EntitySupplier s) {
        Map<String, String> data = new HashMap<>();
        data.put("idSupplier", s.getIdSupplier());
        data.put("name", s.getName());
        data.put("ruc", s.getRuc() != null ? s.getRuc() : "");
        data.put("phone", s.getPhone() != null ? s.getPhone() : "");
        data.put("address", s.getAddress() != null ? s.getAddress() : "");
        data.put("email", s.getEmail() != null ? s.getEmail() : "");
        data.put("status", s.getStatus());
        data.put("createdAt", s.getCreatedAt() != null ? s.getCreatedAt().toString() : "");
        return data;
    }
}
