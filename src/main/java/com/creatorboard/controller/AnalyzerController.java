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

@Controller
@RequestMapping("/analyzer")
public class AnalyzerController {

    @Autowired
    private AnalyzerService analyzerService;

    @Autowired
    private AlsAnalysisRepository alsAnalysisRepository;

    @Autowired
    private UserRepository userRepository;

    @GetMapping
    public String showAnalyzer(Model model, Principal principal) {
        User user = userRepository.findByUsername(principal.getName())
                .orElseThrow();
        List<AlsAnalysis> histories = alsAnalysisRepository.findByUserOrderByAnalyzedAtDesc(user);
        model.addAttribute("histories", histories);
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

        try {
            String json = analyzerService.analyze(file);
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(json);

            if (root.has("error")) {
                model.addAttribute("error", root.get("error").asText());
                return "analyzer";
            }

            String bpmStr = root.get("bpm").asText();
            double bpm = Double.parseDouble(bpmStr);
            int trackCount = root.get("tracks").size();

            // デバイス一覧を収集
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

            // DBに保存
            User user = userRepository.findByUsername(principal.getName())
                    .orElseThrow();
            AlsAnalysis analysis = new AlsAnalysis();
            analysis.setUser(user);
            analysis.setFileName(file.getOriginalFilename());
            analysis.setBpm(bpm);
            analysis.setTrackCount(trackCount);
            analysis.setDevicesJson(mapper.writeValueAsString(allDevices));
            alsAnalysisRepository.save(analysis);

            // 画面表示用
            model.addAttribute("projectName", root.get("project_name").asText());
            model.addAttribute("bpm", bpmStr);
            model.addAttribute("tracks", tracks);
            model.addAttribute("histories",
                    alsAnalysisRepository.findByUserOrderByAnalyzedAtDesc(user));

        } catch (Exception e) {
            model.addAttribute("error",
                    "解析に失敗しました。Flaskサーバーが起動しているか確認してください。");
        }

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