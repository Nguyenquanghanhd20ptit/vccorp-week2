package ite.project.hbase;

import com.google.gson.Gson;
import ite.project.hbase.commons.data.model.paging.Pageable;
import ite.project.hbase.db.hbase.entity.StudentEntity;
import ite.project.hbase.db.hbase.repository.IStudentRepository;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = HbaseApplication.class)
public class HbaseApplicationTests {
    @Autowired
    private IStudentRepository studentRepository;

    @Test
    public void testPutData(){

        Map<String,String> personal = new HashMap<>();
        personal.put("name","Nguyễn Quang Hạnh");
        personal.put("email","hanhd20ptit@gmail.com");
        personal.put("phone_number","0383870219");
        personal.put("address","Ba vì - Hà Nội");

        Map<String,String> academic = new HashMap<>();
        academic.put("msv","D20DCCN225");
        academic.put("class","B20CQCN09");
        academic.put("start_year","2020");
        academic.put("major","Information technology");

        StudentEntity studentEntity = new StudentEntity();
        studentEntity.setRow("row2");
        studentEntity.setPersonal(personal);
        studentEntity.setAcademic(academic);

        studentRepository.putData(studentEntity);
    }

    @Test
    public void testGetData(){
        List<StudentEntity> studentEntities = studentRepository.getDataTable();
        String res = new Gson().toJson(studentEntities);
        System.out.println(res);
        Assert.assertNotNull(res);
    }

    @Test
    public void testGetRowKey(){
        StudentEntity student = studentRepository.getDataByRowKey("row1");
        Assert.assertNotNull(student);
    }

    @Test
    public void testGetPaginated(){
        Pageable pageable = new Pageable();
        pageable.setPage(1);
        pageable.setPageSize(1);
        List<StudentEntity> students = studentRepository.getPaginated(pageable);
        List<StudentEntity> studentTotal = studentRepository.getDataTable();
        Assert.assertNotNull(students);
    }

    @Test
    public void testDeleteRow(){
        studentRepository.deleteRow("row1");
        StudentEntity student = studentRepository.getDataByRowKey("row1");
        Assert.assertNull(student);
    }
}
