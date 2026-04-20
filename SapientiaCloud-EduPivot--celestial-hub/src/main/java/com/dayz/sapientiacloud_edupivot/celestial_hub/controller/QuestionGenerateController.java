package com.dayz.sapientiacloud_edupivot.celestial_hub.controller;

import com.alibaba.fastjson2.JSON;
import com.dayz.sapientiacloud_edupivot.celestial_hub.common.controller.BaseController;
import com.dayz.sapientiacloud_edupivot.celestial_hub.common.enums.ResultEnum;
import com.dayz.sapientiacloud_edupivot.celestial_hub.common.exception.BusinessException;
import com.dayz.sapientiacloud_edupivot.celestial_hub.common.result.Result;
import com.dayz.sapientiacloud_edupivot.celestial_hub.common.security.annotation.HasPermission;
import com.dayz.sapientiacloud_edupivot.celestial_hub.common.security.utils.UserContextUtil;
import com.dayz.sapientiacloud_edupivot.celestial_hub.constant.PermissionConstants;
import com.dayz.sapientiacloud_edupivot.celestial_hub.constant.QuestionConstants;
import com.dayz.sapientiacloud_edupivot.celestial_hub.entity.dto.QuestionGenerateRequestDTO;
import com.dayz.sapientiacloud_edupivot.celestial_hub.entity.dto.QuestionPaperExportRequestDTO;
import com.dayz.sapientiacloud_edupivot.celestial_hub.entity.dto.QuestionResponseDTO;
import com.dayz.sapientiacloud_edupivot.celestial_hub.entity.vo.ChatSessionVO;
import com.dayz.sapientiacloud_edupivot.celestial_hub.enums.SessionTypeEnum;
import com.dayz.sapientiacloud_edupivot.celestial_hub.repository.ChatMessageRepository;
import com.dayz.sapientiacloud_edupivot.celestial_hub.service.IChatSessionService;
import com.dayz.sapientiacloud_edupivot.celestial_hub.service.KafkaQuestionService;
import com.dayz.sapientiacloud_edupivot.celestial_hub.service.QuestionPaperExportService;
import com.dayz.sapientiacloud_edupivot.celestial_hub.utils.ChatMessageUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.List;
import java.util.UUID;
import java.nio.charset.StandardCharsets;

@Slf4j
@Tag(name = "AI Question Generation")
@RestController
@RequestMapping("/question")
@RequiredArgsConstructor
public class QuestionGenerateController extends BaseController {

    private static final String GENERATION_FAILED_MESSAGE =
            "Question generation failed. Please try again later.";

    private final KafkaQuestionService kafkaQuestionService;
    private final QuestionPaperExportService questionPaperExportService;
    private final ChatMessageRepository chatMessageRepository;
    private final IChatSessionService chatSessionService;

    @Deprecated(forRemoval = true)
    @Operation(
            summary = "generateQuestions",
            description = "Deprecated. Use /question/stream instead for AI question generation.",
            deprecated = true
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Question generation completed"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @HasPermission(
            summary = "generateQuestions",
            description = "Submit an AI question-generation request",
            permission = PermissionConstants.CELESTIAL_ADD
    )
    @PostMapping("/generate")
    public Result<List<QuestionResponseDTO>> generateQuestions(
            @Valid @RequestBody QuestionGenerateRequestDTO request) {
        log.warn("Deprecated question-generation endpoint invoked: POST /question/generate. Use POST /question/stream instead.");
        QuestionGenerationExecutionContext context = prepareExecutionContext(request);
        List<QuestionResponseDTO> questions = executeQuestionGeneration(request, context);
        return Result.success(questions == null ? List.of() : questions);
    }

    @Operation(summary = "generateQuestionsStream", description = "Submit an AI question-generation request and receive status updates over SSE.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "SSE stream established"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @HasPermission(
            summary = "generateQuestionsStream",
            description = "Stream AI question-generation status over SSE",
            permission = PermissionConstants.CELESTIAL_ADD
    )
    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public ResponseEntity<Flux<String>> generateQuestionsStream(
            @Valid @RequestBody QuestionGenerateRequestDTO request) {
        QuestionGenerationExecutionContext context = prepareExecutionContext(request);

        Mono<String> terminalEvent = Mono.fromCallable(() -> executeQuestionGeneration(request, context))
                .subscribeOn(Schedulers.boundedElastic())
                .map(questions -> {
                    if (questions == null || questions.isEmpty()) {
                        return buildStreamEvent(new QuestionGenerateStreamEvent(
                                context.requestId(),
                                context.sessionId(),
                                "error",
                                0,
                                GENERATION_FAILED_MESSAGE
                        ));
                    }
                    return buildStreamEvent(new QuestionGenerateStreamEvent(
                            context.requestId(),
                            context.sessionId(),
                            "completed",
                            questions.size(),
                            null
                    ));
                })
                .onErrorResume(error -> {
                    log.error("Question generation stream failed. requestId={}, sessionId={}",
                            context.requestId(), context.sessionId(), error);
                    return Mono.just(buildStreamEvent(new QuestionGenerateStreamEvent(
                            context.requestId(),
                            context.sessionId(),
                            "error",
                            null,
                            error.getMessage()
                    )));
                })
                .cache();

        Flux<String> heartbeats = Flux.interval(Duration.ofSeconds(15))
                .map(sequence -> buildStreamEvent(new QuestionGenerateStreamEvent(
                        context.requestId(),
                        context.sessionId(),
                        "processing",
                        null,
                        null
                )))
                .takeUntilOther(terminalEvent);

        Flux<String> streamBody = Flux.concat(
                Flux.just(buildStreamEvent(new QuestionGenerateStreamEvent(
                        context.requestId(),
                        context.sessionId(),
                        "submitted",
                        null,
                        null
                ))),
                Flux.merge(heartbeats, terminalEvent)
        );

        return buildSseResponse(streamBody);
    }

    @Deprecated(forRemoval = true)
    @Operation(
            summary = "checkKafkaRequest",
            description = "Deprecated. Use /question/stream instead of polling request status.",
            deprecated = true
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Query successful"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @HasPermission(
            summary = "checkKafkaRequest",
            description = "Check whether a Kafka question-generation request has completed",
            permission = PermissionConstants.CELESTIAL_QUERY
    )
    @GetMapping("/status/{requestId}")
    public Result<Boolean> checkKafkaRequest(
            @Parameter(name = "requestId", description = "Question-generation request ID", required = true)
            @PathVariable("requestId") String requestId) {
        log.warn("Deprecated question-generation polling endpoint invoked: GET /question/status/{}. Use POST /question/stream instead.", requestId);
        boolean isCompleted = kafkaQuestionService.checkRequestStatus(requestId);
        return Result.success(isCompleted);
    }

    @Operation(summary = "exportPaperPdf", description = "Export AI generated full paper as PDF.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Paper PDF exported"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @HasPermission(
            summary = "exportPaperPdf",
            description = "Export AI generated full paper as PDF",
            permission = PermissionConstants.CELESTIAL_QUERY
    )
    @PostMapping(value = "/paper/export/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> exportPaperPdf(@Valid @RequestBody QuestionPaperExportRequestDTO request) {
        try {
            QuestionPaperExportService.ExportedPaperFile exportedFile = questionPaperExportService.exportPdf(request);
            return buildFileResponse(exportedFile);
        } catch (BusinessException e) {
            log.warn("Paper PDF export failed. message={}", e.getMessage(), e);
            return buildFileErrorResponse(resolveExportErrorStatus(e), e.getMessage());
        } catch (Exception e) {
            log.error("Paper PDF export failed unexpectedly.", e);
            return buildFileErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, ResultEnum.FAIL.getMessage());
        }
    }

    @Operation(summary = "exportPaperWord", description = "Export AI generated full paper as Word.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Paper Word exported"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @HasPermission(
            summary = "exportPaperWord",
            description = "Export AI generated full paper as Word",
            permission = PermissionConstants.CELESTIAL_QUERY
    )
    @PostMapping(
            value = "/paper/export/word",
            produces = "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
    )
    public ResponseEntity<byte[]> exportPaperWord(@Valid @RequestBody QuestionPaperExportRequestDTO request) {
        try {
            QuestionPaperExportService.ExportedPaperFile exportedFile = questionPaperExportService.exportWord(request);
            return buildFileResponse(exportedFile);
        } catch (BusinessException e) {
            log.warn("Paper Word export failed. message={}", e.getMessage(), e);
            return buildFileErrorResponse(resolveExportErrorStatus(e), e.getMessage());
        } catch (Exception e) {
            log.error("Paper Word export failed unexpectedly.", e);
            return buildFileErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, ResultEnum.FAIL.getMessage());
        }
    }

    private QuestionGenerationExecutionContext prepareExecutionContext(QuestionGenerateRequestDTO request) {
        UUID currentUserId = UserContextUtil.getCurrentUserId();
        UUID sessionId = getOrCreateSessionId(request, currentUserId);
        boolean needGenerateTitle = shouldGenerateSessionTitle(sessionId);
        String requestId = UUID.randomUUID().toString();
        return new QuestionGenerationExecutionContext(requestId, sessionId, needGenerateTitle, currentUserId);
    }

    private List<QuestionResponseDTO> executeQuestionGeneration(QuestionGenerateRequestDTO request,
                                                               QuestionGenerationExecutionContext context) {
        List<QuestionResponseDTO> questions;
        try {
            questions = kafkaQuestionService.generateQuestions(request, context.requestId(), context.userId());
        } catch (RuntimeException e) {
            saveGenerationFailureMessage(context.sessionId(), context.requestId());
            log.warn("Question generation request failed. requestId={}, sessionId={}, error={}",
                    context.requestId(), context.sessionId(), e.getMessage());
            throw e;
        }

        if (questions == null || questions.isEmpty()) {
            log.warn("Question generation request returned empty result. requestId={}, sessionId={}",
                    context.requestId(), context.sessionId());
            saveGenerationFailureMessage(context.sessionId(), context.requestId());
            return List.of();
        }

        triggerSessionTitleIfNecessary(context.sessionId(), context.needGenerateTitle());
        return questions;
    }

    private boolean shouldGenerateSessionTitle(UUID sessionId) {
        if (sessionId == null) {
            return false;
        }
        try {
            return chatMessageRepository.countBySessionId(sessionId) == 0;
        } catch (Exception e) {
            log.debug("Failed to inspect session messages before question generation. sessionId={}, error={}",
                    sessionId, e.getMessage());
            return false;
        }
    }

    private void saveGenerationFailureMessage(UUID sessionId, String requestId) {
        try {
            ChatMessageUtil.saveSystemMessage(sessionId, GENERATION_FAILED_MESSAGE, requestId, chatMessageRepository);
        } catch (Exception e) {
            log.debug("Failed to save question generation failure message. requestId={}", requestId, e);
        }
    }

    private void triggerSessionTitleIfNecessary(UUID sessionId, boolean needGenerateTitle) {
        if (sessionId == null || !needGenerateTitle) {
            return;
        }
        try {
            long messageCountAfter = chatMessageRepository.countBySessionId(sessionId);
            if (messageCountAfter <= 2) {
                chatSessionService.generateSessionTitleAsync(sessionId);
            }
        } catch (Exception e) {
            log.debug("Failed to generate session title after question generation. sessionId={}, error={}",
                    sessionId, e.getMessage());
        }
    }

    private String buildStreamEvent(QuestionGenerateStreamEvent event) {
        return JSON.toJSONString(event);
    }

    private ResponseEntity<Flux<String>> buildSseResponse(Flux<String> body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_EVENT_STREAM);
        headers.setCacheControl("no-cache, no-transform");
        headers.set("X-Accel-Buffering", "no");
        return ResponseEntity.ok()
                .headers(headers)
                .body(body);
    }

    private ResponseEntity<byte[]> buildFileResponse(QuestionPaperExportService.ExportedPaperFile exportedFile) {
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(exportedFile.contentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, buildContentDisposition(exportedFile.fileName()))
                .contentLength(exportedFile.content().length)
                .body(exportedFile.content());
    }

    private ResponseEntity<byte[]> buildFileErrorResponse(HttpStatus status, String message) {
        byte[] content = (message == null ? ResultEnum.FAIL.getMessage() : message).getBytes(StandardCharsets.UTF_8);
        return ResponseEntity.status(status)
                .contentType(new MediaType("text", "plain", StandardCharsets.UTF_8))
                .contentLength(content.length)
                .body(content);
    }

    private HttpStatus resolveExportErrorStatus(BusinessException e) {
        if (e == null) {
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }
        if (e.getCode() == ResultEnum.PARAM_ERROR.getCode()) {
            return HttpStatus.BAD_REQUEST;
        }
        if (e.getCode() == ResultEnum.FORBIDDEN.getCode()) {
            return HttpStatus.FORBIDDEN;
        }
        if (e.getCode() == ResultEnum.UNAUTHORIZED.getCode()) {
            return HttpStatus.UNAUTHORIZED;
        }
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }

    private String buildContentDisposition(String fileName) {
        String encoded = java.net.URLEncoder.encode(fileName, java.nio.charset.StandardCharsets.UTF_8)
                .replace("+", "%20");
        return "attachment; filename*=UTF-8''" + encoded;
    }

    private UUID getOrCreateSessionId(QuestionGenerateRequestDTO request, UUID currentUserId) {
        if (request.getSessionId() != null) {
            return request.getSessionId();
        }

        UUID userId = currentUserId != null ? currentUserId : UserContextUtil.getCurrentUserId();
        if (userId == null) {
            throw new RuntimeException("Current user ID is required to create a question-generation session.");
        }

        ChatSessionVO sessionVO = chatSessionService.addChatSession(
                userId,
                null,
                SessionTypeEnum.SMART_QUESTION.getCode(),
                QuestionConstants.SMART_QUESTION_SESSION_TITLE
        );
        UUID sessionId = sessionVO.getId();
        request.setSessionId(sessionId);
        return sessionId;
    }

    private record QuestionGenerationExecutionContext(String requestId,
                                                      UUID sessionId,
                                                      boolean needGenerateTitle,
                                                      UUID userId) {
    }

    private record QuestionGenerateStreamEvent(String requestId,
                                               UUID sessionId,
                                               String status,
                                               Integer questionCount,
                                               String message) {
    }
}
