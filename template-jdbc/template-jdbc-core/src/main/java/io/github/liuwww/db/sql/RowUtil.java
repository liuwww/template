package io.github.liuwww.db.sql;

import java.lang.reflect.Field;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import io.github.liuwww.common.util.StringUtil;
import io.github.liuwww.db.context.TableMetaData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.liuwww.common.entity.TableEntity;
import io.github.liuwww.common.execption.DbException;
import io.github.liuwww.common.util.BeanUtils;
import io.github.liuwww.common.util.DateUtil;
import io.github.liuwww.common.util.EntryUtil;

public class RowUtil {
    private static Logger logger = LoggerFactory.getLogger(RowUtil.class);

    private static enum DataOpeType {
        INSERT, UPDATE
    }

    public static TableDefaultValue getTableDefaultValue(TableMetaData tmd) {
        TableDefaultValue tableDefaultValue = (TableDefaultValue) BeanUtils
                .getBean(tmd.getTableName().toUpperCase(java.util.Locale.ROOT) + TableDefaultValue.BEAN_NAME_SUFFIX);
        if (tableDefaultValue == null) {
            tableDefaultValue = (TableDefaultValue) BeanUtils.getBean(TableDefaultValue.BEAN_NAME_SUFFIX);
        }
        return tableDefaultValue;
    }

    private static Row defaultValue(Row row, TableMetaData tmd, DataOpeType type) {
        if (!row.getIsEffective()) {
            return row;
        }
        TableDefaultValue tableDefaultValue = (TableDefaultValue) BeanUtils
                .getBean(row.getTableName().toUpperCase(java.util.Locale.ROOT) + TableDefaultValue.BEAN_NAME_SUFFIX);
        if (tableDefaultValue == null) {
            tableDefaultValue = (TableDefaultValue) BeanUtils.getBean(TableDefaultValue.BEAN_NAME_SUFFIX);
        }
        if (tableDefaultValue != null) {
            Map<String, Object> map = null;
            if (type.equals(DataOpeType.INSERT)) {
                map = tableDefaultValue.getNewDefaultValue(tmd);
            } else {
                map = tableDefaultValue.getUpdateDefaultValue(tmd);
            }
            if (map == null) {
                return row;
            }
            Map<String, Object> valuMap = row.getRowValueMap();
            Map<String, Object> addMap = new HashMap<String, Object>();
            for (String key : map.keySet()) {
                Object val = map.get(key);
                if (!valuMap.containsKey(key)) {
                    valuMap.put(key, val);
                    addMap.put(key, val);
                }
            }
            row.setAdditionalMap(addMap);
        } else {
            row.setAdditionalMap(Collections.<String, Object>emptyMap());
        }
        convertInValidVal(row, tmd);
        return row;
    }

    public static void convertInValidVal(Row row, TableMetaData tmd) {
        Map<String, Object> valueVal = row.getRowValueMap();
        for (Entry<String, Object> entry : valueVal.entrySet()) {
            String key = entry.getKey();
            Column column = tmd.getColumnByColumn(key);
            if (column == null) {
                continue;
            }
            if (Types.TIMESTAMP == column.getDataType() || Types.DATE == column.getDataType()) {
                Object val = entry.getValue();
                if (val instanceof java.util.Date) {
                    val = new Timestamp(((Date) val).getTime());
                } else if (val instanceof String) {
                    if (StringUtil.isBlank(val.toString())) {
                        val = null;
                    } else {
                        val = new Timestamp(DateUtil.parse(val.toString()).getTime());
                    }
                } else if (val instanceof Long) {
                    val = new Timestamp((Long) val);
                }
                valueVal.put(key, val);
            }
        }
    }

    public static Row getInsertRow(String tableName, Map<String, Object> fieldVals, TableMetaData tmd) {
        Row row = new Row();
        Map<String, Object> rowValueMap = new HashMap<String, Object>();
        if (tmd == null) {
            throw new DbException("表[" + tableName + "]不存在或没有加载，且未提供 TableMetaData！");
        }
        row.setDbType(tmd.getDbType());
        row.setTableName(tmd.getTableName());
        Column idColumn = tmd.getIdColumn();
        for (Entry<String, Object> entry : fieldVals.entrySet()) {
            Object val = entry.getValue();
            String key = entry.getKey();
            if (val == null) {
                continue;
            }
            Column column = tmd.getColumn(key);
            if (column != null) {
                if (column.equals(idColumn)) {
                    if (StringUtil.isNotBlank(val.toString())) {
                        row.setIdValue(val.toString());
                        row.setIdName(idColumn.getColumnName());
                    }
                    // else
                    // {
                    // String id = SnowflakeIdGeneratorUtil.nextStringId();
                    // row.setIdValue(id);
                    // }
                    // row.setIdName(key);
                    // rowValueMap.put(column.getColumnName(),
                    // row.getIdValue());
                } else {
                    if (column.isNumberColumn() && StringUtil.isBlank(val.toString())) {
                        continue;
                    }
                    rowValueMap.put(column.getColumnName(), val);
                }
            }
        }
        // if (row.getIdName() == null)
        // {
        // String id = SnowflakeIdGeneratorUtil.nextStringId();
        // row.setIdName(idColumn.getName());
        // row.setIdValue(id);
        // rowValueMap.put(idColumn.getColumnName(), id);
        // }

        if (rowValueMap.size() > 0) {
            row.setRowValueMap(rowValueMap);
        } else {
            row.setIsEffective(false);
        }
        return defaultValue(row, tmd, DataOpeType.INSERT);
    }

    public static Row getUpdateRow(String tableName, Map<String, Object> fieldVals, TableMetaData tmd) {
        Row row = new Row();
        Map<String, Object> rowValueMap = new HashMap<String, Object>();
        if (tmd == null) {
            throw new DbException("表[" + tableName + "]不存在或没有加载，且未提供 TableMetaData！");
        }
        row.setDbType(tmd.getDbType());
        row.setTableName(tmd.getTableName());
        Column idColumn = tmd.getIdColumn();

        boolean isUnionKey = tmd.isUnionKey();

        if (!isUnionKey && idColumn == null) {
            throw new DbException("表[" + tableName + "]没有主键！");
        } else if (isUnionKey) {
            row.setIdNames(tmd.getIdNames());
        } else {
            row.setIdName(idColumn.getColumnName());
        }
        for (String key : fieldVals.keySet()) {
            Object val = fieldVals.get(key);
            if (val == null) {
                continue;
            }
            Column column = tmd.getColumn(key);
            if (column != null) {

                if (!isUnionKey) {
                    if (column.equals(idColumn)) {

                        row.setIdValue(val.toString());
                        row.setIdName(column.getColumnName());
                    } else {
                        if (column.isNumberColumn() && StringUtil.isBlank(val.toString())) {
                            val = null;
                            // continue;
                        }
                        rowValueMap.put(column.getColumnName(), val);
                    }
                } else if (isUnionKey) {
                    // 联合主键的情况
                    int index = getObjectIndex(tmd.getIdColumns(), column);
                    if (index >= 0) {
                        String[] idValues = row.getIdValues();
                        if (idValues == null) {
                            idValues = new String[tmd.getIdColumns().length];
                            row.setIdValues(idValues);
                        }
                        if (val != null && StringUtil.isNotBlank(val.toString())) {
                            idValues[index] = val.toString();
                        }
                    } else {
                        if (column.isNumberColumn() && StringUtil.isBlank(val.toString())) {
                            continue;
                        }
                        rowValueMap.put(column.getColumnName(), val);
                    }

                }

            }
        }
        if (isUnionKey) {
            if (rowValueMap.size() > 0) {
                if (row.getIdValues() != null && row.getIdValues().length == row.getIdNames().length) {
                    row.setRowValueMap(rowValueMap);
                } else {
                    row.setIsEffective(false);
                }
            } else {
                row.setIsEffective(false);
            }

        } else {
            if (rowValueMap.size() > 0 && StringUtil.isNotBlank(row.getIdValue())) {
                row.setRowValueMap(rowValueMap);
            } else {
                row.setIsEffective(false);
            }
        }

        return defaultValue(row, tmd, DataOpeType.UPDATE);
    }

    /**
     * 获取基于主键的删除行对象
     * <p>
     * 该方法专门用于<b>基于主键的单条记录删除</b>，只处理主键字段，非主键字段会被忽略。
     * 对于单主键表，只提取主键字段；对于联合主键表，提取所有主键字段。
     * </p>
     * 
     * @param tableName 表名
     * @param fieldVals 字段值映射（应包含主键字段）
     * @param tmd       表元数据
     * @return 删除行对象
     * @throws DbException 当表不存在或没有主键时抛出
     */
    public static Row getDeleteRow(String tableName, Map<String, Object> fieldVals, TableMetaData tmd) {
        Row row = new Row();
        if (tmd == null) {
            throw new DbException("表[" + tableName + "]不存在或没有加载，且未提供 TableMetaData！");
        }
        row.setDbType(tmd.getDbType());
        Map<String, Object> rowValueMap = new HashMap<String, Object>(2);
        row.setTableName(tmd.getTableName());
        Column idColumn = tmd.getIdColumn();
        boolean isUnionKey = tmd.isUnionKey();
        if (!isUnionKey && idColumn == null) {
            throw new DbException("表[" + tableName + "]没有主键！");
        } else if (isUnionKey) {
            row.setIdNames(tmd.getIdNames());
        } else {
            row.setIdName(idColumn.getColumnName());
        }
        for (String key : fieldVals.keySet()) {
            Column column = tmd.getColumn(key);
            if (column != null) {
                if (!isUnionKey) {
                    if (column.equals(idColumn)) {
                        Object val = fieldVals.get(key);
                        rowValueMap.put(column.getColumnName(), val);

                        row.setIdValue(val.toString());
                        row.setIdName(column.getColumnName());
                        row.setRowValueMap(rowValueMap);
                        break;
                    }
                } else if (isUnionKey) {
                    int index = getObjectIndex(tmd.getIdColumns(), column);
                    if (index >= 0) {
                        String[] idValues = row.getIdValues();
                        if (idValues == null) {
                            idValues = new String[tmd.getIdColumns().length];
                            row.setIdValues(idValues);
                        }
                        Object val = fieldVals.get(key);
                        idValues[index] = val.toString();
                    }
                }
            }
        }
        checkUpdateRowValid(row, isUnionKey);
        if (isUnionKey) {
            String[] idNames = row.getIdNames();
            String[] idValues = row.getIdValues();
            if (idValues != null && idValues.length != 0) {
                for (int i = 0; i < idNames.length; i++) {
                    rowValueMap.put(idNames[i], idValues[i]);
                }
            }
            row.setRowValueMap(rowValueMap);
        }
        return row;
    }

    private static void checkUpdateRowValid(Row row, boolean isUnionKey) {
        if (row.getIdName() == null && !isUnionKey) {
            row.setIsEffective(false);
        } else if (isUnionKey) {
            String[] idValues = row.getIdValues();
            if (idValues == null || idValues.length == 0) {
                row.setIsEffective(false);
            } else {
                for (String idVal : idValues) {
                    if (StringUtil.isBlank(idVal)) {
                        row.setIsEffective(false);
                        break;
                    }
                }
            }
        }
    }

    /**
     * 获取基于条件的批量删除行对象
     * <p>
     * 该方法专门用于<b>基于任意条件的批量删除</b>，支持任意字段作为查询条件。
     * 与 {@link #getDeleteRow(String, Map, TableMetaData)} 不同，此方法不限制必须使用主键。
     * </p>
     * 
     * @param tableName 表名
     * @param fieldVals 条件字段值映射（可以是任意字段）
     * @param tmd       表元数据
     * @return 删除行对象
     */
    public static Row getDeleteRows(String tableName, Map<String, Object> fieldVals, TableMetaData tmd) {
        Row row = new Row();
        Map<String, Object> rowValueMap = new HashMap<String, Object>();
        if (tmd != null) {
            row.setTableName(tmd.getTableName());
            row.setDbType(tmd.getDbType());
            for (String key : fieldVals.keySet()) {
                Column column = tmd.getColumn(key);
                if (column != null) {
                    Object val = fieldVals.get(key);
                    rowValueMap.put(column.getColumnName(), val);
                }
            }
        } else {
            row.setTableName(tableName);
        }

        if (rowValueMap.size() == 0) {
            row.setIsEffective(false);
        } else {
            row.setRowValueMap(rowValueMap);
        }
        return row;
    }

    public static <T> Row getInsertRow(TableEntity<T> entity, TableMetaData tmd) {
        Row row = new Row();
        Map<String, Object> rowValueMap = new HashMap<String, Object>();
        if (tmd == null) {
            throw new DbException("表[" + entity.tableName() + "]不存在或没有加载，且未提供 TableMetaData！");
        }
        row.setDbType(tmd.getDbType());
        row.setTableName(tmd.getTableName());
        Column idColumn = tmd.getIdColumn();
        if (idColumn == null) {
            if (logger.isInfoEnabled()) {
                logger.info("表[{}]没有主键字段！", entity.tableName());
            }
            // throw new DbException("表[" + entity.tableName() + "]没有主键字段！");
        }
        for (Column column : tmd.getColumnList()) {
            Field field = EntryUtil.getField(entity.getClass(), column.getName());
            if (field == null) {
                continue;
            }

            Object val = EntryUtil.getFieldValue(entity, column.getName());
            if (val == null) {
                continue;
            }
            if (column.equals(idColumn)) {

                if (val != null && StringUtil.isNotBlank(val.toString())) {
                    row.setIdValue(val.toString());
                    row.setIdName(column.getColumnName());
                    rowValueMap.put(column.getColumnName(), row.getIdValue());
                } else {
                    // String id = SnowflakeIdGeneratorUtil.nextStringId();
                    // row.setIdValue(id);
                }
            } else {
                if (column.isNumberColumn() && StringUtil.isBlank(val.toString())) {
                    continue;
                }
                rowValueMap.put(column.getColumnName(), val);
            }
        }
        // if (row.getIdName() == null)
        // {
        // String id = SnowflakeIdGeneratorUtil.nextStringId();
        // row.setIdName(idColumn.getName());
        // row.setIdValue(id);
        // rowValueMap.put(idColumn.getColumnName(), id);
        // }

        if (rowValueMap.size() > 0) {
            row.setRowValueMap(rowValueMap);
        } else {
            row.setIsEffective(false);
        }
        return defaultValue(row, tmd, DataOpeType.INSERT);
    }

    public static <T> Row getUpdateRow(TableEntity<T> entity, TableMetaData tmd) {
        if (tmd == null) {
            throw new DbException("表[" + entity.tableName() + "]不存在或没有加载，且未提供 TableMetaData！");
        }
        Row row = new Row();
        Map<String, Object> rowValueMap = new HashMap<String, Object>();

        row.setTableName(tmd.getTableName());
        Column idColumn = tmd.getIdColumn();
        boolean isUnionKey = tmd.isUnionKey();

        if (!isUnionKey && idColumn == null) {
            throw new DbException("表[" + entity.tableName() + "]没有主键！");
        } else if (isUnionKey) {
            row.setIdNames(tmd.getIdNames());
        } else {
            row.setIdName(idColumn.getColumnName());
        }
        row.setDbType(tmd.getDbType());
        for (Column column : tmd.getColumnList()) {
            Field field = EntryUtil.getField(entity.getClass(), column.getName());
            if (field == null) {
                continue;
            }

            Object val = EntryUtil.getFieldValue(entity, column.getName());
            if (val == null) {
                continue;
            }
            // 非联合主键情况的处理
            if (!isUnionKey) {
                if (column.equals(idColumn)) {
                    if (val != null && StringUtil.isNotBlank(val.toString())) {
                        row.setIdValue(val.toString());
                    }
                } else {
                    if (column.isNumberColumn() && StringUtil.isBlank(val.toString())) {
                        continue;
                    }
                    rowValueMap.put(column.getColumnName(), val);
                }

            } else if (isUnionKey) {
                // 联合主键的情况
                int index = getObjectIndex(tmd.getIdColumns(), column);
                if (index >= 0) {
                    String[] idValues = row.getIdValues();
                    if (idValues == null) {
                        idValues = new String[tmd.getIdColumns().length];
                        row.setIdValues(idValues);
                    }
                    if (val != null && StringUtil.isNotBlank(val.toString())) {
                        idValues[index] = val.toString();
                    }
                } else {
                    if (column.isNumberColumn() && StringUtil.isBlank(val.toString())) {
                        continue;
                    }
                    rowValueMap.put(column.getColumnName(), val);
                }
            }
        }
        if (checkUpdateSingleRowValid(row, rowValueMap)) {
            row.setRowValueMap(rowValueMap);
        } else {
            row.setIsEffective(false);
        }
        return defaultValue(row, tmd, DataOpeType.UPDATE);
    }

    /**
     * 检查更新单行的数据是否是有效数据（id齐全，并且存在非id的更新数据）
     */
    private static boolean checkUpdateSingleRowValid(Row row, Map<String, Object> rowValueMap) {
        if (rowValueMap == null || rowValueMap.size() == 0) {
            return false;
        }
        boolean idChecked = false;
        if (StringUtil.isNotBlank(row.getIdValue())) {
            idChecked = true;
        }
        if (!idChecked) {
            // 联合主键更新时，主键的每个值都不可为空
            String[] idValues = row.getIdValues();
            if (idValues == null || idValues.length == 0) {
                return false;
            }
            for (String id : idValues) {
                if (StringUtil.isBlank(id)) {
                    return false;
                }
            }
        }

        return true;
    }

    public static <T> Row getDeleteRow(TableEntity<T> entity, TableMetaData tmd) {
        Row row = new Row();
        Map<String, Object> rowValueMap = new HashMap<String, Object>();
        boolean isUnionKey = false;
        if (tmd != null) {
            row.setTableName(tmd.getTableName());
            row.setDbType(tmd.getDbType());
            isUnionKey = tmd.isUnionKey();
            if (isUnionKey) {
                Column[] idColumnList = tmd.getIdColumns();
                row.setIdNames(tmd.getIdNames());
                for (int i = idColumnList.length - 1; i >= 0; i--) {
                    Column idColumn = idColumnList[i];
                    Field field = EntryUtil.getField(entity.getClass(), idColumn.getName());
                    if (field != null) {
                        Object val = EntryUtil.getFieldValue(entity, idColumn.getName());
                        rowValueMap.put(idColumn.getColumnName(), val);
                    }
                }

            } else {
                Column idColumn = tmd.getIdColumn();
                if (idColumn != null) {
                    Field field = EntryUtil.getField(entity.getClass(), idColumn.getName());
                    if (field != null) {
                        Object val = EntryUtil.getFieldValue(entity, idColumn.getName());
                        rowValueMap.put(idColumn.getColumnName(), val);
                        row.setIdName(idColumn.getColumnName());
                        if (val != null) {
                            row.setIdValue(val.toString());
                        }
                    }
                } else {
                    for (Column c : tmd.getColumnList()) {
                        String field = c.getName();
                        if (EntryUtil.hasField(entity, field)) {
                            Object val = EntryUtil.getFieldValue(entity, c.getName());
                            rowValueMap.put(c.getColumnName(), val);
                        }
                    }
                }
            }

        } else {

            row.setTableName(entity.tableName());
        }

        if (StringUtil.isBlank(row.getIdValue()) && !isUnionKey) {
            row.setIsEffective(false);
        } else if (isUnionKey) {
            if (rowValueMap.size() != tmd.getIdColumns().length) {
                row.setIsEffective(false);
            }
        } else {
            row.setRowValueMap(rowValueMap);
        }
        return row;
    }

    public static void setRowValMap(Row row, Map<String, Object> rowValMap) {
        row.setRowValueMap(rowValMap);
    }

    public static int getObjectIndex(Object[] objs, Object obj) {
        if (objs == null) {
            return -1;
        }
        for (int i = 0; i < objs.length; i++) {
            if (objs[i] == obj) {
                return i;
            }
        }
        return -1;
    }

}
