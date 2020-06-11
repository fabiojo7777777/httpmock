package br.com.httpmock.models;

import java.util.Iterator;
import java.util.List;

public class JsonObject
        extends JsonElement
{
    private List<JsonProperty> properties;

    public JsonObject(JsonElement parent, List<JsonProperty> properties)
    {
        super(parent);
        this.properties = properties;
    }

    public List<JsonProperty> getProperties()
    {
        return properties;
    }

    @Override
    public boolean isGarbage()
    {
        return properties == null || properties.size() == 0;
    }

    @Override
    public void removeInternalGarbage()
    {
        if (properties != null)
        {
            Iterator<JsonProperty> it = properties.iterator();
            while (it.hasNext())
            {
                JsonProperty item = it.next();
                if (item == null || item.isGarbage())
                {
                    it.remove();
                }
            }
        }
    }

    @Override
    public int hashCode()
    {
        final int prime  = 31;
        int       result = 1;
        result = prime * result + ((properties == null) ? 0 : properties.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        JsonObject other = (JsonObject) obj;
        if (properties == null)
        {
            if (other.properties != null)
                return false;
        }
        else if (!properties.equals(other.properties))
            return false;
        return true;
    }

    @Override
    public String toString()
    {
        if (properties == null)
        {
            return "null";
        }
        Iterator<JsonProperty> it = properties.iterator();
        if (!it.hasNext())
        {
            return "{}";
        }
        StringBuilder sb = new StringBuilder();
        sb.append('{');
        for (;;)
        {
            JsonProperty e = it.next();
            sb.append(e == properties ? "(this Collection)" : e);
            if (!it.hasNext())
            {
                return sb.append('}').toString();
            }
            sb.append(',').append(' ');
        }
    }

}
