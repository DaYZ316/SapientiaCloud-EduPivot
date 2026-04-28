package com.dayz.sapientiacloud_edupivot.celestial_hub.clients;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.dayz.sapientiacloud_edupivot.celestial_hub.service.agent.config.AgentSkillProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * Minimal OpenTDB client used for pulling external trivia samples into the paper workflow.
 * Official API reference: https://opentdb.com/api_config.php
 */
@Slf4j
@Component
public class OpenTdbClient {

    private static final int MAX_API_AMOUNT = 50;
    private static final String DEFAULT_ENCODE = "url3986";

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final String baseUrl;
    private final String apiPath;
    private final Duration requestTimeout;

    public OpenTdbClient(AgentSkillProperties agentSkillProperties,
                         ObjectMapper objectMapper) {
        AgentSkillProperties.OpenTdb openTdb = agentSkillProperties != null
                ? agentSkillProperties.getOpenTdb()
                : new AgentSkillProperties.OpenTdb();
        long connectTimeoutSeconds = openTdb != null ? openTdb.getConnectTimeoutSeconds() : 5L;
        long requestTimeoutSeconds = openTdb != null ? openTdb.getRequestTimeoutSeconds() : 12L;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(Math.max(1L, connectTimeoutSeconds)))
                .version(HttpClient.Version.HTTP_1_1)
                .build();
        this.objectMapper = objectMapper;
        this.baseUrl = removeTrailingSlash(openTdb != null ? openTdb.getBaseUrl() : null);
        this.apiPath = normalizePath(openTdb != null ? openTdb.getApiPath() : null);
        this.requestTimeout = Duration.ofSeconds(Math.max(1L, requestTimeoutSeconds));
    }

    public List<TriviaQuestion> fetchQuestions(OpenTdbQuery query) {
        OpenTdbQuery normalizedQuery = query != null ? query.normalize() : new OpenTdbQuery(5, null, null, null, DEFAULT_ENCODE);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(buildUri(normalizedQuery))
                .timeout(requestTimeout)
                .header("Accept", "application/json")
                .GET()
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new IllegalStateException("OpenTDB request failed with status " + response.statusCode());
            }

            OpenTdbApiResponse payload = objectMapper.readValue(response.body(), OpenTdbApiResponse.class);
            if (payload == null || payload.response_code() != 0 || CollectionUtils.isEmpty(payload.results())) {
                log.debug("OpenTDB returned no usable results. responseCode={}, amount={}, category={}, difficulty={}, type={}",
                        payload != null ? payload.response_code() : null,
                        normalizedQuery.amount(),
                        normalizedQuery.category(),
                        normalizedQuery.difficulty(),
                        normalizedQuery.type());
                return List.of();
            }

            List<TriviaQuestion> questions = new ArrayList<>();
            for (OpenTdbApiQuestion item : payload.results()) {
                TriviaQuestion question = toTriviaQuestion(item);
                if (question != null) {
                    questions.add(question);
                }
            }
            return questions;
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to deserialize OpenTDB response", ex);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("OpenTDB request was interrupted", ex);
        }
    }

    private URI buildUri(OpenTdbQuery query) {
        List<String> params = new ArrayList<>();
        appendParam(params, "amount", String.valueOf(query.amount()));
        appendParam(params, "category", query.category() != null ? String.valueOf(query.category()) : null);
        appendParam(params, "difficulty", query.difficulty());
        appendParam(params, "type", query.type());
        appendParam(params, "encode", query.encode());
        return URI.create(baseUrl + apiPath + '?' + String.join("&", params));
    }

    private void appendParam(List<String> params, String key, String value) {
        if (!StringUtils.hasText(key) || !StringUtils.hasText(value)) {
            return;
        }
        params.add(URLEncoder.encode(key, StandardCharsets.UTF_8)
                + "="
                + URLEncoder.encode(value, StandardCharsets.UTF_8));
    }

    private TriviaQuestion toTriviaQuestion(OpenTdbApiQuestion item) {
        if (item == null || !StringUtils.hasText(item.question())) {
            return null;
        }

        List<String> incorrectAnswers = new ArrayList<>();
        if (!CollectionUtils.isEmpty(item.incorrect_answers())) {
            for (String answer : item.incorrect_answers()) {
                if (StringUtils.hasText(answer)) {
                    incorrectAnswers.add(decode(answer));
                }
            }
        }

        return new TriviaQuestion(
                decode(item.category()),
                decode(item.type()),
                decode(item.difficulty()),
                decode(item.question()),
                decode(item.correct_answer()),
                incorrectAnswers
        );
    }

    private String decode(String value) {
        if (!StringUtils.hasText(value)) {
            return value;
        }
        return URLDecoder.decode(value, StandardCharsets.UTF_8);
    }

    private String removeTrailingSlash(String value) {
        if (!StringUtils.hasText(value)) {
            return "";
        }
        return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }

    private String normalizePath(String value) {
        if (!StringUtils.hasText(value)) {
            return "/api.php";
        }
        return value.startsWith("/") ? value : "/" + value;
    }

    public record OpenTdbQuery(Integer amount,
                               Integer category,
                               String difficulty,
                               String type,
                               String encode) {

        public OpenTdbQuery normalize() {
            int normalizedAmount = amount != null ? amount : 5;
            normalizedAmount = Math.max(1, Math.min(MAX_API_AMOUNT, normalizedAmount));
            return new OpenTdbQuery(
                    normalizedAmount,
                    category,
                    StringUtils.hasText(difficulty) ? difficulty.trim().toLowerCase() : null,
                    StringUtils.hasText(type) ? type.trim().toLowerCase() : null,
                    StringUtils.hasText(encode) ? encode.trim() : DEFAULT_ENCODE
            );
        }
    }

    public record TriviaQuestion(String category,
                                 String type,
                                 String difficulty,
                                 String question,
                                 String correctAnswer,
                                 List<String> incorrectAnswers) {
    }

    private record OpenTdbApiResponse(int response_code, List<OpenTdbApiQuestion> results) {
    }

    private record OpenTdbApiQuestion(String category,
                                      String type,
                                      String difficulty,
                                      String question,
                                      String correct_answer,
                                      List<String> incorrect_answers) {
    }
}
