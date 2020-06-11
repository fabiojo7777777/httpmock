package br.com.httpmock.matchers;

import java.util.regex.Pattern;

public class MatchesRegex
        implements IMatcher<String>
{
    private Pattern pattern;

    public MatchesRegex(String regex)
    {
        this.pattern = Pattern.compile(regex);
    }

    public MatchesRegex withPattern(String regex)
    {
        this.pattern = Pattern.compile(regex);
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
        if (text == null && this.pattern == null)
        {
            return IMatcher.MAX_HIT_VALUE;
        }
        else if (text != null
                && this.pattern != null
                && this.pattern.matcher(text).matches())
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
        return String.valueOf(pattern);
    }

}
