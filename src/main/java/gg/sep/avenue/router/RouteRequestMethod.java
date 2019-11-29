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

package gg.sep.avenue.router;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lombok.Getter;

/**
 * Enum which holds the possible HTTP methods supported by the router,
 * and maps the enum to the route method annotation class which
 * represents that HTTP Method.
 */
@Getter
public enum RouteRequestMethod {

    DELETE(gg.sep.avenue.router.DELETE.class),
    GET(gg.sep.avenue.router.GET.class),
    HEAD(gg.sep.avenue.router.HEAD.class),
    OPTIONS(gg.sep.avenue.router.OPTIONS.class),
    PATCH(gg.sep.avenue.router.PATCH.class),
    POST(gg.sep.avenue.router.POST.class),
    PUT(gg.sep.avenue.router.PUT.class);

    private Class<? extends Annotation> annotationClass;

    /**
     * Constructs the enum using the given annotation class which represents
     * this enum value.
     * @param annotationClass The annotation class which represents this enum value.
     */
    RouteRequestMethod(final Class<? extends Annotation> annotationClass) {
        this.annotationClass = annotationClass;
    }

    /**
     * Gets the enum assocaited with the given annotation. If it is not a valid/supported
     * annotation, and {@link IllegalArgumentException} will be thrown.
     *
     * @param annotation The annotation for which to get the {@link RouteRequestMethod}.
     * @return The {@link RouteRequestMethod} associated with the given annotation.
     */
    public static RouteRequestMethod forAnnotation(final Annotation annotation) {
        return Stream.of(RouteRequestMethod.values())
            .filter(m -> m.getAnnotationClass().equals(annotation.annotationType()))
            .findFirst()
            .orElseThrow(IllegalArgumentException::new);
    }

    /**
     * Gets a list of supported annotation classes.
     * @return A list of supported annotation classes.
     */
    public static List<Class<? extends Annotation>> getValidAnnotations() {
        return Stream.of(RouteRequestMethod.values())
            .map(RouteRequestMethod::getAnnotationClass)
            .collect(Collectors.toList());
    }
}
