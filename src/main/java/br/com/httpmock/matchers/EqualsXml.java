package br.com.httpmock.matchers;

import br.com.httpmock.models.Xml;

public class EqualsXml
        implements IMatcher<Xml>
{
    private String pattern;

    public EqualsXml(String pattern)
    {
        throw new RuntimeException("Comparação para xml não implementada");
        // this.pattern = pattern;
    }

    public EqualsXml(Xml pattern)
    {
        throw new RuntimeException("Comparação para xml não implementada");
        // this.pattern = pattern;
    }

    public EqualsXml withPattern(String pattern)
    {
        throw new RuntimeException("Comparação para xml não implementada");
        // this.pattern = pattern;
        // return this;
    }

    @Override
    public boolean matches(Xml xml)
    {
        return hit(xml) == IMatcher.MAX_HIT_VALUE;
    }

    @Override
    public int hit(Xml xml)
    {
        throw new RuntimeException("Comparação para xml não implementada");
    }

    @Override
    public String toString()
    {
        throw new RuntimeException("Comparação para xml não implementada");
        // return pattern;
    }
}
