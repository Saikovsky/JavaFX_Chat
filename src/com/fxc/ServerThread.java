package com.fxc;

import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.concurrent.Executors;

public class ServerThread implements Runnable{

    private int portNumber;
    public static ObservableMap<String, String> chatMsg;
    public static ObservableMap<String, ArrayList<String>> oldMsg;
    private static ArrayList<String> list;

    public ServerThread(int portNumber){
        this.portNumber = portNumber;
        chatMsg = FXCollections.observableHashMap();
        oldMsg = FXCollections.observableHashMap();
    }
    public void run(){
        var pool = Executors.newFixedThreadPool(5);
        try (var listener = new ServerSocket(portNumber)) {
            while (true) {
                pool.execute(new ServerHandler(listener.accept()));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class ServerHandler implements Runnable{

        private Socket socket;

        public ServerHandler(Socket socket){
            this.socket = socket;
        }
        public void run() {
            try(
                    BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            ) {
                String inputLine;
                while((inputLine=in.readLine())!=null)
                {
                    //Saving old msg
                    String arr[] = inputLine.split("-");
                    if(oldMsg.containsKey(arr[0])){
                        list = oldMsg.get(arr[0]);
                        list.add(arr[1]);
                    }
                    else{
                        list = new ArrayList<String>();
                        list.add(arr[1]);
                        oldMsg.put(arr[0],list);
                    }
                    //main container for msg
                    chatMsg.put(arr[0],arr[1]);
                }
                socket.close();
            }
            catch(IOException e)
            {
                System.out.println("Exception caught when trying to listen on port or listening for a connection");
                System.out.println(e.getMessage());
            }

        }

    }
}