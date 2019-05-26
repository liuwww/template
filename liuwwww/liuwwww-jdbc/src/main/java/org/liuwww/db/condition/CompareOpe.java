package org.liuwww.db.condition;

/**
 * lt:< <br/>
 * gt:> <br/>
 * ne:!= <br/>
 * eq:== <br/>
 * le:<= <br/>
 * ge:>=<br/>
 * @author lwww 2017年1月10日下午5:27:40
 */
public enum CompareOpe {

    lt("<"), gt(">"), ne("!="), eq("="), le("<="), ge(">="), like("like"), notNull("not null");
    private String val;

    private CompareOpe(String val)
    {
        this.val = val;
    }

    public String getVal()
    {
        return val;
    }

}
