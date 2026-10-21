package com.epiis.apirfbvc.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.epiis.apirfbvc.entity.EntityCustomer;

@Repository
public interface RepositoryCustomer extends JpaRepository<EntityCustomer, String>{
    boolean existsByDocumentNumber(String documentNumber);
}
