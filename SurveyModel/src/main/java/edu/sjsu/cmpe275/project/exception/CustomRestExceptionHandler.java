package edu.sjsu.cmpe275.project.exception;

import org.springframework.http.HttpStatus;

public class CustomRestExceptionHandler extends RuntimeException {
	
	private static final long serialVersionUID = 1L;
	private ExceptionJSONInfo info;
	private HttpStatus status;
	
    public CustomRestExceptionHandler (HttpStatus status, String message) {
        super(message);
        this.status = status;
        info = new ExceptionJSONInfo();
        info.setCode(status.value());
        info.setMsg(message);
    }

	public ExceptionJSONInfo getInfo() {
		return info;
	}

	public void setInfo(ExceptionJSONInfo info) {
		this.info = info;
	}

	public HttpStatus getStatus() {
		return status;
	}

	public void setStatus(HttpStatus status) {
		this.status = status;
	}


}
