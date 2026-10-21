package com.epiis.apirfbvc.staticdata;

public enum EnumStatus {
    ACTIVO("Activo"),
    INACTIVO("Inactivo");

    private String value;

    EnumStatus(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return this.value;
    }
}