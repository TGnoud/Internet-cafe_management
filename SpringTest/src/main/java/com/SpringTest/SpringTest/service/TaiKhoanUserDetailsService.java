package com.SpringTest.SpringTest.service;

import com.SpringTest.SpringTest.entity.TaiKhoan;
import com.SpringTest.SpringTest.repository.TaiKhoanRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class TaiKhoanUserDetailsService implements UserDetailsService {

    @Autowired
    private TaiKhoanRepository taiKhoanRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        TaiKhoan taiKhoan = taiKhoanRepository.findByTenTK(username)
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy tài khoản: " + username));

        List<GrantedAuthority> authorities = new ArrayList<>();
        
        // Gán quyền ADMIN cho tài khoản admin
        if ("admin".equals(username)) {
            authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
        } else {
            // Phân quyền cho các tài khoản khác
            if (taiKhoan.getKhachHang() != null) {
                authorities.add(new SimpleGrantedAuthority("ROLE_CUSTOMER"));
            } else {
                authorities.add(new SimpleGrantedAuthority("ROLE_EMPLOYEE"));
            }
        }

        return new User(
            taiKhoan.getTenTK(),
            taiKhoan.getMatKhau(),
            authorities
        );
    }
}