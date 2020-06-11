package br.com.httpmock.matchers;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import br.com.httpmock.models.JsonElement;
import br.com.httpmock.models.JsonObject;
import br.com.httpmock.models.JsonProperty;

public class EqualsJsonObject
        implements IMatcher<JsonObject>
{
    private boolean    ignoreExtraProperties = true;
    private boolean    ignoreArrayOrder;
    private JsonObject pattern;

    public EqualsJsonObject withIgnoreExtraProperties(boolean ignoreExtraProperties)
    {
        this.ignoreExtraProperties = ignoreExtraProperties;
        return this;
    }

    public EqualsJsonObject withIgnoreArrayOrder(boolean ignoreArrayOrder)
    {
        this.ignoreArrayOrder = ignoreArrayOrder;
        return this;
    }

    public EqualsJsonObject withPattern(JsonObject pattern)
    {
        this.pattern = pattern;
        return this;
    }

    @Override
    public boolean matches(JsonObject jsonObject)
    {
        return hit(jsonObject) == IMatcher.MAX_HIT_VALUE;
    }

    @Override
    public int hit(JsonObject jsonObject)
    {
        final List<JsonProperty> properties1 = EqualsJsonElement.getProperties(pattern);
        final List<JsonProperty> properties2 = EqualsJsonElement.getProperties(jsonObject);

        int[]                    info        = hitListIgnoreArrayOrder(
                properties1,
                properties2,
                ignoreExtraProperties,
                ignoreArrayOrder);

        int                      hitSum      = info[0];
        int                      maxHitSum   = info[1];

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

    private int[] hitListIgnoreArrayOrder(
            List<JsonProperty> list1,
            List<JsonProperty> list2,
            boolean ignoreExtraProperties,
            boolean ignoreArrayOrder)
    {
        int                             hitSum              = 0;
        int                             maxHitSum           = 0;
        Set<Integer>                    matchedList1Indexes = new HashSet<Integer>();
        Set<Integer>                    matchedList2Indexes = new HashSet<Integer>();

        // First, match only properties 100% equals
        Iterator<? extends JsonElement> it1                 = list1.iterator();
        int                             index1              = -1;
        while (it1.hasNext())
        {
            index1++;
            JsonElement item1             = it1.next();

            int         matchedList2Index = -1;
            int         maxHit            = 0;
            for (int i = 0, size = list2.size(); i < size; i++)
            {
                if (!matchedList2Indexes.contains(i))
                {
                    JsonElement item2 = list2.get(i);
                    int         hit   = new EqualsJsonElement()
                            .withPattern(item1)
                            .withIgnoreArrayOrder(ignoreArrayOrder)
                            .withIgnoreExtraProperties(ignoreExtraProperties)
                            .hit(item2);
                    if (hit > maxHit)
                    {
                        maxHit            = hit;
                        matchedList2Index = i;
                        if (maxHit == IMatcher.MAX_HIT_VALUE)
                        {
                            break;
                        }
                    }
                }
            }
            if (maxHit == IMatcher.MAX_HIT_VALUE)
            {
                matchedList1Indexes.add(index1);
                if (matchedList2Index != -1)
                {
                    matchedList2Indexes.add(matchedList2Index);
                }
                hitSum    = hitSum + maxHit;
                maxHitSum = maxHitSum + IMatcher.MAX_HIT_VALUE;
            }
        }

        // Second, match only properties not 100% equals
        it1    = list1.iterator();
        index1 = -1;
        while (it1.hasNext())
        {
            index1++;
            JsonElement item1 = it1.next();

            if (!matchedList1Indexes.contains(index1))
            {
                int matchedList2Index = -1;
                int maxHit            = 0;
                for (int i = 0, size = list2.size(); i < size; i++)
                {
                    if (!matchedList2Indexes.contains(i))
                    {
                        JsonElement item2 = list2.get(i);
                        int         hit   = new EqualsJsonElement()
                                .withPattern(item1)
                                .withIgnoreArrayOrder(ignoreArrayOrder)
                                .withIgnoreExtraProperties(ignoreExtraProperties)
                                .hit(item2);
                        if (hit > maxHit)
                        {
                            maxHit            = hit;
                            matchedList2Index = i;
                            if (maxHit == IMatcher.MAX_HIT_VALUE)
                            {
                                break;
                            }
                        }
                    }
                }
                if (maxHit != IMatcher.MAX_HIT_VALUE)
                {
                    matchedList1Indexes.add(index1);
                    if (matchedList2Index != -1)
                    {
                        matchedList2Indexes.add(matchedList2Index);
                    }
                    hitSum    = hitSum + maxHit;
                    maxHitSum = maxHitSum + IMatcher.MAX_HIT_VALUE;
                }
            }
        }

        if (!ignoreExtraProperties)
        {
            for (int i = 0, size = list2.size(); i < size; i++)
            {
                if (!matchedList2Indexes.contains(i))
                {
                    JsonElement item2 = list2.get(i);
                    int         hit   = new EqualsJsonElement()
                            .withPattern(null)
                            .withIgnoreArrayOrder(ignoreArrayOrder)
                            .withIgnoreExtraProperties(ignoreExtraProperties)
                            .hit(item2);

                    hitSum    = hitSum + hit;
                    maxHitSum = maxHitSum + IMatcher.MAX_HIT_VALUE;
                }
            }
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
