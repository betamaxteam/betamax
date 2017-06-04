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

package software.betamax.tape;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import software.betamax.*;
import software.betamax.Configuration;
import software.betamax.Headers;
import software.betamax.MatchRule;
import software.betamax.TapeMode;
import software.betamax.encoding.DeflateEncoder;
import software.betamax.encoding.GzipEncoder;
import software.betamax.handler.NonWritableTapeException;
import software.betamax.message.Message;
import software.betamax.message.Request;
import software.betamax.message.Response;
import software.betamax.message.tape.RecordedMessage;
import software.betamax.message.tape.RecordedRequest;
import software.betamax.message.tape.RecordedResponse;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

import static com.google.common.net.HttpHeaders.VIA;
import static java.util.Collections.unmodifiableList;

/**
 * Represents a set of recorded HTTP interactions that can be played back or
 * appended to.
 */
public abstract class MemoryTape implements Tape {

    private String name;
    private List<RecordedInteraction> interactions = Lists.newArrayList();

    private transient TapeMode mode = Configuration.DEFAULT_MODE;
    private transient MatchRule matchRule = Configuration.DEFAULT_MATCH_RULE;
    private transient ModifyRule modifyRule = Configuration.DEFAULT_MODIFY_RULE;

    private transient AtomicInteger orderedIndex = new AtomicInteger();

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public TapeMode getMode() {
        return mode;
    }

    @Override
    public void setMode(TapeMode mode) {
        this.mode = mode;
    }

    @Override
    public MatchRule getMatchRule() {
        return this.matchRule;
    }

    @Override
    public void setMatchRule(MatchRule matchRule) {
        this.matchRule = matchRule;
    }

    @Override
    public void setModifyRule(ModifyRule modifyRule) {
        this.modifyRule = modifyRule;
    }

    @Override
    public boolean isReadable() {
        return mode.isReadable();
    }

    @Override
    public boolean isWritable() {
        return mode.isWritable();
    }

    @Override
    public boolean isSequential() {
        return mode.isSequential();
    }

    @Override
    public int size() {
        return interactions.size();
    }

    public List<RecordedInteraction> getInteractions() {
        return unmodifiableList(interactions);
    }

    public void setInteractions(List<RecordedInteraction> interactions) {
        this.interactions = Lists.newArrayList(interactions);
    }

    @Override
    public boolean seek(Request request) {
        if (isSequential()) {
            try {
                // TODO: it's a complete waste of time using an AtomicInteger when this method is called before play in a non-transactional way
                Integer index = orderedIndex.get();
                RecordedInteraction interaction = interactions.get(index);
                RecordedRequest nextRequest = interaction == null ? null : interaction.getRequest();
                return nextRequest != null && matchRule.isMatch(request, nextRequest);
            } catch (IndexOutOfBoundsException e) {
                throw new NonWritableTapeException();
            }
        } else {
            return findMatch(request) >= 0;
        }
    }

    @Override
    public Response play(final Request request) {
        if (!mode.isReadable()) {
            throw new IllegalStateException("the tape is not readable");
        }

        if (mode.isSequential()) {
            Integer nextIndex = orderedIndex.getAndIncrement();
            final RecordedInteraction nextInteraction = interactions.get(nextIndex);
            if (nextInteraction == null) {
                throw new IllegalStateException(String.format("No recording found at position %s", nextIndex));
            }

            if (!matchRule.isMatch(request, nextInteraction.getRequest())) {
                throw new IllegalStateException(String.format("Request %s does not match recorded request %s", stringify(request), stringify(nextInteraction.getRequest())));
            }

            return this.modifyRule.getModifiedResponse(request, nextInteraction.getResponse());
        } else {
            int position = findMatch(request);
            if (position < 0) {
                throw new IllegalStateException("no matching recording found");
            } else {
                return this.modifyRule.getModifiedResponse(request, interactions.get(position).getResponse());
            }
        }
    }

    private String stringify(Request request) {
        return "method: " + request.getMethod() + ", "
                + "uri: " + request.getUri() + ", "
                + "headers: " + request.getHeaders() + ", "
                + "body: " + request.getBodyAsText();
    }

    @Override
    public synchronized void record(Request request, Response response) {
        if (!mode.isWritable()) {
            throw new IllegalStateException("the tape is not writable");
        }

        RecordedInteraction interaction = new RecordedInteraction();

        if (mode.isSequential()) {
            interactions.add(interaction);
        } else {
            int position = findMatch(request);
            if (position >= 0) {
                interactions.set(position, interaction);
            } else {
                interactions.add(interaction);
            }
        }

        interaction.setRequest(recordRequest(request));
        interaction.setResponse(recordResponse(response));
        interaction.setRecorded(new Date());
    }

    @Override
    public String toString() {
        return String.format("Tape[%s]", name);
    }

    private synchronized int findMatch(final Request request) {
        return Iterables.indexOf(interactions, new Predicate<RecordedInteraction>() {
            @Override
            public boolean apply(RecordedInteraction input) {
                return matchRule.isMatch(request, input.getRequest());
            }
        });
    }

    private RecordedRequest recordRequest(Request request) {
        try {
            final RecordedRequest recording = new RecordedRequest();
            recording.setMethod(request.getMethod());
            recording.setUri(request.getUri());

            for (Map.Entry<String, String> header : request.getHeaders().entrySet()) {
                if (!header.getKey().equals(VIA)) {
                    recording.getHeaders().put(header.getKey(), header.getValue());
                }
            }

            if (request.hasBody()) {
                recordBodyInline(request, recording);
            }

            return recording;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private RecordedResponse recordResponse(Response response) {
        try {
            RecordedResponse recording = new RecordedResponse();
            recording.setStatus(response.getStatus());

            for (Map.Entry<String, String> header : response.getHeaders().entrySet()) {
                if (!header.getKey().equals(VIA) && !header.getKey().equals(Headers.X_BETAMAX)) {
                    recording.getHeaders().put(header.getKey(), header.getValue());
                }
            }

            if (response.hasBody()) {
                recordBodyInline(response, recording);
            }

            return recording;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void recordBodyInline(Message message, RecordedMessage recording) throws IOException {
        boolean representAsText = isTextContentType(message.getContentType());
        if (representAsText) {
            // TODO find a better way to accommodate for this behavior
            if (message.getEncoding().equals("gzip")) {
                recording.setBody(new GzipEncoder().decode(new ByteArrayInputStream(message.getBodyAsBinary())));
            } else if (message.getEncoding().equals("deflate")) {
                recording.setBody(new DeflateEncoder().decode(new ByteArrayInputStream(message.getBodyAsBinary())));
            } else {
                recording.setBody(message.getBodyAsText());
            }
        } else {
            recording.setBody(message.getBodyAsBinary());
        }
    }

    private static boolean isTextContentType(String contentType) {
        return contentType != null && Pattern.compile("^text/|application/(json|javascript|(\\w+\\+)?xml|x-www-form-urlencoded)").matcher(contentType).find();
    }

}
