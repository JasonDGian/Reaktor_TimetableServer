package es.iesjandula.reaktor.timetable_server.models.jpa;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "puntos_convivencia_alumno_curso")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PuntosConvivenciaAlumnoCurso 
{
	/**Id emmebido que referencia a las tablas alumnos, curso y puntos convivencia */
	@EmbeddedId
	private PuntosConvivenciaAlumnoCursoId puntosConvivenciaAlumnoCursoId;
	
	/**Id del alumno que es sancionado/recompensado */
	@ManyToOne
	@MapsId("idAlumno")
	private Alumnos idAlumno;
	
	/**Nombre del curso que pertence el alumno*/
	@ManyToOne
	@MapsId("cursoId")
	private Curso nombreCurso;
	
//	/**Año academico del curso */
//	@ManyToOne
//	@JoinColumn(name = "anioAcademico", referencedColumnName = "cursoId")
//	@MapsId("anioAcademicoCurso")
//	private Curso anioAcademicoCurso;
//	
	
	/**Id de los puntos y la sancion/recompensa que el alumno recibe */
	@ManyToOne
	@MapsId("idPuntosConvivencia")
	private PuntosConvivencia idPuntosConvivencia;
	
	/**Fecha en la que se impone la sancion */
	@Column
	private Date fechaSancion;
}
