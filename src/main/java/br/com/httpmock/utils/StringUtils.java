package br.com.httpmock.utils;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.hc.client5.http.entity.GzipDecompressingEntity;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.io.entity.EntityUtils;

public class StringUtils
{
    @SuppressWarnings("unused")
    private static final StringUtils INSTANCE = new StringUtils();

    private StringUtils()
    {
        super();
    }

    public static String getFullErrorMessage(Throwable e)
    {
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        sw.toString();
        return getErrorMessage(e) + ".\nDescrição: \n" + sw.toString();
    }

    public static String getErrorMessage(Throwable e)
    {
        String message = e.getMessage();
        if (message == null)
        {
            message = e.getClass().getCanonicalName();
        }
        return message;
    }

    public static int toInteger(Object value)
    {
        try
        {
            return Integer.parseInt(((String) value).trim());
        }
        catch (Throwable e)
        {
            return 0;
        }
    }

    public static String getHashCode(Object objeto)
    {
        if (objeto == null)
        {
            return Constants.NULL_TEXT;
        }
        else
        {
            return Integer.toString(System.identityHashCode(objeto));
        }
    }

    public static void delay(int milisseconds)
    {
        try
        {
            Thread.sleep(milisseconds);
        }
        catch (InterruptedException e)
        {
        }
    }

    public static String fillLeftWithZeros(String number)
    {
        return fillLeftWithZeros(number, Constants.FIVE);
    }

    public static String fillLeftWithZeros(String number, int digits)
    {
        String text  = Constants.FILLING_ZEROS + number;
        int    start = text.length() - digits;
        if (start < 0)
        {
            start = 0;
        }
        return text.substring(start, text.length());
    }

    public static String getContent(HttpEntity entity)
    {
        try
        {
            if (entity == null)
            {
                return null;
            }
            else
            {
                String contentEncoding = entity.getContentEncoding();
                if (contentEncoding != null && contentEncoding.contains(Constants.GZIP_ENCODING))
                {
                    return EntityUtils.toString(new GzipDecompressingEntity(entity), Constants.UTF8);
                }
                else
                {
                    return EntityUtils.toString(entity, Constants.UTF8);
                }
            }
        }
        catch (Throwable e)
        {
            return getFullErrorMessage(e);
        }
    }

    public static String getTimestamp()
    {
        return new SimpleDateFormat("yyyy-MM-dd-HH.mm.ss.SSS").format(new Date());
    }
}
