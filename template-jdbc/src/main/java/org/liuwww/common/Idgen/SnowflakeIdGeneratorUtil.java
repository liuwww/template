package org.liuwww.common.Idgen;

public class SnowflakeIdGeneratorUtil
{
    private static SnowflakeIdGenerator generator = null;

    static
    {
        generator = new SnowflakeIdGenerator(0, 0);
    }

    public static IdGenerator getIdGenerator()
    {
        return generator;
    }

    public static long nextId()
    {
        return generator.nextId();
    }

    public static String nextStringId()
    {
        return String.valueOf(generator.nextId());
    }

    public static IdInfo getIdInfo(long id)
    {
        return generator.getIdInfo(id);
    }

    public static void main(String[] args)
    {
        System.out.println(nextStringId());
        long id = nextId();
        System.out.println(getIdInfo(id));
    }
}
