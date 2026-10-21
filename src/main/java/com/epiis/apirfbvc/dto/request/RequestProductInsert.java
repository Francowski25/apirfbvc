package com.epiis.apirfbvc.dto.request;

import java.math.BigDecimal;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RequestProductInsert {

    @NotBlank(message = "El campo \"name\" es requerido.")
    private String name;

    private String description;

    @NotBlank(message = "El campo \"barcode\" es requerido.")
    private String barcode;

    private String image;

    @NotNull(message = "El campo \"priceSale\" es requerido.")
    private BigDecimal priceSale;

    @NotNull(message = "El campo \"idCategory\" es requerido.")
    private String idCategory;

    @NotNull(message = "El campo \"idLaboratory\" es requerido.")
    private String idLaboratory;

    private int stockMinimum;
    private int totalStock;
    private boolean requiresPrescription;
    private String nextExpiration;
}