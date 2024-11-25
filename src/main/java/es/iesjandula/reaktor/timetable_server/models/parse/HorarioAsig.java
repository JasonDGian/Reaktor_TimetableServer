package es.iesjandula.reaktor.timetable_server.models.parse;

import java.util.List;

import es.iesjandula.reaktor.timetable_server.models.entities.Actividad;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
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
public class HorarioAsig
{
	/** Attribute actividad*/
	private List<Actividad> actividad;

	// Numero de la asignatura en la clase. como un ID numerico.
	private String horNumIntAs; 
	
	// Unidades de asignaturas distintas involucradas.
	private String totUn;
	
	// Cantidad de actividades que almacena.
	private String totAC;
}