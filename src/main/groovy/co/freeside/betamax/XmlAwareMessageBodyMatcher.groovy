package co.freeside.betamax

import co.freeside.betamax.message.Message

/**
 * A rule for matching message bodies
 * while ignoring specified xml elements.
 */
class XmlAwareMessageBodyMatcher implements Comparator<Message> {
  List<String> xmlElementNames = []

    XmlAwareMessageBodyMatcher(String... xmlElementNames) {
    this.xmlElementNames = xmlElementNames
  }

  int compare(Message a, Message b) {
    (a.hasBody() ? cleanse(a.bodyAsText.text) : null) <=>
    (b.hasBody() ? cleanse(b.bodyAsText.text) : null)
  }

  // Does not work with self-nested element, e.g. <a><a>1</a></a>
  String cleanse(String bodyText) {
    xmlElementNames.inject(bodyText) {bodySoFar, element ->
      bodySoFar.replaceAll("<$element>[^<]*</$element>", "")
               .replaceAll("<$element/>", "")
    }
  }
}
