package br.com.httpmock;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.swing.JOptionPane;

import br.com.httpmock.server.ProxyServer;
import br.com.httpmock.utils.CommandLineUtils;
import br.com.httpmock.utils.Constants;
import br.com.httpmock.utils.FileUtils;
import br.com.httpmock.utils.FileUtils.Parameter;
import br.com.httpmock.utils.HttpUtils;
import br.com.httpmock.utils.StringUtils;
import br.com.httpmock.utils.ValidatorUtils;
import br.com.httpmock.views.NetworkView;

public class Main
{
    public static void main(String[] args)
            throws Exception
    {
        try
        {
            System.out.println("Iniciando o(s) servidor(es)...");

            String configFileName = CommandLineUtils.getParameter("config", args);
            ValidatorUtils.notEmpty(configFileName, "O parâmetro --config seguido do nome do arquivo é obrigatório para a execução da ferramenta");

            List<Parameter> parameters = FileUtils.readParameters(configFileName);
            sortParameters(parameters);

            Map<Integer, List<LocalServer>> mappedLocalServers = getMappedLocalServers(parameters, configFileName);
            if (mappedLocalServers.size() == 0)
            {
                throw new RuntimeException("Não fora(m) encontrado(s) mapeamento(s) de servidor(es) no arquivo de configuração '" + configFileName + "'");
            }
            for (List<LocalServer> localServerList : mappedLocalServers.values())
            {
                ProxyServer.executar(localServerList);
            }

            for (List<LocalServer> localServerList : mappedLocalServers.values())
            {
                for (LocalServer localServer : localServerList)
                {
                    NetworkView.getInstance().addLocalServer(localServer);
                }
            }

            System.out.println("Servidor(es) iniciado(s)");
        }
        catch (Throwable e)
        {
            e.printStackTrace();
            System.out.println("Falha ao iniciar o(s) servidor(es)");
            String mensagem = StringUtils.getFullErrorMessage(e);
            JOptionPane.showMessageDialog(NetworkView.getInstance(), mensagem, "Erro", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }

    private static void sortParameters(List<Parameter> parameters)
    {
        int serverNameCount = 0;
        for (Parameter parameter : parameters)
        {
            if (Constants.VIRTUAL_SERVER.equalsIgnoreCase(parameter.getKey()))
            {
                parameter.setSortOrder2(1);
            }
            else if (Constants.PROXY_PASS_AND_REVERSE.equalsIgnoreCase(parameter.getKey()))
            {
                parameter.setSortOrder2(2);
            }
            else if (Constants.RECORDING_DIRECTORY.equalsIgnoreCase(parameter.getKey()))
            {
                parameter.setSortOrder2(3);
            }
            else if (Constants.PRESERVE_HOST_HEADER.equalsIgnoreCase(parameter.getKey()))
            {
                parameter.setSortOrder2(4);
            }
            else if (Constants.HTTPS_REQUIRE_CLIENT_CERT.equalsIgnoreCase(parameter.getKey()))
            {
                parameter.setSortOrder2(5);
            }
            else if (Constants.HTTPS_KEYSTORE.equalsIgnoreCase(parameter.getKey()))
            {
                parameter.setSortOrder2(6);
            }
            else if (Constants.KEYSTORE_PASSWORD.equalsIgnoreCase(parameter.getKey()))
            {
                parameter.setSortOrder2(7);
            }
            else if (Constants.HTTPS_TRUSTSTORE.equalsIgnoreCase(parameter.getKey()))
            {
                parameter.setSortOrder2(8);
            }
            else if (Constants.TRUSTSTORE_PASSWORD.equalsIgnoreCase(parameter.getKey()))
            {
                parameter.setSortOrder2(9);
            }
            else if (Constants.OFFLINE_MATCH_HEADERS.equalsIgnoreCase(parameter.getKey()))
            {
                parameter.setSortOrder2(10);
            }
            else if (Constants.OFFLINE_IGNORE_PARAMETERS.equalsIgnoreCase(parameter.getKey()))
            {
                parameter.setSortOrder2(11);
            }
            else if (Constants.OFFLINE_CYCLIC_RESPONSES.equalsIgnoreCase(parameter.getKey()))
            {
                parameter.setSortOrder2(12);
            }
            else if (Constants.OFFLINE_IGNORE_HTTP_STATUS.equalsIgnoreCase(parameter.getKey()))
            {
                parameter.setSortOrder2(13);
            }
            else
            {
                parameter.setSortOrder2(Integer.MAX_VALUE - 2);
            }

            if (Constants.VIRTUAL_SERVER.equalsIgnoreCase(parameter.getKey()))
            {
                serverNameCount++;
            }
            parameter.setSortOrder1(serverNameCount);
        }
        Collections.sort(parameters);
    }

    public static class LocalServer
    {
        private boolean          online      = true;
        private String           hostname;
        private String           keystore;
        private String           keystorePassword;
        private String           truststore;
        private String           truststorePassword;
        private boolean          clientAuth;
        private boolean          preserveHostHeader;
        private String           recordingDirectory;
        private List<String>     offlineMatchHeaders;
        private List<String>     offlineIgnoreParameters;
        private boolean          offlineCyclicResponses;
        private List<Integer>    offlineIgnoreHttpStatus;
        private List<UrlMapping> urlMappings = new Vector<UrlMapping>();

        public LocalServer(boolean online,
                String hostname,
                String keystore,
                String keystorePassword,
                String truststore,
                String truststorePassword,
                boolean clientAuth,
                boolean preserveHostHeader,
                String recordingDirectory,
                List<String> offlineMatchHeaders,
                List<String> offlineIgnoreParameters,
                boolean offlineCyclicResponses,
                List<Integer> offlineIgnoreHttpStatus)
        {
            super();
            this.online                  = online;
            this.hostname                = hostname;
            this.keystore                = keystore;
            this.keystorePassword        = keystorePassword;
            this.truststore              = truststore;
            this.truststorePassword      = truststorePassword;
            this.clientAuth              = clientAuth;
            this.preserveHostHeader      = preserveHostHeader;
            this.recordingDirectory      = recordingDirectory;
            this.offlineMatchHeaders     = offlineMatchHeaders;
            this.offlineIgnoreParameters = offlineIgnoreParameters;
            this.offlineCyclicResponses  = offlineCyclicResponses;
            this.offlineIgnoreHttpStatus = offlineIgnoreHttpStatus;
        }

        public boolean isOnline()
        {
            return online;
        }

        public String getHostname()
        {
            return hostname;
        }

        public String getKeystore()
        {
            return keystore;
        }

        public String getKeystorePassword()
        {
            return keystorePassword;
        }

        public String getTruststore()
        {
            return truststore;
        }

        public String getTruststorePassword()
        {
            return truststorePassword;
        }

        public boolean isClientAuth()
        {
            return clientAuth;
        }

        public boolean isPreserveHostHeader()
        {
            return preserveHostHeader;
        }

        public String getRecordingDirectory()
        {
            return recordingDirectory;
        }

        public List<String> getOfflineMatchHeaders()
        {
            return offlineMatchHeaders;
        }

        public List<String> getOfflineIgnoreParameters()
        {
            return offlineIgnoreParameters;
        }

        public boolean isOfflineCyclicResponses()
        {
            return offlineCyclicResponses;
        }

        public List<Integer> getOfflineIgnoreHttpStatus()
        {
            return offlineIgnoreHttpStatus;
        }

        public void addUrlMapping(String fromUrl, String toUrl, String operatingMode)
        {
            if (operatingMode == null)
            {
                this.urlMappings.add(new UrlMapping(fromUrl, toUrl, recordingDirectory, online));
            }
            else if (Constants.ONLINE.equalsIgnoreCase(operatingMode))
            {
                this.urlMappings.add(new UrlMapping(fromUrl, toUrl, null, true));
            }
            else if (Constants.ONLINE_RECORDING.equalsIgnoreCase(operatingMode))
            {
                this.urlMappings.add(new UrlMapping(fromUrl, toUrl, recordingDirectory, true));
            }
            else if (Constants.OFFLINE.equalsIgnoreCase(operatingMode))
            {
                this.urlMappings.add(new UrlMapping(fromUrl, toUrl, recordingDirectory, false));
            }
            else
            {
                throw new RuntimeException("Modo de operação " + operatingMode + " não cadastrado");
            }
        }

        public List<UrlMapping> getUrlMappings()
        {
            return new Vector<UrlMapping>(urlMappings);
        }

    }

    public static class UrlMapping
    {
        private String  fromUrl;
        private String  toUrl;
        private String  recordingDirectory;
        private Boolean online;

        public UrlMapping(String fromUrl, String toUrl, String recordingDirectory, boolean online)
        {
            super();
            this.fromUrl            = fromUrl;
            this.toUrl              = toUrl;
            this.recordingDirectory = recordingDirectory;
            this.online             = online;
        }

        public String getFromUrl()
        {
            return fromUrl;
        }

        public String getToUrl()
        {
            return toUrl;
        }

        public String getRecordingDirectory()
        {
            return recordingDirectory;
        }

        public Boolean isOnline()
        {
            return online;
        }

    }

    private static Map<Integer, List<LocalServer>> getMappedLocalServers(
            List<Parameter> parameters,
            String configFileName)
            throws MalformedURLException
    {
        Map<Integer, List<LocalServer>> serversByPortNumber = new HashMap<Integer, List<LocalServer>>();
        if (parameters != null && parameters.size() > 0)
        {
            String        virtualServer           = null;
            boolean       online                  = false;
            List<String>  path                    = new ArrayList<String>();
            List<String>  toUrl                   = new ArrayList<String>();
            List<String>  operatingMode           = new ArrayList<String>();
            String        recordingDirectory      = null;
            boolean       clientAuth              = false;
            boolean       preserveHostHeader      = false;
            String        keystore                = null;
            String        keystorePassword        = "";
            String        truststore              = null;
            String        truststorePassword      = "";
            List<String>  matchHeaders            = null;
            List<String>  offlineIgnoreParameters = null;
            boolean       offlineCyclicResponses  = false;
            List<Integer> offlineIgnoreHttpStatus = null;
            for (Parameter parameter : parameters)
            {
                if (!Constants.VIRTUAL_SERVER.equalsIgnoreCase(parameter.getKey()))
                {
                    ValidatorUtils.notEmpty(virtualServer,
                            "O parâmetro '"
                                    + Constants.VIRTUAL_SERVER
                                    + "' no arquivo de configuração '"
                                    + configFileName
                                    + "', na linha "
                                    + parameter.getLineNumber()
                                    + " não está preenchido ou definido para este mapeamento ['"
                                    + parameter.getValue()
                                    + "']");
                }
                if (Constants.VIRTUAL_SERVER.equalsIgnoreCase(parameter.getKey()))
                {
                    ValidatorUtils.notEmpty(parameter.getValue(),
                            "O parâmetro '"
                                    + Constants.VIRTUAL_SERVER
                                    + "' no arquivo de configuração '"
                                    + configFileName
                                    + "', na linha "
                                    + parameter.getLineNumber()
                                    + " não está preenchido ['"
                                    + parameter.getValue()
                                    + "']");
                    ValidatorUtils.validUrlOrAuthority(parameter.getValue(),
                            "O parâmetro '"
                                    + Constants.VIRTUAL_SERVER
                                    + "' no arquivo de configuração '"
                                    + configFileName
                                    + "', na linha "
                                    + parameter.getLineNumber()
                                    + " não possui uma url ou autoridade de url válida ['"
                                    + parameter.getValue()
                                    + "']");

                    addLocalServer(
                            serversByPortNumber,
                            virtualServer,
                            online,
                            path,
                            toUrl,
                            operatingMode,
                            recordingDirectory,
                            clientAuth,
                            preserveHostHeader,
                            keystore,
                            keystorePassword,
                            truststore,
                            truststorePassword,
                            matchHeaders,
                            offlineIgnoreParameters,
                            offlineCyclicResponses,
                            offlineIgnoreHttpStatus,
                            configFileName,
                            parameter);

                    virtualServer           = null;
                    online                  = false;
                    path                    = new ArrayList<String>();
                    toUrl                   = new ArrayList<String>();
                    operatingMode           = new ArrayList<String>();
                    recordingDirectory      = null;
                    clientAuth              = false;
                    preserveHostHeader      = false;
                    keystore                = null;
                    keystorePassword        = "";
                    truststore              = null;
                    truststorePassword      = "";
                    matchHeaders            = null;
                    offlineIgnoreParameters = null;
                    offlineCyclicResponses  = false;
                    offlineIgnoreHttpStatus = null;

                    virtualServer           = parameter.getValue();
                }
                else if (Constants.PROXY_PASS_AND_REVERSE.equalsIgnoreCase(parameter.getKey()))
                {
                    String[] values = parameter.getValue().split(" ");
                    if (values.length < 2)
                    {
                        throw new RuntimeException(
                                "O parâmetro '"
                                        + Constants.PROXY_PASS_AND_REVERSE
                                        + "' no arquivo de configuração '"
                                        + configFileName
                                        + "' deve ter no mínimo o path origem e a url de destino. Valor informado ['"
                                        + parameter.getValue() + "']");
                    }
                    ValidatorUtils.notEmpty(values[0],
                            "O parâmetro '"
                                    + Constants.PROXY_PASS_AND_REVERSE
                                    + "' no arquivo de configuração '"
                                    + configFileName
                                    + "', na linha "
                                    + parameter.getLineNumber()
                                    + " não está preenchido corretamente ['"
                                    + values[0]
                                    + "']");
                    ValidatorUtils.notEmpty(values[1],
                            "O parâmetro '"
                                    + Constants.PROXY_PASS_AND_REVERSE
                                    + "' no arquivo de configuração '"
                                    + configFileName
                                    + "', na linha "
                                    + parameter.getLineNumber()
                                    + " não está preenchido corretamente ['"
                                    + values[1]
                                    + "']");
                    ValidatorUtils.validUrlOrAuthority(values[1],
                            "O parâmetro '"
                                    + Constants.PROXY_PASS_AND_REVERSE
                                    + "' no arquivo de configuração '"
                                    + configFileName
                                    + "', na linha "
                                    + parameter.getLineNumber()
                                    + " não possui uma url ou autoridade de url válida ['"
                                    + values[1]
                                    + "']");
                    if (values.length >= 3)
                    {
                        if (!Constants.ONLINE.equalsIgnoreCase(values[2])
                                && !Constants.ONLINE_RECORDING.equalsIgnoreCase(values[2])
                                && !Constants.OFFLINE.equalsIgnoreCase(values[2]))
                        {
                            throw new RuntimeException(
                                    "O parâmetro '"
                                            + Constants.PROXY_PASS_AND_REVERSE
                                            + "' no arquivo de configuração '"
                                            + configFileName
                                            + "', na linha "
                                            + parameter.getLineNumber()
                                            + " não possui um modo de operação válido. Modos válidos: ["
                                            + "'" + Constants.ONLINE.toUpperCase() + "',"
                                            + "'" + Constants.ONLINE_RECORDING.toUpperCase() + "', "
                                            + "'" + Constants.OFFLINE.toUpperCase() + "']. Modo informado: ['"
                                            + values[2]
                                            + "']");
                        }
                    }

                    path.add(values[0]);
                    toUrl.add(values[1]);
                    if (values.length >= 3)
                    {
                        if (!Constants.OFFLINE.toUpperCase().equalsIgnoreCase(values[2]))
                        {
                            online = true;
                        }
                        operatingMode.add(values[2]);
                    }
                    else
                    {
                        online = true;
                        operatingMode.add(null);
                    }
                }
                else if (Constants.RECORDING_DIRECTORY.equalsIgnoreCase(parameter.getKey()))
                {
                    ValidatorUtils.notEmpty(parameter.getValue(),
                            "O parâmetro '"
                                    + Constants.RECORDING_DIRECTORY
                                    + "' no arquivo de configuração '"
                                    + configFileName
                                    + "', na linha "
                                    + parameter.getLineNumber()
                                    + " não está preenchido ['"
                                    + parameter.getValue()
                                    + "']");

                    recordingDirectory = parameter.getValue();
                }
                else if (Constants.PRESERVE_HOST_HEADER.equalsIgnoreCase(parameter.getKey()))
                {
                    preserveHostHeader = true;
                }
                else if (Constants.HTTPS_REQUIRE_CLIENT_CERT.equalsIgnoreCase(parameter.getKey()))
                {
                    clientAuth = true;
                }
                else if (Constants.HTTPS_KEYSTORE.equalsIgnoreCase(parameter.getKey()))
                {
                    ValidatorUtils.notEmpty(parameter.getValue(),
                            "O parâmetro '"
                                    + Constants.HTTPS_KEYSTORE
                                    + "' no arquivo de configuração '"
                                    + configFileName
                                    + "', na linha "
                                    + parameter.getLineNumber()
                                    + " não está preenchido ['"
                                    + parameter.getValue()
                                    + "']");
                    keystore = parameter.getValue();
                }
                else if (Constants.KEYSTORE_PASSWORD.equalsIgnoreCase(parameter.getKey()))
                {
                    ValidatorUtils.notEmpty(parameter.getValue(),
                            "O parâmetro '"
                                    + Constants.KEYSTORE_PASSWORD
                                    + "' no arquivo de configuração '"
                                    + configFileName
                                    + "', na linha "
                                    + parameter.getLineNumber()
                                    + " não está preenchido ['"
                                    + parameter.getValue()
                                    + "']");

                    keystorePassword = parameter.getValue();
                }
                else if (Constants.HTTPS_TRUSTSTORE.equalsIgnoreCase(parameter.getKey()))
                {
                    ValidatorUtils.notEmpty(parameter.getValue(),
                            "O parâmetro '"
                                    + Constants.HTTPS_TRUSTSTORE
                                    + "' no arquivo de configuração '"
                                    + configFileName
                                    + "', na linha "
                                    + parameter.getLineNumber()
                                    + " não está preenchido ['"
                                    + parameter.getValue()
                                    + "']");

                    truststore = parameter.getValue();
                }
                else if (Constants.TRUSTSTORE_PASSWORD.equalsIgnoreCase(parameter.getKey()))
                {
                    ValidatorUtils.notEmpty(parameter.getValue(),
                            "O parâmetro '"
                                    + Constants.TRUSTSTORE_PASSWORD
                                    + "' no arquivo de configuração '"
                                    + configFileName
                                    + "', na linha "
                                    + parameter.getLineNumber()
                                    + " não está preenchido ['"
                                    + parameter.getValue()
                                    + "']");

                    truststorePassword = parameter.getValue();
                }
                else if (Constants.OFFLINE_MATCH_HEADERS.equalsIgnoreCase(parameter.getKey()))
                {
                    ValidatorUtils.notEmpty(parameter.getValue(),
                            "O parâmetro '"
                                    + Constants.OFFLINE_MATCH_HEADERS
                                    + "' no arquivo de configuração '"
                                    + configFileName
                                    + "', na linha "
                                    + parameter.getLineNumber()
                                    + " não está preenchido ['"
                                    + parameter.getValue()
                                    + "']");
                    String[] values = parameter.getValue().split(",");
                    matchHeaders = new Vector<String>();
                    for (String value : values)
                    {
                        ValidatorUtils.notEmpty(value.trim(),
                                "Um dos valores do parâmetro '"
                                        + Constants.OFFLINE_MATCH_HEADERS
                                        + "' no arquivo de configuração '"
                                        + configFileName
                                        + "', na linha "
                                        + parameter.getLineNumber()
                                        + " não está preenchido ['"
                                        + parameter.getValue()
                                        + "']");
                        matchHeaders.add(value.trim());
                    }
                }
                else if (Constants.OFFLINE_IGNORE_PARAMETERS.equalsIgnoreCase(parameter.getKey()))
                {
                    ValidatorUtils.notEmpty(parameter.getValue(),
                            "O parâmetro '"
                                    + Constants.OFFLINE_IGNORE_PARAMETERS
                                    + "' no arquivo de configuração '"
                                    + configFileName
                                    + "', na linha "
                                    + parameter.getLineNumber()
                                    + " não está preenchido ['"
                                    + parameter.getValue()
                                    + "']");
                    String[] values = parameter.getValue().split(",");
                    offlineIgnoreParameters = new Vector<String>();
                    for (String value : values)
                    {
                        ValidatorUtils.notEmpty(value.trim(),
                                "Um dos valores do parâmetro '"
                                        + Constants.OFFLINE_IGNORE_PARAMETERS
                                        + "' no arquivo de configuração '"
                                        + configFileName
                                        + "', na linha "
                                        + parameter.getLineNumber()
                                        + " não está preenchido ['"
                                        + parameter.getValue()
                                        + "']");
                        offlineIgnoreParameters.add(value.trim());
                    }
                }
                else if (Constants.OFFLINE_CYCLIC_RESPONSES.equalsIgnoreCase(parameter.getKey()))
                {
                    offlineCyclicResponses = true;
                }
                else if (Constants.OFFLINE_IGNORE_HTTP_STATUS.equalsIgnoreCase(parameter.getKey()))
                {
                    ValidatorUtils.notEmpty(parameter.getValue(),
                            "O parâmetro '"
                                    + Constants.OFFLINE_IGNORE_HTTP_STATUS
                                    + "' no arquivo de configuração '"
                                    + configFileName
                                    + "', na linha "
                                    + parameter.getLineNumber()
                                    + " não está preenchido ['"
                                    + parameter.getValue()
                                    + "']");
                    String[] values = parameter.getValue().split(",");
                    offlineIgnoreHttpStatus = new Vector<Integer>();
                    for (String value : values)
                    {
                        ValidatorUtils.numeric(value.trim(),
                                "Um dos valores do parâmetro '"
                                        + Constants.OFFLINE_IGNORE_HTTP_STATUS
                                        + "' no arquivo de configuração '"
                                        + configFileName
                                        + "', na linha "
                                        + parameter.getLineNumber()
                                        + " não é um número válido ['"
                                        + parameter.getValue()
                                        + "']");
                        offlineIgnoreHttpStatus.add(StringUtils.toInteger(value.trim()));
                    }
                }
            }

            addLocalServer(
                    serversByPortNumber,
                    virtualServer,
                    online,
                    path,
                    toUrl,
                    operatingMode,
                    recordingDirectory,
                    clientAuth,
                    preserveHostHeader,
                    keystore,
                    keystorePassword,
                    truststore,
                    truststorePassword,
                    matchHeaders,
                    offlineIgnoreParameters,
                    offlineCyclicResponses,
                    offlineIgnoreHttpStatus,
                    configFileName,
                    null);

        }
        return serversByPortNumber;
    }

    private static void addLocalServer(
            Map<Integer, List<LocalServer>> serversByPortNumber,
            String virtualServerEspecified,
            boolean online,
            List<String> path,
            List<String> toUrl,
            List<String> operatingMode,
            String recordingDirectoryEspecified,
            boolean clientAuth,
            boolean preserveHostHeader,
            String keystore,
            String keystorePassword,
            String truststore,
            String truststorePassword,
            List<String> matchHeaders,
            List<String> offlineIgnoreParameters,
            boolean offlineCyclicResponses,
            List<Integer> offlineIgnoreHttpStatus,
            String configFileName,
            Parameter parameter)
            throws MalformedURLException
    {
        if (virtualServerEspecified == null)
        {
            return;
        }
        else
        {
            List<String> virtualServerNames   = new Vector<String>();
            List<String> recordingDirectories = new Vector<String>();
            if (virtualServerEspecified.toUpperCase().startsWith(Constants.HTTP)
                    || virtualServerEspecified.toUpperCase().startsWith(Constants.HTTPS))
            {
                virtualServerNames.add(virtualServerEspecified);
                recordingDirectories.add(recordingDirectoryEspecified);
            }
            else
            {
                URL u1 = new URL("http://" + virtualServerEspecified);
                URL u2 = new URL("https://" + virtualServerEspecified);
                if (HttpUtils.getPort(u1) == HttpUtils.getPort(u2))
                {
                    throw new RuntimeException(
                            "O parâmetro '"
                                    + Constants.VIRTUAL_SERVER
                                    + "' no arquivo de configuração '"
                                    + configFileName
                                    + (parameter == null ? "' antes da última linha" : "', antes da linha " + parameter.getLineNumber())
                                    + " não pode ter a porta especificada quando não for especificado o protocolo do servidor virtual '" +
                                    virtualServerEspecified
                                    + " '");
                }
                virtualServerNames.add("http://" + virtualServerEspecified);
                recordingDirectories.add(appendProtocolOnDirectoryName("http", recordingDirectoryEspecified));
                virtualServerNames.add("https://" + virtualServerEspecified);
                recordingDirectories.add(appendProtocolOnDirectoryName("https", recordingDirectoryEspecified));
            }
            for (int i = 0, size = Math.min(virtualServerNames.size(), recordingDirectories.size()); i < size; i++)
            {
                String virtualServer      = virtualServerNames.get(i);
                String recordingDirectory = recordingDirectories.get(i);
                addLocalServer2(
                        serversByPortNumber,
                        virtualServer,
                        virtualServerEspecified,
                        online,
                        path,
                        toUrl,
                        operatingMode,
                        recordingDirectory,
                        clientAuth,
                        preserveHostHeader,
                        keystore,
                        keystorePassword,
                        truststore,
                        truststorePassword,
                        matchHeaders,
                        offlineIgnoreParameters,
                        offlineCyclicResponses,
                        offlineIgnoreHttpStatus,
                        configFileName,
                        parameter);
            }
        }
    }

    private static String appendProtocolOnDirectoryName(String protocol, String recordingDirectory)
    {
        if (recordingDirectory == null)
        {
            return null;
        }
        int idx1 = recordingDirectory.lastIndexOf("/");
        if (idx1 == -1)
        {
            idx1 = recordingDirectory.lastIndexOf("\\");
        }
        if (idx1 != -1)
        {
            return recordingDirectory.substring(0, idx1 + 1) +
                    protocol +
                    "_" +
                    recordingDirectory.substring(idx1 + 1);
        }
        else
        {
            return protocol +
                    "_" +
                    recordingDirectory;
        }
    }

    private static void addLocalServer2(
            Map<Integer, List<LocalServer>> serversByPortNumber,
            String virtualServer,
            String virtualServerEspecified,
            boolean online,
            List<String> path,
            List<String> toUrl,
            List<String> operatingMode,
            String recordingDirectory,
            boolean clientAuth,
            boolean preserveHostHeader,
            String keystore,
            String keystorePassword,
            String truststore,
            String truststorePassword,
            List<String> matchHeaders,
            List<String> offlineIgnoreParameters,
            boolean offlineCyclicResponses,
            List<Integer> offlineIgnoreHttpStatus,
            String configFileName,
            Parameter parameter)
            throws MalformedURLException
    {
        URL url = new URL(virtualServer);
        virtualServer = url.getProtocol() + "://" + url.getHost() + ":" + HttpUtils.getPort(url);
        int port = HttpUtils.getPort(url);

        if (!online && recordingDirectory == null)
        {
            throw new RuntimeException(
                    "O parâmetro '"
                            + Constants.RECORDING_DIRECTORY
                            + "' no arquivo de configuração '"
                            + configFileName
                            + (parameter == null ? "' antes da última linha" : "', antes da linha " + parameter.getLineNumber())
                            + " é obrigatório quando não informado o parâmetro '"
                            + Constants.PROXY_PASS_AND_REVERSE + "'");
        }

        List<LocalServer> localServerList = serversByPortNumber.get(port);
        if (localServerList == null)
        {
            localServerList = new Vector<LocalServer>();
            serversByPortNumber.put(port, localServerList);
        }
        else
        {
            URL    url2             = new URL(virtualServer);
            String requestHostname2 = url2.getHost();
            int    requestPort2     = HttpUtils.getPort(url2);
            for (LocalServer server : localServerList)
            {
                URL    url1             = new URL(server.getHostname());
                String requestHostname1 = url1.getHost();
                int    requestPort1     = HttpUtils.getPort(url1);
                if (requestHostname1.equals(requestHostname2)
                        && requestPort1 == requestPort2)
                {
                    throw new RuntimeException(
                            "Existem 2 ou mais servidores virtuais mapeados para o mesmo endereço no arquivo de configuração '"
                                    + configFileName
                                    + "' ['" + virtualServerEspecified + "']");
                }
            }
        }
        LocalServer localServer = new LocalServer(
                online,
                virtualServer,
                keystore,
                keystorePassword,
                truststore,
                truststorePassword,
                clientAuth,
                preserveHostHeader,
                recordingDirectory,
                matchHeaders,
                offlineIgnoreParameters,
                offlineCyclicResponses,
                offlineIgnoreHttpStatus);
        localServerList.add(localServer);
        int size = Math.min(path.size(), toUrl.size());
        size = Math.min(size, operatingMode.size());
        for (int i = 0; i < size; i++)
        {
            String fromUrl        = virtualServer + path.get(i);
            String toUrl1         = getToUrlWithDefaultProtocolWhenNoProtocolBasedOnFromUrl(toUrl.get(i), fromUrl);
            String operatingMode1 = operatingMode.get(i);
            ValidatorUtils.validUrl(toUrl1,
                    "A url ou a autoridade de url '"
                            + toUrl.get(i)
                            + "' no arquivo de configuração '"
                            + configFileName
                            + (parameter == null ? "' antes da última linha" : "', antes da linha " + parameter.getLineNumber())
                            + " não é válida");
            if (!Constants.OFFLINE.equalsIgnoreCase(operatingMode1))
            {
                HttpUtils.addProxyMapping(fromUrl, toUrl1);
            }
            if (recordingDirectory == null)
            {
                if (Constants.ONLINE_RECORDING.equalsIgnoreCase(operatingMode1)
                        || Constants.OFFLINE.equalsIgnoreCase(operatingMode1))
                {
                    throw new RuntimeException(
                            "O parâmetro '"
                                    + Constants.RECORDING_DIRECTORY
                                    + "' no arquivo de configuração '"
                                    + configFileName
                                    + (parameter == null ? "' antes da última linha" : "', antes da linha " + parameter.getLineNumber())
                                    + " é obrigatório quando é usado o modo de operação '"
                                    + operatingMode1 + "'");
                }
            }
            localServer.addUrlMapping(fromUrl, toUrl1, operatingMode1);
        }
        if (path.size() == 0 || toUrl.size() == 0)
        {
            localServer.addUrlMapping(virtualServer, "", null);
        }
    }

    private static String getToUrlWithDefaultProtocolWhenNoProtocolBasedOnFromUrl(String toUrl, String fromUrl)
            throws MalformedURLException
    {
        if (toUrl.toUpperCase().startsWith(Constants.HTTP)
                || toUrl.toUpperCase().startsWith(Constants.HTTPS))
        {
            return toUrl;
        }
        else if (new URL(fromUrl).getProtocol().equalsIgnoreCase(Constants.HTTP))
        {
            return "http://" + toUrl;
        }
        else if (new URL(fromUrl).getProtocol().equalsIgnoreCase(Constants.HTTPS))
        {
            return "https://" + toUrl;
        }
        else
        {
            return null;
        }
    }

    public static void testesExecucao(String[] args)
            throws Exception
    {
        NetworkView.getInstance();

        String localHostName1 = "https://localhost.dinamico.com.br";
        String localHostName2 = "http://localhost.dinamico.com.br";
        String localHostName3 = "http://localhost.dinamico.com.br:9999";

        // String localHostName1 = "https://localhost";
        // String localHostName2 = "http://localhost";
        // String localHostName3 = "http://localhost:9999";

        HttpUtils.addProxyMapping("http://localhost.dinamico.com.br/", "http://www.google.com.br");
        HttpUtils.addProxyMapping("https://localhost.dinamico.com.br/", "https://www.google.com.br");
        HttpUtils.addProxyMapping("http://localhost.dinamico.com.br:9999/", "http://wiremock.org");

        List<LocalServer> list1 = new Vector<LocalServer>();
        List<LocalServer> list2 = new Vector<LocalServer>();
        List<LocalServer> list3 = new Vector<LocalServer>();
        list1.add(new LocalServer(true, localHostName1, null, null, null, null, false, false, null, null, null, false, null));
        list2.add(new LocalServer(true, localHostName2, null, null, null, null, false, false, null, null, null, false, null));
        list3.add(new LocalServer(true, localHostName3, null, null, null, null, false, false, null, null, null, false, null));
        ProxyServer.executar(list1);
        ProxyServer.executar(list2);
        ProxyServer.executar(list3);
    }
}
