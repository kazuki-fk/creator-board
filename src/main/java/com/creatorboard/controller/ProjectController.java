package com.creatorboard.controller;

import com.creatorboard.entity.Project;
import com.creatorboard.entity.ProjectLog;
import com.creatorboard.entity.User;
import com.creatorboard.repository.ProjectLogRepository;
import com.creatorboard.repository.ProjectRepository;
import com.creatorboard.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import java.time.LocalDate;
import java.util.List;
import jakarta.validation.Valid;
import org.springframework.validation.BindingResult;

@Controller
@RequestMapping("/projects")
public class ProjectController {

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProjectLogRepository projectLogRepository;

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
            Principal principal,
            @RequestParam(required = false) String initialLog,
            @RequestParam(required = false) String initialLogDate) {
        if (result.hasErrors()) {
            return "project-form";
        }
        User user = userRepository.findByUsername(principal.getName())
                .orElseThrow();
        project.setUser(user);
        projectRepository.save(project);

        if (initialLog != null && !initialLog.isBlank()) {
            ProjectLog log = new ProjectLog();
            log.setProject(project);
            log.setDate(initialLogDate != null && !initialLogDate.isBlank()
                    ? LocalDate.parse(initialLogDate)
                    : LocalDate.now());
            log.setContent(initialLog);
            projectLogRepository.save(log);
        }

        return "redirect:/projects/" + project.getId();
    }

    // 詳細・編集フォーム表示
    @GetMapping("/{id}")
    public String showDetail(@PathVariable Long id, Model model, Principal principal) {
        Project project = projectRepository.findById(id).orElseThrow();
        if (!project.getUser().getUsername().equals(principal.getName())) {
            return "redirect:/dashboard";
        }
        List<ProjectLog> logs = projectLogRepository.findByProjectOrderByDateDesc(project);
        model.addAttribute("project", project);
        model.addAttribute("logs", logs);
        return "project-detail";
    }

    @GetMapping("/{id}/logs/{logId}/delete")
    public String deleteLogGet(@PathVariable Long id,
            @PathVariable Long logId,
            Principal principal) {
        Project project = projectRepository.findById(id).orElseThrow();
        if (!project.getUser().getUsername().equals(principal.getName())) {
            return "redirect:/dashboard";
        }
        projectLogRepository.deleteById(logId);
        return "redirect:/projects/" + id;
    }

    // 編集保存
    @PostMapping("/{id}/edit")
    public String editProject(@PathVariable Long id,
            @Valid @ModelAttribute Project form,
            BindingResult result,
            Principal principal,
            @RequestParam(required = false) String logDate,
            @RequestParam(required = false) String logContent) {
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
        project.setDeadline(form.getDeadline());
        projectRepository.save(project);

        // 日誌があれば保存
        if (logContent != null && !logContent.isBlank()) {
            ProjectLog log = new ProjectLog();
            log.setProject(project);
            log.setDate(logDate != null && !logDate.isBlank()
                    ? LocalDate.parse(logDate)
                    : LocalDate.now());
            log.setContent(logContent);
            projectLogRepository.save(log);
        }

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

    // 制作日誌部分

    // 日誌追加
    @PostMapping("/{id}/logs")
    public String addLog(@PathVariable Long id,
            @RequestParam String date,
            @RequestParam String content,
            Principal principal) {
        Project project = projectRepository.findById(id).orElseThrow();
        if (!project.getUser().getUsername().equals(principal.getName())) {
            return "redirect:/dashboard";
        }
        ProjectLog log = new ProjectLog();
        log.setProject(project);
        log.setDate(LocalDate.parse(date));
        log.setContent(content);
        projectLogRepository.save(log);
        return "redirect:/projects/" + id;
    }

    // 日誌削除
    @PostMapping("/{id}/logs/{logId}/delete")
    public String deleteLog(@PathVariable Long id,
            @PathVariable Long logId,
            Principal principal) {
        Project project = projectRepository.findById(id).orElseThrow();
        if (!project.getUser().getUsername().equals(principal.getName())) {
            return "redirect:/dashboard";
        }
        projectLogRepository.deleteById(logId);
        return "redirect:/projects/" + id;
    }
}