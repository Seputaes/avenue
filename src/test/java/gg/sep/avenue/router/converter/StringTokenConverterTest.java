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

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link StringTokenConverter}.
 */
public class StringTokenConverterTest {

    private static final String DECODED = "he?.,<>LlO w:;orld!@_+=~`\"#\\/$%^&*";
    private static final String ENCODED = "he%3F.%2C%3C%3ELlO%20w%3A%3Borld%21%40_%2B%3D%7E%60%22%23%5C%2F%24%25%5E%26*";

    @Test
    void fromURLPath_DecodesURLToString() {
        assertEquals(DECODED, new StringTokenConverter().fromURLPath(ENCODED));
    }

    @Test
    void toURLPath_EncodesURLToPath() {
        assertEquals(ENCODED, new StringTokenConverter().toURLPath(DECODED));
    }
}
