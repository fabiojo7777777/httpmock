package br.com.httpmock.utils;

import java.net.URL;

public class ValidatorUtils
{
    @SuppressWarnings("unused")
    private static final ValidatorUtils INSTANCE = new ValidatorUtils();

    private ValidatorUtils()
    {
        super();
    }

    public static void notEmpty(String text, String errorMessage)
    {
        if (text == null || "".equals(text.trim()))
        {
            throw new RuntimeException(errorMessage);
        }
    }

    public static void numeric(String text, String errorMessage)
    {
        try
        {
            Integer.parseInt(text);
        }
        catch (Throwable e)
        {
            throw new RuntimeException(errorMessage);
        }
    }

    public static void positive(int number, String errorMessage)
    {
        if (number < 0)
        {
            throw new RuntimeException(errorMessage);
        }
    }

    public static void notNull(Object object, String errorMessage)
    {
        if (object == null)
        {
            throw new RuntimeException(errorMessage);
        }
    }

    public static void validUrl(String value, String errorMessage)
    {
        try
        {
            URL url = new URL(value);
            url.toURI(); // valid URI?
            if (!"HTTP".equalsIgnoreCase(url.getProtocol())
                    && !"HTTPS".equalsIgnoreCase(url.getProtocol()))
            {
                throw new RuntimeException("Somente protocolo http e https são aceitos");
            }
        }
        catch (Throwable e)
        {
            throw new RuntimeException(errorMessage);
        }
    }

}
