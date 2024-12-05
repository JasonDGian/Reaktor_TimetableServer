package es.iesjandula.reaktor.timetable_server.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import es.iesjandula.reaktor.timetable_server.models.jpa.PuntosConvivencia;

@Repository
public interface IPuntosConvivenciaRepository extends JpaRepository<PuntosConvivencia, Long>
{
	
}
