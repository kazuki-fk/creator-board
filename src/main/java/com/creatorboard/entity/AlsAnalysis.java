package com.creatorboard.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "als_analyses")
@Data
public class AlsAnalysis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "file_name")
    private String fileName;

    private Double bpm;

    @Column(name = "track_count")
    private Integer trackCount;

    @Column(columnDefinition = "TEXT")
    private String devicesJson;

    @Column(name = "analyzed_at")
    private LocalDateTime analyzedAt;

    @PrePersist
    protected void onCreate() {
        analyzedAt = LocalDateTime.now();
    }
}