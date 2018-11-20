package com.rus.alm.tools.teamsyncer;

@SuppressWarnings("serial")
public class NoArgumentsException extends RuntimeException
{
	public NoArgumentsException()
	{
	}

	public NoArgumentsException(String message)
	{
		super(message);
	}

	public NoArgumentsException(String message, RuntimeException inner)
	{
		super(message, inner);
	}
}