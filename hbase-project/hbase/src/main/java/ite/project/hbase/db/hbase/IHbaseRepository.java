package ite.project.hbase.db.hbase;

import ite.project.hbase.commons.data.model.paging.Pageable;

import java.util.List;
import java.util.Map;

public interface IHbaseRepository <T>{

    void putData(T entity);

    List<T> getDataTable();

    T getDataByRowKey(String rowKey);

    List<T> getPaginated(Pageable pageable);

    boolean rowKeyExists(String rowKey);

    boolean binExists(String rowKey, String columnFamily, String qualifier);

    void deleteRow(String rowKey);
}
