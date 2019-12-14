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

import java.util.UUID;

import lombok.EqualsAndHashCode;

/**
 * Token converter for UUID patterns in route paths.
 *
 * See: {@link TokenConverter}.
 */
@EqualsAndHashCode(callSuper = false)
public class UUIDTokenConverter implements TokenConverter<UUID> {
    private static final String UUID_PATTERN = "[A-Fa-f0-9]{8}" +
        "-" +
        "[A-Fa-f0-9]{4}" +
        "-" +
        "[A-Fa-f0-9]{4}" +
        "-" +
        "[A-Fa-f0-9]{4}" +
        "-" +
        "[A-Fa-f0-9]{12}";
    private static final String NAME = "uuid";

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() {
        return NAME;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getTokenPattern() {
        return UUID_PATTERN;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UUID fromURLPath(final String value) {
        return UUID.fromString(value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toURLPath(final UUID value) {
        return value.toString();
    }
}
