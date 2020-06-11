package br.com.httpmock.models;

import java.util.Arrays;

public class Binary
{
    private byte[] binary;

    public Binary(byte[] binary)
    {
        super();
        this.binary = binary;
    }

    public byte[] getBinary()
    {
        return binary;
    }

    @Override
    public int hashCode()
    {
        final int prime  = 31;
        int       result = 1;
        result = prime * result + Arrays.hashCode(binary);
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Binary other = (Binary) obj;
        if (!Arrays.equals(binary, other.binary))
            return false;
        return true;
    }

    @Override
    public String toString()
    {
        return "BinaryBody [" + Arrays.toString(binary) + "]";
    }

}
