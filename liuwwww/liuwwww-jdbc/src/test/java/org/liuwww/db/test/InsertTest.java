package org.liuwww.db.test;

import org.junit.runner.RunWith;
import org.liuwww.db.service.IDataService;
import org.liuwww.db.service.IQueryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations =
{ "classpath:spring/spring.xml" })
public class InsertTest
{
    @Autowired
    private IDataService dataService;

    @Autowired
    private IQueryService queryService;

    public void test1()
    {
        // dataService.insert(entity);
    }

}
