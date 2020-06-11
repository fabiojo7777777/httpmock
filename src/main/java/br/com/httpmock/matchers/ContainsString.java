package br.com.httpmock.matchers;

public class ContainsString
        implements IMatcher<String>
{
    private String pattern;

    public ContainsString(String pattern)
    {
        this.pattern = pattern;
    }

    public ContainsString withPattern(String pattern)
    {
        this.pattern = pattern;
        return this;
    }

    @Override
    public boolean matches(String text)
    {
        return hit(text) == IMatcher.MAX_HIT_VALUE;
    }

    @Override
    public int hit(String text)
    {
        if (pattern == null
                && text == null)
        {
            return IMatcher.MAX_HIT_VALUE;
        }
        else if (pattern != null
                && text != null
                && text.contains(pattern))
        {
            return IMatcher.MAX_HIT_VALUE;
        }
        else
        {
            return 0;
        }
    }

    @Override
    public String toString()
    {
        return pattern;
    }
}
