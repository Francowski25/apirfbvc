package com.epiis.apirfbvc.dto.response;

import com.epiis.apirfbvc.generic.ResponseGeneric;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResponseProductStockAlert extends ResponseGeneric {
    private int stockCritico;
    private long porVencer30Dias;
}