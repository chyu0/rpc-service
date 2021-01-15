package com.cy.rpc.client.cache;

import org.springframework.util.CollectionUtils;

import java.util.*;

/**
 * @author chenyu3
 * 保存所有service
 */
public class ServiceCache {

    private static final Set<String> interfaceCaches = Collections.synchronizedSet(new HashSet<>());

    private static final Map<String, Set<String>> appClientCaches = Collections.synchronizedMap(new HashMap<>());

    public static void putInterface(String serviceName) {
        interfaceCaches.add(serviceName);
    }

    public static Set<String> getInterfaceCaches() {
        return interfaceCaches;
    }

    public static void putAppCaches(String appId, String hostname) {
        appClientCaches.computeIfAbsent(appId, k -> new HashSet<>()).add(hostname);
    }

    public static void putAllAppCaches(String appId, List<String> hostnames) {
        if(!CollectionUtils.isEmpty(hostnames)) {
            appClientCaches.computeIfAbsent(appId, k -> new HashSet<>()).addAll(hostnames);
        }
    }


    public static Set<String> getAppCaches(String appId) {
        return appClientCaches.get(appId);
    }


    public static void removeAppCache(String appId, String hostname) {
        if(appClientCaches.get(appId) != null) {
            appClientCaches.get(appId).remove(hostname);
            if(appClientCaches.get(appId).size() == 0) {
                appClientCaches.remove(appId);
            }
        }
    }

    public static void removeAppCache(String appId) {
        if(appClientCaches.get(appId) != null) {
            appClientCaches.remove(appId);
        }
    }

    public static Set<String> getAppCaches() {
        return appClientCaches.keySet();
    }

}
