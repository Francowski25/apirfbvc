package com.epiis.apirfbvc.business;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.epiis.apirfbvc.dto.request.RequestCustomerInsert;
import com.epiis.apirfbvc.dto.response.ResponseCustomerGetAll;
import com.epiis.apirfbvc.dto.response.ResponseCustomerInsert;
import com.epiis.apirfbvc.entity.EntityCustomer;
import com.epiis.apirfbvc.repository.RepositoryCustomer;

@Service
public class BusinessCustomer {
	private final RepositoryCustomer repositoryCustomer;

	public BusinessCustomer(RepositoryCustomer repositoryCustomer) {
		this.repositoryCustomer = repositoryCustomer;
	}
	
	public ResponseCustomerGetAll getAll() {
		ResponseCustomerGetAll response = new ResponseCustomerGetAll();
		
		List<EntityCustomer> listEntityCustomers = repositoryCustomer.findAll();
		
		for(EntityCustomer item: listEntityCustomers) {
			Map<String, String> data = new HashMap<>();
			
			data.put("idCustomer", item.getIdCustomer());
            data.put("documentType", item.getDocumentType());
            data.put("documentNumber", item.getDocumentNumber());
            data.put("name", item.getName());
            data.put("createdAt", item.getCreatedAt().toString());
            
			response.getListCustomers().add(data);
		}
		
		response.success();
		
		return response;
	}
	

	public ResponseCustomerInsert insert(RequestCustomerInsert request) {
	    ResponseCustomerInsert response = new ResponseCustomerInsert();

	    if (request.getDocumentNumber() == null || request.getDocumentNumber().isBlank()) {
	        response.listMessage.add("El número de documento es obligatorio.");
	        return response;
	    }

	    if (request.getName() == null || request.getName().isBlank()) {
	        response.listMessage.add("El nombre es obligatorio.");
	        return response;
	    }

	    if (repositoryCustomer.existsByDocumentNumber(request.getDocumentNumber())) {
	        response.listMessage.add("Ya existe un cliente con ese número de documento.");
	        return response;
	    }

	    EntityCustomer customer = new EntityCustomer();
	    customer.setIdCustomer(UUID.randomUUID().toString());
	    customer.setDocumentType(request.getDocumentType() != null ? request.getDocumentType() : "DNI");
	    customer.setDocumentNumber(request.getDocumentNumber());
	    customer.setName(request.getName());
		customer.setCreatedAt(new java.sql.Date(new Date().getTime()));

	    repositoryCustomer.save(customer);

	    response.success();
	    response.listMessage.add("Cliente registrado correctamente.");
	    return response;
	}
}
