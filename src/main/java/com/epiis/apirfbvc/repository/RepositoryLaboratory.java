package com.epiis.apirfbvc.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.epiis.apirfbvc.entity.EntityLaboratory;

@Repository
public interface RepositoryLaboratory extends JpaRepository<EntityLaboratory, String>{
	boolean existsByName(String name);
}
