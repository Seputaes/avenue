/*
 * Copyright (c) 2019 sep.gg <seputaes@sep.gg>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package gg.sep.avenue.router.core;

import java.lang.annotation.Annotation;

import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;

import gg.sep.avenue.router.GET;
import gg.sep.avenue.router.HEAD;
import gg.sep.avenue.router.POST;

/**
 * Utilities for several shared operations throughout the router.
 */
@UtilityClass
public class RouterUtils {

    /**
     * Gets the string value of an annotation's field.
     *
     * This will throw a {@link ReflectiveOperationException} at runtime if
     * getting the annotation field fails, or if the annotation field
     * is not of type {@link String}.
     *
     * @param annotation The annotation for which to get the field.
     * @param field The field to get on the annotation.
     * @return The string value set on the field.
     */
    @SneakyThrows({ReflectiveOperationException.class, ClassCastException.class})
    public static String getAnnotationField(final Annotation annotation, final String field) {
        final Object pathValue = annotation.getClass().getDeclaredMethod(field).invoke(annotation);
        if (pathValue instanceof String) {
            return (String) pathValue;
        }
        return "";
    }

    /**
     * Gets the paths string array on route method annotations, such as
     * {@link GET}, {@link POST}, {@link HEAD}, etc.
     *
     * This will throw a {@link ReflectiveOperationException} at runtime if
     * getting the annotation field fails, or if the annotation field
     * is not a {@link String} array.
     *
     * @param annotation The annotation for which to get the paths field.
     * @return The paths string array on a route method annotation.
     */
    @SneakyThrows({ReflectiveOperationException.class, ClassCastException.class})
    public static String[] getAnnotationPaths(final Annotation annotation) {
        final Object pathValue = annotation.getClass().getDeclaredMethod("paths").invoke(annotation);
        if (pathValue.getClass().isArray()) {
            return (String[]) pathValue;
        }
        return new String[]{};
    }
}
