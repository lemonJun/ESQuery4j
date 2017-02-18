/*
 * Copyright 2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package lemon.elastic.query4j.esproxy.client;

import static org.elasticsearch.common.settings.ImmutableSettings.settingsBuilder;

import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.lang3.StringUtils;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.AbstractIdleService;
import com.google.inject.Singleton;

import lemon.elastic.query4j.util.SpringStringUtils;

/**
 * transport的连接方式
 *
 * @author WangYazhou
 * @date 2016年4月8日 下午4:46:43
 * @see
 */
@Singleton
public class TransportClientProvider extends AbstractIdleService {

    private static final Logger logger = LoggerFactory.getLogger(TransportClientProvider.class);
    private String clusterNodes = "127.0.0.1:9300";
    private String clusterName = "elasticsearch";
    private Boolean clientTransportSniff = true;
    private Boolean clientIgnoreClusterName = Boolean.FALSE;
    private String clientPingTimeout = "5s";
    private String clientNodesSamplerInterval = "5s";
    private TransportClient client;
    static final String COLON = ":";
    static final String COMMA = ",";
    private Settings settings;

    public TransportClientProvider() {
        logger.info("TransportClientProvider");
        startUp();
    }

    @Override
    protected void startUp() {
        try {
            properties();
            client = new TransportClient(settings());
            Preconditions.checkState(SpringStringUtils.hasText(clusterNodes), "[Assertion failed] clusterNodes settings missing.");
            for (String clusterNode : StringUtils.split(clusterNodes, COMMA)) {
                String hostName = StringUtils.substringBefore(clusterNode, COLON);
                String port = StringUtils.substringAfter(clusterNode, COLON);
                Preconditions.checkState(SpringStringUtils.hasText(hostName), "[Assertion failed] missing host name in 'clusterNodes'");
                Preconditions.checkState(SpringStringUtils.hasText(port), "[Assertion failed] missing port in 'clusterNodes'");
                logger.info("adding transport node : " + clusterNode);
                client.addTransportAddress(new InetSocketTransportAddress(hostName, Integer.valueOf(port)));
            }
            client.connectedNodes();
        } catch (Exception e) {
            logger.error("buildClient error", e);
        }
    }

    //读取配置文件   生成配置信息
    private void properties() {

    }

    private Settings settings() {
        return settingsBuilder().put("cluster.name", clusterName).put("client.transport.sniff", clientTransportSniff).put("client.transport.ignore_cluster_name", clientIgnoreClusterName).put("client.transport.ping_timeout", clientPingTimeout).put("client.transport.nodes_sampler_interval", clientNodesSamplerInterval).build();
    }

    public Settings getSettings() {
        return settings;
    }

    public void setSettings(Settings settings) {
        this.settings = settings;
    }

    public void setClusterNodes(String clusterNodes) {
        this.clusterNodes = clusterNodes;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    public void setClientTransportSniff(Boolean clientTransportSniff) {
        this.clientTransportSniff = clientTransportSniff;
    }

    public String getClientNodesSamplerInterval() {
        return clientNodesSamplerInterval;
    }

    public void setClientNodesSamplerInterval(String clientNodesSamplerInterval) {
        this.clientNodesSamplerInterval = clientNodesSamplerInterval;
    }

    public String getClientPingTimeout() {
        return clientPingTimeout;
    }

    public void setClientPingTimeout(String clientPingTimeout) {
        this.clientPingTimeout = clientPingTimeout;
    }

    public Boolean getClientIgnoreClusterName() {
        return clientIgnoreClusterName;
    }

    public void setClientIgnoreClusterName(Boolean clientIgnoreClusterName) {
        this.clientIgnoreClusterName = clientIgnoreClusterName;
    }

    @Override
    protected void shutDown() throws Exception {
        try {
            logger.info("Closing elasticSearch  client");
            if (client != null) {
                client.close();
            }
        } catch (final Exception e) {
            logger.error("Error closing ElasticSearch client: ", e);
        }
    }

}
