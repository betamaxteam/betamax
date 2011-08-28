---
title: Configuration
layout: documentation
---

The `Recorder` class has some configuration properties that you can override:

* *tapeRoot*: the base directory where tape files are stored. Defaults to `src/test/resources/betamax/tapes`.
* *proxyPort*: the port the Betamax proxy listens on. Defaults to `5555`.

If you have a file called `BetamaxConfig.groovy` or `betamax.properties` somewhere in your classpath it will be picked up by the `Recorder` class.

### Example _BetamaxConfig.groovy_ script

{% highlight java %}
betamax {
    tapeRoot = new File("test/fixtures/tapes")
    proxyPort = 1337
}
{% endhighlight %}

### Example _betamax.properties_ file

{% highlight properties %}
betamax.tapeRoot=test/fixtures/tapes
betamax.proxyPort=1337
{% endhighlight %}
