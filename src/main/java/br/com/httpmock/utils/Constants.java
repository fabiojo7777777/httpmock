package br.com.httpmock.utils;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import org.apache.hc.core5.http.HttpHeaders;

import br.com.httpmock.server.ProxyServer.TextTransformer;

public interface Constants
{
    public static final String          LOCALHOST                                  = "localhost";
    public static final String          HTTPMOCK_FROM_URL                          = "httpmock-from-url";
    public static final String          HTTPMOCK_TO_URL                            = "httpmock-to-url";
    public static final String          MAPPING_FILE_NAME                          = "mappingFileName";
    public static final String[]        BODY_TYPES_NEEDS_PROXY_TEXT_TRANSFORMATION = new String[] {
            // Observation: html and css contains text in content-type
            "text", "json", "javascript", "xml"
    };
    public static final String          VIRTUAL_SERVER                             = "VirtualServer";
    public static final String          PROXY_PASS_AND_REVERSE                     = "ProxyPassAndReverse";
    public static final String          RECORDING_DIRECTORY                        = "RecordingDirectory";
    public static final String          PRESERVE_HOST_HEADER                       = "PreserveHostHeader";
    public static final String          HTTPS_REQUIRE_CLIENT_CERT                  = "HttpsRequireClientCert";
    public static final String          HTTPS_KEYSTORE                             = "HttpsKeystore";
    public static final String          KEYSTORE_PASSWORD                          = "KeystorePassword";
    public static final String          HTTPS_TRUSTSTORE                           = "HttpsTruststore";
    public static final String          TRUSTSTORE_PASSWORD                        = "TruststorePassword";
    public static final String          OFFLINE_MATCH_HEADERS                      = "OfflineMatchHeaders";
    public static final String          OFFLINE_IGNORE_PARAMETERS                  = "OfflineIgnoreParameters";
    public static final String          OFFLINE_CYCLIC_RESPONSES                   = "OfflineCyclicResponses";
    public static final String          OFFLINE_IGNORE_HTTP_STATUS                 = "OfflineIgnoreHttpStatus";
    public static final String          ONLINE                                     = "Online";
    public static final String          OFFLINE                                    = "Offline";
    public static final String          UTF8                                       = "UTF-8";
    public static final Charset         UTF8_CHARSET                               = Charset.forName(UTF8);
    public static final String          ALL                                        = "ALL";
    public static final String          PARAMETER_PREFIX                           = "--";
    public static final String          REFERENCE_SEPARATOR                        = "#";
    public static final String          COMMENTS                                   = "#";
    public static final String          SLASH                                      = "/";
    public static final String          HTTP                                       = "HTTP";
    public static final String          HTTPS                                      = "HTTPS";
    public static final String          QUERY_SEPARATOR                            = "?";
    public static final String          FILLING_ZEROS                              = "00000000000000000000";
    public static final String          NULL_TEXT                                  = "null";
    public static final int             FIVE                                       = 5;
    public static final String          GZIP_ENCODING                              = "gzip";
    public static final Set<String>     HOP_BY_HOP                                 = Collections.unmodifiableSet(new HashSet<>(
            Arrays.asList(
                    HttpHeaders.HOST.toLowerCase(Locale.ROOT),
                    HttpHeaders.CONTENT_LENGTH.toLowerCase(Locale.ROOT),
                    HttpHeaders.TRANSFER_ENCODING.toLowerCase(Locale.ROOT),
                    HttpHeaders.CONNECTION.toLowerCase(Locale.ROOT),
                    HttpHeaders.KEEP_ALIVE.toLowerCase(Locale.ROOT),
                    HttpHeaders.PROXY_AUTHENTICATE.toLowerCase(Locale.ROOT),
                    HttpHeaders.TE.toLowerCase(Locale.ROOT),
                    HttpHeaders.TRAILER.toLowerCase(Locale.ROOT),
                    HttpHeaders.UPGRADE.toLowerCase(Locale.ROOT))));
    public static final String          DEFAULT_KEYSTORE_FILENAME                  = "/httpmock.pkcs12";
    public static final String          DEFAULT_KEYSTORE_PASSWORD                  = "httpmock";
    public static final String          DEFAULT_TRUSTSTORE_FILENAME                = "/httpmock.pkcs12";
    public static final String          DEFAULT_TRUSTSTORE_PASSWORD                = "httpmock";
    public static final String          DEFAULT_KEYSTORE_ALIAS_NAME                = "httpmock";
    public static final int             BUFFER_SIZE                                = 1024 * 2;
    public static final TextTransformer PROXY_URLS_TEXT_TRANSFORMER                = new TextTransformer()
                                                                                   {
                                                                                       @Override
                                                                                       public String transform(String text)
                                                                                       {
                                                                                           return HttpUtils.proxyUrls(text);
                                                                                       }
                                                                                   };
    public static final TextTransformer REVERSE_PROXY_URLS_TEXT_TRANSFORMER        = new TextTransformer()
                                                                                   {
                                                                                       @Override
                                                                                       public String transform(String text)
                                                                                       {
                                                                                           return HttpUtils.reverseProxyUrls(text);
                                                                                       }
                                                                                   };
    public static final int             ONE_HUNDRED_PERCENT                        = 100;
    public static final String          FILES_DIRECTORY                            = "__files";
    public static final String          MAPPINGS_DIRECTORY                         = "mappings";
    public static final long            DISPLAY_MAX_BODY_LENGTH                    = 30000;
}
