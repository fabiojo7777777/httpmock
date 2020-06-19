package br.com.httpmock.utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.charset.UnsupportedCharsetException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Vector;
import java.util.zip.GZIPOutputStream;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.TrustManagerFactory;

import org.apache.hc.client5.http.entity.GzipDecompressingEntity;
import org.apache.hc.client5.http.ssl.TrustAllStrategy;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpHeaders;
import org.apache.hc.core5.http.HttpMessage;
import org.apache.hc.core5.http.ProtocolException;
import org.apache.hc.core5.http.io.entity.ByteArrayEntity;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.message.BasicHeader;
import org.apache.hc.core5.ssl.PrivateKeyDetails;
import org.apache.hc.core5.ssl.PrivateKeyStrategy;
import org.apache.hc.core5.ssl.SSLContexts;
import org.apache.hc.core5.ssl.TrustStrategy;

import br.com.httpmock.server.ProxyServer.TextTransformer;

public class HttpUtils
{
    private static final UrlReplacer      PROXY_URL_REPLACER               = new UrlReplacer();
    private static final UrlReplacer      REVERSE_PROXY_URL_REPLACER       = new UrlReplacer();
    private static final HttpUtils        INSTANCE                         = new HttpUtils();
    @Deprecated // use PROXY_URL_REPLACER
    private static final List<UrlMapping> PROXY                            = new Vector<UrlMapping>();
    @Deprecated // use REVERSE_PROXY_URL_REPLACER
    private static final List<UrlMapping> REVERSE_PROXY                    = new Vector<UrlMapping>();
    @SuppressWarnings("unused")
    private static TrustStrategy          WRITE_CLIENT_CERTS_ON_CONNECTION = new TrustStrategy()
                                                                           {

                                                                               @Override
                                                                               public boolean isTrusted(X509Certificate[] chain, String authType)
                                                                                       throws CertificateException
                                                                               {
                                                                                   for (X509Certificate c : chain)
                                                                                   {
                                                                                       gravarCertificados(c, "clients.pkcs12", c.getIssuerDN().getName(), "clients");
                                                                                   }
                                                                                   return false;
                                                                               }

                                                                           };

    private HttpUtils()
    {
        super();
    }

    public static void addProxyMapping(String fromHost, String fromPath, String toHost, String toPath)
            throws MalformedURLException
    {
        String fromUrl = addPaths(getPath(fromHost), getPath(fromPath));
        String toUrl   = addPaths(getPath(toHost), getPath(toPath));
        addProxyMapping(fromUrl, toUrl);
    }

    public static void addProxyMapping(String fromUrl, String toUrl)
            throws MalformedURLException
    {
        fromUrl = getPath(fromUrl);
        toUrl   = getPath(toUrl);

        for (String from : getUrlListWithAndWithoutDefaultPort(fromUrl))
        {
            UrlMapping urlMapping = INSTANCE.new UrlMapping(from, ommitUrlDefaultPort(toUrl));
            PROXY_URL_REPLACER.addMapping(urlMapping.fromUrl, urlMapping.toUrl);
            PROXY.add(urlMapping);
        }
        for (String to : getUrlListWithAndWithoutDefaultPort(toUrl))
        {
            UrlMapping urlMapping = INSTANCE.new UrlMapping(to, ommitUrlDefaultPort(fromUrl));
            REVERSE_PROXY_URL_REPLACER.addMapping(urlMapping.fromUrl, urlMapping.toUrl);
            REVERSE_PROXY.add(urlMapping);
        }

        Collections.sort(PROXY);
        Collections.sort(REVERSE_PROXY);
    }

    public static String ommitUrlDefaultPort(String txtUrl)
            throws MalformedURLException
    {
        URL    url      = new URL(txtUrl);
        String protocol = url.getProtocol();
        String host     = url.getHost();
        String path     = url.getPath();
        int    port     = HttpUtils.getPort(url);

        if (Constants.HTTP.equalsIgnoreCase(protocol) && port == 80
                || Constants.HTTPS.equalsIgnoreCase(protocol) && port == 443)
        {
            return protocol + "://" + host + path;
        }
        else
        {
            return protocol + "://" + host + ":" + port + path;
        }
    }

    private static List<String> getUrlListWithAndWithoutDefaultPort(String txtUrl)
            throws MalformedURLException
    {
        List<String> urlList  = new ArrayList<String>();

        URL          url      = new URL(txtUrl);
        String       protocol = url.getProtocol();
        String       host     = url.getHost();
        String       path     = url.getPath();
        int          port     = HttpUtils.getPort(url);

        urlList.add(protocol + "://" + host + ":" + port + path);
        if (Constants.HTTP.equalsIgnoreCase(protocol) && port == 80
                || Constants.HTTPS.equalsIgnoreCase(protocol) && port == 443)
        {
            urlList.add(protocol + "://" + host + path);
        }
        return urlList;
    }

    public static List<UrlMapping> getProxyMappings()
    {
        return Collections.unmodifiableList(PROXY);
    }

    public static List<UrlMapping> getReverseProxyMappings()
    {
        return Collections.unmodifiableList(REVERSE_PROXY);
    }

    public static String proxyUrls(String text)
    {
        if (text == null)
        {
            return null;
        }
        return PROXY_URL_REPLACER.replace(text);
    }

    public static String reverseProxyUrls(String text)
    {
        if (text == null)
        {
            return null;
        }
        return REVERSE_PROXY_URL_REPLACER.replace(text);
    }

    public static String getPath(String url)
    {
        return url.contains(Constants.QUERY_SEPARATOR) ? url
                .substring(0, url.indexOf(Constants.QUERY_SEPARATOR)) : url;
    }

    public static String addPaths(String p1, String p2)
    {
        if (p1 == null || p1.length() == 0)
        {
            if (p1 != null && p2 == null)
                return p1;
            return p2;
        }
        if (p2 == null || p2.length() == 0)
            return p1;
        boolean p1EndsWithSlash   = p1.endsWith(Constants.SLASH);
        boolean p2StartsWithSlash = p2.startsWith(Constants.SLASH);
        if (p1EndsWithSlash && p2StartsWithSlash)
        {
            if (p2.length() == 1)
                return p1;
            if (p1.length() == 1)
                return p2;
        }
        StringBuilder buf = new StringBuilder(p1.length() + p2.length() + 2);
        buf.append(p1);
        if (p1.endsWith(Constants.SLASH))
        {
            if (p2.startsWith(Constants.SLASH))
                buf.setLength(buf.length() - 1);
        }
        else if (!p2.startsWith(Constants.SLASH))
        {
            buf.append(Constants.SLASH);
        }
        buf.append(p2);
        return buf.toString();
    }

    public static int getPort(URL url)
    {
        int port = url.getPort();
        if (port == -1)
        {
            if (Constants.HTTP.equalsIgnoreCase(url.getProtocol()))
            {
                port = 80;
            }
            else if (Constants.HTTPS.equalsIgnoreCase(url.getProtocol()))
            {
                port = 443;
            }
        }
        return port;
    }

    public static String extractHost(String txtUri)
    {
        URI uri = URI.create(txtUri);
        return uri.getScheme() + "://" + uri.getRawAuthority();
    }

    public static String extractHost(URI uri)
    {
        return uri.getScheme() + "://" + uri.getRawAuthority();
    }

    public class UrlMapping implements Comparable<UrlMapping>
    {
        private String fromUrl;
        private String toUrl;

        public UrlMapping(String fromUrl, String toUrl)
        {
            super();
            if (fromUrl.endsWith("/"))
            {
                this.fromUrl = fromUrl.substring(0, fromUrl.length() - 1);
            }
            else
            {
                this.fromUrl = fromUrl;
            }
            if (toUrl.endsWith("/"))
            {
                this.toUrl = toUrl.substring(0, toUrl.length() - 1);
            }
            else
            {
                this.toUrl = toUrl;
            }
        }

        public String getFromUrl()
        {
            return fromUrl;
        }

        public String getToUrl()
        {
            return toUrl;
        }

        @Override
        public int compareTo(UrlMapping o)
        {
            int compare1 = -this.fromUrl.compareTo(o.fromUrl);
            if (compare1 != 0)
            {
                return compare1;
            }
            else
            {
                int compare2 = -this.toUrl.compareTo(o.toUrl);
                return compare2;
            }
        }

        @Override
        public String toString()
        {
            return "Mapping [fromUrl=" + fromUrl + ", toUrl=" + toUrl + "]";
        }

    }

    @Deprecated
    public static SSLContext getSSLContext2(
            boolean isHttps,
            String keystoreFilename,
            String keystorePassword,
            String truststoreFilename,
            String truststorePassword,
            boolean clientAuth)
            throws KeyManagementException,
            UnrecoverableKeyException,
            NoSuchAlgorithmException,
            KeyStoreException,
            CertificateException,
            MalformedURLException,
            IOException
    {
        if (!isHttps)
        {
            return null;
        }
        if (keystoreFilename == null)
        {
            keystoreFilename = Constants.DEFAULT_KEYSTORE_FILENAME;
            keystorePassword = Constants.DEFAULT_KEYSTORE_PASSWORD;
        }
        if (truststoreFilename == null)
        {
            truststoreFilename = Constants.DEFAULT_TRUSTSTORE_FILENAME;
            truststorePassword = Constants.DEFAULT_TRUSTSTORE_PASSWORD;
        }

        // initialise the keystore
        char[]          password = keystorePassword.toCharArray();
        KeyStore        ks       = KeyStore.getInstance("JKS");
        FileInputStream fis      = new FileInputStream(keystoreFilename);
        ks.load(fis, password);

        // setup the key manager factory
        KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
        kmf.init(ks, password);

        // setup the trust manager factory
        TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
        tmf.init(ks);

        // setup the HTTPS context and parameters
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
        SSLContext.setDefault(sslContext);
        return sslContext;
    }

    public static SSLContext getSSLContext(
            boolean isHttps,
            String keystoreFilename,
            String keystorePassword,
            String truststoreFilename,
            String truststorePassword,
            boolean clientAuth)
            throws KeyManagementException,
            UnrecoverableKeyException,
            NoSuchAlgorithmException,
            KeyStoreException,
            CertificateException,
            MalformedURLException,
            IOException
    {
        if (!isHttps)
        {
            return null;
        }
        if (keystoreFilename == null)
        {
            keystoreFilename = Constants.DEFAULT_KEYSTORE_FILENAME;
            keystorePassword = Constants.DEFAULT_KEYSTORE_PASSWORD;
        }
        if (truststoreFilename == null)
        {
            truststoreFilename = Constants.DEFAULT_TRUSTSTORE_FILENAME;
            truststorePassword = Constants.DEFAULT_TRUSTSTORE_PASSWORD;
        }

        URL        keystoreUrl   = getResourceUrl(keystoreFilename);
        URL        truststoreUrl = getResourceUrl(truststoreFilename);

        SSLContext sslContext    = SSLContexts
                .custom()
                .loadKeyMaterial(
                        keystoreUrl,
                        keystorePassword.toCharArray(),
                        keystorePassword.toCharArray(),
                        new PrivateKeyStrategy()
                                                 {
                                                     @Override
                                                     public String chooseAlias(Map<String, PrivateKeyDetails> aliases,
                                                             SSLParameters sslParameters)
                                                     {
                                                         // take the first private key in keystore and
                                                         // uses it
                                                         for (String alias : aliases.keySet())
                                                         {
                                                             if (alias != null && !alias.trim().equals(""))
                                                             {
                                                                 return alias;
                                                             }
                                                         }
                                                         return null;
                                                     }
                                                 })
                .loadTrustMaterial(
                        truststoreUrl,
                        truststorePassword.toCharArray(),
                        // clientAuth true : uses the default validation
                        // searching in the truststore loaded
                        // clientAuth false : not uses the truststore and
                        // trust all clients
                        (clientAuth ? null : new TrustAllStrategy()))
                .build();
        return sslContext;
    }

    private static URL getResourceUrl(String resourcePath)
            throws MalformedURLException,
            IOException
    {
        final URL url = HttpUtils.class.getResource(resourcePath);
        if (url != null)
        {
            return url;
        }
        else
        {
            return new File(FileUtils.getApplicationRunningPath() + resourcePath).toURI().toURL();
        }
    }

    public static void gravarCertificados(X509Certificate chain, String filename, String newAliasName, String password)
    {
        try
        {
            KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
            if (new File(filename).exists())
            {
                ks.load(new FileInputStream(filename), password == null ? null : password.toCharArray());
            }
            else
            {
                ks.load(null, password == null ? null : password.toCharArray());
            }
            int qtd = 0;
            try
            {
                ks.setCertificateEntry(newAliasName, chain);
                qtd++;
            }
            catch (Throwable e)
            {
                e.printStackTrace();
            }
            ks.store(new FileOutputStream(new File(filename)), password.toCharArray());
            System.out.println(qtd + " certificados gravados");
        }
        catch (Throwable e)
        {
            e.printStackTrace();
        }
    }

    public static void copyHeaders(HttpMessage fromHttpMessage, HttpMessage toHttpMessage, TextTransformer textTransformer, boolean preserveHostHeader)
    {
        for (final Iterator<Header> it = fromHttpMessage.headerIterator(); it.hasNext();)
        {
            final Header header       = it.next();
            boolean      isHostHeader = HttpHeaders.HOST.toLowerCase(Locale.ROOT).equals(header.getName().toLowerCase(Locale.ROOT));
            if (isHostHeader && preserveHostHeader)
            {
                // preserve original host
                toHttpMessage.addHeader(new BasicHeader(header.getName(), header.getValue()));
            }
            else if (!Constants.HOP_BY_HOP.contains(header.getName().toLowerCase(Locale.ROOT)))
            {
                String  headerValue            = header.getValue();
                boolean isAcceptEncodingHeader = HttpHeaders.ACCEPT_ENCODING.toLowerCase(Locale.ROOT).equals(header.getName().toLowerCase(Locale.ROOT));
                if (isAcceptEncodingHeader)
                {
                    boolean isGzipped = headerValue.toLowerCase(Locale.ROOT).contains(Constants.GZIP_ENCODING.toLowerCase(Locale.ROOT));
                    if (isGzipped)
                    {
                        // remove compression types that are not gzip
                        headerValue = Constants.GZIP_ENCODING;
                    }
                    else
                    {
                        // remove header if they have a compression that are not
                        // gzip
                        continue;
                    }
                }
                else
                {
                    if (textTransformer != null)
                    {
                        headerValue = textTransformer.transform(headerValue);
                    }
                }
                toHttpMessage.addHeader(new BasicHeader(header.getName(), headerValue));
            }
        }

    }

    public static void copyBody(HttpMessage fromHttpMessage, HttpMessage toHttpMessage, TextTransformer textTransformer)
            throws ProtocolException,
            IOException
    {
        HttpEntity entity = getEntity(fromHttpMessage);
        if (entity != null)
        {
            HttpEntity oldEntity   = wrapHttpEntityForUnzipIfNecessary(entity, fromHttpMessage);
            String     contentType = getContentType(fromHttpMessage).toLowerCase(Locale.ROOT);
            String     encoding    = getContentEncoding(fromHttpMessage);
            if (textTransformer != null && needsBodyProxyTransformation(contentType))
            {
                byte[] bytes = textTransformer
                        .transform(EntityUtils.toString(oldEntity, Constants.UTF8))
                        .getBytes(Constants.UTF8);
                setEntity(toHttpMessage,
                        new ByteArrayEntity(
                                zipContentIfNecessary(bytes, fromHttpMessage),
                                ContentType.parse(contentType),
                                encoding));
            }
            else
            {
                byte[] bytes = EntityUtils
                        .toByteArray(oldEntity);
                setEntity(toHttpMessage,
                        new ByteArrayEntity(
                                zipContentIfNecessary(bytes, fromHttpMessage),
                                ContentType.parse(contentType),
                                encoding));
            }
        }
    }

    private static boolean needsBodyProxyTransformation(String contentType)
    {
        for (String type : Constants.BODY_TYPES_NEEDS_PROXY_TEXT_TRANSFORMATION)
        {
            if (contentType.contains(type))
            {
                return true;
            }
        }
        return false;
    }

    public static byte[] zipContentIfNecessary(byte[] bytes, HttpMessage httpMessage)
            throws ProtocolException,
            IOException
    {
        String responseEncoding = getContentEncoding(httpMessage);
        if (responseEncoding.toLowerCase(Locale.ROOT).contains(Constants.GZIP_ENCODING.toLowerCase(Locale.ROOT)))
        {
            final ByteArrayOutputStream zipado = new ByteArrayOutputStream();
            final GZIPOutputStream      gzip   = new GZIPOutputStream(zipado);
            gzip.write(bytes);
            gzip.close();
            return zipado.toByteArray();
        }
        else
        {
            return bytes;
        }
    }

    private static HttpEntity getEntity(HttpMessage httpMessage)
    {
        if (httpMessage instanceof ClassicHttpResponse)
        {
            return ((ClassicHttpResponse) httpMessage).getEntity();
        }
        if (httpMessage instanceof ClassicHttpRequest)
        {
            return ((ClassicHttpRequest) httpMessage).getEntity();
        }
        return null;
    }

    private static void setEntity(HttpMessage httpMessage, HttpEntity httpEntity)
    {
        if (httpMessage instanceof ClassicHttpResponse)
        {
            ((ClassicHttpResponse) httpMessage).setEntity(httpEntity);
        }
        if (httpMessage instanceof ClassicHttpRequest)
        {
            ((ClassicHttpRequest) httpMessage).setEntity(httpEntity);
        }
    }

    public static HttpEntity wrapHttpEntityForUnzipIfNecessary(HttpEntity entity, HttpMessage httpMessage)
            throws ProtocolException,
            UnsupportedCharsetException,
            IOException
    {
        String contentEncoding = getContentEncoding(httpMessage);
        if (contentEncoding.toLowerCase(Locale.ROOT).contains(Constants.GZIP_ENCODING.toLowerCase(Locale.ROOT)))
        {
            return new GzipDecompressingEntity(entity);
        }
        else
        {
            return entity;
        }
    }

    public static String getContentType(HttpMessage httpMessage)
            throws ProtocolException
    {
        String contentType       = ContentType.TEXT_PLAIN.getMimeType();
        Header contentTypeHeader = httpMessage.getHeader(HttpHeaders.CONTENT_TYPE);
        if (contentTypeHeader != null)
        {
            String contentTypeValue = contentTypeHeader.getValue();
            if (contentTypeValue != null)
            {
                return contentTypeValue;
            }
        }
        return contentType;
    }

    public static String getContentEncoding(HttpMessage httpMessage)
            throws ProtocolException
    {
        String contentEncoding       = "";
        Header contentEncodingHeader = httpMessage.getHeader(HttpHeaders.CONTENT_ENCODING);
        if (contentEncodingHeader != null)
        {
            String contentEncodingValue = contentEncodingHeader.getValue();
            if (contentEncodingValue != null)
            {
                contentEncoding = contentEncodingValue;
            }
        }
        return contentEncoding;
    }

}
