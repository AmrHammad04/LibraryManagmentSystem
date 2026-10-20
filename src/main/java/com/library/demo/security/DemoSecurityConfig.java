package com.library.demo.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

import javax.sql.DataSource;

@Configuration
public class DemoSecurityConfig {

    @Bean
    public UserDetailsManager userDetailsManager(DataSource dataSource) {
        JdbcUserDetailsManager jdbcUserDetailsManager = new JdbcUserDetailsManager(dataSource);
                return jdbcUserDetailsManager;
    }
       
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(configurer ->
                configurer
                        .requestMatchers(HttpMethod.GET, "/api/items/**").hasRole("MEMBER")
                        
                        .requestMatchers("/api/members/**").hasRole("LIBRARIAN")
                        .requestMatchers("/api/borrowings/**").hasRole("LIBRARIAN")
                        .requestMatchers(HttpMethod.POST, "/api/items/**").hasRole("LIBRARIAN")
                        .requestMatchers(HttpMethod.PUT, "/api/items/**").hasRole("LIBRARIAN")
                        .requestMatchers(HttpMethod.DELETE, "/api/items/**").hasRole("ADMIN")
                        .requestMatchers("/api/reports/**").hasRole("ADMIN")


                        .requestMatchers(HttpMethod.GET, "/items/books/new").hasRole("LIBRARIAN")
                        .requestMatchers(HttpMethod.POST, "/items/books").hasRole("LIBRARIAN")
                        .requestMatchers(HttpMethod.GET, "/items/books/*/edit").hasRole("LIBRARIAN")
                        .requestMatchers(HttpMethod.POST, "/items/books/*").hasRole("LIBRARIAN")
                        .requestMatchers(HttpMethod.POST, "/items/*/delete").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/items/magazines/new").hasRole("LIBRARIAN")
                        .requestMatchers(HttpMethod.POST, "/items/magazines").hasRole("LIBRARIAN")
                        .requestMatchers(HttpMethod.GET, "/items/newspapers/new").hasRole("LIBRARIAN")
                        .requestMatchers(HttpMethod.POST, "/items/newspapers").hasRole("LIBRARIAN")
                        .requestMatchers(HttpMethod.POST, "/members/*/delete").hasRole("ADMIN")
                        .requestMatchers("/members", "/members/**").hasAnyRole("ADMIN", "LIBRARIAN")
                        .requestMatchers("/borrowings/**").hasRole("LIBRARIAN")
                        .requestMatchers("/reports", "/reports/**").hasRole("ADMIN")

                        .anyRequest().authenticated()
        );

        http.httpBasic(Customizer.withDefaults());
        http.formLogin(form -> form.loginPage("/login").permitAll());

        http.csrf(csrf -> csrf.ignoringRequestMatchers("/api/**"));

        return http.build();
    }
}