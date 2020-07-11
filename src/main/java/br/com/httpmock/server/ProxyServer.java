package br.com.httpmock.server;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;

import javax.net.ssl.SSLParameters;

import org.apache.hc.core5.function.Callback;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.ConnectionClosedException;
import org.apache.hc.core5.http.ConnectionReuseStrategy;
import org.apache.hc.core5.http.ExceptionListener;
import org.apache.hc.core5.http.HttpConnection;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.HttpHeaders;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.ProtocolException;
import org.apache.hc.core5.http.config.CharCodingConfig;
import org.apache.hc.core5.http.impl.Http1StreamListener;
import org.apache.hc.core5.http.impl.bootstrap.HttpRequester;
import org.apache.hc.core5.http.impl.bootstrap.HttpServer;
import org.apache.hc.core5.http.impl.bootstrap.RequesterBootstrap;
import org.apache.hc.core5.http.impl.bootstrap.ServerBootstrap;
import org.apache.hc.core5.http.io.HttpRequestHandler;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.message.BasicClassicHttpRequest;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.apache.hc.core5.http.protocol.HttpCoreContext;
import org.apache.hc.core5.io.CloseMode;
import org.apache.hc.core5.pool.ConnPoolListener;
import org.apache.hc.core5.pool.ConnPoolStats;
import org.apache.hc.core5.pool.PoolStats;
import org.apache.hc.core5.util.Args;
import org.apache.hc.core5.util.Timeout;

import br.com.httpmock.Main.LocalServer;
import br.com.httpmock.utils.Constants;
import br.com.httpmock.utils.HttpUtils;
import br.com.httpmock.utils.StringUtils;
import br.com.httpmock.utils.StubUtils;
import br.com.httpmock.views.NetworkView;

/**
 * Embedded HTTP/1.1 reverse proxy using classic I/O.
 */

/**
 * TODO MULTIPART/FORM-DATA: INCLUIR SUPORTE AO FORMATO
 */
//
// IMPORT A CERTIFICATE TO A KEYSTORE:
// keytool -importcert -file google.cer -keystore httpmock.pkcs12
// -storepass httpmock -alias "google"
//
// CREATE A NEW KEYSTORE:
// PKCS FORMAT:
// keytool -genkeypair -keyalg RSA -alias httpmock -keystore httpmock.pkcs12
// -storepass httpmock -deststoretype pkcs12 -validity 1000000 -keysize 2048
//
// Qual é o seu nome e o seu sobrenome?
// [Unknown]: Fabio Silva
// Qual é o nome da sua unidade organizacional?
// [Unknown]: httpmock
// Qual é o nome da sua empresa?
// [Unknown]: httpmock
// Qual é o nome da sua Cidade ou Localidade?
// [Unknown]: Varzea Paulista
// Qual é o nome do seu Estado ou Município?
// [Unknown]: Sao Paulo
// Quais são as duas letras do código do país desta unidade?
// [Unknown]: BR
// CN=Fabio Silva, OU=httpmock, O=httpmock, L=Varzea Paulista, ST=Sao Paulo,
// C=BR
// Está correto?
// [não]: sim
public class ProxyServer
{
    private static final ProxyServer INSTANCE = new ProxyServer();

    public static void executar(List<LocalServer> localServerList)
            throws Exception
    {
        int                   localHostPort   = -1;
        final ServerBootstrap serverBootstrap = ServerBootstrap.bootstrap();
        if (needsDefaultLocalhostMapping(localServerList))
        {
            serverBootstrap.registerVirtual(Constants.LOCALHOST, "*", getDefaultLocalhostHandler());
        }
        for (LocalServer localServer : localServerList)
        {
            boolean       isOnline               = localServer.isOnline();
            String        localHostName          = localServer.getHostname();
            final String  keystoreFilename       = localServer.getKeystore();
            final String  keystorePassword       = localServer.getKeystorePassword();
            final String  truststoreFilename     = localServer.getTruststore();
            final String  truststorePassword     = localServer.getTruststorePassword();
            final boolean clientAuth             = localServer.isClientAuth();
            boolean       preserveHostHeader     = localServer.isPreserveHostHeader();
            final boolean isHttps                = Constants.HTTPS.equalsIgnoreCase(new URL(localHostName).getProtocol());
            String        canonicalLocalHostName = new URL(localHostName).getHost();
            localHostPort = HttpUtils.getPort(new URL(localHostName));

            String         recordingDirectory      = localServer.getRecordingDirectory();
            List<String>   offlineMatchHeaders     = localServer.getOfflineMatchHeaders();
            List<String>   offlineIgnoreParameters = localServer.getOfflineIgnoreParameters();
            boolean        offlineCyclicResponses  = localServer.isOfflineCyclicResponses();
            List<Integer>  offlineIgnoreHttpStatus = localServer.getOfflineIgnoreHttpStatus();

            IHttpRequester requester               = null;
            if (isOnline)
            {
                requester = INSTANCE.new OnlineHttpRequesterWrapper(RequesterBootstrap
                        .bootstrap()
                        .setSslContext(HttpUtils.getSSLContext(
                                isHttps,
                                keystoreFilename,
                                keystorePassword,
                                truststoreFilename,
                                truststorePassword,
                                clientAuth))
                        .setSslSetupHandler(getSSLHandlerWithWeakTlsAndCiphersEnabled())
                        .setConnectionReuseStrategy(INSTANCE.new NotKeepAliveReuseStrategy())
                        .setStreamListener(INSTANCE.new HttpRequesterStreamListener())
                        .setConnPoolListener(INSTANCE.new HttpRequesterConnPoolListener())
                        .create());
            }
            else
            {
                requester = INSTANCE.new OfflineHttpRequester(
                        recordingDirectory,
                        offlineMatchHeaders,
                        offlineIgnoreParameters,
                        offlineCyclicResponses,
                        offlineIgnoreHttpStatus);
            }

            serverBootstrap
                    .setSslContext(HttpUtils.getSSLContext(
                            isHttps,
                            keystoreFilename,
                            keystorePassword,
                            truststoreFilename,
                            truststorePassword,
                            clientAuth))
                    .setSslSetupHandler(getSSLHandlerWithWeakTlsAndCiphersEnabled())
                    .setConnectionReuseStrategy(INSTANCE.new NotKeepAliveReuseStrategy())
                    .setCharCodingConfig(
                            CharCodingConfig
                                    .custom()
                                    .setCharset(Constants.UTF8_CHARSET)
                                    .build())
                    .setListenerPort(localHostPort)
                    .setStreamListener(INSTANCE.new HttpServerStreamListener())
                    .setExceptionListener(INSTANCE.new HttpServerExceptionListener())
                    .registerVirtual(canonicalLocalHostName, "*",
                            INSTANCE.new ProxyHandler(isOnline,
                                    requester,
                                    localHostName,
                                    preserveHostHeader,
                                    recordingDirectory,
                                    offlineMatchHeaders,
                                    offlineIgnoreParameters,
                                    offlineCyclicResponses,
                                    offlineIgnoreHttpStatus));
            System.out.println("Iniciando servidor virtual na porta " + localHostPort + " [" + localHostName + "]");
            Runtime.getRuntime().addShutdownHook(INSTANCE.new ApplicationShutdownHook(requester));
        }
        final HttpServer server = serverBootstrap.create();
        server.start();
        System.out.println("Ouvindo solicitações na porta " + localHostPort);
        System.out.println("-----------------------------------------------------------");
        // server.awaitTermination(TimeValue.MAX_VALUE);
        Runtime.getRuntime().addShutdownHook(INSTANCE.new ApplicationShutdownHook(server));
    }

    private static Callback<SSLParameters> getSSLHandlerWithWeakTlsAndCiphersEnabled()
    {
        return new Callback<SSLParameters>()
        {

            @Override
            public void execute(final SSLParameters sslParameters)
            {
                sslParameters.setProtocols(sslParameters.getProtocols());
                sslParameters.setCipherSuites(sslParameters.getCipherSuites());
                // sslParameters.setProtocols(TLS.excludeWeak(sslParameters.getProtocols()));
                // sslParameters.setCipherSuites(TlsCiphers.excludeWeak(sslParameters.getCipherSuites()));
            }

        };
    }

    private static HttpRequestHandler getDefaultLocalhostHandler()
    {
        return new HttpRequestHandler()
        {

            @Override
            public void handle(
                    ClassicHttpRequest incomingRequest,
                    ClassicHttpResponse outgoingResponse,
                    HttpContext context)
                    throws HttpException,
                    IOException
            {
                String fromUrl = incomingRequest.getRequestUri();
                try
                {
                    fromUrl = incomingRequest.getUri().toString();
                }
                catch (Throwable e1)
                {
                    // ignore
                }
                String             toUrl           = HttpUtils.proxyUrls(fromUrl);
                ClassicHttpRequest outgoingRequest = incomingRequest;
                try
                {
                    ClassicHttpRequest tempRequest = new BasicClassicHttpRequest(
                            incomingRequest.getMethod(),
                            incomingRequest.getUri());
                    HttpUtils.copyHeaders(incomingRequest, tempRequest, null, true);
                    HttpUtils.copyBody(incomingRequest, tempRequest, null);
                    outgoingRequest = tempRequest;

                    NetworkView.getInstance().addRequest(outgoingRequest, fromUrl, toUrl);

                    String virtualServer = HttpUtils.extractHost(incomingRequest.getUri());

                    outgoingResponse.setCode(HttpStatus.SC_INTERNAL_SERVER_ERROR);
                    outgoingResponse.setHeader(HttpHeaders.CONTENT_TYPE, "text/plain; charset=UTF-8");
                    outgoingResponse.setHeader(HttpHeaders.CONTENT_ENCODING, null);
                    outgoingResponse.setEntity(new StringEntity(
                            "O Servidor Virtual '"
                                    + virtualServer
                                    + "' não está mapeado para ser atendido por esta ferramenta",
                            Constants.UTF8_CHARSET));

                    NetworkView.getInstance().updateRequestWithResponse(outgoingRequest, outgoingResponse, null, fromUrl, toUrl);
                }
                catch (Throwable e)
                {
                    e.printStackTrace();
                    outgoingResponse.setCode(HttpStatus.SC_INTERNAL_SERVER_ERROR);
                    outgoingResponse.setHeader(HttpHeaders.CONTENT_TYPE, "text/plain; charset=UTF-8");
                    outgoingResponse.setHeader(HttpHeaders.CONTENT_ENCODING, null);
                    outgoingResponse.setEntity(new StringEntity(
                            StringUtils.getFullErrorMessage(e),
                            Constants.UTF8_CHARSET));
                    NetworkView.getInstance().updateRequestWithResponse(outgoingRequest, outgoingResponse, null, fromUrl, toUrl);
                }
            }

        };
    }

    private static boolean needsDefaultLocalhostMapping(List<LocalServer> localServerList)
            throws MalformedURLException
    {
        for (LocalServer localServer : localServerList)
        {
            String localHostName          = localServer.getHostname();
            String canonicalLocalHostName = new URL(localHostName).getHost();
            if (Constants.LOCALHOST.equals(canonicalLocalHostName))
            {
                return false;
            }
        }
        return true;
    }

    private class NotKeepAliveReuseStrategy
            implements ConnectionReuseStrategy
    {
        @Override
        public boolean keepAlive(HttpRequest request, HttpResponse response, HttpContext context)
        {
            debugln("**** keepAlive strategy returning false");
            return false;
        }
    }

    private interface IHttpRequester
    {
        public ClassicHttpResponse execute(
                final HttpHost targetHost,
                final ClassicHttpRequest request,
                final Timeout connectTimeout,
                final HttpContext context)
                throws HttpException,
                IOException;

        public void close(CloseMode graceful);

    }

    private class OnlineHttpRequesterWrapper
            implements IHttpRequester
    {
        private HttpRequester httpRequester;

        public OnlineHttpRequesterWrapper(HttpRequester httpRequester)
        {
            this.httpRequester = httpRequester;
        }

        @Override
        public ClassicHttpResponse execute(HttpHost targetHost, ClassicHttpRequest request, Timeout connectTimeout, HttpContext context)
                throws HttpException,
                IOException
        {
            return this.httpRequester.execute(targetHost, request, connectTimeout, context);
        }

        @Override
        public void close(CloseMode closeMode)
        {
            this.httpRequester.close(closeMode);
        }

    }

    private class OfflineHttpRequester
            implements IHttpRequester
    {
        private final StubUtils STUB_UTILS;

        public OfflineHttpRequester(
                String recordingDirectory,
                List<String> offlineMatchHeaders,
                List<String> offlineIgnoreParameters,
                boolean offlineCyclicResponses,
                List<Integer> offlineIgnoreHttpStatus)
                throws IOException,
                ProtocolException
        {
            this.STUB_UTILS = StubUtils.getInstance(recordingDirectory, offlineMatchHeaders, offlineIgnoreParameters, offlineCyclicResponses, offlineIgnoreHttpStatus);
        }

        @Override
        public ClassicHttpResponse execute(
                HttpHost targetHost,
                ClassicHttpRequest incomingRequest,
                Timeout connectTimeout,
                HttpContext context)
                throws ProtocolException,
                IOException
        {
            Args.notNull(targetHost, "HTTP host");
            Args.notNull(incomingRequest, "HTTP incomingRequest");
            return this.STUB_UTILS.getResponse(incomingRequest);
        }

        @Override
        public void close(CloseMode closeMode)
        {
        }
    }

    private class HttpRequesterStreamListener implements Http1StreamListener
    {

        @Override
        public void onRequestHead(final HttpConnection connection, final HttpRequest request)
        {
            debugln("[001][proxy->origin] " + Thread.currentThread() + " " +
                    request.getMethod() + " " + request.getRequestUri());
        }

        @Override
        public void onResponseHead(final HttpConnection connection, final HttpResponse response)
        {
            debugln("[002][proxy<-origin] " + Thread.currentThread() + " status " + response.getCode() + " [" + response.getReasonPhrase() + "]");
        }

        @Override
        public void onExchangeComplete(final HttpConnection connection, final boolean keepAlive)
        {
            debugln("[003][proxy<-origin] " + Thread.currentThread() + " exchange completed; " +
                    "connection " + (keepAlive ? "kept alive" : "cannot be kept alive"));
        }

    }

    private class HttpRequesterConnPoolListener implements ConnPoolListener<HttpHost>
    {

        @Override
        public void onLease(final HttpHost route, final ConnPoolStats<HttpHost> connPoolStats)
        {
            final StringBuilder buf = new StringBuilder();
            buf.append("[004][proxy->origin] " + Thread.currentThread() + " connection leased ").append(route);
            debugln(buf.toString());
        }

        @Override
        public void onRelease(final HttpHost route, final ConnPoolStats<HttpHost> connPoolStats)
        {
            final StringBuilder buf = new StringBuilder();
            buf.append("[005][proxy->origin] " + Thread.currentThread() + " connection released ").append(route);
            final PoolStats totals = connPoolStats.getTotalStats();
            buf.append("; total kept alive: ").append(totals.getAvailable()).append("; ");
            buf.append("total allocated: ").append(totals.getLeased() + totals.getAvailable());
            buf.append(" of ").append(totals.getMax());
            debugln(buf.toString());
        }

    }

    private class HttpServerStreamListener implements Http1StreamListener
    {

        @Override
        public void onRequestHead(final HttpConnection connection, final HttpRequest request)
        {
            debugln("[006][client->proxy] " + Thread.currentThread() + " " +
                    request.getMethod() + " " + request.getRequestUri());
        }

        @Override
        public void onResponseHead(final HttpConnection connection, final HttpResponse response)
        {
            debugln("[007][client<-proxy] " + Thread.currentThread() + " status " + response.getCode() + " [" + response.getReasonPhrase() + "]");
        }

        @Override
        public void onExchangeComplete(final HttpConnection connection, final boolean keepAlive)
        {
            debugln("[008][client<-proxy] " + Thread.currentThread() + " exchange completed; " +
                    "connection " + (keepAlive ? "kept alive" : "cannot be kept alive"));
        }

    }

    private class HttpServerExceptionListener implements ExceptionListener
    {

        @Override
        public void onError(final Exception ex)
        {
            if (ex instanceof SocketException)
            {
                debugln("[009][client->proxy] " + Thread.currentThread() + " " + ex.getMessage());
            }
            else
            {
                debugln("[010][client->proxy] " + Thread.currentThread() + " " + ex.getMessage());
                ex.printStackTrace(System.out);
            }
        }

        @Override
        public void onError(final HttpConnection connection, final Exception ex)
        {
            if (ex instanceof SocketTimeoutException)
            {
                debugln("[011][client->proxy] " + Thread.currentThread() + " time out");
            }
            else if (ex instanceof SocketException || ex instanceof ConnectionClosedException)
            {
                debugln("[012][client->proxy] " + Thread.currentThread() + " " + ex.getMessage());
            }
            else
            {
                debugln("[013][client->proxy] " + Thread.currentThread() + " " + ex.getMessage());
                ex.printStackTrace(System.out);
            }
        }

    }

    private class ProxyHandler
            implements HttpRequestHandler
    {
        private final boolean        ONLINE;
        private final IHttpRequester REQUESTER;
        private final String         PROTOCOL;
        private final String         HOSTNAME;
        private final int            PORT;
        private final boolean        PRESERVE_HOST_HEADER;
        private StubUtils            STUB_UTILS;

        public ProxyHandler(
                final boolean online,
                final IHttpRequester requester,
                final String localhostName,
                final boolean preserveHostHeader,
                final String recordingDirectory,
                List<String> offlineMatchHeaders,
                List<String> offlineIgnoreParameters,
                boolean offlineCyclicResponses,
                List<Integer> offlineIgnoreHttpStatus)
                throws ProtocolException,
                IOException
        {
            super();

            this.ONLINE    = online;
            this.REQUESTER = requester;
            URL url = new URL(localhostName);
            this.PROTOCOL             = url.getProtocol();
            this.HOSTNAME             = url.getHost();
            this.PORT                 = HttpUtils.getPort(url);
            this.PRESERVE_HOST_HEADER = preserveHostHeader;
            if (this.ONLINE && recordingDirectory != null)
            {
                // STUB_UTILS for writing stubs in online mode
                this.STUB_UTILS = StubUtils.getInstance(recordingDirectory, offlineMatchHeaders, offlineIgnoreParameters, offlineCyclicResponses, offlineIgnoreHttpStatus);
            }
        }

        @Override
        public void handle(
                final ClassicHttpRequest incomingRequest,
                final ClassicHttpResponse outgoingResponse,
                final HttpContext serverContext)
        {
            try
            {
                processRequest(incomingRequest, outgoingResponse, serverContext);
            }
            catch (Throwable e)
            {
                e.printStackTrace();

                outgoingResponse.setCode(HttpStatus.SC_INTERNAL_SERVER_ERROR);
                outgoingResponse.setHeader(HttpHeaders.CONTENT_TYPE, "text/plain; charset=UTF-8");
                outgoingResponse.setHeader(HttpHeaders.CONTENT_ENCODING, null);
                outgoingResponse.setEntity(new StringEntity(
                        StringUtils.getFullErrorMessage(e),
                        Constants.UTF8_CHARSET));

                ClassicHttpRequest outgoingRequest = incomingRequest;
                String             fromUrl         = incomingRequest.getRequestUri();
                try
                {
                    fromUrl = incomingRequest.getUri().toString();
                }
                catch (Throwable e1)
                {
                    // ignore
                }
                String toUrl = HttpUtils.proxyUrls(fromUrl);
                if (ONLINE)
                {
                    try
                    {
                        outgoingResponse.setHeader(Constants.HTTPMOCK_FROM_URL, fromUrl);
                        outgoingResponse.setHeader(Constants.HTTPMOCK_TO_URL, toUrl);
                        outgoingRequest = new BasicClassicHttpRequest(
                                incomingRequest.getMethod(),
                                URI.create(toUrl));
                        HttpUtils.copyHeaders(incomingRequest, outgoingRequest, Constants.PROXY_URLS_TEXT_TRANSFORMER, PRESERVE_HOST_HEADER);
                        HttpUtils.copyBody(incomingRequest, outgoingRequest, Constants.PROXY_URLS_TEXT_TRANSFORMER);
                    }
                    catch (Throwable e2)
                    {
                        // ignore
                    }
                }
                NetworkView.getInstance().updateRequestWithResponse(outgoingRequest, outgoingResponse, null, fromUrl, toUrl);
            }
        }

        private void processRequest(
                final ClassicHttpRequest incomingRequest,
                final ClassicHttpResponse outgoingResponse,
                final HttpContext serverContext)
                throws IOException,
                URISyntaxException,
                HttpException
        {
            ClassicHttpResponse incomingResponse = null;
            try
            {
                // validates if the request is for this server or dropped here
                // by mistake
                final String fromUrl = incomingRequest.getUri().toString();
                validateRequestUrlHost(fromUrl);

                // validates the target server: if the target url is the
                // same of the request after the proxy method processing, then
                // no proxy was mapped and proceed this request will cause a new
                // request like this one to this server causing a looping in
                // this server execution. Stops the request here.
                // However if in offline mode, there will not be a new request
                // to do, and no looping, only reading a file... so proceed is
                // authorized
                String toUrl = HttpUtils.proxyUrls(fromUrl);
                if (ONLINE && fromUrl.equals(toUrl))
                {
                    // IF:
                    // 1. this server answer all localhost requests;
                    // 2. proxy mapping have localhost mapping
                    // 3. request host is not localhost
                    //
                    // fromUrl and toUrl will be the same, because request host is not mapped.
                    //
                    // So, try to add request host mapping based on the localhost proxy mapping and
                    // try to proxy url again
                    tryToAddRequestHostnameBasedOnLocalhostMappings(fromUrl);
                    toUrl = HttpUtils.proxyUrls(fromUrl);
                    if (ONLINE && fromUrl.equals(toUrl))
                    {
                        throw new RuntimeException(
                                "A url '"
                                        + fromUrl
                                        + "' não está mapeada para ser proxiada por este servidor proxy. Para ter o proxy e o proxy reverso das mensagens http, esta url precisa estar mapeada na ferramenta");
                    }
                }

                final ClassicHttpRequest requestStub = new BasicClassicHttpRequest(
                        incomingRequest.getMethod(),
                        incomingRequest.getUri());
                HttpUtils.copyHeaders(incomingRequest, requestStub, null, true);
                HttpUtils.copyBody(incomingRequest, requestStub, null);

                final HttpHost           targetHost;
                final ClassicHttpRequest outgoingRequest;
                if (ONLINE)
                {
                    targetHost      = HttpHost.create(HttpUtils.extractHost(toUrl));
                    outgoingRequest = new BasicClassicHttpRequest(
                            incomingRequest.getMethod(),
                            URI.create(toUrl));
                    HttpUtils.copyHeaders(requestStub, outgoingRequest, Constants.PROXY_URLS_TEXT_TRANSFORMER, PRESERVE_HOST_HEADER);
                    HttpUtils.copyBody(requestStub, outgoingRequest, Constants.PROXY_URLS_TEXT_TRANSFORMER);
                }
                else
                {
                    targetHost      = HttpHost.create(HttpUtils.extractHost(incomingRequest.getUri()));
                    outgoingRequest = requestStub;
                }

                NetworkView.getInstance().addRequest(outgoingRequest, fromUrl, toUrl);

                final HttpCoreContext clientContext = HttpCoreContext.create();
                incomingResponse = REQUESTER.execute(
                        targetHost, outgoingRequest, Timeout.ofSeconds(90), clientContext);

                outgoingResponse.setCode(incomingResponse.getCode());
                outgoingResponse.setReasonPhrase(incomingResponse.getReasonPhrase());
                if (ONLINE)
                {
                    HttpUtils.copyHeaders(incomingResponse, outgoingResponse, Constants.REVERSE_PROXY_URLS_TEXT_TRANSFORMER, PRESERVE_HOST_HEADER);
                    HttpUtils.copyBody(incomingResponse, outgoingResponse, Constants.REVERSE_PROXY_URLS_TEXT_TRANSFORMER);
                    outgoingResponse.setHeader(Constants.HTTPMOCK_FROM_URL, fromUrl);
                    outgoingResponse.setHeader(Constants.HTTPMOCK_TO_URL, toUrl);
                    if (STUB_UTILS != null)
                    {
                        STUB_UTILS.writeStubFile(requestStub, outgoingResponse);
                    }
                }
                else
                {
                    HttpUtils.copyHeaders(incomingResponse, outgoingResponse, null, PRESERVE_HOST_HEADER);
                    HttpUtils.copyBody(incomingResponse, outgoingResponse, null);
                }
                NetworkView.getInstance().updateRequestWithResponse(outgoingRequest, outgoingResponse, outgoingResponse.getHeader(Constants.MAPPING_FILE_NAME) != null, fromUrl, toUrl);
            }
            finally
            {
                try
                {
                    if (incomingResponse != null)
                    {
                        incomingResponse.close();
                    }
                }
                catch (Throwable e)
                {
                    // ignore
                }
            }
        }

        private void validateRequestUrlHost(String fromUrl)
                throws MalformedURLException
        {
            URL    url             = new URL(fromUrl);
            String requestProtocol = url.getProtocol();
            String requestHostname = url.getHost();
            int    requestPort     = HttpUtils.getPort(url);

            if (Constants.LOCALHOST.equalsIgnoreCase(this.HOSTNAME))
            {
                // if this server answer all localhost requests, assumes that requestHostname is
                // localhost
                requestHostname = Constants.LOCALHOST;
            }

            // the request is for this server or fell here by mistake?
            if (!this.PROTOCOL.equals(requestProtocol)
                    || !this.HOSTNAME.equals(requestHostname)
                    || this.PORT != requestPort)
            {
                String virtualServer = HttpUtils.extractHost(fromUrl);
                throw new RuntimeException(
                        "O Servidor Virtual '"
                                + virtualServer
                                + "' não está mapeado para ser atendido por esta ferramenta");
            }
        }

        public void tryToAddRequestHostnameBasedOnLocalhostMappings(String fromUrl)
                throws MalformedURLException
        {
            if (Constants.LOCALHOST.equals(this.HOSTNAME))
            {
                URL    url             = new URL(fromUrl);
                String requestHostname = url.getHost();

                // add automatically new mappings from this requestHostname based on the
                // existing localhost mapping
                HttpUtils.addNewHostMappingsBasedOnExistingHostMapping(Constants.LOCALHOST, requestHostname);
            }
        }
    }

    public static interface TextTransformer
    {

        public String transform(String text);

    }

    private class ApplicationShutdownHook extends Thread
    {
        private HttpServer     server;
        private IHttpRequester requester;

        public ApplicationShutdownHook(HttpServer server)
        {
            this.server = server;
        }

        public ApplicationShutdownHook(IHttpRequester requester)
        {
            this.requester = requester;
        }

        @Override
        public void run()
        {
            if (server != null)
            {
                System.out.println("Desligando o servidor na porta " + server.getLocalPort() + "...");
                server.close(CloseMode.GRACEFUL);
                System.out.println("\t[OK] Servidor na porta " + server.getLocalPort() + " parado");
            }
            if (requester != null)
            {
                requester.close(CloseMode.GRACEFUL);
            }
        }

    }

    private static void debugln(Object obj)
    {
        // System.out.println(obj);
    }
}