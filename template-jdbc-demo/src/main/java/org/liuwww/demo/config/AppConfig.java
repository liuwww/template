package org.liuwww.demo.config;

import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.support.http.StatViewServlet;

@Configuration
public class AppConfig
{
    // JdbcTemplateAutoConfiguration 里面已经有了
    /*@Bean
    public JdbcTemplate jdbcTemplate(DataSource dataSource)
    {
        return new JdbcTemplate(dataSource);
    }*/

    // 不使用 spring boot 默认的datasource 是因为Druid 数据源的其他参数不会注入，低版本的spring boot和druid
    // 会因为jmx的使属性重复绑定，重复设置username,paasswod,url而抛出异常
    @ConfigurationProperties(prefix = "spring.ds")
    @Bean
    public DataSource druidDataSource()
    {
        return new DruidDataSource();
    }

    @Bean
    public ServletRegistrationBean<StatViewServlet> statViewServlet()
    {
        ServletRegistrationBean<StatViewServlet> bean = new ServletRegistrationBean<StatViewServlet>(
                new StatViewServlet(), "/druid/*");

        Map<String, String> initParams = new HashMap<String, String>(4);
        initParams.put("loginUsername", "admin");
        initParams.put("loginPassword", "123456");
        initParams.put("allow", "");
        /** 设置初始化参数*/
        bean.setInitParameters(initParams);
        return bean;
    }
}
