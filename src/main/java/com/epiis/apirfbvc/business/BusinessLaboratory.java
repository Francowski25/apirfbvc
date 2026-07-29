package com.epiis.apirfbvc.business;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.epiis.apirfbvc.dto.request.RequestLaboratoryDetail;
import com.epiis.apirfbvc.dto.request.RequestLaboratoryInsert;
import com.epiis.apirfbvc.dto.response.ResponseLaboratoryDetail;
import com.epiis.apirfbvc.dto.response.ResponseLaboratoryGetAll;
import com.epiis.apirfbvc.dto.response.ResponseLaboratoryInsert;
import com.epiis.apirfbvc.dto.response.ResponseLaboratoryStatus;
import com.epiis.apirfbvc.entity.EntityLaboratory;
import com.epiis.apirfbvc.repository.RepositoryLaboratory;
import com.epiis.apirfbvc.repository.RepositoryProduct;

@Service
public class BusinessLaboratory {
	private final RepositoryLaboratory repositoryLaboratory;
    private final RepositoryProduct repositoryProduct;

    public BusinessLaboratory(RepositoryLaboratory repositoryLaboratory,
            RepositoryProduct repositoryProduct) {
		this.repositoryLaboratory = repositoryLaboratory;
		this.repositoryProduct = repositoryProduct;
	}
	
    public ResponseLaboratoryInsert insert(RequestLaboratoryInsert request) throws IOException {
		ResponseLaboratoryInsert response = new ResponseLaboratoryInsert();
		
		EntityLaboratory entityLaboratory = new EntityLaboratory();
		
		if(repositoryLaboratory.existsByName(request.getName())) {
			response.listMessage.add("El nombre ya existe en el sistema.");
			return response;
		}
		
		entityLaboratory.setImage(request.getImage() == null || request.getImage().trim().isEmpty()
			        ? null
			        : request.getImage()
			);
		entityLaboratory.setIdLaboratory(UUID.randomUUID().toString());
		entityLaboratory.setName(request.getName());
		entityLaboratory.setStatus("activo");
		
		entityLaboratory.setCreatedAt(new java.sql.Date(new Date().getTime()));
		entityLaboratory.setUpdatedAt(entityLaboratory.getCreatedAt());

		repositoryLaboratory.save(entityLaboratory);
		
		response.success();
		response.listMessage.add("Registro realizado correctamente.");
		
		return response;
	}
    
	public ResponseLaboratoryGetAll getAll() {
		ResponseLaboratoryGetAll response = new ResponseLaboratoryGetAll();
		
		List<EntityLaboratory> listLaboratories = repositoryLaboratory.findAll();
		
		for(EntityLaboratory item: listLaboratories) {
			Map<String, String> data = new HashMap<>();
			
			data.put("idLaboratory", item.getIdLaboratory());
			data.put("image", item.getImage());
			data.put("name", item.getName());
			data.put("status", item.getStatus());
			data.put("totalProducts", String.valueOf(
	                repositoryProduct.countByLaboratory_IdLaboratory(item.getIdLaboratory())
	            ));
	        data.put("createdAt", item.getCreatedAt().toString());

			response.getListLaboratories().add(data);
		}
		
		response.success();
		
		return response;
	}
	
	public ResponseLaboratoryStatus updateLaboratoryStatus(String id, String newStatus) {
	    ResponseLaboratoryStatus response = new ResponseLaboratoryStatus();
	    try {
	        String safeId = id != null ? id : "";
	        String safeStatus = newStatus != null ? newStatus.toLowerCase() : "activo";

	        Optional<EntityLaboratory> optionalLaboratory = repositoryLaboratory.findById(safeId);

	        if (optionalLaboratory.isPresent()) {
	            EntityLaboratory entityLaboratory = optionalLaboratory.get();

	            if (entityLaboratory.getStatus().equalsIgnoreCase(safeStatus)) {
	                response.warning();
	                response.listMessage.add("El laboratorio ya se encuentra en estado '" + safeStatus + "'.");
	                return response;
	            }

	            if ("inactivo".equals(safeStatus)) {
	                long totalProducts = repositoryProduct.countByLaboratory_IdLaboratory(safeId);

	                if (totalProducts > 0) {
	                    response.warning();
	                    response.listMessage.add("No se puede desactivar el laboratorio porque tiene "
	                            + totalProducts + " producto(s) relacionado(s).");
	                    return response;
	                }
	            }

	            entityLaboratory.setStatus(safeStatus);
	            entityLaboratory.setUpdatedAt(new java.sql.Date(new java.util.Date().getTime()));

	            repositoryLaboratory.save(entityLaboratory);

	            response.success();
	            response.listMessage.add("Estado del laboratorio actualizado a '" + safeStatus + "' correctamente.");
	        } else {
	            response.error();
	            response.listMessage.add("No se encontró el laboratorio con el ID proporcionado.");
	        }
	    } catch (Exception e) {
	        response.exception();
	        response.listMessage.add("Error al actualizar el estado: " + e.getMessage());
	    }

	    return response;
	}

	public ResponseLaboratoryDetail getDetail(RequestLaboratoryDetail request) {
		ResponseLaboratoryDetail response = new ResponseLaboratoryDetail();

		Optional<EntityLaboratory> optLaboratory = repositoryLaboratory.findById(request.getIdLaboratory());

		if (optLaboratory.isEmpty()) {
			response.listMessage.add("El laboratorio no existe.");
			return response;
		}

		EntityLaboratory entityLaboratory = optLaboratory.get();

		response.setIdLaboratory(entityLaboratory.getIdLaboratory());
		response.setImage(entityLaboratory.getImage());
		response.setName(entityLaboratory.getName());
		response.setStatus(entityLaboratory.getStatus());
		response.setTotalProducts(String.valueOf(
			repositoryProduct.countByLaboratory_IdLaboratory(entityLaboratory.getIdLaboratory())
		));
		response.setCreatedAt(entityLaboratory.getCreatedAt().toString());
		response.setUpdatedAt(entityLaboratory.getUpdatedAt().toString());

		response.success();

		return response;
	}
	
}
