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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import com.amazonaws.serverless.proxy.model.AwsProxyResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Unit tests for {@link AwsResponseBuilder}.
 */
@SuppressWarnings("checkstyle:MagicNumbers")
public class AwsResponseBuilderTest {

    @Test
    void defaultBuilder_StatusDefaultsTo200OK() {
        final AwsProxyResponse response = AwsResponseBuilder.newBuilder().build();
        assertEquals(200, response.getStatusCode());
    }

    @Test
    void status_setsStatusCode() {
        final AwsProxyResponse singleStatus = AwsResponseBuilder.newBuilder()
            .status(404)
            .build();
        assertEquals(404, singleStatus.getStatusCode());
        final AwsProxyResponse overwriteStatus = AwsResponseBuilder.newBuilder()
            .status(404)
            .status(301)
            .build();
        assertEquals(301, overwriteStatus.getStatusCode());
    }

    @Test
    void redirect_setsStatusCodeAndHeader() throws Exception {
        final URL location = new URL("https://www.example.com");
        final AwsProxyResponse response = AwsResponseBuilder.newBuilder()
            .redirect(location, 301)
            .build();
        assertEquals(location.toExternalForm(), response.getMultiValueHeaders().getFirst("Location"));
        assertEquals(301, response.getStatusCode());

        final AwsProxyResponse anotherResponse = AwsResponseBuilder.newBuilder()
            .redirect(location, 302)
            .build();
        assertEquals(302, anotherResponse.getStatusCode());
    }

    @ParameterizedTest
    @ValueSource(ints = {200, 404, 500})
    void redirect_InvalidRedirectStatus_ThrowsException(final int invalidStatus) throws Exception {
        final URL url = new URL("https://www.example.com");
        assertThrows(IllegalArgumentException.class, () -> AwsResponseBuilder.newBuilder().redirect(url, invalidStatus));
    }

    @Test
    void setHeader_NewValue_IsSet() {
        final AwsProxyResponse response = AwsResponseBuilder.newBuilder()
            .setHeader("foo", "bar")
            .build();
        assertEquals("bar", response.getMultiValueHeaders().getFirst("foo"));
    }

    @Test
    void setHeader_MultipleValues_ReplacesOldValue() {
        final AwsProxyResponse response = AwsResponseBuilder.newBuilder()
            .setHeader("foo", "bar")
            .setHeader("foo", "baz")
            .build();
        assertEquals("baz", response.getMultiValueHeaders().getFirst("foo"));
        assertEquals(1, response.getMultiValueHeaders().get("foo").size());
    }

    @Test
    void html_setsContentType() {
        final AwsProxyResponse response = AwsResponseBuilder.newBuilder()
            .html()
            .build();
        final AwsProxyResponse overwriteContentType = AwsResponseBuilder.newBuilder()
            .setHeader("Content-Type", "foo")
            .html()
            .build();
        assertEquals("text/html", response.getMultiValueHeaders().getFirst("Content-Type"));
        assertEquals("text/html", overwriteContentType.getMultiValueHeaders().getFirst("Content-Type"));
    }

    @Test
    void json_setsContentType() {
        final AwsProxyResponse response = AwsResponseBuilder.newBuilder()
            .json()
            .build();
        final AwsProxyResponse overwriteContentType = AwsResponseBuilder.newBuilder()
            .setHeader("Content-Type", "foo")
            .json()
            .build();
        assertEquals("application/json", response.getMultiValueHeaders().getFirst("Content-Type"));
        assertEquals("application/json", overwriteContentType.getMultiValueHeaders().getFirst("Content-Type"));
    }

    @Test
    void stringBody_setsBody_setsBase64ToFalse() {
        final AwsProxyResponse response = AwsResponseBuilder.newBuilder()
            .stringBody("foo")
            .build();
        assertEquals("foo", response.getBody());
        assertFalse(response.isBase64Encoded());
    }

    @Test
    void binaryBody_setsBody_setsBase64ToTrue() {
        final byte[] body = "foo".getBytes(StandardCharsets.UTF_8);
        final AwsProxyResponse response = AwsResponseBuilder.newBuilder()
            .binaryBody(body)
            .build();
        assertEquals(Base64.getEncoder().encodeToString(body), response.getBody());
        assertTrue(response.isBase64Encoded());
    }
}
