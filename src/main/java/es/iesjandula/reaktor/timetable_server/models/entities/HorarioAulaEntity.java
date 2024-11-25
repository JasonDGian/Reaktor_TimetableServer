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
public class HorarioAulaEntity
{
	@OneToMany(mappedBy = "horarioAula")
	/** Attribute actividad*/
	private List<ActividadEntity> actividad;
	
	@Id
	/** Attribute horNumIntAu*/
	private String horNumIntAu;
	
	/** Attribute totUn*/
	private String totUn;
	
	/** Attribute totAC*/
	private String totAC;
}