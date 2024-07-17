package ite.project.hbase;


import ite.project.hbase.db.hbase.HbaseRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.HashMap;
import java.util.Map;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = HbaseApplication.class)
public class HbaseApplicationTests {

    @Autowired
    private HbaseRepository hbaseRepository;

    @Test
    public void createTable(){
        try {
            hbaseRepository.createTable("test","user");
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    @Test
    public void putData(){
        String name = "test";
        String columnFamily = "user";
        String rowKey = "row1";
        Map<String,String> data = new HashMap<>();
        data.put("name","hanh");
        data.put("email","hanh123@gmail.com");
        try {
            hbaseRepository.putData(name,columnFamily,rowKey,data);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    @Test
    public void getData(){
        try {
            hbaseRepository.getData("test");
        }catch (Exception e){
            e.printStackTrace();
        }
    }


}
