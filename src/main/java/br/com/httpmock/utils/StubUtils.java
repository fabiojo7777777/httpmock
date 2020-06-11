package br.com.httpmock.utils;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpHeaders;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.http.ProtocolException;
import org.apache.hc.core5.http.io.entity.ByteArrayEntity;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.message.BasicClassicHttpResponse;
import org.apache.hc.core5.http.message.BasicNameValuePair;
import org.apache.hc.core5.net.URLEncodedUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import br.com.httpmock.builders.JsonElementBuilder;
import br.com.httpmock.matchers.EqualsStub;
import br.com.httpmock.matchers.IMatcher;
import br.com.httpmock.matchers.MatchesRegex;
import br.com.httpmock.models.JsonElement;
import br.com.httpmock.models.JsonList;
import br.com.httpmock.models.JsonObject;
import br.com.httpmock.models.JsonProperty;
import br.com.httpmock.models.Stub;
import br.com.httpmock.models.Xml;

public class StubUtils
{
    private static int                                           sequence                 = 0;

    private final String                                         recordingDirectory;
    private final List<String>                                   offlineMatchHeaders;
    private final List<String>                                   offlineIgnoreParameters;
    private boolean                                              offlineIgnoreAllParameters;
    private final LinkedHashMap<EqualsStub, ClassicHttpResponse> stubMatchersSlowSearch;
    private final Map<String, List<EqualsStub>>                  stubMatchersFastSearch;
    private long[]                                               mappingsLastModifiedList = new long[0];
    private boolean                                              offlineCyclicResponses   = false;

    private List<Integer>                                        offlineIgnoreHttpStatus;

    public static StubUtils getInstance(
            String recordingDirectory,
            List<String> offlineMatchHeaders,
            List<String> offlineIgnoreParameters,
            boolean offlineCyclicResponses,
            List<Integer> offlineIgnoreHttpStatus)
            throws ProtocolException,
            IOException
    {
        return new StubUtils(
                recordingDirectory,
                offlineMatchHeaders,
                offlineIgnoreParameters,
                offlineCyclicResponses,
                offlineIgnoreHttpStatus);
    }

    private static synchronized int getSequence()
    {
        sequence++;
        return sequence;
    }

    private StubUtils(
            String recordingDirectory,
            List<String> offlineMatchHeaders,
            List<String> offlineIgnoreParameters,
            boolean offlineCyclicResponses,
            List<Integer> offlineIgnoreHttpStatus)
            throws ProtocolException,
            IOException
    {
        super();
        this.recordingDirectory         = recordingDirectory;
        this.offlineMatchHeaders        = offlineMatchHeaders;
        this.offlineIgnoreParameters    = offlineIgnoreParameters;
        this.offlineIgnoreAllParameters = false;
        if (this.offlineMatchHeaders != null)
        {
            for (int i = 0, size = this.offlineMatchHeaders.size(); i < size; i++)
            {
                String headerName = this.offlineMatchHeaders.get(i);
                if (headerName != null)
                {
                    this.offlineMatchHeaders.set(i, headerName.toLowerCase(Locale.ROOT));
                }
            }
        }
        if (this.offlineIgnoreParameters != null)
        {
            for (String p : this.offlineIgnoreParameters)
            {
                if (Constants.ALL.equalsIgnoreCase(p))
                {
                    this.offlineIgnoreAllParameters = true;
                    break;
                }
            }
        }
        this.stubMatchersSlowSearch  = new LinkedHashMap<EqualsStub, ClassicHttpResponse>();
        this.stubMatchersFastSearch  = new ConcurrentHashMap<String, List<EqualsStub>>();
        this.offlineCyclicResponses  = offlineCyclicResponses;
        this.offlineIgnoreHttpStatus = offlineIgnoreHttpStatus;
        reloadMappingsIfRequired();
    }

    private void loadMappings()
            throws ProtocolException,
            IOException
    {
        this.stubMatchersSlowSearch.clear();
        this.stubMatchersFastSearch.clear();
        List<EqualsStub> stubs = readStubs();
        if (offlineCyclicResponses)
        {
            for (int i = 0, size = stubs.size(); i < size; i++)
            {
                EqualsStub stubMatcher = stubs.get(i);
                if (!isIgnorableHttpResponseStatus(stubMatcher))
                {
                    this.stubMatchersSlowSearch.put(stubMatcher, null);
                    // fast search key: by method and url
                    addFastSearchKey(stubMatcher);
                }
            }
        }
        else
        {
            for (int i = stubs.size() - 1; i >= 0; i--)
            {
                EqualsStub stubMatcher = stubs.get(i);
                if (!isIgnorableHttpResponseStatus(stubMatcher))
                {
                    this.stubMatchersSlowSearch.put(stubMatcher, null);
                    // fast search key: by method and url
                    addFastSearchKey(stubMatcher);
                }
            }
        }
    }

    private boolean isIgnorableHttpResponseStatus(EqualsStub stubMatcher)
            throws IOException
    {
        if (this.offlineIgnoreHttpStatus != null && this.offlineIgnoreHttpStatus.size() > 0)
        {
            Integer status = getResponseStatus(stubMatcher);
            if (this.offlineIgnoreHttpStatus.contains(status))
            {
                return true;
            }
        }
        return false;
    }

    private int getResponseStatus(EqualsStub stubMatcher)
            throws IOException
    {
        int          status           = 0;
        String       mappingFileName  = stubMatcher.getMappingFileName();
        String       json             = FileUtils.readFile(recordingDirectory + "/" + Constants.MAPPINGS_DIRECTORY + "/" + mappingFileName);
        ObjectMapper OBJECT_MAPPER    = new ObjectMapper();
        JsonNode     jsonNode         = OBJECT_MAPPER.readTree(json);
        JsonNode     responseJsonNode = jsonNode.get("response");
        if (responseJsonNode != null)
        {
            status = StringUtils.toInteger(JsonUtils.get(jsonNode, "response.status"));
        }
        return status;
    }

    private void addFastSearchKey(EqualsStub stubMatcher)
    {
        String           key             = getFastSearchKey(stubMatcher);
        List<EqualsStub> stubMatcherList = this.stubMatchersFastSearch.get(key);
        if (stubMatcherList == null)
        {
            stubMatcherList = new Vector<EqualsStub>();
            this.stubMatchersFastSearch.put(key, stubMatcherList);
        }
        stubMatcherList.add(stubMatcher);
    }

    private String getFastSearchKey(EqualsStub stubMatcher)
    {
        List<IMatcher<String>> method = stubMatcher.getMethod();
        List<IMatcher<String>> path   = stubMatcher.getPath();
        return (method + "_" + path).toLowerCase(Locale.ROOT);
    }

    private String getFastSearchKey(Stub stub)
    {
        return getFastSearchKey(
                new EqualsStub()
                        .withMethod(stub.getMethod())
                        .withPath(stub.getPath()));
    }

    private EqualsStub readStubFile(
            String mappingFileName)
            throws IOException,
            ProtocolException
    {
        EqualsStub stubMatcher = new EqualsStub();

        String     json        = FileUtils.readFile(recordingDirectory + "/" + Constants.MAPPINGS_DIRECTORY + "/" + mappingFileName);
        JsonNode   jsonNode    = JsonUtils.read(json);

        stubMatcher.withId(JsonUtils.get(jsonNode, "id"));
        stubMatcher.withUuid(JsonUtils.get(jsonNode, "uuid"));

        JsonNode request = jsonNode.get("request");

        if (request != null)
        {
            stubMatcher.withPath(JsonUtils.get(jsonNode, "request.urlPath"));
            stubMatcher.withMethod(JsonUtils.get(jsonNode, "request.method"));

            // HEADERS
            String headerJsonNode = JsonUtils.get(jsonNode, "request.headers");
            if (headerJsonNode != null)
            {
                List<NameValuePair> headers = getNameValuePairSearch(request, "headers");
                removeOfflineNotMatchedHeaders(headers);
                stubMatcher.withHeader(headers);
            }

            // QUERY PARAMETERS
            String queryParametersJsonNode = JsonUtils.get(jsonNode, "request.queryParameters");
            if (queryParametersJsonNode != null)
            {
                List<NameValuePair> queryParameters = getNameValuePairSearch(request, "queryParameters");
                removeOfflineIgnoredParameters(queryParameters);
                stubMatcher.withQueryParam(queryParameters);
            }

            // BODY PARAMETERS
            String bodyParams = JsonUtils.get(jsonNode, "request.bodyParameters");
            if (bodyParams != null)
            {
                List<NameValuePair> bodyParameters = getNameValuePairSearch(request, "bodyParameters");
                removeOfflineIgnoredParameters(bodyParameters);
                stubMatcher.withBodyParam(bodyParameters);
            }
            else
            {
                // JSON BODY
                JsonNode[] bodyPatterns = JsonUtils.getItems(jsonNode, "request.bodyPatterns");
                if (bodyPatterns != null)
                {
                    for (JsonNode bodyPattern : bodyPatterns)
                    {
                        String equalToJson = JsonUtils.get(bodyPattern, "equalToJson");
                        if (equalToJson != null)
                        {
                            boolean     ignoreArrayOrder    = Boolean.parseBoolean(JsonUtils.get(bodyPattern, "ignoreArrayOrder"));
                            boolean     ignoreExtraElements = Boolean.parseBoolean(JsonUtils.get(bodyPattern, "ignoreExtraElements"));
                            JsonElement jsonElement         = JsonElementBuilder
                                    .Builder()
                                    .build(equalToJson);
                            removeOfflineIgnoredParameters(jsonElement);
                            stubMatcher.withJsonBody(jsonElement, ignoreArrayOrder, ignoreExtraElements);
                        }
                        else
                        {
                            // XML BODY
                            String equalToXml = JsonUtils.get(bodyPattern, "equalToXml");
                            if (equalToXml != null)
                            {
                                Xml xml = new Xml(equalToXml);
                                removeOfflineIgnoredParameters(xml);
                                stubMatcher.withXmlBody(xml);
                            }
                            else
                            {
                                String equalTo = JsonUtils.get(bodyPattern, "equalTo");
                                if (equalTo != null)
                                {
                                    stubMatcher.withTextBody(equalTo);
                                }
                                else
                                {
                                    String matches = JsonUtils.get(bodyPattern, "matches");
                                    if (matches != null)
                                    {
                                        stubMatcher.withTextBody(new MatchesRegex(matches));
                                    }
                                    else
                                    {
                                        // TODO MULTIPART/FORM-DATA: INCLUIR
                                        // SUPORTE AO FORMATO
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        JsonNode responseJsonNode = jsonNode.get("response");
        if (responseJsonNode != null)
        {
            String bodyFileName = JsonUtils.get(jsonNode, "response.bodyFileName");
            if (bodyFileName != null)
            {
                stubMatcher.withBodyFileName(bodyFileName);
            }
        }
        stubMatcher.withMappingFileName(FileUtils.getFileName(mappingFileName));
        return stubMatcher;
    }

    private static List<NameValuePair> getNameValuePairSearch(
            JsonNode jsonNode,
            String path)
    {
        JsonNode            nameValuePairJsonNode = jsonNode.get(path);
        List<NameValuePair> nameValuePairs        = new ArrayList<NameValuePair>();
        if (nameValuePairJsonNode != null)
        {
            Iterator<Entry<String, JsonNode>> it3 = nameValuePairJsonNode.fields();
            while (it3.hasNext())
            {
                Entry<String, JsonNode>           parameter     = it3.next();
                String                            parameterName = parameter.getKey();
                JsonNode                          comparators   = parameter.getValue();
                Iterator<Entry<String, JsonNode>> it4           = comparators.fields();
                while (it4.hasNext())
                {
                    Entry<String, JsonNode> comparator      = it4.next();
                    String                  comparatorName  = comparator.getKey();
                    String                  comparatorValue = comparator.getValue().asText();
                    if ("equalTo".equals(comparatorName))
                    {
                        if (!comparator.getValue().isNull())
                        {
                            nameValuePairs.add(new BasicNameValuePair(parameterName, comparatorValue));
                        }
                        break;
                    }
                    else
                    {
                        throw new RuntimeException("Não existe implementação do comparador " + comparatorName + " nesta ferramenta");
                    }
                }
            }
        }
        return nameValuePairs;
    }

    private ClassicHttpResponse readStubResponse(
            String mappingFileName,
            String bodyFileName)
            throws IOException,
            ProtocolException
    {
        ClassicHttpResponse response         = new BasicClassicHttpResponse(HttpStatus.SC_NOT_FOUND, "Stub não encontrado");
        String              json             = FileUtils.readFile(recordingDirectory + "/" + Constants.MAPPINGS_DIRECTORY + "/" + mappingFileName);
        ObjectMapper        OBJECT_MAPPER    = new ObjectMapper();
        JsonNode            jsonNode         = OBJECT_MAPPER.readTree(json);
        JsonNode            responseJsonNode = jsonNode.get("response");
        if (responseJsonNode != null)
        {
            response.setCode(StringUtils.toInteger(JsonUtils.get(jsonNode, "response.status")));
            response.setReasonPhrase(JsonUtils.get(jsonNode, "response.reasonPhrase"));

            String   contentType         = "text/plain";
            JsonNode respHeadersJsonNode = responseJsonNode.get("headers");
            if (respHeadersJsonNode != null)
            {
                Iterator<Entry<String, JsonNode>> it = respHeadersJsonNode.fields();
                while (it.hasNext())
                {
                    Entry<String, JsonNode> header = it.next();
                    response.setHeader(header.getKey(), header.getValue().asText());
                    if ("content-type".equalsIgnoreCase(header.getKey()))
                    {
                        contentType = header.getValue().asText();
                    }
                }
            }
            response.setHeader(Constants.MAPPING_FILE_NAME, mappingFileName);
            if (bodyFileName != null)
            {
                byte[] binaryFile = FileUtils.readBinaryFile(recordingDirectory + "/" + Constants.FILES_DIRECTORY + "/" + bodyFileName);
                byte[] ziped      = HttpUtils.zipContentIfNecessary(binaryFile, response);
                response.setEntity(new ByteArrayEntity(ziped, ContentType.parse(contentType)));
            }
        }
        return response;
    }

    public void writeStubFile(
            ClassicHttpRequest outgoingRequest,
            ClassicHttpResponse outgoingResponse)
            throws IOException,
            URISyntaxException,
            ProtocolException
    {
        String       requestContentType  = HttpUtils.getContentType(outgoingRequest);
        String       responseContentType = HttpUtils.getContentType(outgoingResponse);
        String[]     filenames           = getBodyAndMappingFilenames(outgoingRequest.getMethod(),
                outgoingResponse.getCode(),
                new URI(outgoingRequest.getPath()),
                ContentType.parse(responseContentType).getMimeType());

        ObjectMapper OBJECT_MAPPER       = new ObjectMapper();

        ObjectNode   stubMapping         = OBJECT_MAPPER.createObjectNode();
        String       uuid                = UUID.randomUUID().toString();
        stubMapping.put("id", uuid);
        stubMapping.put("uuid", uuid);

        ObjectNode request = stubMapping.putObject("request");
        request.put("urlPath", new URI(outgoingRequest.getPath()).getPath());
        request.put("method", outgoingRequest.getMethod());

        List<NameValuePair> queryParams = URLEncodedUtils.parse(
                URI.create(outgoingRequest.getPath()).getQuery(),
                Constants.UTF8_CHARSET);
        addNameValuePairSearchToJson(request, "queryParameters", queryParams);

        if (outgoingRequest.getEntity() != null)
        {
            if (requestContentType.contains("form-urlencoded"))
            {
                String              text       = EntityUtils.toString(outgoingRequest.getEntity(), Constants.UTF8);

                List<NameValuePair> bodyParams = URLEncodedUtils
                        .parse(text, Constants.UTF8_CHARSET);
                addNameValuePairSearchToJson(request, "bodyParameters", bodyParams);

                // This code is only for wiremock compatibility in
                // bodyParameters way of comparison:
                ArrayNode bodyPatterns = null;
                String[]  bodyParams2  = text.split("\\&");
                for (String param : bodyParams2)
                {
                    String[] keyVal = param.split("\\=");
                    if (keyVal.length >= 2)
                    {
                        if (bodyPatterns == null)
                        {
                            bodyPatterns = request.putArray("bodyPatterns");
                        }
                        bodyPatterns
                                .addObject()
                                .put("matches", "(^|.*\\&)" +
                                        keyVal[0] +
                                        "=" +
                                        keyVal[1] +
                                        "(\\&.*|$)");
                    }
                }
            }
            else if (requestContentType.contains("json"))
            {
                String     json         = EntityUtils.toString(outgoingRequest.getEntity(), Constants.UTF8);

                JsonNode   jsonNode     = OBJECT_MAPPER.readTree(json);
                ArrayNode  bodyPatterns = request.putArray("bodyPatterns");
                ObjectNode patt         = bodyPatterns.addObject();
                patt.put("equalToJson", jsonNode.toString());
                patt.put("ignoreArrayOrder", true);
                patt.put("ignoreExtraElements", true);
            }
            else if (requestContentType.contains("xml"))
            {
                // TODO XML: INCLUIR SUPORTE AO FORMATO
                String     xml          = EntityUtils.toString(outgoingRequest.getEntity(), Constants.UTF8);

                ArrayNode  bodyPatterns = request.putArray("bodyPatterns");
                ObjectNode patt         = bodyPatterns.addObject();
                patt.put("equalToXml", xml);
            }
            else
            {
                // TODO MULTIPART/FORM-DATA: INCLUIR SUPORTE AO FORMATO
                String     txt          = EntityUtils.toString(outgoingRequest.getEntity(), Constants.UTF8);

                ArrayNode  bodyPatterns = request.putArray("bodyPatterns");
                ObjectNode patt         = bodyPatterns.addObject();
                patt.put("equalTo", txt);
            }
        }

        Header[] reqHeaders = outgoingRequest.getHeaders();
        addNameValuePairSearchOnJson(request, "headers", reqHeaders);

        ObjectNode response = stubMapping.putObject("response");
        response.put("status", outgoingResponse.getCode());
        response.put("reasonPhrase", outgoingResponse.getReasonPhrase());

        Header[] respHeaders = outgoingResponse.getHeaders();
        if (respHeaders != null)
        {
            if (respHeaders.length > 0)
            {
                ObjectNode responseHeaders = response.putObject("headers");
                for (Header header : respHeaders)
                {
                    responseHeaders.put(header.getName(),
                            header.getValue());
                }
            }
        }

        outgoingResponse.setHeader(Constants.MAPPING_FILE_NAME, FileUtils.getFileName(filenames[1]));
        if (outgoingResponse.getEntity() != null)
        {
            System.out.println("Gravando bodyFile " + filenames[0]);
            response.put("bodyFileName", FileUtils.getFileName(filenames[0]));
            FileUtils.writeToFile(filenames[0],
                    EntityUtils.toByteArray(
                            HttpUtils.wrapHttpEntityForUnzipIfNecessary(
                                    outgoingResponse.getEntity(),
                                    outgoingResponse)));
        }
        FileUtils.writeToFile(filenames[1], stubMapping.toPrettyString().getBytes(Constants.UTF8));
        System.out.println("Gravando mappingfile " + filenames[1]);
        // System.out.println(stubMapping.toPrettyString());
    }

    private static void addNameValuePairSearchToJson(
            ObjectNode objectNode,
            String searchName,
            List<NameValuePair> nameValuePair)
    {
        addNameValuePairSearchOnJson(objectNode, searchName, nameValuePair.toArray(new NameValuePair[nameValuePair.size()]));
    }

    private static void addNameValuePairSearchOnJson(
            ObjectNode objectNode,
            String searchName,
            NameValuePair[] nameValuePair)
    {
        if (nameValuePair != null)
        {
            if (nameValuePair.length > 0)
            {
                ObjectNode parameters = null;
                for (NameValuePair param : nameValuePair)
                {
                    if (parameters == null)
                    {
                        parameters = objectNode.putObject(searchName);
                    }
                    parameters.putObject(param.getName()).put("equalTo", param.getValue());
                }
            }
        }
    }

    private String[] getBodyAndMappingFilenames(
            String method,
            int httpStatus,
            URI uri,
            String contentType)
            throws URISyntaxException,
            IOException
    {
        URI    uriRequestPath = new URI(uri.getPath());
        String extension      = FileUtils.determineFileExtension(
                uriRequestPath.toString(),
                contentType);
        String sequencial     = StringUtils.fillLeftWithZeros(Integer.toString(getSequence()), 10);
        String timestamp      = StringUtils.getTimestamp();

        String filePath;
        if (recordingDirectory != null)
        {
            filePath = recordingDirectory;
        }
        else
        {
            filePath = FileUtils.getApplicationRunningPath();
        }
        if (!filePath.endsWith("/"))
        {
            filePath = filePath + "/";
        }
        String bodyFilename    = filePath
                + Constants.FILES_DIRECTORY + "/"
                + FileUtils.getUniqueFileName(
                        method.toUpperCase(Locale.ROOT) /* + "-body" */,
                        httpStatus,
                        uriRequestPath,
                        sequencial,
                        timestamp,
                        extension);

        String mappingFilename = filePath
                + Constants.MAPPINGS_DIRECTORY + "/"
                + FileUtils.getUniqueFileName(
                        method.toUpperCase(Locale.ROOT) /* + "-mapping" */,
                        httpStatus,
                        uriRequestPath,
                        sequencial,
                        timestamp,
                        "json");

        return new String[] {
                bodyFilename,
                mappingFilename
        };
    }

    private List<EqualsStub> readStubs()
            throws IOException,
            ProtocolException
    {
        List<EqualsStub> stubMatchers = new Vector<EqualsStub>();
        File[]           files        = FileUtils.listDirectoryFiles(recordingDirectory + "/" + Constants.MAPPINGS_DIRECTORY);
        Arrays.sort(files);
        for (File f : files)
        {
            if (f.isFile())
            {
                String     fileName = f.getName();
                EqualsStub entry    = readStubFile(fileName);
                stubMatchers.add(entry);
            }
        }
        System.out.println("Encontrado(s) " + stubMatchers.size() + " stub(s) no cache OFFLINE [" + recordingDirectory + "]");
        return stubMatchers;
    }

    public ClassicHttpResponse getResponse(ClassicHttpRequest incomingRequest)
            throws ProtocolException,
            UnsupportedEncodingException,
            IOException
    {
        synchronized (this.stubMatchersSlowSearch)
        {
            ClassicHttpResponse outgoingResponse = new BasicClassicHttpResponse(HttpStatus.SC_NOT_IMPLEMENTED, "Não implementado");
            try
            {
                reloadMappingsIfRequired();
                @SuppressWarnings("unused")
                boolean emCache = false;
                if (this.stubMatchersSlowSearch.size() == 0)
                {
                    outgoingResponse.setCode(HttpStatus.SC_NOT_FOUND);
                    outgoingResponse.setHeader("Content-Type", "text/plain; charset=UTF-8");
                    outgoingResponse.setEntity(new StringEntity(
                            getEmptyStubCacheResponseText(),
                            Constants.UTF8_CHARSET));
                }
                else
                {
                    Stub stubToMatch = new Stub()
                            .withRequest(incomingRequest);
                    removeOfflineIgnoredParameters(stubToMatch.getQueryParams());
                    removeOfflineIgnoredParameters(stubToMatch.getBodyParams());
                    removeOfflineIgnoredParameters(stubToMatch.getJsonBody());
                    removeOfflineIgnoredParameters(stubToMatch.getXmlBody());
                    removeOfflineNotMatchedHeaders(stubToMatch.getHeaders());

                    // Fast search
                    List<EqualsStub> fastSearch = this.stubMatchersFastSearch.get(getFastSearchKey(stubToMatch));
                    StubSearch       stubSearch = new StubSearch(stubToMatch);
                    stubSearch.search(fastSearch);
                    if (this.offlineCyclicResponses
                            && stubSearch.getMaxMatchPercentage() == Constants.ONE_HUNDRED_PERCENT
                            && stubSearch.getNearStubMatched() != null)
                    {
                        if (fastSearch != null)
                        {
                            // first item found jump to the last position in the
                            // fast search
                            fastSearch.remove(stubSearch.getNearStubMatched());
                            fastSearch.add(stubSearch.getNearStubMatched());
                        }

                        // first item found jump to the last position in the
                        // fast slow search
                        if (this.stubMatchersSlowSearch.containsKey(stubSearch.getNearStubMatched()))
                        {
                            ClassicHttpResponse response = this.stubMatchersSlowSearch.remove(stubSearch.getNearStubMatched());
                            this.stubMatchersSlowSearch.put(stubSearch.getNearStubMatched(), response);
                        }
                    }

                    // Slow search: Search by method and path not found a 100%
                    // match result
                    if (stubSearch.getNearStubMatched() == null
                            && stubSearch.getMaxMatchPercentage() != Constants.ONE_HUNDRED_PERCENT)
                    {
                        Set<EqualsStub> slowSearch = this.stubMatchersSlowSearch.keySet();
                        stubSearch.dirtySearch(slowSearch);
                        if (this.offlineCyclicResponses
                                && stubSearch.getMaxMatchPercentage() == Constants.ONE_HUNDRED_PERCENT
                                && stubSearch.getNearStubMatched() != null)
                        {
                            if (stubMatchersSlowSearch.containsKey(stubSearch.getNearStubMatched()))
                            {
                                // first item found jump to the last position in
                                // the slow search
                                ClassicHttpResponse response = this.stubMatchersSlowSearch.remove(stubSearch.getNearStubMatched());
                                stubMatchersSlowSearch.put(stubSearch.getNearStubMatched(), response);
                            }

                            List<EqualsStub> fastSearch1 = this.stubMatchersFastSearch.get(getFastSearchKey(stubSearch.getNearStubMatched()));
                            if (fastSearch1 != null
                                    && fastSearch1.contains(stubSearch.getNearStubMatched()))
                            {
                                // first item found jump to the last position in
                                // the fast search
                                fastSearch1.remove(stubSearch.getNearStubMatched());
                                fastSearch1.add(stubSearch.getNearStubMatched());
                            }
                        }
                    }

                    // __files search
                    if (stubSearch.getMaxMatchPercentage() < Constants.ONE_HUNDRED_PERCENT
                            && "GET".equalsIgnoreCase(incomingRequest.getMethod()))
                    {
                        try
                        {
                            String      fileName    = recordingDirectory
                                    + "/"
                                    + Constants.FILES_DIRECTORY
                                    + "/"
                                    + incomingRequest.getUri().getPath();
                            String      type        = Files.probeContentType(new File(fileName).toPath());
                            ContentType contentType = null;
                            if (type != null)
                            {
                                contentType = ContentType.create(type, Constants.UTF8);
                            }
                            outgoingResponse.setCode(HttpStatus.SC_SUCCESS);
                            if (contentType != null)
                            {
                                outgoingResponse.setHeader(HttpHeaders.CONTENT_TYPE, contentType);
                            }
                            outgoingResponse.setEntity(
                                    new ByteArrayEntity(FileUtils.readBinaryFile(fileName),
                                            contentType));
                            emCache = true;
                            return outgoingResponse;
                        }
                        catch (Throwable e)
                        {
                            // ignore
                        }
                    }

                    if (stubSearch.getMaxMatchPercentage() == 0)
                    {
                        outgoingResponse.setCode(HttpStatus.SC_NOT_FOUND);
                        outgoingResponse.setHeader("Content-Type", "text/plain; charset=UTF-8");
                        outgoingResponse.setEntity(new StringEntity(
                                getStubNotFoundResponseText(),
                                Constants.UTF8_CHARSET));
                    }
                    else if (stubSearch.getMaxMatchPercentage() < Constants.ONE_HUNDRED_PERCENT)
                    {
                        outgoingResponse.setCode(HttpStatus.SC_NOT_FOUND);
                        outgoingResponse.setHeader("Content-Type", "text/plain; charset=UTF-8");
                        outgoingResponse.setEntity(new StringEntity(
                                getNearStubFoundResponseText(
                                        stubSearch.getNearStubMatched(),
                                        stubToMatch,
                                        stubSearch.getMaxMatchPercentage()),
                                Constants.UTF8_CHARSET));
                    }
                    else if (stubSearch.getMaxMatchPercentage() >= Constants.ONE_HUNDRED_PERCENT)
                    {
                        outgoingResponse = this.stubMatchersSlowSearch.get(stubSearch.getNearStubMatched());
                        if (outgoingResponse == null)
                        {
                            String bodyFileName    = stubSearch.getNearStubMatched().getBodyFileName();
                            String mappingFileName = stubSearch.getNearStubMatched().getMappingFileName();
                            outgoingResponse = readStubResponse(mappingFileName, bodyFileName);
                            // put this response in memory?
                            // this.stubMatchersSlowSearch.put(stubSearch.getNearStubMatched(),
                            // outgoingResponse);
                        }
                        emCache = true;
                    }
                }
                // NetworkView.getInstance().updateRequestWithResponse(incomingRequest,
                // outgoingResponse, emCache);
                return outgoingResponse;
            }
            catch (Throwable e)
            {
                e.printStackTrace();
                outgoingResponse = new BasicClassicHttpResponse(HttpStatus.SC_INTERNAL_SERVER_ERROR, "Erro interno no servidor");
                outgoingResponse.setHeader("Content-Type", "text/plain; charset=UTF-8");
                outgoingResponse.setEntity(new StringEntity(
                        StringUtils.getFullErrorMessage(e),
                        Constants.UTF8_CHARSET));
                // NetworkView.getInstance().updateRequestWithResponse(incomingRequest,
                // outgoingResponse, null);
            }
            return outgoingResponse;
        }
    }

    private void reloadMappingsIfRequired()
            throws ProtocolException,
            IOException
    {

        if (isMappingsModified())
        {
            System.out.println("(Re)carregando o(s) stub(s) de '"
                    + FileUtils.getFullPath(recordingDirectory + "/" + Constants.MAPPINGS_DIRECTORY)
                    + "'...");
            loadMappings();
        }
    }

    private boolean isMappingsModified()
            throws IOException
    {
        File[] files                       = FileUtils.listDirectoryFiles(recordingDirectory + "/" + Constants.MAPPINGS_DIRECTORY);
        long[] newMappingsLastModifiedList = new long[files.length];
        for (int i = 0, size = files.length; i < size; i++)
        {
            if (files[i].isFile())
            {
                newMappingsLastModifiedList[i] = files[i].lastModified();
            }
        }
        if (mappingsLastModifiedList.length != newMappingsLastModifiedList.length)
        {
            mappingsLastModifiedList = newMappingsLastModifiedList;
            return true;
        }
        else
        {
            for (int i = 0, size = newMappingsLastModifiedList.length; i < size; i++)
            {
                if (mappingsLastModifiedList[i] != newMappingsLastModifiedList[i])
                {
                    mappingsLastModifiedList = newMappingsLastModifiedList;
                    return true;
                }
            }
        }
        return false;
    }

    @SuppressWarnings("unused")
    private String getEmptyStubCacheResponseHtml()
    {
        return "<html>"
                + "<head>"
                + "<meta charset=\"UTF-8\"></meta>"
                + "<title>Cache OFFLINE vazio</title>"
                + "</head>"
                + "<body>"
                + "Cache OFFLINE está vazio"
                + "</body>"
                + "</html>";
    }

    @SuppressWarnings("unused")
    private String getStubNotFoundResponseHtml()
    {
        return "<html>"
                + "<head>"
                + "<meta charset=\"UTF-8\"></meta>"
                + "<title>Solicitação não encontrada no cache OFFLINE</title>"
                + "</head>"
                + "<body>"
                + "Solicitação não encontrada no cache OFFLINE"
                + "</body>"
                + "</html>";
    }

    @SuppressWarnings("unused")
    private String getNearStubFoundResponseHtml(
            EqualsStub nearStubMatched,
            Stub stubToMatch,
            int maxMatchPercentage)
            throws IOException
    {
        StringBuffer sb = new StringBuffer();
        sb.append("	<html>");
        sb.append("	<head>");
        sb.append("		<meta charset=\"UTF-8\">");
        sb.append("		<title>Solicitação não encontrada no cache OFFLINE</title>");
        sb.append("	</head>");
        sb.append("	<body>");
        sb.append("	<style>");
        sb.append("		* {");
        sb.append("			font-size: 1em;");
        sb.append("		}");
        sb.append("		table, tr, td {");
        sb.append("			border-collapse:collapse;");
        sb.append("			border: 1px solid black;");
        sb.append("		}");
        sb.append("		td {");
        sb.append("			padding: 0.3em;");
        sb.append("		}");
        sb.append("		.red-font {");
        sb.append("			color:red;");
        sb.append("			font-weight: 900;");
        sb.append("		}");
        sb.append("		.bold-font {");
        sb.append("			font-weight: 900;");
        sb.append("		}");
        sb.append("		td:nth-child(2) > span:hover, td:nth-child(3) > span:hover {");
        sb.append("		    text-overflow: unset;");
        sb.append("		    overflow: auto;");
        sb.append("		}");
        sb.append("		td:nth-child(2) > span, td:nth-child(3) > span {");
        sb.append("		    width: 350px;");
        sb.append("		    max-width: 350px;");
        sb.append("		    min-width: 350px;");
        sb.append("		    overflow-x: hidden;");
        sb.append("		    overflow-y: hidden;");
        sb.append("		    text-overflow: ellipsis;");
        sb.append("		    white-space: nowrap;");
        sb.append("		    display: inline-block;");
        sb.append("		}");
        sb.append("	</style>");
        sb.append("	<table>");
        sb.append("		<tr>");
        sb.append("			<colgroup>");
        sb.append("				<col style=\"width: 10%\">");
        sb.append("				<col style=\"width: 45%\">");
        sb.append("				<col style=\"width: 45%\">");
        sb.append("			</colgroup>");
        sb.append("		</tr>");
        sb.append("		<tr>");
        sb.append("			<td colspan=\"3\">");
        sb.append("				<span>Solicitação não encontrada no cache <span class=\"red-font\">OFFLINE</span>.</br>");
        sb.append("				Entretanto, existe uma solicitação existente com semelhança de <span class=\"red-font bold-font\">" + maxMatchPercentage
                + ",00%</span> em um arquivo de cache:</span>");
        sb.append("			</td>");
        sb.append("		</tr>");
        sb.append("	</table>");

        sb.append("	</br>");
        sb.append("				<span><strong>Arquivo: " + FileUtils.getFullPath(recordingDirectory + "/" + nearStubMatched.getMappingFileName()) + "</strong></span>");
        sb.append("	</br>");
        sb.append("	</br>");

        sb.append("	<table>");
        sb.append("		<tr>");
        sb.append("			<td>");
        sb.append("			</td>");
        sb.append("			<td>");
        sb.append("				<span>Solicitação <span class=\"red-font\">REQUISITADA</span></br></span>");
        sb.append("			</td>");
        sb.append("			<td>");
        sb.append("				<span>Arquivo do cache <span class=\"red-font\">OFFLINE</span></br></span>");
        sb.append("			</td>");
        sb.append("		</tr>");

        buildHtml(sb, "method", stubToMatch.getMethod(), nearStubMatched.getMethod());
        buildHtml(sb, "path", stubToMatch.getPath(), nearStubMatched.getPath());
        buildHtml(sb, "queryParams", stubToMatch.getQueryParams(), nearStubMatched.getQueryParams());
        buildHtml(sb, "bodyParams", stubToMatch.getBodyParams(), nearStubMatched.getBodyParams());
        buildHtml(sb, "jsonBody", stubToMatch.getJsonBody(), nearStubMatched.getJsonBody());
        buildHtml(sb, "xmlBody", stubToMatch.getXmlBody(), nearStubMatched.getXmlBody());
        buildHtml(sb, "binaryBody", stubToMatch.getBinaryBody(), nearStubMatched.getBinaryBody());
        buildHtml(sb, "textBody", stubToMatch.getTextBody(), nearStubMatched.getTextBody());
        buildHtml(sb, "multiparts", stubToMatch.getMultiparts(), nearStubMatched.getMultiparts());
        buildHtml(sb, "headers", stubToMatch.getHeaders(), nearStubMatched.getHeaders());

        sb.append("	</table>");
        sb.append("	</body>");
        sb.append("</html>");
        return sb.toString();
    }

    private <T> void buildHtml(StringBuffer sb, String elementName, T value, List<IMatcher<T>> matchers)
    {
        if (matchers == null)
        {
            matchers = Collections.emptyList();
        }
        String name = elementName;
        if (matchers.size() == 0 && value != null)
        {
            sb.append("		<tr>");
            sb.append("			<td><span>" + name + "</span></td>");
            sb.append("			<td><span>" + (value == null ? "-" : value) + "</span></td>");
            sb.append("			<td><span>-</span></td>");
            sb.append("		</tr>");
        }
        else
        {
            Iterator<IMatcher<T>> it = matchers.iterator();
            while (it.hasNext())
            {
                IMatcher<T> m     = it.next();
                String      color = "";
                if (!m.matches(value))
                {
                    color = "red-font";
                }
                sb.append("		<tr>");
                sb.append("			<td><span>" + name + "</span></td>");
                sb.append("			<td><span>" + (value == null ? "-" : value) + "</span></td>");
                sb.append("			<td><span class=\"" + color + "\">" + matchers + "</span></td>");
                sb.append("		</tr>");
                name = "";
            }
        }
    }

    private <T> void buildHtml(StringBuffer sb, String elementName, List<T> values, List<IMatcher<List<T>>> matchers)
    {
        if (values == null)
        {
            values = Collections.emptyList();
        }
        if (matchers == null)
        {
            matchers = Collections.emptyList();
        }
        Iterator<T>                 it1  = values.iterator();
        Iterator<IMatcher<List<T>>> it2  = matchers.iterator();
        String                      name = elementName;
        while (it1.hasNext() && it2.hasNext())
        {
            T                 item1 = it1.next();
            IMatcher<List<T>> item2 = it2.next();
            String            color = "";
            if (!item2.matches(values))
            {
                color = "red-font";
            }
            sb.append("		<tr>");
            sb.append("			<td><span>" + name + "</span></td>");
            sb.append("			<td><span>" + (item1 == null ? "-" : item1) + "</span></td>");
            sb.append("			<td><span class=\"" + color + "\">" + (item2 == null ? "-" : item2) + "</span></td>");
            sb.append("		</tr>	");
            name = "";
        }
        while (it1.hasNext())
        {
            T item1 = it1.next();
            sb.append("		<tr>");
            sb.append("			<td><span>" + name + "</span></td>");
            sb.append("			<td><span>" + (item1 == null ? "-" : item1) + "</span></td>");
            sb.append("			<td><span>-</span></td>");
            sb.append("		</tr>	");
            name = "";
        }
        while (it2.hasNext())
        {
            IMatcher<List<T>> item2 = it2.next();
            String            color = "";
            if (!item2.matches(values))
            {
                color = "red-font";
            }
            sb.append("		<tr>");
            sb.append("			<td><span>" + name + "</span></td>");
            sb.append("			<td><span>-</span></td>");
            sb.append("			<td><span class=\"" + color + "\">" + (item2 == null ? "-" : item2) + "</span></td>");
            sb.append("		</tr>	");
            name = "";
        }
    }

    private String getEmptyStubCacheResponseText()
    {
        return "Cache OFFLINE está vazio";
    }

    private String getStubNotFoundResponseText()
    {
        return "Solicitação não encontrada no cache OFFLINE";
    }

    private String getNearStubFoundResponseText(
            EqualsStub nearStubMatched,
            Stub stubToMatch,
            int maxMatchPercentage)
            throws IOException
    {
        StringBuffer sb = new StringBuffer();
        sb.append("\n+---------------------------------------------------------------------------------------------------------------------------+");
        sb.append("\n| Solicitação não encontrada no cache OFFLINE. Entretanto, existe uma solicitação existente com semelhança de               |");
        sb.append("\n| " + String.format("%-122s", maxMatchPercentage + ",00% em um arquivo de cache:") + "|");
        sb.append("\n+---------------------------------------------------------------------------------------------------------------------------+");

        sb.append("\n\nArquivo: " + FileUtils.getFullPath(recordingDirectory + "/" + nearStubMatched.getMappingFileName()));
        sb.append("\n\n+-----------------+----------------------------------------------------+----------------------------------------------------+");
        blockText(sb, false, "Atributo", "Solicitação REQUISITADA", "Arquivo do cache OFFLINE", 15, 50, 50);

        buildText(sb, "method", stubToMatch.getMethod(), nearStubMatched.getMethod());
        buildText(sb, "path", stubToMatch.getPath(), nearStubMatched.getPath());
        buildText(sb, "queryParams", stubToMatch.getQueryParams(), nearStubMatched.getQueryParams());
        buildText(sb, "bodyParams", stubToMatch.getBodyParams(), nearStubMatched.getBodyParams());
        buildText(sb, "jsonBody", stubToMatch.getJsonBody(), nearStubMatched.getJsonBody());
        buildText(sb, "xmlBody", stubToMatch.getXmlBody(), nearStubMatched.getXmlBody());
        buildText(sb, "binaryBody", stubToMatch.getBinaryBody(), nearStubMatched.getBinaryBody());
        buildText(sb, "textBody", stubToMatch.getTextBody(), nearStubMatched.getTextBody());
        buildText(sb, "multiparts", stubToMatch.getMultiparts(), nearStubMatched.getMultiparts());
        buildText(sb, "headers", stubToMatch.getHeaders(), nearStubMatched.getHeaders());

        sb.append("\n+-----------------+----------------------------------------------------+----------------------------------------------------+");

        return sb.toString();
    }

    private <T> void buildText(StringBuffer sb, String elementName, T value, List<IMatcher<T>> matchers)
    {
        if (matchers == null)
        {
            matchers = Collections.emptyList();
        }
        String name = elementName;
        if (matchers.size() == 0)
        {
            // value without a restriction of a matcher:
            if ("".equals(name))
            {
                sb.append("\n+                 +----------------------------------------------------+----------------------------------------------------+");
            }
            else
            {
                sb.append("\n+-----------------+----------------------------------------------------+----------------------------------------------------+");
            }
            blockText(sb, false, name, value, "-", 15, 50, 50);
        }
        else
        {
            Iterator<IMatcher<T>> it = matchers.iterator();
            while (it.hasNext())
            {
                IMatcher<T> m    = it.next();
                boolean     mark = false;
                if (!m.matches(value))
                {
                    mark = true;
                }
                if ("".equals(name))
                {
                    sb.append("\n+                 +----------------------------------------------------+----------------------------------------------------+");
                }
                else
                {
                    sb.append("\n+-----------------+----------------------------------------------------+----------------------------------------------------+");
                }
                blockText(sb, mark, name, value, matchers, 15, 50, 50);
                name = "";
            }
        }
    }

    private <T> void buildText(StringBuffer sb, String elementName, List<T> values, List<IMatcher<List<T>>> matchers)
    {
        if (values == null)
        {
            values = Collections.emptyList();
        }
        if (matchers == null)
        {
            matchers = Collections.emptyList();
        }
        Iterator<T>                 it1  = values.iterator();
        Iterator<IMatcher<List<T>>> it2  = matchers.iterator();
        String                      name = elementName;
        while (it1.hasNext() && it2.hasNext())
        {
            T                 item1 = it1.next();
            IMatcher<List<T>> item2 = it2.next();
            boolean           mark  = false;
            if (!item2.matches(values))
            {
                mark = true;
            }
            if ("".equals(name))
            {
                sb.append("\n+                 +----------------------------------------------------+----------------------------------------------------+");
            }
            else
            {
                sb.append("\n+-----------------+----------------------------------------------------+----------------------------------------------------+");
            }
            blockText(sb, mark, name, item1, item2, 15, 50, 50);
            name = "";
        }
        // values without a restriction of a matcher:
        while (it1.hasNext())
        {
            T item1 = it1.next();
            if ("".equals(name))
            {
                sb.append("\n+                 +----------------------------------------------------+----------------------------------------------------+");
            }
            else
            {
                sb.append("\n+-----------------+----------------------------------------------------+----------------------------------------------------+");
            }
            blockText(sb, false, name, item1, "-", 15, 50, 50);
            name = "";
        }
        while (it2.hasNext())
        {
            IMatcher<List<T>> item2 = it2.next();
            boolean           mark  = false;
            if (!item2.matches(values))
            {
                mark = true;
            }
            if ("".equals(name))
            {
                sb.append("\n+                 +----------------------------------------------------+----------------------------------------------------+");
            }
            else
            {
                sb.append("\n+-----------------+----------------------------------------------------+----------------------------------------------------+");
            }
            blockText(sb, mark, name, "-", item2, 15, 50, 50);
            name = "";
        }
    }

    private <T> void blockText(
            StringBuffer sb,
            boolean mark,
            Object v1,
            Object v2,
            Object v3,
            int tam1,
            int tam2,
            int tam3)
    {
        String col1 = (v1 == null ? "-" : v1.toString());
        String col2 = (v2 == null ? "-" : v2.toString());
        String col3 = (v3 == null ? "-" : v3.toString());
        if (v3 != null && v3 instanceof List && ((List<?>) v3).size() == 1)
        {
            col3 = String.valueOf(((List<?>) v3).get(0));
        }
        if (mark)
        {
            // col2 = "DIFERENTE ==>> " + col2;
            col3 = "DIFERENTE ==>> " + col3;
        }

        int ini1 = 0;
        int ini2 = 0;
        int ini3 = 0;

        int fim1 = ini1 + tam1;
        int fim2 = ini2 + tam2;
        int fim3 = ini3 + tam3;

        while (ini1 < col1.length()
                || ini2 < col2.length()
                || ini3 < col3.length())
        {
            sb.append("\n| ");
            if (fim1 < col1.length())
            {
                sb.append(col1.substring(ini1, fim1));
            }
            else if (ini1 < col1.length())
            {
                sb.append(col1.substring(ini1));
                for (int i = 0, size = tam1 - col1.length() % tam1; i < size; i++)
                {
                    sb.append(' ');
                }
            }
            else
            {
                for (int i = 0; i < tam1; i++)
                {
                    sb.append(' ');
                }
            }
            sb.append(" | ");
            if (fim2 < col2.length())
            {
                sb.append(col2.substring(ini2, fim2));
            }
            else if (ini2 < col2.length())
            {
                sb.append(col2.substring(ini2));
                for (int i = 0, size = tam2 - col2.length() % tam2; i < size; i++)
                {
                    sb.append(' ');
                }
            }
            else
            {
                for (int i = 0; i < tam2; i++)
                {
                    sb.append(' ');
                }
            }
            sb.append(" | ");
            if (fim3 < col3.length())
            {
                sb.append(col3.substring(ini3, fim3));
            }
            else if (ini3 < col3.length())
            {
                sb.append(col3.substring(ini3));
                for (int i = 0, size = tam3 - col3.length() % tam3; i < size; i++)
                {
                    sb.append(' ');
                }
            }
            else
            {
                for (int i = 0; i < tam3; i++)
                {
                    sb.append(' ');
                }
            }
            sb.append(" |");
            ini1 += tam1;
            ini2 += tam2;
            ini3 += tam3;
            fim1 += tam1;
            fim2 += tam2;
            fim3 += tam3;
        }
    }

    private class StubSearch
    {
        private Stub       stubToMatch;
        private EqualsStub nearStubMatched;
        private int        maxMatchPercentage;

        public StubSearch(Stub stubToMatch)
        {
            super();
            this.stubToMatch = stubToMatch;
        }

        public EqualsStub getNearStubMatched()
        {
            return nearStubMatched;
        }

        public int getMaxMatchPercentage()
        {
            return maxMatchPercentage;
        }

        public void search(Collection<EqualsStub> search)
        {
            // Discards the last maxMatchPercentage and nearStubMatched result
            // and proceed the search
            resetSearch();
            dirtySearch(search);
        }

        private void resetSearch()
        {
            nearStubMatched    = null;
            maxMatchPercentage = 0;
        }

        public void dirtySearch(Collection<EqualsStub> search)
        {
            if (search == null)
            {
                return;
            }
            // Uses the last maxMatchPercentage and nearStubMatched result and
            // proceed the search
            for (EqualsStub stubMatcher : search)
            {
                int matchPercentage = stubMatcher.hit(stubToMatch);
                if (matchPercentage > maxMatchPercentage)
                {
                    maxMatchPercentage = matchPercentage;
                    nearStubMatched    = stubMatcher;
                    if (maxMatchPercentage == Constants.ONE_HUNDRED_PERCENT)
                    {
                        break;
                    }
                }
            }
        }
    }

    private void removeOfflineNotMatchedHeaders(List<NameValuePair> headers)
    {
        Iterator<NameValuePair> it = headers.iterator();
        while (it.hasNext())
        {
            NameValuePair header = it.next();
            if (offlineMatchHeaders == null
                    || !offlineMatchHeaders.contains(header.getName().toLowerCase(Locale.ROOT)))
            {
                it.remove();
            }
        }
    }

    private void removeOfflineIgnoredParameters(List<NameValuePair> q)
    {
        if (offlineIgnoreAllParameters)
        {
            q.clear();
            return;
        }
        if (offlineIgnoreParameters != null)
        {
            Iterator<NameValuePair> it = q.iterator();
            while (it.hasNext())
            {
                NameValuePair p = it.next();
                if (offlineIgnoreParameters.contains(p.getName()))
                {
                    it.remove();
                }
            }
        }
    }

    private void removeOfflineIgnoredParameters(JsonElement jsonElement)
    {
        List<JsonElement> elementsWithInternalGarbage = new Vector<JsonElement>();
        removeOfflineIgnoredParameters(jsonElement, elementsWithInternalGarbage);
        for (JsonElement element : elementsWithInternalGarbage)
        {
            element.removeGarbage();
        }
    }

    private void removeOfflineIgnoredParameters(JsonElement json, List<JsonElement> elementsWithInternalGarbage)
    {
        if (json instanceof JsonList)
        {
            removeOfflineIgnoredParameters((JsonList) json, elementsWithInternalGarbage);
        }
        else if (json instanceof JsonObject)
        {
            removeOfflineIgnoredParameters((JsonObject) json, elementsWithInternalGarbage);
        }
        else if (json instanceof JsonProperty)
        {
            removeOfflineIgnoredParameters((JsonProperty) json, elementsWithInternalGarbage);
        }
    }

    private void removeOfflineIgnoredParameters(JsonList json, List<JsonElement> elementsWithInternalGarbage)
    {
        for (JsonElement item : json.getList())
        {
            removeOfflineIgnoredParameters(item, elementsWithInternalGarbage);
        }
    }

    private void removeOfflineIgnoredParameters(JsonObject json, List<JsonElement> elementsWithInternalGarbage)
    {
        Iterator<JsonProperty> it          = json.getProperties().iterator();
        boolean                haveGarbage = false;
        while (it.hasNext())
        {
            JsonProperty property = it.next();
            if (removeOfflineIgnoredParameters(property, elementsWithInternalGarbage))
            {
                haveGarbage = true;
                it.remove();
            }
        }
        if (haveGarbage)
        {
            elementsWithInternalGarbage.add(json);
        }
    }

    private boolean removeOfflineIgnoredParameters(JsonProperty json, List<JsonElement> elementsWithInternalGarbage)
    {
        removeOfflineIgnoredParameters(json.getValue(), elementsWithInternalGarbage);
        if (offlineIgnoreAllParameters)
        {
            return true;
        }
        if (offlineIgnoreParameters != null)
        {
            return offlineIgnoreParameters.contains(json.getKey());
        }
        else
        {
            return false;
        }
    }

    private void removeOfflineIgnoredParameters(Xml xmlBody)
    {
        // TODO IMPLEMENTAR IGNORE PARAMETERS TO XML

    }

}
