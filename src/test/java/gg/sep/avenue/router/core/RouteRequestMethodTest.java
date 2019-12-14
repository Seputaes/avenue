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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;

import gg.sep.avenue.router.DELETE;
import gg.sep.avenue.router.GET;
import gg.sep.avenue.router.HEAD;
import gg.sep.avenue.router.OPTIONS;
import gg.sep.avenue.router.PATCH;
import gg.sep.avenue.router.POST;
import gg.sep.avenue.router.PUT;

/**
 * Unit tests for {@link RouteRequestMethod}.
 */
public class RouteRequestMethodTest {

    @ParameterizedTest
    @ValueSource(classes = {GET.class, DELETE.class, POST.class, PATCH.class, PUT.class, OPTIONS.class, HEAD.class})
    <T> void forAnnotation_ValidAnnotation_ReturnsCorrectEnum(final Class<T> clazz) {
        final T mockT = Mockito.mock(clazz);
        // verify the input
        assertTrue(Annotation.class.isAssignableFrom(mockT.getClass()));
        Mockito.doReturn(clazz).when((Annotation) mockT).annotationType();

        final RouteRequestMethod requestMethod = RouteRequestMethod.forAnnotation((Annotation) mockT);
        assertNotNull(requestMethod);
        assertEquals(clazz, requestMethod.getAnnotationClass());
    }

    @Test
    void forAnnotation_InvalidAnnotation_ThrowsException() {
        final Annotation annotation = Mockito.mock(Annotation.class);
        Mockito.doReturn(Annotation.class).when(annotation).annotationType();

        assertThrows(IllegalArgumentException.class, () -> RouteRequestMethod.forAnnotation(annotation));
    }

    @Test
    void getValidAnnotations_ContainsAllValues() {
        final Set<Class<? extends Annotation>> validAnnotations = new HashSet<>(RouteRequestMethod.getValidAnnotations());
        assertEquals(RouteRequestMethod.values().length, validAnnotations.size());

        for (final RouteRequestMethod routeRequestMethod : RouteRequestMethod.values()) {
            assertTrue(validAnnotations.contains(routeRequestMethod.getAnnotationClass()));
        }
    }
}
