package es.iesjandula.reaktor.timetable_server.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import es.iesjandula.reaktor.timetable_server.models.jpa.Alumnos;

public interface IAlumnoRepository extends JpaRepository<Alumnos, Long>
{

}
