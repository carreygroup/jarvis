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
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import org.apache.http.conn.util.InetAddressUtils;

import android.R.string;
import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;

import com.vveye.T2u;

public class Demon 
{
	private Socket  			mSocket         = null;
	private DatagramSocket     	mSendPSocket    = null;
	private DatagramSocket     	mReceviedSocket = null;
	private byte    			Ethnet_Mode     = Ethnet.TCP;
	
	private SocketAddress 					mAddress		= null;
	private ArrayList<ConnectionListener>	mConnListeners	= new ArrayList<ConnectionListener>();
    private PrintWriter                     mPrintWriterClient      = null;    

    private static int              p2p_Port=0;
    T2u t2u=new T2u();
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
		if((Ethnet_Mode==Ethnet.TCP) || (Ethnet_Mode==Ethnet.P2P))
		{
			if(mSocket==null) 
				return false;
			else
				return mSocket.isConnected();
		}
		
		if(Ethnet_Mode==Ethnet.UDP)
		{
			if(mReceviedSocket!=null) return true;
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
    /******************************TCP
     * @throws UnknownHostException ******************************/
    public boolean Connection(byte mode,String argv1,String  argv2) throws UnknownHostException
    {
    	Ethnet_Mode=mode;
    	if(Ethnet_Mode==Ethnet.P2P)
    	{
    		P2PConnect(argv1,argv2);
    		
    	}
    	else
    	if(Ethnet_Mode==Ethnet.TCP)
    	{
        	//���������,������ת��ΪIP��ַ
    		java.net.InetAddress x;
    		x = java.net.InetAddress.getByName(argv1);
    		String host = x.getHostAddress();//�õ��ַ�����ʽ��ip��ַ		
    		
    		int port = Integer.valueOf(argv2);
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
    	else
    	if(Ethnet_Mode==Ethnet.UDP)
    	{
        	//���������,������ת��ΪIP��ַ
    		java.net.InetAddress x;
    		x = java.net.InetAddress.getByName(argv1);
    		String host = x.getHostAddress();//�õ��ַ�����ʽ��ip��ַ		
    		int port = Integer.valueOf(argv2);
    		
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
    	if((Ethnet_Mode==Ethnet.TCP) || (Ethnet_Mode==Ethnet.P2P))
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
    	if((Ethnet_Mode==Ethnet.TCP) || (Ethnet_Mode==Ethnet.P2P))
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
    	
    	if(Ethnet_Mode==Ethnet.P2P)
    	{
			// �رն˿�
			if (p2p_Port > 0)
			{
				T2u.DelPort((char) p2p_Port);
			}	
//			 �ͷ���Դ
			T2u.Exit();	
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
    /**
     * Get IP address from first non-localhost interface
     * @param ipv4  true=return ipv4, false=return ipv6
     * @return  address or empty string
     */
    public static String getLoaclIPAddress(boolean useIPv4) 
    {
        try 
        {
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface intf : interfaces) 
            {
                List<InetAddress> addrs = Collections.list(intf.getInetAddresses());
                for (InetAddress addr : addrs) 
                {
                    if (!addr.isLoopbackAddress()) 
                    {
                        String sAddr = addr.getHostAddress().toUpperCase();
                        boolean isIPv4 = InetAddressUtils.isIPv4Address(sAddr); 
                        if (useIPv4) 
                        {
                            if (isIPv4) 
                                return sAddr;
                        } 
                        else 
                        {
                            if (!isIPv4) 
                            {
                                int delim = sAddr.indexOf('%'); // drop ip6 port suffix
                                return delim<0 ? sAddr : sAddr.substring(0, delim);
                            }
                        }
                    }
                }
            }
        } 
        catch (Exception ex) { } // for now eat exceptions
        return "";
    }
    
	// P2P����
	// ����
		public boolean P2PConnect(String UUID,String PWD) 
		{
			int portstatus = 0;
			int intWaitTime = 0;
			// �豸ID
			if (UUID.length() == 0) 
			{
				Log.e("_DEBUG","�豸ID��");
				return false;
			}

			// ����
			if (PWD.length() == 0) 
			{
				Log.e("_DEBUG","�����");
				return false;
			}
			
			Log.v("_DEBUG","UUID:"+UUID+" Password:"+PWD);
			/*
			//��ȡSharedPreferences����
	        SharedPreferences sharedPre=getSharedPreferences("config", Context.MODE_PRIVATE);
	        //��ȡEditor����
	        Editor editor=sharedPre.edit();
	        //���ò���
	        editor.putString("username", strUUID);
	        editor.putString("password", strPwd);
	        //�ύ
	        editor.commit();
			 */
			// -------------SDK��ʼ��------------------------
			try 
			{
				T2u.Init("nat.vveye.net", (char) 8000, "");
			} 
			catch (Exception e) 
			{
				//Toast.makeText(mHomeActivity, "SDK��ʼ��ʧ��", Toast.LENGTH_SHORT).show();
				Log.e("_DEBUG","SDK��ʼ��ʧ��");
				return false;
			}

			// ---------���������豸-------------------------
			byte[] result = new byte[1500];
			int num = T2u.Search(result);
			String tmp;
			Log.v("_DEBUG","T2u.Search:" + num);
			if (num > 0) 
			{
				tmp = new String(result);
				Log.v("_DEBUG","Device:" + tmp);

			} 
			else 
			{
				//Toast.makeText(mHomeActivity, "û��Ѱ�ҵ��豸", Toast.LENGTH_SHORT).show();				
			}
			// ----------------�ȴ�����---------------------

			while (T2u.Status() == 0) 
			{
				SystemClock.sleep(1000);
				intWaitTime += 1;

				Log.v("_DEBUG","T2u.Status=" + T2u.Status());
				if (intWaitTime > 10) 
				{
					break;
				}
			}

			Log.v("_DEBUG","T2u.status -> " + T2u.Status());

			// -------------------���� �����ض˿�ӳ��--------------------
			if (T2u.Status() == 1) 
			{
				int ret;
				// ---------------��ѯ�豸�Ƿ�����-----------------
				ret = T2u.Query(UUID);
				Log.v("_DEBUG","T2u.Query:" + UUID + " -> " + ret);
				if (ret > 0) 
				{
					// ******************����IP*********************
					// String strIP = Utils.getIPAddress(true);
					// setTitle("IP:" + strIP);
					// --------------��������ӳ��˿�-----------------
					p2p_Port = T2u.AddPortV3(UUID, PWD, "127.0.0.1", (char)8080,(char)0);
					Log.v("_DEBUG","T2u.add_port -> port:" + p2p_Port);

					while(portstatus==0)
					{
						SystemClock.sleep(1000);
						
						portstatus=T2u.PortStatus((char)p2p_Port);
						Log.v("_DEBUG","portstatus="+portstatus);
						if(portstatus==1)
						{
							Log.v("_DEBUG","����Զ���豸��ͨ");
							//return true;
						}else if(portstatus==0)
						{
							Log.v("_DEBUG","���ڽ�������......");
						}else if(portstatus==-1)
						{
							//Toast.makeText(mHomeActivity, "δ�ҵ��豸", Toast.LENGTH_SHORT).show();
							Log.e("_DEBUG","δ�ҵ��豸");
							T2u.Exit();
							return false;
						}else if(portstatus==-5)
						{
							//Toast.makeText(mHomeActivity, "�������", Toast.LENGTH_SHORT).show();
							Log.e("_DEBUG","�������");
							T2u.Exit();
							return false;
						}
					}
				} 
				else 
				{
					//Toast.makeText(mHomeActivity, "�豸������", Toast.LENGTH_SHORT).show();
					return false;
				}
			} 
			else 
			{
				//Toast.makeText(mHomeActivity, "�豸������", Toast.LENGTH_SHORT).show();
				return false;
			}
			// �ж϶˿ں��Ƿ�ӳ��ɹ�
			if (!isNumeric(String.valueOf(p2p_Port))) 
			{
				return false;
			}
			// -------------------���ӳɹ�,���ذ�ť���ڻ״̬--------------------
			String mHost = getLoaclIPAddress(true);
			Log.i("_DEBUG", "IP:" + mHost + "," + "Port:" + p2p_Port);
			
	    	try 
	    	{
	    		mSocket = new Socket();
	    		mAddress = new InetSocketAddress(mHost, p2p_Port);
				mSocket.connect(mAddress, 5000);			
				mPrintWriterClient = new PrintWriter(mSocket.getOutputStream(), true);
				if(mSocket.isConnected())
					Log.v("_DEBUG","Connected!");
				else 
					Log.v("_DEBUG","No Connected!");
				return true;
			} 
	    	catch (IOException e) 
	    	{
				// TODO Auto-generated catch block	    		
				e.printStackTrace();
				return false;
			}
		}
			// ��JAVA�Դ��ĺ���
			public static boolean isNumeric(String str) 
			{
				for (int i = str.length(); --i >= 0;) 
				{
					if (!Character.isDigit(str.charAt(i))) 
					{
						return false;
					}
				}
				return true;
			}
}