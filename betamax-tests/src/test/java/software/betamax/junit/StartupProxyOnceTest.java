package software.betamax.junit;

import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import software.betamax.ProxyConfiguration;

public class StartupProxyOnceTest {

    @Rule
    @ClassRule
    public static RecorderRule recorder = new RecorderRule(ProxyConfiguration.builder().proxyPort(0).build());

    @Test
    @Betamax(tape = "test1")
    public void firstTest() {
        assert recorder.getTape().getName().equals("test1");
    }

    @Test
    @Betamax(tape = "test2")
    public void secondTest() {
        assert recorder.getTape().getName().equals("test2");
    }

    @Test
    @Betamax(tape = "test3")
    public void thirdTest() {
        assert recorder.getTape().getName().equals("test3");
    }
}