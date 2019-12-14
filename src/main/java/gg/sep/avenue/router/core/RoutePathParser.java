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

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import gg.sep.avenue.router.GET;
import gg.sep.avenue.router.HEAD;
import gg.sep.avenue.router.POST;
import gg.sep.avenue.router.Path;
import gg.sep.avenue.router.converter.IntegerTokenConverter;
import gg.sep.avenue.router.converter.StringTokenConverter;
import gg.sep.avenue.router.converter.TokenConverter;
import gg.sep.avenue.router.converter.UUIDTokenConverter;

/**
 * The {@link RoutePathParser} is responsible for taking a route path specified
 * in a route method's annotation ({@link GET}, {@link POST}, {@link HEAD}, etc)
 * and parsing it into a Regex {@link Pattern} which can be used both match
 * the HTTP request's path as well as create matching groups for each of the token
 * variables which might be part of that path, to be passed in as parameters to the
 * route's method.
 *
 * <p>For example given a route path of {@code @GET(paths = "/category/<int:id>")},
 * the {@link #buildRoutePattern(String)} method will parse this into a
 * pattern which will match HTTP request paths of {@code /category/123}, {@code /category/1},
 * etc, but not paths such as {@code /category/foo}. The pattern produced
 * additionally adds matcher groups for each of the variables and, in this case, w
 * would contain a single group with name "id".
 *
 * <p>These matcher groups are further used in {@link Path} annotations on
 * route method parameters to map the variable's id to its assocaited parameter.
 * This mapping, which is stored on each individual {@link Route}, can be constructed
 * using {@link #buildPathParameters(String)}.
 *
 * <p>Adapted from the werkzeug router's parser<br>
 * https://github.com/pallets/werkzeug/blob/0.16.0/src/werkzeug/routing.py<br>
 * werkzeug copyright: 2007 Pallets (BSD 3-Clause Revised)<br>
 * https://github.com/pallets/werkzeug/blob/0.16.0/LICENSE.rst
 */
public final class RoutePathParser {
    private static final Pattern TOKEN_BUILDER_PATTERN = Pattern.compile(
        "(?<static>[^<]*)" +
            "(<" +
            "(?<converter>[a-zA-Z_][a-zA-Z0-9_]*)" +
            ":" +
            "(?<variable>[a-zA-Z_][a-zA-Z-0.9]*)" +
            ">)?"
    );

    private Map<String, TokenConverter<?>> tokenConverters = new ConcurrentHashMap<>();

    private RoutePathParser() {}

    /**
     * Adds a new token converter to the parser.
     *
     * If a token converter with the same name already exists, an
     * {@link IllegalArgumentException} wil lbe thrown.
     *
     * @param tokenConverter The token converter to add to the parser.
     */
    public void addTokenConverter(final TokenConverter<?> tokenConverter) {
        final String tokenName = tokenConverter.getName();
        if (tokenConverters.putIfAbsent(tokenName, tokenConverter) != null) {
            throw new IllegalArgumentException(
                String.format("A token converter with name '%s' is already registered.", tokenName));
        }
    }

    /**
     * Builds a {@link Route}'s {@link Pattern} which can match request paths
     * and be used to parse the route path's tokens into parameters.
     *
     * @param routePath The route's path string.
     * @return A pattern with the token's replaced by each token type's matching pattern
     *         which will match a final request's path.
     */
    public Pattern buildRoutePattern(final String routePath) {
        final StringBuilder finalPatternBuilder = new StringBuilder();
        finalPatternBuilder.append("^"); // pattern should match against a full string

        final Matcher matcher = TOKEN_BUILDER_PATTERN.matcher(routePath);
        while (matcher.find()) {
            // static portion cannot ever be null, since even an empty string or a token
            // by itself will result in an empty string static portion
            final String staticPortion = matcher.group("static");
            finalPatternBuilder.append(staticPortion);

            final String converter = matcher.group("converter");
            final String variable = matcher.group("variable");
            if (converter == null) { // converter will be null if the match consists only of a static group
                continue;
            }
            final TokenConverter<?> tokenConverter = tokenConverters.get(converter);
            if (tokenConverter == null) {
                throw new IllegalStateException("Unknown token converter for type: " + converter);
            }
            // append name group
            finalPatternBuilder.append("(?<").append(variable).append(">");
            finalPatternBuilder.append(tokenConverter.getTokenPattern());
            finalPatternBuilder.append(")");
        }

        finalPatternBuilder.append("$");
        return Pattern.compile(finalPatternBuilder.toString());
    }

    /**
     * Builds a mapping of token variable ids to their converters.
     *
     * This can be used when constructing arguments to pass into the route's method
     * using the {@link Path} parameter annotation to identify the variable's ID.
     *
     * @param routePath The route's path string.
     * @return Mapping of path parameter IDs to the token converters used
     *         to parse that portion of the path.
     */
    public Map<String, TokenConverter<?>> buildPathParameters(final String routePath) {
        final Map<String, TokenConverter<?>> usedTypes = new HashMap<>();
        final Matcher matcher = TOKEN_BUILDER_PATTERN.matcher(routePath);
        while (matcher.find()) {
            final String converter = matcher.group("converter");
            final String variable = matcher.group("variable");
            if (converter == null) {
                continue;
            }
            final TokenConverter<?> tokenConverter = tokenConverters.get(converter);
            if (tokenConverter == null) {
                throw new IllegalStateException("Unknown token converter for type: " + converter);
            }
            usedTypes.put(variable, tokenConverter);
        }
        return usedTypes;
    }

    /**
     * Creates a new instance of the {@link RoutePathParser} builder.
     * @return A new instance of the {@link RoutePathParser} builder.
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Creates a default instance of {@link RoutePathParser} using the
     * preexisting {@link TokenConverter}'s for Strings, Integers, and UUIDs.
     * @return A default instance of {@link RoutePathParser} using
     *         the preexisting {@link TokenConverter}'s.
     */
    public static RoutePathParser defaultParser() {
        return builder()
            .withTokenConverter(new StringTokenConverter())
            .withTokenConverter(new IntegerTokenConverter())
            .withTokenConverter(new UUIDTokenConverter())
            .build();
    }

    /**
     * Builder class for the {@link RoutePathParser} which can be used
     * chain construct a parser with multiple {@link TokenConverter}'s.
     */
    public static final class Builder {
        private Map<String, TokenConverter<?>> tokenConverters = new HashMap<>();

        /**
         * Adds a new token converter to the builder.
         * @param tokenConverter The token converter to add to the builder.
         * @return The builder instance.
         */
        public Builder withTokenConverter(final TokenConverter<?> tokenConverter) {
            this.tokenConverters.put(tokenConverter.getName(), tokenConverter);
            return this;
        }

        /**
         * Constructs the {@link RoutePathParser} using the specified parameters
         * from the builder.
         * @return A new instance of {@link RoutePathParser} using the specified
         * parameters from the builder.
         */
        public RoutePathParser build() {
            final RoutePathParser routePathParser = new RoutePathParser();
            routePathParser.tokenConverters.putAll(tokenConverters);
            return routePathParser;
        }
    }
}
