package br.com.httpmock.matchers;

import java.util.List;
import java.util.Vector;

import br.com.httpmock.models.JsonElement;
import br.com.httpmock.models.JsonList;
import br.com.httpmock.models.JsonObject;
import br.com.httpmock.models.JsonPrimitive;
import br.com.httpmock.models.JsonProperty;

public class EqualsJsonElement
        implements IMatcher<JsonElement>
{
    private boolean     ignoreExtraProperties = true;
    private boolean     ignoreArrayOrder;
    private JsonElement pattern;

    public EqualsJsonElement()
    {
    }

    public EqualsJsonElement(JsonElement pattern)
    {
        this.pattern = pattern;
    }

    public EqualsJsonElement withIgnoreExtraProperties(boolean ignoreExtraProperties)
    {
        this.ignoreExtraProperties = ignoreExtraProperties;
        return this;
    }

    public EqualsJsonElement withIgnoreArrayOrder(boolean ignoreArrayOrder)
    {
        this.ignoreArrayOrder = ignoreArrayOrder;
        return this;
    }

    public EqualsJsonElement withPattern(JsonElement pattern)
    {
        this.pattern = pattern;
        return this;
    }

    @Override
    public boolean matches(JsonElement jsonElement)
    {
        return hit(jsonElement) == IMatcher.MAX_HIT_VALUE;
    }

    @Override
    public int hit(JsonElement jsonElement)
    {
        int hitSum    = 0;
        int maxHitSum = 0;
        if (pattern == null && jsonElement == null)
        {
            return IMatcher.MAX_HIT_VALUE;
        }
        else if (pattern != null && jsonElement != null)
        {
            if (pattern.getClass() == jsonElement.getClass())
            {
                if (pattern instanceof JsonList)
                {
                    hitSum    = new EqualsJsonList()
                            .withPattern((JsonList) pattern)
                            .withIgnoreArrayOrder(ignoreArrayOrder)
                            .withIgnoreExtraProperties(ignoreExtraProperties)
                            .hit((JsonList) jsonElement);
                    maxHitSum = maxHitSum + IMatcher.MAX_HIT_VALUE;
                }
                else if (pattern instanceof JsonObject)
                {
                    hitSum    = new EqualsJsonObject()
                            .withPattern((JsonObject) pattern)
                            .withIgnoreArrayOrder(ignoreArrayOrder)
                            .withIgnoreExtraProperties(ignoreExtraProperties)
                            .hit((JsonObject) jsonElement);
                    maxHitSum = maxHitSum + IMatcher.MAX_HIT_VALUE;
                }
                else if (pattern instanceof JsonProperty)
                {
                    hitSum    = new EqualsJsonProperty()
                            .withPattern((JsonProperty) pattern)
                            .withIgnoreArrayOrder(ignoreArrayOrder)
                            .withIgnoreExtraProperties(ignoreExtraProperties)
                            .hit((JsonProperty) jsonElement);
                    maxHitSum = maxHitSum + IMatcher.MAX_HIT_VALUE;
                }
                else if (pattern instanceof JsonPrimitive)
                {
                    hitSum    = new EqualsJsonPrimitive()
                            .withPattern((JsonPrimitive) pattern)
                            .hit((JsonPrimitive) jsonElement);
                    maxHitSum = maxHitSum + IMatcher.MAX_HIT_VALUE;
                }
            }
            else
            {
                return 0;
            }
        }
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

    public static int getListSize(JsonList jsonList)
    {
        if (jsonList == null)
        {
            return -1;
        }
        else if (jsonList.getList() == null)
        {
            return -1;
        }
        else
        {
            return jsonList.getList().size();
        }
    }

    public static List<JsonElement> getItems(JsonList jsonList)
    {
        final List<JsonElement> list;
        if (jsonList != null && jsonList.getList() != null)
        {
            list = jsonList.getList();
        }
        else
        {
            list = new Vector<JsonElement>();
        }
        return list;
    }

    public static List<JsonProperty> getProperties(JsonObject jsonObject)
    {
        final List<JsonProperty> properties;
        if (jsonObject != null && jsonObject.getProperties() != null)
        {
            properties = jsonObject.getProperties();
        }
        else
        {
            properties = new Vector<JsonProperty>();
        }
        return properties;
    }

    @Override
    public String toString()
    {
        return String.valueOf(pattern);
    }

    public static String getKey(JsonProperty jsonProperty)
    {
        final String key;
        if (jsonProperty != null && jsonProperty.getKey() != null)
        {
            key = jsonProperty.getKey();
        }
        else
        {
            key = null;
        }
        return key;
    }

    public static String getStringValue(JsonProperty jsonProperty)
    {
        final String value;
        if (jsonProperty != null && jsonProperty.getValue() != null)
        {
            value = jsonProperty.getValue().toString();
        }
        else
        {
            value = null;
        }
        return value;
    }

    public static JsonElement getValue(JsonProperty jsonProperty)
    {
        final JsonElement value;
        if (jsonProperty != null && jsonProperty.getValue() != null)
        {
            value = jsonProperty.getValue();
        }
        else
        {
            value = null;
        }
        return value;
    }
}
