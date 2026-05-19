package com.creatorboard.controller;

import com.creatorboard.config.DeviceCategory;
import com.creatorboard.entity.AlsAnalysis;
import com.creatorboard.entity.Project;
import com.creatorboard.entity.User;
import com.creatorboard.repository.AlsAnalysisRepository;
import com.creatorboard.repository.ProjectRepository;
import com.creatorboard.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;
import java.time.LocalDate;
import java.util.*;

@Controller
public class DashboardController {

        @Autowired
        private UserRepository userRepository;

        @Autowired
        private ProjectRepository projectRepository;

        @Autowired
        private AlsAnalysisRepository alsAnalysisRepository;

        private static final int PAGE_SIZE = 6;

        @GetMapping("/dashboard")
        public String showDashboard(Model model, Principal principal,
                        @RequestParam(defaultValue = "0") int todoPage,
                        @RequestParam(defaultValue = "0") int doingPage,
                        @RequestParam(defaultValue = "0") int donePage) {

                // ゲスト（未ログイン）はdemoユーザーのデータを表示
                boolean isGuest = (principal == null);
                String username = isGuest ? "demo" : principal.getName();

                User user = userRepository.findByUsername(username).orElseThrow();
                model.addAttribute("isGuest", isGuest);

                Pageable todoPageable = PageRequest.of(todoPage, PAGE_SIZE, Sort.by("createdAt").descending());
                Pageable doingPageable = PageRequest.of(doingPage, PAGE_SIZE, Sort.by("createdAt").descending());
                Pageable donePageable = PageRequest.of(donePage, PAGE_SIZE, Sort.by("createdAt").descending());

                Page<Project> todoPage_ = projectRepository.findByUserAndStatus(user, "未着手", todoPageable);
                Page<Project> doingPage_ = projectRepository.findByUserAndStatus(user, "進行中", doingPageable);
                Page<Project> donePage_ = projectRepository.findByUserAndStatus(user, "完了", donePageable);

                List<Project> allProjects = projectRepository.findByUser(user);

                model.addAttribute("username", user.getUsername());
                model.addAttribute("totalCount", allProjects.size());
                model.addAttribute("todoCount", todoPage_.getTotalElements());
                model.addAttribute("doingCount", doingPage_.getTotalElements());
                model.addAttribute("doneCount", donePage_.getTotalElements());

                model.addAttribute("todoProjects", todoPage_.getContent());
                model.addAttribute("doingProjects", doingPage_.getContent());
                model.addAttribute("doneProjects", donePage_.getContent());

                model.addAttribute("todoTotalPages", todoPage_.getTotalPages());
                model.addAttribute("doingTotalPages", doingPage_.getTotalPages());
                model.addAttribute("doneTotalPages", donePage_.getTotalPages());
                model.addAttribute("todoCurrentPage", todoPage);
                model.addAttribute("doingCurrentPage", doingPage);
                model.addAttribute("doneCurrentPage", donePage);

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

                LocalDate today = LocalDate.now();
                Set<Long> expiredIds = new HashSet<>();
                for (Project p : allProjects) {
                        if (p.getDeadline() != null && p.getDeadline().isBefore(today)) {
                                expiredIds.add(p.getId());
                        }
                }
                model.addAttribute("expiredIds", expiredIds);

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

                // 解析履歴
                List<AlsAnalysis> analyses = alsAnalysisRepository.findByUserOrderByAnalyzedAtDesc(user);

                Map<String, Integer> drumCount = new LinkedHashMap<>();
                Map<String, Integer> mixingCount = new LinkedHashMap<>();
                Map<String, Integer> fxCount = new LinkedHashMap<>();
                Map<String, Integer> instrumentCount = new LinkedHashMap<>();

                ObjectMapper mapper = new ObjectMapper();
                for (AlsAnalysis a : analyses) {
                        try {
                                if (a.getDevicesJson() != null) {
                                        String[] devices = mapper.readValue(a.getDevicesJson(), String[].class);
                                        for (String d : devices) {
                                                String category = DeviceCategory.classify(d);
                                                switch (category) {
                                                        case "drum" ->
                                                                drumCount.put(d, drumCount.getOrDefault(d, 0) + 1);
                                                        case "mixing" ->
                                                                mixingCount.put(d, mixingCount.getOrDefault(d, 0) + 1);
                                                        case "fx" -> fxCount.put(d, fxCount.getOrDefault(d, 0) + 1);
                                                        default -> instrumentCount.put(d,
                                                                        instrumentCount.getOrDefault(d, 0) + 1);
                                                }
                                        }
                                }
                        } catch (Exception ignored) {
                        }
                }

                model.addAttribute("analyses", analyses.subList(0, Math.min(5, analyses.size())));
                model.addAttribute("drumTop5", sortTop5(drumCount));
                model.addAttribute("mixingTop5", sortTop5(mixingCount));
                model.addAttribute("fxTop5", sortTop5(fxCount));
                model.addAttribute("instrumentTop5", sortTop5(instrumentCount));
                model.addAttribute("hasAnalysis", !analyses.isEmpty());

                return "dashboard";
        }

        private List<Map.Entry<String, Integer>> sortTop5(Map<String, Integer> map) {
                List<Map.Entry<String, Integer>> list = new ArrayList<>(map.entrySet());
                list.sort((a, b) -> b.getValue() - a.getValue());
                return list.subList(0, Math.min(5, list.size()));
        }
}