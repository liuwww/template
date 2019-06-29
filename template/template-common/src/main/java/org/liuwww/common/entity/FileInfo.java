package org.liuwww.common.entity;

import java.io.File;

public class FileInfo
{
    private String fileName;

    private File file;

    private String contentType;

    public FileInfo()
    {

    }

    public FileInfo(File file, String fileName, String contentType)
    {
        super();
        this.fileName = fileName;
        this.file = file;
        this.contentType = contentType;
    }

    public String getFileName()
    {
        return fileName;
    }

    public void setFileName(String fileName)
    {
        this.fileName = fileName;
    }

    public File getFile()
    {
        return file;
    }

    public void setFile(File file)
    {
        this.file = file;
    }

    public String getContentType()
    {
        return contentType;
    }

    public void setContentType(String contentType)
    {
        this.contentType = contentType;
    }

}
