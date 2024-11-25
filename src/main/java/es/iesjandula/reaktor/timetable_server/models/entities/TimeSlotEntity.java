package es.iesjandula.reaktor.timetable_server.models.entities;



import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author David Jason
 *
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class TimeSlotEntity
{
	// Identificador unico de tramo.
	@Id
	private String numeroTramo;
	
	private String diaSemanal;
	
	private String horaInicio;
	
	private String horaFin;
	
	
}