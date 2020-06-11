package br.com.httpmock.matchers;

import java.util.List;
import java.util.Vector;

import org.apache.hc.core5.http.NameValuePair;

import br.com.httpmock.builders.JsonElementBuilder;
import br.com.httpmock.models.JsonElement;
import br.com.httpmock.models.Multipart;
import br.com.httpmock.models.Stub;
import br.com.httpmock.models.Xml;

public class EqualsStub
        implements IMatcher<Stub>
{
    private String                              id;
    private String                              uuid;
    private String                              bodyFileName;
    private String                              mappingFileName;

    // default hit weight on stubs
    private int                                 methodHitWeight = 30;
    private int                                 pathHitWeight   = 25;
    private int                                 queryHitWeight  = 15;
    private int                                 bodyHitWeight   = 15;
    private int                                 headerHitWeight = 15;

    private List<IMatcher<String>>              method          = new Vector<IMatcher<String>>();
    private List<IMatcher<String>>              path            = new Vector<IMatcher<String>>();
    private List<IMatcher<List<NameValuePair>>> queryParams     = new Vector<IMatcher<List<NameValuePair>>>();
    private List<IMatcher<List<NameValuePair>>> bodyParams      = new Vector<IMatcher<List<NameValuePair>>>();
    private List<IMatcher<JsonElement>>         jsonBody        = new Vector<IMatcher<JsonElement>>();
    private List<IMatcher<Xml>>                 xmlBody         = new Vector<IMatcher<Xml>>();
    private List<IMatcher<byte[]>>              binaryBody      = new Vector<IMatcher<byte[]>>();
    private List<IMatcher<String>>              textBody        = new Vector<IMatcher<String>>();
    private List<IMatcher<List<Multipart>>>     multiparts      = new Vector<IMatcher<List<Multipart>>>();
    private List<IMatcher<List<NameValuePair>>> headers         = new Vector<IMatcher<List<NameValuePair>>>();

    public EqualsStub()
    {
        super();
    }

    public EqualsStub withMethodHitWeight(int methodHitWeight)
    {
        this.methodHitWeight = methodHitWeight;
        return this;
    }

    public EqualsStub withPathHitWeight(int pathHitWeight)
    {
        this.pathHitWeight = pathHitWeight;
        return this;
    }

    public EqualsStub withQueryHitWeight(int queryHitWeight)
    {
        this.queryHitWeight = queryHitWeight;
        return this;
    }

    public EqualsStub withBodyHitWeight(int bodyHitWeight)
    {
        this.bodyHitWeight = bodyHitWeight;
        return this;
    }

    public EqualsStub withHeaderHitWeight(int headerHitWeight)
    {
        this.headerHitWeight = headerHitWeight;
        return this;
    }

    public void withId(String id)
    {
        this.id = id;
    }

    public void withUuid(String uuid)
    {
        this.uuid = uuid;
    }

    public void withBodyFileName(String bodyFileName)
    {
        this.bodyFileName = bodyFileName;
    }

    public void withMappingFileName(String mappingFileName)
    {
        this.mappingFileName = mappingFileName;
    }

    public EqualsStub withPattern(Stub stub)
    {
        return withMethod(stub.getMethod())
                .withPath(stub.getPath())
                .withQueryParam(stub.getQueryParams())
                .withBodyParam(stub.getBodyParams())
                .withJsonBody(stub.getJsonBody())
                .withXmlBody(stub.getXmlBody())
                .withBinaryBody(stub.getBinaryBody())
                .withTextBody(stub.getTextBody())
                .withHeader(stub.getHeaders())
                .withMultipart(stub.getMultiparts());
    }

    public String getId()
    {
        return id;
    }

    public String getUuid()
    {
        return uuid;
    }

    public String getBodyFileName()
    {
        return bodyFileName;
    }

    public String getMappingFileName()
    {
        return mappingFileName;
    }

    public List<IMatcher<String>> getMethod()
    {
        return method;
    }

    public List<IMatcher<String>> getPath()
    {
        return path;
    }

    public List<IMatcher<List<NameValuePair>>> getQueryParams()
    {
        return queryParams;
    }

    public List<IMatcher<List<NameValuePair>>> getBodyParams()
    {
        return bodyParams;
    }

    public List<IMatcher<JsonElement>> getJsonBody()
    {
        return jsonBody;
    }

    public List<IMatcher<Xml>> getXmlBody()
    {
        return xmlBody;
    }

    public List<IMatcher<byte[]>> getBinaryBody()
    {
        return binaryBody;
    }

    public List<IMatcher<String>> getTextBody()
    {
        return textBody;
    }

    public List<IMatcher<List<Multipart>>> getMultiparts()
    {
        return multiparts;
    }

    public List<IMatcher<List<NameValuePair>>> getHeaders()
    {
        return headers;
    }

    public EqualsStub withMethod(String method)
    {
        this.method.add(new EqualsString(method));
        return this;
    }

    public EqualsStub withMethod(IMatcher<String> method)
    {
        this.method.add(method);
        return this;
    }

    public EqualsStub withMethods(List<IMatcher<String>> methods)
    {
        for (IMatcher<String> method : methods)
        {
            withMethod(method);
        }
        return this;
    }

    public EqualsStub withPath(String path)
    {
        this.path.add(new EqualsLevenshteinDistance(path));
        return this;
    }

    public EqualsStub withPath(IMatcher<String> path)
    {
        this.path.add(path);
        return this;
    }

    public EqualsStub withPaths(List<IMatcher<String>> paths)
    {
        for (IMatcher<String> path : paths)
        {
            withPath(path);
        }
        return this;
    }

    public EqualsStub withQueryParam(String key, String value)
    {
        this.queryParams.add(new ContainsQueryParam(key, value));
        return this;
    }

    public EqualsStub withQueryParam(NameValuePair queryParam)
    {
        this.queryParams.add(new ContainsQueryParam(queryParam.getName(), queryParam.getValue()));
        return this;
    }

    public EqualsStub withQueryParam(String key, IMatcher<String> pattern)
    {
        this.queryParams.add(new ContainsQueryParam(key, pattern));
        return this;
    }

    public EqualsStub withQueryParam(IMatcher<List<NameValuePair>> queryParam)
    {
        this.queryParams.add(queryParam);
        return this;
    }

    public EqualsStub withQueryParam(List<NameValuePair> queryParams)
    {
        for (NameValuePair queryParam : queryParams)
        {
            withQueryParam(queryParam);
        }
        return this;
    }

    public EqualsStub withQueryParams(List<IMatcher<List<NameValuePair>>> queryParams)
    {
        for (IMatcher<List<NameValuePair>> queryParam : queryParams)
        {
            withQueryParam(queryParam);
        }
        return this;
    }

    public EqualsStub withBodyParam(String key, String value)
    {
        this.bodyParams.add(new ContainsQueryParam(key, value));
        return this;
    }

    public EqualsStub withBodyParam(NameValuePair bodyParam)
    {
        this.bodyParams.add(new ContainsQueryParam(bodyParam.getName(), bodyParam.getValue()));
        return this;
    }

    public EqualsStub withBodyParam(String key, IMatcher<String> pattern)
    {
        this.bodyParams.add(new ContainsQueryParam(key, pattern));
        return this;
    }

    public EqualsStub withBodyParam(IMatcher<List<NameValuePair>> bodyParam)
    {
        this.bodyParams.add(bodyParam);
        return this;
    }

    public EqualsStub withBodyParam(List<NameValuePair> bodyParams)
    {
        for (NameValuePair bodyParam : bodyParams)
        {
            withBodyParam(bodyParam);
        }
        return this;
    }

    public EqualsStub withBodyParams(List<IMatcher<List<NameValuePair>>> bodyParams)
    {
        for (IMatcher<List<NameValuePair>> bodyParam : bodyParams)
        {
            withBodyParam(bodyParam);
        }
        return this;
    }

    public EqualsStub withJsonBody(JsonElement json)
    {
        EqualsJsonElement pattern = new EqualsJsonElement()
                .withPattern(json);
        this.jsonBody.add(pattern);
        return this;
    }

    public EqualsStub withJsonBody(Object json)
    {
        EqualsJsonElement pattern = new EqualsJsonElement()
                .withPattern(
                        JsonElementBuilder
                                .Builder()
                                .build(json));
        this.jsonBody.add(pattern);
        return this;
    }

    public EqualsStub withJsonBody(
            JsonElement json,
            boolean ignoreArrayOrder,
            boolean ignoreExtraProperties)
    {
        EqualsJsonElement pattern = new EqualsJsonElement()
                .withPattern(json)
                .withIgnoreArrayOrder(ignoreArrayOrder)
                .withIgnoreExtraProperties(ignoreExtraProperties);
        this.jsonBody.add(pattern);
        return this;
    }

    public EqualsStub withJsonBody(
            Object json,
            boolean ignoreArrayOrder,
            boolean ignoreExtraProperties)
    {
        EqualsJsonElement pattern = new EqualsJsonElement()
                .withPattern(
                        JsonElementBuilder
                                .Builder()
                                .build(json))
                .withIgnoreArrayOrder(ignoreArrayOrder)
                .withIgnoreExtraProperties(ignoreExtraProperties);
        this.jsonBody.add(pattern);
        return this;
    }

    public EqualsStub withJsonBody(IMatcher<JsonElement> json)
    {
        this.jsonBody.add(json);
        return this;
    }

    public EqualsStub withJsonBody(List<IMatcher<JsonElement>> jsons)
    {
        for (IMatcher<JsonElement> json : jsons)
        {
            withJsonBody(json);
        }
        return this;
    }

    public EqualsStub withXmlBody(String xml)
    {
        this.xmlBody.add(new EqualsXml(xml));
        return this;
    }

    public EqualsStub withXmlBody(Xml xml)
    {
        this.xmlBody.add(new EqualsXml(xml));
        return this;
    }

    public EqualsStub withXmlBody(IMatcher<Xml> xml)
    {
        this.xmlBody.add(xml);
        return this;
    }

    public EqualsStub withXmlBody(List<Xml> xmls)
    {
        for (Xml xml : xmls)
        {
            withXmlBody(xml);
        }
        return this;
    }

    public EqualsStub withXmlBodies(List<IMatcher<Xml>> xmls)
    {
        for (IMatcher<Xml> xml : xmls)
        {
            withXmlBody(xml);
        }
        return this;
    }

    public EqualsStub withBinaryBody(byte[] binary)
    {
        this.binaryBody.add(new EqualsBinary(binary));
        return this;
    }

    public EqualsStub withBinaryBody(IMatcher<byte[]> binary)
    {
        this.binaryBody.add(binary);
        return this;
    }

    public EqualsStub withBinaryBodies(List<IMatcher<byte[]>> binaries)
    {
        for (IMatcher<byte[]> binary : binaries)
        {
            withBinaryBody(binary);
        }
        return this;
    }

    public EqualsStub withTextBody(String text)
    {
        this.textBody.add(new EqualsString(text));
        return this;
    }

    public EqualsStub withTextBody(IMatcher<String> text)
    {
        this.textBody.add(text);
        return this;
    }

    public EqualsStub withTextBody(List<String> texts)
    {
        for (String text : texts)
        {
            withTextBody(text);
        }
        return this;
    }

    public EqualsStub withTextBodies(List<IMatcher<String>> texts)
    {
        for (IMatcher<String> text : texts)
        {
            withTextBody(text);
        }
        return this;
    }

    public EqualsStub withHeader(String key, String value)
    {
        this.headers.add(new ContainsHeader(key, value));
        return this;
    }

    public EqualsStub withHeader(NameValuePair header)
    {
        this.headers.add(new ContainsHeader(header));
        return this;
    }

    public EqualsStub withHeader(String key, IMatcher<String> pattern)
    {
        this.headers.add(new ContainsHeader(key, pattern));
        return this;
    }

    public EqualsStub withHeader(IMatcher<List<NameValuePair>> header)
    {
        this.headers.add(header);
        return this;
    }

    public EqualsStub withHeader(List<NameValuePair> headers)
    {
        for (NameValuePair header : headers)
        {
            withHeader(header);
        }
        return this;
    }

    public EqualsStub withHeaders(List<IMatcher<List<NameValuePair>>> headers)
    {
        for (IMatcher<List<NameValuePair>> header : headers)
        {
            withHeader(header);
        }
        return this;
    }

    public EqualsStub withMultipart(Multipart multipart)
    {
        this.multiparts.add(new ContainsMultipart(multipart));
        return this;
    }

    public EqualsStub withMultipart(IMatcher<List<Multipart>> multipart)
    {
        this.multiparts.add(multipart);
        return this;
    }

    public EqualsStub withMultipart(List<Multipart> multiparts)
    {
        for (Multipart multipart : multiparts)
        {
            withMultipart(multipart);
        }
        return this;
    }

    public EqualsStub withMultiparts(List<IMatcher<List<Multipart>>> multiparts)
    {
        for (IMatcher<List<Multipart>> multipart : multiparts)
        {
            withMultipart(multipart);
        }
        return this;
    }

    @Override
    public boolean matches(Stub stub)
    {
        return hit(stub) == IMatcher.MAX_HIT_VALUE;
    }

    @Override
    public int hit(Stub stub)
    {
        int   hitSum            = 0;
        int   maxHitSum         = 0;

        int[] methodHitInfo     = hitValue(
                method,
                stub.getMethod());

        int[] pathHitInfo       = hitValue(
                path,
                stub.getPath());

        int[] queryParamHitInfo = hitValue(
                queryParams,
                stub.getQueryParams());

        int[] bodyParamHitInfo  = hitValue(
                bodyParams,
                stub.getBodyParams());

        int[] jsonBodyHitInfo   = hitValue(
                jsonBody,
                stub.getJsonBody());

        int[] xmlBodyHitInfo    = hitValue(
                xmlBody,
                stub.getXmlBody());

        int[] binaryBodyHitInfo = hitValue(
                binaryBody,
                stub.getBinaryBody());

        int[] textBodyHitInfo   = hitValue(
                textBody,
                stub.getTextBody());

        int[] headerHitInfo     = hitValue(
                headers,
                stub.getHeaders());

        hitSum    = hitSum
                + methodHitInfo[0] * this.methodHitWeight
                + pathHitInfo[0] * this.pathHitWeight
                + queryParamHitInfo[0] * this.queryHitWeight
                + bodyParamHitInfo[0] * this.queryHitWeight
                + jsonBodyHitInfo[0] * this.bodyHitWeight
                + xmlBodyHitInfo[0] * this.bodyHitWeight
                + binaryBodyHitInfo[0] * this.bodyHitWeight
                + textBodyHitInfo[0] * this.bodyHitWeight
                + headerHitInfo[0] * this.headerHitWeight;

        maxHitSum = maxHitSum
                + methodHitInfo[1] * this.methodHitWeight
                + pathHitInfo[1] * this.pathHitWeight
                + queryParamHitInfo[1] * this.queryHitWeight
                + bodyParamHitInfo[1] * this.queryHitWeight
                + jsonBodyHitInfo[1] * this.bodyHitWeight
                + xmlBodyHitInfo[1] * this.bodyHitWeight
                + binaryBodyHitInfo[1] * this.bodyHitWeight
                + textBodyHitInfo[1] * this.bodyHitWeight
                + headerHitInfo[1] * this.headerHitWeight;

        if (hitSum == maxHitSum)
        {
            return IMatcher.MAX_HIT_VALUE;
        }
        else
        {
            return (int) ((float) hitSum
                    / (float) maxHitSum
                    * (float) IMatcher.MAX_HIT_VALUE);
        }
    }

    private <T> int[] hitValue(List<IMatcher<T>> matchers, T value)
    {
        int hitSum    = 0;
        int maxHitSum = 0;
        if (matchers != null)
        {
            for (IMatcher<T> matcher : matchers)
            {
                if (matcher != null)
                {
                    hitSum    = hitSum + matcher.hit(value);
                    maxHitSum = maxHitSum + IMatcher.MAX_HIT_VALUE;
                }
            }
        }
        return new int[] {
                hitSum,
                maxHitSum
        };
    }

    private <T> int[] hitValue(List<IMatcher<List<T>>> matchers, List<T> values)
    {
        int hitSum    = 0;
        int maxHitSum = 0;
        if (matchers != null)
        {
            for (IMatcher<List<T>> matcher : matchers)
            {
                if (matcher != null)
                {
                    hitSum    = hitSum + matcher.hit(values);
                    maxHitSum = maxHitSum + IMatcher.MAX_HIT_VALUE;
                }
            }
        }
        int matchersSize = (matchers == null ? 0 : matchers.size());
        int valuesSize   = (values == null ? 0 : values.size());

        if (valuesSize == matchersSize)
        {
            hitSum = hitSum + IMatcher.MAX_HIT_VALUE;
        }
        maxHitSum = maxHitSum + IMatcher.MAX_HIT_VALUE;
        return new int[] {
                hitSum,
                maxHitSum
        };
    }

    @Override
    public String toString()
    {
        return "[" +
                (method.size() != 0 ? "method=" + method : "") +
                (path.size() != 0 ? ", path=" + path : "") +
                (queryParams.size() != 0 ? ", queryParams=" + queryParams : "") +
                (bodyParams.size() != 0 ? ", bodyParams=" + bodyParams : "") +
                (jsonBody.size() != 0 ? ", jsonBody=" + jsonBody : "") +
                (xmlBody.size() != 0 ? ", xmlBody=" + xmlBody : "") +
                (binaryBody.size() != 0 ? ", binaryBody=" + binaryBody : "") +
                (textBody.size() != 0 ? ", textBody=" + textBody : "") +
                (multiparts.size() != 0 ? ", multiparts=" + multiparts : "") +
                (headers.size() != 0 ? ", headers=" + headers : "") +
                (bodyFileName != null ? ", bodyFileName=\"" + bodyFileName : "\"") +
                ", mappingFileName=\"" + mappingFileName + "\"" +
                "]";
    }

}
