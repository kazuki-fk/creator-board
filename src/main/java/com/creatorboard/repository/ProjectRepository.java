package com.creatorboard.repository;

import com.creatorboard.entity.Project;
import com.creatorboard.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ProjectRepository extends JpaRepository<Project, Long> {
    List<Project> findByUser(User user);
    List<Project> findByUserAndStatus(User user, String status);
}