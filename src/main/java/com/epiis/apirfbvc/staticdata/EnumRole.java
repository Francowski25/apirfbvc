package com.epiis.apirfbvc.staticdata;

public enum EnumRole {

    ADMINISTRADOR("Administrador"),
    QUIMICO("Químico"),
    VENDEDOR("Vendedor");

    private final String value;

    EnumRole(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return this.value;
    }
}