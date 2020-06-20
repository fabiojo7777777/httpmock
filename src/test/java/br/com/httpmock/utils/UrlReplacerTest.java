package br.com.httpmock.utils;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

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
        Assert.assertEquals("file:\\/\\/lo?teste=1 file:\\/\\/localhost.dinamico.com.br\\/xy?teste=1   file:\\/\\/localhost.dinamico.com.br?teste=1", result);
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

        urlReplacer.addMapping("http://localhost.dinamico.com.br", "file://localhost.dinamico.com.br");
        urlReplacer.addMapping("http://localhost.dinamico.com.br/xyz", "file://localhost.dinamico.com.br?xyz");
        String txt    = "?http%3A%2F%2Flo http%3A%2F%2Flocalhost.dinamico.com.br%2Fxy  http%3A%2F%2Flocalhost.dinamico.com.br";
        String result = urlReplacer.replace(txt);
        Assert.assertEquals("?file%3A%2F%2Flo%2F file%3A%2F%2Flocalhost.dinamico.com.br%2Fxy  file%3A%2F%2Flocalhost.dinamico.com.br", result);
    }

//    //@Test
    public void urlValidAfterReplace()
    {
        String[]     mappingDomains = new String[] {
                "http://l.d.com.br",
                "http://l.d.com.br:80",
                "http://l.e.com",
                "http://l.e.com:80"
        };
        String[]     mappingPaths   = new String[] {
                "",
                "/",
                "/abc",
                "/abc.txt",
                "/abc/",
                "/abc/abc.txt",
                "/abcaa",
                "/abcaa.txt",
                "/abcaa/",
                "/abcaa/abc.txt",
                "/abc/a",
                "/abc/a/",
                "/abc/aa",
                "/abc/aa.txt",
                "/abc/aa/",
                "/abc/aa/abc.txt",
        };

        String[]     inputDomains   = new String[] {
                "http://l.d.com.br:1",
                "http://l.d.com.br:80",
                "http://l.e.com:1",
                "http://l.e.com:80"
        };

        String[]     inputPaths     = new String[] {
                "",
                "/",
                "/abc",
                "/abc/",
                "/abc/a",
                "/abc/aa",
                "/abc/aa/",
        };

        String[]     inputQueries1  = new String[] {
                "",
                "?",
                "?abc.txt=1",
        };
        String[]     inputQueries2  = new String[] {
                "",
                "&",
                "&def.txt=2",
        };

        String[]     inputRefs1     = new String[] {
                "",
                "#",
                "#ghi=3",
        };

        String[]     inputRefs2     = new String[] {
                "",
                "?",
                "?jkl=4",
        };
        String[]     inputRefs3     = new String[] {
                "",
                "&",
                "&mno=5",
        };

        List<String> fromUrls       = new ArrayList<String>();
        List<String> toUrls         = new ArrayList<String>();
        for (String fromDomain : mappingDomains)
        {
            for (String fromPath : mappingPaths)
            {
                for (String toDomain : mappingDomains)
                {
                    for (String toPath : mappingPaths)
                    {
                        if (!(fromDomain + fromPath).equals(toDomain + toPath))
                        {
                            try
                            {
                                new URL(fromDomain + fromPath);
                                new URL(toDomain + toPath);
                                // add only valid urls
                                fromUrls.add(fromDomain + fromPath);
                                toUrls.add(toDomain + toPath);
                            }
                            catch (Throwable e)
                            {
                                // ignore
                            }
                        }
                    }
                }
            }
        }

        List<String> inputUrls = new ArrayList<String>();
        for (String inputDomain : inputDomains)
        {
            for (String inputPath : inputPaths)
            {
                for (String inputQuerie1 : inputQueries1)
                {
                    for (String inputQuerie2 : inputQueries2)
                    {
                        for (String inputRef1 : inputRefs1)
                        {
                            for (String inputRef2 : inputRefs2)
                            {
                                for (String inputRef3 : inputRefs3)
                                {
                                    try
                                    {
                                        String url = inputDomain + inputPath + inputQuerie1 + inputQuerie2 + inputRef1 + inputRef2 + inputRef3;
                                        new URL(url);
                                        // add only valid urls
                                        inputUrls.add(url);
                                    }
                                    catch (Throwable e)
                                    {
                                        // ignore
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        System.out.println("This is a massive test. Wait please...");
        for (int i = 0, size = inputUrls.size(); i < size; i++)
        {
            if (i % 50 == 0)
            {
                float percent = (float) i / (float) size * 100f;
                System.out.println(String.format("%4.2f", percent) + "% completed");
            }
            String inputUrl = inputUrls.get(i);
            for (int j = 0, size2 = fromUrls.size(); j < size2; j++)
            {
                UrlReplacer urlReplacer = new UrlReplacer();
                urlReplacer.addMapping(fromUrls.get(j), toUrls.get(j));
                String outputUrl = urlReplacer.replace(inputUrl);
                // System.out.println(i + " " + inputUrl + " => " + outputUrl);
                Assert.assertFalse(outputUrl.substring(7).contains("//"));
                try
                {
                    new URL(outputUrl);
                }
                catch (Throwable e)
                {
                    System.out.println(i + " " + inputUrl + " => " + outputUrl);
                }
            }
        }
        System.out.println(String.format("%4.2f", 100f) + "% completed");
    }

    @Test
    public void urlWithoutPortOnMappingAndWithPortOnReplacingText1()
    {
        UrlReplacer urlReplacer = new UrlReplacer();
        urlReplacer.addMapping("http://localhost.dinamico.com.br", "http://localhost.dinamico.com.br:81");
        String result = urlReplacer.replace("http://localhost.dinamico.com.br:89");
        Assert.assertEquals("http://localhost.dinamico.com.br:89", result);
    }

    @Test
    public void urlWithPortOnMappingLikePortInitialNumberOnReplacingText1()
    {
        UrlReplacer urlReplacer = new UrlReplacer();
        urlReplacer.addMapping("http://localhost.dinamico.com.br:80", "http://localhost.dinamico.com.br:81");
        String result = urlReplacer.replace("http://localhost.dinamico.com.br:8080");
        Assert.assertEquals("http://localhost.dinamico.com.br:8080", result);
    }

    @Test
    public void urlBasicReplacing1()
    {
        UrlReplacer urlReplacer = new UrlReplacer();
        urlReplacer.addMapping("http://localhost.dinamico.com.br:80", "http://localhost.dinamico.com.br:81");
        String result = urlReplacer.replace("http://localhost.dinamico.com.br:80");
        Assert.assertEquals("http://localhost.dinamico.com.br:81", result);
    }

    @Test
    public void mappingThatTryToGluePathOnDomain1()
    {
        UrlReplacer urlReplacer = new UrlReplacer();
        urlReplacer.addMapping("http://localhost.dinamico.com.br/tes/", "http://localhost.dinamico.com.br");
        String result = urlReplacer.replace("http://localhost.dinamico.com.br/tes/te.html");
        Assert.assertEquals("http://localhost.dinamico.com.br/te.html", result);
    }

    @Test
    public void urlWithoutPortOnMappingAndWithPortOnReplacingText2()
    {
        UrlReplacer urlReplacer = new UrlReplacer();
        urlReplacer.addMapping("http://localhost.dinamico.com.br", "http://localhost.dinamico.com.br:81");
        String result = urlReplacer.replace("http:\\/\\/localhost.dinamico.com.br:89");
        Assert.assertEquals("http:\\/\\/localhost.dinamico.com.br:89", result);
    }

    @Test
    public void urlWithPortOnMappingLikePortInitialNumberOnReplacingText2()
    {
        UrlReplacer urlReplacer = new UrlReplacer();
        urlReplacer.addMapping("http://localhost.dinamico.com.br:80", "http://localhost.dinamico.com.br:81");
        String result = urlReplacer.replace("http:\\/\\/localhost.dinamico.com.br:8080");
        Assert.assertEquals("http:\\/\\/localhost.dinamico.com.br:8080", result);
    }

    @Test
    public void urlBasicReplacing2()
    {
        UrlReplacer urlReplacer = new UrlReplacer();
        urlReplacer.addMapping("http://localhost.dinamico.com.br:80", "http://localhost.dinamico.com.br:81");
        String result = urlReplacer.replace("http:\\/\\/localhost.dinamico.com.br:80");
        Assert.assertEquals("http:\\/\\/localhost.dinamico.com.br:81", result);
    }

    @Test
    public void mappingThatTryToGluePathOnDomain2()
    {
        UrlReplacer urlReplacer = new UrlReplacer();
        urlReplacer.addMapping("http://localhost.dinamico.com.br/tes/", "http://localhost.dinamico.com.br");
        String result = urlReplacer.replace("http:\\/\\/localhost.dinamico.com.br\\/tes\\/te.html");
        Assert.assertEquals("http:\\/\\/localhost.dinamico.com.br\\/te.html", result);
    }

    @Test
    public void urlWithoutPortOnMappingAndWithPortOnReplacingText3()
    {
        UrlReplacer urlReplacer = new UrlReplacer();
        urlReplacer.addMapping("http://localhost.dinamico.com.br", "http://localhost.dinamico.com.br:81");
        String result = urlReplacer.replace("http%3A%2F%2Flocalhost.dinamico.com.br%3A89");
        Assert.assertEquals("http%3A%2F%2Flocalhost.dinamico.com.br%3A89", result);
    }

    @Test
    public void urlWithPortOnMappingLikePortInitialNumberOnReplacingText3()
    {
        UrlReplacer urlReplacer = new UrlReplacer();
        urlReplacer.addMapping("http://localhost.dinamico.com.br:80", "http://localhost.dinamico.com.br:81");
        String result = urlReplacer.replace("http%3A%2F%2Flocalhost.dinamico.com.br%3A8080");
        Assert.assertEquals("http%3A%2F%2Flocalhost.dinamico.com.br%3A8080", result);
    }

    @Test
    public void urlBasicReplacing()
    {
        UrlReplacer urlReplacer = new UrlReplacer();
        urlReplacer.addMapping("http://localhost.dinamico.com.br:80", "http://localhost.dinamico.com.br:81");
        String result = urlReplacer.replace("http%3A%2F%2Flocalhost.dinamico.com.br%3A80");
        Assert.assertEquals("http%3A%2F%2Flocalhost.dinamico.com.br%3A81", result);
    }

    @Test
    public void mappingThatTryToGluePathOnDomain3()
    {
        UrlReplacer urlReplacer = new UrlReplacer();
        urlReplacer.addMapping("http://localhost.dinamico.com.br/tes/", "http://localhost.dinamico.com.br");
        String result = urlReplacer.replace("http%3A%2F%2Flocalhost.dinamico.com.br%2Ftes%2Fte.html");
        Assert.assertEquals("http%3A%2F%2Flocalhost.dinamico.com.br%2Fte.html", result);
    }

}
