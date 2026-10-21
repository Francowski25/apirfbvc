package com.epiis.apirfbvc.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.epiis.apirfbvc.entity.EntityProduct;

@Repository
public interface RepositoryProduct extends JpaRepository<EntityProduct, String> {
	long countByCategory_IdCategory(String idCategory);
	
	long countByLaboratory_IdLaboratory(String idLaboratory); 
	
    boolean existsByName(String name);

}
