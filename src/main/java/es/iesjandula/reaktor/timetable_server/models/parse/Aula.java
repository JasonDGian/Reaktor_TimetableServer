package es.iesjandula.reaktor.timetable_server.models.parse;

import es.iesjandula.reaktor.timetable_server.models.entities.AulaEntity;
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
public class Aula
{

	/** Attribute numIntAu*/
	private String numIntAu;
	
	/** Attribute abreviatura*/
	private String abreviatura;
	
	/** Attribute nombre*/
	private String nombre;
	
	public Aula( AulaEntity aulaEntidad ) {
		this.numIntAu = aulaEntidad.getNumIntAu();
		this.abreviatura = aulaEntidad.getAbreviatura();
		this.nombre = aulaEntidad.getNombre();
	}

}
