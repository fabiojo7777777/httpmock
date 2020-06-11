package br.com.httpmock.matchers;

import br.com.httpmock.models.JsonPrimitive;

public class EqualsJsonPrimitive
        implements IMatcher<JsonPrimitive>
{
    private JsonPrimitive pattern;

    public EqualsJsonPrimitive withPattern(JsonPrimitive pattern)
    {
        this.pattern = pattern;
        return this;
    }

    @Override
    public boolean matches(JsonPrimitive jsonPrimitive)
    {
        return hit(jsonPrimitive) == IMatcher.MAX_HIT_VALUE;
    }

    @Override
    public int hit(JsonPrimitive jsonPrimitive)
    {
        if (pattern == null && jsonPrimitive == null)
        {
            return IMatcher.MAX_HIT_VALUE;
        }
        else if (pattern != null
                && jsonPrimitive != null)
        {
            if (pattern.equals(jsonPrimitive))
            {
                return IMatcher.MAX_HIT_VALUE;
            }
            else
            {
                return 0;
            }
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