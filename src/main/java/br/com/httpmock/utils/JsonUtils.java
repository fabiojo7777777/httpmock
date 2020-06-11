package br.com.httpmock.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonUtils
{
    private JsonUtils()
    {
        super();
    }

    public static String get(JsonNode jsonNode, String path)
    {
        try
        {
            String[] parts = path.split("\\.");
            for (String part : parts)
            {
                jsonNode = jsonNode.get(part);
            }
            return jsonNode.asText();
        }
        catch (Throwable e)
        {
            return null;
        }
    }

    public static JsonNode[] getItems(JsonNode jsonNode, String path)
    {
        try
        {
            String[] parts = path.split("\\.");
            for (String part : parts)
            {
                jsonNode = jsonNode.get(part);
            }
            if (jsonNode.isArray())
            {
                int        size  = jsonNode.size();
                JsonNode[] items = new JsonNode[size];
                for (int i = 0; i < size; i++)
                {
                    items[i] = jsonNode.get(i);
                }
                return items;
            }
            return null;
        }
        catch (Throwable e)
        {
            return null;
        }
    }

    public static JsonNode read(String json)
            throws JsonMappingException,
            JsonProcessingException
    {
        ObjectMapper OBJECT_MAPPER = new ObjectMapper();
        return OBJECT_MAPPER.readTree(json);
    }

    public static String write(Object obj)
            throws JsonProcessingException
    {
        ObjectMapper OBJECT_MAPPER = new ObjectMapper();
        return OBJECT_MAPPER.writeValueAsString(obj);
    }

}
