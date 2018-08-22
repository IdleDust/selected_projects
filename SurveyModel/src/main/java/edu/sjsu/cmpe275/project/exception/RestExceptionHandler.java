package edu.sjsu.cmpe275.project.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class RestExceptionHandler extends ResponseEntityExceptionHandler {
	
	 @ExceptionHandler(CustomRestExceptionHandler.class)
	 public ResponseEntity<Object> handleRestException(CustomRestExceptionHandler ex) {
		 ExceptionJSONInfo info = ex.getInfo();
		 return new ResponseEntity<Object>(info, ex.getStatus());
	   }
}