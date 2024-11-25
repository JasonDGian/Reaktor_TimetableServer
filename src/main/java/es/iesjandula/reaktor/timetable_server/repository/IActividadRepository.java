package es.iesjandula.reaktor.timetable_server.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import es.iesjandula.reaktor.timetable_server.models.entities.Actividad;

//ESTE ID ES TEMPORAL CON FINES DE TESTEO PURAMENTE.
public interface IActividadRepository extends JpaRepository<Actividad, String>
{

}
