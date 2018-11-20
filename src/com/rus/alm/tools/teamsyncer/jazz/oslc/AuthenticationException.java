package com.rus.alm.tools.teamsyncer.jazz.oslc;

@SuppressWarnings("serial")
public class AuthenticationException extends RuntimeException
{
	public AuthenticationException()
	{
	}

	public AuthenticationException(String message)
	{
		super(message);
	}

	public AuthenticationException(String message, RuntimeException inner)
	{
		super(message, inner);
	}
}