package com.artezio.arttime.web.exception_handler;

public class ExceptionHandlerFactory extends javax.faces.context.ExceptionHandlerFactory {
	
	private javax.faces.context.ExceptionHandlerFactory parent;
	 
	public ExceptionHandlerFactory(javax.faces.context.ExceptionHandlerFactory parent) {
	    this.parent = parent;
	}
	 
	@Override
	public javax.faces.context.ExceptionHandler getExceptionHandler() {	 
		return new ExceptionHandler(parent.getExceptionHandler());	
	}
}
