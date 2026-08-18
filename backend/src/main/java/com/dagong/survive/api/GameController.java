package com.dagong.survive.api;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.util.StringUtils;

import com.dagong.survive.common.ApiResponse;
import com.dagong.survive.common.GameConstants;
import com.dagong.survive.service.GameService;

@RestController
@RequestMapping("/api")
public class GameController {

    private final GameService gameService;

    public GameController(GameService gameService) {
        this.gameService = gameService;
    }

    @GetMapping("/careers")
    public ApiResponse<?> careers() {
        return ApiResponse.ok(gameService.careers());
    }

    @GetMapping("/meta")
    public ApiResponse<?> meta() {
        return ApiResponse.ok(gameService.meta());
    }

    @PostMapping("/game/start")
    public ApiResponse<?> start(@RequestHeader(value = GameConstants.HEADER_USER_ID, required = false) String userId,
            @RequestBody Map<String, String> body) {
        String uid = requireUser(userId, body);
        String careerId = body.get("careerId");
        if (!StringUtils.hasText(careerId)) {
            throw new IllegalArgumentException("请选择职业");
        }
        return ApiResponse.ok(gameService.start(uid, careerId));
    }

    @GetMapping("/game/{gameId}")
    public ApiResponse<?> get(@RequestHeader(GameConstants.HEADER_USER_ID) String userId,
            @PathVariable String gameId) {
        return ApiResponse.ok(gameService.get(userId, gameId));
    }

    @PostMapping("/game/{gameId}/choose")
    public ApiResponse<?> choose(@RequestHeader(GameConstants.HEADER_USER_ID) String userId,
            @PathVariable String gameId, @RequestBody Map<String, String> body) {
        return ApiResponse.ok(gameService.choose(userId, gameId, body.get("optionId")));
    }

    @PostMapping("/game/{gameId}/minigame")
    public ApiResponse<?> minigame(@RequestHeader(GameConstants.HEADER_USER_ID) String userId,
            @PathVariable String gameId, @RequestBody Map<String, Object> body) {
        boolean success = Boolean.TRUE.equals(body.get("success"));
        return ApiResponse.ok(gameService.minigame(userId, gameId, success));
    }

    @PostMapping("/game/{gameId}/fight")
    public ApiResponse<?> fight(@RequestHeader(GameConstants.HEADER_USER_ID) String userId,
            @PathVariable String gameId, @RequestBody Map<String, Object> body) {
        int hits = 0;
        if (body.get("hits") instanceof Number) {
            hits = ((Number) body.get("hits")).intValue();
        }
        return ApiResponse.ok(gameService.fight(userId, gameId, hits));
    }

    @PostMapping("/game/{gameId}/patrol")
    public ApiResponse<?> patrol(@RequestHeader(GameConstants.HEADER_USER_ID) String userId,
            @PathVariable String gameId, @RequestBody Map<String, Object> body) {
        boolean success = Boolean.TRUE.equals(body.get("success"));
        return ApiResponse.ok(gameService.patrol(userId, gameId, success));
    }

    @PostMapping("/game/{gameId}/chat")
    public ApiResponse<?> chat(@RequestHeader(GameConstants.HEADER_USER_ID) String userId,
            @PathVariable String gameId, @RequestBody Map<String, Object> body) {
        boolean match = Boolean.TRUE.equals(body.get("match"));
        String text = body.get("text") == null ? "" : String.valueOf(body.get("text"));
        return ApiResponse.ok(gameService.chat(userId, gameId, text, match));
    }

    @PostMapping("/game/{gameId}/asset")
    public ApiResponse<?> asset(@RequestHeader(GameConstants.HEADER_USER_ID) String userId,
            @PathVariable String gameId, @RequestBody Map<String, String> body) {
        return ApiResponse.ok(gameService.sellAsset(userId, gameId, body.get("item")));
    }

    @PostMapping("/game/{gameId}/ad")
    public ApiResponse<?> ad(@RequestHeader(GameConstants.HEADER_USER_ID) String userId,
            @PathVariable String gameId, @RequestBody Map<String, String> body) {
        return ApiResponse.ok(gameService.ad(userId, gameId, body.get("type")));
    }

    @GetMapping("/rank/survive")
    public ApiResponse<?> rankSurvive() {
        return ApiResponse.ok(gameService.rankSurvive());
    }

    @GetMapping("/rank/wealth")
    public ApiResponse<?> rankWealth() {
        return ApiResponse.ok(gameService.rankWealth());
    }

    @PostMapping("/track")
    public ApiResponse<?> track(@RequestHeader(value = GameConstants.HEADER_USER_ID, required = false) String userId,
            @RequestBody Map<String, Object> body) {
        String eventName = String.valueOf(body.get("event"));
        String gameId = body.get("gameId") == null ? null : String.valueOf(body.get("gameId"));
        @SuppressWarnings("unchecked")
        Map<String, Object> payload = body.get("payload") instanceof Map ? (Map<String, Object>) body.get("payload")
                : body;
        gameService.track(userId, gameId, eventName, payload);
        return ApiResponse.ok(Boolean.TRUE);
    }

    @ExceptionHandler({ IllegalArgumentException.class, IllegalStateException.class })
    public ResponseEntity<ApiResponse<?>> onBiz(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.fail(ex.getMessage()));
    }

    private String requireUser(String header, Map<String, String> body) {
        if (StringUtils.hasText(header)) {
            return header;
        }
        String fromBody = body.get("userId");
        if (!StringUtils.hasText(fromBody)) {
            throw new IllegalArgumentException("缺少用户标识");
        }
        return fromBody;
    }
}
