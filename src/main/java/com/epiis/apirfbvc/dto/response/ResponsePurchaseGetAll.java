package com.epiis.apirfbvc.dto.response;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.epiis.apirfbvc.generic.ResponseGeneric;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResponsePurchaseGetAll extends ResponseGeneric {
    List<Map<String, Object>> listPurchases = new ArrayList<>();
}