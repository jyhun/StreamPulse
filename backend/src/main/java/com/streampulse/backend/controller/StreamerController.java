package com.streampulse.backend.controller;

import com.streampulse.backend.dto.StreamerRequestDTO;
import com.streampulse.backend.dto.StreamerResponseDTO;
import com.streampulse.backend.service.StreamerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/streamers")
@RequiredArgsConstructor
public class StreamerController {

    private final StreamerService streamerService;

    @PostMapping
    public ResponseEntity<StreamerResponseDTO> registerStreamer(@RequestBody StreamerRequestDTO streamerRequestDTO) {
        StreamerResponseDTO streamerResponseDTO = streamerService.registerStreamer(streamerRequestDTO);
        return ResponseEntity.ok(streamerResponseDTO);
    }

    @GetMapping("/by-channel/{channelId}")
    public ResponseEntity<StreamerResponseDTO> getStreamerByChannelId(@PathVariable String channelId) {
        StreamerResponseDTO streamerResponseDTO = streamerService.getStreamerByChannelId(channelId);
        return ResponseEntity.ok(streamerResponseDTO);
    }

}
