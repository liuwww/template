package io.github.liuwww.common.util;

import java.util.Date;

import org.apache.commons.beanutils.converters.DateTimeConverter;

/**
 * @Desc:
 * @author liuwww
 */
public class DateConverter extends DateTimeConverter {
	public DateConverter() {
		super();
	}

	public DateConverter(Object defaultValue) {
		super(defaultValue);
	}

	@Override
	protected Class<?> getDefaultType() {
		return Date.class;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	protected Object convertToType(Class targetType, Object value) throws Exception {
		if (value == null) {
			return null;
		}
		return super.convertToType(targetType, value);
	}

}
