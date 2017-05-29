/*
 * Copyright 2011 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package software.betamax.encoding;

import java.io.*;
import java.nio.CharBuffer;
import java.nio.charset.Charset;

public abstract class AbstractEncoder {
    public final String decode(InputStream input, String charset) {
        try {
            Reader reader = new InputStreamReader(getDecodingInputStream(input), charset);
            StringBuilder sb = new StringBuilder();

            CharBuffer buf = CharBuffer.allocate(0x800);
            while (reader.read(buf) != -1) {
                buf.flip();
                sb.append(buf);
                buf.clear();
            }

            return sb.toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public final String decode(InputStream input) {
        return decode(input, Charset.defaultCharset().toString());
    }

    public final byte[] encode(byte[] input) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        OutputStream stream = getEncodingOutputStream(out);
        stream.write(input);
        stream.flush();
        stream.close();
        return out.toByteArray();
    }

    public final byte[] encode(String input, String charset) throws IOException {
        return encode(input.getBytes(charset));
    }

    public final byte[] encode(String input) throws IOException {
        String charset = Charset.defaultCharset().toString();
        return encode(input.getBytes(charset));
    }

    protected abstract InputStream getDecodingInputStream(InputStream input);

    protected abstract OutputStream getEncodingOutputStream(OutputStream output);
}
