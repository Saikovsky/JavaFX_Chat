package com.fxc;

import java.io.IOException;
import java.io.*;
import java.net.*;
import java.util.HashMap;

public class MulticastReceiverThread implements Runnable{

    private volatile HashMap<String, String> networkUsers = new HashMap<String,String>();
    private volatile HashMap<String, String> userIP = new HashMap<String,String>();

    public HashMap<String, String> getNetworkUsers(){
        return networkUsers;
    }
    public HashMap<String, String> getUserIP(){
        return userIP;
    }


    public void run(){
        try{
            InetAddress inetAddress = InetAddress.getLocalHost(); //get localhost info

            NetworkInterface nameInterface = NetworkInterface.getByInetAddress(inetAddress); //get network interface name
            NetworkInterface netIf = NetworkInterface.getByName(nameInterface.toString()); //get network interface by name

            InetAddress mcastaddr = InetAddress.getByName("228.5.6.7"); //addres for multicast
            InetSocketAddress group = new InetSocketAddress(mcastaddr, 6969); //addres and port to group

            MulticastSocket mcsock = new MulticastSocket(6969); //multicast socket
            mcsock.joinGroup(group, netIf);

            byte[] buf = new byte[1000];
            DatagramPacket receivedPacket = new DatagramPacket(buf, buf.length);
            while(true) {

                mcsock.receive(receivedPacket);
                String receiveString = new String(receivedPacket.getData(), 0, receivedPacket.getLength());

                String arr[] = receiveString.split(" ");
                userIP.put(arr[1],arr[2]);
                networkUsers.put(arr[1],arr[0]);

                Thread.sleep(50);
            }
            //mcsock.leaveGroup(group,netIf);
        }
        catch(IOException e)
        {
            System.out.println("IOException msg:"+e);
        }
        catch(InterruptedException e)
        {
            System.out.println("InterruptedException msg:"+e);
        }

    }

}
