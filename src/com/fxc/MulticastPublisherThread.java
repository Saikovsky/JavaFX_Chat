package com.fxc;


import java.io.IOException;
import java.net.*;

public class MulticastPublisherThread implements Runnable{

    private String userNickname = null;
    private volatile int randNumber=(int)(Math.random() * ((45000 - 40000) + 1)) + 40000;

    public MulticastPublisherThread(String userNickname){
        this.userNickname = userNickname;
    }

    public int getRandomPort(){
        return randNumber;
    }

    public void run(){
        try{

            String secretCode = Integer.toString(randNumber);
            InetAddress inetAddress = InetAddress.getLocalHost();

            NetworkInterface nameInterface = NetworkInterface.getByInetAddress(inetAddress);
            InetAddress mcastaddr = InetAddress.getByName("228.5.6.7");

            InetSocketAddress group = new InetSocketAddress(mcastaddr, 6969);
            NetworkInterface netIf = NetworkInterface.getByName(nameInterface.toString());

            MulticastSocket mcsock = new MulticastSocket(6969);
            mcsock.joinGroup(group, netIf);

            String userName = userNickname;
            String sendString = userName +" "+secretCode+" "+ inetAddress.getHostAddress();
            byte[] sendBytes = sendString.getBytes("UTF-8");

            DatagramPacket myPacket = new DatagramPacket(sendBytes,sendBytes.length,mcastaddr,6969);
            while(true)
            {
                mcsock.send(myPacket);
                Thread.sleep(500);
            }
            //mcsock.leaveGroup(group,netIf);
        }
        catch(IOException e)
        {
            System.out.println("Message IOException "+ e);
        }
        catch(InterruptedException e)
        {
            System.out.println("Message InterruptedException "+ e);
        }

    }



}
