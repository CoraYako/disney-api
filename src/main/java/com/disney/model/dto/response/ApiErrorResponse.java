package com.disney.model.dto.response;

import com.disney.model.HttpCodeResponse;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;


public record ApiErrorResponse(LocalDateTime timestamp, String message, String path, HttpCodeResponse errorCode) {
    public static ApiErrorResponseBuilder builder() {
        return new ApiErrorResponseBuilder();
    }

    public static class ApiErrorResponseBuilder {
        private LocalDateTime timestamp;
        private String message;
        private String path;
        private HttpCodeResponse errorCode;

        public ApiErrorResponseBuilder timestamp(@NotNull LocalDateTime timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public ApiErrorResponseBuilder message(@NotNull String message) {
            this.message = message;
            return this;
        }

        public ApiErrorResponseBuilder path(@NotNull String path) {
            this.path = path;
            return this;
        }

        public ApiErrorResponseBuilder errorCode(@NotNull HttpCodeResponse errorCode) {
            this.errorCode = errorCode;
            return this;
        }

        public ApiErrorResponse build() {
            return new ApiErrorResponse(timestamp, message, path, errorCode);
        }
    }
}
