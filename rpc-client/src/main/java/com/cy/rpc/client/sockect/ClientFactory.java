package com.cy.rpc.client.sockect;

import com.cy.rpc.client.Client;
import com.cy.rpc.common.enums.RpcErrorEnum;
import com.cy.rpc.common.exception.RpcException;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author chenyu3
 * 客户端工厂
 */
public class ClientFactory {

    private static final Map<String, List<Client>> services = new ConcurrentHashMap<>(new HashMap<>());

    public static void put(String appName, Client client) {
        services.computeIfAbsent(appName, k -> new ArrayList<>()).add(client);
    }

    public static List<Client> get(String appName){
        return services.get(appName);
    }

    /**
     * 移除appName客户端
     * @param appName
     * @return
     */
    public static void remove(String appName, Client client) {
        List<Client> list = services.get(appName);
        if(list != null) {
            list.remove(client);
        }
    }

    /**
     * 判断appName是否存在，已经创建过连接
     * @param appName
     * @param hostName
     * @return
     */
    public static boolean exist(String appName, String hostName) {
        List<Client> clients = get(appName);
        if(CollectionUtils.isEmpty(clients)) {
            return false;
        }
        for(Client client : clients) {
            String[] address = hostName.split(":");
            if(address.length != 2) {
                throw new RpcException(RpcErrorEnum.INNER_ERROR, appName + "下hostName不合法" + hostName);
            }

            if(client.getRemoteAddress().equals(address[0]) && client.getPort() == Integer.parseInt(address[1])) {
                return true;
            }
        }
        return false;
    }

}
