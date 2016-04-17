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

package software.betamax.proxy;

import com.google.common.base.Predicate;
import com.google.common.base.Throwables;
import com.google.common.collect.Sets;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpRequest;
import org.littleshoot.proxy.*;
import org.littleshoot.proxy.impl.DefaultHttpProxyServer;
import software.betamax.ProxyConfiguration;
import software.betamax.internal.RecorderListener;
import software.betamax.proxy.netty.PredicatedHttpFilters;
import software.betamax.tape.Tape;
import software.betamax.util.BetamaxMitmManager;
import software.betamax.util.ProxyOverrider;
import software.betamax.util.SSLOverrider;

import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.google.common.base.Predicates.not;
import static io.netty.handler.codec.http.HttpMethod.CONNECT;
import static software.betamax.proxy.netty.PredicatedHttpFilters.httpMethodPredicate;

public class ProxyServer implements RecorderListener, TapeProvider {

    private static final Logger LOG = Logger.getLogger(ProxyServer.class.getName());

    private static final Predicate<HttpRequest> NOT_CONNECT = not(httpMethodPredicate(CONNECT));

    private final ProxyConfiguration configuration;

    private final ProxyOverrider proxyOverrider = new ProxyOverrider();
    private final SSLOverrider sslOverrider = new SSLOverrider();

    private HttpProxyServer proxyServer;
    private boolean running;

    private Set<Channel> channels;
    private Tape currentTape;

    public ProxyServer(final ProxyConfiguration configuration) {
        this.configuration = configuration;
        this.channels = Sets.newHashSet();
    }

    @Override
    public void onRecorderStart(final Tape tape) {
        start(tape);
    }

    @Override
    public void onRecorderStop() {
        stop();
    }

    @Override
    public Tape getTape() {
        return currentTape;
    }

    public boolean isRunning() {
        return running;
    }

    public String getHostName() {
        return proxyServer.getListenAddress().getHostName();
    }

    public int getPort() {
        return proxyServer.getListenAddress().getPort();
    }

    public void start() {
        if (isRunning()) {
            throw new IllegalStateException("Betamax proxy server is already running");
        }

        proxyServer = createProxyBootstrap().start();
        running = true;

        overrideProxySettings();
        overrideSSLSettings();
    }

    public void stopServer() {
        if (!isRunning()) {
            throw new IllegalStateException("Betamax proxy server is already stopped");
        }

        restoreOriginalProxySettings();
        restoreOriginalSSLSettings();

        proxyServer.stop();
        running = false;
    }

    public void start(final Tape tape) {
        if (configuration.isCreateProxyOnStartup() && !isRunning()) {
            start();
        }

        this.currentTape = tape;
    }

    public void stop() {
        if (!isRunning()) {
            return;
        }

        if (configuration.isCreateProxyOnStartup()) {
            stopServer();

        } else if (channels != null && !channels.isEmpty()) {

            // close the open channels to ensure that the tape is written to memory
            for (Channel channel : channels) {
                if (channel.isOpen()) {
                    channel.close();
                }
            }
        }
    }

    private void overrideProxySettings() {
        proxyOverrider.activate(configuration.getProxyHost(), getPort(), configuration.getIgnoreHosts());
    }

    private void restoreOriginalProxySettings() {
        proxyOverrider.deactivateAll();
    }

    private void overrideSSLSettings() {
        if (configuration.isSslEnabled()) {
            sslOverrider.activate();
        }
    }

    private void restoreOriginalSSLSettings() {
        if (configuration.isSslEnabled()) {
            sslOverrider.deactivate();
        }
    }

    private HttpProxyServerBootstrap createProxyBootstrap() {

        // find a proxy port, if none is explicitly bound
        int proxyPort = configuration.getProxyPort();
        if (proxyPort <= 0) {
            proxyPort = findProxyPort();
        }

        InetSocketAddress address = new InetSocketAddress(configuration.getProxyHost(), proxyPort);
        LOG.info(String.format("Betamax proxy is binding to %s", address));

        HttpProxyServerBootstrap proxyServerBootstrap = DefaultHttpProxyServer
                .bootstrap()
                .withIdleConnectionTimeout(configuration.getProxyTimeoutSeconds())
                .withAddress(address)
                .withTransparent(true);

        if (configuration.isSslEnabled()) {
            proxyServerBootstrap.withManInTheMiddle(new BetamaxMitmManager());
        } else {
            proxyServerBootstrap.withChainProxyManager(proxyOverrider);
        }

        if (configuration.getProxyUser() != null) {
            proxyServerBootstrap.withProxyAuthenticator(new ProxyAuthenticator() {
                @Override
                public String getRealm() {
                    return null;
                }

                @Override
                public boolean authenticate(String userName, String password) {
                    return configuration.getProxyUser().equals(userName)
                            && configuration.getProxyPassword().equals(password);
                }
            });
        }

        proxyServerBootstrap.withFiltersSource(new HttpFiltersSourceAdapter() {
            @Override
            public int getMaximumRequestBufferSizeInBytes() {
                return configuration.getRequestBufferSize();
            }

            @Override
            public HttpFilters filterRequest(final HttpRequest originalRequest) {
                HttpFilters filters = new BetamaxFilters(originalRequest, ProxyServer.this);
                return new PredicatedHttpFilters(filters, NOT_CONNECT, originalRequest);
            }

            @Override
            public HttpFilters filterRequest(final HttpRequest originalRequest, final ChannelHandlerContext ctx) {
                channels.add(ctx.channel());
                return super.filterRequest(originalRequest, ctx);
            }
        });

        return proxyServerBootstrap;
    }

    private int findProxyPort() {

        Integer httpProxyPort;

        do {
            // use 0 to find an open port
            httpProxyPort = tryBind(0);

            // sleep, then try again
            if (httpProxyPort == null) {
                try {
                    // try not to burn up a lot of CPU time, but don't wait too long
                    Thread.sleep(50);
                } catch (InterruptedException ie) {
                    Throwables.propagate(ie);
                }
            }

        } while (httpProxyPort == null);

        return httpProxyPort;
    }

    private Integer tryBind(int port) {

        Integer boundSocket;
        ServerSocket socket = null;
        try {
            socket = new ServerSocket();
            socket.setReuseAddress(true);
            socket.bind(new InetSocketAddress(port));

            boundSocket = socket.getLocalPort();

        } catch (Exception e) {
            boundSocket = null;
            LOG.log(Level.FINE, "Could not bind port " + port, e);

        } finally {
            if (socket != null) {
                try {
                    socket.close();
                } catch (Exception e) {
                    Throwables.propagate(e);
                }
            }
        }

        return boundSocket;
    }
}