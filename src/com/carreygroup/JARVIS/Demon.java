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

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Enumeration;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.util.Log;


public class Demon 
{
	private Socket  			mSocket         = null;
	private DatagramSocket     	mSendPSocket    = null;
	private DatagramSocket     	mReceviedSocket = null;
	private byte    			Ethnet_Mode     = Ethnet.TCP;
	
	private SocketAddress 					mAddress		= null;
	private ArrayList<ConnectionListener>	mConnListeners	= new ArrayList<ConnectionListener>();
    private PrintWriter                     mPrintWriterClient      = null;    

    /**
     * 网络连接状态观察者
     * @author firefish
     *
     */
    public interface ConnectionListener
    {
    	public void onConnected(Demon connection);
    	public void onDisconnected();
    } 
    
	public Demon()
	{
		System.setProperty("java.net.preferIPv6Addresses", "false");
	}
	/******************************ConnectionListener******************************/
    public void AddConnectionListener(ConnectionListener listener)
    {
    	try 
    	{
			for(ConnectionListener ConnListener : mConnListeners)
			{
				if(listener==ConnListener) return;
			}
			
	    	mConnListeners.add(listener);
	    	if(Connected())
	    	{
	    		listener.onConnected(this);
	    	}
		} catch (Exception e) 
		{
			Log.v("_DEBUG",e.getLocalizedMessage());
		}
    }
    
    private String reverse(String str, int len)
    {
        String result= ""; 
        for(int i=0; i<str.length(); i++)
        { 
        	result = result + str.substring(str.length()-i-1, str.length()-i); 
        }
        
        for(int i=result.length(); i<len; i++)
        {
        	result += "0";
        }
        
        return result;    	
    }
    
	public class SendDataException extends Exception 
	{
		public SendDataException() 
		{
			super();
		}
		public SendDataException(String msg) 
		{
			super(msg);
		}	
		
		@Override
		public String toString()
		{
			return "发送数据失败，网络连接已断开。";			
		}
	}
	
	public class ParseDataException extends Exception 
	{
		public ParseDataException() 
		{
			super();
		}
		public ParseDataException(String msg) 
		{
			super(msg);
		}	
		@Override
		public String toString()
		{
			return "解析返回的数据失败。";			
		}		
	}    

    /******************************Protocol******************************/	
    /**
     * 获取所有继电器的状态
     * @param tcp 连接对象
     * @return 各路继电器的状态，1-开启，2-关闭
     * @throws SendDataException
     * @throws ParseDataException
     */
	public boolean fetchRelayStatus(byte devid) throws SendDataException, ParseDataException
	{
		// 获取继电器状态
		// 获取继电器状态
		Packet packet = new Packet();
		packet.fetchRelayStatus(devid);
		boolean ret=false;
		try 
		{
			//发送命令数据,并等待控制器返回状态数据
			ret = Send(packet);//同步方式获取数据
		} 
		catch (IOException e) 
		{
			throw new SendDataException();
		}
		return ret;
	}
	
	/**
	 * 切换单路开关
	 * @param tcp 连接对象
	 * @param id 继电器序号
	 * @param status 开关状态
	 * @return 切换结果，true-开， false-关
	 * @throws SendDataException
	 * @throws ParseDataException
	 */
	public boolean switchRelay(byte devid,byte relay, boolean status) throws SendDataException, ParseDataException
	{
		Packet packet = new Packet();
		packet.switchRelay(devid,relay, status);
		
		boolean ret = false;
		try 
		{
			ret = Send(packet);
		} 
		catch (IOException e) 
		{
			throw new SendDataException();
		}
		
		return ret;
	}
	
	public boolean SendScene(int devid,int scenesn) throws SendDataException, ParseDataException
	{
		Packet packet = new Packet();
		packet.Scene(devid,scenesn);
		
		boolean ret = false;
		try 
		{
			ret = Send(packet);
		} 
		catch (IOException e) 
		{
			throw new SendDataException();
		}
		return ret;
	}
	

	
	/******************************Protocol******************************/
    
	private void notifyDisconnected() 
	{
		for(ConnectionListener listener : mConnListeners)
		{
			listener.onDisconnected();
		}
	}
	
	public boolean Connected()
	{
		if(Ethnet_Mode==Ethnet.TCP)
		{
			if(mSocket==null) 
				return false;
			else
				return mSocket.isConnected();
		}
		
		if(Ethnet_Mode==Ethnet.UDP)
		{
			if(mReceviedSocket!=null) 
				return true;
		}
		return false;
	}
	
	public Socket getSocket()
	{
		return mSocket;
	}
	
/*	public byte getEthnet_mode()
	{
		return Ethnet_Mode;
	}*/
    /******************************TCP******************************/
    public boolean Connection(byte mode,String host,int port)
    {
    	Ethnet_Mode=mode;
    	if(Ethnet_Mode==Ethnet.TCP)
    	{
	    	try 
	    	{
	    		mSocket = new Socket();
	    		mAddress = new InetSocketAddress(host, port);
				mSocket.connect(mAddress, 5000);			
				mPrintWriterClient = new PrintWriter(mSocket.getOutputStream(), true);
				if(mSocket.isConnected())
					Log.v("_DEBUG","Connected!");
				else 
					Log.v("_DEBUG","No Connected!");
			} 
	    	catch (IOException e) 
	    	{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			// 开启线程定时发送连接检测 
			// new Thread(new ActiveTest(mSocket.socket())).start();
    	}
    	
    	if(Ethnet_Mode==Ethnet.UDP)
    	{
    		mAddress = new InetSocketAddress(host, port);    		
    		try 
    		{
				mSendPSocket = new DatagramSocket();
				mSendPSocket.setBroadcast(true);
				mReceviedSocket=new DatagramSocket(port);
				
			} 
    		catch (Exception e) 
    		{
				// TODO Auto-generated catch block
				e.printStackTrace();
				return false;
			}
    	}
		
		if(Connected())
		{
			for(ConnectionListener listener : mConnListeners)
			{
				listener.onConnected(this);
			}				
		}
    	return Connected();
    }
    
    public boolean Send(Packet p) throws IOException
    {
    	boolean ret=false;
    	if(Ethnet_Mode==Ethnet.TCP)
    	{
	    	ByteBuffer bytebufOut = ByteBuffer.allocate(Ethnet.BUFFER_SIZE); 
	    	bytebufOut = ByteBuffer.wrap(p.toByteArray()); 
	    	try 
	    	{
		    	mPrintWriterClient.print(new String(bytebufOut.array()));//发送方向给服务器
		    	mPrintWriterClient.flush();
				ret=true;
			} 
	    	catch (Exception e) 
			{
				notifyDisconnected();
				throw new IOException("发送数据失败，网络连接已断开。");
			} 
	    	bytebufOut.flip();
	    	bytebufOut.clear();
    	}
    	
    	if(Ethnet_Mode==Ethnet.UDP)
    	{
    		byte[] buff=p.toByteArray();    		
			DatagramPacket packet = new DatagramPacket(buff, buff.length ,mAddress);  
			mSendPSocket.send(packet);//把数据发送到服务端。
			ret=true;
    	}
    	return ret;
    }
    
    public void Close() throws IOException
    {
    	if(Ethnet_Mode==Ethnet.TCP)
    	{
	    	mPrintWriterClient.close();
	    	mPrintWriterClient=null;
			mSocket.close();
			mSocket=null;
    	}
    	
    	if(Ethnet_Mode==Ethnet.UDP)
    	{
    		mSendPSocket.close();
    		mReceviedSocket.close();
    		mSendPSocket=null;
    		mReceviedSocket=null;
    	}
    	mAddress=null;
		notifyDisconnected();       
    }    
    /******************************TCP******************************/
    
    /******************************UDP******************************/
    public byte[] ReceviedByUdp()
    {
    	Log.i("_DEBUG","ReceviedByUdp!");
		byte data[] = new byte[8];  
		//创建一个DatagramPacket对象，并指定DatagramPacket对象的大小  
		DatagramPacket packet = new DatagramPacket(data,data.length);  
		//读取接收到得数据  
		try 
		{
			
			mReceviedSocket.receive(packet);
			
		} 
		catch (IOException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}  
    	return packet.getData();
    }
/*	protected void SendWithUDPSocket(String host,int port,byte[] data) 
	{
		
		DatagramSocket socket;
		try {
			//创建DatagramSocket对象并指定一个端口号，注意，如果客户端需要接收服务器的返回数据,
			//还需要使用这个端口号来receive，所以一定要记住
			socket = new DatagramSocket(port);
			//使用InetAddress(Inet4Address).getByName把IP地址转换为网络地址  
			InetAddress serverAddress = InetAddress.getByName(host);
			//Inet4Address serverAddress = (Inet4Address) Inet4Address.getByName("192.168.1.32");  
			String str = "[2143213;21343fjks;213]";//设置要发送的报文  
			byte data[] = str.getBytes();//把字符串str字符串转换为字节数组  
			//创建一个DatagramPacket对象，用于发送数据。  
			//参数一：要发送的数据  参数二：数据的长度  参数三：服务端的网络地址  参数四：服务器端端口号 
			DatagramPacket packet = new DatagramPacket(data, data.length ,serverAddress ,port);  
			socket.send(packet);//把数据发送到服务端。  
		} 
		catch (SocketException e) 
		{
			e.printStackTrace();
		} 
		catch (UnknownHostException e) 
		{
			e.printStackTrace();
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}  
	}
	
	public void ServerReceviedByUdp(int port)
	{
		//创建一个DatagramSocket对象，并指定监听端口。（UDP使用DatagramSocket）  
		DatagramSocket socket;
		try 
		{
			socket = new DatagramSocket(port);
			//创建一个byte类型的数组，用于存放接收到得数据  
			byte data[] = new byte[32];  
			//创建一个DatagramPacket对象，并指定DatagramPacket对象的大小  
			DatagramPacket packet = new DatagramPacket(data,data.length);  
			//读取接收到得数据  
			socket.receive(packet);  
			//把客户端发送的数据转换为字符串。  
			//使用三个参数的String方法。参数一：数据包 参数二：起始位置 参数三：数据包长  
			String result = new String(packet.getData(),packet.getOffset() ,packet.getLength());  
		} 
		catch (SocketException e) 
		{
			e.printStackTrace();
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}  
	}*/
	/******************************UDP******************************/
}