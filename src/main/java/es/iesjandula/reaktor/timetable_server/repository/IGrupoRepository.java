package es.iesjandula.reaktor.timetable_server.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import es.iesjandula.reaktor.timetable_server.models.entities.GrupoEntity;
import es.iesjandula.reaktor.timetable_server.models.parse.Grupo;

public interface IGrupoRepository extends JpaRepository<GrupoEntity, String>
{

	// Recupera una lista de objetos GrupoEntity y los convierte a clase Grupo tipo parse.
	@Query( "SELECT new es.iesjandula.reaktor.timetable_server.models.parse.Grupo(g) FROM GrupoEntity g"  )
	public List<Grupo> recuperaGruposDeParseo();
	
}
