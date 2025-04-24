package org.c4marathon.assignment.common.exception;

import org.c4marathon.assignment.common.response.ApiResponse;

import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.OptimisticLockException;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(EntityNotFoundException.class)
	public ResponseEntity<ApiResponse<Void>> handleEntityNotFoundException(EntityNotFoundException ex) {
		log.error("자원 미존재 예외 발생: {}", ex.getMessage(), ex);
		return ResponseEntity.status(HttpStatus.NOT_FOUND)
			.body(ApiResponse.res(HttpStatus.NOT_FOUND.value(), ex.getMessage()));
	}

	@ExceptionHandler(IllegalStateException.class)
	public ResponseEntity<ApiResponse<Void>> handleIllegalStateException(IllegalStateException ex) {
		log.warn("예외 발생: {}", ex.getMessage(), ex);
		return ResponseEntity.status(HttpStatus.BAD_REQUEST)
			.body(ApiResponse.res(HttpStatus.BAD_REQUEST.value(), ex.getMessage()));
	}

	@ExceptionHandler(ResponseStatusException.class)
	public ResponseEntity<ApiResponse<Void>> handleResponseStatusException(ResponseStatusException ex) {
		return ResponseEntity.status(ex.getStatusCode())
			.body(ApiResponse.res(ex.getStatusCode().value(), ex.getReason()));
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiResponse<Void>> handleGenericException(Exception ex) {
		log.error("예외 발생: {}", ex.getMessage(), ex);
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.res(500, ex.getMessage()));
	}

	@ExceptionHandler(OptimisticLockException.class)
	public ResponseEntity<ApiResponse<Void>> handleOptimisticLockException(OptimisticLockException ex) {
		log.error("동시성 충돌 예외 발생: {}", ex.getMessage(), ex);
		return ResponseEntity.status(HttpStatus.CONFLICT)
			.body(ApiResponse.res(409, "현재 다른 요청에 의해 처리 중입니다. 잠시 후 다시 시도해주세요."));
	}
}
