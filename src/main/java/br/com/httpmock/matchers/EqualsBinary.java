package br.com.httpmock.matchers;

import java.util.Arrays;

public class EqualsBinary
        implements IMatcher<byte[]>
{
    private byte[] pattern;

    public EqualsBinary(byte[] pattern)
    {
        this.pattern = pattern;
    }

    public EqualsBinary withPattern(byte[] pattern)
    {
        this.pattern = pattern;
        return this;
    }

    @Override
    public boolean matches(byte[] bytes)
    {
        return hit(bytes) == IMatcher.MAX_HIT_VALUE;
    }

    @Override
    public int hit(byte[] bytes)
    {
        if (pattern == null
                && bytes == null)
        {
            return IMatcher.MAX_HIT_VALUE;
        }
        else if (pattern != null
                && bytes != null
                && Arrays.equals(pattern, bytes))
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
        return Arrays.toString(pattern);
    }
}
