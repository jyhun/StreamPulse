package com.streampulse.backend.infra;

import com.streampulse.backend.dto.ChzzkRootResponseDTO;
import com.streampulse.backend.dto.LiveResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class ChzzkOpenApiClient {

    private final RestTemplate restTemplate;

    @Value("${chzzk.api-base-url}")
    private String chzzkBaseUrl;

    @Value("${chzzk.client-id}")
    private String clientId;

    @Value("${chzzk.client-secret}")
    private String clientSecret;

    private static final String NO_CURSOR = "";

    // 커서 체인 관리를 위한 내부 클래스
    private static class Node {
        String cursor;
        Node parent;
        Node(String cursor, Node parent) { this.cursor = cursor; this.parent = parent; }
    }

    public List<LiveResponseDTO> fetchLiveList() {
        List<LiveResponseDTO> liveList = new ArrayList<>();
        Set<String> failedCursors = new HashSet<>();
        Set<String> visitedCursors = new HashSet<>();
        Node currentNode = new Node(NO_CURSOR, null);
        visitedCursors.add(NO_CURSOR);

        while (true) {
            String curCursor = currentNode.cursor;
            ChzzkRootResponseDTO response = fetchPage(curCursor);

            // 오류 응답: API가 {"code":400, "message": "잘못된 next 값입니다."} 형태로 내려오면 response가 null 또는 content가 없음
            if (response == null || response.getContent() == null) {
                log.error("잘못된 next 값 {} 에러 발생.", curCursor);
                Node newNode = handleInvalidNext(currentNode, curCursor, visitedCursors, failedCursors);
                if (newNode == null) {
                    log.warn("부모 노드가 없거나 오류 지속으로 종료합니다.");
                    break;
                }
                currentNode = newNode;
                continue;
            }

            String nextCursor = response.getContent().getPage().getNext();
            log.info("previous cursor: {}, next cursor: {}", curCursor, nextCursor);

            // 실패한 커서 혹은 이미 방문한 커서이면 부모 노드로 이동
            if (failedCursors.contains(nextCursor) || visitedCursors.contains(nextCursor)) {
                currentNode = (currentNode.parent != null) ? currentNode.parent : currentNode;
                continue;
            }
            visitedCursors.add(nextCursor);

            List<LiveResponseDTO> data = response.getContent().getData();
            int viewerCount = data.get(0).getConcurrentUserCount();
            if (viewerCount < 100) {
                log.info("시청자 수 {}명 이하. 종료.", viewerCount);
                break;
            }
            liveList.addAll(data);
            currentNode = new Node(nextCursor, currentNode);
        }
        return liveList;
    }

    /**
     * 오류 응답("잘못된 next 값입니다.") 발생 시, 부모 노드로 돌아가서 해당 페이지의 next 값을 재조회합니다.
     * 만약 부모의 next 값이 이전 오류 값과 같으면 10초 대기 후 재조회하여 값이 달라질 때까지 반복합니다.
     * 부모의 next 값이 유효하면 새로운 노드를 생성하여 반환합니다.
     */
    private Node handleInvalidNext(Node currentNode, String errorNext,
                                   Set<String> visited, Set<String> failed) {
        Node parent = currentNode.parent;
        if (parent == null) return null;
        ChzzkRootResponseDTO parentResp = fetchPage(parent.cursor);
        if (parentResp == null || parentResp.getContent() == null) {
            failed.add(parent.cursor);
            return (parent.parent != null) ? parent.parent : currentNode;
        }
        String parentNext = parentResp.getContent().getPage().getNext();
        while (parentNext.equals(errorNext)) {
            log.info("부모의 next 값 {}가 오류 값 {}와 동일. 10초 후 재조회.", parentNext, errorNext);
            try { Thread.sleep(10000); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            parentResp = fetchPage(parent.cursor);
            if (parentResp == null || parentResp.getContent() == null) break;
            parentNext = parentResp.getContent().getPage().getNext();
        }
        if (parentResp == null || parentResp.getContent() == null) {
            return (parent.parent != null) ? parent.parent : currentNode;
        }
        if (visited.contains(parentNext)) {
            return parent.parent;
        }
        visited.add(parentNext);
        return new Node(parentNext, parent);
    }

    private ChzzkRootResponseDTO fetchPage(String next) {
        String url = chzzkBaseUrl + "/open/v1/lives?size=20" + (!NO_CURSOR.equals(next) ? "&next=" + next : "");
        log.info("url:{}", url);
        HttpHeaders headers = new HttpHeaders();
        headers.set("Client-Id", clientId);
        headers.set("Client-Secret", clientSecret);
        HttpEntity<String> entity = new HttpEntity<>(headers);
        try {
            ResponseEntity<ChzzkRootResponseDTO> resp = restTemplate.exchange(url, HttpMethod.GET, entity, ChzzkRootResponseDTO.class);
            return resp.getBody();
        } catch (Exception e) {
            log.error("치지직 라이브 목록 페이징 호출 실패");
            return null;
        }
    }
}
