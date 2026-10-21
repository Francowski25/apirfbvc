package com.epiis.apirfbvc.business;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

import java.time.LocalDate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.epiis.apirfbvc.dto.request.RequestProductInsert;
import com.epiis.apirfbvc.dto.response.ResponseProductInsert;
import com.epiis.apirfbvc.entity.EntityLot;
import com.epiis.apirfbvc.entity.EntityProduct;
import com.epiis.apirfbvc.repository.RepositoryLot;
import com.epiis.apirfbvc.repository.RepositoryProduct;

@ExtendWith(MockitoExtension.class)
class BusinessProductTest {

    @Mock RepositoryProduct repositoryProduct;
    @Mock RepositoryLot repositoryLot;
    @InjectMocks BusinessProduct businessProduct;

    private RequestProductInsert request() {
        RequestProductInsert r = new RequestProductInsert();
        r.setName("Paracetamol 500 mg");
        r.setStockMinimum(10);
        r.setTotalStock(100);
        r.setIdCategory("cat-1");
        r.setIdLaboratory("lab-1");
        return r;
    }

    @Test
    @DisplayName("PU-01: producto válido se guarda con estado activo")
    void guardaProductoActivo() {
        // 1. PREPARAR
        RequestProductInsert r = request();
        ArgumentCaptor<EntityProduct> captor = ArgumentCaptor.forClass(EntityProduct.class);

        // 2. EJECUTAR
        ResponseProductInsert res = businessProduct.insert(r);

        // 3. VERIFICAR
        verify(repositoryProduct).save(captor.capture());
        assertEquals("success", res.getType());
        assertEquals("activo", captor.getValue().getStatus());
    }

    @Test
    @DisplayName("PU-02: sin vencimiento, el lote usa 2099-12-31 y el stock total")
    void creaLoteConVencimientoPorDefecto() {
        RequestProductInsert r = request();
        ArgumentCaptor<EntityLot> captor = ArgumentCaptor.forClass(EntityLot.class);

        businessProduct.insert(r);

        verify(repositoryLot).save(captor.capture());
        assertEquals(LocalDate.of(2099, 12, 31), captor.getValue().getExpirationDate());
        assertEquals(100, captor.getValue().getCurrentStock().intValue());
    }
}