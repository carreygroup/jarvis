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
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Observable;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.util.Log;


/*public void onCreate() 
{//用于创建线程
    WifiManager manager = (WifiManager) this
            .getSystemService(Context.WIFI_SERVICE);
    udphelper = new UdpHelper(manager);
    
    //传递WifiManager对象，以便在UDPHelper类里面使用MulticastLock
    udphelper.addObserver(MsgReceiveService.this);
    tReceived = new Thread(udphelper);
    tReceived.start();
    super.onCreate();
}*/

/**
 * 
 * UdpHelper帮助类
 * 
 * @author 陈喆榕
 * 
 */
public class UdpHelper  implements Runnable 
{
    public    Boolean IsThreadDisable = false;//指示监听线程是否终止
    private static WifiManager.MulticastLock lock;
    InetAddress mInetAddress;
    

    
    public UdpHelper(WifiManager manager) 
    {
         this.lock= manager.createMulticastLock("UDPwifi"); 
    }
    public void StartListen()  
    {
        // UDP服务器监听的端口
        Integer port = 8903;
        // 接收的字节大小，客户端发送的数据不能超过这个大小
        byte[] message = new byte[100];
        try {
            // 建立Socket连接
            DatagramSocket datagramSocket = new DatagramSocket(port);
            datagramSocket.setBroadcast(true);
            DatagramPacket datagramPacket = new DatagramPacket(message,
                    message.length);
            try {
                while (!IsThreadDisable) {
                    // 准备接收数据
                    Log.d("UDP Demo", "准备接受");
                     this.lock.acquire();
                     
                    datagramSocket.receive(datagramPacket);
                    String strMsg=new String(datagramPacket.getData()).trim();
                    Log.d("UDP Demo", datagramPacket.getAddress()
                            .getHostAddress().toString()
                            + ":" +strMsg );
                    this.lock.release();
                }
            } catch (IOException e) {//IOException
                e.printStackTrace();
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }

    }
    public static void send(String message) 
    {
        message = (message == null ? "Hello IdeasAndroid!" : message);
        int server_port = 8904;
        Log.d("UDP Demo", "UDP发送数据:"+message);
        DatagramSocket s = null;
        try 
        {
            s = new DatagramSocket();
        } 
        catch (SocketException e) {
            e.printStackTrace();
        }
        InetAddress local = null;
        try {
            local = InetAddress.getByName("255.255.255.255");
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        int msg_length = message.length();
        byte[] messageByte = message.getBytes();
        DatagramPacket p = new DatagramPacket(messageByte, msg_length, local,
                server_port);
        try {

            s.send(p);
            s.close();
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
            StartListen();
    }
}