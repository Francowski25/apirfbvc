package com.epiis.apirfbvc.business;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.epiis.apirfbvc.dto.request.RequestUserInsert;
import com.epiis.apirfbvc.dto.response.ResponseUserInsert;
import com.epiis.apirfbvc.entity.EntityUser;
import com.epiis.apirfbvc.repository.RepositoryUser;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class BusinessUserIntegrationTest {

    @Autowired BusinessUser businessUser;
    @Autowired RepositoryUser repositoryUser;

    private RequestUserInsert request(String dni, String email) {
        RequestUserInsert r = new RequestUserInsert();
        r.setDni(dni);
        r.setFirstName("Juan");
        r.setSurName("Pérez");
        r.setEmail(email);
        r.setCellPhone("987654321");
        r.setRole("VENDEDOR");
        r.setPassword("Clave1234");
        return r;
    }

    @Test
    @DisplayName("PI-01: guarda el usuario con la contraseña cifrada")
    void guardaUsuario() {
        ResponseUserInsert res = businessUser.insert(request("12345678", "juan@botica.com"));

        assertEquals("success", res.getType());
        assertEquals(1, repositoryUser.count());
        EntityUser u = repositoryUser.findAll().get(0);
        assertNotEquals("Clave1234", u.getPassword());
        assertEquals("activo", u.getStatus());
    }

    @Test
    @DisplayName("PI-02: mismo DNI dos veces deja un solo registro")
    void rechazaDniDuplicado() {
        businessUser.insert(request("12345678", "a@botica.com"));
        ResponseUserInsert res = businessUser.insert(request("12345678", "b@botica.com"));

        assertNotEquals("success", res.getType());
        assertEquals(1, repositoryUser.count());
    }

    @Test
    @DisplayName("PI-03: mismo correo dos veces deja un solo registro")
    void rechazaCorreoDuplicado() {
        businessUser.insert(request("11111111", "juan@botica.com"));
        ResponseUserInsert res = businessUser.insert(request("22222222", "juan@botica.com"));

        assertNotEquals("success", res.getType());
        assertEquals(1, repositoryUser.count());
    }
}