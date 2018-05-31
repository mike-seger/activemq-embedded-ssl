package com.net128.sample.activemq.embedded.ssl.configuration;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.ActiveMQSslConnectionFactory;
import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.broker.SslBrokerService;
import org.apache.activemq.usage.MemoryUsage;
import org.apache.activemq.usage.SystemUsage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.jms.core.JmsTemplate;

import javax.jms.ConnectionFactory;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import java.security.KeyStore;

@Configuration
public class JmsTestConfiguration {
    @Value("${jms.broker.url}")
    private String jmqBrokerUrl;
    @Value("${jms.broker.truststore}")
    private String jmqBrokerTruststore;
    @Value("${jms.broker.truststore_TYPE}")
    private String jmqBrokerTruststoreType;
    @Value("${jms.broker.truststore_PASSWORD}")
    private String jmqBrokerTruststorePassword;
    @Value("${jms.broker.keystore}")
    private String jmqBrokerKeystore;
    @Value("${jms.broker.keystore_TYPE}")
    private String jmqBrokerKeystoreType;
    @Value("${jms.broker.keystore_PASSWORD}")
    private String jmqBrokerKeystorePassword;
    @Value(("${client.jms.broker.url}"))
    private String clientJmsBrokerUrl;

    @Bean
    public JmsTemplate jmsTestTemplate(final ConnectionFactory inConnectionFactory) {
        final JmsTemplate jmqTestTemplate = new JmsTemplate(inConnectionFactory);
        jmqTestTemplate.setReceiveTimeout(5000L);
        jmqTestTemplate.afterPropertiesSet();
        return jmqTestTemplate;
    }

    @Bean
    @DependsOn("embeddedBroker")
    public ConnectionFactory clientJMSConnectionFactory() {
        final ActiveMQConnectionFactory connectionFactory;

        if (clientJmsBrokerUrl.startsWith("ssl")) {
            final ActiveMQSslConnectionFactory sslConnectionFactory =
                new ActiveMQSslConnectionFactory(clientJmsBrokerUrl);
            try {
                sslConnectionFactory.setTrustStore(jmqBrokerTruststore);
                sslConnectionFactory.setTrustStorePassword(jmqBrokerTruststorePassword);
                sslConnectionFactory.setKeyStore(jmqBrokerKeystore);
                sslConnectionFactory.setKeyStorePassword(jmqBrokerKeystorePassword);
                connectionFactory = sslConnectionFactory;
            } catch (final Exception theException) {
                throw new Error(theException);
            }
        } else {
            connectionFactory = new ActiveMQConnectionFactory(clientJmsBrokerUrl);
        }

        return connectionFactory;
    }

    @Bean(initMethod = "start", destroyMethod = "stop")
    public BrokerService embeddedBroker() {
        SslBrokerService activeMqBroker;

        try {
            activeMqBroker = new SslBrokerService();
            activeMqBroker.setUseJmx(false);
            activeMqBroker.setPersistent(false);
            activeMqBroker.setUseShutdownHook(true);

            final KeyManager[] keystore = readKeystore();
            final TrustManager[] truststore = readTruststore();
            activeMqBroker.addSslConnector(jmqBrokerUrl + "?transport.needClientAuth=true",
                keystore, truststore,null);

            final MemoryUsage activeMqMemoryUsage = new MemoryUsage();
            activeMqMemoryUsage.setPercentOfJvmHeap(20);
            final SystemUsage activeMqSystemUsage = new SystemUsage();
            activeMqSystemUsage.setMemoryUsage(activeMqMemoryUsage);
            activeMqBroker.setSystemUsage(activeMqSystemUsage);
        } catch (final Exception theException) {
            throw new Error("An error occurred starting test ActiveMQ broker", theException);
        }

        return activeMqBroker;
    }

    private KeyManager[] readKeystore() throws Exception {
        final KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        final KeyStore keyStore = KeyStore.getInstance(jmqBrokerKeystoreType);
        final Resource keystoreResource = new ClassPathResource(jmqBrokerKeystore);
        keyStore.load(keystoreResource.getInputStream(), jmqBrokerKeystorePassword.toCharArray());
        keyManagerFactory.init(keyStore, jmqBrokerKeystorePassword.toCharArray());
        final KeyManager[] keystoreManagers = keyManagerFactory.getKeyManagers();
        return keystoreManagers;
    }

    private TrustManager[] readTruststore() throws Exception {
        final KeyStore truststore = KeyStore.getInstance(jmqBrokerTruststoreType);
        final Resource truststoreResource = new ClassPathResource(jmqBrokerTruststore);
        truststore.load(truststoreResource.getInputStream(), null);
        final TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        trustManagerFactory.init(truststore);
        final TrustManager[] trustManagers = trustManagerFactory.getTrustManagers();
        return trustManagers;
    }
}
