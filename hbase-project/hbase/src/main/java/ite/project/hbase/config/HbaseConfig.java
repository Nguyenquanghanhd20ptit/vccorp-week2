package ite.project.hbase.config;

import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.apache.hadoop.hbase.client.HBaseAdmin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Configuration
public class HbaseConfig {
    @Autowired
    private ResourceProperties resourceProperties;

    @Bean
    public org.apache.hadoop.conf.Configuration configuration(){
        org.apache.hadoop.conf.Configuration config = HBaseConfiguration.create();
        config.set("hbase.zookeeper.quorum", resourceProperties.getHbaseZookeeperQuorum());
        config.set("hbase.zookeeper.property.clientPort", resourceProperties.getHbaseZookeeperClientPort());
        return config;
    }

    @Bean
    public Connection getConnection() throws IOException {
        return ConnectionFactory.createConnection(configuration());
    }

    @Bean
    public HBaseAdmin hBaseAdmin() throws IOException {
        return (HBaseAdmin) getConnection().getAdmin();
    }
}
