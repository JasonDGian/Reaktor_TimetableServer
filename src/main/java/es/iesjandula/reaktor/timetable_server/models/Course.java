package es.iesjandula.reaktor.timetable_server.models;

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
public class Course
{
	/** Attribute name*/
	private String name;
	
	/** Attribute classroom*/
	private Classroom classroom;
	
}
