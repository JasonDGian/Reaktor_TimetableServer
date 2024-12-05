package es.iesjandula.reaktor.timetable_server.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import es.iesjandula.reaktor.timetable_server.models.entities.ActividadEntity;
import es.iesjandula.reaktor.timetable_server.models.parse.Actividad;

@Repository
public interface IActividadRepository extends JpaRepository<ActividadEntity, String>
{

	// Recupera un listado de actividades relacinadas a un profesor concreto del que recibimos nombre.
	@Query("SELECT new es.iesjandula.reaktor.timetable_server.models.parse.Actividad(ae) FROM ActividadEntity ae"  )
	public List<Actividad> recuperaListadoActividades();
	
	@Query("SELECT new es.iesjandula.reaktor.timetable_server.models.parse.Actividad(ae) FROM ActividadEntity ae"  )
	public List<Actividad> recuperaListadoActividadesProfesor( @Param( value = "idProfesor" ) String idProfesor );
	
}
