package es.iesjandula.reaktor.timetable_server.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import es.iesjandula.reaktor.timetable_server.models.entities.TimeSlotEntity;
import es.iesjandula.reaktor.timetable_server.models.parse.TimeSlot;

@Repository
public interface ITimeSlotRepository extends JpaRepository<TimeSlotEntity, String>
{

	 @Query( "SELECT new es.iesjandula.reaktor.timetable_server.models.parse.TimeSlot(t) FROM TimeSlotEntity t" )
	 public List<TimeSlot> recuperaListadoTramosHorarios();
	 
	 public List<TimeSlotEntity> findByDayNumber( String daynumber );
	
	 
}
