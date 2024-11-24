package es.iesjandula.reaktor.timetable_server.models.parse;



import jakarta.persistence.Entity;
import jakarta.persistence.Id;
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
@Entity
public class TimeSlot
{
	@Id
	/** Attribute numTr*/
	private String numTr;
	
	/** Attribute numeroDia*/
	private String dayNumber;
	
	/** Attribute horaInicio*/
	private String startHour;
	
	/** Attribute horaFinal*/
	private String endHour;
	
	
}