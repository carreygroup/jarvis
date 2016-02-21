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

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

public class ItemsActivity extends BaseActivity 
{
	private ArrayList<ItemInfo> mItems 		= null;
	private ListView 			lvConfig 	= null;
	private MyAdapter 			mAdapter 	= null;	 
	private MyApplication 		mApp		= null;
	
    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.items);
        
        mApp = ((MyApplication)getApplication());
        mItems = mApp.getItems();
        
        lvConfig = (ListView)findViewById(R.id.lvConfig);
        lvConfig.requestFocusFromTouch();
        lvConfig.setItemsCanFocus(true);
        lvConfig.setOnItemClickListener(new ItemClicked());
        lvConfig.addFooterView(buildFooter());//增加添加新项目按钮
        mAdapter = new MyAdapter(this);
        lvConfig.setAdapter(mAdapter);        
        
        //mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onDestroy()
    {
    	super.onDestroy();
    	
    	// 保存到配置文件 
    	for(int i=0; i<mItems.size(); i++)
    	{
    		mApp.saveItem(mItems.get(i));
    	}
    }  
    
    private View buildFooter() 
    {
        Button btn = new Button(this);  
        btn.setText("添加新项目");
        btn.setOnClickListener(new View.OnClickListener() 
        {
			
			@Override
			public void onClick(View v) 
			{
/*				if(mItems.size() == 12)
				{
					Toast.makeText(SetupActivity.this, "抱歉，不能在添加更多项目了。", Toast.LENGTH_SHORT).show();
					return;
				}*/
					
				ItemInfo Item = new ItemInfo((byte) mItems.size(),"未命名项目", 0);
				Item.SetDevid((byte) 1);
				Item.SetRelay((byte) 1);
				mItems.add(Item);
				mAdapter.notifyDataSetChanged();
			}
		});
        
        return(btn);  
	}
    
    private class ItemClicked implements ListView.OnItemClickListener
    {
    	private ItemInfo 	mItem 	= null;
    	private int 		mIconId = 0;

		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, final int arg2, long arg3) 
		{
			doSetup(arg2);
		}
		
		public void doSetup(final int pos)
		{
	        LayoutInflater inflater = getLayoutInflater();
	        mItem = mItems.get(pos);
	        View viewConfig = inflater.inflate(R.layout.item_base, null);
	        
	        final EditText edtName = (EditText)viewConfig.findViewById(R.id.ItemName);
	        edtName.setText(mItem.GetName());
	        
	        final EditText edtDID = (EditText)viewConfig.findViewById(R.id.ItemDID);
	        edtDID.setText(mItem.GetDevid()+"");
	        
	        final EditText edtRelay = (EditText)viewConfig.findViewById(R.id.Relay);
	        edtRelay.setText(mItem.GetRelay()+"");
	        RadioGroup icons = (RadioGroup)viewConfig.findViewById(R.id.IconGroup);
	        
	        switch(mItem.GetIcon())
	        {
		        case 0:
		        	icons.check(R.id.rbLight);
		        	break;
		        case 1:
		        	icons.check(R.id.rbTV);
		        	break;
		        case 2:
		        	icons.check(R.id.rbFan);
		        	break;
	        	default:
		        	icons.check(R.id.rbLight);
		        	break;	        
	        }
	        
	        icons.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()
	        {
				@Override
				public void onCheckedChanged(RadioGroup group, int checkedId) 
				{
					RadioButton icon = (RadioButton)group.findViewById(checkedId);
					mIconId = Integer.valueOf((String)icon.getTag());
				}
	        	
	        });

	        new AlertDialog.Builder(ItemsActivity.this)
	        	.setTitle("修改第" + (pos+1) + "路设置")
	            .setPositiveButton("确定", new DialogInterface.OnClickListener() 
	            {
	                @Override
					public void onClick(DialogInterface dialog, int whichButton) 
	                {
	                	mItem.SetName(edtName.getText().toString());
	                	mItem.SetDevid(Byte.valueOf(edtDID.getText().toString()));
	                	mItem.SetRelay(Byte.valueOf(edtRelay.getText().toString()));
	                	
	                	mItem.SetIcon(mIconId);
	                	mItems.set(pos, mItem);
	                	mAdapter.notifyDataSetChanged();
	                	// 保存到配置文件 
	                	mApp.saveItem(mItem);
	                	mApp.PushDevIDs(mItem.GetDevid());
	                	//更新修改后的UI元素	                	
	                	mApp.getHomeActivity().updateItems();
                    }
	            })
	            .setNegativeButton("取消", new DialogInterface.OnClickListener() 
	            {
	                @Override
	                public void onClick(DialogInterface dialog, int which) {}
	            })
	            .setView(viewConfig).show();			
		}
    	
    }
        
    private class MyAdapter extends BaseAdapter 
    {
        private class ViewHolder 
        {
            public TextView Name;
            public ImageView Icon;
            //public TextView TimerOn;
            //public TextView TimerOff;            
            public TextView viewConfig;
            //public TextView SetTimer;
            public TextView Edit;
            public TextView Delete;
        }

        private LayoutInflater mInflater;

        public MyAdapter(Context context) 
        {
            mInflater = LayoutInflater.from(context); 
        }
        @Override
		public long getItemId(int arg0) 
        {
            // TODO Auto-generated method stub
            return arg0;
        }
        @Override
		public int getCount() {
            return mItems.size();
        }

        @Override
		public Object getItem(int i) {
            return mItems.get(i);
        }
        @Override
		public View getView(final int position, View convertView, ViewGroup parent) 
        {
            ViewHolder holder = null;
            if (convertView == null) 
            {
                holder = new ViewHolder();
                convertView = mInflater.inflate(R.layout.item, null);
                holder.Name = (TextView) convertView.findViewById(R.id.ItemName);
                //holder.TimerOn = (TextView) convertView.findViewById(R.id.TimerOn);
                //holder.TimerOff = (TextView) convertView.findViewById(R.id.TimerOff);
                holder.viewConfig = (TextView) convertView.findViewById(R.id.ItemConfig);
                //holder.SetTimer = (TextView) convertView.findViewById(R.id.SetTimer);
                holder.Icon = (ImageView) convertView.findViewById(R.id.Icon);
                holder.Edit = (TextView) convertView.findViewById(R.id.ItemConfig);
                holder.Delete = (TextView) convertView.findViewById(R.id.RemoveItem);
                
                convertView.setTag(holder);
            } 
            else 
            {
                holder = (ViewHolder) convertView.getTag();
            }

            ItemInfo item = mItems.get(position);
            holder.Icon.setImageResource(item.GetIconResId());
            holder.Name.setText(item.GetName());
            /*
            if(mLines.get(position).GetTimerOn() != "")
            	holder.TimerOn.setText("定时开启时间:" + item.GetTimerOn());
            else
            	holder.TimerOn.setText("");
            if(mLines.get(position).GetTimerOff() != "")
            	holder.TimerOff.setText("定时关闭时间:" + item.GetTimerOff());            
            else
            	holder.TimerOff.setText("");
            */

            // 执行按钮点击事件
        	holder.Edit.setOnClickListener(new View.OnClickListener() 
        	{
				@Override
				public void onClick(View v) 
				{
					new ItemClicked().doSetup(position);					
				}
        		
        	});
        	
            	// 删除按钮点击事件
            	holder.Delete.setOnClickListener(new View.OnClickListener() 
            	{

					@Override
					public void onClick(View v) 
					{
			            new AlertDialog.Builder(ItemsActivity.this)
		                .setTitle("提醒")
		                .setMessage("确定要删除该项目吗？")                    
		                .setPositiveButton("确定", new DialogInterface.OnClickListener() 
		                {
		                    public void onClick(DialogInterface dialog, int whichButton) 
		                    {
		                    	ItemInfo item = mItems.get(position);
		                    	mApp.deleteItem(item);
		                    	mItems.remove(item);
		                    	mAdapter.notifyDataSetChanged();
		                    }
		                })
		                .setNegativeButton("取消", new DialogInterface.OnClickListener() 
		                {
		                    @Override
		                    public void onClick(DialogInterface dialog, int which) 
		                    {
		                        return;
		                    }
		                }).show(); 
					}
            		
            	});
            	
            // 基本设置点击事件
            holder.viewConfig.setOnClickListener(new View.OnClickListener() 
            {
				
				@Override
				public void onClick(View v) 
				{
					new ItemClicked().doSetup(position);					
				}
			});
            
            /*
            // 定时设置
            holder.SetTimer.setOnClickListener(new View.OnClickListener() 
            {
            	private ItemInfo 	mItem 	= null;
            	
				@Override
				public void onClick(View v) 
				{
			        LayoutInflater inflater = getLayoutInflater();
			        mItem = mLines.get(position);
			        View viewConfig = inflater.inflate(R.layout.config_timer, null);
			        final TimePicker tpOn = (TimePicker)viewConfig.findViewById(R.id.tpOn);
			        final TimePicker tpOff = (TimePicker)viewConfig.findViewById(R.id.tpOff);
			        
			        
			        if(mItem.GetTimerOn() != ""){
				        String[] time = mItem.GetTimerOn().split(":");
				        tpOn.setCurrentHour(Integer.parseInt(time[0]));
				        tpOn.setCurrentMinute(Integer.parseInt(time[1]));
			        }
			        
			        if(mItem.GetTimerOff() != ""){
				        String[] time = mItem.GetTimerOff().split(":");
				        tpOff.setCurrentHour(Integer.parseInt(time[0]));
				        tpOff.setCurrentMinute(Integer.parseInt(time[1]));
			        }			        
			        
			         *调整定时对话框
			        new AlertDialog.Builder(SetupActivity.this)
			        	.setTitle(mItem.GetName() + "定时设置")
			            .setPositiveButton("确定", new DialogInterface.OnClickListener() {
			                @Override
							public void onClick(DialogInterface dialog, int whichButton) {
			                	// TODO 写入控制板
			                	
			                	mItem.SetTimerOn(String.format("%2d:%2d:00", tpOn.getCurrentHour(), tpOn.getCurrentMinute()));
			                	mItem.SetTimerOff(String.format("%2d:%2d:00", tpOff.getCurrentHour(), tpOff.getCurrentMinute()));	
			                	mLines.set(position, mItem);
			                	mAdapter.notifyDataSetChanged();
			                	
			                	// 保存到配置文件 
			                	mApp.saveConfig(mItem);
		                    }
			            })
			            .setNegativeButton("取消", new DialogInterface.OnClickListener() {
			                @Override
			                public void onClick(DialogInterface dialog, int which) {}
			            })
			            .setView(viewConfig).show();
				}
			});
            */
            return convertView;
        }       

    }    
}
