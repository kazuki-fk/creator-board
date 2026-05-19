package com.creatorboard.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.http.HttpMethod;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        // 静的リソース・認証画面・プロフィールは全員OK
                        .requestMatchers("/css/**", "/js/**", "/images/**").permitAll()
                        .requestMatchers("/", "/profile", "/login", "/signup").permitAll()

                        // GETリクエスト（閲覧）は全員OK
                        .requestMatchers(HttpMethod.GET, "/dashboard").permitAll()
                        .requestMatchers(HttpMethod.GET, "/projects/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/analyzer/**").permitAll()

                        // 削除・作成・編集はログイン必須
                        .requestMatchers(HttpMethod.POST, "/projects/new").authenticated()
                        .requestMatchers(HttpMethod.POST, "/projects/*/edit").authenticated()
                        .requestMatchers(HttpMethod.POST, "/projects/*/delete").authenticated()
                        .requestMatchers(HttpMethod.POST, "/projects/*/logs").authenticated()
                        .requestMatchers(HttpMethod.POST, "/projects/*/logs/*/delete").authenticated()
                        .requestMatchers(HttpMethod.POST, "/analyzer/**").authenticated()
                        // ステータス・フェーズ変更はゲストもOK
                        .requestMatchers(HttpMethod.POST, "/projects/*/status").permitAll()
                        .requestMatchers(HttpMethod.POST, "/projects/*/phase").permitAll()

                        // その他は全てログイン必須
                        .anyRequest().authenticated())
                .formLogin(login -> login
                        .loginPage("/login")
                        .defaultSuccessUrl("/dashboard", true)
                        .permitAll())
                .logout(logout -> logout.permitAll())
                .csrf(csrf -> csrf.ignoringRequestMatchers("/h2-console/**"))
                .headers(headers -> headers.frameOptions(frame -> frame.disable()));

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}