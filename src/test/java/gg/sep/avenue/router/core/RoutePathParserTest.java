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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import gg.sep.avenue.router.converter.IntegerTokenConverter;
import gg.sep.avenue.router.converter.StringTokenConverter;
import gg.sep.avenue.router.converter.TokenConverter;
import gg.sep.avenue.router.converter.UUIDTokenConverter;

/**
 * Unit tests for {@link RoutePathParser}.
 */
public class RoutePathParserTest {

    private static final StringTokenConverter STRING_CONVERTER = new StringTokenConverter();
    private static final IntegerTokenConverter INT_CONVERTER = new IntegerTokenConverter();
    private static final UUIDTokenConverter UUID_CONVERTER = new UUIDTokenConverter();

    private static final class NewTokenConverter extends StringTokenConverter {
        @Override
        public String getName() {
            return "new";
        }
    }


    @Test
    void addTokenConverter_DuplicateConverter_ThrowsException() {
        final RoutePathParser routePathParser = RoutePathParser.defaultParser();
        final StringTokenConverter duplicateConverter = new StringTokenConverter();
        assertThrows(IllegalArgumentException.class, () -> routePathParser.addTokenConverter(duplicateConverter));
    }

    @ParameterizedTest
    @MethodSource(value = "validRoutePaths")
    void buildRoutePattern_MatchingPatterns(final String input, final String expectedMatch) {
        final RoutePathParser parser = RoutePathParser.defaultParser();
        final Pattern pattern = parser.buildRoutePattern(input);
        assertTrue(pattern.matcher(expectedMatch).matches());
    }

    @ParameterizedTest
    @MethodSource("negativeRoutePaths")
    void buildRoutePattern_NegativeMatchingPatterns(final String input, final String doesNotMatch) {
        final RoutePathParser parser = RoutePathParser.defaultParser();
        final Pattern pattern = parser.buildRoutePattern(input);
        assertFalse(pattern.matcher(doesNotMatch).matches());
    }

    @Test
    void buildRoutePattern_UnknownTokenConverter_ThrowsException() {
        final RoutePathParser parser = RoutePathParser.defaultParser();
        assertThrows(IllegalStateException.class, () -> parser.buildRoutePattern("/<unknown:foo>"));
    }

    @ParameterizedTest
    @MethodSource("usedTypesPaths")
    void buildPathParameters_KnownTypes(final String input, final Map<String, TokenConverter<?>> expectedUsedTypes) {
        final RoutePathParser parser = RoutePathParser.defaultParser();
        final Map<String, TokenConverter<?>> usedTypes = parser.buildPathParameters(input);
        assertEquals(expectedUsedTypes, usedTypes);
    }

    @Test
    void buildPathParameters_UnknownType_ThrowsException() {
        final RoutePathParser parser = RoutePathParser.defaultParser();
        assertThrows(IllegalStateException.class, () -> parser.buildPathParameters("/<unknown:foo>"));
    }

    @Test
    void addTokenConverter_SuccessfullyAddsAndParses() {
        final NewTokenConverter newTokenConverter = new NewTokenConverter();
        final RoutePathParser parser = RoutePathParser.defaultParser();
        parser.addTokenConverter(newTokenConverter);

        final Pattern pattern = parser.buildRoutePattern("/static/<int:foo>/<new:bar>");
        final Matcher matcher = pattern.matcher("/static/123/helloWorld");
        assertTrue(matcher.matches());
    }

    private static Stream<Arguments> validRoutePaths() {
        return Stream.of(
            // input | URL that will match resulting pattern
            Arguments.arguments("", ""),
            Arguments.arguments("/", "/"),
            Arguments.arguments("/static", "/static"),
            Arguments.arguments("/static/", "/static/"),
            Arguments.arguments("/static/foo", "/static/foo"),

            // single token by itself
            Arguments.arguments("/<string:foo>", "/foo"),
            Arguments.arguments("/<string:foo>", "/bar"),
            Arguments.arguments("/<string:foo>/", "/foo/"),
            Arguments.arguments("/<string:foo>/", "/bar/"),

            // single token after static
            Arguments.arguments("/static/<string:foo>", "/static/foo"),
            Arguments.arguments("/static/<string:foo>", "/static/bar"),
            Arguments.arguments("/static/<string:foo>/", "/static/foo/"),
            Arguments.arguments("/static/<string:foo>/", "/static/bar/"),
            Arguments.arguments("/static/<int:bar>", "/static/1"),
            Arguments.arguments("/static/<int:bar>", "/static/123"),

            // single token before static
            Arguments.arguments("/<string:foo>/static", "/foo/static"),
            Arguments.arguments("/<string:foo>/static", "/bar/static"),

            // multiple tokens after static
            Arguments.arguments("/static/<string:foo>/<int:bar>", "/static/foo/123"),
            Arguments.arguments("/static/<string:foo>/<int:bar>", "/static/bar/1"),
            Arguments.arguments("/static/<string:foo>/<int:bar>/", "/static/foo/123/"),
            Arguments.arguments("/static/<string:foo>/<int:bar>/", "/static/bar/1/"),

            // multiple tokens before static
            Arguments.arguments("/<string:foo>/<int:bar>/static", "/bar/1/static"),
            Arguments.arguments("/<string:foo>/<int:bar>/static", "/foo/123/static"),
            Arguments.arguments("/<string:foo>/<int:bar>/static/", "/foo/123/static/"),
            Arguments.arguments("/<string:foo>/<int:bar>/static/", "/bar/1/static/"),

            // multiple tokens surrounding static
            Arguments.arguments("/<string:foo>/static/<int:bar>", "/foo/static/123"),
            Arguments.arguments("/<string:foo>/static/<int:bar>", "/bar/static/1"),
            Arguments.arguments("/<string:foo>/static/<int:bar>/", "/foo/static/123/"),
            Arguments.arguments("/<string:foo>/static/<int:bar>/", "/bar/static/1/"),

            // multiple static surrounding single token
            Arguments.arguments("/static1/<string:foo>/static2", "/static1/foo/static2"),
            Arguments.arguments("/static1/<string:foo>/static2", "/static1/bar/static2"),

            // complex
            Arguments.arguments("/<int:foo>/<string:bar>/static/<uuid:baz>", "/123/foo/static/b66674f0-5e3c-46c8-a449-5ed36f5a5914")
        );
    }

    private static Stream<Arguments> negativeRoutePaths() {
        return Stream.of(
            Arguments.arguments("/static", "/static/"),
            Arguments.arguments("/static/", "/static"),

            Arguments.arguments("/<int:foo>", "/123/"),
            Arguments.arguments("/<int:foo>/", "/123"),

            Arguments.arguments("/<int:foo>", "/123.0"),
            Arguments.arguments("/<uuid:foo>", "/abc-123-456-7890a")
        );
    }

    private static Stream<Arguments> usedTypesPaths() {
        return Stream.of(
            Arguments.arguments("/static/<string:foo>", map(vars("foo"), conv(STRING_CONVERTER))),
            Arguments.arguments("/<string:foo>", map(vars("foo"), conv(STRING_CONVERTER))),

            Arguments.arguments("/<string:foo>/<int:bar>/<uuid:baz>/<int:qux>",
                map(vars("foo", "bar", "baz", "qux"),
                    conv(STRING_CONVERTER, INT_CONVERTER, UUID_CONVERTER, INT_CONVERTER))),

            Arguments.arguments("/<int:foo>/static/<string:bar>",
                map(vars("foo", "bar"), conv(INT_CONVERTER, STRING_CONVERTER)))
        );
    }

    private static String[] vars(final String... vars) {
        return vars;
    }

    private static TokenConverter<?>[] conv(final TokenConverter<?>... converters) {
        return converters;
    }

    private static Map<String, TokenConverter<?>> map(final String[] vars, final TokenConverter<?>[] converters) {
        if (vars.length != converters.length) {
            throw new IllegalArgumentException("Size of vars and converters must be the same");
        }
        final Map<String, TokenConverter<?>> converterMap = new HashMap<>();
        for (int i = 0; i < vars.length; i++) {
            converterMap.put(vars[i], converters[i]);
        }
        return converterMap;
    }
}
