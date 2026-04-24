package com.creatorboard.controller;

import com.creatorboard.entity.Project;
import com.creatorboard.entity.User;
import com.creatorboard.repository.ProjectRepository;
import com.creatorboard.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

import jakarta.validation.Valid;
import org.springframework.validation.BindingResult;

@Controller
@RequestMapping("/projects")
public class ProjectController {

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private UserRepository userRepository;

    // 新規作成フォーム表示
    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("project", new Project());
        return "project-form";
    }

    // 新規作成保存
    @PostMapping("/new")
    public String createProject(@Valid @ModelAttribute Project project,
            BindingResult result,
            Principal principal) {
        if (result.hasErrors()) {
            return "project-form";
        }
        User user = userRepository.findByUsername(principal.getName())
                .orElseThrow();
        project.setUser(user);
        projectRepository.save(project);
        return "redirect:/dashboard";
    }

    // 詳細・編集フォーム表示
    @GetMapping("/{id}")
    public String showDetail(@PathVariable Long id, Model model, Principal principal) {
        Project project = projectRepository.findById(id).orElseThrow();
        if (!project.getUser().getUsername().equals(principal.getName())) {
            return "redirect:/dashboard";
        }
        model.addAttribute("project", project);
        return "project-detail";
    }

    // 編集保存
    @PostMapping("/{id}/edit")
    public String editProject(@PathVariable Long id,
            @Valid @ModelAttribute Project form,
            BindingResult result,
            Principal principal) {
        if (result.hasErrors()) {
            return "project-detail";
        }
        Project project = projectRepository.findById(id).orElseThrow();
        if (!project.getUser().getUsername().equals(principal.getName())) {
            return "redirect:/dashboard";
        }
        project.setTitle(form.getTitle());
        project.setGenre(form.getGenre());
        project.setBpm(form.getBpm());
        project.setStatus(form.getStatus());
        project.setPhase(form.getPhase());
        project.setMemo(form.getMemo());
        projectRepository.save(project);
        return "redirect:/dashboard";
    }

    // フェーズ更新
    @PostMapping("/{id}/phase")
    public String updatePhase(@PathVariable Long id,
            @RequestParam String phase,
            Principal principal) {
        Project project = projectRepository.findById(id).orElseThrow();
        if (!project.getUser().getUsername().equals(principal.getName())) {
            return "redirect:/dashboard";
        }
        project.setPhase(phase);
        projectRepository.save(project);
        return "redirect:/dashboard";
    }

    // ステータス更新（カンバン列移動）
    @PostMapping("/{id}/status")
    public String updateStatus(@PathVariable Long id,
            @RequestParam String status,
            Principal principal) {
        Project project = projectRepository.findById(id).orElseThrow();
        if (!project.getUser().getUsername().equals(principal.getName())) {
            return "redirect:/dashboard";
        }
        project.setStatus(status);
        projectRepository.save(project);
        return "redirect:/dashboard";
    }

    // 削除
    @PostMapping("/{id}/delete")
    public String deleteProject(@PathVariable Long id, Principal principal) {
        Project project = projectRepository.findById(id).orElseThrow();
        if (!project.getUser().getUsername().equals(principal.getName())) {
            return "redirect:/dashboard";
        }
        projectRepository.delete(project);
        return "redirect:/dashboard";
    }
}