package com.epiis.apirfbvc.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RequestSupplierUpdate {
    private String idSupplier;
    private String name;
    private String ruc;
    private String phone;
    private String address;
    private String email;
}
