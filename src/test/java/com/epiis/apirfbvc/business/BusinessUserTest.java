package com.epiis.apirfbvc.business;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.epiis.apirfbvc.dto.request.RequestUserInsert;
import com.epiis.apirfbvc.dto.response.ResponseUserInsert;
import com.epiis.apirfbvc.entity.EntityUser;
import com.epiis.apirfbvc.repository.RepositoryUser;

@ExtendWith(MockitoExtension.class)
class BusinessUserTest {

    @Mock RepositoryUser repositoryUser;
    @Mock PasswordEncoder passwordEncoder;
    @InjectMocks BusinessUser businessUser;

    private RequestUserInsert request() {
        RequestUserInsert r = new RequestUserInsert();
        r.setDni("12345678");
        r.setFirstName("Juan");
        r.setSurName("Pérez");
        r.setEmail("juan@botica.com");
        r.setCellPhone("987654321");
        r.setRole("VENDEDOR");
        r.setPassword("Clave1234");
        return r;
    }

    @Test
    @DisplayName("PU-03: usuario nuevo queda activo, con contraseña cifrada y avatar por defecto")
    void guardaUsuarioNuevo() {
        when(passwordEncoder.encode("Clave1234")).thenReturn("HASH");
        ArgumentCaptor<EntityUser> captor = ArgumentCaptor.forClass(EntityUser.class);

        ResponseUserInsert res = businessUser.insert(request());

        verify(repositoryUser).save(captor.capture());
        assertEquals("success", res.getType());
        assertEquals("activo", captor.getValue().getStatus());
        assertEquals("HASH", captor.getValue().getPassword());
        assertEquals("avatar.png", captor.getValue().getImage());
    }

    @Test
    @DisplayName("PU-04: DNI duplicado es rechazado")
    void rechazaDniDuplicado() {
        when(repositoryUser.existsByDni("12345678")).thenReturn(true);

        ResponseUserInsert res = businessUser.insert(request());

        assertNotEquals("success", res.getType());
        assertTrue(res.listMessage.contains("El DNI ya se encuentra registrado en el sistema."));
        verify(repositoryUser, never()).save(any());
    }

    @Test
    @DisplayName("PU-05: correo duplicado es rechazado")
    void rechazaCorreoDuplicado() {
        when(repositoryUser.existsByEmail("juan@botica.com")).thenReturn(true);

        ResponseUserInsert res = businessUser.insert(request());

        assertNotEquals("success", res.getType());
        assertTrue(res.listMessage.contains("El correo electrónico ya se encuentra registrado en el sistema."));
        verify(repositoryUser, never()).save(any());
    }
}