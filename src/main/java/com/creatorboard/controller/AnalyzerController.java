package com.creatorboard.controller;

import com.creatorboard.service.AnalyzerService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/analyzer")
public class AnalyzerController {

    @Autowired
    private AnalyzerService analyzerService;

    // Analyzer画面表示
    @GetMapping
    public String showAnalyzer() {
        return "analyzer";
    }

    // ファイルアップロード・解析
    @PostMapping("/upload")
    public String uploadFile(@RequestParam("file") MultipartFile file,
            Model model) {
        // ファイル未選択チェック
        if (file.isEmpty()) {
            model.addAttribute("error", "ファイルを選択してください");
            return "analyzer";
        }

        // .als以外のファイルチェック
        if (!file.getOriginalFilename().endsWith(".als")) {
            model.addAttribute("error", ".alsファイルのみアップロードできます");
            return "analyzer";
        }

        try {
            String json = analyzerService.analyze(file);
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(json);

            // エラーチェック
            if (root.has("error")) {
                model.addAttribute("error", root.get("error").asText());
                return "analyzer";
            }

            // 解析結果をModelに格納
            model.addAttribute("projectName",
                    root.get("project_name").asText());
            model.addAttribute("bpm",
                    root.get("bpm").asText());

            // トラック一覧を整形
            List<TrackInfo> tracks = new ArrayList<>();
            for (JsonNode track : root.get("tracks")) {
                String name = track.get("name").asText();
                List<String> devices = new ArrayList<>();
                for (JsonNode device : track.get("devices")) {
                    devices.add(device.asText());
                }
                tracks.add(new TrackInfo(name, devices));
            }
            model.addAttribute("tracks", tracks);

        } catch (Exception e) {
            model.addAttribute("error",
                    "解析に失敗しました。Flaskサーバーが起動しているか確認してください。");
        }

        return "analyzer";
    }

    // トラック情報を保持するインナークラス
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