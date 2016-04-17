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

package software.betamax;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import software.betamax.internal.RecorderListener;
import software.betamax.proxy.ProxyConfigurationException;
import software.betamax.proxy.ProxyServer;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.UnknownHostException;
import java.util.Collection;

public class ProxyConfiguration extends Configuration {

    public static final String DEFAULT_PROXY_HOST = "0.0.0.0";
    public static final int DEFAULT_REQUEST_BUFFER_SIZE = 8388608; //8MB
    public static final int DEFAULT_PROXY_PORT = 5555;
    public static final int DEFAULT_PROXY_TIMEOUT = 5;
    public static final boolean DEFAULT_CREATE_PROXY_ON_STARTUP = true;

    private final String proxyHost;
    private final int proxyPort;
    private final String proxyUser;
    private final String proxyPassword;
    private final int proxyTimeoutSeconds;
    private final boolean sslEnabled;
    private final int requestBufferSize;
    private final boolean createProxyOnStartup;

    protected ProxyConfiguration(final ProxyConfigurationBuilder<?> builder) {
        super(builder);

        this.proxyHost = builder.proxyHost;
        this.proxyPort = builder.proxyPort;
        this.proxyUser = builder.proxyUser;
        this.proxyPassword = builder.proxyPassword;
        this.proxyTimeoutSeconds = builder.proxyTimeoutSeconds;
        this.sslEnabled = builder.sslEnabled;
        this.requestBufferSize = builder.requestBufferSize;
        this.createProxyOnStartup = builder.createProxyOnStartup;
    }

    public static ProxyConfigurationBuilder<Builder> builder() {
        return new Builder().configureFromPropertiesFile();
    }

    /**
     * The port the Betamax proxy will listen on.
     */
    public int getProxyPort() {
        return proxyPort;
    }

    /**
     * The time (in seconds) the proxy will wait before aborting a request.
     */
    public int getProxyTimeoutSeconds() {
        return proxyTimeoutSeconds;
    }

    /**
     * The buffer size the proxy will use to aggregate incoming requests.
     * Needed if you want to match on request body.
     */
    public int getRequestBufferSize() {
        return requestBufferSize;
    }

    /**
     * If set to true add support for proxying SSL (disable certificate
     * checking).
     */
    public boolean isSslEnabled() {
        return sslEnabled;
    }

    /**
     * Whether or not the proxy should get auto-created when starting; the JUnit rule
     * sets this to false (default is true) so that it can create one proxy efficiently for all unit tests.
     */
    public boolean isCreateProxyOnStartup() {
        return createProxyOnStartup;
    }

    /**
     * @return the hostname or address where the proxy will run. A value of
     * `null` means the proxy will bind to any local address.
     * @see java.net.InetSocketAddress#InetSocketAddress(InetAddress, int)
     */
    public InetAddress getProxyHost() {
        try {
            if (proxyHost == null) {
                return InetAddress.getByName(DEFAULT_PROXY_HOST);
            } else {
                return InetAddress.getByName(proxyHost);
            }
        } catch (UnknownHostException e) {
            throw new ProxyConfigurationException(String.format("Unable to resolve host %s", proxyHost), e);
        }
    }

    /**
     * The raw hostname for the proxy server to bind to.
     */
    public String getProxyHostname() {
        return proxyHost;
    }

    /**
     * The username required to authenticate with the proxy.
     */
    public String getProxyUser() {
        return proxyUser;
    }

    /**
     * The password required to authenticate with the proxy.
     */
    public String getProxyPassword() {
        return proxyPassword;
    }

    /**
     * @return a `java.net.Proxy` instance configured to point to the Betamax
     * proxy.
     */
    public Proxy getProxy() {
        return new Proxy(Proxy.Type.HTTP, new InetSocketAddress(getProxyHost(), getProxyPort()));
    }

    private static final Predicate<RecorderListener> HAS_PROXY_SERVER = new Predicate<RecorderListener>() {
        @Override
        public boolean apply(RecorderListener input) {
            return input instanceof ProxyServer;
        }
    };

    @Override
    public void registerListeners(final Collection<RecorderListener> listeners) {
        if (createProxyOnStartup && !Iterables.any(listeners, HAS_PROXY_SERVER)) {
            listeners.add(new ProxyServer(this));
        }

        super.registerListeners(listeners);
    }

    public static class Builder extends ProxyConfigurationBuilder<Builder> {
        @Override
        protected Builder self() {
            return this;
        }
    }
}
