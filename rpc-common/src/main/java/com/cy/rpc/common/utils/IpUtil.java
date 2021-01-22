package com.cy.rpc.common.utils;

import java.net.*;
import java.util.Enumeration;

public class IpUtil {

    public static String getHostIP(){
        String tempIP = "127.0.0.1";
        try {
            tempIP = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        try{
            Enumeration<NetworkInterface> networks = NetworkInterface.getNetworkInterfaces();
            InetAddress ip = null;
            Enumeration<InetAddress> addrs;
            while (networks.hasMoreElements())
            {
                addrs = networks.nextElement().getInetAddresses();
                while (addrs.hasMoreElements())
                {
                    ip = addrs.nextElement();
                    if (ip instanceof Inet4Address
                            && ip.isSiteLocalAddress()
                            && !ip.getHostAddress().equals(tempIP))
                    {
                        return ip.getHostAddress();
                    }
                }
            }

            return tempIP;
        } catch(Exception e){
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        System.out.println(getHostIP());
    }
}
