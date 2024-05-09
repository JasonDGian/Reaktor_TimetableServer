package es.iesjandula.reaktor.timetable_server.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import es.iesjandula.reaktor.timetable_server.models.jpa.VisitasServicio;
import es.iesjandula.reaktor.timetable_server.models.jpa.VisitasServicioId;

public interface IVisitasServicioRepository extends JpaRepository<VisitasServicio,VisitasServicioId>
{

}
