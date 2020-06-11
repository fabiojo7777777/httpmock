package br.com.httpmock.matchers;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import br.com.httpmock.models.JsonElement;
import br.com.httpmock.models.JsonList;

public class EqualsJsonList
        implements IMatcher<JsonList>
{
    private boolean  ignoreExtraProperties = true;
    private boolean  ignoreArrayOrder;
    private JsonList pattern;

    public EqualsJsonList withIgnoreExtraProperties(boolean ignoreExtraProperties)
    {
        this.ignoreExtraProperties = ignoreExtraProperties;
        return this;
    }

    public EqualsJsonList withIgnoreArrayOrder(boolean ignoreArrayOrder)
    {
        this.ignoreArrayOrder = ignoreArrayOrder;
        return this;
    }

    public EqualsJsonList withPattern(JsonList pattern)
    {
        this.pattern = pattern;
        return this;
    }

    @Override
    public boolean matches(JsonList jsonList)
    {
        return hit(jsonList) == IMatcher.MAX_HIT_VALUE;
    }

    @Override
    public int hit(JsonList jsonList)
    {
        int size1 = EqualsJsonElement.getListSize(pattern);
        int size2 = EqualsJsonElement.getListSize(jsonList);
        if (size1 != size2)
        {
            // null have -1 in size
            if (size1 < 0 || size2 < 0)
            {
                size1++;
                size2++;
            }
            int distance = Math.abs(size1 - size2);
            int maxSize  = Math.max(size1, size2);
            if (maxSize == 0) // division by 0
            {
                return 0;
            }
            else
            {
                return (int) ((float) distance / (float) maxSize * (float) IMatcher.MAX_HIT_VALUE);
            }
        }
        else
        {
            // here, size of both lists are equal
            int                     hitSum    = 0;
            int                     maxHitSum = 0;

            final List<JsonElement> list1     = EqualsJsonElement.getItems(pattern);
            final List<JsonElement> list2     = EqualsJsonElement.getItems(jsonList);

            if (ignoreArrayOrder)
            {
                int[] info = hitListIgnoreArrayOrder(
                        list1,
                        list2,
                        ignoreExtraProperties,
                        ignoreArrayOrder);
                hitSum    = info[0];
                maxHitSum = info[1];
            }
            else
            {
                Iterator<JsonElement> it1 = list1.iterator();
                Iterator<JsonElement> it2 = list2.iterator();
                while (it1.hasNext() && it2.hasNext())
                {
                    JsonElement item1 = it1.next();
                    JsonElement item2 = it2.next();
                    int         hit   = new EqualsJsonElement()
                            .withPattern(item1)
                            .withIgnoreArrayOrder(ignoreArrayOrder)
                            .withIgnoreExtraProperties(ignoreExtraProperties)
                            .hit(item2);
                    hitSum    = hitSum + hit;
                    maxHitSum = maxHitSum + IMatcher.MAX_HIT_VALUE;
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
    }

    private int[] hitListIgnoreArrayOrder(
            List<JsonElement> list1,
            List<JsonElement> list2,
            boolean ignoreExtraProperties,
            boolean ignoreArrayOrder)
    {
        int                             hitSum         = 0;
        int                             maxHitSum      = 0;
        Set<Integer>                    matchedIndexes = new HashSet<Integer>();
        Iterator<? extends JsonElement> it1            = list1.iterator();
        while (it1.hasNext())
        {
            JsonElement item1        = it1.next();

            int         matchedIndex = -1;
            int         maxHit       = 0;
            for (int i = 0, size = list2.size(); i < size; i++)
            {
                if (!matchedIndexes.contains(i))
                {
                    JsonElement item2 = list2.get(i);
                    int         hit   = new EqualsJsonElement()
                            .withPattern(item1)
                            .withIgnoreArrayOrder(ignoreArrayOrder)
                            .withIgnoreExtraProperties(ignoreExtraProperties)
                            .hit(item2);
                    if (hit > maxHit)
                    {
                        maxHit       = hit;
                        matchedIndex = i;
                        if (maxHit == IMatcher.MAX_HIT_VALUE)
                        {
                            break;
                        }
                    }
                }
            }
            if (matchedIndex != -1)
            {
                matchedIndexes.add(matchedIndex);
            }
            hitSum    = hitSum + maxHit;
            maxHitSum = maxHitSum + IMatcher.MAX_HIT_VALUE;
        }
        return new int[] {
                hitSum,
                maxHitSum
        };
    }

    @Override
    public String toString()
    {
        return String.valueOf(pattern);
    }
}
