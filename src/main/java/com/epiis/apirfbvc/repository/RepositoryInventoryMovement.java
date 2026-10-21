package com.epiis.apirfbvc.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.epiis.apirfbvc.entity.EntityInventoryMovement;

@Repository
public interface RepositoryInventoryMovement extends JpaRepository<EntityInventoryMovement, String> {
	List<EntityInventoryMovement> findByType(String type);
	List<EntityInventoryMovement> findByTypeOrderByMovementDateDesc(String type);
	List<EntityInventoryMovement> findByUser_IdUser(String idUser);
}
