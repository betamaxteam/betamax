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

package co.freeside.betamax.io;

import co.freeside.betamax.Configuration;
import co.freeside.betamax.message.Message;
import co.freeside.betamax.message.tape.RecordedMessage;
import co.freeside.betamax.tape.EntityStorage;
import com.google.common.io.*;
import java.io.*;

/**
 * Handles the conversion of HTTP bodies between raw wire form and a (hopefully) more readable
 * representation of it -- for example, between a gzipped and UTF-8 encoded string (byte[]), and
 * its java.lang.String equivalent.
 * <p/>
 * We may fall back, when we cannot understand a {@link co.freeside.betamax.message.Message}'s
 * Content-Type and Content-Encoding, to a no-op conversion, and simply use the raw bytes to
 * save the body to tape (usually resulting in unreadable base-64 encoded bytes written out to yaml).
 * <p/>
 * Created by IntelliJ IDEA. 9/14/14, 12:03 PM
 *
 * @author igrayson
 */
public final class BodyConverter {

    private final FileResolver fileResolver;

    private EntityStorage responseBodyStorage = Configuration.DEFAULT_RESPONSE_BODY_STORAGE;

    public BodyConverter(FileResolver fileResolver) {
        this.fileResolver = fileResolver;
    }

    /**
     * Convert the raw HTTP body of the provided {@link co.freeside.betamax.message.Message} into a serializable,
     * intermediary form. For example, this may convert a UTF8-encoded body to a {@link java.lang.String}.
     * <p/>
     * At its option (i.e., in the case of unknown or invalid encoding), this method may perform a no-op conversion
     * of the raw HTTP body to a byte[].
     *
     * @param message       HTTP message whose body is to be recorded.  The entire
     *                      {@link co.freeside.betamax.message.tape.RecordedMessage} is provided for access to metadata
     *                      useful in detecting the conversion required.
     * @param tapeName      name of the tape
     * @param interactionId some opaque identifier for this transaction, unique within the scope of this tape
     * @return the message's body, in YAML-serializable form
     * @throws IOException indicates unexpected failure in the conversion process.
     * @see org.yaml.snakeyaml.representer.SafeRepresenter for a list of serializable types SnakeYAML automatically
     * understands.
     */
    public Object toRecordedForm(final Message message, final String tapeName, final String interactionId) throws IOException {
        if (EntityStorage.external == responseBodyStorage) {
            return recordBodyToFile(message, tapeName, interactionId);
        } else if (ContentTypes.isTextContentType(message.getContentType())) {
            return CharStreams.toString(message.getBodyAsText());
        } else {
            return ByteStreams.toByteArray(message.getBodyAsBinary());
        }
    }

    /**
     * Convert the serializable HTTP body of a recorded message to a wire-ready form.
     *
     * @param message recorded message containing the HTTP body in serializable form.  The entire
     *                {@link co.freeside.betamax.message.tape.RecordedMessage} is provided for access to metadata useful
     *                in detecting the conversion required.
     * @return raw http body
     * @throws IOException indicates unexpected failure in the conversion process
     */
    public byte[] toWireForm(final RecordedMessage message) throws IOException {
        Object body = message.getBody();
        return body instanceof String ? ((String) body).getBytes(message.getCharset()) : (byte[]) body;
    }

    public void setResponseBodyStorage(EntityStorage responseBodyStorage) {
        this.responseBodyStorage = responseBodyStorage;
    }

    private File recordBodyToFile(final Message message, final String tapeName, final String interactionId) throws IOException {
        String filename = FileTypeMapper.filenameFor(String.format("response-%s", interactionId), message.getContentType());
        File body = fileResolver.toFile(FilenameNormalizer.toFilename(tapeName), filename);
        Files.createParentDirs(body);
        ByteStreams.copy(message.getBodyAsBinary(), Files.newOutputStreamSupplier(body));
        return body;
    }

}
