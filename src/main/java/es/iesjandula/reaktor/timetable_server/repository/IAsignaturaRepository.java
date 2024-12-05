package es.iesjandula.reaktor.timetable_server.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import es.iesjandula.reaktor.timetable_server.models.entities.AsignaturaEntity;
import es.iesjandula.reaktor.timetable_server.models.parse.Asignatura;

@Repository
public interface IAsignaturaRepository extends JpaRepository<AsignaturaEntity, String>
{

	@Query( "SELECT new es.iesjandula.reaktor.timetable_server.models.parse.Asignatura(a) FROM AsignaturaEntity a" )
	public List<Asignatura> recuperaListadoAsignatura();
	
}
