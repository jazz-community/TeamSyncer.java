package com.rus.alm.tools.teamsyncer;

public class Dependency
{
	private int _map_num, _depends_on;

	public final int getMapNum()
	{
		return _map_num;
	}
	public final int getDepNum()
	{
		return _depends_on;
	}

	public Dependency(int mapNum, int depNum)
	{
		_map_num = mapNum;
		_depends_on = depNum;
	}
}