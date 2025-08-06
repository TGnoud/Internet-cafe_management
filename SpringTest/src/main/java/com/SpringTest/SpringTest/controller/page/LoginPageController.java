package com.SpringTest.SpringTest.controller.page;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.ui.Model;

@Controller
public class LoginPageController {

    @GetMapping("/login") // URL để truy cập trang đăng nhập
    public String showLoginPage(@RequestParam(value = "error", required = false) String error,
                                @RequestParam(value = "logout", required = false) String logout,
                                Model model) {
        if (error != null) {
            model.addAttribute("loginError", "Tên đăng nhập hoặc mật khẩu không đúng.");
        }
        if (logout != null) {
            model.addAttribute("logoutMessage", "Bạn đã đăng xuất thành công.");
        }
        // model.addAttribute("loginRequest", new LoginRequestDTO()); // Nếu bạn muốn dùng th:object cho form
        return "admin/login"; // Trả về tên của file login.html
    }

    // Nếu bạn muốn trang chủ cũng là trang login nếu chưa đăng nhập:
    @GetMapping("/")
    public String home() {
        return "redirect:/login"; // Chuyển hướng đến trang login
    }
}