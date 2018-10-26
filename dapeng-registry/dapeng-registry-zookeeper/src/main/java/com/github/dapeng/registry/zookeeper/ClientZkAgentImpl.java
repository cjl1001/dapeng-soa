package com.github.dapeng.registry.zookeeper;


import com.github.dapeng.cookie.CookieRoute;
import com.github.dapeng.core.helper.SoaSystemEnvProperties;
import com.github.dapeng.router.Route;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @author lihuimin
 * @date 2017/12/24
 */
public class ClientZkAgentImpl implements ClientZkAgent {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClientZkAgentImpl.class);
    /**
     * 是否使用 灰度 zk
     */
    private final boolean usingFallbackZk = SoaSystemEnvProperties.SOA_ZOOKEEPER_FALLBACK_ISCONFIG;

    private ClientZk masterZk, fallbackZk;

    public ClientZkAgentImpl() {
        start();
    }

    @Override
    public void start() {
        masterZk = ClientZk.getMasterInstance();
        // fallback
        if (usingFallbackZk) {
            fallbackZk = ClientZk.getFallbackInstance();
        }
    }

    @Override
    public void stop() {
        if (masterZk != null) {
            masterZk.destroy();
        }

        if (fallbackZk != null) {
            fallbackZk.destroy();
        }

    }

    @Override
    public void cancelSyncService(ZkServiceInfo zkInfo) {
        LOGGER.info("cancelSyncService:[" + zkInfo.getService() + "]");
        zkInfo.setStatus(ZkServiceInfo.Status.CANCELED);
    }

    @Override
    public void syncService(ZkServiceInfo zkInfo) {
        //根据同一个zkInfo对象锁住即可
        synchronized (zkInfo) {
            if (zkInfo.getStatus() != ZkServiceInfo.Status.ACTIVE) {
                LOGGER.info(getClass().getSimpleName() + "::syncService[serviceName:" + zkInfo.getService() + "]:zkInfo just created, now sync with zk");
                masterZk.syncServiceZkInfo(zkInfo);
                if (zkInfo.getStatus() != ZkServiceInfo.Status.ACTIVE && usingFallbackZk) {
                    fallbackZk.syncServiceZkInfo(zkInfo);
                }

                LOGGER.info(getClass().getSimpleName() + "::syncService[serviceName:" + zkInfo.getService() + ", status:" + zkInfo.getStatus() + "]");
            }
        }

        if (zkInfo.getStatus() == ZkServiceInfo.Status.ACTIVE && zkInfo.getRuntimeInstances() != null) {

            LOGGER.info(getClass().getSimpleName() + "::syncService[serviceName:" + zkInfo.getService() + "]:zkInfo succeed");
        } else {
            LOGGER.info(getClass().getSimpleName() + "::syncService[serviceName:" + zkInfo.getService() + "]:zkInfo failed");
        }
    }

    @Override
    public List<Route> getRoutes(String service) {
        return masterZk.getRoutes(service);
    }

    @Override
    public List<CookieRoute> getCookieRoutes(String service) {
        return masterZk.getCookieRoutes(service);
    }
}
