package com.cy.rpc.client.sockect;

import com.cy.rpc.client.Client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author chenyu3
 * 客户端工厂
 */
public class ClientFactory {

    private static final Map<String, List<Client>> services = new ConcurrentHashMap<>(new HashMap<>());

    public static void put(String appId, Client client) {
        services.computeIfAbsent(appId, k -> new ArrayList<>()).add(client);
    }

    public static List<Client> get(String appId){
        return services.get(appId);
    }


    public static void remove(String appId, Client client) {
        List<Client> list = services.get(appId);
        if(list != null) {
            list.remove(client);
        }
    }

}
