package com.dagong.survive.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import com.dagong.survive.config.GameProperties;
import com.dagong.survive.domain.ChatLine;

@Component
public class QwenClient {

    private static final String[] FALLBACK = new String[] {
            "在工位上还想着我？先把眼前这摊干完。",
            "收到。你要是又在摸鱼，我可看不出来。",
            "嗯，我在。今晚还加班吗？",
            "你发消息倒是快。下班了记得吃饭。"
    };

    private final GameProperties props;
    private final RestTemplate restTemplate;

    public QwenClient(GameProperties props, RestTemplate restTemplate) {
        this.props = props;
        this.restTemplate = restTemplate;
    }

    public String match(String careerName) {
        return ask("系统：他是一个" + safeCareer(careerName) + "，刚在相亲页滑到你。请你先发一条微信打招呼。像真人，短一点。",
                new ArrayList<ChatLine>(), careerName);
    }

    public String reply(String careerName, List<ChatLine> history, String userText) {
        return ask(userText, history, careerName);
    }

    private String ask(String userText, List<ChatLine> history, String careerName) {
        if (!StringUtils.hasText(props.getQwenApiKey())) {
            return fallback(userText);
        }
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(props.getQwenApiKey());
            Map<String, Object> body = new HashMap<String, Object>();
            body.put("model", props.getQwenModel());
            List<Map<String, String>> messages = new ArrayList<Map<String, String>>();
            messages.add(msg("system", systemPrompt(careerName)));
            int start = Math.max(0, history.size() - 8);
            for (int i = start; i < history.size(); i++) {
                ChatLine line = history.get(i);
                String role = "her".equals(line.getRole()) ? "assistant" : "user";
                messages.add(msg(role, line.getText()));
            }
            messages.add(msg("user", userText));
            body.put("messages", messages);
            ResponseEntity<Map> res = restTemplate.postForEntity(props.getQwenUrl(),
                    new HttpEntity<Map<String, Object>>(body, headers), Map.class);
            String text = parse(res.getBody());
            if (StringUtils.hasText(text)) {
                return text.trim();
            }
        } catch (Exception ignored) {
            return fallback(userText);
        }
        return fallback(userText);
    }

    private String systemPrompt(String careerName) {
        return "你是玩家正在交往或刚匹配到的女朋友，在游戏《打工人：活下去》里。他是" + safeCareer(careerName)
                + "，常在工位偷发微信。用中文微信口吻，2到4句，口语，可以吃醋、关心加班、吐槽生活。不要提自己是AI，不要剧透数值。";
    }

    private String safeCareer(String careerName) {
        return StringUtils.hasText(careerName) ? careerName : "打工人";
    }

    @SuppressWarnings("unchecked")
    private String parse(Map<?, ?> body) {
        if (body == null) {
            return null;
        }
        Object choices = body.get("choices");
        if (!(choices instanceof List) || ((List<?>) choices).isEmpty()) {
            return null;
        }
        Object first = ((List<?>) choices).get(0);
        if (!(first instanceof Map)) {
            return null;
        }
        Object message = ((Map<?, ?>) first).get("message");
        if (!(message instanceof Map)) {
            return null;
        }
        Object content = ((Map<?, ?>) message).get("content");
        return content == null ? null : String.valueOf(content);
    }

    private Map<String, String> msg(String role, String content) {
        Map<String, String> row = new HashMap<String, String>();
        row.put("role", role);
        row.put("content", content);
        return row;
    }

    private String fallback(String userText) {
        int idx = Math.abs((userText == null ? "hi" : userText).hashCode()) % FALLBACK.length;
        return FALLBACK[idx];
    }
}
