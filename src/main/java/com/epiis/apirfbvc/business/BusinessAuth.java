package com.epiis.apirfbvc.business;

import java.util.Optional;

import org.mindrot.jbcrypt.BCrypt;
import org.springframework.stereotype.Service;

import com.epiis.apirfbvc.config.JwtService;
import com.epiis.apirfbvc.dto.request.RequestLogin;
import com.epiis.apirfbvc.dto.request.RequestRefreshToken;
import com.epiis.apirfbvc.dto.response.ResponseLogin;
import com.epiis.apirfbvc.dto.response.ResponseRefreshToken;
import com.epiis.apirfbvc.entity.EntityUser;
import com.epiis.apirfbvc.repository.RepositoryUser;

@Service
public class BusinessAuth {

    private final RepositoryUser repositoryUser;
    private final JwtService jwtService;

    public BusinessAuth(RepositoryUser repositoryUser, JwtService jwtService) {
        this.repositoryUser = repositoryUser;
        this.jwtService = jwtService;
    }

    public ResponseLogin login(RequestLogin request) {
        ResponseLogin response = new ResponseLogin();

        Optional<EntityUser> optional = repositoryUser.findByEmail(request.getEmail());

        if (!optional.isPresent()) {
            response.listMessage.add("Correo no registrado.");
            return response;
        }

        EntityUser user = optional.get();

        if ("inactivo".equalsIgnoreCase(user.getStatus().toString())) {
            response.warning();
            response.listMessage.add("Usuario inactivo, contacte al administrador.");
            return response;
        }

        if (!BCrypt.checkpw(request.getPassword(), user.getPassword())) {
            response.listMessage.add("Contraseña incorrecta.");
            return response;
        }

        String token = jwtService.generateToken(
                user.getIdUser(), user.getEmail(), user.getRole());
        
        String refreshToken = jwtService.generateRefreshToken(
        		user.getIdUser(), user.getEmail());

        response.setToken(token);
        response.setRefreshToken(refreshToken);
        response.success();
        response.listMessage.add("Bienvenido, " + user.getFirstName() + ".");
        response.setIdUser(user.getIdUser());
        response.setFirstName(user.getFirstName());
        response.setSurName(user.getSurName());
        response.setDni(user.getDni());
        response.setEmail(user.getEmail());
        response.setRole(user.getRole());
        response.setPassword(user.getPassword());
        response.setStatus(user.getStatus());
        response.setCreatedAt(user.getCreatedAt());
        response.setUpdatedAt(user.getUpdatedAt());
        response.setCellPhone(user.getCellPhone());
        response.setImage(user.getImage());

        return response;
    }
    
    public ResponseRefreshToken refresh(RequestRefreshToken request) {
    	ResponseRefreshToken response = new ResponseRefreshToken();

    	try {
    		String refreshToken = request.getRefreshToken();

    		if (!jwtService.isRefreshTokenValid(refreshToken)) {
    			response.error();
    			response.listMessage.add("El refresh token es inválido o ha expirado. Inicie sesión nuevamente.");
    			return response;
    		}

    		String email = jwtService.extractUsername(refreshToken);

    		Optional<EntityUser> optional = repositoryUser.findByEmail(email);

    		if (!optional.isPresent()) {
    			response.error();
    			response.listMessage.add("Usuario no encontrado.");
    			return response;
    		}

    		EntityUser user = optional.get();

    		if ("inactivo".equalsIgnoreCase(user.getStatus())) {
    			response.warning();
    			response.listMessage.add("Usuario inactivo, contacte al administrador.");
    			return response;
    		}

    		String newToken = jwtService.generateToken(user.getIdUser(), user.getEmail(), user.getRole());
    		String newRefreshToken = jwtService.generateRefreshToken(user.getIdUser(), user.getEmail());

    		response.setToken(newToken);
    		response.setRefreshToken(newRefreshToken);
    		response.success();

    	} catch (Exception e) {
    		response.exception();
    		response.listMessage.add("Error al renovar la sesión: " + e.getMessage());
    	}

    	return response;
    }
}