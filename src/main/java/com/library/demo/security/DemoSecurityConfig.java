package com.library.demo.security;

import javax.sql.DataSource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class DemoSecurityConfig {

    @Bean
    public UserDetailsManager userDetailsManager(DataSource dataSource) {
        return new JdbcUserDetailsManager(dataSource);
    }

    @Bean
    @Order(1)
    public SecurityFilterChain apiFilterChain(HttpSecurity http) throws Exception {
        http.securityMatcher("/api/**");

        http.authorizeHttpRequests(configurer -> configurer
            .requestMatchers(HttpMethod.DELETE, "/api/items/*").hasRole("ADMIN")
            .requestMatchers(HttpMethod.POST, "/api/items", "/api/items/**").hasAnyRole("ADMIN", "LIBRARIAN")
            .requestMatchers(HttpMethod.PUT, "/api/items/**").hasAnyRole("ADMIN", "LIBRARIAN")
            .requestMatchers(HttpMethod.GET, "/api/items", "/api/items/**").hasAnyRole("ADMIN", "LIBRARIAN", "MEMBER")
            .requestMatchers(HttpMethod.DELETE, "/api/members/*").hasRole("ADMIN")
            .requestMatchers("/api/members", "/api/members/**").hasAnyRole("ADMIN", "LIBRARIAN")
            .requestMatchers("/api/borrowings", "/api/borrowings/**").hasRole("LIBRARIAN")
            .requestMatchers("/api/reports", "/api/reports/**").hasRole("ADMIN")
            .anyRequest().denyAll()
        );

        http.httpBasic(Customizer.withDefaults());
        http.csrf(csrf -> csrf.disable());

        return http.build();
    }

    @Bean
    public SecurityFilterChain webFilterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(configurer -> configurer
            .requestMatchers(HttpMethod.POST, "/items/*/delete").hasRole("ADMIN")
            .requestMatchers(HttpMethod.GET,"/items/books/new","/items/magazines/new","/items/newspapers/new").hasAnyRole("ADMIN", "LIBRARIAN")
            .requestMatchers(HttpMethod.POST,"/items/books","/items/magazines","/items/newspapers").hasAnyRole("ADMIN", "LIBRARIAN")
            .requestMatchers(HttpMethod.GET,"/items/books/*/edit","/items/magazines/*/edit","/items/newspapers/*/edit").hasAnyRole("ADMIN", "LIBRARIAN")
            .requestMatchers(HttpMethod.POST,"/items/books/*","/items/magazines/*","/items/newspapers/*").hasAnyRole("ADMIN", "LIBRARIAN")
            .requestMatchers(HttpMethod.POST, "/members/*/delete").hasRole("ADMIN")
            .requestMatchers("/members", "/members/**").hasAnyRole("ADMIN", "LIBRARIAN")
            .requestMatchers(HttpMethod.POST, "/borrowings/*/delete").hasRole("ADMIN")
            .requestMatchers("/borrowings", "/borrowings/**").hasAnyRole("ADMIN", "LIBRARIAN")
            .requestMatchers("/reports", "/reports/**").hasRole("ADMIN")
            .requestMatchers("/css/**").permitAll()
            .anyRequest().authenticated()
        );

        http.formLogin(form -> form
            .loginPage("/login")
            .defaultSuccessUrl("/items", true)
            .permitAll()
        );

        return http.build();
    }
}
