package es.iesjandula.reaktor.timetable_server.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import es.iesjandula.reaktor.timetable_server.models.jpa.Curso;
import es.iesjandula.reaktor.timetable_server.models.jpa.CursoId;

public interface ICursosRepository extends JpaRepository<Curso,CursoId>
{

}
