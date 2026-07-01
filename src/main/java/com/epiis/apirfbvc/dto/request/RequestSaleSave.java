package com.epiis.apirfbvc.dto.request;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RequestSaleSave {

    private String idCustomer;
    private String idUser;
    private String paymentMethod;
    private Double discount;

    private List<SaleItem> items;

    @Getter
    @Setter
    public static class SaleItem {
        private String idProduct;
        private String idLot;
        private int quantity;
        private double unitPrice;
    }
}