package com.creatorboard;

import com.creatorboard.entity.Project;
import com.creatorboard.entity.ProjectLog;
import com.creatorboard.entity.User;
import com.creatorboard.repository.ProjectLogRepository;
import com.creatorboard.repository.ProjectRepository;
import com.creatorboard.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private ProjectLogRepository projectLogRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (userRepository.findByUsername("demo").isPresent())
            return;

        // デモユーザー作成
        User demo = new User();
        demo.setUsername("demo");
        demo.setEmail("demo@example.com");
        demo.setPassword(passwordEncoder.encode("demo1234"));
        demo.setRole("ROLE_USER");
        userRepository.save(demo);

        // プロジェクト1
        Project p1 = new Project();
        p1.setTitle("// dub techno sketch 01");
        p1.setBpm(130.0);
        p1.setGenre("Techno");
        p1.setStatus("進行中");
        p1.setPhase("ミキシング中");
        p1.setMemo("G minorベース、キックG1、KOC寄りのグルーヴ");
        p1.setDeadline(LocalDate.of(2025, 5, 31));
        p1.setUser(demo);
        projectRepository.save(p1);

        // プロジェクト2
        Project p2 = new Project();
        p2.setTitle("uk bass experiment");
        p2.setBpm(140.0);
        p2.setGenre("House");
        p2.setStatus("未着手");
        p2.setPhase("作曲中");
        p2.setMemo("Hessle Audioっぽいサブベース重視");
        p2.setDeadline(LocalDate.of(2025, 6, 15));
        p2.setUser(demo);
        projectRepository.save(p2);

        // プロジェクト3
        Project p3 = new Project();
        p3.setTitle("ambient loop set");
        p3.setBpm(90.0);
        p3.setGenre("Ambient");
        p3.setStatus("完了");
        p3.setPhase("リリース準備中");
        p3.setMemo("Aniara録音素材ベース");
        p3.setUser(demo);
        projectRepository.save(p3);

        // プロジェクト4
        Project p4 = new Project();
        p4.setTitle("tech house groove");
        p4.setBpm(128.0);
        p4.setGenre("House");
        p4.setStatus("進行中");
        p4.setPhase("編曲中");
        p4.setMemo("Acting Press寄りのグルーヴ感");
        p4.setDeadline(LocalDate.of(2025, 7, 1));
        p4.setUser(demo);
        projectRepository.save(p4);

        // 制作日誌
        ProjectLog log1 = new ProjectLog();
        log1.setProject(p1);
        log1.setDate(LocalDate.of(2025, 4, 20));
        log1.setContent("キックとベースのチューニング完了。G1でロック済み。");
        projectLogRepository.save(log1);

        ProjectLog log2 = new ProjectLog();
        log2.setProject(p1);
        log2.setDate(LocalDate.of(2025, 4, 22));
        log2.setContent("EchoとReverbのセンドバランス調整。Hybrid Reverbでスペース感を追加。");
        projectLogRepository.save(log2);

        ProjectLog log3 = new ProjectLog();
        log3.setProject(p2);
        log3.setDate(LocalDate.of(2025, 4, 23));
        log3.setContent("サブベースのEQ調整。250Hz以下をLPでカット。");
        projectLogRepository.save(log3);
    }
}