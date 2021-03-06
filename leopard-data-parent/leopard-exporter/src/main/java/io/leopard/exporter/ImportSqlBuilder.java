package io.leopard.exporter;

import java.lang.reflect.Field;

import org.springframework.util.StringUtils;

import io.leopard.burrow.util.StringUtil;
import io.leopard.exporter.annotation.Column;
import io.leopard.exporter.annotation.Table;

/**
 * 导出SQL生成器
 * 
 * @author 谭海潮
 *
 */
public class ImportSqlBuilder {

	public static final char ESC_ORACLE = '"';

	public static final char ESC_MYSQL = '`';

	private char esc;

	private Class<?> model;

	public ImportSqlBuilder(Class<?> model, char esc) {
		this.model = model;
		this.esc = esc;
	}

	public String buildSql() {
		String tableName = this.getTableName();
		StringBuilder sb = new StringBuilder();
		sb.append("insert into ").append(esc).append(tableName).append(esc);
		StringBuilder columnNameBuilder = new StringBuilder();
		StringBuilder valueBuilder = new StringBuilder();
		int index = 0;
		int i = 0;
		for (Field field : model.getDeclaredFields()) {
			String columnName = getColumnName(field, i);
			i++;
			if (StringUtils.isEmpty(columnName)) {
				continue;
			}
			if (index > 0) {
				columnNameBuilder.append(", ");
				valueBuilder.append(", ");
			}
			columnNameBuilder.append(esc).append(columnName).append(esc);
			valueBuilder.append("?");
			index++;
		}
		if (index <= 0) {
			throw new RuntimeException("表[" + tableName + "]没有设置任何属性.");
		}
		sb.append('(').append(columnNameBuilder).append(')');
		sb.append(" values(").append(valueBuilder).append(")");
		return sb.toString();
	}

	/**
	 * 获取列名
	 * 
	 * @param field
	 * @return
	 */
	public String getColumnName(Field field, int index) {
		if (index == 0) {
			return field.getName();
		}
		Column column = field.getAnnotation(Column.class);
		if (column == null) {
			return null;
		}
		String columnName = column.alias();
		if (StringUtils.isEmpty(columnName)) {
			return null;
		}
		return field.getName();
	}

	/**
	 * 获取表名称
	 * 
	 * @return
	 */
	public String getTableName() {
		Table table = model.getAnnotation(Table.class);
		if (table == null) {
			throw new NullPointerException("模型[" + model.getName() + "]没有@Table.");
		}
		String tableName = table.value();
		if (StringUtils.isEmpty(tableName)) {
			tableName = StringUtil.camelToUnderline(model.getName());
		}
		return tableName;
	}

}
