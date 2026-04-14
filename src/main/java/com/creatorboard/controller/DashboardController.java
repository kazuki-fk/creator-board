package com.creatorboard.controller;

import com.creatorboard.entity.Project;
import com.creatorboard.entity.User;
import com.creatorboard.repository.ProjectRepository;
import com.creatorboard.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;

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

                List<Project> todoProjects = projectRepository.findByUserAndStatus(user, "未着手");
                List<Project> doingProjects = projectRepository.findByUserAndStatus(user, "進行中");
                List<Project> doneProjects = projectRepository.findByUserAndStatus(user, "完了");
                List<Project> allProjects = projectRepository.findByUser(user);

                // サマリー
                model.addAttribute("totalCount", allProjects.size());
                model.addAttribute("todoCount", todoProjects.size());
                model.addAttribute("doingCount", doingProjects.size());
                model.addAttribute("doneCount", doneProjects.size());

                // カンバン用
                model.addAttribute("todoProjects", todoProjects);
                model.addAttribute("doingProjects", doingProjects);
                model.addAttribute("doneProjects", doneProjects);
                model.addAttribute("username", user.getUsername());

                // フェーズ別集計（棒グラフ用）
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

                // Chart.js用にJSON形式で渡す
                StringBuilder labels = new StringBuilder();
                StringBuilder data = new StringBuilder();
                for (Map.Entry<String, Long> entry : phaseMap.entrySet()) {
                        labels.append("'").append(entry.getKey()).append("',");
                        data.append(entry.getValue()).append(",");
                }

                model.addAttribute("phaseLabels", labels.toString());
                model.addAttribute("phaseData", data.toString());

                return "dashboard";
        }
}