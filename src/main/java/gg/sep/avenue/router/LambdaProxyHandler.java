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

import com.amazonaws.serverless.proxy.model.AwsProxyRequest;
import com.amazonaws.serverless.proxy.model.AwsProxyResponse;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;

import gg.sep.avenue.router.core.Route;

/**
 * {@link LambdaProxyHandler} is an extension of the AWS Lambda {@link RequestStreamHandler},
 * which allows for "hooks" that wrap the input request and output response.
 */
public interface LambdaProxyHandler extends RequestStreamHandler {

    /**
     * Hook for performing actions on the input stream, output stream, or context
     * of the Lambda event prior to parsing it into an {@link AwsProxyRequest}.
     *
     * <p>It is important to note that the AWS Lambda runtime's {@link InputStream}
     * not implement {@link InputStream#mark(int)} or {@link InputStream#reset()},
     * so it is not possible to read from the input stream twice without copying it.
     *
     * <p>If for some reason you to read from the input stream in this method, you will
     * need to read the whole stream into a byte array and store it on your class,
     * and then override {@link #parseInput(InputStream)} to create a secondary input stream
     * which can then be passed into {@code super}.
     *
     * @param inputStream Raw input stream passed from the Lambda runtime.
     * @param outputStream Raw output stream provided by the Lambda runtime.
     * @param context Context of the Lambda event.
     */
    void beforeParse(InputStream inputStream, OutputStream outputStream, Context context);

    /**
     * Parses the Lambda event's input stream into an {@link AwsProxyRequest}.
     *
     * <p>In most situations, the implementation that is present on
     * {@link AbstractLambdaProxyHandler#parseInput} will be sufficient, as it just
     * parses the JSON into the object with a default {@link com.fasterxml.jackson.databind.ObjectMapper} object.
     *
     * @param inputStream The raw input stream passed from the Lambda runtime.
     * @throws IOException Exception thrown if parsing the input into an {@link AwsProxyRequest} fails.
     * @return The parsed {@link AwsProxyRequest} from the Lambda's event input.
     */
    AwsProxyRequest parseInput(InputStream inputStream) throws IOException;

    /**
     * Hook which can be used to perform actions on the {@link AwsProxyRequest} prior to
     * it being handled.
     *
     * @param request The parsed {@link AwsProxyRequest}.
     */
    void beforeHandle(AwsProxyRequest request);

    /**
     * Hook which can be used to perform actions on the handle'd request and it's response
     * prior to it being sent back to the the Lambda's {@link OutputStream}.
     *
     * @param response The response from the {@link #handleRequest(InputStream, OutputStream, Context)}
     *                 method.
     */
    void beforeResponse(AwsProxyResponse response);

    /**
     * Hook which can be used to perform some set of actions after the response has
     * been sent back to the Lambda's output stram.
     */
    void afterResponse();

    /**
     * Hook which can be used to perform actions if invocation of a {@link Route}
     * fails.
     *
     * For example, this could be used to return standard Error page or document if there was
     * a server error.
     *
     * @param e The exception that was thrown during invocation of the {@link Route}.
     * @param outputStream The Lambda's output stream failed.
     * @throws IOException Exception thrown if writing to the output stream failed.
     *                     This exception should ultimately be bubbled up to
     *                     {@link RequestStreamHandler#handleRequest(InputStream, OutputStream, Context)}
     *                     and back out to the Lambda if this step fails.
     */
    void handleInvokeError(Exception e, OutputStream outputStream) throws IOException;
}
