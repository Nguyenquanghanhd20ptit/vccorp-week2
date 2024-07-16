package ite.project.hbase.db.hbase.entity;

import ite.project.hbase.db.hbase.annotation.ColumnFamily;
import ite.project.hbase.db.hbase.annotation.HbaseEntity;
import ite.project.hbase.db.hbase.annotation.HbaseTable;
@HbaseEntity
@HbaseTable(name = "student")
public class StudentEntity {
    @ColumnFamily(name = "personal")
    private String personal;
    @ColumnFamily(name = "personal")
    private String academic;
}
