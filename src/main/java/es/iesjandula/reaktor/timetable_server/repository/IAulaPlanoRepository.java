package es.iesjandula.reaktor.timetable_server.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import es.iesjandula.reaktor.timetable_server.models.entities.AulaPlano;

@Repository
public interface IAulaPlanoRepository extends JpaRepository<AulaPlano, Long> 
{
	
}
