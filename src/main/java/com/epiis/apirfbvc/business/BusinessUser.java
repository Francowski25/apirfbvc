package com.epiis.apirfbvc.business;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.epiis.apirfbvc.dto.request.RequestUserInsert;
import com.epiis.apirfbvc.dto.request.RequestUserUpdatePassword;
import com.epiis.apirfbvc.dto.request.RequestUserUpdateProfile;
import com.epiis.apirfbvc.dto.response.ResponseUserDashboardKpi;
import com.epiis.apirfbvc.dto.response.ResponseUserGetAll;
import com.epiis.apirfbvc.dto.response.ResponseUserInsert;
import com.epiis.apirfbvc.entity.EntitySale;
import com.epiis.apirfbvc.entity.EntityUser;
import com.epiis.apirfbvc.repository.RepositoryLot;
import com.epiis.apirfbvc.repository.RepositorySale;
import com.epiis.apirfbvc.repository.RepositoryUser;

@Service
public class BusinessUser {

	private static final String STATUS_ACTIVO = "activo";
	private static final String DEFAULT_AVATAR = "avatar.png";
	private static final String STATUS_COMPLETADA = "Completada";
	private static final String EMPTY = "";

	private static final String KEY_ID_USER = "idUser";
	private static final String KEY_DNI = "dni";
	private static final String KEY_IMAGE = "image";
	private static final String KEY_FIRST_NAME = "firstName";
	private static final String KEY_SUR_NAME = "surName";
	private static final String KEY_CELL_PHONE = "cellPhone";
	private static final String KEY_EMAIL = "email";
	private static final String KEY_ROLE = "role";
	private static final String KEY_STATUS = "status";
	private static final String KEY_MIS_VENTAS_HOY = "misVentasHoy";
	private static final String KEY_MONTO_VENDIDO_HOY = "montoVendidoHoy";
	private static final String KEY_TICKET_PROMEDIO = "ticketPromedio";
	private static final String KEY_STOCK_CRITICO = "stockCritico";

	private static final String MSG_DNI_EXISTE = "El DNI ya se encuentra registrado en el sistema.";
	private static final String MSG_EMAIL_EXISTE = "El correo electrónico ya se encuentra registrado en el sistema.";
	private static final String MSG_REGISTRO_OK = "Registro realizado correctamente.";
	private static final String MSG_USUARIO_NO_ENCONTRADO = "No se encontró el usuario con el ID proporcionado.";
	private static final String MSG_ESTADO_OK = "Estado del usuario actualizado a '%s' correctamente.";
	private static final String MSG_ERROR_ESTADO = "Error al actualizar el estado: ";
	private static final String MSG_ERROR_KPIS = "Error al obtener KPIs: ";
	private static final String MSG_EMAIL_EN_USO = "El correo ya está en uso por otro usuario.";
	private static final String MSG_PERFIL_OK = "Perfil actualizado correctamente.";
	private static final String MSG_ERROR_PERFIL = "Error al actualizar el perfil: ";
	private static final String MSG_ACTUALIZACION_OK = "Contraseña actualizada correctamente.";
	private static final String MSG_ERROR_ACTUALIZACION = "Error al actualizar la contraseña: ";

	private final RepositoryUser repositoryUser;
	private final PasswordEncoder passwordEncoder;
	private final RepositorySale repositorySale;
	private final RepositoryLot repositoryLot;

	public BusinessUser(
			RepositoryUser repositoryUser,
			RepositorySale repositorySale,
			PasswordEncoder passwordEncoder,
			RepositoryLot repositoryLot) {
		this.repositoryUser = repositoryUser;
		this.passwordEncoder = passwordEncoder;
		this.repositorySale = repositorySale;
		this.repositoryLot = repositoryLot;
	}

	public ResponseUserInsert insert(RequestUserInsert request) {
		ResponseUserInsert response = new ResponseUserInsert();

		if (request.getDni() != null && repositoryUser.existsByDni(request.getDni())) {
			response.error();
			response.listMessage.add(MSG_DNI_EXISTE);
			return response;
		}

		if (request.getEmail() != null && repositoryUser.existsByEmail(request.getEmail())) {
			response.error();
			response.listMessage.add(MSG_EMAIL_EXISTE);
			return response;
		}

		EntityUser entityUser = buildEntityUser(request);
		repositoryUser.save(entityUser);

		response.success();
		response.listMessage.add(MSG_REGISTRO_OK);

		return response;
	}

	private EntityUser buildEntityUser(RequestUserInsert request) {
		EntityUser entityUser = new EntityUser();

		entityUser.setImage(request.getImage() == null ? DEFAULT_AVATAR : request.getImage());
		entityUser.setIdUser(UUID.randomUUID().toString());
		entityUser.setDni(request.getDni());
		entityUser.setFirstName(request.getFirstName());
		entityUser.setSurName(request.getSurName());
		entityUser.setEmail(request.getEmail());
		entityUser.setCellPhone(request.getCellPhone());
		entityUser.setRole(request.getRole());
		entityUser.setStatus(STATUS_ACTIVO);

		String password = request.getPassword();
		entityUser.setPassword(passwordEncoder.encode(password != null ? password : EMPTY));

		entityUser.setCreatedAt(new java.sql.Date(new Date().getTime()));
		entityUser.setUpdatedAt(entityUser.getCreatedAt());

		return entityUser;
	}

	public ResponseUserGetAll getAll() {
		ResponseUserGetAll response = new ResponseUserGetAll();

		for (EntityUser item : repositoryUser.findAll()) {
			response.getListUsers().add(toMap(item));
		}

		response.success();
		return response;
	}

	private Map<String, String> toMap(EntityUser item) {
		Map<String, String> data = new HashMap<>();
		data.put(KEY_ID_USER, item.getIdUser());
		data.put(KEY_DNI, item.getDni());
		data.put(KEY_IMAGE, item.getImage());
		data.put(KEY_FIRST_NAME, item.getFirstName());
		data.put(KEY_SUR_NAME, item.getSurName());
		data.put(KEY_CELL_PHONE, item.getCellPhone());
		data.put(KEY_EMAIL, item.getEmail());
		data.put(KEY_ROLE, item.getRole());
		data.put(KEY_STATUS, item.getStatus());
		return data;
	}

	public ResponseUserInsert updateUserStatus(String id, String newStatus) {
		ResponseUserInsert response = new ResponseUserInsert();
		try {
			String safeId = id != null ? id : EMPTY;
			String safeStatus = newStatus != null ? newStatus.toLowerCase() : STATUS_ACTIVO;

			Optional<EntityUser> optionalUser = repositoryUser.findById(safeId);

			if (optionalUser.isPresent()) {
				EntityUser user = optionalUser.get();

				user.setStatus(safeStatus);
				user.setUpdatedAt(new java.sql.Date(new Date().getTime()));

				repositoryUser.save(user);

				response.success();
				response.listMessage.add(String.format(MSG_ESTADO_OK, safeStatus));
			} else {
				response.error();
				response.listMessage.add(MSG_USUARIO_NO_ENCONTRADO);
			}
		} catch (Exception e) {
			response.exception();
			response.listMessage.add(MSG_ERROR_ESTADO + e.getMessage());
		}

		return response;
	}

	public ResponseUserDashboardKpi getDashboardKpi(String idUser) {
		ResponseUserDashboardKpi response = new ResponseUserDashboardKpi();

		try {
			LocalDate hoy = LocalDate.now();

			List<EntitySale> ventasHoy = repositorySale.findAll().stream()
					.filter(s -> STATUS_COMPLETADA.equals(s.getStatus()))
					.filter(s -> s.getUser() != null)
					.filter(s -> s.getUser().getIdUser().equals(idUser))
					.filter(s -> s.getSaleDate() != null)
					.filter(s -> toLocalDate(s.getSaleDate()).equals(hoy))
					.toList();

			double montoVendido = ventasHoy.stream()
					.map(EntitySale::getTotal)
					.mapToDouble(BigDecimal::doubleValue)
					.sum();

			double ticketPromedio = ventasHoy.isEmpty() ? 0 : montoVendido / ventasHoy.size();

			long stockCritico = repositoryLot.findAll().stream()
					.filter(l -> l.getProduct() != null)
					.filter(l -> l.getCurrentStock() != null)
					.filter(l -> l.getCurrentStock() <= l.getProduct().getStockMinimum())
					.count();

			Map<String, Object> resumen = new HashMap<>();
			resumen.put(KEY_MIS_VENTAS_HOY, ventasHoy.size());
			resumen.put(KEY_MONTO_VENDIDO_HOY, montoVendido);
			resumen.put(KEY_TICKET_PROMEDIO, ticketPromedio);
			resumen.put(KEY_STOCK_CRITICO, stockCritico);

			response.setResumen(resumen);
			response.success();

		} catch (Exception e) {
			response.listMessage.add(MSG_ERROR_KPIS + e.getMessage());
		}

		return response;
	}

	private LocalDate toLocalDate(Date date) {
		return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
	}

	public ResponseUserGetAll updateProfile(RequestUserUpdateProfile request) {
		ResponseUserGetAll response = new ResponseUserGetAll();
		try {
			Optional<EntityUser> optionalUser = repositoryUser.findById(request.getIdUser());

			if (optionalUser.isEmpty()) {
				return userNotFound(response);
			}

			EntityUser entityUser = optionalUser.get();

			if (repositoryUser.existsByEmailAndIdUserNot(request.getEmail(), request.getIdUser())) {
				response.warning();
				response.listMessage.add(MSG_EMAIL_EN_USO);
				return response;
			}

			if (request.getImage() != null && !request.getImage().trim().isEmpty()) {
				entityUser.setImage(request.getImage());
			}

			entityUser.setFirstName(request.getFirstName());
			entityUser.setSurName(request.getSurName());
			entityUser.setEmail(request.getEmail());
			entityUser.setCellPhone(request.getCellPhone());
			entityUser.setUpdatedAt(new java.sql.Date(new Date().getTime()));

			repositoryUser.save(entityUser);

			response.success();
			response.listMessage.add(MSG_PERFIL_OK);
		} catch (Exception e) {
			response.exception();
			response.listMessage.add(MSG_ERROR_PERFIL + e.getMessage());
		}

		return response;
	}

	public ResponseUserGetAll updatePassword(RequestUserUpdatePassword request) {
		ResponseUserGetAll response = new ResponseUserGetAll();
		try {
			Optional<EntityUser> optionalUser = repositoryUser.findById(request.getIdUser());

			if (optionalUser.isEmpty()) {
				return userNotFound(response);
			}

			EntityUser entityUser = optionalUser.get();

			entityUser.setPassword(passwordEncoder.encode(request.getPassword()));
			entityUser.setUpdatedAt(new java.sql.Date(new Date().getTime()));

			repositoryUser.save(entityUser);

			response.success();
			response.listMessage.add(MSG_ACTUALIZACION_OK);
		} catch (Exception e) {
			response.exception();
			response.listMessage.add(MSG_ERROR_ACTUALIZACION + e.getMessage());
		}

		return response;
	}

	private ResponseUserGetAll userNotFound(ResponseUserGetAll response) {
		response.error();
		response.listMessage.add(MSG_USUARIO_NO_ENCONTRADO);
		return response;
	}
}