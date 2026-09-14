package com.pedidos360.notificaciones;

import java.util.Collection;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtTimestampValidator;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    private static final String ISSUER =
        "https://sts.windows.net/fd969d4c-5256-48f4-899c-b33c192dc051/";

    private static final String ISSUER_WITHOUT_SLASH =
        "https://sts.windows.net/fd969d4c-5256-48f4-899c-b33c192dc051";

    private static final String JWK_SET_URI =
        "https://login.microsoftonline.com/fd969d4c-5256-48f4-899c-b33c192dc051/discovery/v2.0/keys";

    private static final String API_AUDIENCE =
        "api://ef080cc3-9d30-4a1a-89b5-c5236f9bd00d";

    @Bean
    public JwtDecoder jwtDecoder() {
        NimbusJwtDecoder decoder =
            NimbusJwtDecoder.withJwkSetUri(JWK_SET_URI).build();

        OAuth2TokenValidator<Jwt> issuerValidator = token -> {
            String issuer = token.getIssuer().toString();

            if (ISSUER.equals(issuer) || ISSUER_WITHOUT_SLASH.equals(issuer)) {
                return OAuth2TokenValidatorResult.success();
            }

            OAuth2Error error = new OAuth2Error(
                "invalid_token",
                "El issuer del token no es valido.",
                null
            );

            return OAuth2TokenValidatorResult.failure(error);
        };

        OAuth2TokenValidator<Jwt> audienceValidator = token -> {
            Collection<String> audiences = token.getAudience();

            if (audiences != null && audiences.contains(API_AUDIENCE)) {
                return OAuth2TokenValidatorResult.success();
            }

            OAuth2Error error = new OAuth2Error(
                "invalid_token",
                "El token no contiene la audiencia esperada.",
                null
            );

            return OAuth2TokenValidatorResult.failure(error);
        };

        OAuth2TokenValidator<Jwt> validator =
            new DelegatingOAuth2TokenValidator<>(
                new JwtTimestampValidator(),
                issuerValidator,
                audienceValidator
            );

        decoder.setJwtValidator(validator);

        return decoder;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.GET, "/api/notificaciones/estado")
                .hasAuthority("SCOPE_read")
                .anyRequest()
                .authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> {}));

        return http.build();
    }
}