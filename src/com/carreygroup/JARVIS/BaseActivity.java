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

//import com.mobclick.android.MobclickAgent;
import com.carreygroup.JARVIS.R;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.RelativeLayout;

/**
 *
 * @author firefish
 */
public class BaseActivity extends Activity implements OnClickListener{
    
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        requestWindowFeature(Window.FEATURE_NO_TITLE);        
    }
    
    @Override
    public void onStart(){
    	super.onStart();
    	RelativeLayout header = (RelativeLayout)findViewById(R.id.header);
    	//if(header != null)
    	//	((RelativeLayout)header.getParent()).setBackgroundResource(((MyApplication)getApplication()).getBackgroundResId());
    }
    
    @Override
    public void onResume() { 
        super.onResume(); 
        //MobclickAgent.onResume(this);
    }
    
    @Override
    public void onPause() { 
        super.onPause(); 
        //MobclickAgent.onPause(this); 
    }     
    
   
    /*
    protected void setCaption(String strCaption){
        TextView txtCaption = (TextView) findViewById(R.id.txtCaption); 
        txtCaption.setText(strCaption);
    }

    protected void showBackButton(boolean isShow){        
        TextView btnBack = (TextView) findViewById(R.id.btnBack);        
        btnBack.setOnClickListener(this);
        
        if(isShow)
        	btnBack.setVisibility(View.VISIBLE);
        else
        	btnBack.setVisibility(View.GONE);     
    }
 
    protected void showActionButton(String strAction){        
        ImageView imgLogo = (ImageView) findViewById(R.id.logo);
        imgLogo.setVisibility(View.INVISIBLE);  
        
        TextView btnAction = (TextView) findViewById(R.id.btnAction);        
        btnAction.setVisibility(View.VISIBLE);
        btnAction.setText(strAction); 
    }
    */ 
    @Override
	public void onClick(View view) {
        finish();
    	//onKeyDown(KeyEvent.KEYCODE_BACK, null);
    }  
     
}
