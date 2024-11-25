package es.iesjandula.reaktor.timetable_server.models.parse;

import java.util.List;

import es.iesjandula.reaktor.timetable_server.models.entities.Aula;
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
public class Aulas
{
	/** Attribute aula*/
	private List<Aula> aula;
	
	/** Attribute totAu*/
	private String totAu;
}