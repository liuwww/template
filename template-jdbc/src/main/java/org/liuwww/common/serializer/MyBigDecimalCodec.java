package org.liuwww.common.serializer;

import java.io.IOException;
import java.lang.reflect.Type;
import java.math.BigDecimal;

import com.alibaba.fastjson.serializer.BigDecimalCodec;
import com.alibaba.fastjson.serializer.JSONSerializer;
import com.alibaba.fastjson.serializer.SerializeWriter;
import com.alibaba.fastjson.serializer.SerializerFeature;

public class MyBigDecimalCodec extends BigDecimalCodec
{
    public final static MyBigDecimalCodec instance = new MyBigDecimalCodec();

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

        BigDecimal val = (BigDecimal) object;
        if (Constants.isBigger(val))
        {
            out.writeString(val.toString());
        }
        else
        {
            out.write(val.toString());
        }
        if (out.isEnabled(SerializerFeature.WriteClassName) && fieldType != BigDecimal.class && val.scale() == 0)
        {
            out.write('.');
        }
    }

}
