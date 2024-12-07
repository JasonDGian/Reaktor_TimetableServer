package es.iesjandula.reaktor.timetable_server.models.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "studentsEntity")
@Entity
public class StudentsEntity 
{

	@Id
	@Column
	/** AAttribute id */
	private long idStudents;
	@Column
	/** Attribute name*/
	private String name;
	@Column
	/** Attribute lastName*/
	private String lastName;
	@Column
	/** Attribute course*/
	private String course;
	@Column
	/**Year of school registration */
	private String matriculationYear;
	@Column
	/**Fisrt tutor lastname */
	private String firstTutorLastName;
	@Column
	/**Second tutor lastname */
	private String secondTutorLastName;
	@Column
	/**Tutor name */
	private String tutorName;
	@Column
	/**Tutor phone */
	private String tutorPhone;
	@Column
	/**Tutor email */
	private String tutorEmail;
    // campo para indicar si el estudiante está en el baño
    @Column
    private Boolean inBathroom;

}
