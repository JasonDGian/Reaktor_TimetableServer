package es.iesjandula.reaktor.timetable_server.models.parse;

import java.util.List;

import es.iesjandula.reaktor.timetable_server.models.entities.TimeSlot;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author David Martinez
 *
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TramosHorarios
{
	/** Attribute tramo*/
	private List<TimeSlot> tramo;
	
	/** Attribute totTr*/
	private String totTr;
}
