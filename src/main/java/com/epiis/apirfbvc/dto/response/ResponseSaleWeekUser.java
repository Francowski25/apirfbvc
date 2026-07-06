package com.epiis.apirfbvc.dto.response;

import java.util.ArrayList;
import java.util.List;

import com.epiis.apirfbvc.generic.ResponseGeneric;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResponseSaleWeekUser extends ResponseGeneric {

    private List<String> labels = new ArrayList<>();

    private List<Double> values = new ArrayList<>();

}