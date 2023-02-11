package org.liuwww.db.condition;

/**
 * lt:< <br/>
 * gt:> <br/>
 * ne:!= <br/>
 * eq:== <br/>
 * le:<= <br/>
 * ge:>=<br/>
 * like: like<br/>
 * notNull: not null <br/>
 * isNull: is null<br/>
 * emptyStr: = ''<br/>
 * in:
 * @author lwww 2017年1月10日下午5:27:40
 */
public enum CompareOpe {

    lt("<", false), gt(">", false), ne("!=", false), eq("=", false), le("<=", false), ge(">=", false), like("like",
            false), notNull("not null", true), isNull("is null", true), emptyStr("=''", true), in("in", false);
    private String val;

    private boolean defaultEffective;

    private CompareOpe(String val, boolean defaultEffective)
    {
        this.val = val;
        this.defaultEffective = defaultEffective;
    }

    public String getVal()
    {
        return val;
    }

    public boolean defaultEffective()
    {
        return defaultEffective;
    }

}
