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
public class ResponsePurchaseReport extends ResponseGeneric {

    private Map<String, Object> resumen = new HashMap<>();

    private List<Map<String, Object>> detalle = new ArrayList<>();

}