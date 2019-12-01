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
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;

import com.amazonaws.serverless.proxy.model.AwsProxyRequest;
import lombok.experimental.UtilityClass;

import gg.sep.avenue.router.Body;
import gg.sep.avenue.router.Header;
import gg.sep.avenue.router.Path;
import gg.sep.avenue.router.Query;
import gg.sep.avenue.router.converter.TokenConverter;

/**
 * Utility class which is responsible for getting route parameter data off of the
 * {@link AwsProxyRequest} request depending on the annotation present on the parameter.
 *
 * This class exposes an interface {@link ParameterEvaluator}, with the specific parameter annotation's
 * implementation retrieved from {@link #getEvaluator(Class)}.
 */
@UtilityClass
class ParameterUtils {

    private static final Map<Class<? extends Annotation>, ParameterEvaluator> PARAMETER_EVALUATORS = new HashMap<>();

    /**
     * Models a lambda which retrieves a parameter's value from a {@link AwsProxyRequest}
     * depending on the parameter's annotation type.
     */
    interface ParameterEvaluator {
        Object getParamValue(Annotation annotation, AwsProxyRequest request, Route route);
    }

    /*
     * Add the default parameter evaluators to the static map.
     */
    static {
        PARAMETER_EVALUATORS.put(Query.class, ParameterUtils::query);
        PARAMETER_EVALUATORS.put(Path.class, ParameterUtils::path);
        PARAMETER_EVALUATORS.put(Header.class, ParameterUtils::header);
        PARAMETER_EVALUATORS.put(Body.class, ParameterUtils::body);
    }

    /**
     * Retrieves the associated parameter evaluator for the given annotation class.
     * @param annotationClass The annotation's type/class.
     * @return The parameter evaluator associated with the given annotation type, if available.
     */
    static ParameterEvaluator getEvaluator(final Class<? extends Annotation> annotationClass) {
        return PARAMETER_EVALUATORS.get(annotationClass);
    }

    /**
     * Handles the retrieval of {@link Query} parameters off of a request for a given route.
     *
     * @param annotation The annotation on the route method parameter.
     * @param request The {@link AwsProxyRequest} request which triggered the route.
     * @param route The route which was triggered.
     * @return The query string value to pass into the parameter.
     */
    private static Object query(final Annotation annotation, final AwsProxyRequest request, final Route route) {
        final String value = RouterUtils.getAnnotationField(annotation, "value");
        return request.getMultiValueQueryStringParameters().getFirst(value);
    }

    /**
     * Handles the retrieval of {@link Header} parameters off of a request for a given route.
     *
     * @param annotation The annotation on the route method parameter.
     * @param request The {@link AwsProxyRequest} request which triggered the route.
     * @param route The route which was triggered.
     * @return The header string value to pass into the parameter.
     */
    private static Object header(final Annotation annotation, final AwsProxyRequest request, final Route route) {
        final String value = RouterUtils.getAnnotationField(annotation, "value");
        return request.getMultiValueHeaders().getFirst(value);
    }

    /**
     * Handles the retrieval of {@link Path} parameters off of a request for a given route.
     *
     * @param annotation The annotation on the route method parameter.
     * @param request The {@link AwsProxyRequest} request which triggered the route.
     * @param route The route which was triggered.
     * @return The parsed path value to pass into the parameter.
     */
    private static Object path(final Annotation annotation, final AwsProxyRequest request, final Route route) {
        final String value = RouterUtils.getAnnotationField(annotation, "value");
        final TokenConverter tokenConverter = route.getPathParameters().get(value);
        if (tokenConverter == null) {
            throw new IllegalStateException("Unknown token for Path parameter: " + value);
        }
        final Matcher matcher = route.getPattern().matcher(request.getPath());
        matcher.find(0); // matches the whole string, no repeats
        final String tokenValue = matcher.group(value);
        return tokenConverter.fromURLPath(tokenValue);
    }

    /**
     * Handles the retrieval of {@link Body} parameters off of a request for a given route.
     *
     * @param annotation The annotation on the route method parameter.
     * @param request The {@link AwsProxyRequest} request which triggered the route.
     * @param route The route which was triggered.
     * @return The body value to pass into the parameter.
     */
    private static Object body(final Annotation annotation, final AwsProxyRequest request, final Route route) {
        final String bodyValue = request.getBody();
        return request.isBase64Encoded() ?
            Base64.getDecoder().decode(bodyValue.getBytes(StandardCharsets.UTF_8)) : bodyValue;
    }
}
