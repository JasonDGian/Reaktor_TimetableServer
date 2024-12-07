package es.iesjandula.reaktor.timetable_server.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import es.iesjandula.reaktor.timetable_server.models.entities.StudentsEntity;

@Repository
public interface IStudentsRepository extends JpaRepository<StudentsEntity, Long>
{
	// filtra los estudiantes que están en el baño
	//@Query("SELECT s FROM StudentEntity s WHERE s.inBathroom = true")  
	
    public List<StudentsEntity> findAllByInBathroomTrue();
}
