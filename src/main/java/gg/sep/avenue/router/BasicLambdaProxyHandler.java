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
import java.io.OutputStream;
import java.util.Optional;

import com.amazonaws.serverless.proxy.model.AwsProxyRequest;
import com.amazonaws.serverless.proxy.model.AwsProxyResponse;
import com.amazonaws.services.lambda.runtime.Context;
import com.google.gson.Gson;

/**
 * Basic implementation of {@link LambdaProxyHandler} which services the most basic needs.
 *
 * <p>No before/after hooks are implemented, the request is simply parsed and handled by
 * the route and the response sent back to the output stream. If no route matching
 * the request was found, a simple 404 JSON message is returned.
 */
public class BasicLambdaProxyHandler extends AbstractLambdaProxyHandler {

    /**
     * Initializes the proxy handler with a default {@link Gson} object.
     */
    public BasicLambdaProxyHandler() {
        super(new Gson());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void handleRequest(final InputStream input, final OutputStream output,
                              final Context context) throws IOException {
        beforeParse(input, output, context);
        final AwsProxyRequest request = parseInput(input);
        beforeHandle(request);

        final Optional<Route> foundRoute = findRoute(request);
        if (foundRoute.isEmpty()) {
            sendResponse(notFound(), output);
            return;
        }
        final Route route = foundRoute.get();
        invokeAndRespond(route, request, output);
    }

    /**
     * Invokes the route with the request and sends the response back to the Lambda's
     * output stream.
     *
     * Exceptions thrown during the course of the invoke will be caught and
     * sent to {@link #handleInvokeError(Exception, OutputStream)} for processing.
     *
     * @param route The route to invoke.
     * @param request The AWS Lambda request which triggered the route.
     * @param output The output stream to send any responses to.
     * @throws IOException Exception thrown if sending the response to the output stream failed.
     */
    private void invokeAndRespond(final Route route, final AwsProxyRequest request,
                                  final OutputStream output) throws IOException {
        try {
            final AwsProxyResponse response = route.invoke(request);
            beforeResponse(response);
            sendResponse(response, output);
            afterResponse();
        } catch (final IOException e) {
            throw e; // catch the IOException from sendResponse and re-throw it
        } catch (final Exception e) {
            handleInvokeError(e, output);
        }
    }
}
