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

public class ItemInfo 
{
	private int id = 0x00;//ID
	private String name = "";//Ãû³Æ
	private boolean status = false;//
	private int icon = 0;
	private byte DevID=0;
	private byte Relay=0;
	//private String timerOn = "";
	//private String timerOff = "";
	
	private static final int[] 	ICONS = {R.drawable.icon_light, R.drawable.icon_tv, R.drawable.icon_fan};
	
	ItemInfo(byte id, String name, int icon)
	{
		this.id = id;
		this.name = name;
		this.icon = icon;
	}
	
	public int GetID()
	{
		return id;
	}
	
	public void SetID(int value)
	{
		id = value;
	}	
	
	public String GetName()
	{
		return name;
	}
	
	public void SetName(String value)
	{
		name = value;
	}
	
	public boolean GetStatus()
	{
		return status;
	}
	
	public void SetStatus(boolean value)
	{
		status = value;
	}
	
	public int GetIcon()
	{
		return icon;
	}
	
	public void SetIcon(int image)
	{
		this.icon = image;
	}
	
	public int GetIconResId()
	{
		if(this.icon > ICONS.length)
			return ICONS[0];
		
		return ICONS[this.icon];
	}
	
	public byte GetDevid()
	{
		return DevID;
	}
	
	public void SetDevid(byte id)
	{
		this.DevID = id;
	}

	public byte GetRelay()
	{
		return Relay;
	}
	
	public void SetRelay(byte relay)
	{
		this.Relay = relay;
	}
	/*
	public String GetTimerOn()
	{
		return timerOn;
	}
	
	public void SetTimerOn(String value){
		timerOn = value;
	}	
	
	public String GetTimerOff(){
		return timerOff;
	}
	
	public void SetTimerOff(String value){
		timerOff = value;
	}*/	
}
