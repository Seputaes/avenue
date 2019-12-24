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

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link RouterUtils}.
 */
public class RouterUtilsTest {

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.PARAMETER)
    @interface TestAnnotation {
        String field() default "";
        String[] array();
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.PARAMETER)
    @interface TestPathsAnnotation {
        String[] paths();
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.PARAMETER)
    @interface TestPathsDifferentTypeAnnotation {
        String paths() default "";
    }

    static final class TestClass {
        void foo(@TestAnnotation(field = "helloWorld", array = {"foo", "bar"}) final String foo) {

        }

        void paths(@TestPathsAnnotation(paths = {"foo", "bar"}) final String foo) {

        }

        void pathsDifferentType(@TestPathsDifferentTypeAnnotation() final String foo) {}
    }

    @Test
    void getAnnotationField_ValidField() throws Exception {
        final TestClass testClass = new TestClass();
        final Method method = testClass.getClass().getDeclaredMethod("foo", String.class);
        final Annotation annotation = method.getParameters()[0].getAnnotation(TestAnnotation.class);

        final String value = RouterUtils.getAnnotationField(annotation, "field");
        assertEquals("helloWorld", value);
    }

    @Test
    void getAnnotationField_InvalidField_ThrowsException() throws Exception {
        final TestClass testClass = new TestClass();
        final Method method = testClass.getClass().getDeclaredMethod("foo", String.class);
        final Annotation annotation = method.getParameters()[0].getAnnotation(TestAnnotation.class);

        assertThrows(NoSuchMethodException.class, () -> RouterUtils.getAnnotationField(annotation, "invalid"));
    }

    @Test
    void getAnnotationField_AnnotationIsNotString_ReturnsEmptyString() throws Exception {
        final TestClass testClass = new TestClass();
        final Method method = testClass.getClass().getDeclaredMethod("foo", String.class);
        final Annotation annotation = method.getParameters()[0].getAnnotation(TestAnnotation.class);

        assertEquals("", RouterUtils.getAnnotationField(annotation, "array"));
    }

    @Test
    void getAnnotationPaths_ValidField() throws Exception {
        final TestClass testClass = new TestClass();
        final Method method = testClass.getClass().getDeclaredMethod("paths", String.class);
        final Annotation annotation = method.getParameters()[0].getAnnotation(TestPathsAnnotation.class);

        final String[] value = RouterUtils.getAnnotationPaths(annotation);
        assertArrayEquals(new String[] {"foo", "bar"}, value);
    }

    @Test
    void getAnnotationPaths_InvalidField_ThrowsException() throws Exception {
        final TestClass testClass = new TestClass();
        final Method method = testClass.getClass().getDeclaredMethod("foo", String.class);
        final Annotation annotation = method.getParameters()[0].getAnnotation(TestAnnotation.class);

        assertThrows(NoSuchMethodException.class, () -> RouterUtils.getAnnotationPaths(annotation));
    }

    @Test
    void getAnnotationPaths_AnnotationIsNotString_ReturnsEmptyArray() throws Exception {
        final TestClass testClass = new TestClass();
        final Method method = testClass.getClass().getDeclaredMethod("pathsDifferentType", String.class);
        final Annotation annotation = method.getParameters()[0].getAnnotation(TestPathsDifferentTypeAnnotation.class);

        assertArrayEquals(new String[] {}, RouterUtils.getAnnotationPaths(annotation));
    }
}
