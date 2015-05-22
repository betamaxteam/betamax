package co.freeside.betamax

import co.freeside.betamax.message.Response

/**
 * A rule for matching response bodies
 * while ignoring specified xml elements.
 */
class XmlAwareResponseBodyMatcher implements Comparator<Response> {
  List<String> xmlElementNames = []

  XmlAwareResponseBodyMatcher(String... xmlElementNames) {
    this.xmlElementNames = xmlElementNames
  }

  int compare(Response a, Response b) {
    (a.hasBody() ? cleanse(a.bodyAsText.text) : null) <=>
    (b.hasBody() ? cleanse(b.bodyAsText.text) : null)
  }

  // Does not work with self-nested element, e.g. <a><a>1</a></a>
  String cleanse(String bodyText) {
    xmlElementNames.inject(bodyText) {bodySoFar, element ->
      bodySoFar.replaceAll("<$element>[^<]*</$element>", "")
    }
  }
}
