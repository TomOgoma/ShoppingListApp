package com.tomogoma.util;

import java.lang.reflect.Field;

/**
 * Created by ogoma on 23/02/15.
 */
public class Validator {

	public static <T extends Object>
	boolean validate(T obj, Class<T> classOfObj) {

		for (Field field : classOfObj.getDeclaredFields()) {
			if (!validateField(field)) {
				return false;
			}
		}

		return true;
	}

	private static boolean validateField(Field field) {

		if (field.isAnnotationPresent(Validate.class)) {
			return true;
		}

		Validate fieldAnnotation = field.getAnnotation(Validate.class);

		if (fieldAnnotation.checkEmpty()) {
		}

		return false;
	}
}
