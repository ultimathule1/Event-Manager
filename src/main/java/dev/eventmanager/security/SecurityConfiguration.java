package dev.eventmanager.security;

import dev.eventmanager.security.exceptions.handlers.CustomAccessDeniedHandler;
import dev.eventmanager.security.exceptions.handlers.CustomAuthenticationEntryPoint;
import dev.eventmanager.security.jwt.JwtTokenFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;

@Configuration
public class SecurityConfiguration {

    private final CustomUserDetails customUserDetails;
    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;
    private final CustomAccessDeniedHandler customAccessDeniedHandler;
    private final JwtTokenFilter jwtTokenFilter;

    public SecurityConfiguration(CustomUserDetails customUserDetails, CustomAuthenticationEntryPoint customAuthenticationEntryPoint, CustomAccessDeniedHandler customAccessDeniedHandler, JwtTokenFilter jwtTokenFilter) {
        this.customUserDetails = customUserDetails;
        this.customAuthenticationEntryPoint = customAuthenticationEntryPoint;
        this.customAccessDeniedHandler = customAccessDeniedHandler;
        this.jwtTokenFilter = jwtTokenFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .formLogin(AbstractHttpConfigurer::disable)
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorizeRequests ->
                                authorizeRequests
                                        .requestMatchers(HttpMethod.POST, "/users")
                                        .permitAll()
                                        .requestMatchers(HttpMethod.GET, "/users/{id}")
                                        .hasAnyAuthority("ADMIN")
                                        .requestMatchers(HttpMethod.POST, "/users/auth")
                                        .permitAll()

                                        .requestMatchers(HttpMethod.GET, "/locations/{id}")
                                        .hasAnyAuthority("ADMIN", "USER")
                                        .requestMatchers(HttpMethod.GET, "/locations")
                                        .hasAnyAuthority("ADMIN", "USER")
                                        .requestMatchers(HttpMethod.POST, "/locations")
                                        .hasAnyAuthority("ADMIN")
                                        .requestMatchers(HttpMethod.PUT, "/locations")
                                        .hasAnyAuthority("ADMIN")
                                        .requestMatchers(HttpMethod.DELETE, "/locations/**")
                                        .hasAnyAuthority("ADMIN")

                                        .requestMatchers(HttpMethod.POST, "/events/search")
                                        .hasAnyAuthority("ADMIN","USER")
                                        .requestMatchers(HttpMethod.POST, "/events")
                                        .hasAnyAuthority("USER")
                                        .requestMatchers(HttpMethod.GET, "/events/my")
                                        .hasAnyAuthority("USER")
                                        .requestMatchers(HttpMethod.GET, "/events/{id}")
                                        .hasAnyAuthority("ADMIN", "USER")
                                        .requestMatchers(HttpMethod.DELETE, "/events/{id}")
                                        .hasAnyAuthority("ADMIN", "USER")
                                        .requestMatchers(HttpMethod.PUT, "/events/{id}")
                                        .hasAnyAuthority("ADMIN", "USER")

                                        .requestMatchers("/events/registrations/{id}")
                                        .hasAnyAuthority("USER")

                                        .anyRequest().authenticated()
                )
                .exceptionHandling(exception -> {
                    exception
                            .authenticationEntryPoint(customAuthenticationEntryPoint)
                            .accessDeniedHandler(customAccessDeniedHandler);
                })
                .addFilterBefore(jwtTokenFilter, AnonymousAuthenticationFilter.class)
                .build();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration configuration
    ) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        var authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(customUserDetails);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return web -> web.debug(true).ignoring()
                .requestMatchers("/css/**",
                        "/js/**",
                        "/img/**",
                        "/lib/**",
                        "/favicon.ico",
                        "/swagger-ui/**",
                        "/v2/api-docs",
                        "/v3/api-docs",
                        "/configuration/ui",
                        "/swagger-resources/**",
                        "/configuration/security",
                        "/swagger-ui.html",
                        "/webjars/**",
                        "/v3/api-docs/swagger-config",
                        "/openapi.yaml"
                );
    }
}
