package com.creatorboard.controller;

import com.creatorboard.entity.AlsAnalysis;
import com.creatorboard.entity.User;
import com.creatorboard.repository.AlsAnalysisRepository;
import com.creatorboard.repository.UserRepository;
import com.creatorboard.service.AnalyzerService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

import com.creatorboard.entity.Project;
import com.creatorboard.repository.ProjectRepository;
import java.util.List;

@Controller
@RequestMapping("/analyzer")
public class AnalyzerController {

    @Autowired
    private AnalyzerService analyzerService;

    @Autowired
    private AlsAnalysisRepository alsAnalysisRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @PostMapping("/apply-bpm")
    public String applyBpm(@RequestParam Long projectId,
            @RequestParam Double bpm,
            Principal principal) {
        Project project = projectRepository.findById(projectId).orElseThrow();
        if (!project.getUser().getUsername().equals(principal.getName())) {
            return "redirect:/analyzer";
        }
        project.setBpm(bpm);
        projectRepository.save(project);
        return "redirect:/projects/" + projectId;
    }

    @GetMapping
    public String showAnalyzer(Model model, Principal principal) {
        User user = userRepository.findByUsername(principal.getName())
                .orElseThrow();
        List<AlsAnalysis> histories = alsAnalysisRepository.findByUserOrderByAnalyzedAtDesc(user);
        List<Project> projects = projectRepository.findByUser(user);
        model.addAttribute("histories", histories);
        model.addAttribute("projects", projects);
        return "analyzer";
    }

    @PostMapping("/upload")
    public String uploadFile(@RequestParam("file") MultipartFile file,
            Model model,
            Principal principal) {
        if (file.isEmpty()) {
            model.addAttribute("error", "ファイルを選択してください");
            return "analyzer";
        }

        if (!file.getOriginalFilename().endsWith(".als")) {
            model.addAttribute("error", ".alsファイルのみアップロードできます");
            return "analyzer";
        }

        // DBに保存
        User user = userRepository.findByUsername(principal.getName())
                .orElseThrow();

        try {
            String json = analyzerService.analyze(file);
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(json);

            if (root.has("error")) {
                model.addAttribute("error", root.get("error").asText());
                List<Project> projectsOnError = projectRepository.findByUser(user);
                model.addAttribute("projects", projectsOnError);
                return "analyzer";
            }

            String bpmStr = root.get("bpm").asText();
            double bpm = Double.parseDouble(bpmStr);
            int trackCount = root.get("tracks").size();

            List<String> allDevices = new ArrayList<>();
            List<TrackInfo> tracks = new ArrayList<>();
            for (JsonNode track : root.get("tracks")) {
                String name = track.get("name").asText();
                List<String> devices = new ArrayList<>();
                for (JsonNode device : track.get("devices")) {
                    String d = device.asText();
                    devices.add(d);
                    if (!allDevices.contains(d)) {
                        allDevices.add(d);
                    }
                }
                tracks.add(new TrackInfo(name, devices));
            }

            AlsAnalysis analysis = new AlsAnalysis();
            analysis.setUser(user);
            analysis.setFileName(file.getOriginalFilename());
            analysis.setBpm(bpm);
            analysis.setTrackCount(trackCount);
            analysis.setDevicesJson(mapper.writeValueAsString(allDevices));
            alsAnalysisRepository.save(analysis);

            model.addAttribute("projectName", root.get("project_name").asText());
            model.addAttribute("bpm", bpmStr);
            model.addAttribute("tracks", tracks);
            model.addAttribute("histories",
                    alsAnalysisRepository.findByUserOrderByAnalyzedAtDesc(user));

        } catch (Exception e) {
            model.addAttribute("error",
                    "解析に失敗しました。Flaskサーバーが起動しているか確認してください。");
        }

        List<Project> projects = projectRepository.findByUser(user);
        model.addAttribute("projects", projects);

        return "analyzer";
    }

    // 解析履歴の詳細表示
    @GetMapping("/{id}")
    public String showAnalysisDetail(@PathVariable Long id,
            Model model,
            Principal principal) {
        AlsAnalysis analysis = alsAnalysisRepository.findById(id).orElseThrow();

        if (!analysis.getUser().getUsername().equals(principal.getName())) {
            return "redirect:/analyzer";
        }

        // デバイス一覧をJSONから復元
        List<String> devices = new ArrayList<>();
        try {
            ObjectMapper mapper = new ObjectMapper();
            String[] arr = mapper.readValue(analysis.getDevicesJson(), String[].class);
            devices = List.of(arr);
        } catch (Exception ignored) {
        }

        // 全プロジェクト一覧
        User user = userRepository.findByUsername(principal.getName()).orElseThrow();
        List<Project> projects = projectRepository.findByUser(user);

        model.addAttribute("selectedAnalysis", analysis);
        model.addAttribute("selectedDevices", devices);
        model.addAttribute("projects", projects);
        model.addAttribute("histories",
                alsAnalysisRepository.findByUserOrderByAnalyzedAtDesc(user));

        return "analyzer";
    }

    public static class TrackInfo {
        private String name;
        private List<String> devices;

        public TrackInfo(String name, List<String> devices) {
            this.name = name;
            this.devices = devices;
        }

        public String getName() {
            return name;
        }

        public List<String> getDevices() {
            return devices;
        }
    }
}