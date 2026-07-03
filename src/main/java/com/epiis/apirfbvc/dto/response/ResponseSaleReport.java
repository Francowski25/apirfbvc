package com.epiis.apirfbvc.dto.response;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.epiis.apirfbvc.generic.ResponseGeneric;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResponseSaleReport extends ResponseGeneric {
    Map<String, Object> resumen = new HashMap<>();
    List<Map<String, Object>> detalle = new ArrayList<>();
}
