package com.SpringTest.SpringTest.service;


import com.SpringTest.SpringTest.dto.request.LoginRequest;
import com.SpringTest.SpringTest.dto.response.PhienSuDungInfoResponse;

public interface AuthService {
    PhienSuDungInfoResponse login(LoginRequest loginRequest);
    void logout(int maPhien); // Giả sử maPhien là int
}