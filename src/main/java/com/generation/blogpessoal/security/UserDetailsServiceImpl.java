package com.generation.blogpessoal.security;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.generation.blogpessoal.model.Usuario;
import com.generation.blogpessoal.repository.UsuarioRepository;

// Classe responsável por implemntar as regras de negócio e 
// as tratativas de dados de uma parte do ou recurso do sistema
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

	// Injeção de dependência
	@Autowired
	private UsuarioRepository usuarioRepository;

	@Override
	public UserDetails loadUserByUsername(String userName) throws UsernameNotFoundException {

		Optional<Usuario> usuario = usuarioRepository.findByUsuario(userName);

		// Caso o tenha o Objeto usuario no banco de dados,
		//ele executa o método construtor da classe UserDetailsImpl
		//(usuario = parametro)
		if (usuario.isPresent())
			return new UserDetailsImpl(usuario.get());
		// Caso o usuário não seja encontrado, será devolvido o HTTP Status 403 - FORBIDDEN
		else
			throw new ResponseStatusException(HttpStatus.FORBIDDEN);

	}
}
