package br.com.httpmock.models;

public abstract class JsonElement
{
    private JsonElement parent;

    public JsonElement(JsonElement parent)
    {
        this.parent = parent;
    }

    public JsonElement getParent()
    {
        return this.parent;
    }

    public void setParent(JsonElement parent)
    {
        this.parent = parent;
    }

    public abstract boolean isGarbage();

    public void removeGarbage()
    {
        removeInternalGarbage();
        if (isGarbage())
        {
            if (getParent() != null)
            {
                getParent().removeGarbage();
            }
        }
    }

    public abstract void removeInternalGarbage();
}
