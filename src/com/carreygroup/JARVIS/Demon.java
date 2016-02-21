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
     * ��������״̬�۲���
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
			return "��������ʧ�ܣ����������ѶϿ���";			
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
			return "�������ص�����ʧ�ܡ�";			
		}		
	}    

    /******************************Protocol******************************/	
    /**
     * ��ȡ���м̵�����״̬
     * @param tcp ���Ӷ���
     * @return ��·�̵�����״̬��1-������2-�ر�
     * @throws SendDataException
     * @throws ParseDataException
     */
	public boolean fetchRelayStatus(byte devid) throws SendDataException, ParseDataException
	{
		// ��ȡ�̵���״̬
		// ��ȡ�̵���״̬
		Packet packet = new Packet();
		packet.fetchRelayStatus(devid);
		boolean ret=false;
		try 
		{
			//������������,���ȴ�����������״̬����
			ret = Send(packet);//ͬ����ʽ��ȡ����
		} 
		catch (IOException e) 
		{
			throw new SendDataException();
		}
		return ret;
	}
	
	/**
	 * �л���·����
	 * @param tcp ���Ӷ���
	 * @param id �̵������
	 * @param status ����״̬
	 * @return �л������true-���� false-��
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
			// �����̶߳�ʱ�������Ӽ�� 
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
		    	mPrintWriterClient.print(new String(bytebufOut.array()));//���ͷ����������
		    	mPrintWriterClient.flush();
				ret=true;
			} 
	    	catch (Exception e) 
			{
				notifyDisconnected();
				throw new IOException("��������ʧ�ܣ����������ѶϿ���");
			} 
	    	bytebufOut.flip();
	    	bytebufOut.clear();
    	}
    	
    	if(Ethnet_Mode==Ethnet.UDP)
    	{
    		byte[] buff=p.toByteArray();    		
			DatagramPacket packet = new DatagramPacket(buff, buff.length ,mAddress);  
			mSendPSocket.send(packet);//�����ݷ��͵�����ˡ�
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
		//����һ��DatagramPacket���󣬲�ָ��DatagramPacket����Ĵ�С  
		DatagramPacket packet = new DatagramPacket(data,data.length);  
		//��ȡ���յ�������  
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
			//����DatagramSocket����ָ��һ���˿ںţ�ע�⣬����ͻ�����Ҫ���շ������ķ�������,
			//����Ҫʹ������˿ں���receive������һ��Ҫ��ס
			socket = new DatagramSocket(port);
			//ʹ��InetAddress(Inet4Address).getByName��IP��ַת��Ϊ�����ַ  
			InetAddress serverAddress = InetAddress.getByName(host);
			//Inet4Address serverAddress = (Inet4Address) Inet4Address.getByName("192.168.1.32");  
			String str = "[2143213;21343fjks;213]";//����Ҫ���͵ı���  
			byte data[] = str.getBytes();//���ַ���str�ַ���ת��Ϊ�ֽ�����  
			//����һ��DatagramPacket�������ڷ������ݡ�  
			//����һ��Ҫ���͵�����  �����������ݵĳ���  ������������˵������ַ  �����ģ��������˶˿ں� 
			DatagramPacket packet = new DatagramPacket(data, data.length ,serverAddress ,port);  
			socket.send(packet);//�����ݷ��͵�����ˡ�  
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
		//����һ��DatagramSocket���󣬲�ָ�������˿ڡ���UDPʹ��DatagramSocket��  
		DatagramSocket socket;
		try 
		{
			socket = new DatagramSocket(port);
			//����һ��byte���͵����飬���ڴ�Ž��յ�������  
			byte data[] = new byte[32];  
			//����һ��DatagramPacket���󣬲�ָ��DatagramPacket����Ĵ�С  
			DatagramPacket packet = new DatagramPacket(data,data.length);  
			//��ȡ���յ�������  
			socket.receive(packet);  
			//�ѿͻ��˷��͵�����ת��Ϊ�ַ�����  
			//ʹ������������String����������һ�����ݰ� ����������ʼλ�� �����������ݰ���  
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