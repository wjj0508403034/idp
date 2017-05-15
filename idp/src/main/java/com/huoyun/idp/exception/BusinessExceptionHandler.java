package com.huoyun.idp.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.huoyun.idp.common.ErrorCode;
import com.huoyun.idp.locale.LocaleService;

@ControllerAdvice
public class BusinessExceptionHandler extends ResponseEntityExceptionHandler {
	private static final Logger LOGGER = LoggerFactory.getLogger(BusinessExceptionHandler.class);

	@Autowired
	private LocaleService localeService;

	@ExceptionHandler(Exception.class)
	public ResponseEntity<Object> UnexpectedError(Exception exception) {
		logger.error("Unkown Exception:", exception);
		return this.BusinessError(new BusinessException(ErrorCode.Unknown_Error));
	}

	@ExceptionHandler(BusinessException.class)
	public ResponseEntity<Object> BusinessError(BusinessException businessException) {
		businessException.setMessage(this.localeService.getErrorMessage(businessException.getCode()));
		LOGGER.error("Business Exception:", businessException);
		return new ResponseEntity<Object>(new BusinessExceptionResponse(businessException), HttpStatus.BAD_REQUEST);
	}
}
