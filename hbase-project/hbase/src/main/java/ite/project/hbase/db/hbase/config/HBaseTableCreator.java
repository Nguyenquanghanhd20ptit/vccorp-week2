package ite.project.hbase.db.hbase.config;

import ite.project.hbase.db.hbase.annotation.ColumnFamily;
import ite.project.hbase.db.hbase.annotation.HbaseEntity;
import ite.project.hbase.db.hbase.annotation.HbaseId;
import ite.project.hbase.db.hbase.annotation.HbaseTable;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.util.Bytes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class HBaseTableCreator {
    @Autowired
    private Connection connection;
    @Autowired
    private Admin admin;

    public <T> void createTableHbase(Class<T> tClass) {
        try {
            if (tClass.isAnnotationPresent(HbaseTable.class) && tClass.isAnnotationPresent(HbaseEntity.class)) {
                HbaseTable annotation = tClass.getAnnotation(HbaseTable.class);
                String name = annotation.name();
                Field[] declaredFields = tClass.getDeclaredFields();
                if (!StringUtils.isEmpty(name) && checkHaveRowKeyAnnotation(declaredFields)) {
                    TableName table = TableName.valueOf(name);
                    List<String> columnFamilyNames = getColumnFamily(declaredFields);
                    //create table
                    if (!admin.tableExists(table)) {
                        List<ColumnFamilyDescriptor> columnFamilyDescriptors = columnFamilyNames.stream()
                                .map(cfName -> ColumnFamilyDescriptorBuilder.newBuilder(Bytes.toBytes(cfName)).setMaxVersions(1).build())
                                .collect(Collectors.toList());
                        TableDescriptorBuilder tableDescriptorBuilder = TableDescriptorBuilder.newBuilder(table);
                        for (ColumnFamilyDescriptor cfDescriptor : columnFamilyDescriptors) {
                            tableDescriptorBuilder.setColumnFamily(cfDescriptor);
                        }

                        TableDescriptor tableDescriptor = tableDescriptorBuilder.build();
                        admin.createTable(tableDescriptor);
                    } else {
                        TableDescriptor tableDescriptor = admin.getDescriptor(table);
                        ColumnFamilyDescriptor[] columnFamilies = tableDescriptor.getColumnFamilies();
                        List<String> columnFamilyNameDbs = Arrays.stream(columnFamilies).map(columnFamilyDescriptor -> columnFamilyDescriptor.getNameAsString())
                                .collect(Collectors.toList());

                        // su dung set de giam do phuc tap thuat toan
                        Set<String> columnFamilyNameDbSet = new HashSet<>(columnFamilyNameDbs);
                        List<String> newColumnFamilies = columnFamilyNames.stream()
                                .filter(cf -> !columnFamilyNameDbSet.contains(cf))
                                .collect(Collectors.toList());

                        if(newColumnFamilies.size() > 0){
                            // add new column families
                            for (String newColumnFamily : newColumnFamilies) {
                                ColumnFamilyDescriptor columnFamilyDescriptor = ColumnFamilyDescriptorBuilder
                                        .newBuilder(Bytes.toBytes(newColumnFamily))
                                        .setMaxVersions(1)
                                        .build();
                                admin.addColumnFamily(table, columnFamilyDescriptor);
                            }
                        }
                    }
                } else {
                    throw new SecurityException("Annotation HbaseTable name is null or not have annotation @HBaseRow");
                }
            } else {
                throw  new SecurityException("class not have annotation @HbaseTable");
            }

        } catch (Exception e) {
            throw  new SecurityException(e.getMessage());
        }
    }

    private boolean checkHaveRowKeyAnnotation(Field[] fields) {
        for (Field field : fields) {
            if (field.isAnnotationPresent(HbaseId.class)) {
                return true;
            }
        }
        return false;
    }

    private List<String> getColumnFamily(Field[] fields) {
        return Arrays.stream(fields).map(field -> {
            String fieldName = field.getName();
            if (field.isAnnotationPresent(ColumnFamily.class)) {
                ColumnFamily annotation = field.getAnnotation(ColumnFamily.class);
                return StringUtils.isEmpty(annotation.name()) ? fieldName : annotation.name();
            } else {
                return null;
            }
        }).filter(Objects::nonNull).collect(Collectors.toList());
    }
}
