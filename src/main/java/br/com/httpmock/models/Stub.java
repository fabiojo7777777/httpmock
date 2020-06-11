package br.com.httpmock.models;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.http.ProtocolException;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.net.URLEncodedUtils;

import br.com.httpmock.builders.JsonElementBuilder;
import br.com.httpmock.utils.Constants;
import br.com.httpmock.utils.HttpUtils;

public class Stub
{
    private String              method;
    private String              path;
    private List<NameValuePair> queryParams = new Vector<NameValuePair>();
    private List<NameValuePair> bodyParams  = new Vector<NameValuePair>();
    private JsonElement         jsonBody;
    private Xml                 xmlBody;
    private byte[]              binaryBody;
    private String              textBody;
    private List<NameValuePair> headers     = new Vector<NameValuePair>();
    private List<Multipart>     multiparts  = new Vector<Multipart>();

    public Stub withMethod(String method)
    {
        this.method = method;
        return this;
    }

    public Stub withPath(String path)
    {
        this.path = path;
        return this;
    }

    public Stub withQueryParam(String key, String value)
    {
        this.queryParams.add(new QueryParam(key, value));
        removeDuplicateNames(this.queryParams);
        return this;
    }

    public Stub withQueryParam(NameValuePair queryParam)
    {
        this.queryParams.add(queryParam);
        removeDuplicateNames(this.queryParams);
        return this;
    }

    public Stub withQueryParams(String query)
    {
        if (query == null)
        {
            return this;
        }
        String queryParameters = query;
        if (queryParameters.indexOf('?') != -1)
        {
            queryParameters = URI.create(query).getQuery();
        }
        List<NameValuePair> queryParams = URLEncodedUtils.parse(queryParameters, Constants.UTF8_CHARSET);
        return withQueryParams(queryParams);
    }

    public Stub withQueryParams(List<NameValuePair> queryParams)
    {
        this.queryParams.addAll(queryParams);
        removeDuplicateNames(this.queryParams);
        return this;
    }

    private void removeDuplicateNames(List<NameValuePair> nameValuePair)
    {
        List<String>            names = new ArrayList<String>();
        Iterator<NameValuePair> it    = nameValuePair.iterator();
        while (it.hasNext())
        {
            NameValuePair q = it.next();
            if (names.contains(q.getName()))
            {
                it.remove();
            }
            names.add(q.getName());
        }
    }

    public Stub withBodyParam(String key, String value)
    {
        this.bodyParams.add(new QueryParam(key, value));
        removeDuplicateNames(this.bodyParams);
        return this;
    }

    public Stub withBodyParam(NameValuePair bodyParam)
    {
        this.bodyParams.add(bodyParam);
        removeDuplicateNames(this.bodyParams);
        return this;
    }

    private Stub withBodyParams(String query)
    {
        if (query == null)
        {
            return this;
        }
        String queryParameters = query;
        if (queryParameters.indexOf('?') != -1)
        {
            queryParameters = URI.create(query).getQuery();
        }
        List<NameValuePair> bodyParams = URLEncodedUtils.parse(queryParameters, Constants.UTF8_CHARSET);
        return withBodyParams(bodyParams);
    }

    public Stub withBodyParams(List<NameValuePair> bodyParams)
    {
        this.bodyParams.addAll(bodyParams);
        removeDuplicateNames(this.bodyParams);
        return this;
    }

    public Stub withJsonBody(String json)
    {
        this.jsonBody = JsonElementBuilder.Builder().build(json);
        return this;
    }

    public Stub withJsonBody(JsonElement jsonBody)
    {
        this.jsonBody = jsonBody;
        return this;
    }

    public Stub withXmlBody(String xml)
    {
        this.xmlBody = new Xml(xml);
        return this;
    }

    public Stub withXmlBody(Xml xml)
    {
        this.xmlBody = xml;
        return this;
    }

    public Stub withBinaryBody(byte[] binaryBody)
    {
        this.binaryBody = binaryBody;
        return this;
    }

    public Stub withTextBody(String textBody)
    {
        this.textBody = textBody;
        return this;
    }

    public Stub withBody(ClassicHttpRequest request)
            throws ProtocolException,
            UnsupportedEncodingException,
            IOException
    {
        if (request.getEntity() != null)
        {
            withBody(HttpUtils.getContentType(request), new String(EntityUtils.toByteArray(request.getEntity()), Constants.UTF8));
        }
        return this;
    }

    public Stub withHeader(String key, String value)
    {
        this.headers.add(new Header(key, value));
        return this;
    }

    public Stub withHeader(NameValuePair header)
    {
        this.headers.add(header);
        return this;
    }

    public Stub withHeaders(List<NameValuePair> headers)
    {
        this.headers.addAll(headers);
        return this;
    }

    public Stub withHeaders(NameValuePair[] headers)
    {
        for (NameValuePair header : headers)
        {
            withHeader(header);
        }
        return this;
    }

    public Stub withMultipart(Multipart multipart)
    {
        this.multiparts.add(multipart);
        return this;
    }

    public Stub withMultiparts(List<Multipart> multiparts)
    {
        this.multiparts.addAll(multiparts);
        return this;
    }

    public Stub withRequest(ClassicHttpRequest request)
            throws ProtocolException,
            UnsupportedEncodingException,
            IOException
    {
        withPath(URI.create(request.getPath()).getPath());
        withMethod(request.getMethod());
        withQueryParams(URI.create(request.getPath()).getQuery());
        withHeaders(request.getHeaders());
        withBody(request);
        return this;
    }

    private void withBody(String contentType, String body)
    {
        // TODO MULTIPART/FORM-DATA: INCLUIR SUPORTE AO FORMATO
        if (contentType == null)
        {
            this.textBody = body;
        }
        else if (contentType.contains("form-urlencoded"))
        {
            withBodyParams(body);
        }
        else if (contentType.contains("json"))
        {
            withJsonBody(JsonElementBuilder
                    .Builder()
                    .build(body));

        }
        else if (contentType.contains("xml"))
        {
            withXmlBody(body);
        }
        else
        {
            this.textBody = body;
        }
    }

    public String getMethod()
    {
        return method;
    }

    public String getPath()
    {
        return path;
    }

    public List<NameValuePair> getQueryParams()
    {
        return queryParams;
    }

    public List<NameValuePair> getBodyParams()
    {
        return bodyParams;
    }

    public JsonElement getJsonBody()
    {
        return jsonBody;
    }

    public Xml getXmlBody()
    {
        return xmlBody;
    }

    public byte[] getBinaryBody()
    {
        return binaryBody;
    }

    public String getTextBody()
    {
        return textBody;
    }

    public List<Multipart> getMultiparts()
    {
        return multiparts;
    }

    public List<NameValuePair> getHeaders()
    {
        return headers;
    }

    @Override
    public int hashCode()
    {
        final int prime  = 31;
        int       result = 1;
        result = prime * result + ((binaryBody == null) ? 0 : binaryBody.hashCode());
        result = prime * result + ((bodyParams == null) ? 0 : bodyParams.hashCode());
        result = prime * result + ((headers == null) ? 0 : headers.hashCode());
        result = prime * result + ((jsonBody == null) ? 0 : jsonBody.hashCode());
        result = prime * result + ((method == null) ? 0 : method.hashCode());
        result = prime * result + ((path == null) ? 0 : path.hashCode());
        result = prime * result + ((queryParams == null) ? 0 : queryParams.hashCode());
        result = prime * result + ((textBody == null) ? 0 : textBody.hashCode());
        result = prime * result + ((multiparts == null) ? 0 : textBody.hashCode());
        result = prime * result + ((xmlBody == null) ? 0 : xmlBody.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Stub other = (Stub) obj;
        if (binaryBody == null)
        {
            if (other.binaryBody != null)
                return false;
        }
        else if (!binaryBody.equals(other.binaryBody))
            return false;
        if (bodyParams == null)
        {
            if (other.bodyParams != null)
                return false;
        }
        else if (!bodyParams.equals(other.bodyParams))
            return false;
        if (headers == null)
        {
            if (other.headers != null)
                return false;
        }
        else if (!headers.equals(other.headers))
            return false;
        if (jsonBody == null)
        {
            if (other.jsonBody != null)
                return false;
        }
        else if (!jsonBody.equals(other.jsonBody))
            return false;
        if (method == null)
        {
            if (other.method != null)
                return false;
        }
        else if (!method.equals(other.method))
            return false;
        if (path == null)
        {
            if (other.path != null)
                return false;
        }
        else if (!path.equals(other.path))
            return false;
        if (queryParams == null)
        {
            if (other.queryParams != null)
                return false;
        }
        else if (!queryParams.equals(other.queryParams))
            return false;
        if (textBody == null)
        {
            if (other.textBody != null)
                return false;
        }
        else if (!textBody.equals(other.textBody))
            return false;
        if (xmlBody == null)
        {
            if (other.xmlBody != null)
                return false;
        }
        else if (!xmlBody.equals(other.xmlBody))
            return false;
        if (multiparts == null)
        {
            if (other.multiparts != null)
                return false;
        }
        else if (!multiparts.equals(other.multiparts))
            return false;
        return true;
    }

    @Override
    public String toString()
    {
        return "[method=[" + method + "]"
                + ", path=[" + path + "]"
                + (queryParams.size() != 0 ? ", queryParams=" + queryParams : "")
                + (bodyParams.size() != 0 ? ", bodyParams=" + bodyParams : "")
                + (jsonBody != null ? ", jsonBody=[" + jsonBody + "]" : "")
                + (xmlBody != null ? ", xmlBody=[" + xmlBody + "]" : "")
                + (binaryBody != null ? ", binaryBody=" + Arrays.toString(binaryBody) : "")
                + (textBody != null ? ", textBody=[" + textBody + "]" : "")
                + (multiparts.size() != 0 ? ", multiparts=" + multiparts : "")
                + (headers.size() != 0 ? ", headers=" + headers : "")
                + "]";
    }

}
