package br.com.httpmock.utils;

import org.junit.Assert;
import org.junit.Test;

public class UrlReplacerTest
{
    @Test
    public void urlReplace()
    {
        UrlReplacer urlReplacer = new UrlReplacer();

        urlReplacer.addMapping("http://a", "file://a");
        urlReplacer.addMapping("http://lo/ttt", "file://lo");
        urlReplacer.addMapping("http://localhost.dinamico.com.br", "file://localhost.dinamico.com.br");
        urlReplacer.addMapping("http://localhost.dinamico.com.br/", "file://localhost.dinamico.com.br/");
        urlReplacer.addMapping("http://localhost.dinamico.com.br/xyz", "file://localhost.dinamico.com.br/xyz");
        String txt    = "http://lo/ttt?teste=1 http://localhost.dinamico.com.br/xy?teste=1   http://localhost.dinamico.com.br?teste=1";
        String result = urlReplacer.replace(txt);
        Assert.assertEquals("file://lo/?teste=1 file://localhost.dinamico.com.br/xy?teste=1   file://localhost.dinamico.com.br/?teste=1", result);
    }

    @Test
    public void jsonUrlReplace()
    {
        UrlReplacer urlReplacer = new UrlReplacer();

        urlReplacer.addMapping("http://a", "file://a");
        urlReplacer.addMapping("http://lo/ttt", "file://lo");
        urlReplacer.addMapping("http://localhost.dinamico.com.br", "file://localhost.dinamico.com.br");
        urlReplacer.addMapping("http://localhost.dinamico.com.br/", "file://localhost.dinamico.com.br/");
        urlReplacer.addMapping("http://localhost.dinamico.com.br/xyz", "file://localhost.dinamico.com.br/xyz");
        String txt    = "http:\\/\\/lo\\/ttt?teste=1 http:\\/\\/localhost.dinamico.com.br\\/xy?teste=1   http:\\/\\/localhost.dinamico.com.br?teste=1";
        String result = urlReplacer.replace(txt);
        Assert.assertEquals("file:\\/\\/lo\\/?teste=1 file:\\/\\/localhost.dinamico.com.br\\/xy?teste=1   file:\\/\\/localhost.dinamico.com.br\\/?teste=1", result);
    }

    @Test
    public void urlEncodeReplace()
    {
        UrlReplacer urlReplacer = new UrlReplacer();

        urlReplacer.addMapping("http://a", "file://a");
        urlReplacer.addMapping("http://lo", "file://lo");
        urlReplacer.addMapping("http://localhost.dinamico.com.br", "file://localhost.dinamico.com.br");
        urlReplacer.addMapping("http://localhost.dinamico.com.br/", "file://localhost.dinamico.com.br/");
        urlReplacer.addMapping("http://localhost.dinamico.com.br/xyz", "file://localhost.dinamico.com.br/xyz");
        String txt    = "http%3A%2F%2Flo http%3A%2F%2Flocalhost.dinamico.com.br%2Fxy  http%3A%2F%2Flocalhost.dinamico.com.br%2Fxyz";
        String result = urlReplacer.replace(txt);
        Assert.assertEquals("file%3A%2F%2Flo%2F file%3A%2F%2Flocalhost.dinamico.com.br%2Fxy  file%3A%2F%2Flocalhost.dinamico.com.br%2Fxyz", result);
    }

    @Test
    public void urlEncodeReplaceEndText()
    {
        UrlReplacer urlReplacer = new UrlReplacer();

        urlReplacer.addMapping("http://a", "file://a");
        urlReplacer.addMapping("http://lo", "file://lo");
        urlReplacer.addMapping("http://localhost.dinamico.com.br", "file://localhost.dinamico.com.br");
        urlReplacer.addMapping("http://localhost.dinamico.com.br/", "file://localhost.dinamico.com.br/");
        urlReplacer.addMapping("http://localhost.dinamico.com.br/xyz", "file://localhost.dinamico.com.br/xyz");
        String txt    = "?http%3A%2F%2Flo http%3A%2F%2Flocalhost.dinamico.com.br%2Fxy  http%3A%2F%2Flocalhost.dinamico.com.br?";
        String result = urlReplacer.replace(txt);
        Assert.assertEquals("?file%3A%2F%2Flo%2F file%3A%2F%2Flocalhost.dinamico.com.br%2Fxy  file%3A%2F%2Flocalhost.dinamico.com.br%2F?", result);
    }

    @Test
    public void urlEncodeReplaceEndText2()
    {
        UrlReplacer urlReplacer = new UrlReplacer();

        urlReplacer.addMapping("http://a", "file://a");
        urlReplacer.addMapping("http://lo", "file://lo");
        urlReplacer.addMapping("http://localhost.dinamico.com.br%", "file://localhost.dinamico.com.br");
        urlReplacer.addMapping("http://localhost.dinamico.com.br/xyz", "file://localhost.dinamico.com.br?xyz");
        String txt    = "?http%3A%2F%2Flo http%3A%2F%2Flocalhost.dinamico.com.br%2Fxy  http%3A%2F%2Flocalhost.dinamico.com.br";
        String result = urlReplacer.replace(txt);
        Assert.assertEquals("?file%3A%2F%2Flo%2F file%3A%2F%2Flocalhost.dinamico.com.br%2Fxy  file%3A%2F%2Flocalhost.dinamico.com.br%2F", result);
    }
}
