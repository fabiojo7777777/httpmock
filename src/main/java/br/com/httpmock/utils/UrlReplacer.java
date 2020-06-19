package br.com.httpmock.utils;

import org.apache.hc.core5.net.URLEncodedUtils;

public class UrlReplacer
{
    private boolean   isSearchCompiled = false;

    private String[]  fromUrlArray     = new String[0];
    private String[]  toUrlArray       = new String[0];
    private boolean[] isPlainUrl       = new boolean[0];
    private boolean[] isJsonEncoded    = new boolean[0];
    private boolean[] isUrlEncoded     = new boolean[0];
    private int[]     newHostLength    = new int[0];

    private int[][]   indexesBitmask;
    private int[]     lastCharacterBitMask;
    private int       maxFromUrlLength;

    public String replace(String text)
    {
        compileSearch();

        int           matchedIndexesBitmask          = 0xffffffff;               // all indexes matched at the beginning
        int           matchedLastCharacterBitMask    = 0x00000000;               // all lengths not matched at the beginning
        StringBuilder sb                             = new StringBuilder(text);
        int           size                           = sb.length();

        int           newMatchedIndexesBitMask       = 0;
        int           newMatchedLastCharacterBitMask = 0;
        int           newFinalMask                   = 0;

        int           matchedLength                  = 0;
        int           characterIndex                 = 0;                        // Integer.MAX_VALUE;
        int           i                              = 0;
        int           searchStartIndex               = 0;                        // -1;
        int[]         finalMaskHistory               = new int[maxFromUrlLength];
        int           characterIndexAdvance          = 0;

        // debugln("Search text: " + sb);
        for (i = 0; i < size; i++)
        {
            if (characterIndex >= indexesBitmask.length)
            {
                characterIndex   = 0;
                i                = searchStartIndex;
                searchStartIndex = i + 1;
                finalMaskHistory = new int[maxFromUrlLength];
                // debugln(String.format("IX IX CH => %32s %32s %32s %3s", "MATCHED CHARACTERS
                // MASK", "MATCHED LAST CHARACTER", "FINAL MASK", "LEN"));
                continue;
            }

            matchedLength = i - searchStartIndex + 1;

            char characterValue = (char) (sb.charAt(i) & 0xff); // 256 ASCII CHARACTERS
            newMatchedIndexesBitMask            = indexesBitmask[characterIndex][characterValue] & matchedIndexesBitmask;
            newMatchedLastCharacterBitMask      = lastCharacterBitMask[matchedLength] | matchedLastCharacterBitMask;
            newFinalMask                        = newMatchedIndexesBitMask & newMatchedLastCharacterBitMask;

            finalMaskHistory[matchedLength - 1] = newFinalMask;
            // debugln(String.format("%2d, %2d %1s => %32s %32s %32s %3d", i, (int)
            // characterIndex, characterValue, binary(newMatchedIndexesBitMask),
            // binary(newMatchedLastCharacterBitMask), binary(newFinalMask),
            // matchedLength));
            if (newMatchedIndexesBitMask == 0)
            {
                characterIndexAdvance = 0;
                exit1: for (int j = matchedLength - 1; j >= 0; j--)
                {
                    newFinalMask = finalMaskHistory[j];
                    for (int k = 0; k < 32; k++)
                    {
                        int indexMask = 1 << k;
                        if ((newFinalMask & indexMask) > 0)
                        {
                            sb.replace(searchStartIndex, searchStartIndex + fromUrlArray[k].length(), toUrlArray[k]);
                            characterIndexAdvance = toUrlArray[k].length() - 1;
                            // APPEND A '/' AFTER THE HOST IF IT IS NOT PRESENT
                            if (isPlainUrl[k])
                            {
                                if (searchStartIndex + newHostLength[k] >= sb.length()
                                        || sb.charAt(searchStartIndex + newHostLength[k]) != '/')
                                {
                                    sb.insert(searchStartIndex + newHostLength[k], '/');
                                    characterIndexAdvance++;
                                }
                            }
                            else if (isJsonEncoded[k])
                            {
                                if (searchStartIndex + newHostLength[k] + 1 >= sb.length()
                                        || sb.charAt(searchStartIndex + newHostLength[k]) != '\\'
                                        || sb.charAt(searchStartIndex + newHostLength[k] + 1) != '/')
                                {
                                    sb.insert(searchStartIndex + newHostLength[k], "\\/");
                                    characterIndexAdvance += 2;
                                }
                            }
                            else if (isUrlEncoded[k])
                            {
                                if (searchStartIndex + newHostLength[k] + 2 >= sb.length()
                                        || sb.charAt(searchStartIndex + newHostLength[k]) != '%'
                                        || sb.charAt(searchStartIndex + newHostLength[k] + 1) != '2'
                                        || sb.charAt(searchStartIndex + newHostLength[k] + 2) != 'F')
                                {
                                    sb.insert(searchStartIndex + newHostLength[k], "%2F");
                                    characterIndexAdvance += 3;
                                }
                            }
                            size = sb.length();
                            // debugln("bateu o histórico " + (j + 1) + " com mapeamento de índice " + k + "
                            // [" + fromUrlArray[k] + "]");
                            // debugln("New search text: " + toUrlArray[k] + " " + sb);
                            break exit1;
                        }
                    }
                }
                characterIndex   = 0;
                i                = searchStartIndex + characterIndexAdvance;
                searchStartIndex = i + 1;
                finalMaskHistory = new int[maxFromUrlLength];
                // debugln(String.format("IX IX CH => %32s %32s %32s %3s", "MATCHED CHARACTERS
                // MASK", "MATCHED LAST CHARACTER", "FINAL MASK", "LEN"));
                continue;
            }
            characterIndex++;
        }
        exit2: for (int j = matchedLength - 1; j >= 0; j--)
        {
            newFinalMask = finalMaskHistory[j];
            for (int k = 0; k < 32; k++)
            {
                int indexMask = 1 << k;
                if ((newFinalMask & indexMask) > 0)
                {
                    sb.replace(searchStartIndex, searchStartIndex + fromUrlArray[k].length(), toUrlArray[k]);
                    characterIndexAdvance = toUrlArray[k].length() - 1;
                    // APPEND A '/' AFTER THE HOST IF IT IS NOT PRESENT
                    if (isPlainUrl[k])
                    {
                        if (searchStartIndex + newHostLength[k] >= sb.length()
                                || sb.charAt(searchStartIndex + newHostLength[k]) != '/')
                        {
                            sb.insert(searchStartIndex + newHostLength[k], '/');
                            characterIndexAdvance++;
                        }
                    }
                    else if (isJsonEncoded[k])
                    {
                        if (searchStartIndex + newHostLength[k] + 1 >= sb.length()
                                || sb.charAt(searchStartIndex + newHostLength[k]) != '\\'
                                || sb.charAt(searchStartIndex + newHostLength[k] + 1) != '/')
                        {
                            sb.insert(searchStartIndex + newHostLength[k], "\\/");
                            characterIndexAdvance += 2;
                        }
                    }
                    else if (isUrlEncoded[k])
                    {
                        if (searchStartIndex + newHostLength[k] + 2 >= sb.length()
                                || sb.charAt(searchStartIndex + newHostLength[k]) != '%'
                                || sb.charAt(searchStartIndex + newHostLength[k] + 1) != '2'
                                || sb.charAt(searchStartIndex + newHostLength[k] + 2) != 'F')
                        {
                            sb.insert(searchStartIndex + newHostLength[k], "%2F");
                            characterIndexAdvance += 3;
                        }
                    }
                    size = sb.length();
                    // debugln("bateu o histórico " + (j + 1) + " com mapeamento de índice " + k + "
                    // [" + fromUrlArray[k] + "]");
                    // debugln("New search text: " + toUrlArray[k] + " " + sb);
                    break exit2;
                }
            }
        }
        return sb.toString();
    }

//    private String binary(int bitmask)
//    {
//        String txt = "0000000000000000000000000000000000000000000000000000000000000000" + Integer.toBinaryString(bitmask);
//        return txt.substring(txt.length() - 32, txt.length());
//    }

    private synchronized void compileSearch()
    {
        if (!isSearchCompiled)
        {
            maxFromUrlLength = getMaxFromUrlLength();

            indexesBitmask   = new int[maxFromUrlLength][256]; // 256 ASCII CHARACTERS
            for (int fromIndex = 0, size1 = fromUrlArray.length; fromIndex < size1; fromIndex++)
            {
                String fromUrl = fromUrlArray[fromIndex];
                int    mask    = (1 << fromIndex);
                for (int characterIndex = 0, size2 = fromUrl.length(); characterIndex < size2; characterIndex++)
                {
                    char characterValue = (char) (fromUrl.charAt(characterIndex) & 0xff);// 256 ASCII CHARACTERS
                    indexesBitmask[characterIndex][characterValue] |= mask;
                }
            }

            lastCharacterBitMask = new int[maxFromUrlLength + 1];
            for (int fromIndex = 0, size1 = fromUrlArray.length; fromIndex < size1; fromIndex++)
            {
                String fromUrl = fromUrlArray[fromIndex];
                int    mask    = (1 << fromIndex);
                lastCharacterBitMask[fromUrl.length()] |= mask;
            }

            isSearchCompiled = true;
        }
    }

    private int getMaxFromUrlLength()
    {
        int maxFromUrlLength = 0;
        for (String fromUrl : fromUrlArray)
        {
            if (fromUrl.length() > maxFromUrlLength)
            {
                maxFromUrlLength = fromUrl.length();
            }
        }
        return maxFromUrlLength;
    }

    public synchronized void addMapping(String fromUrl, String toUrl)
    {
        if (!isSearchCompiled)
        {
            String fromUrlJsonEncoded = fromUrl.replace("/", "\\/");
            String fromUrlUrlEncoded  = URLEncodedUtils.format(
                    URLEncodedUtils.parse(fromUrl, Constants.UTF8_CHARSET),
                    Constants.UTF8_CHARSET);
            fromUrlArray                          = addElements(fromUrlArray, 3);
            fromUrlArray[fromUrlArray.length - 1] = fromUrlJsonEncoded;
            fromUrlArray[fromUrlArray.length - 2] = fromUrlUrlEncoded;
            fromUrlArray[fromUrlArray.length - 3] = fromUrl;

            String toUrlJsonEncoded = toUrl.replace("/", "\\/");
            String toUrlUrlEncoded  = URLEncodedUtils.format(
                    URLEncodedUtils.parse(toUrl, Constants.UTF8_CHARSET),
                    Constants.UTF8_CHARSET);
            toUrlArray                              = addElements(toUrlArray, 3);
            toUrlArray[toUrlArray.length - 1]       = toUrlJsonEncoded;
            toUrlArray[toUrlArray.length - 2]       = toUrlUrlEncoded;
            toUrlArray[toUrlArray.length - 3]       = toUrl;

            isJsonEncoded                           = addElements(isJsonEncoded, 3);
            isJsonEncoded[isJsonEncoded.length - 1] = true;
            isJsonEncoded[isJsonEncoded.length - 2] = false;
            isJsonEncoded[isJsonEncoded.length - 3] = false;

            isUrlEncoded                            = addElements(isUrlEncoded, 3);
            isUrlEncoded[isUrlEncoded.length - 1]   = false;
            isUrlEncoded[isUrlEncoded.length - 2]   = true;
            isUrlEncoded[isUrlEncoded.length - 3]   = false;

            isPlainUrl                              = addElements(isPlainUrl, 3);
            isPlainUrl[isPlainUrl.length - 1]       = false;
            isPlainUrl[isPlainUrl.length - 2]       = false;
            isPlainUrl[isPlainUrl.length - 3]       = true;

            int newHostJsonEncodedLength = HttpUtils.extractHost(toUrl).replace("/", "\\/").length();
            int newHostUrlEncodedLenght  = URLEncodedUtils.format(
                    URLEncodedUtils.parse(HttpUtils.extractHost(toUrl), Constants.UTF8_CHARSET),
                    Constants.UTF8_CHARSET).length();
            int newHostUrlLength         = HttpUtils.extractHost(toUrl).length();
            newHostLength                           = addElements(newHostLength, 3);
            newHostLength[newHostLength.length - 1] = newHostJsonEncodedLength;
            newHostLength[newHostLength.length - 2] = newHostUrlEncodedLenght;
            newHostLength[newHostLength.length - 3] = newHostUrlLength;
        }
        else
        {
            throw new UnsupportedOperationException("Mapeamentos só podem ser adicionados enquanto a busca não está compilada");
        }
    }

    private String[] addElements(String[] array, int amount)
    {
        String[] newArray = new String[array.length + amount];
        System.arraycopy(array, 0, newArray, 0, array.length);
        return newArray;
    }

    private int[] addElements(int[] array, int amount)
    {
        int[] newArray = new int[array.length + amount];
        System.arraycopy(array, 0, newArray, 0, array.length);
        return newArray;
    }

    private boolean[] addElements(boolean[] array, int amount)
    {
        boolean[] newArray = new boolean[array.length + amount];
        System.arraycopy(array, 0, newArray, 0, array.length);
        return newArray;
    }

//    public static void debugln(Object obj)
//    {
//        System.out.println(obj);
//    }

}
