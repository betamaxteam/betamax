package software.betamax.junit;

import org.junit.rules.ExternalResource;
import software.betamax.proxy.ProxyServer;

public class ProxyServerResource extends ExternalResource {

    private final ProxyServer proxyServer;

    public ProxyServerResource(final ProxyServer proxyServer) {
        this.proxyServer = proxyServer;
    }

    @Override
    protected void before() throws Throwable {
        proxyServer.start();
    }

    @Override
    protected void after() {
        proxyServer.stopServer();
    }
}