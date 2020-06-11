package br.com.httpmock.models;

import java.util.Iterator;
import java.util.List;

public class JsonList
        extends JsonElement
{
    private List<JsonElement> list;

    public JsonList(JsonElement parent, List<JsonElement> list)
    {
        super(parent);
        this.list = list;
    }

    public List<JsonElement> getList()
    {
        return list;
    }

    @Override
    public boolean isGarbage()
    {
        return list == null || list.size() == 0;
    }

    @Override
    public void removeInternalGarbage()
    {
        if (list != null)
        {
            Iterator<JsonElement> it = list.iterator();
            while (it.hasNext())
            {
                JsonElement item = it.next();
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
        result = prime * result + ((list == null) ? 0 : list.hashCode());
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
        JsonList other = (JsonList) obj;
        if (list == null)
        {
            if (other.list != null)
                return false;
        }
        else if (!list.equals(other.list))
            return false;
        return true;
    }

    @Override
    public String toString()
    {
        return String.valueOf(list);
    }

}
