package org.liuwww.common.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.configuration.reloading.FileChangedReloadingStrategy;
import org.liuwww.common.execption.SysException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class XmlReaderUtil
{
    private static final Logger logger = LoggerFactory.getLogger(XmlReaderUtil.class);

    private Map<String, XMLConfiguration> sqlPropMap;

    private static XmlReaderUtil instance = new XmlReaderUtil();

    private XmlReaderUtil()
    {
        sqlPropMap = new HashMap<String, XMLConfiguration>();
    }

    private XMLConfiguration getTheConfig(String path)
    {
        return instance.sqlPropMap.get(path);
    }

    private void setTheConfig(String path, XMLConfiguration config)
    {
        instance.sqlPropMap.put(path, config);
    }

    private static Configuration getConfig(String file)
    {
        XMLConfiguration config = instance.getTheConfig(file);
        if (config == null)
        {
            synchronized (XmlReaderUtil.class)
            {
                if (config == null)
                {
                    try
                    {
                        config = new XMLConfiguration();
                        config.setEncoding("utf-8");
                        config.setDelimiterParsingDisabled(true);
                        config.load(file + ".xml");
                        config.setReloadingStrategy(new FileChangedReloadingStrategy());
                        instance.setTheConfig(file, config);
                    }
                    catch (ConfigurationException e)
                    {
                        if (logger.isErrorEnabled())
                        {
                            logger.error("sql file load error", e);
                        }
                        throw new SysException("", e);
                    }
                }

            }
        }
        return config;
    }

    public static String getProp(String fileName, String tag)
    {
        Configuration config = getConfig(fileName);
        if (config != null)
        {
            List<Object> list = config.getList(tag);
            if (list.size() > 1)
            {
                return listToString(list);
            }
            else
            {
                return config.getString(tag);
            }

        }
        return null;
    }

    /** 
     * 将存放在sourceFilePath目录下的源文件，打包成fileName名称的zip文件，并存放到zipFilePath路径下 
     * @param sourceFilePath :待压缩的文件路径 
     * @param zipFilePath :压缩后存放路径 
     * @param fileName :压缩后文件的名称 
     * @return 
     */
    public static boolean fileToZip(String sourceFilePath, String zipFilePath, String fileName)
    {
        boolean flag = false;
        File sourceFile = new File(sourceFilePath);
        FileInputStream fis = null;
        BufferedInputStream bis = null;
        FileOutputStream fos = null;
        ZipOutputStream zos = null;

        if (sourceFile.exists() == false)
        {
            System.out.println("待压缩的文件目录：" + sourceFilePath + "不存在.");
        }
        else
        {
            try
            {
                File zipFile = new File(zipFilePath + "/" + fileName + ".zip");
                if (zipFile.exists())
                {
                    System.out.println(zipFilePath + "目录下存在名字为:" + fileName + ".zip" + "打包文件.");
                }
                else
                {
                    File[] sourceFiles = sourceFile.listFiles();
                    if (null == sourceFiles || sourceFiles.length < 1)
                    {
                        System.out.println("待压缩的文件目录：" + sourceFilePath + "里面不存在文件，无需压缩.");
                    }
                    else
                    {
                        fos = new FileOutputStream(zipFile);
                        zos = new ZipOutputStream(new BufferedOutputStream(fos));
                        byte[] bufs = new byte[1024 * 10];
                        for (int i = 0; i < sourceFiles.length; i++)
                        {
                            // 创建ZIP实体，并添加进压缩包
                            ZipEntry zipEntry = new ZipEntry(sourceFiles[i].getName());
                            zos.putNextEntry(zipEntry);
                            // 读取待压缩的文件并写进压缩包里
                            fis = new FileInputStream(sourceFiles[i]);
                            bis = new BufferedInputStream(fis, 1024 * 10);
                            int read = 0;
                            while ((read = bis.read(bufs, 0, 1024 * 10)) != -1)
                            {
                                zos.write(bufs, 0, read);
                            }
                        }
                        flag = true;
                    }
                }
            }
            catch (FileNotFoundException e)
            {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
            catch (IOException e)
            {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
            finally
            {
                // 关闭流
                try
                {
                    if (null != bis)
                        bis.close();
                    if (null != zos)
                        zos.close();
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                    throw new RuntimeException(e);
                }
            }
        }
        return flag;
    }

    public static void delFile(String path, String filename)
    {
        File file = new File(path + "/" + filename);
        if (file.exists() && file.isFile())
            file.delete();
    }

    public static void delDir(String path)
    {
        File dir = new File(path);
        if (dir.exists())
        {
            File[] tmp = dir.listFiles();
            for (int i = 0; i < tmp.length; i++)
            {
                if (tmp[i].isDirectory())
                {
                    delDir(path + "/" + tmp[i].getName());
                }
                else
                {
                    tmp[i].delete();
                }
            }
            dir.delete();
        }
    }

    private static String listToString(List<Object> list)
    {
        StringBuilder sql = new StringBuilder();
        for (Object obj : list)
        {
            sql.append(obj).append(",");
        }
        sql = sql.delete(sql.length() - 1, sql.length());
        return sql.toString();
    }
}
