package es.iesjandula.reaktor.timetable_server.models.parse;

import es.iesjandula.reaktor.timetable_server.models.entities.ProfesorEntity;
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
public class Profesor implements Comparable<Profesor>
{
	/** Attribute numIntPR*/
	private String numIntPR;
	
	/** Attribute abreviatura*/
	private String abreviatura;
	
	/** Attribute nombre*/
	private String nombre;
	
	/** Attribute primerApellido*/
	private String primerApellido;
	
	/** Attribute segundoApellido*/
	private String segundoApellido;
	
	// Constructore de mapeo.
	public Profesor ( ProfesorEntity profesorEntidad) {
		this.abreviatura = profesorEntidad.getAbreviatura();
		this.numIntPR = profesorEntidad.getNumIntPR();
		this.nombre = profesorEntidad.getNombre();
		this.primerApellido = profesorEntidad.getPrimerApellido();
		this.segundoApellido = profesorEntidad.getSegundoApellido();
	}

	@Override
	public int compareTo(Profesor other) {
		if(this.primerApellido.compareTo(other.primerApellido)!=0)
		{
			return this.primerApellido.compareTo(other.primerApellido);
		}
		else if(this.segundoApellido.compareTo(other.segundoApellido)!=0)
		{
			return this.segundoApellido.compareTo(other.segundoApellido);
		}
		else
		{
			return this.nombre.compareTo(other.nombre);
		}
	}
}