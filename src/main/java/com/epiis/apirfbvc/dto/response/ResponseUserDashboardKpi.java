package com.epiis.apirfbvc.dto.response;

import java.util.HashMap;
import java.util.Map;

import com.epiis.apirfbvc.generic.ResponseGeneric;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResponseUserDashboardKpi extends ResponseGeneric {

    Map<String, Object> resumen = new HashMap<>();

}