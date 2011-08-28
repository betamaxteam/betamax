---
title: Usage
layout: documentation
---

To use Betamax you just need to annotate your JUnit test or [Spock][spock] specifications with `@Betamax(tape="tape_name")` and include a `betamax.Recorder` Rule.

### JUnit example

{% highlight java %}
import betamax.Betamax
import betamax.Recorder
import org.junit.*

class MyTest {

    @Rule public Recorder recorder = new Recorder()

    @Betamax(tape="my tape")
    @Test
    void testMethodThatAccessesExternalWebService() {

    }
}
{% endhighlight %}

### Spock example

{% highlight java %}
import betamax.Betamax
import betamax.Recorder
import org.junit.*
import spock.lang.*

class MySpec extends Specification {

    @Rule Recorder recorder = new Recorder()

    @Betamax(tape="my tape")
    def "feature that accesses external web service"() {

    }
}
{% endhighlight %}

## Recording and playback

Betamax will record to the current tape when it intercepts any HTTP request with a combination of method and URI that does not match anything that is already on the tape. If a recorded interaction with the same method and URI _is_ found then the proxy does not forward the request to the target URI but instead returns the previously recorded response to the requestor.

In future it will be possible to match recorded interactions based on criteria other than just method and URI.

## Tape modes

Betamax supports three different read/write modes for tapes. The tape mode is set by adding a `mode` argument to the `@Betamax` annotation.

* `READ_WRITE`: This is the default mode. If the proxy intercepts a request that matches a recording on the tape then the recorded response is played back. Otherwise the request is forwarded to the target URI and the response recorded.
* `READ_ONLY`: The proxy will play back responses from tape but if it intercepts an unknown request it will not forward it to the target URI or record anything, instead it responds with a `403: Forbidden` status code.
* `WRITE_ONLY`: The proxy will always forward the request to the target URI and record the response regardless of whether or not a matching request is already on the tape. Any existing recorded interactions will be overwritten.

## Security

Betamax is a testing tool and not a spec-compliant HTTP proxy. It ignores _any_ and _all_ headers that would normally be used to prevent a proxy caching or storing HTTP traffic. You should ensure that sensitive information such as authentication credentials is removed from recorded tapes before committing them to your app's source control repository.

## Caveats

By default [Apache _HttpClient_][httpclient] takes no notice of Java's HTTP proxy settings. The Betamax proxy can only intercept traffic from HttpClient if the client instance is set up to use a [`ProxySelectorRoutePlanner`][proxyselector]. When Betamax is not active this will mean HttpClient traffic will be routed via the default proxy configured in Java (if any).

### Configuring HttpClient

{% highlight java %}
def client = new DefaultHttpClient()
client.routePlanner = new ProxySelectorRoutePlanner(
    client.connectionManager.schemeRegistry,
    ProxySelector.default
)
{% endhighlight %}

The same is true of [Groovy _HTTPBuilder_][httpbuilder] and its [_RESTClient_][restclient] variant as they are wrappers around _HttpClient_.

### Configuring HTTPBuilder

{% highlight java %}
def http = new HTTPBuilder("http://groovy.codehaus.org")
def routePlanner = new ProxySelectorRoutePlanner(
    http.client.connectionManager.schemeRegistry,
    ProxySelector.default
)
http.client.routePlanner = routePlanner
{% endhighlight %}

_HTTPBuilder_ also includes a [_HttpURLClient_][httpurlclient] class which needs no special configuration as it uses a `java.net.URLConnection` rather than _HttpClient_.

[httpclient]:http://hc.apache.org/httpcomponents-client-ga/httpclient/index.html
[proxyselector]:http://hc.apache.org/httpcomponents-client-ga/httpclient/apidocs/org/apache/http/impl/conn/ProxySelectorRoutePlanner.html
[httpbuilder]:http://groovy.codehaus.org/modules/http-builder/
[restclient]:http://groovy.codehaus.org/modules/http-builder/doc/rest.html
[httpurlclient]:http://groovy.codehaus.org/modules/http-builder/doc/httpurlclient.html
[spock]:http://spockframework.org/

