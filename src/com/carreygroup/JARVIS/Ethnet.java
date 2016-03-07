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

public class Ethnet
{
	public final static int BUFFER_SIZE = 8;
/*	public static final byte SYNC	   = 0x01; //同步通讯
	public static final byte ASYNC       = 0x02; //异步通讯	
*/
	public static final byte MSG_UPDATE_STATUS	   = 0x01; //更新项目状态
	public static final byte MSG_STATUS_TIMEOUT	   = 0x02; //更新项目状态
	
	public static final byte TCP	   = 0x00; //TCP模式
	public static final byte UDP       = 0x01; //UDP模式
	public static final byte P2P       = 0x02; //P2P模式
	
	public static final int THREAD_TIMEOUT  = 1000; //线程超时
	public static final int SELECT_TIMEOUT  = 500; //线程超时
}