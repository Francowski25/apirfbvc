package com.epiis.apirfbvc.business;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.epiis.apirfbvc.dto.request.RequestCategoryDetail;
import com.epiis.apirfbvc.dto.request.RequestCategoryDisable;
import com.epiis.apirfbvc.dto.request.RequestCategoryInsert;
import com.epiis.apirfbvc.dto.response.ResponseCategoryDetail;
import com.epiis.apirfbvc.dto.response.ResponseCategoryDisable;
import com.epiis.apirfbvc.dto.response.ResponseCategoryGetAll;
import com.epiis.apirfbvc.dto.response.ResponseCategoryInsert;
import com.epiis.apirfbvc.entity.EntityCategory;
import com.epiis.apirfbvc.repository.RepositoryCategory;
import com.epiis.apirfbvc.repository.RepositoryProduct;

@Service
public class BusinessCategory {
	private final RepositoryCategory repositoryCategory;
	private final RepositoryProduct repositoryProduct;

	public BusinessCategory(RepositoryCategory repositoryCategory, RepositoryProduct repositoryProduct) {
		this.repositoryCategory = repositoryCategory;
		this.repositoryProduct = repositoryProduct;
	}
	
	public ResponseCategoryInsert insert(RequestCategoryInsert request) throws IOException {
		ResponseCategoryInsert response = new ResponseCategoryInsert();
		
		EntityCategory entityCategory = new EntityCategory();
		
		if(repositoryCategory.existsByName(request.getName())) {
			response.listMessage.add("El nombre ya existe en el sistema.");
			return response;
		}
		
		entityCategory.setImage(request.getImage() == null || request.getImage().trim().isEmpty()
			        ? null
			        : request.getImage()
			);
		entityCategory.setIdCategory(UUID.randomUUID().toString());
		entityCategory.setName(request.getName());
		entityCategory.setStatus("activo");
		
		entityCategory.setCreatedAt(new java.sql.Date(new Date().getTime()));
		entityCategory.setUpdatedAt(entityCategory.getCreatedAt());

		repositoryCategory.save(entityCategory);
		
		response.success();
		response.listMessage.add("Registro realizado correctamente.");
		
		return response;
	}
	
	
	public ResponseCategoryGetAll getAll() {
		ResponseCategoryGetAll response = new ResponseCategoryGetAll();
		
		List<EntityCategory> listCategories = repositoryCategory.findAll();
		
		for(EntityCategory item: listCategories) {
			Map<String, String> data = new HashMap<>();
			
			data.put("idCategory", item.getIdCategory());
			data.put("image", item.getImage());
			data.put("name", item.getName());
			data.put("status", item.getStatus());
			data.put("totalProducts", String.valueOf(
			    repositoryProduct.countByCategory_IdCategory(item.getIdCategory())
			));
	        data.put("createdAt", item.getCreatedAt().toString());
			
			response.getListCategories().add(data);
		}
		
		response.success();
		
		return response;
	}

	public ResponseCategoryDisable updateCategoryStatus(String id, String newStatus) {
	    ResponseCategoryDisable response = new ResponseCategoryDisable();
	    try {
	        String safeId = id != null ? id : "";
	        String safeStatus = newStatus != null ? newStatus.toLowerCase() : "activo";

	        Optional<EntityCategory> optionalCategory = repositoryCategory.findById(safeId);

	        if (optionalCategory.isPresent()) {
	            EntityCategory entityCategory = optionalCategory.get();

	            if (entityCategory.getStatus().equalsIgnoreCase(safeStatus)) {
	                response.warning();
	                response.listMessage.add("La categoría ya se encuentra en estado '" + safeStatus + "'.");
	                return response;
	            }

	            if ("inactivo".equals(safeStatus)) {
	                long totalProducts = repositoryProduct.countByCategory_IdCategory(safeId);

	                if (totalProducts > 0) {
	                    response.warning();
	                    response.listMessage.add("No se puede desactivar la categoría porque tiene "
	                            + totalProducts + " producto(s) relacionado(s).");
	                    return response;
	                }
	            }

	            entityCategory.setStatus(safeStatus);
	            entityCategory.setUpdatedAt(new java.sql.Date(new java.util.Date().getTime()));

	            repositoryCategory.save(entityCategory);

	            response.success();
	            response.listMessage.add("Estado de la categoría actualizado a '" + safeStatus + "' correctamente.");
	        } else {
	            response.error();
	            response.listMessage.add("No se encontró la categoría con el ID proporcionado.");
	        }
	    } catch (Exception e) {
	        response.exception();
	        response.listMessage.add("Error al actualizar el estado: " + e.getMessage());
	    }

	    return response;
	}

	public ResponseCategoryDetail getDetail(RequestCategoryDetail request) {
	    ResponseCategoryDetail response = new ResponseCategoryDetail();

	    Optional<EntityCategory> optCategory = repositoryCategory.findById(request.getIdCategory());

	    if (optCategory.isEmpty()) {
	        response.listMessage.add("La categoría no existe.");
	        return response;
	    }

	    EntityCategory entityCategory = optCategory.get();

	    response.setIdCategory(entityCategory.getIdCategory());
	    response.setImage(entityCategory.getImage());
	    response.setName(entityCategory.getName());
	    response.setStatus(entityCategory.getStatus());
	    response.setTotalProducts(String.valueOf(
	        repositoryProduct.countByCategory_IdCategory(entityCategory.getIdCategory())
	    ));
	    response.setCreatedAt(entityCategory.getCreatedAt().toString());
	    response.setUpdatedAt(entityCategory.getUpdatedAt().toString());

	    response.success();

	    return response;
	}

}
