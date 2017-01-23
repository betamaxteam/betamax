/*
 * Copyright 2013 the original author or authors.
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

package software.betamax.proxy.netty;

import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.CompositeByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpMessage;
import software.betamax.message.AbstractMessage;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.netty.buffer.Unpooled.copiedBuffer;

public abstract class NettyMessageAdapter<T extends HttpMessage> extends AbstractMessage {

    protected final T delegate;
    private final Map<String, List<String>> headers = new HashMap<>();
    private final CompositeByteBuf body = Unpooled.compositeBuffer();

    protected NettyMessageAdapter(T delegate) {
        this.delegate = delegate;
        copyHeaders(delegate);
    }

    private static String joinHeaderValues(List<String> values) {
        String headerString = "";

        for (int i = 0; i < values.size(); i++) {
            String s = values.get(i);

            if (i > 0) {
                headerString += ", ";
            }

            headerString += s;
        }

        return headerString;
    }

    private void putInHeaders(String name, String value) {
        if (!headers.containsKey(name)) {
            headers.put(name, new ArrayList<>());
        }

        List<String> values = headers.get(name);

        for (String aValue : values) {
            if (aValue.equals(value)) {
                return;
            }
        }

        values.add(value);
    }

    /**
     * LittleProxy will use multiple request / response objects and sometimes
     * subsequent ones will contain additional headers.
     */
    public void copyHeaders(HttpMessage httpMessage) {
        for (String name : httpMessage.headers().names()) {
            for (String value : httpMessage.headers().getAll(name)) {
                putInHeaders(name, value);
            }
        }
    }

    public void append(HttpContent chunk) throws IOException {
        body.addComponent(copiedBuffer(chunk.content()));
        body.writerIndex(body.writerIndex() + chunk.content().readableBytes());
    }

    @Override
    public Map<String, String> getHeaders() {
        HashMap<String, String> map = new HashMap<>();
        for (String name : headers.keySet()) {
            map.put(name, getHeader(name));
        }
        return map;
    }

    @Override
    public String getHeader(String name) {
        return joinHeaderValues(headers.get(name));
    }

    @Override
    public void addHeader(String name, String value) {
        putInHeaders(name, value);
    }

    @Override
    public boolean hasBody() {
        return body.capacity() > 0;
    }

    @Override
    protected InputStream getBodyAsStream() throws IOException {
        //Copy the body into a new ByteBuf so that it can be consumed multiple times.
        return new ByteBufInputStream(Unpooled.copiedBuffer(body));
    }
}
