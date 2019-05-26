package org.liuwww.db.test;

import org.liuwww.db.query.Entity;

public class Staff implements Entity<Staff>
{

    private String staffId;

    private String alias;

    // 生日
    private String birthday;

    // 证件号码
    private String certCode;

    // 证件类型
    private String certType;

    // 联系地址
    private String conAddr;

    // 联系电话
    private String conMobile;

    // 手机
    private String conTel;

    // 是否是专业人员
    private String isDis;

    // 是否显示在首页
    private String isDisHome;

    // 员工级别
    private String staffLevel;

    // 是否接单
    private String isOrder;

    // 创建时间
    private String createDate;

    // 组织架构
    private String depId;

    // 邮箱
    private String email;

    private String fancy;

    // MSN
    private String msn;

    // 员工名称
    private String staffName;

    // 密码
    private String password;

    // QQ
    private String qq;

    // 备注
    private String remark;

    // 性别
    private String sex;

    private String wcode;

    private String zfbAccount;

    private String operaStaffId;

    private String sts;

    private String stsDate;

    // 职位
    private String position;

    private String wxId;

    // 学位
    private String degree;

    // 专业领域
    private String proField;

    // 业务领域
    private String busiField;

    // 简历
    private String resume;

    // 证书
    private String certificate;

    // 服务的客户
    private String custs;

    // 合同期限
    private String contractDate;

    // 合同期止
    private String contractEndDate;

    // 工资级别
    private String salaryLevel;

    // 入职时间
    private String joinDate;

    // 董事长奖励
    private String reward;

    // 工龄工资初始值
    private String workAge;

    private String upStaffId;

    // 是否工龄
    private String isWyear;

    // 是否社保
    private String isSos;

    public String getIsWyear()
    {
        return isWyear;
    }

    public void setIsWyear(String isWyear)
    {
        this.isWyear = isWyear;
    }

    public String getIsSos()
    {
        return isSos;
    }

    public void setIsSos(String isSos)
    {
        this.isSos = isSos;
    }

    public String getUpStaffId()
    {
        return upStaffId;
    }

    public void setUpStaffId(String upStaffId)
    {
        this.upStaffId = upStaffId;
    }

    public String getReward()
    {
        return reward;
    }

    public void setReward(String reward)
    {
        this.reward = reward;
    }

    public String getWorkAge()
    {
        return workAge;
    }

    public void setWorkAge(String workAge)
    {
        this.workAge = workAge;
    }

    public String getContractEndDate()
    {
        return contractEndDate;
    }

    public void setContractEndDate(String contractEndDate)
    {
        this.contractEndDate = contractEndDate;
    }

    public String getJoinDate()
    {
        return joinDate;
    }

    public void setJoinDate(String joinDate)
    {
        this.joinDate = joinDate;
    }

    public String getSalaryLevel()
    {
        return salaryLevel;
    }

    public void setSalaryLevel(String salaryLevel)
    {
        this.salaryLevel = salaryLevel;
    }

    public String getContractDate()
    {
        return contractDate;
    }

    public void setContractDate(String contractDate)
    {
        this.contractDate = contractDate;
    }

    public String getCertificate()
    {
        return certificate;
    }

    public void setCertificate(String certificate)
    {
        this.certificate = certificate;
    }

    public String getCusts()
    {
        return custs;
    }

    public void setCusts(String custs)
    {
        this.custs = custs;
    }

    public String getDegree()
    {
        return degree;
    }

    public void setDegree(String degree)
    {
        this.degree = degree;
    }

    public String getProField()
    {
        return proField;
    }

    public void setProField(String proField)
    {
        this.proField = proField;
    }

    public String getBusiField()
    {
        return busiField;
    }

    public void setBusiField(String busiField)
    {
        this.busiField = busiField;
    }

    public String getResume()
    {
        return resume;
    }

    public void setResume(String resume)
    {
        this.resume = resume;
    }

    public String getStaffId()
    {
        return staffId;
    }

    public void setStaffId(String staffId)
    {
        this.staffId = staffId;
    }

    public String getAlias()
    {
        return alias;
    }

    public void setAlias(String alias)
    {
        this.alias = alias;
    }

    public String getBirthday()
    {
        return birthday;
    }

    public void setBirthday(String birthday)
    {
        this.birthday = birthday;
    }

    public String getCertCode()
    {
        return certCode;
    }

    public void setCertCode(String certCode)
    {
        this.certCode = certCode;
    }

    public String getCertType()
    {
        return certType;
    }

    public void setCertType(String certType)
    {
        this.certType = certType;
    }

    public String getConAddr()
    {
        return conAddr;
    }

    public void setConAddr(String conAddr)
    {
        this.conAddr = conAddr;
    }

    public String getConMobile()
    {
        return conMobile;
    }

    public void setConMobile(String conMobile)
    {
        this.conMobile = conMobile;
    }

    public String getConTel()
    {
        return conTel;
    }

    public void setConTel(String conTel)
    {
        this.conTel = conTel;
    }

    public String getIsDis()
    {
        return isDis;
    }

    public void setIsDis(String isDis)
    {
        this.isDis = isDis;
    }

    public String getIsDisHome()
    {
        return isDisHome;
    }

    public void setIsDisHome(String isDisHome)
    {
        this.isDisHome = isDisHome;
    }

    public String getStaffLevel()
    {
        return staffLevel;
    }

    public void setStaffLevel(String staffLevel)
    {
        this.staffLevel = staffLevel;
    }

    public String getIsOrder()
    {
        return isOrder;
    }

    public void setIsOrder(String isOrder)
    {
        this.isOrder = isOrder;
    }

    public String getCreateDate()
    {
        return createDate;
    }

    public void setCreateDate(String createDate)
    {
        this.createDate = createDate;
    }

    public String getDepId()
    {
        return depId;
    }

    public void setDepId(String depId)
    {
        this.depId = depId;
    }

    public String getEmail()
    {
        return email;
    }

    public void setEmail(String email)
    {
        this.email = email;
    }

    public String getFancy()
    {
        return fancy;
    }

    public void setFancy(String fancy)
    {
        this.fancy = fancy;
    }

    public String getMsn()
    {
        return msn;
    }

    public void setMsn(String msn)
    {
        this.msn = msn;
    }

    public String getStaffName()
    {
        return staffName;
    }

    public void setStaffName(String staffName)
    {
        this.staffName = staffName;
    }

    public String getPassword()
    {
        return password;
    }

    public void setPassword(String password)
    {
        this.password = password;
    }

    public String getQq()
    {
        return qq;
    }

    public void setQq(String qq)
    {
        this.qq = qq;
    }

    public String getRemark()
    {
        return remark;
    }

    public void setRemark(String remark)
    {
        this.remark = remark;
    }

    public String getSex()
    {
        return sex;
    }

    public void setSex(String sex)
    {
        this.sex = sex;
    }

    public String getWcode()
    {
        return wcode;
    }

    public void setWcode(String wcode)
    {
        this.wcode = wcode;
    }

    public String getZfbAccount()
    {
        return zfbAccount;
    }

    public void setZfbAccount(String zfbAccount)
    {
        this.zfbAccount = zfbAccount;
    }

    public String getOperaStaffId()
    {
        return operaStaffId;
    }

    public void setOperaStaffId(String operaStaffId)
    {
        this.operaStaffId = operaStaffId;
    }

    public String getSts()
    {
        return sts;
    }

    public void setSts(String sts)
    {
        this.sts = sts;
    }

    public String getStsDate()
    {
        return stsDate;
    }

    public void setStsDate(String stsDate)
    {
        this.stsDate = stsDate;
    }

    public String getPosition()
    {
        return position;
    }

    public void setPosition(String position)
    {
        this.position = position;
    }

    public String getWxId()
    {
        return wxId;
    }

    public void setWxId(String wxId)
    {
        this.wxId = wxId;
    }

    @Override
    public String tableName()
    {
        return "staff";
    }
}