package ite.project.hbase.db.hbase.utils;

import ite.project.hbase.db.hbase.annotation.ColumnFamily;
import ite.project.hbase.db.hbase.annotation.HbaseId;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.util.Bytes;
import org.springframework.util.StringUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toMap;

public class HbaseUtils {
    public static <T> T convertResultToData(Class<T> entityClass, Result result) {
        try {
            if (result == null || entityClass == null) return null;
            // khoi tao object
            T object = entityClass.newInstance();
            String row = new String(result.getRow());
            Field[] declaredFields = entityClass.getDeclaredFields();
            //save row to object
            saveRow(declaredFields, object, row);

            List<String> fieldNames = Arrays.stream(declaredFields).map(field -> field.getName())
                    .collect(Collectors.toList());

            Map<String, String> fieldColumnFamilydMap = fieldColumnFamilyMap(declaredFields);

            //de entity co the luu data trong column family
            Map<String, Method> columnFamilyMethodMap = columnFamilyMethodSetMap(entityClass, fieldNames, fieldColumnFamilydMap);

            // chua data cua colum family
            Map<String, Map<String, String>> columnFamilyDataMap = columnFamilyMethodMap.keySet()
                    .stream().map(key -> Pair.of(key, new HashMap<String, String>()))
                    .collect(toMap(Pair::getKey, Pair::getValue));

            for (Cell cell : result.rawCells()) {
                String key = Bytes.toString(cell.getQualifierArray(), cell.getQualifierOffset(), cell.getQualifierLength());
                String value = Bytes.toString(cell.getValueArray(), cell.getValueOffset(), cell.getValueLength());
                String columnFamily = Bytes.toString(cell.getFamilyArray(), cell.getFamilyOffset(), cell.getFamilyLength());

                if (columnFamilyDataMap.containsKey(columnFamily)) {
                    Map<String, String> dataMap = columnFamilyDataMap.get(columnFamily);
                    dataMap.put(key, value);
                    columnFamilyDataMap.put(columnFamily, dataMap);
                }
            }

            for (Map.Entry<String, Method> entry : columnFamilyMethodMap.entrySet()) {
                String columnFamily = entry.getKey();
                Method method = entry.getValue();
                Map<String, String> data = columnFamilyDataMap.get(columnFamily);
                Class<?> parameterType = method.getParameterTypes()[0];
                // store data
                if (Map.class.equals(parameterType)) {
                    method.invoke(object, data);
                } else {
                    throw new RuntimeException("Data type invalid");
                }
            }
            return object;
        } catch (Exception e) {
            return null;
        }
    }

    public static  <T> Map<String,Map<String,String>> convertObjectToData(Class<T> tClass,T obj){
        try {
            Map<String,Map<String,String>> result = new HashMap<>();
            Field[] declaredFields = tClass.getDeclaredFields();
            List<String> fieldNames = Arrays.stream(declaredFields).map(field -> field.getName())
                    .collect(Collectors.toList());
            Map<String, String> fieldColumnFamilyMap = fieldColumnFamilyMap(declaredFields);
            Map<String, Method> columnFamilyMethodGetMap = columnFamilyMethodGetMap(tClass, fieldNames, fieldColumnFamilyMap);

            for (Map.Entry<String,Method> entry : columnFamilyMethodGetMap.entrySet()){
                String columnFamily = entry.getKey();
                Method method = entry.getValue();
                Class<?> parameterType = method.getReturnType();
                if(Map.class.equals(parameterType)){
                    Map<String,String> data = (Map<String, String>) method.invoke(obj);
                    if(data != null){
                        result.put(columnFamily,data);
                    }
                }else {
                    throw new RuntimeException("Data type invalid");
                }
            }
            return result;
        }catch (Exception e){
            return null;
        }
    }

    public  static <T> String getRowKeyValue(Class<T> tClass,Object obj) {
        try {
            Field[] fields = tClass.getDeclaredFields();
            for (Field field : fields){
                if(field.isAnnotationPresent(HbaseId.class)){
                    field.setAccessible(true);
                    Object rowKeyValue = field.get(obj);
                    return rowKeyValue.toString();
                }
            }
            return null;
        }catch (Exception e){
            return null;
        }
    }

    private static void saveRow(Field[] fields, Object obj, String row) {
        try {
            for (Field field : fields) {
                if (field.isAnnotationPresent(HbaseId.class)) {
                    field.setAccessible(true);
                    field.set(obj, row);
                    return;
                }
            }
        } catch (Exception e) {
        }
    }

    private static Map<String, String> fieldColumnFamilyMap(Field[] declaredFields) {
        return Arrays.stream(declaredFields).map(field -> {
            String fieldName = field.getName();
            String colFamilyName = "";
            if (field.isAnnotationPresent(ColumnFamily.class)) {
                ColumnFamily columnAnnotation = field.getAnnotation(ColumnFamily.class);
                String name = columnAnnotation.name();
                colFamilyName = StringUtils.isEmpty(name) ? fieldName : name;
            } else return null;
            return Pair.of(fieldName, colFamilyName);
        }).filter(Objects::nonNull).collect(toMap(Pair::getKey, Pair::getValue));
    }

    public static <T> Map<String, Method> columnFamilyMethodGetMap(Class<T> tClass,
                                                                   Collection<String> fieldNames,
                                                                   Map<String, String> fieldColumnFamilyMap) {
        Map<String, String> methodNameFieldMap = fieldNames.stream()
                .map(s -> {
                    String firstStr = s.substring(0, 1);
                    String methodName = s.replaceFirst(firstStr, firstStr.toUpperCase());
                    return Pair.of("get" + methodName, s);
                }).collect(Collectors.toMap(Pair::getKey, Pair::getValue));

        Method[] methods = tClass.getMethods();
        return Arrays.stream(methods).filter(method -> method.getName().startsWith("get")
                && methodNameFieldMap.containsKey(method.getName()))
                .map(method -> {
                    String columnFamily = fieldColumnFamilyMap.getOrDefault(methodNameFieldMap.get(method.getName()),null );
                    return columnFamily != null ? Pair.of(columnFamily,method) : null;
                }).filter(Objects::nonNull).collect(toMap(Pair::getKey,Pair::getValue));
    }

    private static <T> Map<String, Method> columnFamilyMethodSetMap(Class<T> tclass,
                                                                    Collection<String> fieldNames,
                                                                    Map<String, String> fieldColumnFamilyMap) {

        Map<String, String> methodNameFieldMap = fieldNames.stream().map(s -> {
            String firstStr = s.substring(0, 1);
            return Pair.of("set" + s.replaceFirst(firstStr, firstStr.toUpperCase()), s);
        }).collect(toMap(Pair::getKey, Pair::getValue));

        Method[] methods = tclass.getMethods();
        return Arrays.stream(methods).filter(method -> method.getName().startsWith("set")
                        && methodNameFieldMap.containsKey(method.getName()))
                .map(method -> {
                    String columnFamilyName = fieldColumnFamilyMap.getOrDefault(methodNameFieldMap.get(method.getName()), null);
                    return columnFamilyName != null ? Pair.of(columnFamilyName, method) : null;
                }).filter(Objects::nonNull).collect(toMap(Pair::getKey, Pair::getValue));
    }
}


