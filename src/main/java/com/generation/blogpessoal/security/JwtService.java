package com.generation.blogpessoal.security;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtService {

	public static final String SECRET = "5367566B59703373367639792F423F4528482B4D6251655468576D5A71347437";

	// Responsável por codificat a SECRET em Base64 e gerar Signature do Token
	private Key getSignKey() {
		byte[] keyBytes = Decoders.BASE64.decode(SECRET);
		return Keys.hmacShaKeyFor(keyBytes);
	}

	// Retorna todas as claims, inseridas no payload do Token JWT
	private Claims extractAllClaims(String token) {
		return Jwts.parserBuilder().setSigningKey(getSignKey()).build().parseClaimsJws(token).getBody();
	}

	public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
		final Claims claims = extractAllClaims(token);
		return claimsResolver.apply(claims);
	}

	// Recupera os dados da claim sub - user/email
	public String extractUsername(String token) {
		return extractClaim(token, Claims::getSubject);
	}

	// Recupera os dados sa claim exp - data e hora de expiração do Token JWT
	public Date extractExpiration(String token) {
		return extractClaim(token, Claims::getExpiration);
	}

	// Recupera os dados da claim exp e verifica se o token está expirado
	// Caso o token esteja expirado, será necessário autenticar novamente e gerar um
	// novo token
	private Boolean isTokenExpired(String token) {
		return extractExpiration(token).before(new Date());
	}

	// Valida se o Token pertence ao usuário que enviou
	public Boolean validateToken(String token, UserDetails userDetails) {
		final String username = extractUsername(token);
		return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
	}

	// Cria o Token JWT
	private String createToken(Map<String, Object> claims, String userName) {
		return Jwts.builder().setClaims(claims).setSubject(userName).setIssuedAt(new Date(System.currentTimeMillis()))
				.setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60))
				.signWith(getSignKey(), SignatureAlgorithm.HS256).compact();
	}

	// É responsável por gerar um novo Token a partir do user
	public String generateToken(String userName) {
		Map<String, Object> claims = new HashMap<>();
		return createToken(claims, userName);
	}

}