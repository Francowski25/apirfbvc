package com.epiis.apirfbvc.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.epiis.apirfbvc.entity.EntitySupplier;

@Repository
public interface RepositorySupplier extends JpaRepository<EntitySupplier, String> {
	   boolean existsByRuc(String ruc);
	    boolean existsByRucAndIdSupplierNot(String ruc, String idSupplier);
}
