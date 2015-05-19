package co.freeside.betamax

import java.net.*
import javax.net.ssl.*
import java.security.*
import java.security.cert.*
import org.apache.http.conn.ssl.AllowAllHostnameVerifier

public class TrustModifier {
   def relaxHostChecking(HttpURLConnection conn) {
      if (conn instanceof HttpsURLConnection) {
         HttpsURLConnection httpsConnection = (HttpsURLConnection) conn
         httpsConnection.setSSLSocketFactory(socketFactory())
         httpsConnection.setHostnameVerifier(new AllowAllHostnameVerifier())
      }
   }

   SSLSocketFactory socketFactory(HttpsURLConnection httpsConnection) {
     SSLContext ctx = SSLContext.getInstance("TLS")
     ctx.init(null, (TrustManager[])[new AlwaysTrustManager()], null)
     ctx.getSocketFactory()
   }

   private static class AlwaysTrustManager implements X509TrustManager {
     @Override
     void checkClientTrusted(X509Certificate[] certs, String auth) {}
     @Override
     void checkServerTrusted(X509Certificate[] certs, String auth) {}
     @Override
     X509Certificate[] getAcceptedIssuers() {null}
   }
}
