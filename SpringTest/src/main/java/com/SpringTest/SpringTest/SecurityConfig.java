package com.SpringTest.SpringTest;

import com.SpringTest.SpringTest.service.TaiKhoanUserDetailsService; // Bạn sẽ cần tạo service này
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true, securedEnabled = true, jsr250Enabled = true) // Để dùng @PreAuthorize, @Secured
public class SecurityConfig {

    @Autowired
    private TaiKhoanUserDetailsService taiKhoanUserDetailsService; // Service để load user từ DB

    @Bean
    public static PasswordEncoder passwordEncoder() {
        return NoOpPasswordEncoder.getInstance();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(authorize -> authorize
                        // Cho phép tất cả mọi người truy cập vào các URL này
                        .requestMatchers("/", "/login", "/logout", "/css/**", "/js/**", "/images/**").permitAll()
                        // Yêu cầu quyền 'ADMIN' cho các trang quản lý
                        .requestMatchers("/admin/**").hasAnyRole("ADMIN", "MANAGER")
                        // Yêu cầu quyền 'EMPLOYEE' cho các trang nhân viên
                        .requestMatchers("/employee/**").hasRole("EMPLOYEE")
                        // Yêu cầu quyền 'MANAGER' cho các trang quản lý cấp cao
                        .requestMatchers("/manager/**").hasRole("MANAGER")
                        // Tất cả các yêu cầu khác đều cần phải được xác thực
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login") // Đường dẫn đến trang đăng nhập tùy chỉnh
                        .loginProcessingUrl("/login") // URL xử lý form đăng nhập
                        .defaultSuccessUrl("/admin/dashboard", true) // Trang chuyển hướng sau khi đăng nhập thành công
                        .failureUrl("/login?error=true") // Trang chuyển hướng khi đăng nhập thất bại
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutRequestMatcher(new AntPathRequestMatcher("/logout")) // URL để thực hiện logout
                        .logoutSuccessUrl("/login?logout=true") // Trang chuyển hướng sau khi logout thành công
                        .permitAll()
                );
        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder authenticationManagerBuilder =
                http.getSharedObject(AuthenticationManagerBuilder.class);
        authenticationManagerBuilder
                .userDetailsService(taiKhoanUserDetailsService) // Cung cấp UserDetailsService
                .passwordEncoder(passwordEncoder());        // Cung cấp PasswordEncoder
        return authenticationManagerBuilder.build();
    }
}