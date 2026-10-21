package com.epiis.apirfbvc.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.epiis.apirfbvc.entity.EntityInventoryMovementDetail;

@Repository
public interface RepositoryInventoryMovementDetail extends JpaRepository<EntityInventoryMovementDetail, String> {
	List<EntityInventoryMovementDetail> findByMovement_IdMovement(String idMovement);
	
    List<EntityInventoryMovementDetail> findByProduct_IdProduct(String idProduct);

}
