package com.invite.invite;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;



@Configuration
@EnableWebSecurity
public class WebSecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception 
    {
        
        http.authorizeHttpRequests((requests)-> 
                requests.requestMatchers("/images/*","/convite/**","/error","/favicon.ico")
                .permitAll()
                .anyRequest()
                .authenticated())
            .formLogin((form) -> form.permitAll())
            .logout((logout)-> logout.permitAll())
            .csrf(AbstractHttpConfigurer::disable)
            .cors((cors)-> cors.disable());
            
        return http.build();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource()
    {
        CorsConfiguration corsConfig = new CorsConfiguration();
        corsConfig.setAllowedMethods(java.util.Arrays.asList("*"));
        corsConfig.setAllowedMethods(java.util.Arrays.asList("*"));
        corsConfig.setAllowedHeaders(java.util.Arrays.asList("*"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfig);
        return source;
    }

    @Bean
    public UserDetailsService userDetailsService()
    {
        
        /*
         * Em cenário de produção os dados de segurança para login de usuário devem ser obtidos do banco de dados
         * assim como regras (roles) de autorização
         * 
         */
        UserDetails user = User.builder()
        .username("admin")
        .password("{noop}1234")
        .roles("USER","ADMIN")
        .build();

        return new InMemoryUserDetailsManager(user);
    }

 

    public PasswordEncoder passwordEncoder()
    {
        return new BCryptPasswordEncoder();
    }
}
