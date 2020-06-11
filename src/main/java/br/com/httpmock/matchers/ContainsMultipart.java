package br.com.httpmock.matchers;

import java.util.List;

import br.com.httpmock.models.Multipart;

public class ContainsMultipart
        implements IMatcher<List<Multipart>>
{
    private Multipart pattern;

    public ContainsMultipart(String pattern)
    {
        throw new RuntimeException("Comparação para xml não implementada");
        // this.pattern = pattern;
    }

    public ContainsMultipart(Multipart pattern)
    {
        throw new RuntimeException("Comparação para multipart não implementada");
        // this.pattern = pattern;
    }

    public ContainsMultipart withPattern(Multipart pattern)
    {
        throw new RuntimeException("Comparação para multipart não implementada");
        // this.pattern = pattern;
        // return this;
    }

    @Override
    public boolean matches(List<Multipart> multipart)
    {
        throw new RuntimeException("Comparação para multipart não implementada");
        // return hit(multipart) == IMatcher.MAX_HIT_VALUE;
    }

    @Override
    public int hit(List<Multipart> multipart)
    {
        throw new RuntimeException("Comparação para multipart não implementada");
    }

    @Override
    public String toString()
    {
        return String.valueOf(pattern);
    }

}
