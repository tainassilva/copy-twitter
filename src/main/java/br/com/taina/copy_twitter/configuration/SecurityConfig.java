package br.com.taina.copy_twitter.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception{

        http
                // Toda a requisição vai precisar de autenticação, exceto as que eu permitir acesso a todos 
                .authorizeHttpRequests(authorize ->
                        authorize.anyRequest().authenticated())
                .csrf( csrf -> csrf.disable())
                // O Spring Security automaticamente buscará as configurações do issuer
                // (provedor OAuth2) para validar os tokens.
                .oauth2ResourceServer(oauth2 ->
                        oauth2.jwt(Customizer.withDefaults()))
                // Não depende de sessão e sim dos tokens ...
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                return http.build();
    }

}
