package com.rus.alm.tools.teamsyncer;

/** 
 Parsed program command line options
*/
public class Options
{
	private boolean _opt_rm = false;
	public final boolean getopt_rm()
	{
		return _opt_rm;
	}
	public final void setopt_rm(boolean value)
	{
		_opt_rm = value;
	}
	private boolean _opt_verbose = false;
	public final boolean getopt_verbose()
	{
		return _opt_verbose;
	}
	public final void setopt_verbose(boolean value)
	{
		_opt_verbose = value;
	}
	private boolean _opt_check_map = false;
	public final boolean getopt_check_map()
	{
		return _opt_check_map;
	}
	public final void setopt_check_map(boolean value)
	{
		_opt_check_map = value;
	}
	private boolean _opt_log = false;
	public final boolean getopt_log()
	{
		return _opt_log;
	}
	public final void setopt_log(boolean value)
	{
		_opt_log = value;
	}
	private String jazzRepository;
	public final String getjazzRepository()
	{
		return jazzRepository;
	}
	public final void setjazzRepository(String value)
	{
		jazzRepository = value;
	}
	private String user;
	public final String getuser()
	{
		return user;
	}
	public final void setuser(String value)
	{
		user = value;
	}
	private String passwd;
	public final String getpasswd()
	{
		return passwd;
	}
	public final void setpasswd(String value)
	{
		passwd = value;
	}
	private String logdir;
	public final String getlogdir()
	{
		return logdir;
	}
	public final void setlogdir(String value)
	{
		logdir = value;
	}
	//public List<string> roles { get; set; }
	private String mappingfile;
	public final String getmappingfile()
	{
		return mappingfile;
	}
	public final void setmappingfile(String value)
	{
		mappingfile = value;
	}

	public final boolean AllMandatoryOptionsPresent()
	{
		// He 2018-09-18: jazz repository as command line parameter no longer mandatory
		//if (!_opt_check_map)
		//{
		//	  if (getjazzRepository() == null)
		//	  {
		//		  return false;
		//	  }
		//}
		//if (user == null)
		//    return false;
		//if (passwd == null)
		//    return false;
		if (getmappingfile() == null)
		{
			return false;
		}
		return true;
	}
}