package com.epiis.apirfbvc.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.epiis.apirfbvc.entity.EntityCategory;

@Repository
public interface RepositoryCategory extends JpaRepository<EntityCategory, String>{
	boolean existsByName(String name);
}
