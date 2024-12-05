package es.iesjandula.reaktor.timetable_server.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import es.iesjandula.reaktor.timetable_server.models.entities.ProfesorEntity;
import es.iesjandula.reaktor.timetable_server.models.parse.Profesor;

@Repository
public interface IProfesorRepository extends JpaRepository<ProfesorEntity, String>
{

	@Query("SELECT new es.iesjandula.reaktor.timetable_server.models.parse.Profesor(p) FROM ProfesorEntity p")
	public List<Profesor> recuperaListadoProfesores();
	
}

