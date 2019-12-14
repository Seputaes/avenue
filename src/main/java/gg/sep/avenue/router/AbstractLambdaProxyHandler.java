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

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import com.amazonaws.serverless.proxy.model.AwsProxyRequest;
import com.amazonaws.serverless.proxy.model.AwsProxyResponse;
import com.amazonaws.services.lambda.runtime.Context;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;

import gg.sep.avenue.router.core.Route;

/**
 * Abstract implementation of {@link LambdaProxyHandler} that provides
 * most of the default functionality for handling AWS Lambda events.
 *
 * The most basic implementation is {@link BasicLambdaProxyHandler}.
 */
@Log4j2
public abstract class AbstractLambdaProxyHandler implements LambdaProxyHandler {

    @Getter(AccessLevel.PROTECTED)
    private ObjectMapper objectMapper;

    @Getter(AccessLevel.PROTECTED)
    private Set<RouteController> registeredControllers = ConcurrentHashMap.newKeySet();

    @Getter(AccessLevel.PROTECTED)
    private Set<Route> registeredRoutes = ConcurrentHashMap.newKeySet();

    /**
     * Creates an instance of the class using the specified {@link ObjectMapper} class,
     * which might contain custom type adapters for your own needs.
     *
     * @param objectMapper Instance of {@link ObjectMapper} to use.
     */
    public AbstractLambdaProxyHandler(final ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * Attempts to find the route which handles a given {@link AwsProxyRequest}.
     *
     * <p>If more than one route is thrown which could possibly handle the event,
     * an {@link IllegalStateException} will be thrown.
     *
     * @param request The input request to be used to find the route.
     * @return Returns an optional containing the route if one was found,
     *         otherwise an empty optional.
     */
    protected Optional<Route> findRoute(final AwsProxyRequest request) {
        final Set<Route> matchingRoutes = registeredRoutes.stream()
            .filter(route -> route.handlesRequest(request))
            .collect(Collectors.toSet());

        if (matchingRoutes.size() > 1) {
            final String msg = String.format(
                "More than one route handles request. path=%s, routes=%s",
                request.getPath(), matchingRoutes);
            log.error(msg);
            throw new IllegalStateException(msg);
        }
        return matchingRoutes.isEmpty() ? Optional.empty() : Optional.of(matchingRoutes.iterator().next());
    }

    /**
     * Register's a new {@link RouteController}, adding all of it's routes to the handler.
     *
     * By default, any duplicate {@link Route}'s found in the controller will cause
     * an exception to be thrown.
     *
     * @param controller Instance of a route controller to register.
     */
    protected void registerController(final RouteController controller) {
        registerController(controller, true);
    }

    /**
     * Performs the same actions as {@link #registerController(RouteController)}, but will ignore any
     * duplicate {@link Route}'s found if {@code skipDuplicateRoutes} is set to {@code true}.
     *
     * @param controller Instance of a route controller to register.
     * @param skipDuplicateRoutes Whether to skip/ignore any duplicate {@link Route}'s found on
     *                            the controller.
     */
    protected void registerController(final RouteController controller, final boolean skipDuplicateRoutes) {
        if (!registeredControllers.add(controller)) {
            log.info("Controller '{}' has already been registered. Skipping.", controller);
            return;
        }

        for (final Route route : controller.getRoutes()) {
            if (!registeredRoutes.add(route)) {
                if (skipDuplicateRoutes) {
                    log.info("Found duplicate route {}, skipping", route);
                    continue;
                }
                throw new IllegalArgumentException("Controller contains duplicate routes. Route: " + route);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AwsProxyRequest parseInput(final InputStream inputStream) throws IOException {
        final Reader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
        return objectMapper.readValue(reader, AwsProxyRequest.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void beforeParse(final InputStream inputStream, final OutputStream outputStream, final Context context) {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void beforeHandle(final AwsProxyRequest request) {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void beforeResponse(final AwsProxyResponse response) {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void afterResponse() {
    }

    @Override
    public void handleInvokeError(final Exception e, final OutputStream output) throws IOException {
        // TODO: Response Builder
        final AwsProxyResponse response = new AwsProxyResponse(500);
        response.setBody("Server Error");
        sendResponse(response, output);
    }

    /**
     * Serializes the {@link AwsProxyResponse} back to JSON, and sends the response to the
     * given {@link OutputStream}. The stream is then closed.
     *
     * @param response The response to serialize and send to the output stream.
     * @param outputStream The output stream to used when sending the response.
     * @throws IOException Exception thrown if writing to or closing the {@link OutputStream} fails.
     */
    protected void sendResponse(final AwsProxyResponse response, final OutputStream outputStream) throws IOException {
        final String payload = objectMapper.writeValueAsString(response);
        outputStream.write(payload.getBytes(StandardCharsets.UTF_8));
        outputStream.close();
    }
}
