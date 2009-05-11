package com.protocol7.tcpproxy;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class Proxy {
    private static class Pump extends Thread {

        private InputStream in;
        private OutputStream out;
        
        public Pump(InputStream in, OutputStream out) {
            this.in = in;
            this.out = out;
        }

        @Override
        public void run() {
            try {
                byte[] buffer = new byte[1024];
                int read = in.read(buffer);

                while(read > -1) {
                    out.write(buffer, 0, read);
                    if(read > 0) {
                        String s = new String(buffer).trim();
                        if(s.length() > 0) {
                            System.out.println(s);
                        }
                    }
                    read = in.read(buffer);
                }
                System.out.println("Pump done, exiting");
            } catch (IOException e) {
                System.err.println("Pump failed, exiting");
            }
        }
    }
    
    public static void main(String[] args) throws Exception {
        final int localPort = Integer.parseInt(args[0]);
        final InetAddress remoteAddress = InetAddress.getByName(args[1]);
        final int remotePort = Integer.parseInt(args[2]);

        ServerSocket ss = new ServerSocket(localPort);
     
        while(true) {
            System.out.println("Proxy waiting for connection");
            final Socket inSocket = ss.accept();

            System.out.println("Starting new proxy");
            new Thread(){
                @Override
                public void run() {
                    try {
                        Socket outSocket = new Socket(remoteAddress, remotePort);
                        System.out.println("Connected to remote server");
                        
                        new Pump(inSocket.getInputStream(), outSocket.getOutputStream()).start();
                        new Pump(outSocket.getInputStream(), inSocket.getOutputStream()).start();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }.start();
        }
    }
}
