package com.epiis.apirfbvc.dto.response;

import java.util.HashMap;
import java.util.Map;

import com.epiis.apirfbvc.generic.ResponseGeneric;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResponseSaleKpi extends ResponseGeneric {
    private Map<String, Object> kpi = new HashMap<>();
}