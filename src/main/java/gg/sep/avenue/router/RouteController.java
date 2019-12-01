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

import java.util.Set;

import gg.sep.avenue.router.core.Route;
import gg.sep.avenue.router.core.RouteRequestMethod;

/**
 * Interface for modeling route controllers which are made up
 * of individual methods which handle HTTP requests.
 *
 * <p>Public methods in the class can be marked with the request method
 * annotations in order to designate them as a route handler method.
 * These annotations are such as {@link GET}, {@link POST}, {@link HEAD}, etc.
 *
 * <p>A full list of valid HTTP methods and their annotations can be found in
 * {@link RouteRequestMethod}.
 */
public interface RouteController {
    /**
     * Returns the full set of routes contained in this controller.
     *
     * The standard implementation of this method is present on {@link AbstractRouteController}.
     *
     * @return The full set of routes contained in this controller.
     */
    Set<Route> getRoutes();
}
