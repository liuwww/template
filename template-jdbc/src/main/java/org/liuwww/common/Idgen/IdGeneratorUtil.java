package org.liuwww.common.Idgen;

public class IdGeneratorUtil
{
    private static IdGenerator generator = null;

    static
    {
        generator = new IdGenerator(0, 0);
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
