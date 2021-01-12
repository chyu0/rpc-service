package com.cy.rpc.client.properties;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

/**
 * @author chenyu3
 * service bean name重写，考虑到重复bean情况，没有重复bean name不需要定义，会按照默认配置生效
 */
@Slf4j
@ConfigurationProperties(prefix = "rpc.beans.name")
@Setter
@Component
public class RpcClientBeanNameOverrideProperties {

    private Map<String, String> override;

    /**
     * 读取重写的class bean
     * @param clazz
     * @return
     */
    public RpcServiceOverride getOverride(String clazz) {
        String overrideValue = override.get(clazz);
        if(StringUtils.isBlank(overrideValue)) {
            log.debug("读取rpc.beans.name.override为空！clazz:{}", clazz);
            return null;
        }

        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.readValue(overrideValue, RpcServiceOverride.class);
        } catch (IOException e) {
            log.error("读取rpc.beans.name.override失败", e);
            return null;
        }
    }
}
