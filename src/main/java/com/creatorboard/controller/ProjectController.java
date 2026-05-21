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

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.*;
import java.util.UUID;

@Controller
@RequestMapping("/projects")
public class ProjectController {

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProjectLogRepository projectLogRepository;

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    // guestユーザー判定ヘルパー
    private boolean isGuest(Principal principal) {
        return principal == null || principal.getName().equals("guest");
    }

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
            @RequestParam(required = false) String initialLogDate,
            @RequestParam(required = false) MultipartFile alsFile) throws IOException {
        if (result.hasErrors()) {
            return "project-form";
        }
        User user = userRepository.findByUsername(principal.getName())
                .orElseThrow();
        project.setUser(user);

        // ファイルが添付されていれば保存
        if (alsFile != null && !alsFile.isEmpty()) {
            Path dir = Paths.get(uploadDir, String.valueOf(user.getId()));
            Files.createDirectories(dir);
            String uniqueName = UUID.randomUUID() + "_" + alsFile.getOriginalFilename();
            Files.copy(alsFile.getInputStream(), dir.resolve(uniqueName),
                    StandardCopyOption.REPLACE_EXISTING);
            project.setFilePath(dir.resolve(uniqueName).toString());
            project.setFileName(alsFile.getOriginalFilename());
        }

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

        boolean guest = isGuest(principal);
        if (guest) {
            if (!project.getUser().getUsername().equals("demo")) {
                return "redirect:/dashboard";
            }
        } else {
            if (!project.getUser().getUsername().equals(principal.getName())) {
                return "redirect:/dashboard";
            }
        }

        List<ProjectLog> logs = projectLogRepository.findByProjectOrderByDateDesc(project);
        model.addAttribute("project", project);
        model.addAttribute("logs", logs);
        model.addAttribute("isGuest", guest);
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

    // ダウンロード用
    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> download(@PathVariable Long id,
            Principal principal) throws MalformedURLException {
        Project project = projectRepository.findById(id).orElseThrow();
        if (!project.getUser().getUsername().equals(principal.getName())) {
            return ResponseEntity.status(403).build();
        }
        if (project.getFilePath() == null) {
            return ResponseEntity.notFound().build();
        }
        Path filePath = Paths.get(project.getFilePath());
        Resource resource = new UrlResource(filePath.toUri());
        if (!resource.exists()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + project.getFileName() + "\"")
                .body(resource);
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
        // デモユーザーのプロジェクトは編集不可
        if (project.getUser().getUsername().equals("demo")) {
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

    // ステータス更新（カンバン列移動）
    @PostMapping("/{id}/status")
    public String updateStatus(@PathVariable Long id,
            @RequestParam String status,
            Principal principal) {
        Project project = projectRepository.findById(id).orElseThrow();
        boolean guest = isGuest(principal);
        if (guest) {
            // ゲストはdemoユーザーのプロジェクトのみ操作可能
            if (!project.getUser().getUsername().equals("demo")) {
                return "redirect:/dashboard";
            }
        } else {
            if (!project.getUser().getUsername().equals(principal.getName())) {
                return "redirect:/dashboard";
            }
        }
        project.setStatus(status);
        projectRepository.save(project);
        return "redirect:/dashboard";
    }

    // フェーズ更新
    @PostMapping("/{id}/phase")
    public String updatePhase(@PathVariable Long id,
            @RequestParam String phase,
            Principal principal) {
        Project project = projectRepository.findById(id).orElseThrow();
        boolean guest = isGuest(principal);
        if (guest) {
            // ゲストはdemoユーザーのプロジェクトのみ操作可能
            if (!project.getUser().getUsername().equals("demo")) {
                return "redirect:/dashboard";
            }
        } else {
            if (!project.getUser().getUsername().equals(principal.getName())) {
                return "redirect:/dashboard";
            }
        }
        project.setPhase(phase);
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
        // デモユーザーのプロジェクトは削除不可
        if (project.getUser().getUsername().equals("demo")) {
            return "redirect:/dashboard";
        }
        projectRepository.delete(project);
        return "redirect:/dashboard";
    }

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
        // デモユーザーのプロジェクトは日誌追加不可
        if (project.getUser().getUsername().equals("demo")) {
            return "redirect:/projects/" + id;
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
        // デモユーザーのプロジェクトは日誌削除不可
        if (project.getUser().getUsername().equals("demo")) {
            return "redirect:/projects/" + id;
        }
        projectLogRepository.deleteById(logId);
        return "redirect:/projects/" + id;
    }
}