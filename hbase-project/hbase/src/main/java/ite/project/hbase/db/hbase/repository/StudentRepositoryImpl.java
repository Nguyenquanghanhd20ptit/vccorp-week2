package ite.project.hbase.db.hbase.repository;

import ite.project.hbase.db.hbase.AbsHbaseRepository;
import ite.project.hbase.db.hbase.entity.StudentEntity;
import org.springframework.stereotype.Repository;

@Repository
public class StudentRepositoryImpl extends AbsHbaseRepository<StudentEntity>
        implements IStudentRepository {

}
