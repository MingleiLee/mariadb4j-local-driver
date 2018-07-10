package com.jeedsoft.marialocal.util;

public class StringUtil
{
    public static boolean isEmpty(String s)
    {
        return s == null || s.trim().length() == 0;
    }

    public static boolean isEmpty(StringBuilder sb)
    {
        if (sb != null) {
            for (int i = sb.length() - 1; i >= 0; --i) {
                if (!Character.isWhitespace(sb.charAt(i))) {
                    return false;
                }
            }
        }
        return true;
    }

    public static boolean isEmpty(Object o)
    {
        if (o == null) {
            return true;
        }
        else if (o instanceof String) {
            return isEmpty((String)o);
        }
        else if (o instanceof StringBuilder) {
            return isEmpty((StringBuilder)o);
        }
        else {
            return false;
        }
    }
    
    public static String join(Object[] items)
    {
        return join(items, ", ", false, null, null, null);
    }
    
    public static String join(Object[] items, String joiner)
    {
        return join(items, joiner, false, null, null, null);
    }
    
    public static String joinIgnoreEmpty(Object[] items, String joiner)
    {
        return join(items, joiner, true, null, null, null);
    }
    
    public static String join(Object[] items, String joiner, String defaultValue)
    {
        return join(items, joiner, false, defaultValue, null, null);
    }
    
    public static String join(Object[] items, String joiner, String defaultValue, String quote)
    {
        return join(items, joiner, false, defaultValue, quote, quote);
    }

    public static String join(Object[] items, String joiner, String defaultValue, String quote1, String quote2)
    {
        return join(items, joiner, false, defaultValue, quote1, quote2);
    }
    
    public static String join(Object[] items, String joiner, boolean ignoreEmpty, String defaultValue,
            String quote1, String quote2)
    {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < items.length; ++i) {
            if (ignoreEmpty && isEmpty(items[i])) {
                continue;
            }
            if (i > 0) {
                sb.append(joiner);
            }
            Object item = items[i] == null ? defaultValue : items[i];
            if (quote1 != null) {
                sb.append(quote1);
            }
            sb.append(item);
            if (quote2 != null) {
                sb.append(quote2);
            }
        }
        return sb.toString();
    }
    
    public static String join(Iterable<?> items)
    {
        return join(items, ", ", null, null);
    }
    
    public static String join(Iterable<?> items, String joiner)
    {
        return join(items, joiner, null, null);
    }
    
    public static String join(Iterable<?> items, String joiner, String defaultValue)
    {
        return join(items, joiner, defaultValue, null);
    }
    
    public static String join(Iterable<?> items, String joiner, String defaultValue, String quote)
    {
        return join(items, joiner, defaultValue, quote, quote);
    }
    
    public static String join(Iterable<?> items, String joiner, String defaultValue, String quote1, String quote2)
    {
        StringBuilder sb = new StringBuilder();
        boolean isFirst = true;
        for (Object item: items) {
            if (isFirst) {
                isFirst = false;
            }
            else {
                sb.append(joiner);
            }
            if (quote1 != null) {
                sb.append(quote1);
            }
            sb.append(item == null ? defaultValue : item);
            if (quote2 != null) {
                sb.append(quote2);
            }
        }
        return sb.toString();
    }

    public static String join(String item, int count)
    {
        return join(item, count, ", ");
    }

    public static String join(String item, int count, String joiner)
    {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < count; ++i) {
            if (i > 0) {
                sb.append(joiner);
            }
            sb.append(item);
        }
        return sb.toString();
    }
}
