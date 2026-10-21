package com.epiis.apirfbvc.dto.response;

import com.epiis.apirfbvc.generic.ResponseGeneric;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResponseRefreshToken extends ResponseGeneric {
	private String token;
	private String refreshToken;
}