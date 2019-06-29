package org.liuwww.common.serializer;

import java.io.IOException;
import java.lang.reflect.Type;

import com.alibaba.fastjson.serializer.JSONSerializer;
import com.alibaba.fastjson.serializer.LongCodec;
import com.alibaba.fastjson.serializer.SerializeWriter;
import com.alibaba.fastjson.serializer.SerializerFeature;

public class MyLongCodec extends LongCodec
{
    public static LongCodec instance = new MyLongCodec();

    @Override
    public void write(JSONSerializer serializer, Object object, Object fieldName, Type fieldType, int features)
            throws IOException
    {
        SerializeWriter out = serializer.getWriter();

        if (object == null)
        {
            if (out.isEnabled(SerializerFeature.WriteNullNumberAsZero))
            {
                out.write('0');
            }
            else
            {
                out.writeNull();
            }
            return;
        }
        Long value = (Long) object;
        if (Constants.isBigger(value))
        {
            out.writeString(object.toString());
        }
        else
        {
            out.writeLong(value);
        }
        if (serializer.isEnabled(SerializerFeature.WriteClassName))
        {
            if (value <= Integer.MAX_VALUE && value >= Integer.MIN_VALUE)
            {
                if (fieldType != Long.class)
                {
                    out.write('L');
                }
            }
        }
    }
}
