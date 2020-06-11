package br.com.httpmock.matchers;

public interface IMatcher<T>
{
    // A HIT WITH THIS VALUE MEANS TWO ELEMENTS ARE 100% EQUALS
    public static final int MAX_HIT_VALUE = 100;

    public boolean matches(T pattern);

    public int hit(T wrapper);

}
