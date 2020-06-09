/**
 * 
 * Original file name: ClassUtil.java
 * Created by gannunziata
 */
package utils;

import java.lang.annotation.Annotation;

/**
 * 
 * @author gannunziata
 */
public class ClassUtil {
	public static boolean hasAnnotation(Class<?> class1, Class<? extends Annotation> annota) {
		if (class1 == null || annota == null)
			return false;

		if (class1.getAnnotation(annota) != null)
			return true;

		return false;
	}
}
