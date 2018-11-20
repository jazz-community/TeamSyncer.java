package com.rus.alm.tools.teamsyncer;

import java.util.*;

public class ArgumentsHandling
{
	private static HashMap<String, ArrayList<String>> CL_dict;
	private static ArrayList<String> ErrorList;

	private static void enqueueStrayParamsToErrList(String optionName, int maxValues)
	{
		if (CL_dict.get(optionName) != null)
		{
			if (CL_dict.get(optionName).size() > maxValues)
			{
				for (int i = maxValues; i < CL_dict.get(optionName).size(); i++)
				{
					ErrorList.add("Stray argument: " + CL_dict.get(optionName).get(i));
				}
			}
		}
	}

	private static boolean BoolOption(String optionName, boolean isMandatory)
	{
		if (CL_dict.containsKey(optionName))
		{
			enqueueStrayParamsToErrList(optionName, 0);
			CL_dict.remove(optionName);
			return true;
		}
		else if (isMandatory)
		{
			ErrorList.add("Missing option: " + optionName);
		}
		return false;
	}

	private static String SingleStringParameter(String parName, boolean isMandatory)
	{
		String ret = null;
		if (CL_dict.containsKey(parName))
		{
			if (CL_dict.get(parName) == null)
			{
				ErrorList.add("Missing value for parameter: " + parName);
			}
			else
			{
				ret = CL_dict.get(parName).get(0);
				enqueueStrayParamsToErrList(parName, 1);
			}
			CL_dict.remove(parName);
		}
		else if (isMandatory)
		{
			ErrorList.add("Missing parameter: " + parName);
		}
		return ret;
	}

	@SuppressWarnings("unused")
	private static ArrayList<String> ListStringParameter(String parName, boolean isMandatory)
	{
		ArrayList<String> ret = null;
		if (CL_dict.containsKey(parName))
		{
			if (CL_dict.get(parName) == null)
			{
				ErrorList.add("Missing value(s) for parameter: " + parName);
			}
			else
			{
				ret = new ArrayList<String>();
				ret.addAll(CL_dict.get(parName));
			}
			CL_dict.remove(parName);
		}
		else if (isMandatory)
		{
			ErrorList.add("Missing parameter: " + parName);
		}
		return ret;
	}

	public static Options getOptionsFromArguments(String[] args)
	{
		if (args.length == 0)
		{
			throw new NoArgumentsException();
		}

		Options o = new Options();

		CL_dict = CommandLineParser.ParseCommandLine(args, true, true);
		ErrorList = new ArrayList<String>();

		o.setopt_check_map(BoolOption("chkmap", false));
		// He 2018-09-18: jazz repository as command line parameter no longer mandatory
		//o.setjazzRepository(SingleStringParameter("url", !o.getopt_check_map()));
		o.setjazzRepository(SingleStringParameter("url", false));
		o.setuser(SingleStringParameter("usr", false));
		o.setpasswd(SingleStringParameter("pwd", false));
		o.setopt_rm(BoolOption("rm", false));
		o.setopt_verbose(BoolOption("v", false));
		o.setmappingfile(SingleStringParameter("map", true));
		o.setlogdir(SingleStringParameter("log", false));

		if (CL_dict.containsKey(""))
		{
			enqueueStrayParamsToErrList("", 0);
			CL_dict.remove("");
		}

		// Complain about parsing errors
		if ((!CL_dict.isEmpty()) || (!ErrorList.isEmpty()))
		{
			String m = "";
			int i = 0;
			for (String err : ErrorList)
			{
				if (i > 0)
				{
					m += "\n";
				}
				m += err;
				i++;
			}
			for (String opt : CL_dict.keySet())
			{
				if (i > 0)
				{
					m += "\n";
				}
				m += "Unknown option: " + opt;
				i++;
			}
			throw new ArgumentsParseException(m);
		}

		if (o.getjazzRepository() != null)
		{
			int limit = -1;
			if (o.getjazzRepository().length() > 9)
			{
				limit = o.getjazzRepository().indexOf('/', 10);
			}
			if (limit > 0)
			{
				o.setjazzRepository(o.getjazzRepository().substring(0, limit));
			}
		}
		if (o.getlogdir() != null)
		{
			o.setopt_log(true);
		}

		return o;
	}

	public static String getUsageMessage(String applicationName)
	{
		// He 2018-09-18: --url in brackets as jazz repository as command line parameter no longer mandatory
		return "Usage: " + applicationName + " [--url <jazzserver url>] [--usr <username>] [--pwd <password>]\n" +
			   StringHelper.padLeft("", applicationName.length() + 7) + " --map <syncer mapping filename>  [-chkmap] [-rm] [-v]\n" +
			   StringHelper.padLeft("", applicationName.length() + 7) + " [--log <directory name for log files>]\n\n" +
			   StringHelper.padLeft("", applicationName.length() + 7) + " --log  : enable logging + write log files to specified directory\n" +
			   StringHelper.padLeft("", applicationName.length() + 7) + " -chkmap: check mapping file only (note: if present, connection options don't matter)\n" +
			   StringHelper.padLeft("", applicationName.length() + 7) + " -rm    : connect to 'rm' repository (instead of 'ccm')\n" +
			   StringHelper.padLeft("", applicationName.length() + 7) + " -v     : verbose, show some infos while syncing groups";
	}
}