/*
 * This file is part of JARVIS
 * COPYRIGHT (C) 2008 - 2016, Carrey Group
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Change Logs:
 * Date           Author       Notes
 * 20014-01-01     Caesar      the first version
 */
package com.carreygroup.JARVIS;

public class SceneItem 
{
	private byte id = 0;
	private String name = "";
	private int nScene=1;
	private int devid=1;
	
	SceneItem(byte id, String name, int did,int SceneSN)
	{
		this.id = id;
		this.name = name;
		this.devid=did;
		this.nScene=SceneSN;
	}
	
	public byte getID()
	{
		return id;
	}
	
	public void setID(byte value)
	{
		id = value;
	}	
	
	public String getName()
	{
		return name;
	}
	
	public void setName(String value)
	{
		name = value;
	}
	
	public int getSceneSN()
	{	
		return this.nScene;
	}
	
/*	public byte getStatus(int index)
	{
		if(index >= status.length)
			return 0;
		
		return status[index];
	}*/
	
	public void setSceneSN(int value)
	{
		nScene = value;
	}	

	public int getDevID()
	{	
		return this.devid;
	}
	
	public void setDevID(int value)
	{	
		devid=value;
	}
/*	public void setStatus(int index, byte value)
	{
		if(index >= status.length)
			return;
		
		status[index] = value;
	}*/	
}
