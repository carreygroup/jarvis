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

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class ScenesActivity extends BaseActivity
{
	private ArrayList<SceneItem> 	mScenes		= null;
	private ListView				lvScenes	= null;
	private MyAdapter 				mAdapter	= null;	 
	private MyApplication 			mApp		= null;
    
    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.scenes);
        
        mApp = ((MyApplication)getApplication());
        mScenes = mApp.getScenes();
        
        lvScenes = (ListView)findViewById(R.id.lvScenes);
        lvScenes.addFooterView(buildFooter());
        mAdapter = new MyAdapter(this);
        lvScenes.setAdapter(mAdapter);  
    }
    
    @Override
    public void onDestroy()
    {
    	super.onDestroy();
    	
    	// 保存到配置文件 
    	for(int i=0; i<mScenes.size(); i++)
    	{
    		mApp.saveScene(mScenes.get(i));
    	}
    }    

    private View buildFooter() 
    {
        Button btn = new Button(this);  
        btn.setText("添加新场景");
        btn.setOnClickListener(new View.OnClickListener() 
        {
			
			@Override
			public void onClick(View v) 
			{
/*				if(mScenes.size() == 12)
				{
					Toast.makeText(ScenesActivity.this, "抱歉，不能在添加更多场景了。", Toast.LENGTH_SHORT).show();
					return;
				}*/
					
				SceneItem scene = new SceneItem((byte) mScenes.size(), "未命名场景", 1, 1);				
				mScenes.add(scene);
				mAdapter.notifyDataSetChanged();
			}
		});
        
        return(btn);  
	}

    private void switchScene(int devid,int scene)
    {
    	if(!mApp.getDemon().Connected())
        {
        	Toast.makeText(ScenesActivity.this, "请先连接控制器！", Toast.LENGTH_SHORT).show();
        	return;
        }
		// 发送命令
        try 
        {
        	mApp.getDemon().SendScene(devid,scene); // 发送执行场景命令
		} 
        catch (SendDataException e) 
        {
			Toast.makeText(ScenesActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
			return;
		} 
        catch (ParseDataException e) 
        {
			Toast.makeText(ScenesActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
			return;
		}
    }
	
    private class MyAdapter extends BaseAdapter 
    {
        private class ViewHolder 
        {
            public TextView Name;
/*            public TextView[] LineNames = new TextView[12];
            public TextView[] LineStatus = new TextView[12];*/
            public TextView Execute;
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
		public int getCount() 
        {
            return mScenes.size();
        }

        @Override
		public Object getItem(int i) 
        {
            return mScenes.get(i);
        }
        @Override
		public View getView(final int position, View convertView, ViewGroup parent) 
        {
            ViewHolder holder = null;
            if (convertView == null) 
            {
                holder = new ViewHolder();
                convertView = mInflater.inflate(R.layout.scene_item, null);
                holder.Name = (TextView) convertView.findViewById(R.id.SceneName);
                holder.Execute = (TextView) convertView.findViewById(R.id.Execute);
                holder.Delete = (TextView) convertView.findViewById(R.id.Delete);
                
                convertView.setTag(holder);
            } 
            else 
            {
                holder = (ViewHolder) convertView.getTag();
            }

            holder.Name.setText(mScenes.get(position).getName());
            
            // 执行按钮点击事件
        	holder.Execute.setOnClickListener(new View.OnClickListener() 
        	{

				@Override
				public void onClick(View v) 
				{
					switchScene(mScenes.get(position).getDevID(),mScenes.get(position).getSceneSN());
				}
        		
        	});
        	
/*            if(position>=2)
            { */   
            	// 删除按钮点击事件
            	holder.Delete.setOnClickListener(new View.OnClickListener() 
            	{

					@Override
					public void onClick(View v) 
					{
			            new AlertDialog.Builder(ScenesActivity.this)
		                .setTitle("提醒")
		                .setMessage("确定要删除该场景吗？")                    
		                .setPositiveButton("确定", new DialogInterface.OnClickListener() 
		                {
		                    public void onClick(DialogInterface dialog, int whichButton) 
		                    {
		                    	SceneItem scens = mScenes.get(position);
		                    	mApp.deleteScene(scens);
		                    	mScenes.remove(scens);
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
            	
            	// 修改名称
	            holder.Name.setOnClickListener(new View.OnClickListener() 
	            {
					
					@Override
					public void onClick(View v) 
					{
				        LayoutInflater inflater = getLayoutInflater();
				        View viewEdit = inflater.inflate(R.layout.scene_base, null);
				        
				        final EditText edtName = (EditText)viewEdit.findViewById(R.id.SceneName);
				        edtName.setText(mScenes.get(position).getName());				        

				        final EditText edtSceneDID = (EditText)viewEdit.findViewById(R.id.SceneDID);
				        edtSceneDID.setText(mScenes.get(position).getDevID()+"");	
				        
				        final EditText edtSceneSN = (EditText)viewEdit.findViewById(R.id.SID);
				        edtSceneSN.setText(mScenes.get(position).getSceneSN()+"");	
				        
				        new AlertDialog.Builder(ScenesActivity.this)
				        	.setTitle("修改场景名称")
				            .setPositiveButton("确定", new DialogInterface.OnClickListener() 
				            {
				                @Override
								public void onClick(DialogInterface dialog, int whichButton) 
				                {
				                	mScenes.get(position).setName(edtName.getText().toString());
				                	mScenes.get(position).setDevID(Integer.valueOf(edtSceneDID.getText().toString()));
				                	mScenes.get(position).setSceneSN(Integer.valueOf(edtSceneSN.getText().toString()));
				                	mAdapter.notifyDataSetChanged();
			                    }
				            })
				            .setNegativeButton("取消", new DialogInterface.OnClickListener() 
				            {
				                @Override
				                public void onClick(DialogInterface dialog, int which) {}
				            })
				            .setView(viewEdit).show();							
					}
				});
        	//}
/*            for(int i=0; i<12; i++)
            {
	            holder.LineNames[i].setText(mLines.get(i).GetName());
	            holder.LineStatus[i].setText(mScenes.get(position).getStatus(i) == 1 ? "开" : "关");
	            
	            holder.LineNames[i].setTag(i);
	            
	            if(position<2)
	            	continue;
	            
	            holder.LineNames[i].setOnClickListener(new View.OnClickListener() 
	            {
					
					@Override
					public void onClick(View v) 
					{						
						Integer index = (Integer)v.getTag();
						byte status = mScenes.get(position).getStatus(index);
						if(status == 0)
							status = 1;
						else
							status = 0;
						Log.d("onclick", index.toString());
						mScenes.get(position).setStatus(index, status);
						mAdapter.notifyDataSetChanged();
					}
				});
            }*/
            
            return convertView;
        }  
    }
}
