package com.creatorboard.repository;

import com.creatorboard.entity.AlsAnalysis;
import com.creatorboard.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AlsAnalysisRepository extends JpaRepository<AlsAnalysis, Long> {
    List<AlsAnalysis> findByUserOrderByAnalyzedAtDesc(User user);
}