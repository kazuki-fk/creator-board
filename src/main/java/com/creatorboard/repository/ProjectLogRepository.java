package com.creatorboard.repository;

import com.creatorboard.entity.ProjectLog;
import com.creatorboard.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ProjectLogRepository extends JpaRepository<ProjectLog, Long> {
    List<ProjectLog> findByProjectOrderByDateDesc(Project project);
}