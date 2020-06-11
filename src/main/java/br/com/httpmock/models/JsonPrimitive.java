package br.com.httpmock.models;

public class JsonPrimitive
        extends JsonElement
{
    private Object value;

    public JsonPrimitive(JsonElement parent, Object value)
    {
        super(parent);
        this.value = value;
    }

    public Object getValue()
    {
        return value;
    }

    @Override
    public boolean isGarbage()
    {
        return value == null;
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
        JsonPrimitive other = (JsonPrimitive) obj;
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
        return String.valueOf(value);
    }

}
