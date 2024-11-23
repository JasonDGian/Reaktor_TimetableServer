package es.iesjandula.reaktor.timetable_server.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import es.iesjandula.reaktor.timetable_server.models.parse.Profesor;

public interface IProfesorRepository extends JpaRepository<Profesor, String>
{

}
