package ite.project.hbase.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
public class ResourceProperties {
    private final String hbaseZookeeperQuorum;
    private final String hbaseZookeeperClientPort;

    public ResourceProperties(@Value("${hbase.config.hbase.zookeeper.quorum}") String hbaseZookeeperQuorum,
                              @Value("${hbase.config.hbase.zookeeper.property.clientPort}") String hbaseZookeeperClientPort) {
        this.hbaseZookeeperQuorum = hbaseZookeeperQuorum;
        this.hbaseZookeeperClientPort = hbaseZookeeperClientPort;
    }
}
