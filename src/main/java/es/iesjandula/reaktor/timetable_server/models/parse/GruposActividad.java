package es.iesjandula.reaktor.timetable_server.models.parse;



import es.iesjandula.reaktor.timetable_server.models.entities.Grupo;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
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
public class GruposActividad
{
	/** Attribute totGrAct*/
	@Id
	private String totGrAct;
	
	/** Attribute grupo1*/
	@OneToOne
	private Grupo grupo1; // Pruebas Jaydee: cambiado a entidad Grupo desde String
	
	/** Attribute grupo2*/
	@OneToOne
	private Grupo grupo2; // Pruebas Jaydee: cambiado a entidad Grupo desde String
	
	/** Attribute grupo3*/
	@OneToOne
	private Grupo grupo3; // Pruebas Jaydee: cambiado a entidad Grupo desde String
	
	/** Attribute grupo4*/
	@OneToOne
	private Grupo grupo4; // Pruebas Jaydee: cambiado a entidad Grupo desde String
	
	/** Attribute grupo5*/
	@OneToOne
	private Grupo grupo5; // Pruebas Jaydee: cambiado a entidad Grupo desde String

}
