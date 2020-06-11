package br.com.httpmock.matchers;

import br.com.httpmock.models.JsonElement;
import br.com.httpmock.models.JsonProperty;

public class EqualsJsonProperty
        implements IMatcher<JsonProperty>
{
    private boolean      ignoreExtraProperties = true;
    private boolean      ignoreArrayOrder;
    private JsonProperty pattern;
    private int          keyHitWeight          = 70;
    private int          valueHitWeight        = 30;

    public EqualsJsonProperty withIgnoreExtraProperties(boolean ignoreExtraProperties)
    {
        this.ignoreExtraProperties = ignoreExtraProperties;
        return this;
    }

    public EqualsJsonProperty withIgnoreArrayOrder(boolean ignoreArrayOrder)
    {
        this.ignoreArrayOrder = ignoreArrayOrder;
        return this;
    }

    public EqualsJsonProperty withPattern(JsonProperty pattern)
    {
        this.pattern = pattern;
        return this;
    }

    @Override
    public boolean matches(JsonProperty jsonProperty)
    {
        return hit(jsonProperty) == IMatcher.MAX_HIT_VALUE;
    }

    @Override
    public int hit(JsonProperty jsonProperty)
    {
        String key1         = EqualsJsonElement.getKey(pattern);
        String key2         = EqualsJsonElement.getKey(jsonProperty);
        String stringValue1 = EqualsJsonElement.getStringValue(pattern);
        String stringValue2 = EqualsJsonElement.getStringValue(jsonProperty);
        if (EqualsString.equals(key1, key2)
                && EqualsString.equals(stringValue1, stringValue2))
        {
            return IMatcher.MAX_HIT_VALUE;
        }
        else
        {
            int         hitSum      = 0;
            int         maxHitSum   = 0;

            int         keyDistance = EqualsLevenshteinDistance.hit(key1, key2);

            JsonElement value1      = EqualsJsonElement.getValue(pattern);
            JsonElement value2      = EqualsJsonElement.getValue(jsonProperty);
            int         valueHit    = new EqualsJsonElement()
                    .withPattern(value1)
                    .withIgnoreArrayOrder(ignoreArrayOrder)
                    .withIgnoreExtraProperties(ignoreExtraProperties)
                    .hit(value2);
            // int valueDistance = EqualsLevenshteinDistance.hit(value1,
            // value2);

            hitSum    = hitSum
                    + (keyDistance * keyHitWeight)
                    + (valueHit * valueHitWeight);

            maxHitSum = maxHitSum
                    + (IMatcher.MAX_HIT_VALUE * keyHitWeight)
                    + (IMatcher.MAX_HIT_VALUE * valueHitWeight);

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
    }

    @Override
    public String toString()
    {
        return String.valueOf(pattern);
    }
}