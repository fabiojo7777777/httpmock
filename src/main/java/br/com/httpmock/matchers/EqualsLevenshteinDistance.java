package br.com.httpmock.matchers;

import org.apache.commons.text.similarity.LevenshteinDistance;

public class EqualsLevenshteinDistance
        implements IMatcher<String>
{
    private String                                  pattern;
    private static ThreadLocal<LevenshteinDistance> LEVENSHTEIN_DISTANCE = new ThreadLocal<LevenshteinDistance>()
                                                                         {
                                                                             public LevenshteinDistance initialValue()
                                                                             {
                                                                                 return new LevenshteinDistance();
                                                                             }
                                                                         };

    public EqualsLevenshteinDistance(String pattern)
    {
        this.pattern = pattern;
    }

    public EqualsLevenshteinDistance withPattern(String pattern)
    {
        this.pattern = pattern;
        return this;
    }

    @Override
    public boolean matches(String text)
    {
        return hit(text) == IMatcher.MAX_HIT_VALUE;
    }

    @Override
    public int hit(String text)
    {
        return hit(pattern, text);
    }

    public static int hit(String text1, String text2)
    {
        LevenshteinDistance levenshteinDistance = LEVENSHTEIN_DISTANCE.get();
        String              t1                  = text1 == null ? "" : text1;
        String              t2                  = text2 == null ? "" : text2;

        int                 maxLength           = Math.max(t1.length(), t2.length());
        if (maxLength == 0)
        {
            return IMatcher.MAX_HIT_VALUE;
        }

        Integer distance = levenshteinDistance.apply(t1, t2);
        if (distance == null)
        {
            distance = 0;
        }

        int similarity = maxLength - distance;
        if (similarity < 0 || similarity > maxLength)
        {// error in algorithm? then, not similar
            similarity = 0;
        }

        if (similarity == maxLength)
        {
            return IMatcher.MAX_HIT_VALUE;
        }
        else
        {
            return (int) ((float) similarity / (float) maxLength * (float) IMatcher.MAX_HIT_VALUE);
        }
    }

    @Override
    public String toString()
    {
        return pattern;
    }
}
