package com.palettee.global.exception;

import com.palettee.global.logging.*;
import com.palettee.global.slack.SlackNotification;
import jakarta.servlet.http.*;
import lombok.*;
import lombok.extern.slf4j.*;
import org.springframework.http.*;
import org.springframework.validation.*;
import org.springframework.web.bind.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.*;
import org.springframework.web.servlet.mvc.method.annotation.*;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    // 예상했던 예외들 커스텀
    @ExceptionHandler(PaletteException.class)
    public ResponseEntity<ErrorResponse> paletteExceptionHandler(
            PaletteException e, HttpServletRequest request
    ) {
        ErrorCode code = e.getErrorCode();
        ErrorResponse errorResponse =
                new ErrorResponse(
                        code.getStatus(),
                        code.getReason());

        this.logError(request, code.getStatus(), code.getReason(), e);

        return ResponseEntity.status(HttpStatus.valueOf(code.getStatus())).body(errorResponse);
    }

    // 예상치 못했던 예외들은 500에러
    @SlackNotification
    @ExceptionHandler(Exception.class)
    protected ResponseEntity<ErrorResponse> handleException(
            Exception e, HttpServletRequest request
    ) {
        log.error("INTERNAL_SERVER_ERROR", e);
        ErrorCode internalServerError = ErrorCode.INTERNAL_SERVER_ERROR;

        ErrorResponse errorResponse =
                new ErrorResponse(
                        internalServerError.getStatus(),
                        internalServerError.getReason());

        this.logError(request, internalServerError.getStatus(), internalServerError.getReason(), e);

        return ResponseEntity.status(HttpStatus.valueOf(internalServerError.getStatus()))
                .body(errorResponse);
    }

    // @Valid 로 잡힌 예외들 커스텀
    @SneakyThrows
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {

        // TODO : 지금 validation 에러가 응답으로 보이긴 하는데 이상함.
        // 동일한 invalid body 여도 getDefaultMessage 했을 때 오는 메시지가 랜덤함. 어케 고치지???
        // invalid 한 필드까지는 고정적인 것 같은데 어떤 메시지가 선택되는지가 매번 다름..
        BindingResult bindingResult = ex.getBindingResult();

        if (!bindingResult.hasErrors()) {
            log.error(
                    "Expected request body validation has error but it wasn't. "
                            + "Unexpected validation detected.");
        }

        String message = bindingResult.getAllErrors().get(0).getDefaultMessage();
        ErrorResponse errorResponse = new ErrorResponse(status.value(), message);

        HttpServletRequest httpReq = ((ServletWebRequest) request).getRequest();

        this.logError(httpReq, status.value(), message, ex);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * @see MDCLoggingFilter
     */
    private void logError(HttpServletRequest req, int status, String msg, Exception e) {

        if (req == null) {
            log.error("Exception has been occurred : {}, {}", status, msg, e);
            return;
        }

        Object requestUUID = req.getAttribute("custom-request-uuid");
        String httpMethod = req.getMethod();
        String requestUrl = req.getRequestURL().toString();

        log.error("Exception has been occurred on REQUEST ID [{}] - [{} \"{}\"] : {}, {}",
                requestUUID != null ? requestUUID : "UNKNOWN",
                httpMethod, requestUrl,
                status, msg, e);
    }
}
