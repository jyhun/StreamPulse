package com.streampulse.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private StreamEvent streamEvent;

    @Column(length = 100)
    private String receiverId;

    private LocalDateTime sentAt;

    @Column(columnDefinition = "TEXT")
    private String message;

    private boolean success;

    @Column(length = 500)
    private String errorMessage;

}
