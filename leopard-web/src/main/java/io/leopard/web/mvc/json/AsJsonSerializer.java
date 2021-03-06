package io.leopard.web.mvc.json;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.BeanUtils;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.SerializerProvider;

import io.leopard.json.As;
import io.leopard.lang.util.BeanUtil;
import io.leopard.web.mvc.AbstractJsonSerializer;

/**
 * 支持单个和列表数据输出
 * 
 * @author 谭海潮
 *
 * @param <T>
 */
public abstract class AsJsonSerializer<T> extends AbstractJsonSerializer<Object> {

	@SuppressWarnings("unchecked")
	@Override
	public void out(Object value, JsonGenerator gen, SerializerProvider serializers) throws IOException, JsonProcessingException {
		// System.err.println("BaseJsonSerializer value:" + value);
		String fieldName = gen.getOutputContext().getCurrentName();
		Field field = getCurrentField(gen);
		field.setAccessible(true);
		As as = field.getAnnotation(As.class);
		if (as == null) {
			if (value instanceof List) {
				throw new RuntimeException("属性[" + fieldName + "]没有设置@As");
			}
			else {
				Object data = this.get((T) value, gen, field);
				Object currentValue = gen.getOutputContext().getCurrentValue();
				// TODO 这里需要忽略有值的属性
				BeanUtils.copyProperties(data, currentValue);
				gen.writeObject(value);
			}
			return;
		}
		Class<?> asClazz = as.value();
		// System.err.println("field:" + field.toGenericString());
		gen.writeObject(value);
		Object data;
		if (value instanceof List) {
			List<Object> list = new ArrayList<Object>();
			for (T key : (List<T>) value) {
				Object element = this.get(key, gen, field);
				list.add(BeanUtil.convert(element, asClazz));
			}
			data = list;
		}
		else {
			Object element = this.get((T) value, gen, field);
			data = BeanUtil.convert(element, asClazz);
		}
		String newFieldName = this.getFieldName(fieldName);
		gen.writeFieldName(newFieldName);
		gen.writeObject(data);
	}

	/**
	 * 获取VO的属性
	 * 
	 * @param gen
	 * @param fieldName
	 * @return
	 */
	protected Field getCurrentField(JsonGenerator gen) {
		Object currentValue = gen.getOutputContext().getCurrentValue();
		if (currentValue == null) {
			return null;
		}
		String fieldName = gen.getOutputContext().getCurrentName();
		Class<?> clazz = currentValue.getClass();
		try {
			Field field = clazz.getDeclaredField(fieldName);
			// field.setAccessible(true);
			return field;
		}
		catch (NoSuchFieldException e) {
			return null;
		}
		catch (SecurityException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	/**
	 * 获取VO的属性值
	 * 
	 * @param gen
	 * @param fieldName
	 * @return
	 */
	protected Object getFieldValue(JsonGenerator gen, String fieldName) {
		Object currentValue = gen.getOutputContext().getCurrentValue();
		if (currentValue == null) {
			return null;
		}
		Class<?> clazz = currentValue.getClass();
		try {
			Field field = clazz.getDeclaredField(fieldName);
			field.setAccessible(true);
			return field.get(currentValue);
		}
		catch (NoSuchFieldException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
		catch (SecurityException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
		catch (IllegalAccessException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	protected String getFieldName(String fieldName) {
		if ("uid".equals(fieldName)) {
			return "user";
		}
		if ("uidList".equals(fieldName)) {
			return "userList";
		}
		return fieldName.replace("Id", "");
	}

	public abstract Object get(T value, JsonGenerator gen, Field field);
}
