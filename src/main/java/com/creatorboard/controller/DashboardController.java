package com.creatorboard.controller;

import com.creatorboard.entity.AlsAnalysis;
import com.creatorboard.entity.Project;
import com.creatorboard.entity.User;
import com.creatorboard.repository.AlsAnalysisRepository;
import com.creatorboard.repository.ProjectRepository;
import com.creatorboard.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;
import java.util.*;

@Controller
public class DashboardController {

        @Autowired
        private UserRepository userRepository;

        @Autowired
        private ProjectRepository projectRepository;

        @Autowired
        private AlsAnalysisRepository alsAnalysisRepository;

        @GetMapping("/")
        public String showDashboard(Model model, Principal principal) {
                User user = userRepository.findByUsername(principal.getName())
                                .orElseThrow();

                // プロジェクト
                List<Project> todoProjects = projectRepository.findByUserAndStatus(user, "未着手");
                List<Project> doingProjects = projectRepository.findByUserAndStatus(user, "進行中");
                List<Project> doneProjects = projectRepository.findByUserAndStatus(user, "完了");
                List<Project> allProjects = projectRepository.findByUser(user);

                model.addAttribute("username", user.getUsername());
                model.addAttribute("totalCount", allProjects.size());
                model.addAttribute("todoCount", todoProjects.size());
                model.addAttribute("doingCount", doingProjects.size());
                model.addAttribute("doneCount", doneProjects.size());
                model.addAttribute("todoProjects", todoProjects);
                model.addAttribute("doingProjects", doingProjects);
                model.addAttribute("doneProjects", doneProjects);

                // フェーズ別集計
                Map<String, Long> phaseMap = new LinkedHashMap<>();
                phaseMap.put("作曲中", 0L);
                phaseMap.put("編曲中", 0L);
                phaseMap.put("レコーディング中", 0L);
                phaseMap.put("ミキシング中", 0L);
                phaseMap.put("マスタリング中", 0L);
                phaseMap.put("リリース準備中", 0L);

                for (Project p : allProjects) {
                        if (p.getPhase() != null && phaseMap.containsKey(p.getPhase())) {
                                phaseMap.put(p.getPhase(), phaseMap.get(p.getPhase()) + 1);
                        }
                }

                // グラフ表示用ラベルは短縮版
                Map<String, String> labelMap = new LinkedHashMap<>();
                labelMap.put("作曲中", "作曲中");
                labelMap.put("編曲中", "編曲中");
                labelMap.put("レコーディング中", "録音中");
                labelMap.put("ミキシング中", "MIX中");
                labelMap.put("マスタリング中", "MAST中");
                labelMap.put("リリース準備中", "リリース");

                StringBuilder labels = new StringBuilder();
                StringBuilder data = new StringBuilder();
                for (Map.Entry<String, Long> entry : phaseMap.entrySet()) {
                        labels.append("'").append(labelMap.get(entry.getKey())).append("',");
                        data.append(entry.getValue()).append(",");
                }

                model.addAttribute("phaseLabels", labels.toString());
                model.addAttribute("phaseData", data.toString());

                // 解析履歴（最新5件）
                List<AlsAnalysis> analyses = alsAnalysisRepository.findByUserOrderByAnalyzedAtDesc(user);

                // よく使うデバイス集計
                Map<String, Integer> deviceCount = new LinkedHashMap<>();
                ObjectMapper mapper = new ObjectMapper();
                for (AlsAnalysis a : analyses) {
                        try {
                                if (a.getDevicesJson() != null) {
                                        String[] devices = mapper.readValue(
                                                        a.getDevicesJson(), String[].class);
                                        for (String d : devices) {
                                                deviceCount.put(d, deviceCount.getOrDefault(d, 0) + 1);
                                        }
                                }
                        } catch (Exception ignored) {
                        }
                }

                // 件数順にソートしてTop5取得
                List<Map.Entry<String, Integer>> deviceRanking = new ArrayList<>(deviceCount.entrySet());
                deviceRanking.sort((a, b) -> b.getValue() - a.getValue());
                List<Map.Entry<String, Integer>> top5 = deviceRanking.subList(0, Math.min(5, deviceRanking.size()));

                model.addAttribute("analyses", analyses.subList(0, Math.min(5, analyses.size())));
                model.addAttribute("deviceTop5", top5);
                model.addAttribute("hasAnalysis", !analyses.isEmpty());

                return "dashboard";
        }
}