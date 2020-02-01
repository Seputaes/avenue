/*
 * Copyright (c) 2020 sep.gg <seputaes@sep.gg>
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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;

import com.amazonaws.serverless.proxy.internal.testutils.AwsProxyRequestBuilder;
import com.amazonaws.serverless.proxy.model.AwsProxyRequest;
import com.amazonaws.serverless.proxy.model.AwsProxyResponse;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;

import gg.sep.avenue.router.core.Route;

/**
 * Unit tests for {@link AbstractRouteController} implementation of {@link RouteController}.
 */
@SuppressWarnings({"checkstyle:missingjavadocmethod", "checkstyle:missingjavadoctype"})
public class RouteControllerTest {

    public static class RouteControllerTestController extends AbstractRouteController {
        @GET(paths = "/")
        public AwsProxyResponse basicRoute() {
            return AwsResponseBuilder.newBuilder()
                .status(HttpStatus.SC_OK)
                .build();
        }

        @Deprecated
        public AwsProxyResponse unknownAnnotation() {
            return null;
        }
    }

    public static class DuplicateRouteController extends AbstractRouteController {
        @GET(paths = "/foo")
        public AwsProxyResponse routeOne() {
            return null;
        }

        @GET(paths = "/foo")
        public AwsProxyResponse routeTwo() {
            return null;
        }
    }

    @Test
    void getRoutes_ReturnsSimpleRoutes() throws Exception {
        final RouteController testController = new RouteControllerTestController();
        final Set<Route> routes = testController.getRoutes();
        assertEquals(1, routes.size());
        final Route route = routes.iterator().next();

        final AwsProxyRequest request = new AwsProxyRequestBuilder()
            .path("/")
            .method("GET")
            .build();
        assertTrue(route.handlesRequest(request));
        assertEquals(HttpStatus.SC_OK, route.invoke(request).getStatusCode());
    }

    @Test
    void getRoutes_DuplicateRouteThrowsException() {
        final RouteController testController = new DuplicateRouteController();
        assertThrows(IllegalStateException.class, testController::getRoutes);
    }
}
