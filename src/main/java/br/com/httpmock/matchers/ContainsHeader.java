package br.com.httpmock.matchers;

import java.util.List;

import org.apache.hc.core5.http.NameValuePair;

public class ContainsHeader
        implements IMatcher<List<NameValuePair>>
{
    private String           name;
    private IMatcher<String> pattern;

    public ContainsHeader(String name, String pattern)
    {
        this.name    = name;
        this.pattern = new EqualsLevenshteinDistance(pattern);
    }

    public ContainsHeader(String key, IMatcher<String> pattern)
    {
        this.name    = key;
        this.pattern = pattern;
    }

    public ContainsHeader(NameValuePair pattern)
    {
        this.name    = pattern.getName();
        this.pattern = new EqualsLevenshteinDistance(pattern.getValue());
    }

    public ContainsHeader withPattern(NameValuePair pattern)
    {
        this.name    = pattern.getName();
        this.pattern = new EqualsLevenshteinDistance(pattern.getValue());
        return this;
    }

    @Override
    public boolean matches(List<NameValuePair> header)
    {
        return hit(header) == IMatcher.MAX_HIT_VALUE;
    }

    @Override
    public int hit(List<NameValuePair> headers)
    {
        if (pattern == null && headers == null)
        {
            return IMatcher.MAX_HIT_VALUE;
        }
        else if (pattern != null
                && headers != null)
        {
            return maxHit(headers, pattern);
        }
        else
        {
            return 0;
        }
    }

    private int maxHit(
            List<NameValuePair> headers,
            IMatcher<String> pattern)
    {
        int maxHit = 0;
        for (int i = 0, size = headers.size(); i < size; i++)
        {
            NameValuePair item = headers.get(i);
            if (item != null)
            {
                if (EqualsIgnoreCaseString.equalsIgnoreCase(name, item.getName()))
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
        return "" + name + ": " + pattern + "";
    }

}
