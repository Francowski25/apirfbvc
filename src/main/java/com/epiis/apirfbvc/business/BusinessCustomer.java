package com.epiis.apirfbvc.business;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.epiis.apirfbvc.dto.request.RequestCustomerInsert;
import com.epiis.apirfbvc.dto.response.ResponseCustomerGetAll;
import com.epiis.apirfbvc.dto.response.ResponseCustomerInsert;
import com.epiis.apirfbvc.dto.response.ResponseCustomerReport;
import com.epiis.apirfbvc.entity.EntityCustomer;
import com.epiis.apirfbvc.entity.EntitySale;
import com.epiis.apirfbvc.repository.RepositoryCustomer;
import com.epiis.apirfbvc.repository.RepositorySale;

@Service
public class BusinessCustomer {
	private static final String TOTAL_COMPRAS = "totalCompras";
	private static final String TOTAL_MONTO = "totalMonto";

	private final RepositoryCustomer repositoryCustomer;
	private final RepositorySale repositorySale;

	public BusinessCustomer(RepositoryCustomer repositoryCustomer,
			RepositorySale repositorySale) {
		this.repositoryCustomer = repositoryCustomer;
		this.repositorySale = repositorySale;
	}

	public ResponseCustomerGetAll getAll() {
		ResponseCustomerGetAll response = new ResponseCustomerGetAll();

		List<EntityCustomer> listEntityCustomers = repositoryCustomer.findAll();

		for (EntityCustomer item : listEntityCustomers) {
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

	public ResponseCustomerReport getReportFrequent() {
		ResponseCustomerReport response = new ResponseCustomerReport();

		try {

			List<EntitySale> ventas = repositorySale.findAll().stream()
					.filter(s -> "Completada".equals(s.getStatus()))
					.filter(s -> s.getCustomer() != null)
					.toList();

			Map<String, Map<String, Object>> porCliente = new LinkedHashMap<>();

			for (EntitySale s : ventas) {

				String idCustomer = s.getCustomer().getIdCustomer();
				String customerName = s.getCustomer().getName();
				String documentType = s.getCustomer().getDocumentType();
				String documentNumber = s.getCustomer().getDocumentNumber();

				porCliente.computeIfAbsent(idCustomer, k -> {
					Map<String, Object> map = new HashMap<>();
					map.put("idCustomer", idCustomer);
					map.put("customerName", customerName);
					map.put("documentType", documentType);
					map.put("documentNumber", documentNumber);
					map.put(TOTAL_COMPRAS, 0);
					map.put(TOTAL_MONTO, 0.0);
					return map;
				});

				Map<String, Object> cliente = porCliente.get(idCustomer);

				cliente.put(TOTAL_COMPRAS,
						(int) cliente.get(TOTAL_COMPRAS) + 1);

				cliente.put(TOTAL_MONTO,
						(double) cliente.get(TOTAL_MONTO)
								+ s.getTotal().doubleValue());
			}

			List<Map<String, Object>> detalle = new ArrayList<>(porCliente.values());

			detalle.sort((a, b) -> Integer.compare(
					(int) b.get(TOTAL_COMPRAS),
					(int) a.get(TOTAL_COMPRAS)));

			double totalMonto = detalle.stream()
					.mapToDouble(d -> (double) d.get(TOTAL_MONTO))
					.sum();

			Map<String, Object> resumen = new HashMap<>();
			resumen.put("totalClientes", detalle.size());
			resumen.put(TOTAL_COMPRAS, ventas.size());
			resumen.put(TOTAL_MONTO, totalMonto);

			response.setResumen(resumen);
			response.setDetalle(detalle);
			response.success();

		} catch (Exception e) {
			response.listMessage.add("Error al generar reporte: " + e.getMessage());
		}

		return response;
	}
}