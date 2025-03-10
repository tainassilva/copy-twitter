package br.com.taina.copy_twitter.controller;

import br.com.taina.copy_twitter.dto.LoginRequestDto;
import br.com.taina.copy_twitter.dto.LoginResponseDto;
import br.com.taina.copy_twitter.entity.Role;
import br.com.taina.copy_twitter.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.stream.Collectors;

@RestController
public class TokenController {

    // será obrigatoriamente injetada no momento da criação da instância
    // e que o valor dessas dependências não poderá ser alterado depois.
    private final JwtEncoder jwtEncoder;

    private final UserRepository userRepository;

    private BCryptPasswordEncoder passwordEncoder;

    public TokenController(JwtEncoder jwtEncoder,
                           UserRepository userRepository,
                           BCryptPasswordEncoder passwordEncoder) {
        this.jwtEncoder = jwtEncoder;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }


    // Recebe o request(Username e senha) e o response retorna o token e a expiração
    @PostMapping("/login")
    @Transactional
    public ResponseEntity<LoginResponseDto> login(@RequestBody LoginRequestDto loginRequest){

        var user = userRepository.findByUsername(loginRequest.username());

        // Se o usuário não existir e a senha não for correta ...
        if(user.isEmpty() || !user.get().isLoginCorrect(loginRequest, passwordEncoder)){
            throw new BadCredentialsException("User or password is invalid!");
        }
        var now = Instant.now();
        var expiresIn = 1440L;

        // Obtém os papéis (roles) associados ao usuário
        //Caso o usuário tenha mais de uma role , vai juntando esses escopos
        var scopes = user.get().getRoles()
                // Transforma o conjunto de papéis em um fluxo (stream)
                .stream()
                // Para cada papel (role) no fluxo, extrai o nome do papel (Role::getName)
                .map(Role::getName)
                // Junta todos os nomes dos papéis em uma única string, separando-os por espaço.
                .collect(Collectors.joining(" "));


        // Cria um objeto JwtClaimsSet, que contém as informações (claims) do JWT (JSON Web Token).
        var claims = JwtClaimsSet.builder()
                // Define o emissor (issuer) do JWT, que neste caso é a string "mybackend".
                .issuer("mybackend")
                // Define o sujeito (subject) do JWT, que neste caso é o ID do usuário convertido para string.
                .subject(user.get().getUserId().toString())
                .issuedAt(now)
                .expiresAt(now.plusSeconds(expiresIn))
                .claim("scope", scopes)  // Adiciona um claim "scope" no JWT com as permissões ou papéis do usuário.
                .build();

        // Codifica o JwtClaimsSet em um JWT usando o jwtEncoder. O resultado é um token JWT.
        var jwtValue = jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();

        // Retorna uma resposta HTTP com o JWT gerado e o tempo de expiração.
        return ResponseEntity.ok(new LoginResponseDto(jwtValue, expiresIn));
    }
}
