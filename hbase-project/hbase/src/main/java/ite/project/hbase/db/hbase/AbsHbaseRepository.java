package ite.project.hbase.db.hbase;

import com.google.gson.Gson;
import ite.project.hbase.commons.data.model.paging.Pageable;
import ite.project.hbase.db.hbase.annotation.HbaseEntity;
import ite.project.hbase.db.hbase.annotation.HbaseTable;
import ite.project.hbase.utils.LogUtils;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.filter.PageFilter;
import org.apache.hadoop.hbase.util.Bytes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static ite.project.hbase.db.hbase.utils.HbaseUtils.*;

public abstract class AbsHbaseRepository<T> implements IHbaseRepository<T> {
    @Autowired
    protected LogUtils logUtils;
    @Autowired
    protected Connection connection;
    @Autowired
    protected Admin admin;
    protected Class<T> entityClass;
    protected TableName tableName;
    protected Gson gson = new Gson();

    @PostConstruct
    public void init() {
        this.logUtils.logInfo("Init class repository", this.getClass().getSimpleName());
        this.entityClass = (Class<T>) ((ParameterizedType) (getClass().getGenericSuperclass()))
                .getActualTypeArguments()[0];
        this.tableName = getTableName();
    }

    @Override
    public void putData(T entity) {
        try {
            logUtils.logInfo(logUtils.STEP_TRACE, "Put data to Hbase: " + gson.toJson(entity));
            Table table = connection.getTable(tableName);
            String rowKeyValue = getRowKeyValue(entityClass, entity);
            if (rowKeyValue != null) {
                Put put = new Put(Bytes.toBytes(rowKeyValue));
                Map<String, Map<String, String>> data = convertObjectToData(entityClass, entity);

                if (data != null) {
                    for (Map.Entry<String, Map<String, String>> entry : data.entrySet()) {
                        String columnFamilyName = entry.getKey();
                        Map<String, String> dataCells = entry.getValue();

                        dataCells.entrySet().stream().forEach(dataCell -> {
                            put.addColumn(Bytes.toBytes(columnFamilyName),
                                    Bytes.toBytes(dataCell.getKey()), Bytes.toBytes(dataCell.getValue()));
                        });
                    }
                    table.put(put);

                    logUtils.logInfo(logUtils.STEP_TRACE, "Put data success");
                } else {
                    throw new RuntimeException("error");
                }
            } else {
                throw new RuntimeException("rowKey is null");
            }
        } catch (Exception e) {
            logUtils.logErr(logUtils.STEP_TRACE, e);
            logUtils.logInfo(logUtils.STEP_TRACE, "Put data failed");
        }
    }

    @Override
    public List<T> getDataTable() {
        try {
            logUtils.logInfo(logUtils.STEP_TRACE, "get data from Hbase");
            Table table = connection.getTable(tableName);
            ResultScanner scanner = table.getScanner(new Scan());
            List<T> entities = new ArrayList<>();
            for (Result result : scanner) {
                T object = convertResultToData(entityClass, result);
                entities.add(object);
            }
            logUtils.logInfo(logUtils.STEP_TRACE, "Put data success: " + gson.toJson(entities));
            return entities;
        } catch (Exception e) {
            logUtils.logErr(logUtils.STEP_TRACE, e);
            logUtils.logInfo(logUtils.STEP_TRACE, "Get data failed");
            return null;
        }
    }

    @Override
    public T getDataByRowKey(String rowKey) {
        try {
            logUtils.logInfo(logUtils.STEP_TRACE, "get data from Hbase");
            Table table = connection.getTable(tableName);
            Get get = new Get(Bytes.toBytes(rowKey));
            Result result = table.get(get);
            T object = convertResultToData(entityClass, result);
            logUtils.logInfo(logUtils.STEP_TRACE, "End Get data success: " + gson.toJson(object));
            return object;
        } catch (Exception e) {
            logUtils.logErr(logUtils.STEP_TRACE, e);
            logUtils.logInfo(logUtils.STEP_TRACE, "Get data failed");
            return null;
        }
    }

    @Override
    public List<T> getPaginated(Pageable pageable){
        try {
            logUtils.logInfo(logUtils.STEP_TRACE, "Begin paginated scan: startRow=" + pageable.getOffset() + ", pageSize=" + pageable.getPageSize());
            Table table = connection.getTable(tableName);
            Scan scan = new Scan();
            scan.withStartRow(Bytes.toBytes(pageable.getOffset()));
            PageFilter pageFilter = new PageFilter(pageable.getPageSize());
            scan.setFilter(pageFilter);
            ResultScanner scanner = table.getScanner(scan);
            List<T> entities = new ArrayList<>();
            for (Result result : scanner) {
                T object = convertResultToData(entityClass, result);
                entities.add(object);
            }
            logUtils.logInfo(logUtils.STEP_TRACE, "Get data success: " + gson.toJson(entities));
            return entities;
        }catch (Exception e){
            logUtils.logErr(logUtils.STEP_TRACE, e);
            logUtils.logInfo(logUtils.STEP_TRACE, "Paginated scan failed");
            return null;
        }
    }
    @Override
    public boolean rowKeyExists(String rowKey) {
        try {
            logUtils.logInfo(logUtils.STEP_TRACE, "Begin check exist rowKey: " + rowKey);
            Table table = connection.getTable(tableName);
            Get get = new Get(rowKey.getBytes());
            boolean isExisted = table.exists(get);
            logUtils.logInfo(logUtils.STEP_TRACE, "Check RowKey success: " + isExisted);
            return isExisted;
        } catch (Exception e) {
            logUtils.logErr(logUtils.STEP_TRACE, e);
            logUtils.logInfo(logUtils.STEP_TRACE, "Check rowKey failed");
            return false;
        }
    }

    @Override
    public boolean binExists(String rowKey, String columnFamily, String qualifier) {
        try {
            logUtils.logInfo(logUtils.STEP_TRACE, "Begin check exist bin: rowKey=" + rowKey + ", columnFamily=" + columnFamily + ", qualifier=" + qualifier);
            Table table = connection.getTable(tableName);
            Get get = new Get(rowKey.getBytes());
            get.addColumn(columnFamily.getBytes(), qualifier.getBytes());
            Result result = table.get(get);
            boolean isExisted = !result.isEmpty();
            logUtils.logInfo(logUtils.STEP_TRACE, "Check bin success: " + isExisted);
            return isExisted;
        } catch (Exception e) {
            logUtils.logErr(logUtils.STEP_TRACE, e);
            logUtils.logInfo(logUtils.STEP_TRACE, "Check bin failed");
            return false;
        }
    }

    @Override
    public void deleteRow(String rowKey) {
        try {
            logUtils.logInfo(logUtils.STEP_TRACE, "Delete row: " + rowKey);
            Table table = connection.getTable(tableName);
            Delete delete = new Delete(Bytes.toBytes(rowKey));
            table.delete(delete);
            logUtils.logInfo(logUtils.STEP_TRACE, "delete row success");
        } catch (Exception e) {
            logUtils.logErr(logUtils.STEP_TRACE, e);
            logUtils.logInfo(logUtils.STEP_TRACE, "Delete data failed");
        }
    }

    private TableName getTableName() {
        try {
            if (this.entityClass.isAnnotationPresent(HbaseEntity.class) && this.entityClass.isAnnotationPresent(HbaseTable.class)) {
                String name = this.entityClass.getAnnotation(HbaseTable.class).name();
                if (!StringUtils.isEmpty(name)) {
                    TableName table = TableName.valueOf(name);
                    if (!admin.tableExists(table)) {
                        throw new RuntimeException("error");
                    }
                    return table;
                } else {
                    throw new RuntimeException("error");
                }
            } else {
                throw new RuntimeException("error");
            }
        } catch (Exception e) {
            throw new RuntimeException("error");
        }
    }
}
