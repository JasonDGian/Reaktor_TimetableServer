package es.iesjandula.reaktor.timetable_server.models.parse;

import java.util.List;

import es.iesjandula.reaktor.timetable_server.models.entities.Grupo;
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
public class Grupos
{
	/** Attribute grupo*/
	private List<Grupo> grupo;
	
	/** Attribute totGr*/
	private String totGr;
}