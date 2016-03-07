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


import android.app.ActivityGroup;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.net.UnknownHostException;

//import com.mobclick.android.MobclickAgent;
import com.carreygroup.JARVIS.Demon.ConnectionListener;


public class MainActivity extends ActivityGroup implements ConnectionListener  
{

    private LinearLayout 		container	= null;
    private RelativeLayout 		layConnect	= null;
    private ImageView 			mImgConnectionStatus 	= null;
    private TextView 			btnHome		= null;
    private TextView 			btnScenes	= null;
    private TextView 			btnItems	= null;
    private TextView 			btnAbout	= null;
    
    private Intent				intentHome	= null;
    private Intent				intentScenes	= null;
    private Intent				intentItems	= null;
    private Intent				intentAbout	= null;
    
    private View				viewHome	= null;
    private View				viewScenes	= null;
    private View				viewItems	= null;
    private View				viewAbout	= null;
    
/*    private WifiManager 		wifiManager	= null;
    private ProgressDialog 		pdOpenWifi	= null;    */
    
    MyApplication 				mApp 		= null;    

    @Override
    protected void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        // 隐藏标题栏
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.main);
        
        //MobclickAgent.onError(this);
        
        mApp = (MyApplication)getApplication();
        intentHome = new Intent(MainActivity.this, HomeActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intentScenes = new Intent(MainActivity.this, ScenesActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intentItems = new Intent(MainActivity.this, ItemsActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intentAbout = new Intent(MainActivity.this, AboutActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        // 按钮
        btnHome = (TextView)findViewById(R.id.btnHome);
        btnScenes = (TextView)findViewById(R.id.btnScenes); 
        btnItems = (TextView)findViewById(R.id.btnItems);
        btnAbout = (TextView)findViewById(R.id.btnAbout);  
        
        int width = getWindowManager().getDefaultDisplay().getWidth()/4;
        btnHome.setWidth(width);
        btnScenes.setWidth(width);
        btnItems.setWidth(width);
        btnAbout.setWidth(width);

        btnHome.setOnClickListener(new OnHomeClicked());
        btnScenes.setOnClickListener(new OnScenesClicked());       
        btnItems.setOnClickListener(new OnSetupClicked());
        btnAbout.setOnClickListener(new OnAboutClicked());

        container = (LinearLayout) findViewById(R.id.container);   
        layConnect = (RelativeLayout)findViewById(R.id.layConnect);
        
        mImgConnectionStatus = (ImageView)findViewById(R.id.ConnectionStatus);
        mImgConnectionStatus.setOnClickListener(new OnStatusClicked());
        
        Button btnConnect = (Button)findViewById(R.id.btnConnect);
        btnConnect.setOnClickListener(new OnConnectClicked());
        
        //ip,port数据
        EditText edtIP = (EditText)findViewById(R.id.edtIP);
        EditText edtPort = (EditText)findViewById(R.id.edtPort);

        edtIP.setText(mApp.getHostIP());
        edtPort.setText(mApp.getPort()+"");
        
        mApp.getDemon().AddConnectionListener((ConnectionListener)this); 
        
        new OnHomeClicked().onClick(null);
    }
    
    /*
    @Override
    public void onPause(){    
    	super.onPause();
    	
    	// 断开连接    	
    	//TCP tcp = app.getConnection();
		//try {
		//	if(tcp != null && tcp.isConnected())
		//		tcp.close();
		//} catch (IOException e) {
		//	Toast.makeText(MainActivity.this, "断开控制器连接失败！", Toast.LENGTH_SHORT).show();
		//}
		
    	
    	// 添加通知栏图标
		CharSequence from = mApp.getText(R.string.app_name);  
		CharSequence msg = mApp.getText(R.string.notification_msg);
		CharSequence desc = mApp.getText(R.string.notification_desc);
		          
		Intent intent = new Intent();  
		ComponentName componentName = new ComponentName("com.carreygroup.JARVIS","com.carreygroup.JARVIS.MainActivity");  
		intent.setComponent(componentName);  
		intent.setAction("android.intent.action.MAIN");  
		intent.addCategory("android.intent.category.LAUNCHER");  
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);  
		intent.addFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);  

		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, intent, 0);  
		Notification notif = new Notification(R.drawable.icon, msg, System.currentTimeMillis());  
		notif.setLatestEventInfo(this, from, desc, contentIntent);  
		notif.flags |= Notification.FLAG_ONGOING_EVENT;

		NotificationManager nm = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);  
		nm.notify(R.string.app_name, notif);    
    }
    
    
    @Override
    public void onResume(){    
    	super.onResume();
    	
    	// 移除图标
    	NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);  
        nm.cancel(R.string.app_name);   	
    }
    */
    
	@Override
	public void onConnected(Demon connection) 
	{
		Log.i("_DEBUG", "MainActivity onConnected!");
		mImgConnectionStatus.setImageResource(R.drawable.ico_connected);
		mApp.CreateListener();
	}

	@Override
	public void onDisconnected() 
	{
		Log.i("_DEBUG", "MainActivity onDisconnected!");
		mApp.DestroyListener();
		mImgConnectionStatus.setImageResource(R.drawable.ico_disconnected);	
	}    
    
    /**
     * 连接状态图标点击事件
     * @author firefish
     *
     */
    private class OnStatusClicked implements OnClickListener 
    {
    	@Override
    	public void onClick(View v) 
    	{
    		RadioGroup ethnetgroup = (RadioGroup)findViewById(R.id.EthnetGroup);
    		if(layConnect.isShown())
    			layConnect.setVisibility(View.GONE);
    		else
    		{
    	        switch(mApp.getEthnetMode())
    	        {
    		        case 0:
    		        	ethnetgroup.check(R.id.rbTCP);
    		        	((EditText)findViewById(R.id.edtPort)).setInputType(0x90);//密码明文显示
    		        	break;
    		        case 1:
    		        	ethnetgroup.check(R.id.rbUDP);
    		        	((EditText)findViewById(R.id.edtPort)).setInputType(0x90);//密码明文显示
    		        	break;
    		        case 2:
    		        	ethnetgroup.check(R.id.rbP2P);
    		        	((EditText)findViewById(R.id.edtPort)).setInputType(0x81);//密码隐藏
    		        	break;
    	        	default:
    	        		ethnetgroup.check(R.id.rbTCP);
    	        		((EditText)findViewById(R.id.edtPort)).setInputType(0x90);//密码明文显示
    		        	break;	        
    	        }
    	        
    	        ethnetgroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()
    	        {
    				@Override
    				public void onCheckedChanged(RadioGroup group, int checkedId) 
    				{
    					RadioButton ethnetgroup = (RadioButton)group.findViewById(checkedId);
    					int ethmode = Integer.valueOf((String)ethnetgroup.getTag());
    					
						TextView lblIP=(TextView)findViewById(R.id.lblIP);
    					TextView lblPort=(TextView)findViewById(R.id.lblPort);
		
    					if(ethmode==Ethnet.P2P)
    					{
        					lblIP.setText("用户名:");
        					lblPort.setText("密码:");
        					((EditText)findViewById(R.id.edtPort)).setInputType(0x81);//密码隐藏
    					}
    					else 
    					{
        					lblIP.setText("以太网地址:");
        					lblPort.setText("端口:");
        					((EditText)findViewById(R.id.edtIP)).setText("");
        					((EditText)findViewById(R.id.edtPort)).setText("");
        					((EditText)findViewById(R.id.edtPort)).setInputType(0x90);//密码明文显示
						}
    					mApp.setEthnetMode(ethmode);
    				}
    	        	
    	        });
    	        layConnect.setVisibility(View.VISIBLE);
    		}
    			
    		
    		Button btnConnect = (Button)findViewById(R.id.btnConnect);
    		
    		if(mApp.getDemon().Connected())
    		{
    			RadioButton bTCP = (RadioButton)ethnetgroup.findViewById(R.id.rbTCP);
    			RadioButton bUDP = (RadioButton)ethnetgroup.findViewById(R.id.rbUDP);
    			RadioButton bP2P = (RadioButton)ethnetgroup.findViewById(R.id.rbP2P);
    			
    			bTCP.setClickable(false);
    			bUDP.setClickable(false);
    			bP2P.setClickable(false);
    			btnConnect.setText("断开");
    		}
    		else
    		{
    			RadioButton bTCP = (RadioButton)ethnetgroup.findViewById(R.id.rbTCP);
    			RadioButton bUDP = (RadioButton)ethnetgroup.findViewById(R.id.rbUDP);
    			RadioButton bP2P = (RadioButton)ethnetgroup.findViewById(R.id.rbP2P);
    			
    			bTCP.setClickable(true);
    			bUDP.setClickable(true);
    			bP2P.setClickable(true);
    			btnConnect.setText("连接");
    		}
    	}
    }    
    
    /**
     * 连接按钮点击事件
     *
     */    
    private class OnConnectClicked implements OnClickListener 
    {
    	@Override
    	public void onClick(View v) 
    	{
    		if(mApp.getDemon().Connected())
    		{   
    			// 断开连接
    			try 
    			{
					mApp.getDemon().Close();
				} 
    			catch (IOException e) 
				{
    				Toast.makeText(MainActivity.this, "断开控制器连接失败！", Toast.LENGTH_SHORT).show();    				
    				return;
				}
    		}
    		else
    		{
				try 
				{
					String strIpAddr = ((EditText)findViewById(R.id.edtIP)).getText().toString();  					
					// 建立连接    			  	
					String Argv2 = ((EditText)findViewById(R.id.edtPort)).getText().toString();
					
		    		boolean ret=mApp.getDemon().Connection((byte)mApp.getEthnetMode(),strIpAddr,Argv2);
		    		if(ret == false)
		    		{
						Toast.makeText(MainActivity.this, "连接控制器失败！", Toast.LENGTH_SHORT).show();
						return;
					}
		    		mApp.saveHostIP(strIpAddr);
		    		mApp.saveHostPort(Argv2);
		    		mApp.saveEthnetMode();
				}
				catch (UnknownHostException e) 
				{
		            e.printStackTrace();
				}
    		}
			layConnect.setVisibility(View.GONE);		
    	}
    }  
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) 
    {
        //按下键盘上返回按钮
        if(keyCode == KeyEvent.KEYCODE_BACK)
        {
    		if(layConnect.isShown())
    		{
    			layConnect.setVisibility(View.GONE);
    		}
    		else
    		{
	            new AlertDialog.Builder(this)
                //.setIcon(R.drawable.services)
                .setTitle("提示")
                .setMessage("确定要退出吗？")
                .setNegativeButton("取消", new DialogInterface.OnClickListener() 
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which) 
                    {
                    
                    }
                })
                .setPositiveButton("是的", new DialogInterface.OnClickListener() 
                {
                    @Override
					public void onClick(DialogInterface dialog, int whichButton) 
                    {
                        finish();
                    }
                }).show();	     			
    		}
    		
        	return true;
        }else{
            return super.onKeyDown(keyCode, event);
        }
    }    

    /*
   class WifiLoader extends AsyncTask<Void, Void, Integer>{
        private ConnectivityManager connManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);    
        NetworkInfo.State state = null;
        
        @Override
        protected Integer doInBackground(Void... params) {   
            do{
                state = connManager.getNetworkInfo(1).getState();
            }while(state != NetworkInfo.State.CONNECTED / * && state != NetworkInfo.State.CONNECTING* /);
            
            return null;
        }  
        
        @Override
        protected void onPreExecute() {
            wifiManager.setWifiEnabled(true);
            pdOpenWifi = ProgressDialog.show(MainActivity.this, null, getResources().getString(R.string.wifi_opening_desc), true);
            pdOpenWifi.setOnCancelListener(new DialogInterface.OnCancelListener() {
                     @Override
					public void onCancel(DialogInterface dialog) {
                         finish();
                     }
                });
        }
        
        @Override
        protected void onPostExecute(Integer result) {
            pdOpenWifi.dismiss();
            addHost(false);
        }
    }    

    @Override
    protected void onDestroy() {
        // 关闭wifi
        wifiManager.setWifiEnabled(false);
        
        super.onDestroy();
        System.exit(0);
    } 
    */
    
    private class OnHomeClicked implements Button.OnClickListener
    {
        @Override
		public void onClick(View view) 
        {
        	if(viewHome == null)
        	{        		
        		viewHome = getLocalActivityManager().startActivity("ModuleHome",intentHome).getDecorView();
        	}
        	
            container.removeAllViews();
            btnScenes.setBackgroundResource(0);
            btnItems.setBackgroundResource(0);
            btnAbout.setBackgroundResource(0);
            btnHome.setBackgroundResource(R.drawable.bg_toolbar_selected);
            btnHome.setPadding(0, 0, 0, 0);
            container.addView(viewHome);
        }
    }
    
    private class OnScenesClicked implements Button.OnClickListener
    {
        @Override
		public void onClick(View view) 
        {
        	if(viewScenes == null)
        	{
        		viewScenes = getLocalActivityManager().startActivity("ModuleScenes",intentScenes).getDecorView();
        	}
        	
            container.removeAllViews();
            btnHome.setBackgroundResource(0);
            btnItems.setBackgroundResource(0);
            btnAbout.setBackgroundResource(0);
            btnScenes.setBackgroundResource(R.drawable.bg_toolbar_selected);
            btnScenes.setPadding(0, 0, 0, 0);
            container.addView(viewScenes);
        }
    }

    private class OnSetupClicked implements Button.OnClickListener
    {
        @Override
		public void onClick(View view) 
        {
        	if(viewItems == null)
        	{
        		viewItems = getLocalActivityManager().startActivity("ModuleSetup",intentItems).getDecorView();
        	}
        	
            container.removeAllViews();
            btnHome.setBackgroundResource(0);
            btnScenes.setBackgroundResource(0);
            btnAbout.setBackgroundResource(0);
            btnItems.setBackgroundResource(R.drawable.bg_toolbar_selected);
            btnItems.setPadding(0, 0, 0, 0);
            container.addView(viewItems);
        }
    }

    private class OnAboutClicked implements Button.OnClickListener
    {
        @Override
		public void onClick(View view) 
        {
        	if(viewAbout == null)
        	{
        		viewAbout = getLocalActivityManager().startActivity("ModuleAbout",intentAbout).getDecorView();
        	}
        	
            container.removeAllViews();
            btnHome.setBackgroundResource(0);
            btnScenes.setBackgroundResource(0);
            btnItems.setBackgroundResource(0);
            btnAbout.setBackgroundResource(R.drawable.bg_toolbar_selected);
            btnAbout.setPadding(0, 0, 0, 0);
            container.addView(viewAbout);
        }
    }    
}
