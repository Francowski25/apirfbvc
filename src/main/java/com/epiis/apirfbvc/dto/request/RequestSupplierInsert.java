package com.epiis.apirfbvc.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RequestSupplierInsert {
    private String name;
    private String ruc;
    private String phone;
    private String address;
    private String email;
}