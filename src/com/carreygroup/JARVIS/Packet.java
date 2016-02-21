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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Serializable;

import android.util.Log;


/**
 *
 * @author firefish
 */
public class Packet implements Serializable
{
    private byte      		bBegin		= 0x55;    
    private byte            bAddress	= 0;
    private byte			bCtrlCode	= 0x00;
    private byte[]			baData    	= new byte[4];
    private byte			bSUM		= 0;

    public Packet()
    {
    	bAddress=0;
    }
    /*
     * 帧控制码
     */
    public final class Command
    {
        public static final byte POWER_ON	     = 0x12; // 开某一路
        public static final byte POWER_OFF       = 0x11; // 关某一路
        public static final byte GET_STATUS      = 0x10; // 查询
        public static final byte CONTROL_SCENE   = 0x01; // 控制
    }
    /**
     * 解析数据包
     * @param msg
     * @throws IOException
     */
    public Packet(byte[] msg) throws IOException
    {
        bBegin = msg[0];
        bAddress=msg[1];
        bCtrlCode = msg[2];
        System.arraycopy(msg, 3, baData, 0, 4);
        bSUM = msg[7];       
        
        // 校验
        if(getSumCode() != bSUM)
        {
        	Log.e("MyPackage", "Virify failed!");
        	throw new IOException("数据校验失败！");
        }
    }
    
    public boolean CheckPacket(byte[] msg)
    {
        bBegin = 0x22;
        bAddress=msg[1];
        bCtrlCode = msg[2];
        System.arraycopy(msg, 3, baData, 0, 4);
        bSUM = msg[7];       
        
        // 校验
        if(getSumCode() == bSUM)
        {
        	return true;
        }
        return false;
    }
    public byte getDevID()
    {
    	return bAddress;
    }
    
    public byte[] getData()
    {
    	return baData;
    }
    public void switchRelay(byte devid,byte relay, boolean status)
    {   
    	bAddress=devid;
    	if(status)
    		bCtrlCode = Command.POWER_ON;
    	else 
    		bCtrlCode = Command.POWER_OFF;    	
   		baData[3] = relay;
    }
    
    public void Scene(int devid,int scenesn)
    {      
    	bAddress=(byte) devid;
    	bCtrlCode = Command.CONTROL_SCENE;
    	baData[2] = (byte) (scenesn>>8);
    	baData[3] = (byte) scenesn;
    }
    
/*    public void switchAll(boolean status)
    {      	
    	bCtrlCode = 0x11;

    	baData = new byte[13];
    	
    	if(status)
    		baData[0] = 0x0F;
    	else
    		baData[0] = 0x1F;
    	
    	for(int i=0; i<16; i++)
    	{
    		baData[i+1] = 0x00;
    	}
    }*/
    
    /**
     * 读取全部继电器状态
     */
    public void fetchRelayStatus(byte devid)
    {
    	bCtrlCode = Command.GET_STATUS;
    	bAddress=devid;
    	bSUM=getSumCode();
    }
    
    /**
     * 读取单路继电器状态
     * @param line
     */
/*    public void fetchLineStatus(byte line)
    {
    	bCtrlCode = 0x01;
    	baData = new byte[]{line};
    }*/
    
    
    /**
     * 将包序列化成字节数组
     * @return 序列化结果
     * @throws IOException 
     */
    public byte[] toByteArray() throws IOException
    {
        ByteArrayOutputStream bs = new ByteArrayOutputStream();
        DataOutputStream stream = new DataOutputStream(bs);
        byte[] ret;
        
        stream.writeByte(bBegin);
        stream.writeByte(bAddress);
        stream.writeByte(bCtrlCode);
        stream.write(baData);
        
        // 校验码     
	    stream.writeByte(getSumCode());
	    
        stream.flush();
        stream.close();
        bs.flush();
        
        ret = bs.toByteArray();
        bs.close();

        return ret;
    }
    
    /**
     * 计算校验码
     * @return 当前报文数据校验码
     */
    private byte getSumCode()
    {
    	byte ret=0;
    	ret+=bBegin;    
    	ret+=bAddress;
    	ret+=bCtrlCode;
    	ret+=baData[0];
    	ret+=baData[1];
    	ret+=baData[2];
    	ret+=baData[3];
        ret = (byte)(ret % 256);
        return ret;
        
    }
}
