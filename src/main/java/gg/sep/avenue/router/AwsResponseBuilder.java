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

import java.net.URL;
import java.util.Base64;

import com.amazonaws.serverless.proxy.model.AwsProxyResponse;
import com.amazonaws.serverless.proxy.model.Headers;
import org.apache.http.HttpStatus;

/**
 * Builder class for more easily constructing {@link com.amazonaws.serverless.proxy.model.AwsProxyResponse}.
 */
public final class AwsResponseBuilder {

    private final AwsProxyResponse response = new AwsProxyResponse(HttpStatus.SC_OK);

    private AwsResponseBuilder() {
    }

    /**
     * Creates a new instance of the builder.
     * @return A new instance of the builder.
     */
    public static AwsResponseBuilder newBuilder() {
        return new AwsResponseBuilder();
    }

    // HTTP Statuses

    /**
     * Sets the given status on the response. It is recommended to use the statuses on
     * {@link HttpStatus}.
     *
     * @param statusCode Status code to set on the response.
     * @return The builder instance.
     */
    public AwsResponseBuilder status(final int statusCode) {
        response.setStatusCode(statusCode);
        return this;
    }

    /**
     * Sets the response to be a redirect to another URL.
     *
     * @param redirectLocation The URL which will be set as the "Location" header.
     * @param statusCode Status code of the response. If the status code is not a "3xx" class
     *                   code, an {@link IllegalArgumentException} will be thrown.
     * @return The builder instance.
     */
    public AwsResponseBuilder redirect(final URL redirectLocation, final int statusCode) {
        if (!String.valueOf(statusCode).startsWith("3")) {
            throw new IllegalArgumentException("Redirect status codes must be 3xx class");
        }
        replaceHeader("Location", redirectLocation.toExternalForm());
        return status(statusCode);
    }

    /**
     * Sets a header on the response, replacing any existing values for that header.
     *
     * @param name Name of the header.
     * @param value Value of the header.
     * @return The builder instance.
     */
    public AwsResponseBuilder setHeader(final String name, final String value) {
        replaceHeader(name, value);
        return this;
    }

    /**
     * Sets the content type of the response to be "text/html".
     * @return The builder instance.
     */
    public AwsResponseBuilder html() {
        setContentType("text/html");
        return this;
    }

    /**
     * Sets the content type of the response to be "application/json".
     * @return The builder instance.
     */
    public AwsResponseBuilder json() {
        setContentType("application/json");
        return this;
    }

    /**
     * Sets a string/UTF-8 body on the response.
     *
     * If you're sending Base64 encoded data, use {@link #binaryBody(byte[])} instead.
     * @param bodyText The text to send in the response body.
     * @return The builder instance.
     */
    public AwsResponseBuilder stringBody(final String bodyText) {
        response.setBase64Encoded(false);
        response.setBody(bodyText);
        return this;
    }

    /**
     * Sets a binary body on the response.
     *
     * This binary data will be Base64 encoded before being sent.
     * @param bytes The bytes of data to send in the response body.
     * @return The builder instance.
     */
    public AwsResponseBuilder binaryBody(final byte[] bytes) {
        response.setBase64Encoded(true);
        response.setBody(Base64.getEncoder().encodeToString(bytes));
        return this;
    }

    /**
     * Returns the built {@link AwsProxyResponse}.
     * @return The built response.
     */
    public AwsProxyResponse build() {
        return response;
    }

    /**
     * Updates the "Content-Type" header on the response.
     * @param value The value of the content type.
     */
    private void setContentType(final String value) {
        replaceHeader("Content-Type", value);
    }

    /**
     * Replaces a header on the response, if it exists, otherwise sets the header.
     *
     * @param name Name of the header to replace/update.
     * @param value Value of the header to replace/update.
     */
    private void replaceHeader(final String name, final String value) {
        if (response.getMultiValueHeaders() == null) {
            response.setMultiValueHeaders(new Headers());
        }
        final Headers currentHeaders = response.getMultiValueHeaders();
        currentHeaders.remove(name);
        currentHeaders.putSingle(name, value);
    }
}
