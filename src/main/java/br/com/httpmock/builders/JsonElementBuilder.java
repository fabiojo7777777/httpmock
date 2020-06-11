package br.com.httpmock.builders;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;

import com.fasterxml.jackson.databind.ObjectMapper;

import br.com.httpmock.models.JsonElement;
import br.com.httpmock.models.JsonList;
import br.com.httpmock.models.JsonObject;
import br.com.httpmock.models.JsonPrimitive;
import br.com.httpmock.models.JsonProperty;

public class JsonElementBuilder
{
    private LinkedHashMap<String, Object> map;

    private JsonElementBuilder()
    {
        super();
    }

    public static JsonElementBuilder Builder()
    {
        return new JsonElementBuilder();
    }

    public JsonElementBuilder withProperty(String key, String value)
    {
        if (map == null)
        {
            map = new LinkedHashMap<String, Object>();
        }
        map.put(key, value);
        return this;
    }

    public JsonElement build(Object o)
    {
        Object  obj       = o;
        boolean converted = false;
        if (o instanceof String)
        {
            ObjectMapper OBJECT_MAPPER = new ObjectMapper();
            try
            {
                obj = OBJECT_MAPPER.readValue((String) o, LinkedHashMap.class);
            }
            catch (Throwable e1)
            {
                // ignore
                try
                {
                    obj = OBJECT_MAPPER.readValue((String) o, List.class);
                }
                catch (Throwable e2)
                {
                    // ignore
                }
            }
            converted = true;
        }
        return wrapElement(null, obj, converted);
    }

    private static JsonElement wrapElement(JsonElement parent, Object o, boolean converted)
    {
        Object obj = o;
        if (!converted)
        {
            ObjectMapper OBJECT_MAPPER = new ObjectMapper();
            try
            {
                obj = OBJECT_MAPPER.convertValue(o, LinkedHashMap.class);
            }
            catch (Throwable e1)
            {
                // ignore
                try
                {
                    obj = OBJECT_MAPPER.convertValue(o, List.class);
                }
                catch (Throwable e2)
                {
                    // ignore
                }
            }
            converted = true;
        }

        if (obj instanceof List)
        {
            return wrapList(parent, (List<?>) obj, converted);
        }
        else if (obj instanceof LinkedHashMap)
        {
            return wrapObject(parent, (LinkedHashMap<?, ?>) obj, converted);
        }
        else
        {
            return wrapPrimitive(parent, obj, converted);
        }
    }

    private static JsonElement wrapList(JsonElement parent, List<?> list, boolean converted)
    {
        List<JsonElement> items    = new ArrayList<JsonElement>();
        JsonList          jsonList = new JsonList(parent, items);
        for (Object item : list)
        {
            items.add(wrapElement(jsonList, item, converted));
        }
        return jsonList;
    }

    private static JsonElement wrapObject(JsonElement parent, LinkedHashMap<?, ?> obj, boolean converted)
    {
        List<JsonProperty> properties = new ArrayList<JsonProperty>();
        JsonObject         jsonObject = new JsonObject(parent, properties);
        for (Entry<?, ?> entry : obj.entrySet())
        {
            Object       key      = entry.getKey();
            Object       value    = entry.getValue();

            String       k        = key == null ? null : key.toString();
            JsonProperty property = new JsonProperty(jsonObject, k, null);
            JsonElement  v        = wrapElement(property, value, converted);

            property.setValue(v);

            properties.add(property);
        }
        return jsonObject;
    }

    private static JsonElement wrapPrimitive(JsonElement parent, Object value, boolean converted)
    {
        return new JsonPrimitive(parent, value == null ? null : value);
    }

    public JsonElement build()
    {
        return build(map);
    }

}
