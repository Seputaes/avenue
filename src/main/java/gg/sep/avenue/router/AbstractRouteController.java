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

import static java.lang.String.format;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.amazonaws.serverless.proxy.model.AwsProxyResponse;
import lombok.extern.log4j.Log4j2;
import org.apache.logging.log4j.Level;

/**
 * Abstract implementation of a {@link RouteController}.
 *
 * Generally, implementations of {@link RouteController} should extend
 * this abstract class, as it provides most of the necessary functionality
 * surrounding building and finding routes which match Lambda requests.
 */
@Log4j2
public abstract class AbstractRouteController implements RouteController {

    private RoutePathParser parser;

    /**
     * Constructs the route controller using the specified path parser.
     * @param parser Route path parser to use for this controller.
     */
    protected AbstractRouteController(final RoutePathParser parser) {
        this.parser = parser;
    }

    /**
     * Constructs the route controller using the default path parser.
     */
    protected AbstractRouteController() {
        this.parser = RoutePathParser.defaultParser();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<Route> getRoutes() {
        final Collection<Class<? extends Annotation>> validAnnotations = RouteRequestMethod.getValidAnnotations();

        final List<Method> publicMethods = getHandlerMethods();
        final Set<Route> foundRoutes = new HashSet<>();

        for (final Method method : publicMethods) {
            for (final Annotation annotation : method.getAnnotations()) {
                if (!validAnnotations.contains(annotation.annotationType())) {
                    continue;
                }

                // build the route for this method
                final Set<Route> routes = buildRoutes(annotation, method);
                // check if there's a duplicate route within the controller
                for (final Route route : routes) {
                    if (!foundRoutes.add(route)) {
                        throw log.throwing(Level.ERROR,
                            new IllegalStateException(
                                format("Controller '%s' contains duplicate routes: %s", this, route)));
                    }
                }
            }
        }
        return foundRoutes;
    }

    /**
     * Builds each one of the routes mapped to a route handler method along with it's annotation.
     *
     * For each path that is part of the annotation's paths, a single route will be generated.
     *
     * @param annotation The annotation on the method.
     * @param method The route handler method.
     * @return A set of routes which are mapped to this method.
     */
    private Set<Route> buildRoutes(final Annotation annotation, final Method method) {
        final Set<Route> routes = new HashSet<>();
        for (final String routePath : RouterUtils.getAnnotationPaths(annotation)) {
            final Route route = Route.builder()
                .routeRequestMethod(RouteRequestMethod.forAnnotation(annotation))
                .controller(this)
                .method(method)
                .pathParameters(parser.buildPathParameters(routePath))
                .pattern(parser.buildRoutePattern(routePath))
                .build();
            routes.add(route);
        }
        return routes;
    }

    /**
     * Get a list of valid handler method on the controller.
     *
     * The handler methods must be public and must have a return type of {@link AwsProxyResponse}.
     *
     * @return List of valid handler methods on the route.
     */
    private List<Method> getHandlerMethods() {
        return Stream.of(getClass().getDeclaredMethods())
            .filter(method -> Modifier.isPublic(method.getModifiers()))
            .filter(method -> method.getReturnType().equals(AwsProxyResponse.class))
            .collect(Collectors.toList());
    }
}
