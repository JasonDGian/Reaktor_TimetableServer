package es.iesjandula.reaktor.timetable_server.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import es.iesjandula.reaktor.timetable_server.models.jpa.PuntosConvivencia;

public interface IPuntosConvivenciaRepository extends JpaRepository<PuntosConvivencia, Long>
{
	
}
