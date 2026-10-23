package ru.learning.taskapiservice.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name="tasks")
public class Task {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

     @Column(nullable = false)
    private String title;

    @Setter
    @Getter
    private  String description;

    @Setter
    @Getter
    @Column(nullable = false, length = 30)
    private String status;
    @Column(nullable = false, length = 30)
    private String priority;
    @Setter
    @Getter
    @Column(nullable = false,length = 30)
    private String assignee;

    @Setter
    @Getter
    @Column(name = "created_at", nullable = false, updatable = false)
    @Schema(type = "string", example = "2026-10-22T15:13:05.04423")
    private LocalDateTime createdAt;

    @Setter
    @Getter
    @Column(name = "updated_at", nullable = false)
    @Schema(type = "string", example = "2026-10-22T15:13:05.04423")
    private LocalDateTime updatedAt;

    public Task(){
    }

    @PrePersist
    protected void onCreate(){
        LocalDateTime now = LocalDateTime.now();

        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate(){
        updatedAt = LocalDateTime.now();
    }
}
