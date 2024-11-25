package es.iesjandula.reaktor.timetable_server.models.entities;

import java.util.List;

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
@Entity
public class GrupoEntity implements Comparable<GrupoEntity>
{

	@Id
	/** Attribute abreviatura*/
	private String abreviatura;
	
	/** Attribute nombre*/
	private String nombre;
	
	@OneToMany
	private List<GruposActividadEntity> gruposActividad;
	
	@Override
	public int compareTo(GrupoEntity other) {
		return this.nombre.compareTo(other.nombre);
	}
	
	
}