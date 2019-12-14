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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import com.amazonaws.serverless.proxy.model.AwsProxyRequest;
import com.amazonaws.serverless.proxy.model.AwsProxyResponse;
import com.amazonaws.serverless.proxy.model.Headers;
import com.amazonaws.serverless.proxy.model.MultiValuedTreeMap;
import org.junit.jupiter.api.Test;

import gg.sep.avenue.router.RouteController;
import gg.sep.avenue.router.converter.TokenConverter;
import gg.sep.avenue.router.data.TestRouteController;

/**
 * Unit tests for {@link Route}.
 */
public class RouteTest {

    private static final RoutePathParser PARSER = RoutePathParser.defaultParser();

    private static Route route(final String routePath, final RouteController controller, final Method routeMethod) {
        return route(routePath, controller, routeMethod, PARSER.buildPathParameters(routePath));
    }

    private static Route route(final String routePath, final RouteController controller,
                               final Method routeMethod, final Map<String, TokenConverter<?>> pathParameters) {
        return Route.builder()
            .routeRequestMethod(RouteRequestMethod.GET)
            .controller(controller)
            .method(routeMethod)
            .pattern(PARSER.buildRoutePattern(routePath))
            .pathParameters(pathParameters)
            .build();
    }

    @Test
    void handlesRequest_MethodAndPathMatch_ReturnsTrue() {
        final AwsProxyRequest request = new AwsProxyRequest();
        request.setPath("/foo");
        request.setHttpMethod("GET");

        final Route route = Route.builder()
            .pattern(Pattern.compile("^/foo$"))
            .routeRequestMethod(RouteRequestMethod.GET)
            .build();

        assertTrue(route.handlesRequest(request));
    }

    @Test
    void handlesRequest_OnlyMethodMatches_ReturnsFalse() {
        final AwsProxyRequest request = new AwsProxyRequest();
        request.setPath("/foo");
        request.setHttpMethod("GET");

        final Route route = Route.builder()
            .pattern(Pattern.compile("^/foobar$"))
            .routeRequestMethod(RouteRequestMethod.GET)
            .build();

        assertFalse(route.handlesRequest(request));
    }

    @Test
    void handlesRequest_OnlyPathMatches_ReturnsFalse() {
        final AwsProxyRequest request = new AwsProxyRequest();
        request.setPath("/foo");
        request.setHttpMethod("POST");

        final Route route = Route.builder()
            .pattern(Pattern.compile("^/foo$"))
            .routeRequestMethod(RouteRequestMethod.GET)
            .build();

        assertFalse(route.handlesRequest(request));
    }

    @Test
    void handlesRequest_NeitherMatch_ReturnsTrue() {
        final AwsProxyRequest request = new AwsProxyRequest();
        request.setPath("/foo");
        request.setHttpMethod("POST");

        final Route route = Route.builder()
            .pattern(Pattern.compile("^/foobar$"))
            .routeRequestMethod(RouteRequestMethod.GET)
            .build();

        assertFalse(route.handlesRequest(request));
    }

    @Test
    void invoke_SingleSlash_ProcessesRequest() throws Exception {
        final AwsProxyRequest request = new AwsProxyRequest();
        final RouteController controller = new TestRouteController();
        final Route route = route("/", controller,
            controller.getClass().getMethod("singleSlash"));

        final AwsProxyResponse response = route.invoke(request);
        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
    }

    @Test
    void invoke_SingleStaticNoParams() throws Exception {
        final AwsProxyRequest request = new AwsProxyRequest();
        final RouteController controller = new TestRouteController();
        final Route route = route("/", controller,
            controller.getClass().getMethod("singleStaticNoParams"));

        final AwsProxyResponse response = route.invoke(request);
        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
    }

    @Test
    void invoke_SingleStaticRequestFirstParam() throws Exception {
        final AwsProxyRequest request = new AwsProxyRequest();
        request.setBody("foo");
        final RouteController controller = new TestRouteController();
        final Route route = route("/", controller,
            controller.getClass().getMethod("singleStaticRequestFirstParam", AwsProxyRequest.class));

        final AwsProxyResponse response = route.invoke(request);
        assertNotNull(response);
        assertEquals(request.getBody(), response.getBody());
    }

    @Test
    void invoke_StaticWithTokenString() throws Exception {
        final AwsProxyRequest request = new AwsProxyRequest();
        request.setPath("/static/foo");
        final RouteController controller = new TestRouteController();
        final Route route = route("/static/<string:foo>", controller,
            controller.getClass().getMethod("staticWithStringToken", String.class));

        final AwsProxyResponse response = route.invoke(request);
        assertNotNull(response);
        assertEquals("foo", response.getBody());
    }

    @Test
    void invoke_StaticWithTokenStringRequestFirstParam() throws Exception {
        final AwsProxyRequest request = new AwsProxyRequest();
        request.setPath("/static/foo");
        request.setBody("bar");
        final RouteController controller = new TestRouteController();
        final Route route = route("/static/<string:foo>", controller,
            controller.getClass().getMethod("staticWithStringTokenRequestFirstParam", AwsProxyRequest.class, String.class));

        final AwsProxyResponse response = route.invoke(request);
        assertNotNull(response);
        assertEquals("barfoo", response.getBody());
    }

    @Test
    void invoke_multiplePaths_FirstPath() throws Exception {
        final AwsProxyRequest request = new AwsProxyRequest();
        request.setPath("/static/123");

        final RouteController controller = new TestRouteController();
        final Route route = route("/static/<int:id>", controller,
            controller.getClass().getMethod("multiplePaths", Integer.class));

        final AwsProxyResponse response = route.invoke(request);
        assertNotNull(response);
        assertEquals("123", response.getBody());
    }

    @Test
    void invoke_multiplePaths_SecondPath() throws Exception {
        final AwsProxyRequest request = new AwsProxyRequest();
        request.setPath("/static/123/index");

        final RouteController controller = new TestRouteController();
        final Route route = route("/static/<int:id>/index", controller,
            controller.getClass().getMethod("multiplePaths", Integer.class));

        final AwsProxyResponse indexResponse = route.invoke(request);
        assertNotNull(indexResponse);
        assertEquals("123", indexResponse.getBody());
    }

    @Test
    void invoke_UnknownTokenType() throws Exception {
        final AwsProxyRequest request = new AwsProxyRequest();
        request.setPath("/foo");
        final RouteController controller = new TestRouteController();
        final Map<String, TokenConverter<?>> pathParameters = new HashMap<>();
        final Route route = route("/", controller,
            controller.getClass().getMethod("unknownTokenType", Object.class), pathParameters);

        assertThrows(IllegalStateException.class, () -> route.invoke(request));
    }

    @Test
    void invoke_InvalidReturnType() throws Exception {
        final AwsProxyRequest request = new AwsProxyRequest();
        final RouteController controller = new TestRouteController();
        final Route route = route("/", controller,
            controller.getClass().getMethod("invalidReturnType"));
        assertThrows(IllegalStateException.class, () -> route.invoke(request));
    }

    @Test
    void invoke_MultipleParameterAnnotations() throws Exception {
        final AwsProxyRequest request = new AwsProxyRequest();
        final RouteController controller = new TestRouteController();
        final Route route = route("/", controller,
            controller.getClass().getMethod("multipleAnnotations", String.class));
        assertThrows(IllegalStateException.class, () -> route.invoke(request));
    }

    @Test
    void invoke_NoParameterAnnotations() throws Exception {
        final AwsProxyRequest request = new AwsProxyRequest();
        final RouteController controller = new TestRouteController();
        final Route route = route("/", controller,
            controller.getClass().getMethod("noAnnotations", String.class));
        assertThrows(IllegalStateException.class, () -> route.invoke(request));
    }

    @Test
    void invoke_QueryParameter_SingleParameter() throws Exception {
        final AwsProxyRequest request = new AwsProxyRequest();
        final MultiValuedTreeMap<String, String> treeMap = new MultiValuedTreeMap<>();
        treeMap.add("foo", "bar");
        request.setMultiValueQueryStringParameters(treeMap);

        final RouteController controller = new TestRouteController();

        final Route route = route("/", controller,
            controller.getClass().getMethod("singleQueryParameter", String.class));
        final AwsProxyResponse response = route.invoke(request);
        assertNotNull(response);
        assertEquals("bar", response.getBody());
    }

    @Test
    void invoke_QueryParameter_MultipleParameter() throws Exception {
        final AwsProxyRequest request = new AwsProxyRequest();
        final MultiValuedTreeMap<String, String> treeMap = new MultiValuedTreeMap<>();
        treeMap.add("foo", "bar");
        treeMap.add("baz", "qux");
        request.setMultiValueQueryStringParameters(treeMap);

        final RouteController controller = new TestRouteController();

        final Route route = route("/", controller,
            controller.getClass().getMethod("multipleQueryParameters", String.class, String.class));
        final AwsProxyResponse response = route.invoke(request);
        assertNotNull(response);
        assertEquals("barqux", response.getBody());
    }

    @Test
    void invoke_QueryParameter_QueryParameterDoesNotExist() throws Exception {
        final AwsProxyRequest request = new AwsProxyRequest();
        final MultiValuedTreeMap<String, String> treeMap = new MultiValuedTreeMap<>();
        treeMap.add("baz", "bar");
        request.setMultiValueQueryStringParameters(treeMap);

        final RouteController controller = new TestRouteController();

        final Route route = route("/", controller,
            controller.getClass().getMethod("singleQueryParameter", String.class));
        final AwsProxyResponse response = route.invoke(request);
        assertNotNull(response);
        assertEquals("null", response.getBody());
    }

    @Test
    void invoke_HeaderParameter_SingleParameter() throws Exception {
        final AwsProxyRequest request = new AwsProxyRequest();
        final Headers headers = new Headers();
        headers.add("foo", "bar");
        request.setMultiValueHeaders(headers);

        final RouteController controller = new TestRouteController();

        final Route route = route("/", controller,
            controller.getClass().getMethod("singleHeaderParameter", String.class));
        final AwsProxyResponse response = route.invoke(request);
        assertNotNull(response);
        assertEquals("bar", response.getBody());
    }

    @Test
    void invoke_HeaderParameter_MultipleParameter() throws Exception {
        final AwsProxyRequest request = new AwsProxyRequest();
        final Headers headers = new Headers();
        headers.add("foo", "bar");
        headers.add("baz", "qux");
        request.setMultiValueHeaders(headers);

        final RouteController controller = new TestRouteController();

        final Route route = route("/", controller,
            controller.getClass().getMethod("multipleHeaderParameters", String.class, String.class));
        final AwsProxyResponse response = route.invoke(request);
        assertNotNull(response);
        assertEquals("barqux", response.getBody());
    }

    @Test
    void invoke_HeaderParameter_QueryParameterDoesNotExist() throws Exception {
        final AwsProxyRequest request = new AwsProxyRequest();
        final Headers headers = new Headers();
        headers.add("baz", "bar");
        request.setMultiValueHeaders(headers);

        final RouteController controller = new TestRouteController();

        final Route route = route("/", controller,
            controller.getClass().getMethod("singleHeaderParameter", String.class));
        final AwsProxyResponse response = route.invoke(request);
        assertNotNull(response);
        assertEquals("null", response.getBody());
    }

    @Test
    void invoke_BodyParameter_ValidBody() throws Exception {
        final AwsProxyRequest request = new AwsProxyRequest();
        request.setBody("foo");

        final RouteController controller = new TestRouteController();

        final Route route = route("/", controller,
            controller.getClass().getMethod("bodyHandler", String.class));
        final AwsProxyResponse response = route.invoke(request);
        assertNotNull(response);
        assertEquals("foo", response.getBody());
    }

    @Test
    void invoke_BodyParameter_base64EncodedBody() throws Exception {
        final String encoded = "Zm9v";
        final String decoded = "foo";

        final AwsProxyRequest request = new AwsProxyRequest();
        request.setBody(encoded);
        request.setIsBase64Encoded(true);

        final RouteController controller = new TestRouteController();

        final Route route = route("/", controller,
            controller.getClass().getMethod("b64BodyHandler", byte[].class));
        final AwsProxyResponse response = route.invoke(request);
        assertNotNull(response);
        assertEquals(decoded, response.getBody());
    }

    @Test
    void invoke_UnknownParameterAnnotation() throws Exception {
        final AwsProxyRequest request = new AwsProxyRequest();

        final RouteController controller = new TestRouteController();

        final Route route = route("/", controller,
            controller.getClass().getMethod("unknownAnnotation", String.class));

        assertThrows(IllegalStateException.class, () -> route.invoke(request));
    }

    @Test
    void equals_PathAndPatternMatch_ReturnsTrue() {
        final Route route1 = Route.builder()
            .routeRequestMethod(RouteRequestMethod.GET)
            .pattern(Pattern.compile("^/foo$"))
            .build();
        final Route route2 = Route.builder()
            .routeRequestMethod(RouteRequestMethod.GET)
            .pattern(Pattern.compile("^/foo$"))
            .build();
        assertEquals(route1, route2);
        assertEquals(route1.hashCode(), route2.hashCode());
    }

    @Test
    void equals_OnlyPathMatches_ReturnsFalse() {
        final Route route1 = Route.builder()
            .routeRequestMethod(RouteRequestMethod.GET)
            .pattern(Pattern.compile("^/foo$"))
            .build();
        final Route route2 = Route.builder()
            .routeRequestMethod(RouteRequestMethod.GET)
            .pattern(Pattern.compile("^/foobar$"))
            .build();
        assertNotEquals(route1, route2);
        assertNotEquals(route1.hashCode(), route2.hashCode());
    }

    @Test
    void equals_OnlyPatternMatches_ReturnsFalse() {
        final Route route1 = Route.builder()
            .routeRequestMethod(RouteRequestMethod.GET)
            .pattern(Pattern.compile("^/foo$"))
            .build();
        final Route route2 = Route.builder()
            .routeRequestMethod(RouteRequestMethod.POST)
            .pattern(Pattern.compile("^/foo$"))
            .build();
        assertNotEquals(route1, route2);
        assertNotEquals(route1.hashCode(), route2.hashCode());
    }

    @Test
    void equals_NeitherMatch_ReturnsFalse() {
        final Route route1 = Route.builder()
            .routeRequestMethod(RouteRequestMethod.GET)
            .pattern(Pattern.compile("^/foobar$"))
            .build();
        final Route route2 = Route.builder()
            .routeRequestMethod(RouteRequestMethod.POST)
            .pattern(Pattern.compile("^/foo$"))
            .build();
        assertNotEquals(route1, route2);
        assertNotEquals(route1.hashCode(), route2.hashCode());
    }

    @Test
    void equals_DifferentType_ReturnsFalse() {
        final Route route1 = Route.builder()
            .routeRequestMethod(RouteRequestMethod.GET)
            .pattern(Pattern.compile("^/foobar$"))
            .build();
        assertNotEquals(route1, "");
    }
}
