/**
 * @file UtilityMethods.java
 * @company Techversant Infotech
 * @author Nipin J George
 * @date August 29,2025
 * @version 1.0
 * @description Utility class providing common helper methods.
 */

package com.techversant.customer_service.utils;

import com.techversant.customer_service.utils.exceptions.FailedToFilterCustomersException;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;

import static com.techversant.customer_service.utils.Constants.FAILED_TO_FILTER_USERS;

public class UtilityMethods {

    private UtilityMethods() {
        throw new UnsupportedOperationException("Utility class - cannot be instantiated.");
    }

    /**
     * Processes and sanitizes string filter parameters in the given object for customer filtering.
     * For each string property (except "sortField" and "status"), if the value is blank, it sets the property to {@code null}. Otherwise, it trims, converts the value to lowercase, and wraps it with '%' wildcards for SQL LIKE queries.
     *
     * @param object the filter object whose string properties need to be sanitized
     * @throws FailedToFilterCustomersException if any reflection-related error occurs during processing
     */
    public static void filterParameterCheckerForViewAllCustomers(Object object) {
        try {
            for (PropertyDescriptor propertyDescriptor : Introspector.getBeanInfo(object.getClass(), Object.class).getPropertyDescriptors()) {
                Method getterMethod = propertyDescriptor.getReadMethod();
                Method setterMethod = propertyDescriptor.getWriteMethod();
                if (getterMethod != null && setterMethod != null && propertyDescriptor.getPropertyType() == String.class && !propertyDescriptor.getName().equals("sortField") && !propertyDescriptor.getName().equals("status")) {
                    Object value = getterMethod.invoke(object);
                    if (value != null) {
                        String stringValue = value.toString();
                        if (stringValue.isBlank()) {
                            setterMethod.invoke(object, (Object) null);
                        } else {
                            setterMethod.invoke(object, "%" + stringValue.trim().toLowerCase() + "%");
                        }
                    }
                }
            }
        } catch (IntrospectionException | ReflectiveOperationException e) {
            throw new FailedToFilterCustomersException(FAILED_TO_FILTER_USERS);
        }
    }
}
