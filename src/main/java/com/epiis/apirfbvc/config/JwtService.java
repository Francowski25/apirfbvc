package com.epiis.apirfbvc.config;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {

	private static final Logger LOGGER = LoggerFactory.getLogger(JwtService.class);

	private static final String CLAIM_ID_USER = "idUser";
	private static final String CLAIM_ROLE = "role";
	private static final String CLAIM_TYPE = "type";
	private static final String TYPE_ACCESS = "access";
	private static final String TYPE_REFRESH = "refresh";

	private final JwtProperties jwtProperties;

	public JwtService(JwtProperties jwtProperties) {
		this.jwtProperties = jwtProperties;
	}

	public String generateToken(String idUser, String email, String role) {
		Map<String, Object> extraClaims = new HashMap<>();
		extraClaims.put(CLAIM_ID_USER, idUser);
		extraClaims.put(CLAIM_ROLE, role);
		extraClaims.put(CLAIM_TYPE, TYPE_ACCESS);

		return Jwts.builder()
				.setClaims(extraClaims)
				.setSubject(email)
				.setIssuedAt(new Date(System.currentTimeMillis()))
				.setExpiration(new Date(System.currentTimeMillis() + jwtProperties.getTimeAuthMs()))
				.signWith(getSignInKey(), SignatureAlgorithm.HS256)
				.compact();
	}

	public String generateRefreshToken(String idUser, String email) {
		Map<String, Object> extraClaims = new HashMap<>();
		extraClaims.put(CLAIM_ID_USER, idUser);
		extraClaims.put(CLAIM_TYPE, TYPE_REFRESH);

		return Jwts.builder()
				.setClaims(extraClaims)
				.setSubject(email)
				.setIssuedAt(new Date(System.currentTimeMillis()))
				.setExpiration(new Date(System.currentTimeMillis() + jwtProperties.getRefreshTimeMs()))
				.signWith(getSignInKey(), SignatureAlgorithm.HS256)
				.compact();
	}

	public String extractType(String token) {
		return extractClaim(token, claims -> claims.get(CLAIM_TYPE, String.class));
	}

	public boolean isRefreshTokenValid(String token) {
		try {
			return TYPE_REFRESH.equals(extractType(token)) && !isTokenExpired(token);
		} catch (ExpiredJwtException e) {
			LOGGER.debug("Token refresh expirado");
			return false;
		} catch (JwtException e) {
			LOGGER.warn("Token refresh inválido: {}", e.getMessage());
			return false;
		}
	}

	public String extractUsername(String token) {
		return extractClaim(token, Claims::getSubject);
	}

	public String extractIdUser(String token) {
		return extractClaim(token, claims -> claims.get(CLAIM_ID_USER, String.class));
	}

	public String extractRole(String token) {
		return extractClaim(token, claims -> claims.get(CLAIM_ROLE, String.class));
	}

	public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
		final Claims claims = extractAllClaims(token);
		return claimsResolver.apply(claims);
	}

	private Claims extractAllClaims(String token) {
		return Jwts.parserBuilder()
				.setSigningKey(getSignInKey())
				.build()
				.parseClaimsJws(token)
				.getBody();
	}

	public boolean isTokenValid(String token, String expectedEmail) {
		final String email = extractUsername(token);
		return email.equals(expectedEmail) && !isTokenExpired(token);
	}

	private boolean isTokenExpired(String token) {
		return extractExpiration(token).before(new Date());
	}

	private Date extractExpiration(String token) {
		return extractClaim(token, Claims::getExpiration);
	}

	private Key getSignInKey() {
		return Keys.hmacShaKeyFor(jwtProperties.getSecretKey().getBytes(java.nio.charset.StandardCharsets.UTF_8));
	}
}