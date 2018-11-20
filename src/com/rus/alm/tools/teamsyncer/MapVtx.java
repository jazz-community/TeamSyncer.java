package com.rus.alm.tools.teamsyncer;

public class MapVtx
{
	private String _from, _to;
	private int _map_num;

	public final String getFrom()
	{
		return _from;
	}
	public final String getTo()
	{
		return _to;
	}
	public final int getMapNum()
	{
		return _map_num;
	}

	public MapVtx(int mapNum, String From, String To)
	{
		_from = From;
		_to = To;
		_map_num = mapNum;
	}
}