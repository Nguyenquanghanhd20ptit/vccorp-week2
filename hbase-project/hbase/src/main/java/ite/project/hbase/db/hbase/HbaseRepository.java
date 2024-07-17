package ite.project.hbase.db.hbase;

import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.util.Bytes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Map;

@Repository
public class HbaseRepository {

    @Autowired
    private Connection connection;
    @Autowired
    private  Admin admin;


    public void createTable(String name, String colFamily) throws Exception {
        TableName table = TableName.valueOf(name);
        if (!admin.tableExists(table)) {
            TableDescriptor tableDes = TableDescriptorBuilder.newBuilder(table)
                    .setColumnFamily(ColumnFamilyDescriptorBuilder.newBuilder(Bytes.toBytes(colFamily))
                            .setMaxVersions(1)
                            .build())
                    .build();
            admin.createTable(tableDes);
        } else {
            System.out.println("Table [" + name + "] already exists.");
        }
    }

    public void putData(String name, String colFamily, String rowkey, Map<String, String> data) throws Exception {
        Table table = connection.getTable(TableName.valueOf(name));
        Put put = new Put(Bytes.toBytes(rowkey));
        for (Map.Entry<String, String> entry : data.entrySet()) {
            put.addColumn(Bytes.toBytes(colFamily), Bytes.toBytes(entry.getKey()), Bytes.toBytes(entry.getValue()));
        }
        table.put(put);
    }

    public void getData(String name) throws Exception {
        Table table = connection.getTable(TableName.valueOf(name));
        ResultScanner scanner = table.getScanner(new Scan());
        for (Result result : scanner) {
            System.out.println("Row: " + new String(result.getRow()));
            for (Cell cell : result.rawCells()) {
                System.out.println("ColFamily: " + Bytes.toString(cell.getFamilyArray(), cell.getFamilyOffset(), cell.getFamilyLength()) +
                        ", Value: " + Bytes.toString(cell.getValueArray(), cell.getValueOffset(), cell.getValueLength()));
            }
        }
    }
}
