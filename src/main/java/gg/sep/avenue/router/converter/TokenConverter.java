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

package gg.sep.avenue.router.converter;

import gg.sep.avenue.router.core.RoutePathParser;

/**
 * Describes a class that is used to parse route pattern tokens into their associated
 * types before passing into the route's method as a parameter.
 *
 * <p>For example, given a route with annotation containing {@code @GET(paths = "/category/<int:foo>")},
 * and a request of {@code GET /category/123}, the String value {@code "123"} would use the
 * {@link IntegerTokenConverter} to convert the string into an {@link Integer}.
 *
 * <p>These token converters can be registered on your Lambda's handler's route parser
 * using {@link RoutePathParser#addTokenConverter(TokenConverter)}.
 *
 * @param <T> The return type after the token input string has been parsed/converted.
 */
public interface TokenConverter<T> {

    /**
     * Gets the name or "key" of the token.
     *
     * <p>For example, given a token format of {@code <string:foo>},
     * this method should return "string".
     *
     * <p>This key needs to be unique per instance of {@link RoutePathParser}.
     * @return The name or "key" used to identify the token converter in a given route pattern.
     */
    String getName();

    /**
     * Returns a regex pattern which will accurately identify objects
     * of type {@code T}, and will be substituted in place of the route's token
     * in order to match a given request path.
     *
     * <p>For example, a {@link String} type would return a pattern of {@code [^/]{1,}}, which
     * would result in a route pattern of {@code @GET(paths = "/category/<string:foo>"} being
     * replaced with {@code "^/category/<?(foo)[^/]{1,}>$"}.
     *
     * @return The regex which should be used to replace the token in the final pattern.
     */
    String getTokenPattern();

    /**
     * Converts the path's input string into type {@code T}. This method takes
     * the raw string, and should account for it being in an URL-encoded format.
     *
     * @param value The raw string value of the token in the path.
     * @return The resulting object of type {@code T}.
     */
    T fromURLPath(String value);

    /**
     * Performs the reverse of {@link #fromURLPath(String)}, turning
     * an object {@code T} into a URL-encoded string.
     *
     * @param value The object T.
     * @return A URL-safe representation of the object {@code T}.
     */
    String toURLPath(T value);
}
