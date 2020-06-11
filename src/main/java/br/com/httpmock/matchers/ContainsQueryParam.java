package br.com.httpmock.matchers;

import java.util.List;

import org.apache.hc.core5.http.NameValuePair;

public class ContainsQueryParam
        implements IMatcher<List<NameValuePair>>
{
    private String           name;
    private IMatcher<String> pattern;

    public ContainsQueryParam(String name, IMatcher<String> pattern)
    {
        this.name    = name;
        this.pattern = pattern;
    }

    public ContainsQueryParam(String name, String pattern)
    {
        this.name    = name;
        this.pattern = new EqualsLevenshteinDistance(pattern);
    }

    public ContainsQueryParam(NameValuePair pattern)
    {
        this.name    = pattern.getName();
        this.pattern = new EqualsLevenshteinDistance(pattern.getValue());
    }

    public ContainsQueryParam withPattern(NameValuePair pattern)
    {
        this.name    = pattern.getName();
        this.pattern = new EqualsLevenshteinDistance(pattern.getValue());
        return this;
    }

    @Override
    public boolean matches(List<NameValuePair> text)
    {
        return hit(text) == IMatcher.MAX_HIT_VALUE;
    }

    @Override
    public int hit(List<NameValuePair> queryParams)
    {
        if (pattern == null && queryParams == null)
        {
            return IMatcher.MAX_HIT_VALUE;
        }
        else if (pattern != null
                && queryParams != null)
        {
            return maxHit(queryParams, pattern);
        }
        else
        {
            return 0;
        }
    }

    private int maxHit(
            List<NameValuePair> queryParams,
            IMatcher<String> pattern)
    {
        int maxHit = 0;
        for (int i = 0, size = queryParams.size(); i < size; i++)
        {
            NameValuePair item = queryParams.get(i);
            if (item != null)
            {
                if (EqualsString.equals(name, item.getName()))
                {
                    int hit = pattern.hit(item.getValue());
                    if (hit > maxHit)
                    {
                        maxHit = hit;
                        if (maxHit == IMatcher.MAX_HIT_VALUE)
                        {
                            break;
                        }
                    }
                }
            }
        }
        return maxHit;
    }

    @Override
    public String toString()
    {
        return "" + name + "=" + pattern + "";
    }
}
