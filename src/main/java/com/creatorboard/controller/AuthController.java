package com.creatorboard.controller;

import com.creatorboard.entity.User;
import com.creatorboard.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.ui.Model;

@Controller
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // 新規登録画面を表示する
    @GetMapping("/signup")
    public String showSignupForm() {
        return "signup"; // templates/signup.html を表示
    }

    // 登録ボタンが押された時の処理
    @PostMapping("/signup")
    public String registerUser(User user, Model model) {
        // 重複チェックを追加
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            model.addAttribute("error", "このユーザー名はすでに使われています");
            return "signup";
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole("ROLE_USER");
        userRepository.save(user);
        return "redirect:/login";
    }

    // @GetMapping("/")
    // public String showDashboard(org.springframework.ui.Model model,
    // java.security.Principal principal) {
    // if (principal == null) {
    // return "redirect:/login";
    // }

    // // ログイン中のユーザー名を取得
    // String username = principal.getName();
    // model.addAttribute("username", username);

    // // まだDBにデータがない場合のためのダミーデータ（後でDB連携に書き換えます）
    // // List<Project> projects = projectRepository.findByUser(...);

    // model.addAttribute("projects", java.util.Collections.emptyList());

    // return "dashboard";
    // }

    @GetMapping("/login")
    public String showLoginForm() {
        return "login"; // templates/login.html を表示
    }
}