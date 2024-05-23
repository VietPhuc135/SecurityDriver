package com.example.signingoogle2.Config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
//import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable);
//                .authorizeRequests(authz -> authz
//                    .anyRequest().authenticated()
//                )
//                .formLogin(formLogin -> formLogin
//                        .loginPage("/login").defaultSuccessUrl("/oauth2/**")
//                        .permitAll()
//                )
//                .oauth2Login(oauth2 -> oauth2.loginPage("/oauth2/**"))
//                .logout(logout -> logout
//                        .logoutSuccessUrl("/login") // Sau khi đăng xuất cũng chuyển đến /index
//                );

        return http.build();
//                .authorizeHttpRequests(auth -> auth
//                        .requestMatchers("/", "/login**", "/error**", "/assets/**", "/static/**").permitAll()
//                        .anyRequest().authenticated()
//                )
//                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
//                .authorizeRequests()
//                // Cho phép truy cập vào các trang tĩnh mà không cần xác thực
//                .requestMatchers("/resources/**", "/css/**", "/js/**", "/images/**", "/webjars/**", "/assets/**", "/static/**","/", "/login**", "/error**").permitAll()
//                // Cấu hình các quyền truy cập cho các trang còn lại
//                .anyRequest().authenticated()
//                .and()
//                .oauth2Login(oauth2 -> oauth2.loginPage("/login"));


    }
}
