package com.epiis.apirfbvc.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.epiis.apirfbvc.entity.EntityUser;

@Repository
public interface RepositoryUser extends JpaRepository<EntityUser, String> {
	
	Optional<EntityUser> findByEmail(String email);
	
	boolean existsByDni(String dni);
	
    boolean existsByEmail(String email);
    
    boolean existsByEmailAndIdUserNot(String email, String idUser);
}