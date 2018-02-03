package com.github.dapeng.monitor.filter;

import com.github.dapeng.basic.api.counter.CounterServiceClient;
import com.github.dapeng.basic.api.counter.domain.DataPoint;
import com.github.dapeng.core.SoaException;
import com.github.dapeng.core.SoaHeader;
import com.github.dapeng.core.TransactionContext;
import com.github.dapeng.core.filter.Filter;
import com.github.dapeng.core.filter.FilterChain;
import com.github.dapeng.core.filter.FilterContext;
import com.github.dapeng.monitor.config.MonitorProperties;
import com.github.dapeng.monitor.domain.ServiceSimpleInfo;
import com.github.dapeng.org.apache.thrift.TException;
import com.github.dapeng.util.SoaSystemEnvProperties;
import com.google.common.collect.ImmutableMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * author with struy.
 * Create by 2018/1/29 20:56
 * email :yq1724555319@gmail.com
 */

public class QpsFilter implements Filter {
    private static final Logger LOGGER = LoggerFactory.getLogger(QpsFilter.class);
    private static final int PERIOD = MonitorProperties.MONITOR_QPS_PERIOD;
    private static final String DATA_BASE = MonitorProperties.MONITOR_INFLUXDB_DATABASE;
    private static final String SERVER_IP = SoaSystemEnvProperties.SOA_CONTAINER_IP;
    private static final Integer SERVER_PORT = SoaSystemEnvProperties.SOA_CONTAINER_PORT;
    private Map<ServiceSimpleInfo, AtomicInteger> qpsStats = new ConcurrentHashMap<>(16);
    private final CounterServiceClient SERVICE_CLIENT = new CounterServiceClient();

    @Override
    public void onEntry(FilterContext ctx, FilterChain next) {
        SoaHeader soaHeader = TransactionContext.Factory.getCurrentInstance().getHeader();
        ServiceSimpleInfo simpleInfo = new ServiceSimpleInfo(soaHeader.getServiceName(), soaHeader.getMethodName(), soaHeader.getVersionName());

        if (qpsStats.containsKey(simpleInfo)) {
            qpsStats.get(simpleInfo).incrementAndGet();
        } else {
            qpsStats.put(simpleInfo, new AtomicInteger(1));
        }
        try {
            next.onEntry(ctx);
        } catch (TException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    @Override
    public void onExit(FilterContext ctx, FilterChain prev) {

    }


    public void init() {
        final AtomicInteger INDEX = new AtomicInteger(0);
        final Timer timer = new Timer("QpsFilter-Timer-" + INDEX.incrementAndGet());
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, 1);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        LOGGER.debug("QpsMonitorFilter 定时上送时间:{} 上送间隔:{}ms", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss S").format(calendar.getTime()), PERIOD * 1000);
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    final Calendar calendar = Calendar.getInstance();
                    final long millis = calendar.getTimeInMillis();
                    uploadQPSStat(millis, ImmutableMap.copyOf(qpsStats));
                } catch (Exception e) {
                    LOGGER.error(e.getMessage(), e);
                }
            }
        }, calendar.getTime(), PERIOD * 1000);
    }

    /**
     * upload
     * @param millis
     * @param qps
     */
    private void uploadQPSStat(Long millis, Map<ServiceSimpleInfo, AtomicInteger> qps) {
        LOGGER.debug("上送时间:{}ms  上送数据{} ", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss S").format(millis), qps);

        List<DataPoint> points = new ArrayList<>(16);

        qps.forEach(((serviceSimpleInfo, atomicInteger) -> {
            DataPoint point = new DataPoint();
            Map<String,String> tag =  new ConcurrentHashMap<>(16);
            tag.put("service_name", serviceSimpleInfo.getServiceName());
            tag.put("method_name", serviceSimpleInfo.getMethodName());
            tag.put("version_name", serviceSimpleInfo.getVersionName());
            tag.put("server_ip", SERVER_IP);
            tag.put("server_port", SERVER_PORT.toString());
            tag.put("period", PERIOD+"");
            tag.put("analysis_time", millis.toString());
            point.setBizTag("dapeng_qps");
            point.setDatabase(DATA_BASE);
            point.setTags(tag);
            point.setValue(atomicInteger.get()+"");
            points.add(point);
        }));

        try {
            SERVICE_CLIENT.submitPoints(points);
        } catch (SoaException e) {
            LOGGER.error(e.getMessage(), e);
        }finally {
            qpsStats.clear();
        }
    }
}
