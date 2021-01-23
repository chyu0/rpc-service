package com.cy.rpc.client.cache;

import java.util.*;

/**
 * @author chenyu3
 * 保存所有service
 */
public class ServiceCache {

    private static final Set<String> interfaceCaches = Collections.synchronizedSet(new HashSet<>());

    private static final Map<String, Set<String>> appHostCaches = Collections.synchronizedMap(new HashMap<>());

    public static void putInterface(String serviceName) {
        interfaceCaches.add(serviceName);
    }

    public static Set<String> getInterfaceCaches() {
        return interfaceCaches;
    }

    public static void putAppCaches(String appName, String hostname) {
        appHostCaches.computeIfAbsent(appName, k -> new HashSet<>()).add(hostname);
    }

    public static Set<String> getAppCaches(String appName) {
        return appHostCaches.get(appName);
    }


    public static void removeAppCache(String appName, String hostname) {
        if(appHostCaches.get(appName) != null) {
            appHostCaches.get(appName).remove(hostname);
            if(appHostCaches.get(appName).size() == 0) {
                appHostCaches.remove(appName);
            }
        }
    }

}
