---
title: Home
layout: documentation
---

A Groovy record/playback proxy for testing applications that access external HTTP resources. Inspired by Ruby's [VCR][vcr].

Betamax can record and play back HTTP interactions made by your app so that your tests can run without any real HTTP traffic going to external URLs. The first time a test is run any HTTP traffic is recorded to a _tape_ and subsequent runs will play back the recorded HTTP response without connecting to the external server.

Betamax works with JUnit and [Spock][spock]. Although it is written in [Groovy][groovy] Betamax can be used to test applications written in any JVM language so long as HTTP connections are made in a way that respects Java's `http.proxyHost` and `http.proxyPort` system properties.

Tapes are stored to disk as [YAML][yaml] files and can be modified (or even created) by hand and committed to your project's source control repository so they can be shared by other members of your team and used by your CI server. Different tests can use different tapes to simulate varying response conditions. Each tape can hold multiple request/response interactions but each must (currently) have a unique request method and URI. An example tape file can be found [here][tapeexample].

[vcr]:http://relishapp.com/myronmarston/vcr
[yaml]:http://yaml.org/
[tapeexample]:https://github.com/robfletcher/betamax/blob/master/src/test/resources/betamax/tapes/smoke_spec.yaml
[spock]:http://spockframework.org/
[groovy]:http://groovy.codehaus.org