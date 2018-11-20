package com.rus.alm.tools.teamsyncer;

import java.util.*;

public class MapVtxList
{
	private ArrayList<MapVtx> _list = new ArrayList<MapVtx>();

	public final void Add(int mapNum, String From, String To)
	{
		_list.add(new MapVtx(mapNum, From, To));
	}

	public final int isAmongFrom(String name)
	{
		for (MapVtx mvtx : _list)
		{
			if (mvtx.getFrom().equals(name))
			{
				return mvtx.getMapNum();
			}
		}
		return 0;
	}

	public final int isAmongTo(String name)
	{
		for (MapVtx mvtx : _list)
		{
			if (mvtx.getTo().equals(name))
			{
				return mvtx.getMapNum();
			}
		}
		return 0;
	}

	public final void BuildDepList(ArrayList<Dependency> depListToFill)
	{
		for (MapVtx mvtx : _list)
		{
			int q = isAmongTo(mvtx.getFrom());
			if (q > 0)
			{
				boolean found = false;
				for (Dependency d : depListToFill)
				{
					if ((d.getMapNum() == mvtx.getMapNum()) && (d.getDepNum() == q))
					{
						found = true;
					}
				}
				if (!found)
				{
					depListToFill.add(new Dependency(mvtx.getMapNum(), q));
				}
			}
		}
	}
}