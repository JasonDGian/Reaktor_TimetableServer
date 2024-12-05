package es.iesjandula.reaktor.timetable_server.models.parse;

import es.iesjandula.reaktor.timetable_server.models.entities.ActividadEntity;
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
@Entity
public class Asignatura
{
	@Id
	/** Attribute numIntAs */
	private String numIntAs;

	/** Attribute abreviatura */
	private String abreviatura;

	/** Attribute nombre */
	private String nombre;
	
	@OneToMany(mappedBy = "asignaturas")
	private ActividadEntity actividad;
	
}