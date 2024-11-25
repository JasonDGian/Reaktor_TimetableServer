package es.iesjandula.reaktor.timetable_server.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import es.iesjandula.reaktor.timetable_server.models.entities.TimeSlot;

public interface ITimeSlotRepository extends JpaRepository<TimeSlot, String >
{

}
