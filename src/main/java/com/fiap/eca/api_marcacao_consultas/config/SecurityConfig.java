package com.fiap.eca.api_marcacao_consultas.config;

import com.fiap.eca.api_marcacao_consultas.security.JwtAuthenticationFilter;
import com.fiap.eca.api_marcacao_consultas.security.JwtTokenProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    private final JwtTokenProvider jwtTokenProvider;
    public SecurityConfig(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Habilita o CORS com nossa configuração personalizada
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/usuarios/login",
                                "/api/auth/login",
                                "/h2-console/**"
                        ).permitAll()
                        .requestMatchers(HttpMethod.POST, "/usuarios").permitAll()
                        .requestMatchers(HttpMethod.GET, "/usuarios").authenticated()
                        .requestMatchers(HttpMethod.POST, "/consultas").authenticated()
                        .anyRequest().authenticated()
                )
                .headers(headers -> headers
                        .frameOptions(frame -> frame.disable())
                        .contentSecurityPolicy(csp -> csp
                                .policyDirectives("script-src 'self' 'unsafe-inline'")
                        )
                )
                .addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider), UsernamePasswordAuthenticationFilter.class)
                .formLogin(form -> form.disable())
                .httpBasic(httpBasic -> httpBasic.disable());
        return http.build();
    }
    // Bean que define as permissões de CORS
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Permite que o frontend rodando no localhost:8081 faça requisições
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:8081"));

        // Permite os métodos HTTP necessários
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));

        // Cabeçalhos que podem ser enviados na requisição
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "X-Requested-With"));

        // Permite o envio de cookies/tokens
        configuration.setAllowCredentials(true);

        // Tempo máximo que o navegador pode cachear as permissões
        configuration.setMaxAge(3600L);
        // Aplica esta configuração a todas as rotas da API
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
