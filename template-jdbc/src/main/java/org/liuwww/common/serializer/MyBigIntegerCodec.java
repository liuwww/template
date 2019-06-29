package org.liuwww.common.serializer;

import java.io.IOException;
import java.lang.reflect.Type;
import java.math.BigInteger;

import com.alibaba.fastjson.serializer.BigIntegerCodec;
import com.alibaba.fastjson.serializer.JSONSerializer;
import com.alibaba.fastjson.serializer.SerializeWriter;
import com.alibaba.fastjson.serializer.SerializerFeature;

public class MyBigIntegerCodec extends BigIntegerCodec
{
    public final static MyBigIntegerCodec instance = new MyBigIntegerCodec();

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

        BigInteger val = (BigInteger) object;
        if (Constants.isBigger(val))
        {
            out.writeString(val.toString());
        }
        else
        {
            out.write(val.toString());
        }
    }
}
