# NOTE - This is a Banno-specific fork of Betamax.
See additional Banno-specific notes at the end of this document.

# &beta;etamax [![Build Status](https://secure.travis-ci.org/robfletcher/betamax.png?branch=master)](http://travis-ci.org/robfletcher/betamax)

Betamax is a tool for mocking external HTTP resources such as web services and REST APIs in your tests. The project was inspired by the [VCR][vcr] library for Ruby.

Betamax is written in [Groovy][groovy] but is compatible with tests written using [JUnit][junit] or [Spock][spock] for applications written in any JVM language.

## Usage

### Record

Add a `@Rule Recorder` property to your test and annotate test methods with `@Betamax`. The first time the test runs any HTTP traffic is recorded to _tape_.

### Playback

Future test runs replay responses from _tape_ without traffic going to the real target. No more 3rd party downtime or rate limits breaking your tests. You can even run your tests offline! Insert different _tapes_ to stub different responses.

### Customize
_Tapes_ are just [YAML][yaml] files so you can edit them with a text editor, commit to source control, share with your team & use on continuous integration.

## Full documentation

	Full documentation can be found on [Betamax's home page][home].

## Project status

The current stable version of Betamax is 1.1.1 which is available from [Maven Central][mavenrepo].

Add `'co.freeside:betamax:1.1.1'` as a test dependency to your [Gradle][gradle], [Ivy][ivy], [Grails][grails] or [Maven][maven] project (or anything that can use Maven repositories).

Development versions are available from [Sonatype][sonatype].

Betamax's tests run on [Travis CI][travis].

Please get in touch if you have any  feedback. You can raise defects and feature requests via [GitHub issues][issues].

[gradle]:http://gradle.org/
[grails]:http://grails.org/
[groovy]:http://groovy.codehaus.org/
[home]:http://freeside.co/betamax
[issues]:http://github.com/robfletcher/betamax/issues
[ivy]:http://ant.apache.org/ivy/
[junit]:http://junit.org/
[maven]:http://maven.apache.org/
[mavenrepo]:http://repo1.maven.org/maven2/co/freeside/betamax/
[sonatype]:https://oss.sonatype.org/content/groups/public/co/freeside/betamax/
[spock]:http://spockframework.org/
[travis]:http://travis-ci.org/robfletcher/betamax
[vcr]:http://relishapp.com/myronmarston/vcr
[yaml]:http://yaml.org/

## Notes on running tests from inside IntelliJ IDEA

Go to _Settings -> Compiler_ and ensure that `*.keystore` appears in the _Resource patterns_ otherwise IDEA will not
make the SSL keystore available on the classpath when tests run.

## Banno Notes

We've forked Betamax because it hasn't had a commit by the maintainer since June 2014, nor a release since 2012.  We needed some features so we forked from the 1.1.2 release.

## New Features

### Request Matching Doesn't Fail if Body is Empty

### Groovy, Gradle and Other Dependencies Upgraded

### New Tape Mode: Reconcile

We periodically want to check our tapes against the live services, such as JxChange, to detect breaking changes on their end.  To this end we created a new tape mode, 'RECONCILE', which sends all requests to the live server and then looks for matching requests on the tape, and compares the responses using the new ResponseMatcher and ResponseMatchRule(s).  If they match, the test passes as normal.  If the response from the server is different from the taped one, the interaction is written to a 'reconciliation error tape' whose name will be the regular tape name plus '_reconciliation_errors', e.g. 'my_spec_reconciliation_errors.yaml'.  A Jenkins job can therefore fail on the presence of such files.

### More Flexible Request/Response matching

Responses and requests may not match exactly due to date fields, etc.  This can affect both reconciliation mode and taped response lookup, respectively.  Since both requests and response implement Message, we've created an XmlAwareMessageMatcher than can be used to create request/response matchers that ignore certain elements, e.g.

```scala
new XmlAwareMessageBodyMatcher("trackingId", "timestamp")
```

This would ignore values inside <trackingId> and <timestamp> elements.  If more sophisticated checks are needed, simply implement more rules.

### Banno Fork Development Notes

- This project is written in Groovy and uses Gradle for builds.  './gradlew' is the wrapper script to run, and it will install Groovy and Gradle for you.
- To see full output in the console, use the '-i -s' flags, e.g. './gradlew clean test -i -s'
- To build, run './gradlew clean test' (or whatever goals you need).
- To release, run './gradlew clean test uploadArchives'.  You'll be prompted for Nexus credentials.
- To be compatible with Java 1.7, we must build under 1.7 to avoid an error due to Groovy leveraging new 1.8 things such as ToIntFunction.
- The version number has the '-banno' suffix, e.g. '1.1.2-banno-SNAPSHOT'
- See Brian H. if you have other questions.
