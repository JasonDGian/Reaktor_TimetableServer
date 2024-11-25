package es.iesjandula.reaktor.timetable_server.models.entities;

import es.iesjandula.reaktor.timetable_server.models.parse.GruposActividad;
import es.iesjandula.reaktor.timetable_server.models.parse.HorarioAsig;
import es.iesjandula.reaktor.timetable_server.models.parse.HorarioGrup;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
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
public class Actividad implements Comparable<Actividad>
{
	/** Attribute gruposActividad */
	@OneToOne
	private GruposActividad gruposActividad;

	@Id
	//Numero de actividad.
	private String numAct;

	// Numero de unidades distintas.
	private String numUn;

	/** Attribute tramo */
	@OneToOne
	private TimeSlot tramo;			// Pruebas Jaydee : Convertido a entidad desde String.
	
	/** Attribute aula */
	@OneToOne
	private Aula aula;				// Pruebas Jaydee : Convertido a entidad desde String.

	@OneToOne
	/** Attribute profesor */
	private Profesor profesor;		// Pruebas Jaydee : Convertido a entidad desde String.

	@OneToOne
	/** Attribute asignatura */
	private Asignatura asignatura; 	// Pruebas Jaydee : Convertido a entidad desde String.

	
	/**
	 * Method compareTo
	 * @param other
	 * @return
	 */
	@Override
	public int compareTo(Actividad other)
	{
		// -- USED FOR SORT ACTIVIDAD BY TRAMO ID ORDER ---
		// PRUEBAS JAYUDEE: Metodo modificado añadiendo .getNumTr() a los tramos.
		int thisNumber = Integer.parseInt(this.tramo.getNumTr());
		int otherNumber = Integer.parseInt(other.tramo.getNumTr());
		return thisNumber-otherNumber;
	}
}