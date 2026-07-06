package com.epiis.apirfbvc.business;

import java.io.IOException;
import java.time.LocalDate;
import java.math.BigDecimal;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.epiis.apirfbvc.dto.request.RequestUserInsert;
import com.epiis.apirfbvc.dto.response.ResponseUserDashboardKpi;
import com.epiis.apirfbvc.dto.response.ResponseUserGetAll;
import com.epiis.apirfbvc.dto.response.ResponseUserInsert;
import com.epiis.apirfbvc.entity.EntitySale;
import com.epiis.apirfbvc.entity.EntityUser;
import com.epiis.apirfbvc.repository.RepositoryLot;
import com.epiis.apirfbvc.repository.RepositorySale;
import com.epiis.apirfbvc.repository.RepositoryUser;
import org.springframework.security.crypto.password.PasswordEncoder;

@Service
public class BusinessUser {
	private final RepositoryUser repositoryUser;
    private final PasswordEncoder passwordEncoder;
    private final RepositorySale repositorySale;
    private final RepositoryLot repositoryLot;
	
	public BusinessUser(
			RepositoryUser repositoryUser,
			RepositorySale repositorySale,
            PasswordEncoder passwordEncoder,
            RepositoryLot repositoryLot
	) {
		this.repositoryUser = repositoryUser;
		this.passwordEncoder = passwordEncoder;
		this.repositorySale = repositorySale;
		this.repositoryLot = repositoryLot;
	}
	
	public ResponseUserInsert insert(RequestUserInsert request) throws IOException {
		ResponseUserInsert response = new ResponseUserInsert();
		
		if (request.getDni() != null && repositoryUser.existsByDni(request.getDni())) {
	        response.error();
	        response.listMessage.add("El DNI ya se encuentra registrado en el sistema.");
	        return response;
	    }
	    
	    if (request.getEmail() != null && repositoryUser.existsByEmail(request.getEmail())) {
	        response.error();
	        response.listMessage.add("El correo electrónico ya se encuentra registrado en el sistema.");
	        return response;
	    }
		
		EntityUser entityUser = new EntityUser();
		
		entityUser.setImage(request.getImage() == null ? "avatar.png" : request.getImage());		
		entityUser.setIdUser(UUID.randomUUID().toString());
		entityUser.setDni(request.getDni());
		entityUser.setFirstName(request.getFirstName());
		entityUser.setSurName(request.getSurName());
		entityUser.setEmail(request.getEmail());
		entityUser.setCellPhone(request.getCellPhone());
		entityUser.setRole(request.getRole());
		entityUser.setStatus("activo");
		
		String password = request.getPassword();
		entityUser.setPassword(passwordEncoder.encode(password != null ? password : ""));
		
		entityUser.setCreatedAt(new java.sql.Date(new Date().getTime()));
		entityUser.setUpdatedAt(entityUser.getCreatedAt());

		repositoryUser.save(entityUser);
		
		response.success();
		response.listMessage.add("Registro realizado correctamente.");
		
		return response;
	}
	
	public ResponseUserGetAll getAll() {
		ResponseUserGetAll response = new ResponseUserGetAll();
		
		List<EntityUser> listEntityUsers = repositoryUser.findAll();
		
		for(EntityUser item: listEntityUsers) {
			Map<String, String> data = new HashMap<>();
			
			data.put("idUser", item.getIdUser());
			data.put("dni", item.getDni());		
			data.put("image", item.getImage());
			data.put("firstName", item.getFirstName());
			data.put("surName", item.getSurName());
			data.put("cellPhone", item.getCellPhone());		
			data.put("email", item.getEmail());
			data.put("role", item.getRole());
			data.put("status", item.getStatus());
			
			response.getListUsers().add(data);
		}
		
		response.success();
		
		return response;
	}

	public ResponseUserInsert updateUserStatus(String id, String newStatus) {
	    ResponseUserInsert response = new ResponseUserInsert();
	    try {
	    	String safeId = id != null ? id : "";
	        String safeStatus = newStatus != null ? newStatus.toLowerCase() : "activo";
	        
	        Optional<EntityUser> optionalUser = repositoryUser.findById(safeId);
	        
	        if (optionalUser.isPresent()) {
	            EntityUser user = optionalUser.get();
	            
	            user.setStatus(safeStatus);
	            user.setUpdatedAt(new java.sql.Date(new java.util.Date().getTime()));
	            
	            repositoryUser.save(user);
	            
	            response.success();
	            response.listMessage.add("Estado del usuario actualizado a '" + safeStatus + "' correctamente.");
	        } else {
	            response.error();
	            response.listMessage.add("No se encontró el usuario con el ID proporcionado.");
	        }
	    } catch (Exception e) {
	        response.exception();
	        response.listMessage.add("Error al actualizar el estado: " + e.getMessage());
	    }
	    
	    return response;
	}
	
	public ResponseUserDashboardKpi getDashboardKpi(String idUser) {

	    ResponseUserDashboardKpi response = new ResponseUserDashboardKpi();

	    try {

	        LocalDate hoy = LocalDate.now();

	        List<EntitySale> ventasHoy = repositorySale.findAll().stream()
	                .filter(s -> "Completada".equals(s.getStatus()))
	                .filter(s -> s.getUser() != null)
	                .filter(s -> s.getUser().getIdUser().equals(idUser))
	                .filter(s -> s.getSaleDate() != null)
	                .filter(s -> s.getSaleDate().toInstant()
	                        .atZone(ZoneId.systemDefault())
	                        .toLocalDate()
	                        .equals(hoy))
	                .collect(Collectors.toList());

	        double montoVendido = ventasHoy.stream()
	                .map(EntitySale::getTotal)
	                .mapToDouble(BigDecimal::doubleValue)
	                .sum();

	        double ticketPromedio =
	                ventasHoy.isEmpty()
	                ? 0
	                : montoVendido / ventasHoy.size();

	        long stockCritico = repositoryLot.findAll().stream()
	                .filter(l -> l.getProduct() != null)
	                .filter(l -> l.getCurrentStock() != null)
	                .filter(l -> l.getCurrentStock() <= l.getProduct().getStockMinimum())
	                .count();

	        Map<String, Object> resumen = new HashMap<>();

	        resumen.put("misVentasHoy", ventasHoy.size());
	        resumen.put("montoVendidoHoy", montoVendido);
	        resumen.put("ticketPromedio", ticketPromedio);
	        resumen.put("stockCritico", stockCritico);

	        response.setResumen(resumen);

	        response.success();

	    } catch (Exception e) {
	        response.listMessage.add("Error al obtener KPIs: " + e.getMessage());
	    }

	    return response;
	}
}
