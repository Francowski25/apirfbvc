package com.epiis.apirfbvc.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.epiis.apirfbvc.entity.EntityInventory;

@Repository
public interface RepositoryInventory extends JpaRepository<EntityInventory, String>{

}
