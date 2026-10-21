package com.epiis.apirfbvc.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.epiis.apirfbvc.entity.EntityLot;

@Repository
public interface RepositoryLot extends JpaRepository<EntityLot, String> {
    List<EntityLot> findByProduct_IdProduct(String idProduct);
    
    List<EntityLot> findByExpirationDateBetween(LocalDate start, LocalDate end);
    
    List<EntityLot> findByProduct_IdProductAndCurrentStockGreaterThanOrderByExpirationDateAsc(String idProduct, int stock);
    
    boolean existsByCode(String code);

}
