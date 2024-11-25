package es.iesjandula.reaktor.timetable_server.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import es.iesjandula.reaktor.timetable_server.models.parse.GruposActividad;

//ESTE ID ES TEMPORAL CON FINES DE TESTEO PURAMENTE.
public interface IGruposActividadRepository extends JpaRepository<GruposActividad, String>
{

}
