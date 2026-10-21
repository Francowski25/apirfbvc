package com.epiis.apirfbvc.repository;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.epiis.apirfbvc.entity.EntitySale;

@Repository
public interface RepositorySale extends JpaRepository<EntitySale, String> {

    List<EntitySale> findByStatusOrderBySaleDateDesc(String status);

    List<EntitySale> findBySaleDateGreaterThanEqualAndStatus(Date startDate, String status);

    List<EntitySale> findBySaleDateBetweenAndStatus(Date start, Date end, String status);

    boolean existsBySaleNumber(String saleNumber);
}