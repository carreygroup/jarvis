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

import android.R.integer;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.MulticastLock;
import android.os.Bundle;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Handler;

import org.apache.http.conn.util.InetAddressUtils;

public class MyApplication extends Application
{
	private Demon                           demon=null;
	private String							mHost			= "192.168.1.255";
	private String                          mPort           = "8080";
	private int                             mEthent         = 1;
	
    private SharedPreferences				mConfig			= null;
    private ArrayList<ItemInfo> 			mItems 			= new ArrayList<ItemInfo>();
    private ArrayList<SceneItem> 			mScenes			= new ArrayList<SceneItem>();    
    private ArrayList<Byte>                 mDeviceIDs      = new ArrayList<Byte>();
    private HomeActivity                    mHomeActivity   = null;
    
    private static final String KEY_ITEM_NAME_SCHEMA		= "Item%dName";
    private static final String KEY_ITEM_IMAGE_SCHEMA		= "Item%dImage";
    private static final String KEY_ITEM_DEVID_SCHEMA		= "Item%dDEVID";
    private static final String KEY_ITEM_RELAY_SCHEMA		= "Item%dRELAY";
/*    private static final String CONFIG_KEY_TIMERON_SCHEMA	= "Line%dTimerOn";
    private static final String CONFIG_KEY_TIMEROFF_SCHEMA	= "Line%dTimerOff";*/
    private static final String KEY_SCENE_NAME_SCHEMA		= "Scene%dName";
    private static final String KEY_SCENE_DEVID_SCHEMA	    = "Scene%dDEVID";
    private static final String KEY_SCENE_SCENESN_SCHEMA	= "Scene%dStatus"; 
    private static final String KEY_HOST_IP					= "Host";
    private static final String KEY_HOST_PORT				= "Port";
    private static final String KEY_ETHNETMODE      		= "EthnetMode";
    
    private boolean ThreadRun = false;
   //private Handler m_HomeHandler = null;
    
    private Thread m_ReceiverThread = null;
    private Thread m_DetectorTHread = null;

    WifiManager wifimanager = null;
    
    @Override
    public void onCreate() 
    {    
    	try 
    	{
    		demon=new Demon();
		} 
    	catch (Exception e) 
    	{
			Toast.makeText(MyApplication.this, "初始化网络失败！", Toast.LENGTH_SHORT).show();
			return;
		}
    	
    	LoadData();
    }
    
    public void LoadData()
    {
        mConfig = getSharedPreferences("jarvis.xml",Context.MODE_PRIVATE);
        mItems.clear();
        mScenes.clear();
        mDeviceIDs.clear();
        
    	byte i=0;
        for(i=0; ; i++)
        {
        	try
        	{
	        	// 线路配置信息
	        	String Item_Name = mConfig.getString(String.format(KEY_ITEM_NAME_SCHEMA, i), "");
	        	if(Item_Name != "")
	        	{
		        	int icon = mConfig.getInt(String.format(KEY_ITEM_IMAGE_SCHEMA, i), 0);
		        	//String timerOn = mConfig.getString(String.format(CONFIG_KEY_TIMERON_SCHEMA, i), "");
		        	//String timerOff = mConfig.getString(String.format(CONFIG_KEY_TIMEROFF_SCHEMA, i), "");
		        	int DevID= mConfig.getInt(String.format(KEY_ITEM_DEVID_SCHEMA, i), 0);
		        	int relay= mConfig.getInt(String.format(KEY_ITEM_RELAY_SCHEMA, i), 0);
		        	ItemInfo item = new ItemInfo(i, Item_Name, icon);
		        	item.SetDevid((byte) DevID);
		        	item.SetRelay((byte) relay);
		        	//item.SetTimerOn(timerOn);
		        	//item.SetTimerOff(timerOff);
		        	PushDevIDs((byte) DevID);
		        	mItems.add(item);
	        	}
	        	else
	        		break;
        	}
        	catch(Exception e)
        	{
        		break;
        	}
        }
        if(i==0)
        {
        	for(int index=0;index<16;index++)
        	{
	        	ItemInfo item = new ItemInfo((byte) index, "项目"+(index+1), 0);
	        	item.SetDevid((byte) 1);
	        	item.SetRelay((byte) (index+1));
	        	PushDevIDs((byte) 1);
	        	mItems.add(item);
        	}
        }
        for(i=0; ; i++)
        {
        	try
        	{
	        	// 分组信息                	
	        	String Scene_Name = mConfig.getString(String.format(KEY_SCENE_NAME_SCHEMA, i), "");
	        	if(Scene_Name != "")
	        	{
	        		int nDevid = mConfig.getInt(String.format(KEY_SCENE_DEVID_SCHEMA, i), 0);
	        		int nScene = mConfig.getInt(String.format(KEY_SCENE_SCENESN_SCHEMA, i), 0);
	        		SceneItem sitem=new SceneItem(i, Scene_Name, nDevid, nScene);
	        		mScenes.add(sitem);
	        	}
	        	else
	        		break;
        	}
        	catch(Exception e)
        	{
        		break;
        	}
        }
        mHost = mConfig.getString(KEY_HOST_IP, "192.168.1.255");
        mPort=  mConfig.getString(KEY_HOST_PORT, "8080");
        mEthent = mConfig.getInt(KEY_ETHNETMODE, 0);
    }
    public void PushDevIDs(Byte devid)
    {
    	boolean found=false;
    	for(int i=0;i<mDeviceIDs.size();i++)
    	{
    		if(mDeviceIDs.get(i)==devid)
    		{
    			found=true;
    			break;
    		}    		
    	}
    	if(!found)
    	{
    		mDeviceIDs.add(devid);
    	}
    }
    /**
     * 保存配置项
     * @param item
     */
    public void saveItem(ItemInfo item)
    {
    	Editor editor = mConfig.edit();
    	editor.putString(String.format(KEY_ITEM_NAME_SCHEMA, item.GetID()), item.GetName());
    	editor.putInt(String.format(KEY_ITEM_IMAGE_SCHEMA, item.GetID()), item.GetIcon());
    	editor.putInt(String.format(KEY_ITEM_DEVID_SCHEMA, item.GetID()), item.GetDevid());
    	editor.putInt(String.format(KEY_ITEM_RELAY_SCHEMA, item.GetID()), item.GetRelay());
    	//editor.putString(String.format(CONFIG_KEY_TIMERON_SCHEMA, item.GetID()), item.GetTimerOn());
    	//editor.putString(String.format(CONFIG_KEY_TIMEROFF_SCHEMA, item.GetID()), item.GetTimerOff());
    	editor.commit();
    }
    
    /**
     * 保存分组信息
     * @param scene
     */
    public void saveScene(SceneItem scene)
    {   	
    	if(scene.getName() == "") return;
    	
    	Editor editor = mConfig.edit();
    	editor.putString(String.format(KEY_SCENE_NAME_SCHEMA, scene.getID()), scene.getName());
    	editor.putInt(String.format(KEY_SCENE_DEVID_SCHEMA, scene.getID()), scene.getDevID());
    	editor.putInt(String.format(KEY_SCENE_SCENESN_SCHEMA, scene.getID()), scene.getSceneSN());
    	editor.commit();    	
    }
    
    public void deleteScene(SceneItem scene)
    {
    	Editor editor = mConfig.edit();
    	editor.remove(String.format(KEY_SCENE_NAME_SCHEMA, scene.getID()));
    	editor.remove(String.format(KEY_SCENE_DEVID_SCHEMA, scene.getID()));
    	editor.remove(String.format(KEY_SCENE_SCENESN_SCHEMA, scene.getID()));
        editor.commit();  
    }
  
    public void deleteItem(ItemInfo item)
    {
    	Editor editor = mConfig.edit();
    	editor.remove(String.format(KEY_ITEM_NAME_SCHEMA, item.GetID()));
    	editor.remove(String.format(KEY_ITEM_IMAGE_SCHEMA, item.GetID()));
    	editor.remove(String.format(KEY_ITEM_DEVID_SCHEMA, item.GetID()));
    	editor.remove(String.format(KEY_ITEM_RELAY_SCHEMA, item.GetID()));
        editor.commit();  
    }
    /**
     * 保存控制主机地址
     * @param ip
     */
    public void saveHostIP(String ip)
    {
    	mHost=ip;
    	mConfig.edit().putString(KEY_HOST_IP, ip).commit();
    }
    
    public String getHostIP()
    {
    	return mHost;
    }
 
    public void saveHostPort(String port)
    {
    	mPort=port;
    	mConfig.edit().putString(KEY_HOST_PORT, port).commit();
    }
  
    public int getEthnetMode()
    {
    	return mEthent;
    }
 
    public void setEthnetMode(int mode)
    {
    	mEthent=mode;
    }
    
    public void saveEthnetMode()
    {
    	mConfig.edit().putInt(KEY_ETHNETMODE, mEthent).commit();
    }
    
    public String getPort()
    {
    	return mPort;
    }
    
    public ArrayList<ItemInfo> getItems()
    {
    	return mItems;
    }
    
    public ArrayList<SceneItem> getScenes()
    {
    	return mScenes;
    }
    
    public ArrayList<Byte> getDeviceStatus()
    {
    	return mDeviceIDs;
    }

    public Demon getDemon()
    {
    	return demon;
    }

    public void SetItemStatus(byte devid,byte relay,boolean status)
    {
    	for(int i=0;i<mItems.size();i++)
    	{
    		if((mItems.get(i).GetDevid()==devid)&&(mItems.get(i).GetRelay()==relay))
    		{
    			mItems.get(i).SetStatus(status);
    			break;
    		}
    	}
    }
    
    public void SetItemStatus(byte devid,byte[] status)
    {
    	for(int i=0;i<mItems.size();i++)
    	{
    		int s=0;
    		if(mItems.get(i).GetDevid()==devid)
    		{
    			byte relay=mItems.get(i).GetRelay();
    			if(relay>8)
    				s=(status[2]>>(relay-8-1))&0x01;
		    	else 
		    		s=(status[3]>>(relay-1))&0x01;
    			if(s==1)
    				mItems.get(i).SetStatus(true);
    			else 
    				mItems.get(i).SetStatus(false);	
    		}
    	}
    }
    
    public void setHomeActivity(HomeActivity m)
    {
    	mHomeActivity=m;
    }
    
    public HomeActivity getHomeActivity()
    {
    	return mHomeActivity;
    }

    public void CreateListener()
    {
    	if(demon.Connected())
    	{
    		try 
    		{
    			ThreadRun=true;
        		if(m_ReceiverThread!=null) m_ReceiverThread.interrupt();
        		if(m_DetectorTHread!=null) m_DetectorTHread.interrupt();

        		if((getEthnetMode()==Ethnet.TCP)||(getEthnetMode()==Ethnet.P2P))
        		{
        			m_DetectorTHread = new Thread(m_Detector);
        			m_ReceiverThread = new Thread(m_TCP_Receiver); 
            		m_ReceiverThread.start();
            		m_DetectorTHread.start();
        		}
        		
        		if(getEthnetMode()==Ethnet.UDP)
        		{
        			try 
        			{
        				wifimanager = (WifiManager)this.getSystemService(Context.WIFI_SERVICE);
        				m_DetectorTHread = new Thread(m_Detector);
        				m_ReceiverThread = new Thread(m_UDP_Receiver); 
                		m_ReceiverThread.start();
                		m_DetectorTHread.start();
					} 
        			catch (Exception e) 
        			{
						// TODO: handle exception
        				Log.e("_DEBUG","无法启动本机的WIFI UDP广播接收功能,UDP监听线程无法创建!");
        				Toast.makeText(mHomeActivity, "无法启动本机的WIFI UDP广播接收功能,UDP监听线程无法创建!", Toast.LENGTH_SHORT).show();
					}
        		}
        		//mThreadClient = new Thread(mRunnable);  
			} 
    		catch (Exception e) 
    		{
    			Toast.makeText(mHomeActivity, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
			}
    	}
    }
    
    public void DestroyListener()
    {
		try 
		{
    		if(m_ReceiverThread!=null) m_ReceiverThread.interrupt();
			if(m_DetectorTHread!=null) m_DetectorTHread.interrupt();
			
			ThreadRun=false;
			m_ReceiverThread=null;
			m_DetectorTHread=null;
		} 
		catch (Exception e) 
		{
			Toast.makeText(mHomeActivity, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
		}
    }
	
	//线程:向设备发送查询数据
	private Runnable	m_Detector	= new Runnable() 
	{
		public void run()
		{
			Looper.prepare();
			Log.i("_DEBUG","Detector Thread!");
	        try 
	        {
				while (demon.Connected()&&ThreadRun)
				{
					try
					{
						for(int i=0;i<getDeviceStatus().size();i++)
						{
							byte devid=getDeviceStatus().get(i);
							demon.fetchRelayStatus(devid);
						}
						
					}
					catch (Exception e)
					{
						Toast.makeText(mHomeActivity,e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
					}
					Thread.sleep(Ethnet.THREAD_TIMEOUT);
				}
			} 
	        catch (Exception e) 
	        {
	        	Toast.makeText(mHomeActivity, "无法建立侦听线程！", Toast.LENGTH_SHORT).show();
			}
		}
	};
	//线程:监听服务器发来的消息
	private Runnable	m_TCP_Receiver	= new Runnable() 
	{
		public void run()
		{
			try 
			{
				Looper.prepare();
				BufferedInputStream mBufferedReaderClient=null;
				if(demon.Connected())
				{
					mBufferedReaderClient = new BufferedInputStream(new DataInputStream(demon.getSocket().getInputStream()));				
				}
				byte[] buffer = new byte[8];
				while (demon.Connected()&&ThreadRun)
				{
					try
					{
						int readLen = 0;
						if((readLen = mBufferedReaderClient.read(buffer))>0)
						{	
							Bundle bundle = new Bundle();
							Message msg = new Message();
				            msg.what = Ethnet.MSG_UPDATE_STATUS;
				            bundle.putByteArray("data", buffer);
				            msg.setData(bundle);
				            mHomeActivity.mHandler.sendMessage(msg);
						}
					}
					catch (Exception e)
					{
						Toast.makeText(mHomeActivity, "无法建立侦听线程！", Toast.LENGTH_SHORT).show();
					}
				}
				if(mBufferedReaderClient!=null)	mBufferedReaderClient.close();
				mBufferedReaderClient=null;
			} catch (Exception e) {
				// TODO: handle exception
			}

		}
	};

	//线程:监听服务器发来的消息
	private Runnable	m_UDP_Receiver	= new Runnable() 
	{
		private WifiManager.MulticastLock lock;

		public void run()
		{
			try 
			{
				Looper.prepare();
				Log.i("_DEBUG","UDP Thread!");
				this.lock= wifimanager.createMulticastLock("UDPWIFI"); 
				while (demon.Connected()&&ThreadRun)
				{
					try
					{
						this.lock.acquire();
						byte[] buffer=demon.ReceviedByUdp();
						this.lock.release();
						
						Bundle bundle = new Bundle();
						Message msg = new Message();
			            msg.what = Ethnet.MSG_UPDATE_STATUS;
			            bundle.putByteArray("data", buffer);
			            msg.setData(bundle);
			            mHomeActivity.mHandler.sendMessage(msg);
			            Log.v("_DEBUG","UDP Recevied!");
					}
					catch (Exception e)
					{
						Log.e("_DEBUG","UDP Recevied failed!");
						//Toast.makeText(mHomeActivity, "无法建立侦听线程！", Toast.LENGTH_SHORT).show();
					}
				}
			} catch (Exception e) {
				// TODO: handle exception
			}

		}
	};
}
