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

import java.util.ArrayList;

import com.carreygroup.JARVIS.Demon.ParseDataException;
import com.carreygroup.JARVIS.Demon.SendDataException;
import com.carreygroup.JARVIS.R;
import com.carreygroup.JARVIS.Demon.ConnectionListener;

import android.R.bool;
import android.R.integer;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class HomeActivity extends BaseActivity implements ConnectionListener 
{
    private MyAdapter 				mAdapter	= null;	   
    private MyApplication 			mApp		= null;
    
    @SuppressLint("HandlerLeak")
	public Handler mHandler = new Handler()
	{		
		  public void handleMessage(Message msg)										
		  {											
			  super.handleMessage(msg);
			  
			  Log.v("_DEBUG","ReceviedByUdp");
			  if(msg.what == Ethnet.MSG_UPDATE_STATUS)
			  {
				  byte[] data=msg.getData().getByteArray("data");
				  try
				  {
					  Packet rp=new Packet();
					  if(rp.CheckPacket(data))
					  {
						  
						  mAdapter.turn(rp.getDevID(), rp.getData());
					  }
				  }
				  catch (Exception e)
				  {
					  
				  }
			  }
			  if(msg.what==Ethnet.MSG_STATUS_TIMEOUT)
			  {
				  Log.v("_DEBUG","MSG_STATUS_TIMEOUT");
			  }
		  }									
	 };
	 
	@Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home);
        
        mApp = ((MyApplication)getApplication());
        mApp.getDemon().AddConnectionListener((ConnectionListener)this); 

        mAdapter = new MyAdapter(this);
        GridView mGvItems = (GridView) findViewById(R.id.gvItems);
        mGvItems.setOnItemClickListener(new OnItemClicked());
        mGvItems.setAdapter(mAdapter);
        mApp.setHomeActivity(this);
        

    }    
    
    @Override
    public void onStart()
    {
    	super.onStart();
    	//updateStatus();
    	//mApp.CreateListener();
    }
    
    private class OnItemClicked implements GridView.OnItemClickListener
    {
        public void onItemClick(AdapterView<?> av, View view, int i, long l) 
        {        	
            /*if(mTcp == null)*/
        	if(!mApp.getDemon().Connected())
            {
            	Toast.makeText(HomeActivity.this, "请先连接控制器！", Toast.LENGTH_SHORT).show();
            	return;
            }
            
    		// 发送开关命令
            boolean status = mAdapter.isOn(i); // 获取当前状态
            byte devid=mAdapter.getDevID(i);
            byte relay=mAdapter.getRelay(i);
            try 
            {
            	status = mApp.getDemon().switchRelay(devid,relay, !status); // 反转状态
			} 
            catch (SendDataException e) 
            {
				Toast.makeText(HomeActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
				return;
			} 
            catch (ParseDataException e) 
            {
				Toast.makeText(HomeActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
				return;
			}	
			
			// 更新图标
/*			if(status)
				mAdapter.turnOn(i);
			else
				mAdapter.turnOff(i);*/
        }   
    }      

/*	private void updateStatus() 
	{
		if(!mApp.getDemon().Connected()) return;
		// 取所有继电器状态并更新界面显示
		try 
		{
			for(int i=0;i<mApp.getDeviceStatus().size();i++)
			{
				//获取状态数据
				byte devid=mApp.getDeviceStatus().get(i);
				int Status = mApp.getDemon().fetchRelayStatus(devid);
				//Status=0xFFFF;
				//更新状态
				mAdapter.turn(devid,Status);				
			}
		} 
		catch (SendDataException e) 
		{
			Toast.makeText(HomeActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
			return;
		} 
		catch (ParseDataException e) 
		{
			Toast.makeText(HomeActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
			return;
		}
	} */   

	@Override
	public void onConnected(Demon connection) 
	{
		//连接后更新状态图标
		//updateStatus();
		//mApp.CreateListener();
	}

	@Override
	public void onDisconnected() 
	{
		//mApp.DemonClose();
		//mApp.DestroyListener();
		if(mApp.getItems()!=null)
		{
			for(byte i=0;i<(mApp.getItems().size());i++)
			{
				mAdapter.turnOff(i);
			}
		}		
	}
    
	public void updateItems()
	{
		mAdapter.notifyDataSetChanged();		
	}
	
    private class MyAdapter extends BaseAdapter 
    {
        private LayoutInflater m_Inflater = null;
        
        private class ViewHolder 
        {
            public TextView Name;
            public ImageView Icon;
            //public ImageView Timer;
        }        
          
        public MyAdapter(Context context) 
        {
            m_Inflater = LayoutInflater.from(context);
        }  
          
        @Override  
        public int getCount() 
        {  
            if(mApp.getItems() == null)
                return 0;
            
            return mApp.getItems().size();  
        }  
  
        @Override  
        public Object getItem(int position) 
        {  
            return mApp.getItems().get(position);  
        }  
  
        @Override  
        public long getItemId(int position) 
        {  
            return mApp.getItems().get(position).GetID();  
        }  
  
        @Override  
        public View getView(int position, View convertView, ViewGroup parent) 
        {  
            ViewHolder holder = null;
            if (convertView == null) 
            {
                holder = new ViewHolder();
                convertView = m_Inflater.inflate(R.layout.items_grid, null);
                holder.Name = (TextView) convertView.findViewById(R.id.ItemName);
                holder.Icon = (ImageView) convertView.findViewById(R.id.Icon);
                //holder.Timer = (ImageView) convertView.findViewById(R.id.Timer);
                convertView.setTag(holder);
            } 
            else 
            {
                holder = (ViewHolder) convertView.getTag();
            }  

            holder.Name.setText(mApp.getItems().get(position).GetName());      
            
            if(mApp.getItems().get(position).GetStatus())
            	holder.Icon.setImageResource(mApp.getItems().get(position).GetIconResId());
            else
            {
            	Bitmap bmpOriginal = BitmapFactory.decodeResource(getResources(), mApp.getItems().get(position).GetIconResId());
            	holder.Icon.setImageBitmap(ImageTools.toGrayscale(bmpOriginal, 5));
            }
            /*
	        if(mItems.get(position).GetTimerOn() != "" && mItems.get(position).GetTimerOff() != "")
	        {
	        	holder.Timer.setVisibility(View.VISIBLE); 
	        }
            */
            return convertView;
        }
        
        public void turn(byte devid,byte relay, boolean status)
        {
        	mApp.SetItemStatus(devid, relay, status);
        	mAdapter.notifyDataSetChanged();
        }
        
        public void turn(byte devid,byte[] status)
        {
        	mApp.SetItemStatus(devid,status);
        	mAdapter.notifyDataSetChanged();
       	
        }
        
        public void turnOn(int position)
        {
        	mApp.getItems().get(position).SetStatus(true);
        	mAdapter.notifyDataSetChanged();
        }
        
        public void turnOff(int position)
        {
        	mApp.getItems().get(position).SetStatus(false);
        	mAdapter.notifyDataSetChanged();
        }     
        
        public boolean isOn(int position)
        {
        	if(mApp.getItems().get(position).GetStatus())
        		return true;
        	else
        		return false;
        }
        public byte getDevID(int position)
        {
        	return mApp.getItems().get(position).GetDevid();
        }
        public byte getRelay(int position)
        {
        	return mApp.getItems().get(position).GetRelay();
        }
    }
   
}