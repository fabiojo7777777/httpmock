package br.com.httpmock.utils;

public class CommandLineUtils
{
    @SuppressWarnings("unused")
    private static final CommandLineUtils INSTANCE = new CommandLineUtils();

    private CommandLineUtils()
    {
        super();
    }

    public static String getParameter(String parameterName, String[] args)
    {
        if (args == null || parameterName == null)
        {
            return null;
        }
        String fullParameterName = Constants.PARAMETER_PREFIX + parameterName;
        for (int i = 0, size = args.length; i < size; i++)
        {
            if (fullParameterName.equals(args[i]))
            {
                if (i + 1 < size && args[i + 1] != null && !args[i + 1].startsWith(Constants.PARAMETER_PREFIX))
                {
                    return args[i + 1];
                }
                else
                {
                    return null;
                }
            }
        }
        return null;
    }
}
