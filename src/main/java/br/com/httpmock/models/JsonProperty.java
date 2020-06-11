package br.com.httpmock.models;

public class JsonProperty
        extends JsonElement
{
    private String      key;
    private JsonElement value;

    public JsonProperty(JsonElement parent, String key, JsonElement value)
    {
        super(parent);
        this.key   = key;
        this.value = value;
    }

    public String getKey()
    {
        return key;
    }

    public JsonElement getValue()
    {
        return value;
    }

    public void setValue(JsonElement value)
    {
        this.value = value;
    }

    @Override
    public boolean isGarbage()
    {
        return value == null || value.isGarbage();
    }

    @Override
    public void removeInternalGarbage()
    {
        // Nothing to do
    }

    @Override
    public int hashCode()
    {
        final int prime  = 31;
        int       result = 1;
        result = prime * result + ((key == null) ? 0 : key.hashCode());
        result = prime * result + ((value == null) ? 0 : value.hashCode());
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
        JsonProperty other = (JsonProperty) obj;
        if (key == null)
        {
            if (other.key != null)
                return false;
        }
        else if (!key.equals(other.key))
            return false;
        if (value == null)
        {
            if (other.value != null)
                return false;
        }
        else if (!value.equals(other.value))
            return false;
        return true;
    }

    @Override
    public String toString()
    {
        return key + ":" + value;
    }

}
