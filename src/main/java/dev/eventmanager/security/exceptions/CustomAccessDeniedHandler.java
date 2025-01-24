package dev.eventmanager.security.exceptions;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.eventmanager.web.ErrorMessage;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;

@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    private final Logger log = LoggerFactory.getLogger(CustomAccessDeniedHandler.class);

    private final ObjectMapper objectMapper;

    public CustomAccessDeniedHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            AccessDeniedException accessDeniedException) throws IOException, ServletException {
        log.error("Handling access denied exception", accessDeniedException);
        ErrorMessage errorMessage = new ErrorMessage(
                "Forbidden",
                accessDeniedException.getMessage(),
                LocalDateTime.now()
        );

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.getWriter().write(objectMapper.writeValueAsString(errorMessage));
    }
}
