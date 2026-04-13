package com.creatorboard.controller;

import com.creatorboard.entity.User;
import com.creatorboard.repository.ProjectRepository;
import com.creatorboard.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;

@Controller
public class DashboardController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @GetMapping("/")
    public String showDashboard(Model model, Principal principal) {
        User user = userRepository.findByUsername(principal.getName())
                .orElseThrow();

        model.addAttribute("username", user.getUsername());
        model.addAttribute("todoProjects",
                projectRepository.findByUserAndStatus(user, "未着手"));
        model.addAttribute("doingProjects",
                projectRepository.findByUserAndStatus(user, "進行中"));
        model.addAttribute("doneProjects",
                projectRepository.findByUserAndStatus(user, "完了"));

        return "dashboard";
    }
}