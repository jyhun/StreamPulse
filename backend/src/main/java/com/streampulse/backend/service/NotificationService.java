package com.streampulse.backend.service;

import com.streampulse.backend.dto.NotificationRequestDTO;
import com.streampulse.backend.entity.Notification;
import com.streampulse.backend.entity.StreamEvent;
import com.streampulse.backend.entity.StreamMetrics;
import com.streampulse.backend.enums.EventType;
import com.streampulse.backend.infra.DiscordNotifier;
import com.streampulse.backend.repository.NotificationRepository;
import com.streampulse.backend.repository.StreamEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final StreamEventRepository streamEventRepository;
    private final DiscordNotifier discordNotifier;
    private final RestTemplate restTemplate;

    @Value("${processor.url}")
    private String processorUrl;

    public void saveNotification(NotificationRequestDTO notificationRequestDTO) {
        StreamEvent streamEvent = streamEventRepository.findById(notificationRequestDTO.getStreamEventId())
                .orElseThrow(() -> new IllegalArgumentException("방송 이벤트를 찾을 수 없습니다."));

        Notification notification = Notification.builder()
                .streamEvent(streamEvent)
                .receiverId(notificationRequestDTO.getReceiverId())
                .success(notificationRequestDTO.isSuccess())
                .message(notificationRequestDTO.getMessage())
                .errorMessage(notificationRequestDTO.getErrorMessage())
                .build();

        notificationRepository.save(notification);
    }

    public void requestStreamStatusNotification(String channelId, EventType eventType) {
        String url = processorUrl + "/api/stream-status";

        Map<String, String> payload = new HashMap<>();
        payload.put("streamerId", channelId);
        payload.put("eventType", eventType.name());
        restTemplate.postForEntity(url, payload, Void.class);
    }


    public void requestStreamHotNotification(StreamEvent streamEvent) {
        String url = processorUrl + "/api/send-notification";

        // 한국 시간대로 시간대 설정
        ZoneId seoulZoneId = ZoneId.of("Asia/Seoul");

        // 데이터 추출
        StreamMetrics metrics = streamEvent.getMetrics();
        String channelId = metrics.getSession().getStreamer().getChannelId();
        String streamerUrl = "https://chzzk.naver.com/live/" + channelId;
        String nickname = metrics.getSession().getStreamer().getNickname();
        String title = metrics.getTitle();
        String category = metrics.getCategory();
        int viewerCount = metrics.getViewerCount();
        int averageViewerCount = metrics.getSession().getStreamer().getAverageViewerCount();
        String summary = streamEvent.getSummary();

        // 감지 시각 (한국 시간)
        ZonedDateTime detectedAtSeoul = streamEvent.getDetectedAt().atZone(seoulZoneId);
        String formattedDate = detectedAtSeoul.format(DateTimeFormatter.ofPattern("yyyy년 M월 d일 HH:mm"));

        // 방송 시작 시각 (한국 시간)
        ZonedDateTime startedAtSeoul = metrics.getSession().getStartedAt().atZone(seoulZoneId);

        // 평균 대비 증가율 계산 (0 division 방지)
        float viewerIncreaseRate = 0;
        if (averageViewerCount > 0) {
            viewerIncreaseRate = ((float) viewerCount / averageViewerCount) * 100;
        }

        // Payload 구성
        Map<String, Object> payload = new HashMap<>();
        payload.put("streamEventId", streamEvent.getId());
        payload.put("streamerId", channelId);
        payload.put("streamerUrl", streamerUrl);
        payload.put("nickname", nickname);
        payload.put("title", title);
        payload.put("category", category);
        payload.put("viewerCount", viewerCount);
        payload.put("summary", summary);
        payload.put("formattedDate", formattedDate);
        payload.put("startedAt", startedAtSeoul.toString());
        payload.put("viewerIncreaseRate", viewerIncreaseRate);

        // 전송
        restTemplate.postForEntity(url, payload, Void.class);
    }


}