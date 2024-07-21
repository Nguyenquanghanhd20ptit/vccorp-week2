package ite.project.hbase.config;

import ite.project.hbase.db.hbase.annotation.HbaseEntity;
import ite.project.hbase.db.hbase.config.HBaseTableCreator;
import org.reflections.Reflections;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class MyApplicationRunner implements ApplicationRunner {
    @Autowired
    private HBaseTableCreator hBaseTableCreator;
    @Override
    public void run(ApplicationArguments args) {
        Set<Class<?>> classAnnotations = new Reflections("ite.project.hbase").getTypesAnnotatedWith(HbaseEntity.class);
        for (Class<?> clazz : classAnnotations){
            hBaseTableCreator.createTableHbase(clazz);
        }
    }
}