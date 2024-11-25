package es.iesjandula.reaktor.timetable_server.models.parse;

import java.util.List;

import es.iesjandula.reaktor.timetable_server.models.entities.Asignatura;
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
public class Asignaturas
{
	private Long id_asignaturas;
	
	/** Attribute asignatura*/
	private List<Asignatura> asignatura;
	
	/** Attribute totAs*/
	private String totAs;

}