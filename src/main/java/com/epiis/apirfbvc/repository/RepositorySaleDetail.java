package com.epiis.apirfbvc.repository;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.epiis.apirfbvc.entity.EntitySaleDetail;

@Repository
public interface RepositorySaleDetail extends JpaRepository<EntitySaleDetail, String> {

    List<EntitySaleDetail> findBySale_IdSale(String idSale);

    List<EntitySaleDetail> findBySale_SaleDateGreaterThanEqual(Date startDate);
}