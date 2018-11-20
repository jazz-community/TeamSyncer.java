package com.rus.alm.tools.teamsyncer;

@SuppressWarnings("serial")
public class ArgumentsParseException extends RuntimeException
{
	public ArgumentsParseException()
	{
	}

	public ArgumentsParseException(String message)
	{
		super(message);
	}

	public ArgumentsParseException(String message, RuntimeException inner)
	{
		super(message, inner);
	}
}