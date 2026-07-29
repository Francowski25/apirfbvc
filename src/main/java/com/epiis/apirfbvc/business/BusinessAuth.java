package com.epiis.apirfbvc.business;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.epiis.apirfbvc.dto.request.RequestLogin;
import com.epiis.apirfbvc.dto.response.ResponseLogin;
import com.epiis.apirfbvc.entity.EntityUser;
import com.epiis.apirfbvc.repository.RepositoryUser;

import org.mindrot.jbcrypt.BCrypt;

@Service

public class BusinessAuth {

    private final RepositoryUser repositoryUser;
    
    public BusinessAuth(RepositoryUser repositoryUser) {
    		this.repositoryUser = repositoryUser;
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

        response.success();
        response.listMessage.add("Bienvenido, " + user.getFirstName() + ".");
        response.setIdUser(user.getIdUser());
        response.setFirstName(user.getFirstName());
        response.setSurName(user.getSurName());
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
}