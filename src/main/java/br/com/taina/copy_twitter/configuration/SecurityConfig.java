package br.com.taina.copy_twitter.configuration;

import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.web.SecurityFilterChain;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Value("${jwt.public.key}")
    private RSAPublicKey publicKey;  // A chave pública para validar os tokens JWT

    @Value("${jwt.private.key}")
    private RSAPrivateKey privateKey;  // A chave privada para assinar os tokens JWT

    /**
     * Método que configura a segurança da aplicação.
     *
     * @param http a configuração de segurança.
     * @return a configuração finalizada de SecurityFilterChain.
     *
     * Descrição:
     * A configuração define as permissões de acesso e a autenticação necessária:
     * - O método `requestMatchers(HttpMethod.POST, "/users").permitAll()` permite
     *   acesso sem autenticação à rota de criação de usuários.
     * - O método `requestMatchers(HttpMethod.POST, "/login").permitAll()` permite
     *   acesso sem autenticação à rota de login.
     * - Todas as outras rotas requerem autenticação (`anyRequest().authenticated()`).
     * - A proteção contra CSRF é desativada com `csrf().disable()`, o que é comum em APIs REST.
     * - A autenticação baseada em tokens JWT é configurada com `.oauth2ResourceServer(oauth2 -> oauth2.jwt())`.
     * - A aplicação é configurada para ser stateless, ou seja, não utiliza sessões no servidor,
     *   utilizando a configuração `sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)`.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(HttpMethod.POST, "/users").permitAll()  // Permite acesso sem autenticação para a criação de usuários
                        .requestMatchers(HttpMethod.POST, "/login").permitAll()  // Permite acesso sem autenticação para login
                        .anyRequest().authenticated())  // Todas as outras rotas requerem autenticação
                .csrf(csrf -> csrf.disable())  // Desativa a proteção CSRF (útil para APIs REST)
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()))  // Configura o servidor de recursos OAuth2 para usar JWT
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));  // Configuração para tornar a aplicação stateless (sem sessões)

        return http.build();
    }

    /**
     * Método que configura o JwtDecoder, utilizado para decodificar e validar os tokens JWT.
     *
     * O JwtDecoder usa a chave pública configurada para validar a assinatura do JWT.
     *
     * @return o JwtDecoder configurado para usar a chave pública.
     */
    @Bean
    public JwtDecoder jwtDecoder() {
        return NimbusJwtDecoder.withPublicKey(publicKey).build();  // Configura o JwtDecoder com a chave pública para validação dos tokens
    }

    /**
     * Método que configura o JwtEncoder, utilizado para criar e assinar novos tokens JWT.
     *
     * O JwtEncoder utiliza a chave privada para assinar os tokens, garantindo a integridade e autenticidade.
     *
     * @return o JwtEncoder configurado para assinar tokens com a chave privada.
     */
    @Bean
    public JwtEncoder jwtEncoder() {
        JWK jwk = new RSAKey.Builder(this.publicKey).privateKey(privateKey).build();  // Cria a chave RSA a ser usada para assinatura do JWT
        var jwks = new ImmutableJWKSet<>(new JWKSet(jwk));  // Cria um conjunto imutável de JWKs com a chave RSA
        return new NimbusJwtEncoder(jwks);  // Retorna o JwtEncoder configurado com a chave RSA para assinatura de JWTs
    }

    /**
     * Método que configura o BCryptPasswordEncoder, utilizado para criptografar senhas de forma segura.
     *
     * @return o BCryptPasswordEncoder configurado.
     */
    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();  // Retorna uma instância do BCryptPasswordEncoder, que é utilizado para criptografar senhas
    }

}
