package br.com.httpmock.utils;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class FileUtils
{
    private static final FileUtils    INSTANCE          = new FileUtils();
    private final Map<String, String> COMMON_MIME_TYPES = new ConcurrentHashMap<String, String>();

    private FileUtils()
    {
        super();
        COMMON_MIME_TYPES.put("image/jpeg", "jpeg");
        COMMON_MIME_TYPES.put("image/gif", "gif");
        COMMON_MIME_TYPES.put("image/tiff", "tiff");
        COMMON_MIME_TYPES.put("image/png", "png");
        COMMON_MIME_TYPES.put("image/x-icon", "ico");
        COMMON_MIME_TYPES.put("image/svg+xml", "svg");
        COMMON_MIME_TYPES.put("audio/x-aiff", "aiff");
        COMMON_MIME_TYPES.put("video/x-ms-asf", "asf");
        COMMON_MIME_TYPES.put("video/mpeg", "mp2");
        COMMON_MIME_TYPES.put("audio/mpeg", "mp3");
        COMMON_MIME_TYPES.put("video/quicktime", "mov");
        COMMON_MIME_TYPES.put("application/pdf", "pdf");
        COMMON_MIME_TYPES.put("image/webp", "webp");
        COMMON_MIME_TYPES.put("application/javascript", "js");
        COMMON_MIME_TYPES.put("application/json", "json");
        COMMON_MIME_TYPES.put("text/html", "html");
        COMMON_MIME_TYPES.put("stylesheet", "css");
        COMMON_MIME_TYPES.put("text/css", "css");
        COMMON_MIME_TYPES.put("text/javascript", "js");
        COMMON_MIME_TYPES.put("text/plain", "js");
        COMMON_MIME_TYPES.put("image/bmp", "bmp");
        COMMON_MIME_TYPES.put("multipart/form-data", "txt");
        COMMON_MIME_TYPES.put("application/x-www-form-urlencoded", "txt");
        COMMON_MIME_TYPES.put("application/octet-stream", "bin");
    }

    public static String getApplicationRunningPath()
            throws IOException
    {
        try
        {
            String caminho = FileUtils.class.getProtectionDomain()
                    .getCodeSource().getLocation().toURI().getPath();
            if (!caminho.endsWith(".jar"))
            {
                caminho = new File("").toURI().getPath();
            }
            else
            {
                caminho = caminho.substring(0, caminho.lastIndexOf('/') + 1);
            }
            return caminho;
        }
        catch (Throwable e)
        {
            throw new IOException("Erro ao obter caminho de execução da aplicação", e);
        }
    }

    public static List<Parameter> readParameters(String fileName)
            throws IOException
    {
        List<Parameter>   parameters = new ArrayList<Parameter>();
        File              file       = new File(getApplicationRunningPath() + fileName);
        FileInputStream   fis        = new FileInputStream(file);
        InputStreamReader isr        = new InputStreamReader(fis, Constants.UTF8);
        BufferedReader    br         = new BufferedReader(isr);

        String            line       = br.readLine();
        int               lineNumber = 1;
        while (line != null)
        {
            line = line.replaceAll("[\\t ]+", " ").trim();
            if (!line.startsWith(Constants.COMMENTS))
            {
                if (!line.equals(""))
                {
                    int index = line.indexOf(' ');
                    if (index != -1)
                    {
                        String key   = line.substring(0, index).trim();
                        String value = line.substring(index).trim();
                        parameters.add(INSTANCE.new Parameter(key, value, lineNumber));
                    }
                    else
                    {
                        parameters.add(INSTANCE.new Parameter(line.trim(), "", lineNumber));
                    }
                }
            }
            line = br.readLine();
            lineNumber++;
        }
        close(br, isr, fis);

        return parameters;
    }

    private static void close(Closeable... closeables)
    {
        if (closeables != null)
        {
            for (Closeable closeable : closeables)
            {
                try
                {
                    closeable.close();
                }
                catch (Throwable e)
                {
                    // ignore
                }
            }
        }
    }

    public class Parameter
            implements Comparable<Parameter>
    {
        private String key;
        private String value;
        private int    lineNumber;
        private int    sortOrder1;
        private int    sortOrder2;

        public Parameter(String key, String value, int lineNumber)
        {
            super();
            this.key        = key;
            this.value      = value;
            this.lineNumber = lineNumber;
        }

        public String getKey()
        {
            return key;
        }

        public String getValue()
        {
            return value;
        }

        public int getLineNumber()
        {
            return lineNumber;
        }

        public int getSortOrder1()
        {
            return sortOrder1;
        }

        public void setSortOrder1(int sortOrder1)
        {
            this.sortOrder1 = sortOrder1;
        }

        public int getSortOrder2()
        {
            return sortOrder2;
        }

        public void setSortOrder2(int sortOrder2)
        {
            this.sortOrder2 = sortOrder2;
        }

        @Override
        public int compareTo(Parameter p)
        {
            int order1 = this.getSortOrder1() - p.getSortOrder1();
            if (order1 != 0)
            {
                return order1;
            }
            else
            {
                int order2 = this.getSortOrder2() - p.getSortOrder2();
                return order2;
            }
        }

        @Override
        public String toString()
        {
            return "['" + key + "'= '" + value + "']\n";
        }

    }

    public static String determineFileExtension(String txtUri, String mimeType)
    {
        if (mimeType != null)
        {
            String extension = INSTANCE.COMMON_MIME_TYPES.get(mimeType);
            if (extension != null)
            {
                return extension;
            }
            else if (mimeType.contains("json"))
            {
                return "json";
            }
            else if (mimeType.contains("xml"))
            {
                return "xml";
            }
            else if (mimeType.contains("text"))
            {
                return "txt";
            }
        }
        if (txtUri != null)
        {
            String uri  = URI.create(txtUri).getPath();
            int    idx1 = uri.lastIndexOf('/');
            if (idx1 != -1)
            {
                int idx2 = uri.lastIndexOf('.');
                if (idx2 != -1 && idx2 > idx1)
                {
                    return uri.substring(idx2 + 1);
                }
            }
        }
        return "txt";
    }

    public static String getUniqueFileName(String prefix, int httpStatus, URI uri, String id, String timestamp, String extension)
    {
        String pathPart = uri.getPath()
                .replace('/', '-')
                .replace(" ", "");

        pathPart = pathPart.length() <= 1 ? "(root)" : pathPart.replaceAll("[,~:/?#\\[\\]@!\\$&'()*+;=]", "_");
        if (pathPart.length() > 150)
        {
            pathPart = pathPart.substring(0, 150);
        }
        return (/*
                 * timestamp + "-" +
                 */ prefix
                + "-"
                + httpStatus
                + "-"
                + pathPart
                + "-"
                + id
                + "."
                + extension)
                        .replace("--", "-");
    }

    public static void writeToFile(String fileName, byte[] byteArray)
            throws IOException
    {
        try
        {
            writeToFileWithFileChannel(fileName, byteArray);
        }
        catch (Throwable e)
        {
            writeToFileWithStream(fileName, byteArray);
        }
    }

    private static void writeToFileWithFileChannel(String fileName, byte[] byteArray)
            throws IOException
    {
        FileOutputStream     fos  = null;
        FileChannel          fc   = null;
        ByteArrayInputStream bais = null;
        try
        {
            File file = new File(getApplicationRunningPath() + fileName);
            file.getParentFile().mkdirs();
            fos  = new FileOutputStream(file);
            fc   = fos.getChannel();
            bais = new ByteArrayInputStream(byteArray);
            fc.transferFrom(Channels.newChannel(bais), 0, byteArray.length);
        }
        finally
        {
            close(fc, fos, bais);
        }
    }

    private static void writeToFileWithStream(String fileName, byte[] byteArray)
            throws IOException
    {
        FileOutputStream fos = null;
        try
        {
            File file = new File(getApplicationRunningPath() + fileName);
            file.getParentFile().mkdirs();
            fos = new FileOutputStream(file);
            fos.write(byteArray);
            fos.flush();
        }
        finally
        {
            close(fos);
        }
    }

    public static String readFile(String fileName)
            throws IOException
    {
        StringBuffer      sb   = new StringBuffer();
        File              file = new File(getApplicationRunningPath() + fileName);
        FileInputStream   fis  = new FileInputStream(file);
        InputStreamReader isr  = new InputStreamReader(fis, Constants.UTF8);
        BufferedReader    br   = new BufferedReader(isr);

        String            line = br.readLine();
        while (line != null)
        {
            sb.append(line);
            line = br.readLine();
        }
        close(br, isr, fis);

        return sb.toString();
    }

    public static byte[] readBinaryFile(String fileName)
            throws IOException
    {
        File                  file = new File(getApplicationRunningPath() + fileName);
        FileInputStream       fis  = new FileInputStream(file);
        DataInputStream       dis  = new DataInputStream(fis);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int                   dado = dis.read();
        while (dado != -1)
        {
            baos.write(dado);
            dado = dis.read();
        }

        close(dis, fis);
        return baos.toByteArray();
    }

    public static String getFileName(String path)
    {
        if (path == null)
        {
            return null;
        }
        String fileName = path;
        int    idx      = fileName.lastIndexOf('/');
        if (idx != -1)
        {
            fileName = fileName.substring(idx + 1);
        }
        idx = fileName.lastIndexOf('\\');
        if (idx != -1)
        {
            fileName = fileName.substring(idx + 1);
        }
        return fileName;
    }

    public static File[] listDirectoryFiles(String directory)
            throws IOException
    {
        File   dir   = new File(getApplicationRunningPath() + directory);
        File[] files = dir.listFiles();
        if (files == null)
        {
            return new File[0];
        }
        else
        {
            return files;
        }
    }

    public static String getFullPath(String path)
            throws IOException
    {
        return new File(getApplicationRunningPath() + path).getCanonicalPath();
    }
}
