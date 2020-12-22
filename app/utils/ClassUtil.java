/**
 * Copyright 2014 by Eclettica s.r.l. All Rights Reserved.
 *
 * This file is part of EdicolaCommon project.
 *
 * The contents of this file are subject to the Eclettica Private License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.eclettica.net/
 * 
 * Original file name: ClassUtil.java
 * Created by gannunziata at 15:48:48
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
