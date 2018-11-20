package com.rus.alm.tools.teamsyncer;

import java.util.*;

  public final class CommandLineParser
  {
	/** 
	 Transform the command line string array into a Dictionary.
	 
	 @param args The string array, usually string[] args from your main function
	 @return A Dictionary where the arguments are the keys and parameters to the arguments are in a List. Keep in mind that the List may be null!
	*/
	public static HashMap<String, ArrayList<String>> ParseCommandLine(String[] args)
	{
	  return ParseCommandLine(args, true, false);
	}

	/** 
	 Transform the command line string array into a Dictionary.
	 
	 @param args The string array, usually string[] args from your main function
	 @param ignoreArgumentCase Ignore the case of arguments? (if set to false, then "/beta" and "/Beta" are two different parameters
	 @return A Dictionary where the arguments are the keys and parameters to the arguments are in a List. Keep in mind that the List may be null!
	*/
	public static HashMap<String, ArrayList<String>> ParseCommandLine(String[] args, boolean ignoreArgumentCase)
	{
	  return ParseCommandLine(args, ignoreArgumentCase, false);
	}

	/** 
	 Transform the command line string array into a Dictionary.
	 
	 @param args The string array, usually string[] args from your main function
	 @param ignoreArgumentCase Ignore the case of arguments? (if set to false, then "/beta" and "/Beta" are two different arguments)
	 @param allowMultipleParameters Allow multiple parameters to one argument.
	 @return A Dictionary where the arguments are the keys and parameters to the arguments are in a List. Keep in mind that the List may be null!
	 
	 If allowMultipleParameters is set to true, then "/delta omega kappa" will cause omega and kappa to be two parameters to the argument delta.
	 If allowMultipleParameters is set to false, then omega will be a parameter to delta, but kappa will be assigned to string.Empty.
	 
	*/
	public static HashMap<String, ArrayList<String>> ParseCommandLine(String[] args, boolean ignoreArgumentCase, boolean allowMultipleParameters)
	{
	  HashMap<String, ArrayList<String>> result = new HashMap<String, ArrayList<String>>();
	  String currentArgument = "";

	  for (int i = 0; i < args.length; i++)
	  {
		// Is this an argument?
		if ((args[i].startsWith("-") || args[i].startsWith("/")) && args[i].length() > 1)
		{
		  currentArgument = StringHelper.remove(args[i], 0,1);
		  if (currentArgument.startsWith("-"))
		  {
			  currentArgument = StringHelper.remove(currentArgument, 0,1);
		  }
		  if (ignoreArgumentCase)
		  {
			currentArgument = currentArgument.toLowerCase(Locale.ROOT);
		  }
		  if (!result.containsKey(currentArgument))
		  {
			result.put(currentArgument, null);
		  }
		}
		else // No, it's a parameter
		{
		  ArrayList<String> paramValues = null;
		  if (result.containsKey(currentArgument))
		  {
			paramValues = result.get(currentArgument);
		  }
		  if (paramValues == null)
		  {
			paramValues = new ArrayList<String>();
		  }
		  paramValues.add(args[i]);
		  result.put(currentArgument, paramValues);
		  if (!allowMultipleParameters)
		  {
			currentArgument = "";
		  }
		}
	  }
	  return result;
	}
  }