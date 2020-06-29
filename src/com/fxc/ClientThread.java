package com.fxc;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

public class ClientThread implements Runnable {

    private Socket clientSocket;
    private PrintWriter out;

    public ClientThread(int portNumber, String hostName) throws UnknownHostException, IOException{

        clientSocket = new Socket(hostName, portNumber);
        out = new PrintWriter(clientSocket.getOutputStream(),true);

    }

    public void writeToServer(String input) {
        out.println(input);
    }

    public void run() {
                System.out.println("Running!");
    }

}

