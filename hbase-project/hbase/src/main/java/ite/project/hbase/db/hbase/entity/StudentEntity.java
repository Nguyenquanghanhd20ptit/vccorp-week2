package ite.project.hbase.db.hbase.entity;

import ite.project.hbase.db.hbase.annotation.ColumnFamily;
import ite.project.hbase.db.hbase.annotation.HbaseEntity;
import ite.project.hbase.db.hbase.annotation.HbaseId;
import ite.project.hbase.db.hbase.annotation.HbaseTable;
import lombok.Data;

import java.util.Map;

@Data
@HbaseEntity
@HbaseTable(name = "student")
public class StudentEntity {
    @HbaseId
    private String row;
    @ColumnFamily(name = "personal")
    private Map<String,String> personal;
    @ColumnFamily(name = "academic")
    private Map<String,String> academic;
}
