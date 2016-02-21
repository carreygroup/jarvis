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

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.widget.TextView;

public class AboutActivity extends BaseActivity {
    @Override 
    public void onCreate(Bundle icicle) {  
        super.onCreate(icicle);
        setContentView(R.layout.about);        
        
        TextView txtVersion = (TextView)this.findViewById(R.id.txtVersion);
        txtVersion.setText("版　　本：" + getVersion());
    }


    public String getVersion(){
        String packageName = "com.carreygroup.JARVIS"; // app的包名 
        String sv = "";
        try {
            PackageInfo pkinfo = getPackageManager().getPackageInfo(packageName, PackageManager.GET_CONFIGURATIONS);
            sv = pkinfo.versionName;            
        } catch (NameNotFoundException e) {

        }
        
        return sv;
    } 
}
