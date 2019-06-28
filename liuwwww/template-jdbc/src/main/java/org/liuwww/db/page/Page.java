package org.liuwww.db.page;

import java.io.Serializable;
import java.util.List;

public class Page implements Serializable
{
    private static final long serialVersionUID = 1L;

    /**
     * 页码，从1开始
     */
    private int pageNum = 1;

    /**
     * 页面大小
     */
    private int pageSize = 10;

    /**
     * 总数
     */
    private long total;

    /**
     * 总页数
     */
    private int pages;

    private List<?> rows;

    /**
     * 起始行
     */
    private int startRow;

    /**
     * 末行
     */
    private int endRow;

    /**
     * 包含count查询
     */
    private boolean count = true;

    /**
     * 排序字段
     */
    private String sortName;;

    /**
     * asc ,desc
     */
    private String sortOrder;

    /**
     * 第一页
     */
    private int firstPage;

    /**
     * 前一页
     */
    private int prePage;

    /**
     * 下一页
     */
    private int nextPage;

    /**
     * 
     * 最后一页
     */
    private int lastPage;

    /**
     * 是否为第一页
     */
    private boolean isFirstPage = false;

    /**
     * 是否为最后一页
     */
    private boolean isLastPage = false;

    /**
     * 是否有前一页
     */
    private boolean hasPreviousPage = false;

    /**
     * 是否有下一页
     */
    private boolean hasNextPage = false;

    /**
     * 导航页码数
     */
    private int navigatePages = 3;

    /**
     * 所有导航页号
     */
    private int[] navigatepageNums;

    public Page()
    {
        calculateStartAndEndRow();
    }

    public Page(int pageNum, int pageSize)
    {
        this(pageNum, pageSize, true);
    }

    public Page(int pageNum, int pageSize, boolean count)
    {
        this.pageNum = pageNum;
        this.pageSize = pageSize;
        this.count = count;
        calculateStartAndEndRow();
    }

    public int getPages()
    {
        return pages;
    }

    public void setPages(int pages)
    {
        this.pages = pages;
    }

    public int getEndRow()
    {
        return endRow;
    }

    public void setEndRow(int endRow)
    {
        this.endRow = endRow;
    }

    public int getPageNum()
    {
        return pageNum;
    }

    public void setPageNum(int pageNum)
    {
        this.pageNum = (pageNum <= 0) ? 1 : pageNum;
    }

    public int getPageSize()
    {
        return pageSize;
    }

    public void setPageSize(int pageSize)
    {
        this.pageSize = pageSize;
    }

    public int getStartRow()
    {
        return startRow;
    }

    public void setStartRow(int startRow)
    {
        this.startRow = startRow;
    }

    public long getTotal()
    {
        return total;
    }

    public void setTotal(long total)
    {
        this.total = total;
        if (total == -1)
        {
            pages = 1;
            return;
        }
        if (pageSize > 0)
        {
            pages = (int) (total / pageSize + ((total % pageSize == 0) ? 0 : 1));
        }
        else
        {
            pages = 0;
        }
        // 分页合理化，针对不合理的页码自动处理
        if (pageNum > pages)
        {
            pageNum = pages;
            calculateStartAndEndRow();
        }
        // 计算导航页
        calcNavigatepageNums();
        // 计算前后页，第一页，最后一页
        calcPage();
        // 判断页面边界
        judgePageBoudary();
    }

    /**
     * 计算起止行号
     */
    private void calculateStartAndEndRow()
    {
        this.startRow = this.pageNum > 0 ? (this.pageNum - 1) * this.pageSize : 0;
        this.endRow = this.startRow + this.pageSize * (this.pageNum > 0 ? 1 : 0);
        this.startRow++;
    }

    public boolean isCount()
    {
        return this.count;
    }

    public void setCount(boolean count)
    {
        this.count = count;
    }

    public String getSortName()
    {
        return sortName;
    }

    public void setSortName(String sortName)
    {
        this.sortName = sortName;
    }

    public String getSortOrder()
    {
        return sortOrder;
    }

    public void setSortOrder(String sortOrder)
    {
        this.sortOrder = sortOrder;
    }

    /**
     * 设置页码
     *
     * @param pageNum
     * @return
     */
    public void pageNum(int pageNum)
    {
        this.pageNum = (pageNum <= 0) ? 1 : pageNum;
    }

    /**
     * 设置页面大小
     *
     * @param pageSize
     * @return
     */
    public void pageSize(int pageSize)
    {
        this.pageSize = pageSize;
        calculateStartAndEndRow();
    }

    /**
     * 是否执行count查询
     *
     * @param count
     * @return
     */
    public void count(Boolean count)
    {
        this.count = count;
    }

    public List<?> getRows()
    {
        return rows;
    }

    public void setRows(List<?> rows)
    {
        this.rows = rows;
    }

    /**
     * 计算导航页
     */
    private void calcNavigatepageNums()
    {
        // 当总页数小于或等于导航页码数时
        if (pages <= navigatePages)
        {
            navigatepageNums = new int[pages];
            for (int i = 0; i < pages; i++)
            {
                navigatepageNums[i] = i + 1;
            }
        }
        else
        { // 当总页数大于导航页码数时
            navigatepageNums = new int[navigatePages];
            int startNum = pageNum - navigatePages / 2;
            int endNum = pageNum + navigatePages / 2;

            if (startNum < 1)
            {
                startNum = 1;
                // (最前navigatePages页
                for (int i = 0; i < navigatePages; i++)
                {
                    navigatepageNums[i] = startNum++;
                }
            }
            else if (endNum > pages)
            {
                endNum = pages;
                // 最后navigatePages页
                for (int i = navigatePages - 1; i >= 0; i--)
                {
                    navigatepageNums[i] = endNum--;
                }
            }
            else
            {
                // 所有中间页
                for (int i = 0; i < navigatePages; i++)
                {
                    navigatepageNums[i] = startNum++;
                }
            }
        }
    }

    /**
     * 计算前后页，第一页，最后一页
     */
    private void calcPage()
    {
        if (navigatepageNums != null && navigatepageNums.length > 0)
        {
            firstPage = 1;
            lastPage = pages;
            if (pageNum > 1)
            {
                prePage = pageNum - 1;
            }
            if (pageNum < pages)
            {
                nextPage = pageNum + 1;
            }
        }
    }

    /**
     * 判定页面边界
     */
    private void judgePageBoudary()
    {
        isFirstPage = pageNum == 1;
        isLastPage = pageNum == pages;
        hasPreviousPage = pageNum > 1;
        hasNextPage = pageNum < pages;
    }

    public int getFirstPage()
    {
        return firstPage;
    }

    public void setFirstPage(int firstPage)
    {
        this.firstPage = firstPage;
    }

    public int getPrePage()
    {
        return prePage;
    }

    public void setPrePage(int prePage)
    {
        this.prePage = prePage;
    }

    public int getNextPage()
    {
        return nextPage;
    }

    public void setNextPage(int nextPage)
    {
        this.nextPage = nextPage;
    }

    public int getLastPage()
    {
        return lastPage;
    }

    public void setLastPage(int lastPage)
    {
        this.lastPage = lastPage;
    }

    public boolean getIsFirstPage()
    {
        return isFirstPage;
    }

    public void setIsFirstPage(boolean isFirstPage)
    {
        this.isFirstPage = isFirstPage;
    }

    public boolean getIsLastPage()
    {
        return isLastPage;
    }

    public void setLastPage(boolean isLastPage)
    {
        this.isLastPage = isLastPage;
    }

    public boolean getHasPreviousPage()
    {
        return hasPreviousPage;
    }

    public void setHasPreviousPage(boolean hasPreviousPage)
    {
        this.hasPreviousPage = hasPreviousPage;
    }

    public boolean getHasNextPage()
    {
        return hasNextPage;
    }

    public void setHasNextPage(boolean hasNextPage)
    {
        this.hasNextPage = hasNextPage;
    }

    public int getNavigatePages()
    {
        return navigatePages;
    }

    public void setNavigatePages(int navigatePages)
    {
        this.navigatePages = navigatePages;
    }

    public int[] getNavigatepageNums()
    {
        return navigatepageNums;
    }

    public void setNavigatepageNums(int[] navigatepageNums)
    {
        this.navigatepageNums = navigatepageNums;
    }

}
