package com.artezio.arttime.exceptions;

import javax.ejb.ApplicationException;

@ApplicationException(rollback = true)
public class ActualTimeRemovalException  extends Exception{

	private static final long serialVersionUID = -3549572436627243399L;
	
	public ActualTimeRemovalException(String message) {
		super(message);
	}
}
