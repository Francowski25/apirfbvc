package com.epiis.apirfbvc.business;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.epiis.apirfbvc.dto.request.RequestSaleSave;
import com.epiis.apirfbvc.dto.response.ResponseMovementCreate;
import com.epiis.apirfbvc.dto.response.ResponseSaleSave;
import com.epiis.apirfbvc.entity.EntitySale;
import com.epiis.apirfbvc.entity.EntityUser;
import com.epiis.apirfbvc.repository.RepositoryCustomer;
import com.epiis.apirfbvc.repository.RepositorySale;
import com.epiis.apirfbvc.repository.RepositorySaleDetail;
import com.epiis.apirfbvc.repository.RepositoryUser;

@ExtendWith(MockitoExtension.class)
class BusinessSaleTest {

    @Mock RepositorySale repositorySale;
    @Mock RepositorySaleDetail repositorySaleDetail;
    @Mock RepositoryCustomer repositoryCustomer;
    @Mock RepositoryUser repositoryUser;
    @Mock BusinessMovement businessMovement;
    @InjectMocks BusinessSale businessSale;

    private RequestSaleSave.SaleItem item(String idProduct, String idLot, int qty, double price) {
        RequestSaleSave.SaleItem i = new RequestSaleSave.SaleItem();
        i.setIdProduct(idProduct);
        i.setIdLot(idLot);
        i.setQuantity(qty);
        i.setUnitPrice(price);
        return i;
    }

    private RequestSaleSave request(List<RequestSaleSave.SaleItem> items) {
        RequestSaleSave r = new RequestSaleSave();
        r.setIdUser("u1");
        r.setPaymentMethod("Efectivo");
        r.setItems(items);
        return r;
    }

    private List<RequestSaleSave.SaleItem> dosProductos() {
        return List.of(item("p1", "l1", 3, 2.50), item("p2", "l2", 2, 4.00));
    }

    @Test
    @DisplayName("PU-06: venta sin productos es rechazada")
    void rechazaVentaSinItems() {
        ResponseSaleSave res = businessSale.save(request(List.of()));

        assertNotEquals("success", res.getType());
        assertTrue(res.listMessage.contains("Debe agregar al menos un producto."));
        verify(repositorySale, never()).save(any());
    }

    @Test
    @DisplayName("PU-07: venta con usuario inexistente es rechazada")
    void rechazaUsuarioInexistente() {
        ResponseSaleSave res = businessSale.save(request(dosProductos()));

        assertNotEquals("success", res.getType());
        assertTrue(res.listMessage.contains("Usuario no encontrado."));
        verify(repositorySale, never()).save(any());
    }

    @Test
    @DisplayName("PU-08: subtotal 15.50, IGV 2.79 y total 18.29")
    void calculaTotales() {
        // 1. PREPARAR
        when(repositoryUser.findById("u1")).thenReturn(Optional.of(new EntityUser()));
        ResponseMovementCreate ok = new ResponseMovementCreate();
        ok.success();
        when(businessMovement.create(any())).thenReturn(ok);
        ArgumentCaptor<EntitySale> captor = ArgumentCaptor.forClass(EntitySale.class);

        // 2. EJECUTAR
        ResponseSaleSave res = businessSale.save(request(dosProductos()));

        // 3. VERIFICAR
        verify(repositorySale).save(captor.capture());
        EntitySale sale = captor.getValue();
        assertEquals("success", res.getType());
        assertEquals(0, new BigDecimal("15.50").compareTo(sale.getSubtotal()));
        assertEquals(0, new BigDecimal("2.79").compareTo(sale.getIgv()));
        assertEquals(0, new BigDecimal("18.29").compareTo(sale.getTotal()));
        assertEquals("Completada", sale.getStatus());
    }

    @Test
    @DisplayName("PU-09: si el movimiento de salida falla, no se guarda la venta")
    void noGuardaVentaSiMovimientoFalla() {
        when(repositoryUser.findById("u1")).thenReturn(Optional.of(new EntityUser()));
        ResponseMovementCreate fail = new ResponseMovementCreate();
        fail.error();
        when(businessMovement.create(any())).thenReturn(fail);

        ResponseSaleSave res = businessSale.save(request(dosProductos()));

        assertNotEquals("success", res.getType());
        verify(repositorySale, never()).save(any());
    }
}