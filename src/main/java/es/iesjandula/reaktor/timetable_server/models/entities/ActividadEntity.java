package es.iesjandula.reaktor.timetable_server.models.entities;

import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
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
public class ActividadEntity implements Comparable<ActividadEntity>
{
	/** Attribute gruposActividad */
	@OneToMany
	private List<GruposActividadEntity> gruposActividad;

	@Id
	/** Attribute numAct */
	private String numAct;

	/** Attribute numUn */
	private String numUn;

	@ManyToOne
	/** Attribute tramo */
	private TimeSlotEntity tramo;

	@ManyToOne
	/** Attribute aula */
	private AulaEntity aula;

	@ManyToOne
	/** Attribute profesor */
	private ProfesorEntity profesor;

	/** Attribute asignatura */
	@ManyToOne
	private AsignaturaEntity asignatura;
	
	// Adicionales Jaydee ----------------------------------
	@ManyToOne
	private HorarioProfEntity horarioProfesor;
	
	@ManyToOne
	private HorarioAulaEntity horarioAula;
	
	@ManyToOne
	private HorarioGrupEntity horarioGrupo;
	
	
	/**
	 * Method compareTo
	 * @param other
	 * @return
	 */
	@Override
	public int compareTo(ActividadEntity other)
	{
		// -- USED FOR SORT ACTIVIDAD BY TRAMO ID ORDER ---
		int thisNumber = Integer.parseInt(this.tramo.getNumeroTramo());
		int otherNumber = Integer.parseInt(other.tramo.getNumeroTramo());
		return thisNumber-otherNumber;
	}
}