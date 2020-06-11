package br.com.httpmock.matchers;

public class EqualsIgnoreCaseString
        implements IMatcher<String>
{
    private String pattern;

    public EqualsIgnoreCaseString(String pattern)
    {
        this.pattern = pattern;
    }

    public EqualsIgnoreCaseString withPattern(String pattern)
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
        if (equalsIgnoreCase(pattern, text))
        {
            return IMatcher.MAX_HIT_VALUE;
        }
        else
        {
            return 0;
        }
    }

    public static boolean equalsIgnoreCase(String text1, String text2)
    {
        if (text1 == null && text2 == null)
        {
            return true;
        }
        else if (text1 != null
                && text2 != null
                && text1.equalsIgnoreCase(text2))
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    @Override
    public String toString()
    {
        return pattern;
    }
}
