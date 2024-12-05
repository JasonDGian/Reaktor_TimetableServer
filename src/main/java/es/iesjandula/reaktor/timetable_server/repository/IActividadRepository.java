package es.iesjandula.reaktor.timetable_server.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import es.iesjandula.reaktor.timetable_server.models.entities.ActividadEntity;

public interface IActividadRepository extends JpaRepository<ActividadEntity, String>
{

}
