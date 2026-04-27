package com.creatorboard.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "projects")
@Data
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @NotBlank(message = "プロジェクト名は必須です")
    @Size(max = 50, message = "プロジェクト名は50文字以内で入力してください")
    private String title;

    private Double bpm;

    private String genre;

    @Column(nullable = false)
    private String status;

    private String phase;

    @Column(columnDefinition = "TEXT")
    private String memo;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    private LocalDate deadline;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    @Column(name = "file_path")
    private String filePath;

    @Column(name = "file_name")
    private String fileName;
}