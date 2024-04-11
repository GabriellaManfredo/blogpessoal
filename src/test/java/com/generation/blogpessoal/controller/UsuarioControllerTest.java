package com.generation.blogpessoal.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Optional;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.generation.blogpessoal.model.Usuario;
import com.generation.blogpessoal.repository.UsuarioRepository;
import com.generation.blogpessoal.service.UsuarioService;

// @SpringBootTest - Caso a porta principal esteja em uso, irá utilizar outra automaticamente
// @TestInstance - ciclo de vida da classe teste será por classe

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class UsuarioControllerTest {

	// Enviar as requisições para a aplicação
	@Autowired
	private TestRestTemplate testRestTemplate; // Simula ao Insomnia o ambiente de teste

	// Persiste os objetos no banco de dados de teste c/ senha criptografada
	@Autowired
	private UsuarioService usuarioService;

	// Limpa o banco de dados de testes
	@Autowired
	private UsuarioRepository usuarioRepository;

	// Apaga todos os dados da tabela e cria o usuário root@root.com para testar os
	// métodos protegidos por autenticação
	@BeforeAll
	void start() {

		usuarioRepository.deleteAll();

		usuarioService.cadastrarUsuario(new Usuario(0L, "Root2", "root@root.com", "rootroot", "-"));

	}

	// Método 1 - cria um usuario e indica que este método executará um teste
	// @DisplayName configura uma mensagem que será exibida ao invés do nome do método
	// Espera status 201
	@Test
	@DisplayName("Cadastrar Um Usuário")
	public void deveCriarUmUsuario() {

		HttpEntity<Usuario> corpoRequisicao = new HttpEntity<Usuario>(
				new Usuario(0L, "Paulo Antunes", "paulo_antunes@email.com.br", "13465278", "-"));

		ResponseEntity<Usuario> corpoResposta = testRestTemplate.exchange("/usuarios/cadastrar", HttpMethod.POST,
				corpoRequisicao, Usuario.class);

		assertEquals(HttpStatus.CREATED, corpoResposta.getStatusCode());

	}

	// Método 2 - objetivo de testar o erro e não a persistência do bd
	// Espera o status 400
	@Test
	@DisplayName("Não deve permitir duplicação do Usuário")
	public void naoDeveDuplicarUsuario() {

		usuarioService.cadastrarUsuario(new Usuario(0L, "Maria da Silva", "maria_silva@email.com.br", "13465278", "-"));

		HttpEntity<Usuario> corpoRequisicao = new HttpEntity<Usuario>(
				new Usuario(0L, "Maria da Silva", "maria_silva@email.com.br", "13465278", "-"));

		ResponseEntity<Usuario> corpoResposta = testRestTemplate.exchange("/usuarios/cadastrar", HttpMethod.POST,
				corpoRequisicao, Usuario.class);

		assertEquals(HttpStatus.BAD_REQUEST, corpoResposta.getStatusCode());
	}

	// Método 3 - objetivo persiste e atualiza um objeto no bd
	// Espera status 200
	@Test
	@DisplayName("Atualizar um Usuário")
	public void deveAtualizarUmUsuario() {

		Optional<Usuario> usuarioCadastrado = usuarioService.cadastrarUsuario(
				new Usuario(0L, "Juliana Andrews", "juliana_andrews@email.com.br", "juliana123", "-"));

		Usuario usuarioUpdate = new Usuario(usuarioCadastrado.get().getId(), "Juliana Andrews Ramos",
				"juliana_ramos@email.com.br", "juliana123", "-");

		HttpEntity<Usuario> corpoRequisicao = new HttpEntity<Usuario>(usuarioUpdate);

		ResponseEntity<Usuario> corpoResposta = testRestTemplate.withBasicAuth("root@root.com", "rootroot")
				.exchange("/usuarios/atualizar", HttpMethod.PUT, corpoRequisicao, Usuario.class);

		assertEquals(HttpStatus.OK, corpoResposta.getStatusCode());

	}

	// Método 4 - Persistir 2 objetos no bd e vericar se o método está listando
	// todos os usuários
	// Espera status 200
	@Test
	@DisplayName("Listar todos os Usuários")
	public void deveMostrarTodosUsuarios() {

		usuarioService.cadastrarUsuario(
				new Usuario(0L, "Sabrina Sanches", "sabrina_sanches@email.com.br", "sabrina123", "-"));

		usuarioService.cadastrarUsuario(
				new Usuario(0L, "Ricardo Marques", "ricardo_marques@email.com.br", "ricardo123", "-"));

		ResponseEntity<String> resposta = testRestTemplate.withBasicAuth("root@root.com", "rootroot")
				.exchange("/usuarios/all", HttpMethod.GET, null, String.class);

		assertEquals(HttpStatus.OK, resposta.getStatusCode());

	}
}
