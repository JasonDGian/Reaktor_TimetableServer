package es.iesjandula.reaktor.timetable_server.rest;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import es.iesjandula.reaktor.timetable_server.exceptions.HorariosError;
import es.iesjandula.reaktor.timetable_server.models.ActitudePoints;
import es.iesjandula.reaktor.timetable_server.models.ApplicationPdf;
import es.iesjandula.reaktor.timetable_server.models.Classroom;
import es.iesjandula.reaktor.timetable_server.models.Course;
import es.iesjandula.reaktor.timetable_server.models.Hour;
import es.iesjandula.reaktor.timetable_server.models.InfoError;
import es.iesjandula.reaktor.timetable_server.models.Rol;
import es.iesjandula.reaktor.timetable_server.models.Student;
import es.iesjandula.reaktor.timetable_server.models.Teacher;
import es.iesjandula.reaktor.timetable_server.models.TeacherMoment;
import es.iesjandula.reaktor.timetable_server.models.entities.ActividadEntity;
import es.iesjandula.reaktor.timetable_server.models.entities.AsignaturaEntity;
import es.iesjandula.reaktor.timetable_server.models.entities.AulaEntity;
import es.iesjandula.reaktor.timetable_server.models.entities.GrupoEntity;
import es.iesjandula.reaktor.timetable_server.models.entities.ProfesorEntity;
import es.iesjandula.reaktor.timetable_server.models.entities.TimeSlotEntity;
import es.iesjandula.reaktor.timetable_server.models.parse.Actividad;
import es.iesjandula.reaktor.timetable_server.models.parse.Asignatura;
import es.iesjandula.reaktor.timetable_server.models.parse.Aula;
import es.iesjandula.reaktor.timetable_server.models.parse.AulaPlano;
import es.iesjandula.reaktor.timetable_server.models.parse.Centro;
import es.iesjandula.reaktor.timetable_server.models.parse.Grupo;
import es.iesjandula.reaktor.timetable_server.models.parse.GruposActividad;
import es.iesjandula.reaktor.timetable_server.models.parse.HorarioAsig;
import es.iesjandula.reaktor.timetable_server.models.parse.HorarioAula;
import es.iesjandula.reaktor.timetable_server.models.parse.HorarioGrup;
import es.iesjandula.reaktor.timetable_server.models.parse.HorarioProf;
import es.iesjandula.reaktor.timetable_server.models.parse.Profesor;
import es.iesjandula.reaktor.timetable_server.models.parse.TimeSlot;
import es.iesjandula.reaktor.timetable_server.repository.IActividadRepository;
import es.iesjandula.reaktor.timetable_server.repository.IAlumnoRepository;
import es.iesjandula.reaktor.timetable_server.repository.IAsignaturaRepository;
import es.iesjandula.reaktor.timetable_server.repository.IAulaRepository;
import es.iesjandula.reaktor.timetable_server.repository.ICursosRepository;
import es.iesjandula.reaktor.timetable_server.repository.IGrupoRepository;
import es.iesjandula.reaktor.timetable_server.repository.IProfesorRepository;
import es.iesjandula.reaktor.timetable_server.repository.IPuntosConvivenciaALumnoCursoRepository;
import es.iesjandula.reaktor.timetable_server.repository.IPuntosConvivenciaRepository;
import es.iesjandula.reaktor.timetable_server.repository.ITimeSlotRepository;
import es.iesjandula.reaktor.timetable_server.repository.IVisitasServicioRepository;
import es.iesjandula.reaktor.timetable_server.utils.JPAOperations;
import es.iesjandula.reaktor.timetable_server.utils.StudentOperation;
import es.iesjandula.reaktor.timetable_server.utils.TimeTableUtils;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;

/**
 * @author David Martinez, Pablo Ruiz Canovas
 */
@RestController
@RequestMapping("/horarios")
@Slf4j
public class TimetableRest
{
	/** Attribute centroPdfs , used for get the info of PDFS */
	private Centro centroPdfs;

	/** Clase que se encarga de las operaciones logicas del servidor */
	private TimeTableUtils util;

	/** Clase que se encarga de gestionar las operaciones con los estudiantes */
	private StudentOperation studentOperation;

	/** Clase que se encarga de manejar las operaciones con la base de datos */
	private JPAOperations operations;

	/** Lista de estudiantes cargados por csv */
	private List<Student> students;

	/** Lista de los planos de las aulas */
	private List<AulaPlano> aulas;

	/** Objeto que guarda el error actual de la pagina */
	private InfoError infoError;

	/** Repositorio que contiene todas las operaciones CRUD de la entidad Alumnos */
	@Autowired
	private IAlumnoRepository alumnoRepo;

	/** Repositorio que contiene todas las operaciones CRUD de la entidad Curso */
	@Autowired
	private ICursosRepository cursoRepo;

	/**
	 * Repositorio que contiene todas las operaciones CRUD de la entidad Puntos
	 * convivencia
	 */
	@Autowired
	private IPuntosConvivenciaRepository puntosRepo;

	/**
	 * Repositorio que contiene todas las operaciones CRUD de la entidad
	 * PuntosConvivenciaAlumnosCurso
	 */
	@Autowired
	private IPuntosConvivenciaALumnoCursoRepository sancionRepo;

	/**
	 * Repositorio que contiene todas las operaciones CRUD de la entidad
	 * VisitasServicio
	 */
	@Autowired
	private IVisitasServicioRepository visitasRepo;

	// --------------------------- JAYDEE
	@Autowired
	private IAsignaturaRepository asignaturaRepo;

	@Autowired
	private IGrupoRepository grupoRepo;

	@Autowired
	private IAulaRepository aulaRepo;

	@Autowired
	private IProfesorRepository profesorRepo;

	@Autowired
	private ITimeSlotRepository timeslotRepo;

	@Autowired
	private IActividadRepository actividadRepo;

	public TimetableRest()
	{
		this.util = new TimeTableUtils();
		this.studentOperation = new StudentOperation();
		this.students = new LinkedList<Student>();
	}

	/**
	 * Este metodo parsea el XML que recibe. Va por este orden. 1- Asignaturas. 2-
	 * Grupos 3- Aulas 4- Profes 5- Tramos
	 *
	 * @param xmlFile
	 * @return ResponseEntity
	 */
	@RequestMapping(value = "/send/xml", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<?> sendXmlToObjects(@RequestPart MultipartFile xmlFile, HttpSession session)
	{
		try
		{
			File xml = new File(xmlFile.getOriginalFilename());
			log.info("FILE NAME: " + xml.getName());
			if (xml.getName().endsWith(".xml"))
			{
				// ES UN XML
				DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
				DocumentBuilder documentBuilder;
				// -- OBJECT CENTRO ---
				Centro centro = new Centro();
				try
				{
					InputStream is = xmlFile.getInputStream();
					documentBuilder = builderFactory.newDocumentBuilder();
					Document document = documentBuilder.parse(is);

					// --- ELEMENTO ROOT CENTRO ------
					Element rootCenterElement = document.getDocumentElement();
					// --- ELEMENT CENTRO ATTRIBUTES ---
					centro.setNombreCentro(rootCenterElement.getAttribute("nombre_centro"));
					centro.setAutor(rootCenterElement.getAttribute("autor"));
					centro.setFecha(rootCenterElement.getAttribute("fecha"));
					// --------------------------------------------------------------------------------------------------
					// --- CARGA DATOS ---
					// --------------------------------------------------------------------------------------------------

					// --- ALMACENAMIENTO ASIGNATURAS ---
					log.info("- Almacenando asignaturas. -");

					// Recupera elemento de NODO padre ASIGNATURAS
					Element asignaturasElemet = (Element) rootCenterElement.getElementsByTagName("ASIGNATURAS").item(0);

					// Recupera elemento de NODOS hijos ASIGNATURA
					NodeList asignaturasNodeList = asignaturasElemet.getElementsByTagName("ASIGNATURA");

					// Almacena en BBDD el los valores listados en la lista de nodos XML.
					this.saveValuesOfAsignatura(asignaturasNodeList);
					log.info("Asignaturas almacenadas en BBDD: {}", asignaturasNodeList.getLength());

					// --------------------------------------------------------------------------------------------------

					// --- ALMACENAMIENTO GRUPOS ---
					log.info("- Almacenando Grupos -.");

					// Recupera elemento de NODO padre GRUPOS
					Element gruposElement = (Element) rootCenterElement.getElementsByTagName("GRUPOS").item(0);

					// Recupera listado de nodos GRUPO.
					NodeList gruposNodeList = gruposElement.getElementsByTagName("GRUPO");

					// Almacena en BBDD el los valores listados en la lista de nodos XML.
					this.saveValuesOfGrupo(gruposNodeList);
					log.info("Grupos almacenados en BBDD: {}", gruposNodeList.getLength());

					// --------------------------------------------------------------------------------------------------

					// --- ALMACENAMIENTO AULAS ---
					log.info("- Almacenando Aulas -.");

					// Recupera elemento de NODO padre AULAS
					Element aulasElement = (Element) rootCenterElement.getElementsByTagName("AULAS").item(0);

					// Recupera listado de nodos AULA.
					NodeList aulasNodeList = aulasElement.getElementsByTagName("AULA");

					// Almacena en BBDD el los valores listados en la lista de nodos XML.
					this.saveValuesOfAula(aulasNodeList);
					log.info("Aulas almacenadas en BBDD: {}", aulasNodeList.getLength());
					// --------------------------------------------------------------------------------------------------

					// --- ALMACENAMIENTO PROFESORES ---
					log.info("- Almacenando Profesores -.");

					// Recupera elemento de NODO padre PROFESORES
					Element profesoresElement = (Element) rootCenterElement.getElementsByTagName("PROFESORES").item(0);

					// Recupera listado de nodos PROFESOR.
					NodeList profesoresNodeList = profesoresElement.getElementsByTagName("PROFESOR");

					// Almacena en BBDD el los valores listados en la lista de nodos XML.
					this.saveValuesOfProfesor(profesoresNodeList);
					log.info("Profesores almacenados en BBDD: {}", profesoresNodeList.getLength());

					// --------------------------------------------------------------------------------------------------

					// --- ALMACENAMIENTO TRAMOS HORARIOS ---
					log.info("- Almacenando Tramos Horarios -.");

					// Recupera elemento de NODO padre TRAMOS HORARIOS
					Element tramosHorariosElement = (Element) rootCenterElement.getElementsByTagName("TRAMOS_HORARIOS")
							.item(0);

					// Recupera listado de nodos TRAMO
					NodeList tramosHorariosNodeList = tramosHorariosElement.getElementsByTagName("TRAMO");

					// Almacena en BBDD el los valores listados en la lista de nodos XML.
					this.saveValuesOfTramo(tramosHorariosNodeList);
					log.info("Tramos horarios almacenados en BBDD: {}", tramosHorariosNodeList.getLength());

					// --------------------------------------------------------------------------------------------------
					// Nota de David Jason:
					// En esta sección del código se trata de almacenar una serie de registros
					// clasificados por tipo que introducen información acerca de como se relacionan
					// las entidades cuyos datos se han ido almacenando hasta ahora.
					// Es decir:
					// HorariosAsig introducirá registros en la tabla ACTIVIDADES que relacionará
					// profesores, aulas, horas, asignaturas etc...
					// HorariosGrupos hará lo mismo, añadiendo otros registros y asi hasta obtener
					// el lote de información completo.
					// --------------------------------------------------------------------------------------------------

					// --- HORARIOS ---

					// --------------------------------------------------------------------------------------------------
					// --- HORARIOS ELEMENT ---
					Element horariosElement = (Element) rootCenterElement.getElementsByTagName("HORARIOS").item(0);

					// --------------------------------------------------------------------------------------------------

					// HORARIOS ASIG.
					log.info("- Almacenando datos de Horarios Asignatura -");

					// Recupera elemento de NODO padre de HORARIOS ASIGNATURAS.
					Element horariosAsignaturasElement = (Element) horariosElement
							.getElementsByTagName("HORARIOS_ASIGNATURAS").item(0);

					// Recupera listado de nodos HORARIOS ASIGNATURAS.
					NodeList horarioAsigNodeList = horariosAsignaturasElement.getElementsByTagName("HORARIO_ASIG");

					// Almacena en BBDD el los valores listados en la lista de nodos XML.
					this.saveValuesOfHorarioAsig(horarioAsigNodeList);
					log.info("Horarios Asignatura almacenados en BBDD: {}", horarioAsigNodeList.getLength());

					// --------------------------------------------------------------------------------------------------

					// HORARIO GRUPOS
					log.info("- Almacenando datos de Horarios Grupo -");

					// Recupera elemento de NODO padre de HORARIO GRUPOS.
					Element horariosGruposElement = (Element) horariosElement.getElementsByTagName("HORARIOS_GRUPOS")
							.item(0);

					// Recupera listado de nodos HORARIOS GRUPOS.
					NodeList horarioGrupNodeList = horariosGruposElement.getElementsByTagName("HORARIO_GRUP");

					// Almacena en BBDD el los valores listados en la lista de nodos XML.
					this.saveValuesOfHorarioGrup(horarioGrupNodeList);
					log.info("Horarios Grupo almacenados en BBDD: {}", horarioGrupNodeList.getLength());

					// --------------------------------------------------------------------------------------------------

					// HORARIOS AULA
					log.info("- Almacenando datos de Horarios Aula -");

					// Recupera elemento de NODO padre de HORARIO AULAS.
					Element horariosAulasElement = (Element) horariosElement.getElementsByTagName("HORARIOS_AULAS")
							.item(0);

					// Recupera listado de nodos HORARIOS AULA.
					NodeList horarioAulaNodeList = horariosAulasElement.getElementsByTagName("HORARIO_AULA");
					
					// Almacena en BBDD el los valores listados en la lista de nodos XML.
					this.saveValuesOfHorarioAula(horarioAulaNodeList);
					log.info("Horarios Profesores almacenados en BBDD: {}", horarioAulaNodeList.getLength());
					
					// --------------------------------------------------------------------------------------------------

					// --- HORARIOS PROFESORES
					log.info("- Almacenando datos de Horarios Profesores -");
					
					// Recupera elemento de NODO padre de HORARIO PROFESORES.
					Element horariosProfesoresElement = (Element) horariosElement
							.getElementsByTagName("HORARIOS_PROFESORES").item(0);

					// Recupera listado de nodos HORARIOS PROFESOR.
					NodeList horarioProfNodeList = horariosProfesoresElement.getElementsByTagName("HORARIO_PROF");

					// Almacena en BBDD el los valores listados en la lista de nodos XML.
					this.saveValuesOfHorarioProf(horarioProfNodeList);
					log.info("Horarios Profesores almacenados en BBDD: {}", horarioProfNodeList.getLength());
					
					// -------------------------------------------------------------------------------------------------------------------------------------------------

					// -------------------------------------------------------------------------------------------------------------------------------------------------
					log.info("File :" + xmlFile.getName() + " load-Done");

					// Cargamos las operaciones con base de datos
					this.operations = new JPAOperations(this.alumnoRepo, this.cursoRepo, this.puntosRepo,
							this.sancionRepo, this.visitasRepo);
				} catch (ParserConfigurationException exception)
				{
					String error = "Parser Configuration Exception";
					log.error(error, exception);
					HorariosError horariosException = new HorariosError(400, exception.getLocalizedMessage(),
							exception);
					return ResponseEntity.status(400).body(horariosException.toMap());

				} catch (SAXException exception)
				{
					String error = "SAX Exception";
					log.error(error, exception);
					HorariosError horariosException = new HorariosError(400, exception.getLocalizedMessage(),
							exception);
					return ResponseEntity.status(400).body(horariosException.toMap());
				} catch (IOException exception)
				{
					String error = "In Out Exception";
					log.error(error, exception);
					HorariosError horariosException = new HorariosError(400, exception.getLocalizedMessage(),
							exception);
					return ResponseEntity.status(400).body(horariosException.toMap());
				}

				// --- SESSION ---------

				// session.setAttribute("storedCentro", centro);
				log.info("UserVisits: " + centro);

				// --- SESSION RESPONSE_ENTITY ---------
				this.centroPdfs = centro;
				return ResponseEntity.ok(session.getAttribute("storedCentro"));

			} else
			{
				// NO ES UN XML
				String error = "The file is not a XML file";
				HorariosError horariosException = new HorariosError(400, error, new Exception());
				log.error(error, horariosException);
				return ResponseEntity.status(400).body(horariosException.toMap());
			}
		} catch (Exception except)
		{
			// SERVER ERROR
			String error = "Server Error";
			HorariosError horariosException = new HorariosError(500, except.getLocalizedMessage(), except);
			log.error(error, horariosException);
			return ResponseEntity.status(500).body(horariosException.toMap());
		}
	}

	// ------------------------- METODOS PARA ALMACENAR ENTIDADES DE DATOS EN BBDD.
	// Estas entidades representan los datos basicos sin relacion entre ellos.

	/**
	 * Autor: David Jason G.
	 * 
	 * Guarda los valores de los tramos horarios a partir de una lista de nodos XML.
	 * El método recorre la lista de nodos proporcionada, extrae los atributos
	 * necesarios para crear instancias de la entidad TimeSlotEntity, y luego guarda
	 * todas las entidades creadas en la base de datos utilizando un repositorio.
	 *
	 * @param tramosHorariosNodeList la lista de nodos XML que contienen la
	 *                               información de los tramos horarios. Cada nodo
	 *                               debe tener atributos que correspondan a las
	 *                               horas de inicio y fin, el número del día y el
	 *                               identificador del tramo.
	 */
	private void saveValuesOfTramo(NodeList tramosHorariosNodeList)
	{
		List<TimeSlotEntity> listadoTramos = new ArrayList<>();

		for (int i = 0; i < tramosHorariosNodeList.getLength(); i++)
		{
			TimeSlotEntity newTramo = new TimeSlotEntity();
			newTramo.setEndHour(tramosHorariosNodeList.item(i).getAttributes().item(0).getTextContent());
			newTramo.setStartHour(tramosHorariosNodeList.item(i).getAttributes().item(1).getTextContent());
			newTramo.setDayNumber(tramosHorariosNodeList.item(i).getAttributes().item(3).getTextContent());
			newTramo.setNumTr(tramosHorariosNodeList.item(i).getAttributes().item(2).getTextContent());
			listadoTramos.add(newTramo);
		}

		this.timeslotRepo.saveAllAndFlush(listadoTramos);
	}

	/**
	 * Autor: David Jason G.
	 * 
	 * Guarda los valores de los profesores a partir de una lista de nodos XML. El
	 * método recorre la lista de nodos proporcionada, extrae los atributos
	 * necesarios para crear instancias de la entidad ProfesorEntity, y luego guarda
	 * todas las entidades creadas en la base de datos utilizando un repositorio.
	 *
	 * La información extraída incluye: - Abreviatura del profesor. - Número interno
	 * del profesor (numIntPR). - Nombre completo, dividido en nombre, primer
	 * apellido y segundo apellido.
	 *
	 * @param profesoresNodeList la lista de nodos XML que contienen la información
	 *                           de los profesores. Cada nodo debe tener atributos
	 *                           que representen la abreviatura, el número interno,
	 *                           y el nombre completo en formato "Apellidos,
	 *                           Nombre".
	 */
	private void saveValuesOfProfesor(NodeList profesoresNodeList)
	{
		// Crea una lista para rellenar con entidades profesor.
		List<ProfesorEntity> listadoProfesores = new ArrayList<>();

		// Por cada elemento profesor en el nodo xml
		for (int i = 0; i < profesoresNodeList.getLength(); i++)
		{
			// Crea un nuevo profesor.
			ProfesorEntity newProfesor = new ProfesorEntity();

			// Asigna los valores simples de Abreviatura e Identificador numIntPr.
			newProfesor.setAbreviatura(profesoresNodeList.item(i).getAttributes().item(0).getTextContent());
			newProfesor.setNumIntPR(profesoresNodeList.item(i).getAttributes().item(2).getTextContent());

			// Recoge nombre completo y lo separa en un string de cadenas.
			String nombreCompleto = profesoresNodeList.item(i).getAttributes().item(1).getTextContent();
			String[] nombreCompletoSpit = nombreCompleto.split(",");
			String[] apellidosSplit = nombreCompletoSpit[0].split(" ");

			// Configura nombre y apellidos basandose en el array de cadenas.
			newProfesor.setNombre(nombreCompletoSpit[nombreCompletoSpit.length - 1].trim());
			newProfesor.setPrimerApellido(apellidosSplit[0].trim());
			newProfesor.setSegundoApellido(apellidosSplit[1].trim());

			// Añade profesor actual al listado de profesores.
			listadoProfesores.add(newProfesor);
		}
		// Almacena listado completo en BBDD.
		this.profesorRepo.saveAll(listadoProfesores);
	}

	/**
	 * Autor: David Jason G.
	 * 
	 * Guarda los valores de las aulas a partir de una lista de nodos XML. El método
	 * recorre la lista de nodos proporcionada, extrae los atributos necesarios para
	 * crear instancias de la entidad AulaEntity, y luego guarda todas las entidades
	 * creadas en la base de datos utilizando un repositorio.
	 *
	 * La información extraída incluye: - Abreviatura del aula. - Número interno del
	 * aula (numIntAu) (identificador). - Nombre del aula.
	 *
	 * @param aulasNodeList la lista de nodos XML que contienen la información de
	 *                      las aulas. Cada nodo debe tener atributos que
	 *                      representen la abreviatura, el número interno y el
	 *                      nombre del aula.
	 */
	private void saveValuesOfAula(NodeList aulasNodeList)
	{
		// Crea una nueva lista para ir almacenando las aulas que rellena.
		List<AulaEntity> listadoDeAulas = new ArrayList<>();

		// Por cada elemento aula en el nodo.
		for (int i = 0; i < aulasNodeList.getLength(); i++)
		{
			// Crea un nuevo objeto aula.
			AulaEntity aulaEntidad = new AulaEntity();

			// Configura abreviatura.
			aulaEntidad.setAbreviatura(aulasNodeList.item(i).getAttributes().item(0).getTextContent());

			// Configura identificador.
			aulaEntidad.setNumIntAu(aulasNodeList.item(i).getAttributes().item(2).getTextContent());

			// Configura nombre del aula.
			aulaEntidad.setNombre(aulasNodeList.item(i).getAttributes().item(1).getTextContent());

			// Agrega al listado de aulas.
			listadoDeAulas.add(aulaEntidad);
		}

		// Almacena todas las aulas listadas en BBDD.
		this.aulaRepo.saveAllAndFlush(listadoDeAulas);
		log.debug("Aulas almacenadas en BBDD");
	}

	/**
	 * Autor: David Jason G.
	 * 
	 * Guarda los valores de los grupos a partir de una lista de nodos XML. El
	 * método recorre la lista de nodos proporcionada, extrae los atributos
	 * necesarios para crear instancias de la entidad {@code GrupoEntity}, y luego
	 * guarda todas las entidades creadas en la base de datos utilizando un
	 * repositorio.
	 *
	 * La información extraída incluye: - Abreviatura del grupo. - Número interno
	 * del grupo (numIntGr). - Nombre del grupo.
	 *
	 * @param gruposNodeList la lista de nodos XML que contienen la información de
	 *                       los grupos. Cada nodo debe tener atributos que
	 *                       representen la abreviatura, el número interno y el
	 *                       nombre del grupo.
	 */
	private void saveValuesOfGrupo(NodeList gruposNodeList)
	{
		List<GrupoEntity> listadoGrupos = new ArrayList<>();

		for (int i = 0; i < gruposNodeList.getLength(); i++)
		{
			GrupoEntity newGrupo = new GrupoEntity();
			newGrupo.setAbreviatura(gruposNodeList.item(i).getAttributes().item(0).getTextContent());
			newGrupo.setNumIntGr(gruposNodeList.item(i).getAttributes().item(2).getTextContent());
			newGrupo.setNombre(gruposNodeList.item(i).getAttributes().item(1).getTextContent());

			listadoGrupos.add(newGrupo);
		}
		this.grupoRepo.saveAllAndFlush(listadoGrupos);
	}

	/**
	 * Autor: David Jason G.
	 * 
	 * Guarda los valores de las asignaturas a partir de una lista de nodos XML. El
	 * método recorre la lista de nodos proporcionada, extrae los atributos
	 * necesarios para crear instancias de la entidad AsignaturaEntity, y luego
	 * guarda todas las entidades creadas en la base de datos utilizando un
	 * repositorio.
	 *
	 * La información extraída incluye: - Abreviatura de la asignatura. - Número
	 * identificacdor de la asignatura (numIntAs). - Nombre de la asignatura.
	 *
	 * @param asignaturasNodeList la lista de nodos XML que contienen la información
	 *                            de las asignaturas. Cada nodo debe tener atributos
	 *                            que representen la abreviatura, el número interno
	 *                            y el nombre de la asignatura.
	 */
	private void saveValuesOfAsignatura(NodeList asignaturasNodeList)
	{
		List<AsignaturaEntity> listadoAsignaturas = new ArrayList<>();

		for (int i = 0; i < asignaturasNodeList.getLength(); i++)
		{
			AsignaturaEntity newAsignatura = new AsignaturaEntity();
			newAsignatura.setAbreviatura(asignaturasNodeList.item(i).getAttributes().item(0).getTextContent());
			newAsignatura.setNumIntAs(asignaturasNodeList.item(i).getAttributes().item(2).getTextContent());
			newAsignatura.setNombre(asignaturasNodeList.item(i).getAttributes().item(1).getTextContent());

			listadoAsignaturas.add(newAsignatura);
		}
		this.asignaturaRepo.saveAllAndFlush(listadoAsignaturas);
	}

	// ---------------- METODOS PARA ALMACENAR HORARIOS EN BBDD.
	
	/**
	 * Autor: David Jason G.
	 * 
	 * Procesa y guarda una actividad asociada a cada grupo presente en un mapa de nodos.
	 * 
	 * @param actividadEntity la entidad de la actividad que será asociada a los grupos.
	 * @param gruposActividadNodeMap un mapa de nodos que contiene los grupos relacionados con la actividad.
	 *                                Cada nodo representa un grupo único.
	 * 
	 * <p>Funcionamiento:</p>
	 * <ul>
	 *     <li>Itera sobre cada nodo del mapa de grupos de actividad.</li>
	 *     <li>Para cada nodo:
	 *         <ul>
	 *             <li>Obtiene la información del grupo.</li>
	 *             <li>Asocia la actividad al grupo mediante el método {@link #saveActividadWithGroup(Node, ActividadEntity)}.</li>
	 *         </ul>
	 *     </li>
	 * </ul>
	 * 
	 * <p>Este método garantiza que una entrada de actividad sea creada para cada grupo asociado,
	 * preservando la relación entre actividades y grupos.</p>
	 * 
	 * @see ActividadEntity
	 * @see #saveActividadWithGroup(Node, ActividadEntity)
	 */
	private void saveValuesOfGruposActividadAttrs(ActividadEntity actividadEntity, NamedNodeMap gruposActividadNodeMap)
	{
		// Por cada grupo en el nodo de grupos.
		for (int i = 0; i < gruposActividadNodeMap.getLength(); i++)
		{
			// Asigna nodo.
			Node node = gruposActividadNodeMap.item(i);

			// Guarda una instancia de la actividad con el grupo del iteración actual.
			this.saveActividadWithGroup(node, actividadEntity);
		}
	}

	/**
	 * Autor: David Jason G.
	 * 
	 * Asocia una actividad a un grupo específico y guarda la relación en la base de datos.
	 * 
	 * @param node el nodo XML que representa un grupo. Su contenido debe incluir el identificador del grupo.
	 * @param actividadEntity la entidad de actividad que será asociada al grupo.
	 * 
	 * <p>Funcionamiento:</p>
	 * <ul>
	 *     <li>Verifica si el nombre del nodo contiene la palabra clave "grupo_".</li>
	 *     <li>Recupera el identificador del grupo desde el contenido del nodo.</li>
	 *     <li>Busca en el repositorio la entidad {@link GrupoEntity} correspondiente al identificador.</li>
	 *     <li>Asocia la entidad del grupo a la actividad mediante {@link ActividadEntity#setGrupo(GrupoEntity)}.</li>
	 *     <li>Guarda la actividad actualizada en la base de datos utilizando el repositorio de actividades.</li>
	 *     <li>Registra la operación mediante un mensaje de depuración.</li>
	 * </ul>
	 * 
	 * <p><strong>Nota:</strong> Este método crea una nueva entrada de actividad para cada grupo asociado.
	 * Esto reemplaza la lógica anterior de reutilizar una actividad única con múltiples referencias a grupos.</p>
	 * 
	 * @throws NoSuchElementException si no se encuentra el grupo en el repositorio {@code grupoRepo}.
	 * 
	 * @see ActividadEntity
	 * @see GrupoEntity
	 * @see ActividadRepository
	 * @see GrupoRepository
	 */
	private void saveActividadWithGroup(Node node, ActividadEntity actividadEntity)
	{
		if (node.getNodeName().contains("grupo_"))
		{
			// Recupera el ID.
			String idGrupo = node.getTextContent();
			// Busca el grupo correspondiente al ID recuperado.
			GrupoEntity nuevoGrupo = grupoRepo.findById(idGrupo).get();

			// Setea el grupo a la nueva actividad.
			actividadEntity.setGrupo(nuevoGrupo);

			// Guarda en base de datos.
			this.actividadRepo.saveAndFlush(actividadEntity);
			log.debug( "Grupo asignado a actividad: {}" , nuevoGrupo.toString() );
		}
	}
	
	/**
	 * Autor: David Jason G.
	 * 
	 * Procesa una lista de nodos XML que representan horarios de asignaturas,
	 * recupera datos de las actividades asociadas y las guarda en la base de datos 
	 * (SOLO ALMACENA REGISTROS DE LA ACTIVIDAD).
	 *
	 * @param horarioAsigNodeList una lista de nodos XML que representan horarios de asignaturas.
	 *                            Cada nodo debe contener datos de actividades asociadas.
	 * @throws Exception si ocurre un error durante el procesamiento, como la falta de una entidad
	 *                   requerida (Asignatura, Aula, Profesor o Tramo) en la base de datos.
	 *
	 * <p>Funcionamiento:</p>
	 * <ul>
	 *     <li>Por cada nodo en la lista de horarios de asignaturas:</li>
	 *     <ul>
	 *         <li>Obtiene los nodos de actividades asociadas al horario de asignatura.</li>
	 *         <li>Por cada actividad encontrada:</li>
	 *         <ul>
	 *             <li>Crea una instancia de {@link ActividadEntity} y asigna sus atributos
	 *                 basándose en la información de los nodos XML y los repositorios.</li>
	 *             <li>Recupera entidades relacionadas (Aula, Profesor, Tramo, Asignatura)
	 *                 y las asigna a la actividad. Lanza una excepción si alguna de estas entidades
	 *                 no se encuentra en la base de datos.</li>
	 *             <li>Obtiene los datos de los grupos asociados a la actividad y utiliza
	 *                 el método {@link #saveValuesOfGruposActividadAttrs(ActividadEntity, NamedNodeMap)}
	 *                 para procesarlos.</li>
	 *         </ul>
	 *     </ul>
	 * </ul>
	 *
	 * <p><strong>Nota:</strong> Las entidades relacionadas son críticas para completar el
	 * registro de la actividad. Si alguna de ellas no está presente en la base de datos,
	 * el método lanza una excepción para garantizar la integridad de los datos.</p>
	 *
	 * @see ActividadEntity
	 * @see AsignaturaEntity
	 * @see AulaEntity
	 * @see ProfesorEntity
	 * @see TimeSlotEntity
	 * @see #saveValuesOfGruposActividadAttrs(ActividadEntity, NamedNodeMap)
	 */
	private void saveValuesOfHorarioAsig(NodeList horarioAsigNodeList) throws Exception
	{
		// Por cada elemento en el nodemap de horario asignatura.
		for (int i = 0; i < horarioAsigNodeList.getLength(); i++)
		{

			// Instrucciones para recoger el nodelist de Actividad.
			Element horarioAsigElement = (Element) horarioAsigNodeList.item(i);
			NodeList actividadNodeList = horarioAsigElement.getElementsByTagName("ACTIVIDAD");

			// POR CADA ACTIVIDAD EN EL NODELIST DE ACTIVIDADES DE HORARIO ASIGNATURAS.
			for (int j = 0; j < actividadNodeList.getLength(); j++)
			{
				// Crea una nuevo objeto de actividad en bbdd (nuevo registro actividad).
				ActividadEntity actividadEntity = new ActividadEntity();

				// ASIGNACIÓN DE REFERENCIAS A OTRAS ENTIDADES EN EL REGISTRO TRATADO EN
				// ITERACIÓN ACTUAL.

				// Referencia al AULA de la actividad (referencia a clave primaria).
				String aulaId = actividadNodeList.item(j).getAttributes().item(0).getTextContent();
				Optional<AulaEntity> aula = aulaRepo.findById(aulaId);
				// aqui peta
				if (!aula.isPresent())
				{
					String mensaje = "El AULA referenciada en saveValuesOfHorarioAsig no encontrada.";
					log.error(mensaje);
					throw new Exception(mensaje);
				}
				actividadEntity.setAula(aula.get());

				// Asigna atributo de Numero Actividad.
				actividadEntity.setNumAct(actividadNodeList.item(j).getAttributes().item(1).getTextContent());

				// Asigna atributo de Numero Unidad
				actividadEntity.setNumUn(actividadNodeList.item(j).getAttributes().item(2).getTextContent());

				// Referencia al PROFESOR de la actividad (referencia a clave primaria).
				String profeId = actividadNodeList.item(j).getAttributes().item(3).getTextContent();
				Optional<ProfesorEntity> profe = profesorRepo.findById(profeId);
				if (!profe.isPresent())
				{
					String mensaje = "El PROFESOR referenciado en saveValuesOfHorarioAsig no encontrado.";
					log.error(mensaje);
					throw new Exception(mensaje);
				}
				actividadEntity.setProfesor(profe.get());

				// Referencia al TRAMO HORARIO de la actividad (referencia a clave primaria).
				String tramoId = actividadNodeList.item(j).getAttributes().item(4).getTextContent();
				Optional<TimeSlotEntity> tramo = timeslotRepo.findById(tramoId);
				if (!tramo.isPresent())
				{
					String mensaje = "El TRAMO HORARIO referenciado en saveValuesOfHorarioAsig no encontrado.";
					log.error(mensaje);
					throw new Exception(mensaje);
				}
				actividadEntity.setTramo(tramo.get());

				// ATENCION: NO ES EL MISMO MECANISMO QUE PARA EL RESTO DE REFERENCIAS.
				// Recupera el ID que identifica la asignatura de la iteración actual.
				String asignaturaId = horarioAsigNodeList.item(i).getAttributes().item(0).getTextContent();
				Optional<AsignaturaEntity> asignatura = asignaturaRepo.findById(asignaturaId);
				if (!asignatura.isPresent())
				{
					String mensaje = "La ASIGNATURA referenciada en saveValuesOfHorarioAsig no encontrada.";
					log.error(mensaje);
					throw new Exception(mensaje);
				}
				actividadEntity.setAsignatura(asignatura.get());

				// recoge el nodelist de grupos actividad.
				NamedNodeMap gruposActividadNodeMap = ((Element) actividadNodeList.item(j))
						.getElementsByTagName("GRUPOS_ACTIVIDAD").item(0).getAttributes();

				this.saveValuesOfGruposActividadAttrs(actividadEntity, gruposActividadNodeMap);
			}

		}
	}
	
	private void saveValuesOfHorarioGrup(NodeList horarioGrupNodeList) throws Exception
	{
		// Por cada horario de grupo en el nodo.
		for (int i = 0; i < horarioGrupNodeList.getLength(); i++)
		{

			// GETTING ELEMENT HORARIO_GRUP (i)
			Element horarioGrupElement = (Element) horarioGrupNodeList.item(i);
			// GETTING THE ACTIVIDAD NODE LIST
			NodeList actividadNodeList = horarioGrupElement.getElementsByTagName("ACTIVIDAD");

			for (int j = 0; j < actividadNodeList.getLength(); j++)
			{
				ActividadEntity actividadEntity = new ActividadEntity();

				String aulaId = actividadNodeList.item(j).getAttributes().item(1).getTextContent();
				String tramoId = actividadNodeList.item(j).getAttributes().item(5).getTextContent();
				String profeId = actividadNodeList.item(j).getAttributes().item(4).getTextContent();
				String asignaturaId = actividadNodeList.item(j).getAttributes().item(0).getTextContent();
				
				// Recupera entidades.
				Optional <AulaEntity> aula = aulaRepo.findById(aulaId);
				Optional <TimeSlotEntity> tramo = timeslotRepo.findById(tramoId);
				Optional <ProfesorEntity> profesor = profesorRepo.findById(profeId);
				Optional <AsignaturaEntity> asignatura = asignaturaRepo.findById(asignaturaId);
				
				// Bloque de control que levanta error si alguna entidad es nula (no se
				// encuentra en bbdd.)
				if (asignatura.isEmpty())
				{
					throw new Exception("Objeto Asignatura en saveValuesOfHorarioAula es nulo.");
				}
				if (aula.isEmpty())
				{
					throw new Exception("Objeto Aula en saveValuesOfHorarioAula es nulo.");
				}
				if (profesor.isEmpty())
				{
					throw new Exception("Objeto Profesor en saveValuesOfHorarioAula es nulo.");
				}
				if (tramo.isEmpty())
				{
					throw new Exception("Objeto Tramo en saveValuesOfHorarioAula es nulo.");
				}
				
				
				// Configura atributos planos.
				actividadEntity.setNumAct(actividadNodeList.item(j).getAttributes().item(2).getTextContent());
				actividadEntity.setNumUn(actividadNodeList.item(j).getAttributes().item(3).getTextContent());
				
				// Configura atributos entidad.
				actividadEntity.setAula(aula.get());
				actividadEntity.setTramo(tramo.get());
				actividadEntity.setProfesor(profesor.get());
				actividadEntity.setAsignatura(asignatura.get());

				// Almacena actividad configurada en BBDD.
				actividadRepo.saveAndFlush(actividadEntity);

			}
		}
	}

	/**
	 * Por cada aula en el nodo AULAS configura una nueva entidad de Aula y la
	 * guarda en BBDD invocando al metodo de guardado por grupos. Este metodo no
	 * guarda nada en BBDD por si mismo, esa funciona la realiza el metodo
	 * 'saveValuesOfGruposActividadAttrs'.
	 * 
	 * @param horarioAulaNodeList - Nodo XML con el listado de aulas.
	 * @throws Exception - Error en caso de que alguna de las entidades recuperadas
	 *                   presente error.
	 */
	private void saveValuesOfHorarioAula(NodeList horarioAulaNodeList) throws Exception
	{
		// Por cada entrada de horario
		for (int i = 0; i < horarioAulaNodeList.getLength(); i++)
		{
			// Identificador de aula recuperado en nodo padre.
			String idAula = horarioAulaNodeList.item(i).getAttributes().item(0).getTextContent();

			// Recupera datos de la actividad.
			Element horarioAulaElement = (Element) horarioAulaNodeList.item(i);
			NodeList actividadNodeList = horarioAulaElement.getElementsByTagName("ACTIVIDAD");

			// Por cada elemento actividad en el nodelist.
			for (int j = 0; j < actividadNodeList.getLength(); j++)
			{
				// Crea nueva actividad.
				ActividadEntity actividadEntity = new ActividadEntity();

				// Recupera los datos del nodo XML.
				String idProfe = actividadNodeList.item(j).getAttributes().item(3).getTextContent();
				String idTramo = actividadNodeList.item(j).getAttributes().item(4).getTextContent();
				String numUn = actividadNodeList.item(j).getAttributes().item(2).getTextContent();
				String numAct = actividadNodeList.item(j).getAttributes().item(1).getTextContent();
				String idAsignatura = actividadNodeList.item(j).getAttributes().item(0).getTextContent();

				// Recupera las entidades objetivo.
				Optional<AsignaturaEntity> asignatura = asignaturaRepo.findById(idAsignatura);
				Optional<AulaEntity> aula = aulaRepo.findById(idAula);
				Optional<ProfesorEntity> profesor = profesorRepo.findById(idProfe);
				Optional<TimeSlotEntity> tramo = timeslotRepo.findById(idTramo);

				// Bloque de control que levanta error si alguna entidad es nula (no se
				// encuentra en bbdd.)
				if (asignatura.isEmpty())
				{
					throw new Exception("Objeto Asignatura en saveValuesOfHorarioAula es nulo.");
				}
				if (aula.isEmpty())
				{
					throw new Exception("Objeto Aula en saveValuesOfHorarioAula es nulo.");
				}
				if (profesor.isEmpty())
				{
					throw new Exception("Objeto Profesor en saveValuesOfHorarioAula es nulo.");
				}
				if (tramo.isEmpty())
				{
					throw new Exception("Objeto Tramo en saveValuesOfHorarioAula es nulo.");
				}

				// Atributos simples (String o Integer)
				actividadEntity.setNumAct(numAct);
				actividadEntity.setNumUn(numUn);

				// Configura los campos recuperando entidades interesadas.
				// ATENCION: Los grupos se administran diferente, ver metodo
				// 'saveValuesOfGruposActividadAttrs'.
				actividadEntity.setAsignatura(asignatura.get());
				actividadEntity.setAula(aula.get());
				actividadEntity.setProfesor(profesor.get());
				actividadEntity.setTramo(tramo.get());

				// Recupera el listado de grupos de la actividad.
				NamedNodeMap gruposActividadNodeMap = ((Element) actividadNodeList.item(j))
						.getElementsByTagName("GRUPOS_ACTIVIDAD").item(0).getAttributes();

				// Metodo que almacenar en base de datos un registro por cada grupo asociado a
				// la actividad.
				this.saveValuesOfGruposActividadAttrs(actividadEntity, gruposActividadNodeMap);
			}
		}
	}
	
	private void saveValuesOfHorarioProf(NodeList horarioProfNodeList) throws Exception
	{
		// Por cada elemento en el nodelist.
		for (int i = 0; i < horarioProfNodeList.getLength(); i++)
		{
			// Identificador de profesor recuperado en nodo padre.
			String idProfe = horarioProfNodeList.item(i).getAttributes().item(0).getTextContent();
			
			// Recupera datos de la actividad.
			Element horarioProfElement = (Element) horarioProfNodeList.item(i);
			NodeList actividadNodeList = horarioProfElement.getElementsByTagName("ACTIVIDAD");

			for (int j = 0; j < actividadNodeList.getLength(); j++)
			{
				// Crea nueva entidad.
				ActividadEntity actividadEntity = new ActividadEntity();
				
				// Recupera entidades interesadas.
				String idAsignatura = actividadNodeList.item(j).getAttributes().item(0).getTextContent();
				String idTramo = actividadNodeList.item(j).getAttributes().item(4).getTextContent();
				String idAula = actividadNodeList.item(j).getAttributes().item(1).getTextContent();
				String numAct = actividadNodeList.item(j).getAttributes().item(2).getTextContent();
				String numUn = actividadNodeList.item(j).getAttributes().item(3).getTextContent();
				
				// Recupera las entidades objetivo.
				Optional<AsignaturaEntity> asignatura = asignaturaRepo.findById(idAsignatura);
				Optional<AulaEntity> aula = aulaRepo.findById(idAula);
				Optional<ProfesorEntity> profesor = profesorRepo.findById(idProfe);
				Optional<TimeSlotEntity> tramo = timeslotRepo.findById(idTramo);

				// Bloque de control que levanta error si alguna entidad es nula (no se
				// encuentra en bbdd.)
				if (asignatura.isEmpty())
				{
					throw new Exception("Objeto Asignatura en saveValuesOfHorarioAula es nulo.");
				}
				if (aula.isEmpty())
				{
					throw new Exception("Objeto Aula en saveValuesOfHorarioAula es nulo.");
				}
				if (profesor.isEmpty())
				{
					throw new Exception("Objeto Profesor en saveValuesOfHorarioAula es nulo.");
				}
				if (tramo.isEmpty())
				{
					throw new Exception("Objeto Tramo en saveValuesOfHorarioAula es nulo.");
				}
				
				// Configura atributos planos o simples (String o Integer)
				actividadEntity.setNumAct(numAct);
				actividadEntity.setNumUn(numUn);
				
				// Configura entidad de actividad.
				actividadEntity.setProfesor(profesor.get());
				actividadEntity.setAsignatura(asignatura.get());
				actividadEntity.setAula(aula.get());
				actividadEntity.setTramo(tramo.get());
				
				// Recupera el listado de grupos de la actividad.
				NamedNodeMap gruposActividadNodeMap = ((Element) actividadNodeList.item(j)).getElementsByTagName("GRUPOS_ACTIVIDAD").item(0).getAttributes();

				// Metodo que almacenar en base de datos un registro por cada grupo asociado a
				// la actividad.
				this.saveValuesOfGruposActividadAttrs(actividadEntity, gruposActividadNodeMap);

			}
		}
	}

	// --------------------  METODOS DE ALMACENAMIENTO EN SESION.
	
	/**
	 * Method gettingValuesOfTramo
	 *
	 * @param tramosHorariosNodeList
	 * @param tramosList
	 */
	private void gettingValuesOfTramo(NodeList tramosHorariosNodeList, List<TimeSlot> tramosList)
	{
		for (int i = 0; i < tramosHorariosNodeList.getLength(); i++)
		{
			TimeSlot newTramo = new TimeSlot();
			newTramo.setEndHour(tramosHorariosNodeList.item(i).getAttributes().item(0).getTextContent());
			newTramo.setStartHour(tramosHorariosNodeList.item(i).getAttributes().item(1).getTextContent());
			newTramo.setDayNumber(tramosHorariosNodeList.item(i).getAttributes().item(3).getTextContent());
			newTramo.setNumTr(tramosHorariosNodeList.item(i).getAttributes().item(2).getTextContent());

			tramosList.add(newTramo);
		}
	}

	/**
	 * Method gettingValuesOfProfesor
	 *
	 * @param profesoresNodeList
	 * @param profesoresList
	 */
	private void gettingValuesOfProfesor(NodeList profesoresNodeList, List<Profesor> profesoresList)
	{
		for (int i = 0; i < profesoresNodeList.getLength(); i++)
		{
			Profesor newProfesor = new Profesor();
			newProfesor.setAbreviatura(profesoresNodeList.item(i).getAttributes().item(0).getTextContent());
			newProfesor.setNumIntPR(profesoresNodeList.item(i).getAttributes().item(2).getTextContent());

			// --- GETTING FULL NAME ---
			String nombreCompleto = profesoresNodeList.item(i).getAttributes().item(1).getTextContent();
			String[] nombreCompletoSpit = nombreCompleto.split(",");
			// --- GETING LASTNAME ---
			String[] apellidosSplit = nombreCompletoSpit[0].split(" ");

			/// --- SETTING VALUES ---
			newProfesor.setNombre(nombreCompletoSpit[nombreCompletoSpit.length - 1].trim());
			newProfesor.setPrimerApellido(apellidosSplit[0].trim());
			newProfesor.setSegundoApellido(apellidosSplit[1].trim());

			profesoresList.add(newProfesor);
		}
	}

	/**
	 * Method gettingValuesOfAula
	 *
	 * @param aulasNodeList
	 * @param aulasList
	 */
	private void gettingValuesOfAula(NodeList aulasNodeList, List<Aula> aulasList)
	{
		for (int i = 0; i < aulasNodeList.getLength(); i++)
		{
			Aula newAula = new Aula();
			newAula.setAbreviatura(aulasNodeList.item(i).getAttributes().item(0).getTextContent());
			newAula.setNumIntAu(aulasNodeList.item(i).getAttributes().item(2).getTextContent());
			newAula.setNombre(aulasNodeList.item(i).getAttributes().item(1).getTextContent());

			aulasList.add(newAula);
		}
	}

	/**
	 * Method gettingValuesOfGrupo
	 *
	 * @param gruposNodeList
	 * @param gruposList
	 */
	private void gettingValuesOfGrupo(NodeList gruposNodeList, List<Grupo> gruposList)
	{
		for (int i = 0; i < gruposNodeList.getLength(); i++)
		{
			Grupo newGrupo = new Grupo();
			newGrupo.setAbreviatura(gruposNodeList.item(i).getAttributes().item(0).getTextContent());
			newGrupo.setNumIntGr(gruposNodeList.item(i).getAttributes().item(2).getTextContent());
			newGrupo.setNombre(gruposNodeList.item(i).getAttributes().item(1).getTextContent());

			gruposList.add(newGrupo);
		}
	}

	/**
	 * Method gettingValuesOfAsignatura
	 *
	 * @param asignaturasNodeList
	 * @param asignaturasList
	 */
	private void gettingValuesOfAsignatura(NodeList asignaturasNodeList, List<Asignatura> asignaturasList)
	{
		for (int i = 0; i < asignaturasNodeList.getLength(); i++)
		{
			Asignatura newAsignatura = new Asignatura();
			newAsignatura.setAbreviatura(asignaturasNodeList.item(i).getAttributes().item(0).getTextContent());
			newAsignatura.setNumIntAs(asignaturasNodeList.item(i).getAttributes().item(2).getTextContent());
			newAsignatura.setNombre(asignaturasNodeList.item(i).getAttributes().item(1).getTextContent());

			asignaturasList.add(newAsignatura);
		}
	}

	/**
	 * Method gettingValuesOfHorarioProf
	 *
	 * @param horarioProfNodeList
	 * @param horarioProfList
	 */
	private void gettingValuesOfHorarioProf(NodeList horarioProfNodeList, List<HorarioProf> horarioProfList)
	{
		for (int i = 0; i < horarioProfNodeList.getLength(); i++)
		{
			HorarioProf newHorarioProf = new HorarioProf();
			newHorarioProf.setHorNumIntPR(horarioProfNodeList.item(i).getAttributes().item(0).getTextContent());
			newHorarioProf.setTotAC(horarioProfNodeList.item(i).getAttributes().item(1).getTextContent());
			newHorarioProf.setTotUn(horarioProfNodeList.item(i).getAttributes().item(2).getTextContent());

			// GETTING ELEMENT HORARIO_PROF(i)
			Element horarioProfElement = (Element) horarioProfNodeList.item(i);
			// GETTING THE ACTIVIDAD NODE LIST
			NodeList actividadNodeList = horarioProfElement.getElementsByTagName("ACTIVIDAD");

			// --- ACTIVIDAD LIST ---
			List<Actividad> actividadList = new ArrayList<>();
			for (int j = 0; j < actividadNodeList.getLength(); j++)
			{
				Actividad newActividad = new Actividad();
				newActividad.setAsignatura(actividadNodeList.item(j).getAttributes().item(0).getTextContent());
				newActividad.setNumAct(actividadNodeList.item(j).getAttributes().item(2).getTextContent());
				newActividad.setNumUn(actividadNodeList.item(j).getAttributes().item(3).getTextContent());
				newActividad.setTramo(actividadNodeList.item(j).getAttributes().item(4).getTextContent());
				newActividad.setAula(actividadNodeList.item(j).getAttributes().item(1).getTextContent());

				// --- GETTING GRUPOS ACTIVIDAD ---
				GruposActividad gruposActividad = new GruposActividad();
				// ((Element)actividadNodeList.item(j)).getElementsByTagName("GRUPOS_ACTIVIDAD").item(0).getAttributes().item(0).getTextContent()

				// --- IF THE ELEMENT GRUPOS_ACTIVIDAD HAS 1 , 2, 3 , 4 OR 5 ATTRIBUTES---
				NamedNodeMap gruposActividadNodeMap = ((Element) actividadNodeList.item(j))
						.getElementsByTagName("GRUPOS_ACTIVIDAD").item(0).getAttributes();
				this.gettingValuesOfGruposActividadAttrs(actividadList, newActividad, gruposActividad,
						gruposActividadNodeMap);
			}
			// --- ADD ACTIVIDAD LIST TO HORARIO_AULA
			newHorarioProf.setActividad(actividadList);
			// log.info(actividadList.toString());

			// -- ADD HORARIO_AULA TO LIST ---
			horarioProfList.add(newHorarioProf);
		}
	}

	private void gettingValuesOfHorarioAula(NodeList horarioAulaNodeList, List<HorarioAula> horarioAulaList)
	{
		for (int i = 0; i < horarioAulaNodeList.getLength(); i++)
		{
			HorarioAula newHorarioAula = new HorarioAula();
			newHorarioAula.setHorNumIntAu(horarioAulaNodeList.item(i).getAttributes().item(0).getTextContent());
			newHorarioAula.setTotAC(horarioAulaNodeList.item(i).getAttributes().item(1).getTextContent());
			newHorarioAula.setTotUn(horarioAulaNodeList.item(i).getAttributes().item(2).getTextContent());

			// GETTING ELEMENT HORARIO AULA (i)
			Element horarioAulaElement = (Element) horarioAulaNodeList.item(i);
			// GETTING THE ACTIVIDAD NODE LIST
			NodeList actividadNodeList = horarioAulaElement.getElementsByTagName("ACTIVIDAD");

			// --- ACTIVIDAD LIST ---
			List<Actividad> actividadList = new ArrayList<>();
			for (int j = 0; j < actividadNodeList.getLength(); j++)
			{
				Actividad newActividad = new Actividad();
				newActividad.setAsignatura(actividadNodeList.item(j).getAttributes().item(0).getTextContent());
				newActividad.setNumAct(actividadNodeList.item(j).getAttributes().item(1).getTextContent());
				newActividad.setNumUn(actividadNodeList.item(j).getAttributes().item(2).getTextContent());
				newActividad.setTramo(actividadNodeList.item(j).getAttributes().item(4).getTextContent());
				newActividad.setProfesor(actividadNodeList.item(j).getAttributes().item(3).getTextContent());

				// --- GETTING GRUPOS ACTIVIDAD ---
				GruposActividad gruposActividad = new GruposActividad();
				// ((Element)actividadNodeList.item(j)).getElementsByTagName("GRUPOS_ACTIVIDAD").item(0).getAttributes().item(0).getTextContent()

				// --- IF THE ELEMENT GRUPOS_ACTIVIDAD HAS 1 , 2, 3 , 4 OR 5 ATTRIBUTES---
				NamedNodeMap gruposActividadNodeMap = ((Element) actividadNodeList.item(j))
						.getElementsByTagName("GRUPOS_ACTIVIDAD").item(0).getAttributes();
				this.gettingValuesOfGruposActividadAttrs(actividadList, newActividad, gruposActividad,
						gruposActividadNodeMap);
			}
			// --- ADD ACTIVIDAD LIST TO HORARIO_AULA
			newHorarioAula.setActividad(actividadList);
			// log.info(actividadList.toString());

			// -- ADD HORARIO_AULA TO LIST ---
			horarioAulaList.add(newHorarioAula);
		}
	}
	/**
	 * Method gettingValuesOfHorarioGrup
	 *
	 * @param horarioGrupNodeList
	 * @param horarioGrupList
	 */
	private void gettingValuesOfHorarioGrup(NodeList horarioGrupNodeList, List<HorarioGrup> horarioGrupList)
	{
		for (int i = 0; i < horarioGrupNodeList.getLength(); i++)
		{
			HorarioGrup newHorarioGrup = new HorarioGrup();
			newHorarioGrup.setHorNumIntGr(horarioGrupNodeList.item(i).getAttributes().item(0).getTextContent());
			newHorarioGrup.setTotAC(horarioGrupNodeList.item(i).getAttributes().item(1).getTextContent());
			newHorarioGrup.setTotUn(horarioGrupNodeList.item(i).getAttributes().item(2).getTextContent());

			// GETTING ELEMENT HORARIO_GRUP (i)
			Element horarioGrupElement = (Element) horarioGrupNodeList.item(i);
			// GETTING THE ACTIVIDAD NODE LIST
			NodeList actividadNodeList = horarioGrupElement.getElementsByTagName("ACTIVIDAD");

			// --- ACTIVIDAD LIST ---
			List<Actividad> actividadList = new ArrayList<>();
			for (int j = 0; j < actividadNodeList.getLength(); j++)
			{
				Actividad newActividad = new Actividad();
				newActividad.setAula(actividadNodeList.item(j).getAttributes().item(1).getTextContent());
				newActividad.setNumAct(actividadNodeList.item(j).getAttributes().item(2).getTextContent());
				newActividad.setNumUn(actividadNodeList.item(j).getAttributes().item(3).getTextContent());
				newActividad.setTramo(actividadNodeList.item(j).getAttributes().item(5).getTextContent());
				newActividad.setProfesor(actividadNodeList.item(j).getAttributes().item(4).getTextContent());
				newActividad.setAsignatura(actividadNodeList.item(j).getAttributes().item(0).getTextContent());

				actividadList.add(newActividad);
			}
			// --- ADD ACTIVIDAD LIST TO HORARIO_GRUP
			newHorarioGrup.setActividad(actividadList);
			// log.info(actividadList.toString());

			// -- ADD HORARIO_GRUP TO LIST ---
			horarioGrupList.add(newHorarioGrup);
		}
	}

	/**
	 * Method gettingValuesOfHorarioAsig
	 *
	 * @param horarioAsigNodeList
	 * @param horarioAsigList
	 */
	private void gettingValuesOfHorarioAsig(NodeList horarioAsigNodeList, List<HorarioAsig> horarioAsigList)
	{
		for (int i = 0; i < horarioAsigNodeList.getLength(); i++)
		{
			HorarioAsig newHorarioAsig = new HorarioAsig();
			newHorarioAsig.setHorNumIntAs(horarioAsigNodeList.item(i).getAttributes().item(0).getTextContent());
			newHorarioAsig.setTotAC(horarioAsigNodeList.item(i).getAttributes().item(1).getTextContent());
			newHorarioAsig.setTotUn(horarioAsigNodeList.item(i).getAttributes().item(2).getTextContent());

			// GETTING ELEMENT HORARIO ASIG (i)
			Element horarioAsigElement = (Element) horarioAsigNodeList.item(i);
			// GETTING THE ACTIVIDAD NODE LIST
			NodeList actividadNodeList = horarioAsigElement.getElementsByTagName("ACTIVIDAD");

			// --- ACTIVIDAD LIST ---
			List<Actividad> actividadList = new ArrayList<>();
			this.gettingValuesOfActividad(actividadNodeList, actividadList);
			// --- ADD ACTIVIDAD LIST TO HORARIO_ASIG
			newHorarioAsig.setActividad(actividadList);
			// log.info(actividadList.toString());

			// -- ADD HORARIO_ASIG TO LIST ---
			horarioAsigList.add(newHorarioAsig);
		}
	}

	/**
	 * Method gettingValuesOfActividad
	 *
	 * @param actividadNodeList
	 * @param actividadList
	 */
	private void gettingValuesOfActividad(NodeList actividadNodeList, List<Actividad> actividadList)
	{
		for (int j = 0; j < actividadNodeList.getLength(); j++)
		{
			Actividad newActividad = new Actividad();
			newActividad.setAula(actividadNodeList.item(j).getAttributes().item(0).getTextContent());
			newActividad.setNumAct(actividadNodeList.item(j).getAttributes().item(1).getTextContent());
			newActividad.setNumUn(actividadNodeList.item(j).getAttributes().item(2).getTextContent());
			newActividad.setTramo(actividadNodeList.item(j).getAttributes().item(4).getTextContent());
			newActividad.setProfesor(actividadNodeList.item(j).getAttributes().item(3).getTextContent());

			// --- GETTING GRUPOS ACTIVIDAD ---
			GruposActividad gruposActividad = new GruposActividad();
			// ((Element)actividadNodeList.item(j)).getElementsByTagName("GRUPOS_ACTIVIDAD").item(0).getAttributes().item(0).getTextContent()

			// --- IF THE ELEMENT GRUPOS_ACTIVIDAD HAS 1 , 2, 3 , 4 OR 5 ATTRIBUTES---
			NamedNodeMap gruposActividadNodeMap = ((Element) actividadNodeList.item(j))
					.getElementsByTagName("GRUPOS_ACTIVIDAD").item(0).getAttributes();
			this.gettingValuesOfGruposActividadAttrs(actividadList, newActividad, gruposActividad,
					gruposActividadNodeMap);
		}
	}

	/**
	 * Method gettingValuesOfGruposActividadAttrs
	 *
	 * @param actividadList
	 * @param newActividad
	 * @param gruposActividad
	 * @param gruposActividadNodeMapgettingValuesOfActividad
	 */
	private void gettingValuesOfGruposActividadAttrs(List<Actividad> actividadList, Actividad newActividad,
			GruposActividad gruposActividad, NamedNodeMap gruposActividadNodeMap)
	{
		if (gruposActividadNodeMap.getLength() == 1)
		{
			// 1 ATTR CASE
			Node node = gruposActividadNodeMap.item(0);
			gruposActividad = this.getGruposActividadAttributeTexts(gruposActividad, node);

		}
		if (gruposActividadNodeMap.getLength() == 2)
		{
			// 2 ATTR CASE
			Node nodeOne = gruposActividadNodeMap.item(0);
			Node nodeTwo = gruposActividadNodeMap.item(1);

			gruposActividad = this.getGruposActividadAttributeTexts(gruposActividad, nodeOne);
			gruposActividad = this.getGruposActividadAttributeTexts(gruposActividad, nodeTwo);

		}
		if (gruposActividadNodeMap.getLength() == 3)
		{
			// 3 ATTR CASE
			Node nodeOne = gruposActividadNodeMap.item(0);
			Node nodeTwo = gruposActividadNodeMap.item(1);
			Node nodeThree = gruposActividadNodeMap.item(2);

			gruposActividad = this.getGruposActividadAttributeTexts(gruposActividad, nodeOne);
			gruposActividad = this.getGruposActividadAttributeTexts(gruposActividad, nodeTwo);
			gruposActividad = this.getGruposActividadAttributeTexts(gruposActividad, nodeThree);

		}
		if (gruposActividadNodeMap.getLength() == 4)
		{
			// 4 ATTR CASE
			Node nodeOne = gruposActividadNodeMap.item(0);
			Node nodeTwo = gruposActividadNodeMap.item(1);
			Node nodeThree = gruposActividadNodeMap.item(2);
			Node nodeFour = gruposActividadNodeMap.item(3);

			gruposActividad = this.getGruposActividadAttributeTexts(gruposActividad, nodeOne);
			gruposActividad = this.getGruposActividadAttributeTexts(gruposActividad, nodeTwo);
			gruposActividad = this.getGruposActividadAttributeTexts(gruposActividad, nodeThree);
			gruposActividad = this.getGruposActividadAttributeTexts(gruposActividad, nodeFour);

		}
		if (gruposActividadNodeMap.getLength() == 5)
		{
			// 5 ATTR CASE
			Node nodeOne = gruposActividadNodeMap.item(0);
			Node nodeTwo = gruposActividadNodeMap.item(1);
			Node nodeThree = gruposActividadNodeMap.item(2);
			Node nodeFour = gruposActividadNodeMap.item(3);
			Node nodeFive = gruposActividadNodeMap.item(4);

			gruposActividad = this.getGruposActividadAttributeTexts(gruposActividad, nodeOne);
			gruposActividad = this.getGruposActividadAttributeTexts(gruposActividad, nodeTwo);
			gruposActividad = this.getGruposActividadAttributeTexts(gruposActividad, nodeThree);
			gruposActividad = this.getGruposActividadAttributeTexts(gruposActividad, nodeFour);
			gruposActividad = this.getGruposActividadAttributeTexts(gruposActividad, nodeFive);

		}
		if (gruposActividadNodeMap.getLength() == 6)
		{
			// 6 ATTR CASE
			Node nodeOne = gruposActividadNodeMap.item(0);
			Node nodeTwo = gruposActividadNodeMap.item(1);
			Node nodeThree = gruposActividadNodeMap.item(2);
			Node nodeFour = gruposActividadNodeMap.item(3);
			Node nodeFive = gruposActividadNodeMap.item(4);
			Node nodeSix = gruposActividadNodeMap.item(5);
			gruposActividad = this.getGruposActividadAttributeTexts(gruposActividad, nodeOne);
			gruposActividad = this.getGruposActividadAttributeTexts(gruposActividad, nodeTwo);
			gruposActividad = this.getGruposActividadAttributeTexts(gruposActividad, nodeThree);
			gruposActividad = this.getGruposActividadAttributeTexts(gruposActividad, nodeFour);
			gruposActividad = this.getGruposActividadAttributeTexts(gruposActividad, nodeFive);
			gruposActividad = this.getGruposActividadAttributeTexts(gruposActividad, nodeSix);
		}
		newActividad.setGruposActividad(gruposActividad);
		// log.info(gruposActividad.toString());
		actividadList.add(newActividad);
	}

	// ------------------ METODOS DE RECUPERACIÓN DE DATOS.
	
	/**
	 * Recupera el listado de profesores.
	 * 
	 * @param session
	 * @return
	 */
	@RequestMapping(value = "/get/teachers", produces = "application/json")
	public ResponseEntity<?> getProfesores(HttpSession session)
	{
		try
		{
			// Llamada a base de datos para recuperar listado de profesores.
			List<ProfesorEntity> profesores = this.profesorRepo.findAll();
			// Devuelve un listado ordenado de profesores.
			return ResponseEntity.ok().body(this.util.ordenarLista(profesores));
			
		} catch (Exception exception)
		{
			String error = "Server Error";
			HorariosError horariosError = new HorariosError(500, error, exception);
			log.error(error, exception);
			return ResponseEntity.status(500).body(horariosError);
		}
	}

	/**
	 * Method getListStudentsAlphabetically
	 *
	 * @return
	 */
	@RequestMapping(value = "/get/sortstudents", produces = "application/json")
	public ResponseEntity<?> getListStudentsAlphabetically()
	{
		try
		{
			if (this.students.isEmpty())
			{
				HorariosError error = new HorariosError(400, "No se han cargado estudiantes");
				return ResponseEntity.status(404).body(error.toMap());
			}

			return ResponseEntity.ok().body(this.util.ordenarLista(this.students));
		} catch (Exception exception)
		{
			// -- CATCH ANY ERROR ---
			String error = "Server Error";
			HorariosError horariosError = new HorariosError(500, error, exception);
			log.error(error, exception);
			return ResponseEntity.status(500).body(horariosError);
		}
	}

	/**
	 * Method extracted
	 *
	 * @param gruposActividad
	 * @param node
	 */
	private GruposActividad getGruposActividadAttributeTexts(GruposActividad gruposActividad, Node node)
	{
		if (node.getNodeName().equals("tot_gr_act"))
		{
			gruposActividad.setTotGrAct(node.getTextContent());
		}
		if (node.getNodeName().equals("grupo_1"))
		{
			gruposActividad.setGrupo1(node.getTextContent());
		}
		if (node.getNodeName().equals("grupo_2"))
		{
			gruposActividad.setGrupo2(node.getTextContent());
		}
		if (node.getNodeName().equals("grupo_3"))
		{
			gruposActividad.setGrupo3(node.getTextContent());
		}
		if (node.getNodeName().equals("grupo_4"))
		{
			gruposActividad.setGrupo4(node.getTextContent());
		}
		if (node.getNodeName().equals("grupo_5"))
		{
			gruposActividad.setGrupo5(node.getTextContent());
		}
		return gruposActividad;
	}

	/**
	 * Method getListCourse
	 *
	 * @param session
	 * @return ResponseEntity
	 */
	@RequestMapping(value = "/get/courses", produces = "application/json")
	public ResponseEntity<?> getListCourse(HttpSession session)
	{
		List<Course> listaCurso = new ArrayList<>();
		Course curso;
		Classroom classroom;
		List<Aula> listaAula = new ArrayList<>();
		try
		{
			// -- GETTING LIST OF AULA IN CENTER ---
			listaAula = this.centroPdfs.getDatos().getAulas().getAula();

			// -- FOR EAHC AULA IN listAula ---
			for (int i = 0; i < listaAula.size(); i++)
			{
				if (listaAula.get(i).getAbreviatura().isEmpty() || (listaAula.get(i).getAbreviatura() == null))
				{
					continue;
				}
				String nombreAula = listaAula.get(i).getNombre();

				String[] plantaAula = listaAula.get(i).getAbreviatura().split("\\.");

				String plantaNumero = "";
				String numeroAula = "";
				// -- THE VALUES WITH CHARACTERS ONLY HAVE 1 POSITION ---
				if (plantaAula.length > 1)
				{
					plantaNumero = plantaAula[0].trim();
					numeroAula = plantaAula[1].trim();
				} else
				{
					plantaNumero = plantaAula[0].trim();
					numeroAula = plantaAula[0].trim();
				}

				// -- IMPORTANT , CLASSROOM PLANTANUMERO AND NUMEROAULA , CHANGED TO STRING
				// BECAUSE SOME PARAMETERS CONTAINS CHARACTERS ---
				classroom = new Classroom(plantaNumero, numeroAula);
				curso = new Course(nombreAula, classroom);
				listaCurso.add(curso);
			}

		} catch (Exception exception)
		{
			// -- CATCH ANY ERROR ---
			String error = "Server Error";
			HorariosError horariosError = new HorariosError(500, error, exception);
			log.info(error, horariosError);
			return ResponseEntity.status(500).body(horariosError);
		}
		return ResponseEntity.ok().body(listaCurso);
	}

	/**
	 * Method getClassroomTeacher
	 *
	 * @param name
	 * @param lastname
	 * @return
	 */
	@RequestMapping(value = "/teacher/get/classroom", produces = "application/json")
	public ResponseEntity<?> getClassroomTeacher(@RequestParam(required = true) String name,
			@RequestParam(required = true) String lastname, HttpSession session)
	{
		try
		{
			if (!name.isEmpty() && !name.isBlank() && !lastname.isBlank() && !lastname.isEmpty())
			{

				for (Profesor prof : this.centroPdfs.getDatos().getProfesores().getProfesor())
				{
					String profName = prof.getNombre().trim().toLowerCase();
					String profLastName = prof.getPrimerApellido().trim().toLowerCase() + " "
							+ prof.getSegundoApellido().trim().toLowerCase();

					// log.info(prof.toString());
					if (profName.equalsIgnoreCase(name.trim()) && profLastName.equalsIgnoreCase(lastname.trim()))
					{
						log.info("EXISTE " + prof);

						// Getting the actual time
						String actualTime = LocalDateTime.now().getHour() + ":" + LocalDateTime.now().getMinute();
						log.info(actualTime);

						TimeSlot profTramo = null;
						profTramo = this.gettingTramoActual(this.centroPdfs, actualTime, profTramo);

						// --- IF PROF TRAMO IS NOT NULL ---
						if (profTramo != null)
						{
							for (HorarioProf horarioProf : this.centroPdfs.getHorarios().getHorariosProfesores()
									.getHorarioProf())
							{
								if (prof.getNumIntPR().equalsIgnoreCase(horarioProf.getHorNumIntPR()))
								{
									log.info("ENCONTRADO HORARIO PROF-> " + horarioProf);

									Actividad profActividad = null;
									for (Actividad actividad : horarioProf.getActividad())
									{
										if (actividad.getTramo().trim().equalsIgnoreCase(profTramo.getNumTr().trim()))
										{
											log.info("ENCONTRADO ACTIVIDAD -> " + actividad);
											profActividad = actividad;

											// --- LEAVING ACTIVIDAD FOR EACH ---
											break;
										}
									}
									if (profActividad == null)
									{
										log.info("EL TRAMO " + profTramo
												+ "\nNO EXISTE EN LAS ACTIVIDADES DEL PROFESOR " + prof);
										// --- ERROR ---
										String info = "El profesor no se encuentra en ningun aula";
										return ResponseEntity.ok().body(info);
									}

									// --- IF PROF ACTIVIAD IS NOT NULL ---
									if (profActividad != null)
									{
										// --- GETTING THE ACTUAL AULA FROM AND GENERATE CLASSROOM ---
										Aula profAula = null;
										for (Aula aula : this.centroPdfs.getDatos().getAulas().getAula())
										{
											if (aula.getNumIntAu().trim()
													.equalsIgnoreCase(profActividad.getAula().trim()))
											{
												log.info("AULA ENCONTRADA PARA LA ACTIVIDAD --> " + profActividad + "\n"
														+ aula);
												// --- SETTING THE AULA VALUE TO PROF AULA ---
												profAula = aula;

												// --- LEAVING AULA FOR EACH ---
												break;
											}
										}

										// --- ASIGNATURA ---
										Asignatura asignatura = null;
										for (Asignatura asig : this.centroPdfs.getDatos().getAsignaturas()
												.getAsignatura())
										{
											// --- EQUAL ASIGNATURA ID --
											if (asig.getNumIntAs().trim()
													.equalsIgnoreCase(profActividad.getAsignatura().trim()))
											{
												asignatura = asig;
											}
										}

										if (profAula != null)
										{
											log.info("AULA ACTUAL PROFESOR: " + prof + "\n" + profAula);
											String nombreAula = profAula.getNombre();

											String[] plantaAula = profAula.getAbreviatura().split("\\.");

											String plantaNumero = "";
											String numeroAula = "";

											// -- THE VALUES WITH CHARACTERS ONLY HAVE 1 POSITION ---
											if (plantaAula.length > 1)
											{
												plantaNumero = plantaAula[0].trim();
												numeroAula = plantaAula[1].trim();
											} else
											{
												plantaNumero = plantaAula[0].trim();
												numeroAula = plantaAula[0].trim();
												if (plantaNumero.isEmpty() || numeroAula.isEmpty())
												{
													plantaNumero = nombreAula;
													numeroAula = nombreAula;
												}
											}

											Map<String, Object> mapa = new HashMap<String, Object>();
											Classroom classroom = new Classroom(numeroAula, plantaNumero,
													profAula.getNombre());
											mapa.put("classroom", classroom);
											mapa.put("subject", asignatura);
											log.info(mapa.toString());
											return ResponseEntity.ok().body(mapa);
										}
									}

									// --- LEAVING PROFTRAMO FOREACH ---
									break;
								}
							}
							// --- LEAVING PROF FOREACH ---
							break;
						} else
						{
							// --- ERROR ---
							LocalDateTime dateTime = LocalDateTime.now();
							String error = "Tramo no encontrado para fecha actual: " + dateTime + " ";
							HorariosError horariosError = new HorariosError(400, error, null);
							log.info(error, horariosError);
							return ResponseEntity.ok().body(horariosError);
						}
					}
				}
			}

			// --- ERROR ---
			String error = "Error on parameters from header";
			HorariosError horariosError = new HorariosError(500, error, null);
			log.info(error, horariosError);
			return ResponseEntity.status(400).body(horariosError);
		} catch (Exception exception)
		{
			// -- CATCH ANY ERROR ---
			String error = "Server Error";
			HorariosError horariosError = new HorariosError(500, error, exception);
			log.info(error, horariosError);
			return ResponseEntity.status(500).body(horariosError);
		}
	}

	/**
	 * Method getClassroomTeacher
	 *
	 * @param name
	 * @param lastname
	 * @return
	 */
	@RequestMapping(value = "/teacher/get/classroom/tramo", produces = "application/json", consumes = "application/json")
	public ResponseEntity<?> getClassroomTeacherSchedule(@RequestParam(required = true) String name,
			@RequestParam(required = true) String lastname, @RequestBody(required = true) TimeSlot profTime,
			HttpSession session)
	{
		try
		{
			log.info(profTime.toString());
			if (!name.isEmpty() && !name.isBlank() && !lastname.isBlank() && !lastname.isEmpty())
			{
				for (Profesor prof : this.centroPdfs.getDatos().getProfesores().getProfesor())
				{
					String profName = prof.getNombre().trim().toLowerCase();
					String profLastName = prof.getPrimerApellido().trim().toLowerCase() + " "
							+ prof.getSegundoApellido().trim().toLowerCase();

					log.info(prof.toString());
					if (profName.equalsIgnoreCase(name.trim()) && profLastName.equalsIgnoreCase(lastname.trim()))
					{
						log.info("EXISTE " + prof);

						// Getting the actual time
						String actualTime = LocalDateTime.now().getHour() + ":" + LocalDateTime.now().getMinute();
						log.info(actualTime);

						// --- IF PROF TRAMO IS NOT NULL ---
						if (profTime != null)
						{
							for (HorarioProf horarioProf : this.centroPdfs.getHorarios().getHorariosProfesores()
									.getHorarioProf())
							{
								if (prof.getNumIntPR().equalsIgnoreCase(horarioProf.getHorNumIntPR()))
								{
									log.info("ENCONTRADO HORARIO PROF-> " + horarioProf);

									Actividad profActividad = null;
									for (Actividad actividad : horarioProf.getActividad())
									{
										if (actividad.getTramo().trim().equalsIgnoreCase(profTime.getNumTr().trim()))
										{
											log.info("ENCONTRADO ACTIVIDAD -> " + actividad);
											profActividad = actividad;

											// --- LEAVING ACTIVIDAD FOR EACH ---
											break;
										}
									}
									if (profActividad == null)
									{
										log.info("EL TRAMO " + profTime + "\nNO EXISTE EN LAS ACTIVIDADES DEL PROFESOR "
												+ prof);
										// --- ERROR ---
										String error = "EL TRAMO " + profTime
												+ "\nNO EXISTE EN LAS ACTIVIDADES DEL PROFESOR " + prof;
										HorariosError horariosError = new HorariosError(500, error, null);
										log.info(error, horariosError);
										return ResponseEntity.ok()
												.body("El profesor en el tramo " + profTime.getStartHour() + " - "
														+ profTime.getEndHour() + " no se encuentra en ningun aula");
									}

									// --- IF PROF ACTIVIAD IS NOT NULL ---
									if (profActividad != null)
									{
										// --- GETTING THE ACTUAL AULA FROM AND GENERATE CLASSROOM ---
										Aula profAula = null;
										for (Aula aula : this.centroPdfs.getDatos().getAulas().getAula())
										{
											if (aula.getNumIntAu().trim()
													.equalsIgnoreCase(profActividad.getAula().trim()))
											{
												log.info("AULA ENCONTRADA PARA LA ACTIVIDAD --> " + profActividad + "\n"
														+ aula);
												// --- SETTING THE AULA VALUE TO PROF AULA ---
												profAula = aula;

												// --- LEAVING AULA FOR EACH ---
												break;
											}
										}

										// --- ASIGNATURA ---
										Asignatura asignatura = null;
										for (Asignatura asig : this.centroPdfs.getDatos().getAsignaturas()
												.getAsignatura())
										{
											// --- EQUAL ASIGNATURA ID --
											if (asig.getNumIntAs().trim()
													.equalsIgnoreCase(profActividad.getAsignatura().trim()))
											{
												asignatura = asig;
											}
										}

										if (profAula != null)
										{
											log.info("AULA ACTUAL PROFESOR: " + prof + "\n" + profAula);
											String nombreAula = profAula.getNombre();

											String[] plantaAula = profAula.getAbreviatura().split("\\.");

											String plantaNumero = "";
											String numeroAula = "";

											// -- THE VALUES WITH CHARACTERS ONLY HAVE 1 POSITION ---
											if (plantaAula.length > 1)
											{
												plantaNumero = plantaAula[0].trim();
												numeroAula = plantaAula[1].trim();
											} else
											{
												plantaNumero = plantaAula[0].trim();
												numeroAula = plantaAula[0].trim();
												if (plantaNumero.isEmpty() || numeroAula.isEmpty())
												{
													plantaNumero = nombreAula;
													numeroAula = nombreAula;
												}
											}
											Map<String, Object> mapa = new HashMap<String, Object>();
											Classroom classroom = new Classroom(numeroAula, plantaNumero,
													profAula.getNombre());
											mapa.put("classroom", classroom);
											mapa.put("subject", asignatura);
											log.info(mapa.toString());

											return ResponseEntity.ok().body(mapa);
										}
									}

									// --- LEAVING PROFTRAMO FOREACH ---
									break;
								}
							}
							// --- LEAVING PROF FOREACH ---
							break;
						} else
						{
							// --- ERROR ---
							String error = "Tramo introducido null" + profTime;
							HorariosError horariosError = new HorariosError(400, error, null);
							log.info(error, horariosError);
							return ResponseEntity.status(400).body(horariosError);
						}
					}
				}
			}

			// --- ERROR ---
			String error = "Error on parameters from header";
			HorariosError horariosError = new HorariosError(500, error, null);
			log.info(error, horariosError);
			return ResponseEntity.status(400).body(horariosError);
		} catch (Exception exception)
		{
			// -- CATCH ANY ERROR ---
			String error = "Server Error";
			HorariosError horariosError = new HorariosError(500, error, exception);
			log.info(error, horariosError);
			return ResponseEntity.status(500).body(horariosError);
		}
	}

	/**
	 * Method getClassroomCourse
	 *
	 * @param courseName
	 * @return ResponseEntity
	 */
	@RequestMapping(value = "/get/teachersubject", produces = "application/json")
	public ResponseEntity<?> getTeacherSubject(@RequestParam(required = true) String courseName, HttpSession session)
	{
		try
		{
			if (!courseName.isBlank() && !courseName.isBlank())
			{

				// --- IF EXIST THE COURSE ---
				Grupo grup = null;
				for (Grupo grupo : this.centroPdfs.getDatos().getGrupos().getGrupo())
				{
					if (grupo.getNombre().trim().equalsIgnoreCase(courseName.trim()))
					{
						// --- EXIST THE COURSE ---
						grup = grupo;
					}
				}
				if (grup != null)
				{
					// --- GRUPO EXIST , NOW GET THE ACUTAL TRAMO ---
					TimeSlot acutalTramo = null;

					// Getting the actual time
					String actualTime = LocalDateTime.now().getHour() + ":" + LocalDateTime.now().getMinute();
					log.info(actualTime);

					acutalTramo = this.gettingTramoActual(this.centroPdfs, actualTime, acutalTramo);

					// --- CHECKING IF THE TRAMO ACTUAL EXISTS ---
					if (acutalTramo != null)
					{
						// --- TRAMO ACTUAL EXISTS ---

						// --- NOW GETTING THE HORARIO GRUP , WITH THE SAME ID OF THE GROUP ---
						HorarioGrup horario = null;
						for (HorarioGrup horarioGrup : this.centroPdfs.getHorarios().getHorariosGrupos()
								.getHorarioGrup())
						{
							// --- EQUAL IDS ---
							if (horarioGrup.getHorNumIntGr().trim().equalsIgnoreCase(grup.getNumIntGr().trim()))
							{
								// --- THE HORARIO GROUP OF THE GROUP ---
								horario = horarioGrup;
							}
						}

						// --- IF THE HORARIO GRUP EXIST ---
						if (horario != null)
						{
							// --- GETTING THE HORARIO GRUP ACTIVIDADES ----
							Actividad activ = null;
							for (Actividad actividad : horario.getActividad())
							{
								// --- GETTING THE ACTIVIDAD WITH THE SAME ID OF THE ACTUAL TRAMO ---
								if (actividad.getTramo().trim().equalsIgnoreCase(acutalTramo.getNumTr().trim()))
								{
									activ = actividad;
								}
							}

							// --- IF EXIST THIS ACTIVIDAD ---
							if (activ != null)
							{
								// --- NOW GET THE PROFESOR AND ASIGNATURA BY PROFESOR ID AND THE ASIGNATURA ID
								// ---

								// --- PROFESOR ---
								Profesor profesor = null;
								for (Profesor prof : this.centroPdfs.getDatos().getProfesores().getProfesor())
								{
									// --- EQUAL PROFESSOR ID --
									if (prof.getNumIntPR().trim().equalsIgnoreCase(activ.getProfesor().trim()))
									{
										profesor = prof;
									}
								}

								// --- ASIGNATURA ---
								Asignatura asignatura = null;
								for (Asignatura asig : this.centroPdfs.getDatos().getAsignaturas().getAsignatura())
								{
									// --- EQUAL ASIGNATURA ID --
									if (asig.getNumIntAs().trim().equalsIgnoreCase(activ.getAsignatura().trim()))
									{
										asignatura = asig;
									}
								}

								if ((profesor != null) && (asignatura != null))
								{
									// --- THE FINAL PROFESSOR AND ASIGNATURA ---
									log.info("PROFESOR: " + profesor + "\n" + "ASIGNATURA: " + asignatura);
									TeacherMoment teacherMoment = new TeacherMoment();

									// --- TELEFONO - EMAIL - AND -ROL - IS FAKE AND HARDCODED, BECAUSE THE XML DONT
									// HAVE THIS INFO ---
									// --setting teacher---
									teacherMoment.setTeacher(new Teacher(profesor.getNombre().trim(),
											profesor.getPrimerApellido().trim() + " "
													+ profesor.getSegundoApellido().trim(),
											profesor.getNombre().trim() + "@email.com", "000-000-000",
											List.of(Rol.conserje)));

									// --- setting asignatura name ---
									teacherMoment.setSubject(asignatura.getNombre().trim());

									Classroom clase = this.util.searchClassroom(activ.getAula(),
											this.centroPdfs.getDatos().getAulas().getAula());
									teacherMoment.setClassroom(clase);

									// --- RETURN THE THEACER MOMENT , WIOUTH CLASSROOM ---
									return ResponseEntity.ok().body(teacherMoment);

								} else
								{

									// --- ERROR ---
									String error = "PROFESOR O ASIGNATURA NO ENCONTRADOS O NULL " + profesor + "\n"
											+ asignatura;

									log.info(error);

									HorariosError horariosError = new HorariosError(400, error, null);
									log.info(error, horariosError);
									return ResponseEntity.status(400).body(horariosError);
								}

							} else
							{
								// --- ERROR ---
								String error = "ERROR , ACTIVIDAD NULL O NO ENCONTRADA";

								log.info(error);

								HorariosError horariosError = new HorariosError(400, error, null);
								log.info(error, horariosError);
								return ResponseEntity.status(400).body(horariosError);
							}

						} else
						{
							// --- ERROR ---
							String error = "ERROR , HORARIO GRUP NULL O NO ENCONTRADO";

							log.info(error);

							HorariosError horariosError = new HorariosError(400, error, null);
							log.info(error, horariosError);
							return ResponseEntity.status(400).body(horariosError);
						}
					} else
					{
						// --- ERROR ---
						String error = "ERROR , TRAMO NULL O NO EXISTE";

						log.info(error);

						HorariosError horariosError = new HorariosError(400, error, null);
						log.info(error, horariosError);
						return ResponseEntity.status(400).body(horariosError);
					}
				} else
				{
					// --- ERROR ---
					String error = "ERROR GRUPO NULL O NO ENCONTRADO ";

					log.info(error);

					HorariosError horariosError = new HorariosError(400, error, null);
					log.info(error, horariosError);
					return ResponseEntity.status(400).body(horariosError);
				}
			} else
			{
				// --- ERROR ---
				String error = "ERROR , CURSO EN BLANCO O NO PERMITIDO";

				log.info(error);

				HorariosError horariosError = new HorariosError(400, error, null);
				log.info(error, horariosError);
				return ResponseEntity.status(400).body(horariosError);
			}
		} catch (Exception exception)
		{
			// -- CATCH ANY ERROR ---
			String error = "Server Error";
			HorariosError horariosError = new HorariosError(500, error, exception);
			log.info(error, horariosError);
			return ResponseEntity.status(500).body(horariosError);
		}
	}

	/**
	 * Method getClassroomCourse
	 *
	 * @param courseName
	 * @param session
	 * @return ResponseEntity
	 */
	@RequestMapping(value = "/get/classroomcourse", produces = "application/json")
	public ResponseEntity<?> getClassroomCourse(@RequestParam(required = true) String courseName, HttpSession session)
	{
		try
		{
			// --- CHECKING IF THE COURSE NAME IS NOT BLANK AND NOT EMPTY ---
			if (!courseName.isBlank() && !courseName.isEmpty())
			{
				// --- IF THE GRUPO EXISTS (NOT NULL) ---

				// --- CHECK IF THE AULA EXISTS ---
				Aula aula = null;
				for (Aula aul : this.centroPdfs.getDatos().getAulas().getAula())
				{
					// --- REPLACE º,'SPACE', - , FOR EMPTY , FROM GRUPO NOMBRE AND GRUPO ABRV ---
					// --- THIS IS FOR TRY TO GET THE MAX POSIBILITIES OF GET THE AULA FROM CURSO
					// ---

//						String aulaName = grupo.getNombre().trim().toLowerCase().replace("º", "").replace(" ", "")
//								.replace("-", "");
//						String aulaAbr = grupo.getAbreviatura().trim().toLowerCase().replace("º", "")
//								.replace(" ", "").replace("-", "");

					// --- CHECK IF COURSENAME EXISTS ON THE AULA NAME OR THE COURSE ABRV EXIST ON
					// THE AULA NAME ---
//						if (aul.getNombre().trim().toLowerCase().replace("-", "").contains(aulaName)
//								|| aul.getNombre().trim().toLowerCase().replace("-", "").contains(aulaAbr))
//						{
//							// -- IF EXISTS , SET THE VALUE OF AUL (FOREACH) ON AULA ---
//							aula = aul;
//						}
					if (aul.getNombre().equalsIgnoreCase(courseName))
					{
						aula = aul;
					}
				}

				// --- IF THE AULA IS NOT NULL (EXISTS) ---
				if (aula != null)
				{
					String nombreAula = aula.getNombre();

					// --- SPLIT BY '.' ---
					String[] plantaAula = aula.getAbreviatura().split("\\.");

					String plantaNumero = "";
					String numeroAula = "";
					// -- THE VALUES WITH CHARACTERS ONLY HAVE 1 POSITION ---
					if (plantaAula.length > 1)
					{
						plantaNumero = plantaAula[0].trim();
						numeroAula = plantaAula[1].trim();
					} else
					{
						plantaNumero = plantaAula[0].trim();
						numeroAula = plantaAula[0].trim();
					}

					// -- IMPORTANT , CLASSROOM PLANTANUMERO AND NUMEROAULA , CHANGED TO STRING
					// BECAUSE SOME PARAMETERS CONTAINS CHARACTERS ---
					Classroom classroom = new Classroom(numeroAula, plantaNumero, nombreAula);

					// --- RETURN FINALLY THE CLASSROOM ---
					return ResponseEntity.ok(classroom);

				} else
				{
					// --- ERROR ---
					String error = "ERROR AULA NOT FOUND OR NULL";

					log.info(error);

					HorariosError horariosError = new HorariosError(400, error, null);
					log.info(error, horariosError);
					return ResponseEntity.status(400).body(horariosError);
				}

			} else
			{
				// --- ERROR ---
				String error = "ERROR HEADER COURSE NAME EMPTY OR BLANK";

				log.info(error);

				HorariosError horariosError = new HorariosError(400, error, null);
				log.info(error, horariosError);
				return ResponseEntity.status(400).body(horariosError);
			}
		} catch (Exception exception)
		{
			// -- CATCH ANY ERROR ---
			String error = "Server Error";
			HorariosError horariosError = new HorariosError(500, error, exception);
			log.info(error, horariosError);
			return ResponseEntity.status(500).body(horariosError);
		}
	}

	@RequestMapping(value = "/get/tramos", produces = "application/json")
	public ResponseEntity<?> getNumTramos()
	{
		try
		{
			List<TimeSlot> tramos = this.centroPdfs.getDatos().getTramosHorarios().getTramo();
			return ResponseEntity.ok().body(tramos);
		} catch (Exception exception)
		{
			String message = "Error de servidor, no se encuentran datos de los tramos";
			log.error(message, exception);
			HorariosError error = new HorariosError(500, message, exception);
			return ResponseEntity.status(500).body(error.toMap());
		}
	}

	/**
	 * Method getListHours
	 *
	 * @return ResponseEntity
	 */
	@RequestMapping(value = "/get/hours", produces = "application/json")
	public ResponseEntity<?> getListHours(HttpSession session)
	{
		try
		{
			// --- CASTING OBJECT TO STORED CENTRO ---
			// Centro centro = (Centro) session.getAttribute("storedCentro");
			List<Hour> hourList = new ArrayList<>();
			for (int i = 0; i < 7; i++)
			{
				// --- GETTING THE INFO OF EACH TRAMO, BUT ONLY THE FIRST 7 TRAMOS , BECAUSE THT
				// REPRESENT "LUNES" "PRIMERA-ULTIMA-HORA" ---
				TimeSlot tramo = this.centroPdfs.getDatos().getTramosHorarios().getTramo().get(i);

				// --- GETTING THE HOURNAME BY THE ID OF THE TRAMO 1-7 (1,2,3,R,4,5,6) ---
				String hourName = "";
				switch (tramo.getNumTr().trim())
				{
				case "1":
				{
					hourName = "primera";
					break;
				}
				case "2":
				{
					hourName = "segunda";
					break;
				}
				case "3":
				{
					hourName = "tercera";
					break;
				}
				case "4":
				{
					hourName = "recreo";
					break;
				}
				case "5":
				{
					hourName = "cuarta";
					break;
				}
				case "6":
				{
					hourName = "quinta";
					break;
				}
				case "7":
				{
					hourName = "sexta";
					break;
				}
				default:
				{
					// --- DEFAULT ---
					hourName = "Desconocido";
					break;
				}
				}
				// --- ADD THE INFO OF THE TRAMO ON HOUR OBJECT ---
				hourList.add(new Hour(hourName, tramo.getStartHour().trim(), tramo.getEndHour().trim()));
			}
			// --- RESPONSE WITH THE HOURLIST ---
			return ResponseEntity.ok(hourList);

		} catch (Exception exception)
		{
			// -- CATCH ANY ERROR ---
			String error = "Server Error";
			HorariosError horariosError = new HorariosError(500, error, exception);
			log.info(error, horariosError);
			return ResponseEntity.status(500).body(horariosError);
		}

	}

	/**
	 * Metodo que registra una visita al baño en la base de datos por parte de un
	 * alumno
	 * 
	 * @param name
	 * @param lastname
	 * @param course
	 * @return ok si todo ha ido bien, error si los parametros fallan o surge un
	 *         error de servidor
	 */
	@RequestMapping("/student/visita/bathroom")
	public ResponseEntity<?> postVisit(@RequestParam(required = true, name = "name") String name,
			@RequestParam(required = true, name = "lastName") String lastname,
			@RequestParam(required = true, name = "course") String course)
	{
		try
		{
			// Buscamos el estudiante
			Student student = this.studentOperation.findStudent(name, lastname, course, this.students);
			// En caso de que no haya ido al baño se anota
			this.operations.comprobarVisita(student);
			// Si no hay error devolvemos que todo ha ido bien
			return ResponseEntity.ok().build();
		} catch (HorariosError exception)
		{
			log.error("Error al registrar la ida de un estudiante", exception);
			return ResponseEntity.status(404).body(exception.toMap());
		} catch (Exception exception)
		{
			String error = "Server Error";
			HorariosError horariosError = new HorariosError(500, error, exception);
			log.error(error, exception);
			return ResponseEntity.status(500).body(horariosError);
		}
	}

	/**
	 * @author MANU
	 * @param name
	 * @param lastname
	 * @param course
	 * @param session
	 * @return
	 */
	@RequestMapping("/student/regreso/bathroom")
	public ResponseEntity<?> postReturnBathroom(@RequestParam(required = true, name = "name") String name,
			@RequestParam(required = true, name = "lastName") String lastname,
			@RequestParam(required = true, name = "course") String course)
	{
		try
		{
			// Buscamos el estudiante
			Student student = this.studentOperation.findStudent(name, lastname, course, this.students);
			// En caso de que haya ido al baño se anota si esta, en caso de que no hay ido
			// se manda un error
			this.operations.comprobarVuelta(student);
			// Si no hay error devolvemos que todo ha ido bien
			return ResponseEntity.ok().build();
		} catch (HorariosError exception)
		{
			log.error("Error al registrar la vuelta de un estudiante", exception);
			return ResponseEntity.status(404).body(exception.toMap());
		} catch (Exception exception)
		{
			String error = "Server Error";
			HorariosError horariosError = new HorariosError(500, error, exception);
			log.error(error, exception);
			return ResponseEntity.status(500).body(horariosError);
		}
	}

	/**
	 * @author MANU
	 * @param name
	 * @param lastname
	 * @param fechaInicio
	 * @param fechaEnd
	 * @param session
	 * @return
	 */
	@RequestMapping(value = "/get/veces/visitado/studentFechas", produces = "application/json")
	public ResponseEntity<?> getNumberVisitsBathroom(@RequestParam(required = true, name = "name") String name,
			@RequestParam(required = true, name = "lastName") String lastname,
			@RequestParam(required = true, name = "course") String course,
			@RequestParam(required = true, name = "fechaInicio") String fechaInicio,
			@RequestParam(required = true, name = "fechaFin") String fechaEnd, HttpSession session)
	{
		try
		{
			// Obtenemos el estudiante por su nombre apellido y curso
			Student student = this.studentOperation.findStudent(name, lastname, course, this.students);

			List<Map<String, String>> visitasAlumno = this.operations.getVisitaAlumno(student, fechaInicio, fechaEnd);

			// Establecemos dos tipos de respuesta, una correcta si la lista contiene datos
			// y un error en caso contrario
			ResponseEntity<?> respuesta = !visitasAlumno.isEmpty() ? ResponseEntity.ok().body(visitasAlumno)
					: ResponseEntity.status(404).body(
							"El alumno no ha ido en el periodo " + fechaInicio + " - " + fechaEnd + " al servicio");

			// Devolvemos una de las dos respuestas
			return respuesta;

		} catch (Exception exception)
		{
			String error = "Server Error";
			HorariosError horariosError = new HorariosError(500, error, exception);
			log.error(error, exception);
			return ResponseEntity.status(500).body(horariosError);
		}
	}

	/**
	 * @author MANU
	 * @param fechaInicio
	 * @param fechaEnd
	 * @param session
	 * @return
	 */
	@RequestMapping(value = "/get/students/visitas/bathroom", produces = "application/json")
	public ResponseEntity<?> getListTimesBathroom(
			@RequestParam(required = true, name = "fechaInicio") String fechaInicio,
			@RequestParam(required = true, name = "fechaFin") String fechaEnd, HttpSession session)
	{
		try
		{
			List<Map<String, Object>> visitas = this.operations.getVisitasAlumnos(fechaInicio, fechaEnd);

			// Establecemos dos tipos de respuesta, una correcta si la lista contiene datos
			// y un error en caso contrario
			ResponseEntity<?> respuesta = !visitas.isEmpty() ? ResponseEntity.ok().body(visitas)
					: ResponseEntity.status(404)
							.body("No hay alumnos en el periodo " + fechaInicio + " - " + fechaEnd + " al servicio");

			// Devolvemos una de las dos respuestas
			return respuesta;
		} catch (Exception exception)
		{
			String error = "Server Error";
			HorariosError horariosError = new HorariosError(500, error, exception);
			log.error(error, exception);
			return ResponseEntity.status(500).body(horariosError);
		}
	}

	/**
	 * Metodo que devuelve el numero de visitas realizadas por un alumno al servicio
	 * (solo cuenta aquellas en las que la fecha de vuelta no sea nula)
	 * 
	 * @param name
	 * @param lastname
	 * @param course
	 * @return numero de veces que ha ido al servicio
	 */
	@RequestMapping(value = "/get/student/numero-veces-servicio", produces = "application/json")
	public ResponseEntity<?> getDayHourBathroom(@RequestParam(required = true, name = "name") String name,
			@RequestParam(required = true, name = "lastname") String lastname,
			@RequestParam(required = true, name = "course") String course)
	{
		try
		{
			// Obtenemos el estudiante por su nombre apellido y curso
			Student student = this.studentOperation.findStudent(name, lastname, course, this.students);

			// Obtenemos el numero de vecew que ha ido y vuelto del servicio
			int numVecesBathroom = this.operations.obtenerNumeroVecesServicio(student);

			return ResponseEntity.ok().body(numVecesBathroom);
		} catch (Exception exception)
		{
			String error = "Error en el servidor";
			HorariosError horariosError = new HorariosError(500, error, exception);
			log.error(error, exception);
			return ResponseEntity.status(500).body(horariosError.toMap());
		}
	}

	/**
	 * Method getSchedulePdf
	 *
	 * @param name
	 * @param lastname
	 * @param session
	 * @return
	 */
	@RequestMapping(value = "/get/horario/teacher/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
	public ResponseEntity<?> getSchedulePdf(@RequestParam(required = true) String name,
			@RequestParam(required = true) String lastname)
	{
		try
		{
			if (!name.trim().isBlank() && !name.trim().isEmpty() && !lastname.trim().isBlank()
					&& !lastname.trim().isEmpty())
			{
				if (this.centroPdfs != null)
				{
					Centro centro = this.centroPdfs;

					// --- GETTING THE PROFESSOR AND CHECK IF EXISTS ---
					if (lastname.split(" ").length < 2)
					{
						// -- CATCH ANY ERROR ---
						String error = "ERROR NO HAY DOS APELLIDOS DEL PROFESOR O NO ENCONTRADOS EN HEADERS";
						HorariosError horariosError = new HorariosError(400, error, null);
						log.info(error, horariosError);
						return ResponseEntity.status(400).body(horariosError);
					}
					String profFirstLastName = lastname.trim().split(" ")[0];
					String profSecondLastName = lastname.trim().split(" ")[1];

					Profesor profesor = null;
					for (Profesor prof : centro.getDatos().getProfesores().getProfesor())
					{
						if (prof.getNombre().trim().equalsIgnoreCase(name.trim())
								&& prof.getPrimerApellido().trim().equalsIgnoreCase(profFirstLastName)
								&& prof.getSegundoApellido().trim().equalsIgnoreCase(profSecondLastName))
						{
							// --- PROFESSOR EXISTS , SET THE VALUE OF PROF IN PROFESOR ---
							profesor = prof;
							log.info("PROFESOR ENCONTRADO -> " + profesor.toString());
						}
					}

					if (profesor != null)
					{
						// --- PROFESOR EXISTS ---
						HorarioProf horarioProfesor = null;
						for (HorarioProf horarioProf : centro.getHorarios().getHorariosProfesores().getHorarioProf())
						{
							if (horarioProf.getHorNumIntPR().trim().equalsIgnoreCase(profesor.getNumIntPR().trim()))
							{
								// --- HORARIO PROFESOR EXISTS , SET THE VALUE ON HORARIO PROFESOR---
								horarioProfesor = horarioProf;
							}
						}

						if (horarioProfesor != null)
						{
							// --- HORARIO EXISTS ---
							// --- CREATING THE MAP WITH KEY STRING TRAMO DAY AND VALUE LIST OF ACTIVIDAD
							// ---
							Map<String, List<Actividad>> profesorMap = new HashMap<>();

							// --- FOR EACH ACTIVIDAD , GET THE TRAMO DAY , AND PUT ON MAP WITH THE
							// ACTIVIDADES OF THIS DAY (LIST ACTIVIDAD) ---
							for (Actividad actividad : horarioProfesor.getActividad())
							{
								TimeSlot tramo = this.extractTramoFromCentroActividad(centro, actividad);

								// --- CHECKING IF THE TRAMO DAY EXISTS ---
								if (!profesorMap.containsKey(tramo.getDayNumber().trim()))
								{
									// --- ADD THE NEW KEY AND VALUE ---
									List<Actividad> actividadList = new ArrayList<>();
									actividadList.add(actividad);
									Collections.sort(actividadList);

									profesorMap.put(tramo.getDayNumber().trim(), actividadList);
								} else
								{
									// -- ADD THE VALUE TO THE ACTUAL VALUES ---
									List<Actividad> actividadList = profesorMap.get(tramo.getDayNumber().trim());
									actividadList.add(actividad);
									Collections.sort(actividadList);
									profesorMap.put(tramo.getDayNumber().trim(), actividadList);
								}
							}

							// --- CALLING TO APPLICATION PDF , TO GENERATE PDF ---
							ApplicationPdf pdf = new ApplicationPdf();
							try
							{
								// -- CALLING TO THE METHOD GET INFO PDF OF APLICATION PDF TO CREATE THE PDF ---
								pdf.getInfoPdf(centro, profesorMap, profesor);

								// --- GETTING THE PDF BY NAME URL ---
								File file = new File(
										profesor.getNombre().trim() + "_" + profesor.getPrimerApellido().trim() + "_"
												+ profesor.getSegundoApellido() + "_Horario.pdf");

								// --- SETTING THE HEADERS WITH THE NAME OF THE FILE TO DOWLOAD PDF ---
								HttpHeaders responseHeaders = new HttpHeaders();
								// --- SET THE HEADERS ---
								responseHeaders.set("Content-Disposition", "attachment; filename=" + file.getName());

								try
								{
									// --- CONVERT FILE TO BYTE[] ---
									byte[] bytesArray = Files.readAllBytes(file.toPath());

									// --- RETURN OK (200) WITH THE HEADERS AND THE BYTESARRAY ---
									return ResponseEntity.ok().headers(responseHeaders).body(bytesArray);
								} catch (IOException exception)
								{
									// --- ERROR ---
									String error = "ERROR GETTING THE BYTES OF PDF ";

									log.info(error);

									HorariosError horariosError = new HorariosError(500, error, exception);
									log.info(error, horariosError);
									return ResponseEntity.status(500).body(horariosError);
								}
							} catch (HorariosError exception)
							{
								// --- ERROR ---
								String error = "ERROR getting the info pdf ";

								log.info(error);

								HorariosError horariosError = new HorariosError(400, error, exception);
								log.info(error, horariosError);
								return ResponseEntity.status(400).body(horariosError);
							}

						} else
						{
							// --- ERROR ---
							String error = "ERROR HORARIO_PROFESOR NOT FOUNT OR NULL";

							log.info(error);

							HorariosError horariosError = new HorariosError(400, error, null);
							log.info(error, horariosError);
							return ResponseEntity.status(400).body(horariosError);
						}
					} else
					{
						// --- ERROR ---
						String error = "ERROR PROFESOR NOT FOUND OR NULL";

						log.info(error);

						HorariosError horariosError = new HorariosError(400, error, null);
						log.info(error, horariosError);
						return ResponseEntity.status(400).body(horariosError);
					}

				} else
				{
					// --- ERROR ---
					String error = "ERROR centroPdfs NULL OR NOT FOUND";

					log.info(error);

					HorariosError horariosError = new HorariosError(400, error, null);
					log.info(error, horariosError);
					return ResponseEntity.status(400).body(horariosError);
				}
			} else
			{
				// --- ERROR ---
				String error = "ERROR PARAMETROS HEADER NULL OR EMPTY, BLANK";

				log.info(error);

				HorariosError horariosError = new HorariosError(400, error, null);
				log.info(error, horariosError);
				return ResponseEntity.status(400).body(horariosError);
			}
		} catch (Exception exception)
		{
			// -- CATCH ANY ERROR ---
			String error = "Server Error";
			HorariosError horariosError = new HorariosError(500, error, exception);
			log.info(error, horariosError);
			return ResponseEntity.status(500).body(horariosError);
		}
	}

	/**
	 * Method extractTramoFromCentroActividad
	 *
	 * @param centro
	 * @param actividad
	 * @param tramo
	 * @return
	 */
	private TimeSlot extractTramoFromCentroActividad(Centro centro, Actividad actividad)
	{
		for (TimeSlot tram : centro.getDatos().getTramosHorarios().getTramo())
		{
			// --- GETTING THE TRAMO ---
			if (actividad.getTramo().trim().equalsIgnoreCase(tram.getNumTr().trim()))
			{
				return tram;
			}
		}
		return null;
	}

	/**
	 * Method getSchedulePdf
	 *
	 * @param name
	 * @param lastname
	 * @param session
	 * @return
	 */
	@RequestMapping(value = "/get/grupo/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
	public ResponseEntity<?> getGroupSchedule(@RequestParam(required = true, name = "group") String grupo)
	{
		try
		{
			// --- CHEKING THE GRUPO ---
			if ((grupo != null) && !grupo.trim().isBlank() && !grupo.trim().isEmpty())
			{
				// --- CHECKING IF THE PDF CENTRO IS NULL ---
				if (this.centroPdfs != null)
				{
					// --- CHEKING IF GRUPO EXISTS ---
					Grupo grupoFinal = null;
					for (Grupo grup : this.centroPdfs.getDatos().getGrupos().getGrupo())
					{
						// --- RAPLACING "SPACE", " º " AND " - " FOR EMPTY , THT IS FOR GET MORE
						// SPECIFIC DATA ---
						String grupoParam = grupo.replace(" ", "").replace("-", "").replace("º", "");
						String grupName = grup.getNombre().replace(" ", "").replace("-", "").replace("º", "");
						String grupAbr = grup.getAbreviatura().replace(" ", "").replace("-", "").replace("º", "");

						if (grupName.trim().toLowerCase().contains(grupoParam.trim().toLowerCase())
								|| grupAbr.trim().toLowerCase().contains(grupoParam.trim().toLowerCase()))
						{
							grupoFinal = grup;
						}
					}

					// --- IF GRUPO EXISTS ---
					if (grupoFinal != null)
					{
						// --- GRUPO EXISTS ---

						// -- CHEKING HORARIO_GRUP FROM GRUPO_FINAL ---
						HorarioGrup horarioGrup = null;
						for (HorarioGrup horarioGrp : this.centroPdfs.getHorarios().getHorariosGrupos()
								.getHorarioGrup())
						{
							// -- GETTING THE HORARIO_GRUP OF THE GRUP ---
							if (horarioGrp.getHorNumIntGr().trim().equalsIgnoreCase(grupoFinal.getNumIntGr().trim()))
							{
								horarioGrup = horarioGrp;
							}
						}

						// --- IF EXISTS HORARIO_GRUP ---
						if (horarioGrup != null)
						{
							// --- GETTING THE ACTIVIDAD LIST OF THE GRUPO ---
							List<Actividad> actividadList = horarioGrup.getActividad();

							// --- ACTIVIDAD_LIST HV MORE THAN 0 ACTIVIDAD AN IS NOT NULL ---
							if ((actividadList != null) && (actividadList.size() > 0))
							{
								// --- GENERATE THE MAP FOR TRAMO DAY , AND ACTIVIDAD LIST ---
								Map<String, List<Actividad>> grupoMap = new HashMap<>();

								// --- CALSIFICATE EACH ACTIVIDAD ON THE SPECIFIC DAY ---
								for (Actividad actv : actividadList)
								{
									// --- GET THE TRAMO ---
									TimeSlot tramo = this.extractTramoFromCentroActividad(this.centroPdfs, actv);

									// --- IF THE MAP NOT CONTAINS THE TRAMO DAY NUMBER , ADD THE DAY NUMBER AND THE
									// ACTIVIDAD LIST ---
									if (!grupoMap.containsKey(tramo.getDayNumber().trim()))
									{
										List<Actividad> temporalList = new ArrayList<>();
										temporalList.add(actv);
										grupoMap.put(tramo.getDayNumber().trim(), temporalList);

									} else
									{
										// --- IF THE MAP ALRREADY CONTAINS THE TRAMO DAY , GET THE ACTIVIDAD LIST AND
										// ADD THE ACTV , FINALLY PUT THEN ON THE DAY ---
										List<Actividad> temporalList = grupoMap.get(tramo.getDayNumber().trim());
										temporalList.add(actv);
										grupoMap.put(tramo.getDayNumber().trim(), temporalList);
									}
								}

								// --- IF THE MAP IS NOT EMPTY , LAUNCH THE PDF GENERATION ---
								if (!grupoMap.isEmpty())
								{

									log.info(grupoMap.toString());

									try
									{
										ApplicationPdf applicationPdf = new ApplicationPdf();
										applicationPdf.getInfoPdfHorarioGrupoCentro(this.centroPdfs, grupoMap,
												grupo.trim());

										// --- GETTING THE PDF BY NAME URL ---
										File file = new File("Horario" + grupo + ".pdf");

										// --- SETTING THE HEADERS WITH THE NAME OF THE FILE TO DOWLOAD PDF ---
										HttpHeaders responseHeaders = new HttpHeaders();

										// --- REPLACE SPACES AND º BECAUSE THAT MADE CONFLICTS ON SAVE FILE ---
										String fileName = file.getName().replace("º", "").replace(" ", "_");
										// --- SET THE HEADERS ---
										responseHeaders.set("Content-Disposition", "attachment; filename=" + fileName);

										// --- CONVERT FILE TO BYTE[] ---
										byte[] bytesArray = Files.readAllBytes(file.toPath());

										// --- RETURN OK (200) WITH THE HEADERS AND THE BYTESARRAY ---
										return ResponseEntity.ok().headers(responseHeaders).body(bytesArray);
									} catch (HorariosError exception)
									{
										// --- ERROR ---
										String error = "ERROR getting the info pdf ";

										log.info(error);

										HorariosError horariosError = new HorariosError(400, error, exception);
										log.info(error, horariosError);
										return ResponseEntity.status(400).body(horariosError);
									}

								} else
								{
									// --- ERROR ---
									String error = "ERROR grupoMap IS EMPTY OR NOT FOUND";

									log.info(error);

									HorariosError horariosError = new HorariosError(400, error, null);
									log.info(error, horariosError);
									return ResponseEntity.status(400).body(horariosError);
								}
							} else
							{
								// --- ERROR ---
								String error = "ERROR actividadList HAVE 0 ACTIVIDAD OR IS NULL";

								log.info(error);

								HorariosError horariosError = new HorariosError(400, error, null);
								log.info(error, horariosError);
								return ResponseEntity.status(400).body(horariosError);
							}

						} else
						{
							// --- ERROR ---
							String error = "ERROR horarioGrup NULL OR NOT FOUND";

							log.info(error);

							HorariosError horariosError = new HorariosError(400, error, null);
							log.info(error, horariosError);
							return ResponseEntity.status(400).body(horariosError);
						}
					} else
					{
						// --- ERROR ---
						String error = "ERROR GRUPO_FINAL NULL OR NOT FOUND";

						log.info(error);

						HorariosError horariosError = new HorariosError(400, error, null);
						log.info(error, horariosError);
						return ResponseEntity.status(400).body(horariosError);
					}
				} else
				{
					// --- ERROR ---
					String error = "ERROR CENTRO_PDFS NULL OR NOT FOUND";

					log.info(error);

					HorariosError horariosError = new HorariosError(400, error, null);
					log.info(error, horariosError);
					return ResponseEntity.status(400).body(horariosError);
				}
			} else
			{
				// --- ERROR ---
				String error = "ERROR GRUPO PARAMETER ERROR";

				log.info(error);

				HorariosError horariosError = new HorariosError(400, error, null);
				log.info(error, horariosError);
				return ResponseEntity.status(400).body(horariosError);
			}
		} catch (Exception exception)
		{
			// -- CATCH ANY ERROR ---
			String error = "Server Error";
			HorariosError horariosError = new HorariosError(500, error, exception);
			log.info(error, horariosError);
			return ResponseEntity.status(500).body(horariosError);
		}
	}

	/**
	 * Method getTeacherClassroom
	 *
	 * @param name
	 * @param lastname
	 * @param session
	 * @return ResponseEntity
	 */
	@RequestMapping(value = "/get/teacher/Classroom", produces = "application/json")
	public ResponseEntity<?> getTeacherClassroom(@RequestHeader(required = true) String name,
			@RequestHeader(required = true) String lastname, HttpSession session)
	{
		try
		{
			// --- checking stored CENTRO ---
			if ((session.getAttribute("storedCentro") != null)
					&& (session.getAttribute("storedCentro") instanceof Centro))
			{
				Centro centro = (Centro) session.getAttribute("storedCentro");

				if ((name != null) && !name.trim().isBlank() && !name.trim().isEmpty())
				{
					// -- NOMBRE Y APELLIDOS CON CONTENIDO ---

					Student student = null;
					for (Student st : students)
					{
						// -- CHECKING IF STUDENT EXISTS ---
						if (st.getName().trim().equalsIgnoreCase(name.trim())
								&& st.getLastName().trim().equalsIgnoreCase(lastname.trim()))
						{
							student = st;
						}

					}

					if (student != null)
					{
						// --- STUDENT EXISTS ---
						Grupo grupo = null;
						for (Grupo grp : centro.getDatos().getGrupos().getGrupo())
						{
							String nombreGrp = grp.getNombre().trim().replace("º", "").replace(" ", "").replace("-",
									"");
							String abrvGrp = grp.getAbreviatura().trim().replace("º", "").replace(" ", "").replace("-",
									"");

							log.info(student.getCourse().toString());
							String nombreGrupo = student.getCourse().trim().replace("º", "").replace(" ", "")
									.replace("-", "");

							if (nombreGrp.toLowerCase().contains(nombreGrupo.toLowerCase())
									|| abrvGrp.toLowerCase().contains(nombreGrupo.toLowerCase()))
							{
								grupo = grp;
							}
						}

						if (grupo != null)
						{
							// --- GRUPO EXISTS ---

							HorarioGrup horarioGrup = null;
							for (HorarioGrup horarioGrp : centro.getHorarios().getHorariosGrupos().getHorarioGrup())
							{
								if (horarioGrp.getHorNumIntGr().trim().equalsIgnoreCase(grupo.getNumIntGr().trim()))
								{
									horarioGrup = horarioGrp;
								}
							}

							if (horarioGrup != null)
							{
								// --- HORARIO_GRUP EXISTS ---

								// Getting the actual time
								String actualTime = LocalDateTime.now().getHour() + ":"
										+ LocalDateTime.now().getMinute();

								TimeSlot tramoActual = null;

								tramoActual = this.gettingTramoActual(centro, actualTime, tramoActual);

								if (tramoActual != null)
								{
									// --- TRAMO ACTUAL EXISTS ---
									Actividad actividadActual = null;

									for (Actividad actv : horarioGrup.getActividad())
									{
										if (actv.getTramo().trim().equalsIgnoreCase(tramoActual.getNumTr().trim()))
										{
											actividadActual = actv;
										}
									}

									if (actividadActual != null)
									{
										log.info(actividadActual.toString());
										// --- ACTIVIDAD ACTUAL EXISTS ---
										TeacherMoment teacherMoment = new TeacherMoment();
										Teacher teacher = new Teacher();
										Classroom classroom = new Classroom();

										// -- GETTING TEACHER ---
										for (Profesor profesor : centro.getDatos().getProfesores().getProfesor())
										{
											if (profesor.getNumIntPR().trim()
													.equalsIgnoreCase(actividadActual.getProfesor().trim()))
											{
												teacher.setName(profesor.getNombre().trim());
												teacher.setLastName(profesor.getPrimerApellido().trim() + " "
														+ profesor.getSegundoApellido().trim());
											}
										}

										// --- GETTING THE CLASSROOM ---
										for (Aula aula : centro.getDatos().getAulas().getAula())
										{
											if (aula.getNumIntAu().trim()
													.equalsIgnoreCase(actividadActual.getAula().trim()))
											{
												String nombreAula = aula.getNombre();

												String[] plantaAula = aula.getAbreviatura().split("\\.");

												String plantaNumero = "";
												String numeroAula = "";
												// -- THE VALUES WITH CHARACTERS ONLY HAVE 1 POSITION ---
												if (plantaAula.length > 1)
												{
													plantaNumero = plantaAula[0].trim();
													numeroAula = plantaAula[1].trim();
												} else
												{
													plantaNumero = plantaAula[0].trim();
													numeroAula = plantaAula[0].trim();
												}

												classroom.setFloor(plantaNumero);
												classroom.setNumber(numeroAula);
											}
										}

										// --- BUILD THE TEACHER MOMENT ---
										teacherMoment.setClassroom(classroom);
										teacherMoment.setTeacher(teacher);

										log.info(teacherMoment.toString());

										// --- RETURN THE TEACHER MOMENT ---
										return ResponseEntity.ok(teacherMoment);
									} else
									{
										// --- ERROR ---
										String error = "ERROR ACTIVDAD ACTUAL NO EXISTENTE OR NULL";

										log.info(error);

										HorariosError horariosError = new HorariosError(400, error, null);
										log.info(error, horariosError);
										return ResponseEntity.status(400).body(horariosError);
									}

								} else
								{
									// --- ERROR ---
									String error = "ERROR TRAMO ACTUAL NO EXISTENTE OR NULL";

									log.info(error);

									HorariosError horariosError = new HorariosError(400, error, null);
									log.info(error, horariosError);
									return ResponseEntity.status(400).body(horariosError);
								}

							} else
							{
								// --- ERROR ---
								String error = "ERROR HORARIO GRUP NOT FOUND OR NULL";

								log.info(error);

								HorariosError horariosError = new HorariosError(400, error, null);
								log.info(error, horariosError);
								return ResponseEntity.status(400).body(horariosError);
							}

						} else
						{
							// --- ERROR ---
							String error = "GRUPO NOT FOUND OR NULL";

							log.info(error);

							HorariosError horariosError = new HorariosError(400, error, null);
							log.info(error, horariosError);
							return ResponseEntity.status(400).body(horariosError);
						}

					} else
					{
						// --- ERROR ---
						String error = "ERROR STUDENT NOT FOUND OR NULL";

						log.info(error);

						HorariosError horariosError = new HorariosError(400, error, null);
						log.info(error, horariosError);
						return ResponseEntity.status(400).body(horariosError);
					}

				} else
				{
					// --- ERROR ---
					String error = "ERROR DE PARAMETROS";

					log.info(error);

					HorariosError horariosError = new HorariosError(400, error, null);
					log.info(error, horariosError);
					return ResponseEntity.status(400).body(horariosError);
				}

			} else
			{
				// --- ERROR ---
				String error = "ERROR storedCentro NOT FOUND OR NULL";

				log.info(error);

				HorariosError horariosError = new HorariosError(400, error, null);
				log.info(error, horariosError);
				return ResponseEntity.status(400).body(horariosError);
			}
		} catch (Exception exception)
		{
			String error = "Server Error";
			HorariosError horariosError = new HorariosError(500, error, exception);
			log.error(error, exception);
			return ResponseEntity.status(500).body(horariosError);
		}

	}

	/**
	 * Method gettingTramoActual
	 *
	 * @param centro
	 * @param actualTime
	 * @param tramoActual
	 * @return
	 */
	private TimeSlot gettingTramoActual(Centro centro, String actualTime, TimeSlot tramoActual)
	{
		for (TimeSlot tramo : centro.getDatos().getTramosHorarios().getTramo())
		{
			int numTr = Integer.parseInt(tramo.getNumTr());

			// --- GETTING THE HORA,MINUTO , INICIO AND FIN ---
			int horaInicio = Integer.parseInt(tramo.getStartHour().split(":")[0].trim());
			int minutoInicio = Integer.parseInt(tramo.getStartHour().split(":")[1].trim());

			int horaFin = Integer.parseInt(tramo.getEndHour().split(":")[0].trim());
			int minutoFin = Integer.parseInt(tramo.getEndHour().split(":")[1].trim());

			// --- GETTING THE HORA, MINUTO ACTUAL ---
			int horaActual = Integer.parseInt(actualTime.split(":")[0].trim());
			int minutoActual = Integer.parseInt(actualTime.split(":")[1].trim());

			// --- USE CALENDAR INSTANCE FOR GET INTEGER WITH THE NUMBER OF THE DAY ON THE
			// WEEK ---
			Calendar calendar = Calendar.getInstance();
			// --- PARSIN CALENDAR DAY_OF_WEK TO NUMBER -1 (-1 BECAUSE THIS START ON
			// SUNDAY)--
			int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1;

			// --- IF DAY IS 0 , IS 7 , BACUSE IS SUNDAY ---
			if (dayOfWeek == 0)
			{
				dayOfWeek = 7;
			}
			if (dayOfWeek >= 6)
			{
				log.warn("DIA EXCEDIDO: (6:SABADO-7:DOMINGO) -> " + dayOfWeek);
			}

			// --- DAY OF TRAMO ---
			if (Integer.parseInt(tramo.getDayNumber()) == dayOfWeek)
			{
				// --- IF HORA ACTUAL EQUALS HORA INICIO ---
				if (horaActual == horaInicio)
				{
					// --- CHEKING IF THE MINUTO ACTUAL IS GREATER THAN THE MINUTO INICIO AND
					// HORA ACTUAL LESS THAN HORA FIN ---
					if ((minutoActual >= minutoInicio) && (horaActual <= horaFin))
					{
						// --- SETTING THE VALUE OF TRAMO INTO PROF TRAMO ---
						log.info("ENCONTRADO -> " + tramo);
						tramoActual = tramo;

					}
				}
				// --- IF HORA ACTUAL EQUALS HORA FIN ---
				else if (horaActual == horaFin)
				{
					// --- CHEKING IF THE MINUTO ACTUAL IS LESS THAN MINUTO FIN ---
					if (minutoActual <= minutoFin)
					{
						// --- SETTING THE VALUE OF TRAMO INTO PROF TRAMO ---
						log.info("ENCONTRADO -> " + tramo);
						tramoActual = tramo;

					}
				}
			}
		}
		return tramoActual;
	}

	/**
	 * Method getTeachersSchedule
	 *
	 * @return ResponseEntity , File PDF
	 */
	@RequestMapping(value = "/get/teachers/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
	public ResponseEntity<?> getTeachersSchedule()
	{
		try
		{
			Map<Profesor, Map<String, List<Actividad>>> mapProfesors = new HashMap<>();
			if (this.centroPdfs != null)
			{
				// --- CENTRO PDF IS LOADED---
				for (Profesor profesor : this.centroPdfs.getDatos().getProfesores().getProfesor())
				{
					// --- FOR EACH PROFESOR ---
					HorarioProf horarioProf = null;
					for (HorarioProf horarioPrf : this.centroPdfs.getHorarios().getHorariosProfesores()
							.getHorarioProf())
					{
						if (horarioPrf.getHorNumIntPR().trim().equalsIgnoreCase(profesor.getNumIntPR().trim()))
						{
							horarioProf = horarioPrf;
						}
					}

					if (horarioProf != null)
					{
						// --- HORARIO PROF EXISTS ---

						// --- FOR EACH ACTIVIDAD ---
						Map<String, List<Actividad>> mapProfesor = new HashMap<>();
						for (Actividad atcv : horarioProf.getActividad())
						{
							TimeSlot temporalTramo = this.extractTramoFromCentroActividad(this.centroPdfs, atcv);

							if (!mapProfesor.containsKey(temporalTramo.getDayNumber().trim()))
							{
								List<Actividad> temporalList = new ArrayList<>();
								temporalList.add(atcv);
								mapProfesor.put(temporalTramo.getDayNumber().trim(), temporalList);
							} else
							{
								List<Actividad> temporalList = mapProfesor.get(temporalTramo.getDayNumber().trim());
								temporalList.add(atcv);
								mapProfesor.put(temporalTramo.getDayNumber().trim(), temporalList);
							}
						}

						// --- ADD THE PROFESSOR WITH THE PROFESSOR MAP ---
						mapProfesors.put(profesor, mapProfesor);
					} else
					{
						log.error("ERROR profesor " + profesor + " HORARIO PROF NOT FOUND OR NULL");
					}
				}

				try
				{
					// --- USING APPLICATION PDF TO GENERATE THE PDF , WITH ALL TEACHERS ---
					ApplicationPdf applicationPdf = new ApplicationPdf();
					applicationPdf.getAllTeachersPdfInfo(mapProfesors, this.centroPdfs);

					// --- GETTING THE PDF BY NAME URL ---
					File file = new File("All_Teachers_Horarios.pdf");

					// --- SETTING THE HEADERS WITH THE NAME OF THE FILE TO DOWLOAD PDF ---
					HttpHeaders responseHeaders = new HttpHeaders();
					// --- SET THE HEADERS ---
					responseHeaders.set("Content-Disposition", "attachment; filename=" + file.getName());

					try
					{
						// --- CONVERT FILE TO BYTE[] ---
						byte[] bytesArray = Files.readAllBytes(file.toPath());

						// --- RETURN OK (200) WITH THE HEADERS AND THE BYTESARRAY ---
						return ResponseEntity.ok().headers(responseHeaders).body(bytesArray);
					} catch (IOException exception)
					{
						// --- ERROR ---
						String error = "ERROR GETTING THE BYTES OF PDF ";

						log.info(error);

						HorariosError horariosError = new HorariosError(500, error, exception);
						log.info(error, horariosError);
						return ResponseEntity.status(500).body(horariosError);
					}
				} catch (HorariosError exception)
				{
					// --- ERROR ---
					String error = "ERROR getting the info pdf ";

					log.info(error);

					HorariosError horariosError = new HorariosError(400, error, exception);
					log.info(error, horariosError);
					return ResponseEntity.status(400).body(horariosError);
				}
			} else
			{
				// --- ERROR ---
				String error = "ERROR centroPdfs IS NULL OR NOT FOUND";

				log.info(error);

				HorariosError horariosError = new HorariosError(400, error, null);
				log.info(error, horariosError);
				return ResponseEntity.status(400).body(horariosError);
			}
		} catch (Exception exception)
		{
			String error = "Server Error";
			HorariosError horariosError = new HorariosError(500, error, exception);
			log.error(error, exception);
			return ResponseEntity.status(500).body(horariosError);
		}
	}

	/**
	 * Method getTeachersSchedule
	 *
	 * @return ResponseEntity , File PDF
	 */
	@RequestMapping(value = "/get/grupos/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
	public ResponseEntity<?> getGlobalSchedule()
	{
		try
		{
			Map<Grupo, Map<String, List<Actividad>>> mapGroups = new HashMap<>();
			if (this.centroPdfs != null)
			{
				// --- CENTRO PDF IS LOADED---
				for (Grupo grupo : this.centroPdfs.getDatos().getGrupos().getGrupo())
				{
					// --- FOR EACH GRUPO ---
					HorarioGrup horarioGrup = null;
					for (HorarioGrup horarioGrp : this.centroPdfs.getHorarios().getHorariosGrupos().getHorarioGrup())
					{
						if (horarioGrp.getHorNumIntGr().trim().equalsIgnoreCase(grupo.getNumIntGr().trim()))
						{
							horarioGrup = horarioGrp;
						}
					}

					if (horarioGrup != null)
					{
						// --- HORARIO GRUP EXISTS ---

						// --- FOR EACH ACTIVIDAD ---
						Map<String, List<Actividad>> mapGroup = new HashMap<>();
						for (Actividad atcv : horarioGrup.getActividad())
						{
							TimeSlot temporalTramo = this.extractTramoFromCentroActividad(this.centroPdfs, atcv);

							if (!mapGroup.containsKey(temporalTramo.getDayNumber().trim()))
							{
								List<Actividad> temporalList = new ArrayList<>();
								temporalList.add(atcv);
								mapGroup.put(temporalTramo.getDayNumber().trim(), temporalList);
							} else
							{
								List<Actividad> temporalList = mapGroup.get(temporalTramo.getDayNumber().trim());
								temporalList.add(atcv);
								mapGroup.put(temporalTramo.getDayNumber().trim(), temporalList);
							}
						}

						// --- ADD THE PROFESSOR WITH THE PROFESSOR MAP ---
						mapGroups.put(grupo, mapGroup);
					} else
					{
						log.error("ERROR grupo " + grupo + " HORARIO grup NOT FOUND OR NULL");
					}
				}

				try
				{
					// --- USING APPLICATION PDF TO GENERATE THE PDF , WITH ALL TEACHERS ---
					ApplicationPdf applicationPdf = new ApplicationPdf();
					applicationPdf.getAllGroupsPdfInfo(mapGroups, this.centroPdfs);

					// --- GETTING THE PDF BY NAME URL ---
					File file = new File("All_Groups_Horarios.pdf");

					// --- SETTING THE HEADERS WITH THE NAME OF THE FILE TO DOWLOAD PDF ---
					HttpHeaders responseHeaders = new HttpHeaders();
					// --- SET THE HEADERS ---
					responseHeaders.set("Content-Disposition", "attachment; filename=" + file.getName());

					try
					{
						// --- CONVERT FILE TO BYTE[] ---
						byte[] bytesArray = Files.readAllBytes(file.toPath());

						// --- RETURN OK (200) WITH THE HEADERS AND THE BYTESARRAY ---
						return ResponseEntity.ok().headers(responseHeaders).body(bytesArray);
					} catch (IOException exception)
					{
						// --- ERROR ---
						String error = "ERROR GETTING THE BYTES OF PDF ";

						log.info(error);

						HorariosError horariosError = new HorariosError(500, error, exception);
						log.info(error, horariosError);
						return ResponseEntity.status(500).body(horariosError);
					}
				} catch (HorariosError exception)
				{
					// --- ERROR ---
					String error = "ERROR getting the info pdf ";

					log.info(error);

					HorariosError horariosError = new HorariosError(400, error, exception);
					log.info(error, horariosError);
					return ResponseEntity.status(400).body(horariosError);
				}
			} else
			{
				// --- ERROR ---
				String error = "ERROR centroPdfs IS NULL OR NOT FOUND";

				log.info(error);

				HorariosError horariosError = new HorariosError(400, error, null);
				log.info(error, horariosError);
				return ResponseEntity.status(400).body(horariosError);
			}
		} catch (Exception exception)
		{
			String error = "Server Error";
			HorariosError horariosError = new HorariosError(500, error, exception);
			log.error(error, exception);
			return ResponseEntity.status(500).body(horariosError);
		}
	}

	/**
	 * method getListAlumnoFirstSurname
	 *
	 * @param course
	 * @return
	 */
	// REQUEST MAPPING FOR GETTING SORTED STUDENT LIST BASED ON FIRST SURNAME AND
	// COURSE
	@RequestMapping(value = "/get/course/sort/students", produces = "application/json")
	public ResponseEntity<?> getListAlumnoFirstSurname(@RequestParam(required = true) String course)
	{
		try
		{
			if (this.students.isEmpty())
			{
				throw new HorariosError(409, "No hay alumnos cargados en el servidor");
			}

			Student[] sortStudents = this.studentOperation.sortStudentCourse(course, this.students);

			return ResponseEntity.ok().body(sortStudents);
		} catch (HorariosError exception)
		{
			log.error("Error al devolver los alumnos ordenados", exception);
			return ResponseEntity.status(400).body(exception.toMap());
		} catch (Exception exception)
		{
			// -- CATCH ANY ERROR --
			// RETURN A SERVER ERROR MESSAGE AS A RESPONSEENTITY WITH HTTP STATUS 500
			// (INTERNAL SERVER ERROR)
			String error = "Server Error";
			HorariosError horariosError = new HorariosError(500, error, exception);
			log.error(error, exception);
			return ResponseEntity.status(500).body(horariosError);
		}
	}

	@RequestMapping(value = "/get/students-course", produces = "application/json")
	public ResponseEntity<?> getStudentsCourse()
	{
		try
		{

			if (this.students.isEmpty())
			{
				throw new HorariosError(409, "No hay alumnos cargados en el servidor");
			}

			List<String> courseStudent = new LinkedList<String>();

			for (Student student : this.students)
			{
				if (!courseStudent.contains(student.getCourse()))
				{
					courseStudent.add((student.getCourse()));
				}
			}

			Collections.sort(courseStudent);

			return ResponseEntity.ok().body(courseStudent);
		} catch (HorariosError exception)
		{
			log.error("Error al devolver los cursos de los alumnos", exception);
			return ResponseEntity.status(409).body(exception.toMap());
		} catch (Exception exception)
		{
			// -- CATCH ANY ERROR --
			// RETURN A SERVER ERROR MESSAGE AS A RESPONSEENTITY WITH HTTP STATUS 500
			// (INTERNAL SERVER ERROR)
			String error = "Server Error";
			HorariosError horariosError = new HorariosError(500, error, exception);
			log.error(error, exception);
			return ResponseEntity.status(500).body(horariosError);
		}
	}

	// ENDPOINT FOR GETTING COEXISTENCE ACTITUDE POINTS

	/**
	 * Method getListPointsCoexistence
	 * 
	 * @return ResponseEntity
	 */
	@RequestMapping(value = "/get/points", produces = "application/json")
	public ResponseEntity<?> getListPointsCoexistence()
	{
		try
		{
			List<ActitudePoints> listActitudePoints = this.util.loadPoints();

			// --CHECK IF THE LIST OF ACTITUDE POINTS IS NOT EMPTY--
			if (!listActitudePoints.isEmpty())
			{
				// --RETURN THE LIST OF COEXISTENCE ACTITUDE POINTS AS A RESPONSEENTITY WITH
				// HTTP STATUS 200 (OK)--
				return ResponseEntity.ok().body(listActitudePoints);
			} else
			{
				// --RETURN AN ERROR MESSAGE AS A RESPONSEENTITY WITH HTTP STATUS 400 (BAD
				// REQUEST)--
				String error = "List not found";
				HorariosError horariosError = new HorariosError(400, error, null);
				log.error(error);
				return ResponseEntity.status(400).body(horariosError);
			}
		} catch (Exception exception)
		{
			// CATCH ANY ERROR
			String error = "Server Error";
			HorariosError horariosError = new HorariosError(500, error, exception);
			log.error(error, exception);
			// --RETURN A SERVER ERROR MESSAGE AS A RESPONSEENTITY WITH HTTP STATUS 500
			// (INTERNAL SERVER ERROR)--
			return ResponseEntity.status(500).body(horariosError);
		}
	}

	@RequestMapping(value = "/get/coursenames", produces = "application/json")
	public ResponseEntity<?> getCourseNames()
	{
		try
		{
			// Crea una lista vacia de grupos.
			List<Grupo> grupos = grupoRepo.recuperaGruposDeParseo();

			return ResponseEntity.ok().body(this.util.ordenarLista(grupos));
		} catch (Exception exception)
		{
			// -- CATCH ANY ERROR --
			String error = "Server Error";
			HorariosError horariosError = new HorariosError(500, error, exception);
			log.error(error, exception);
			// -- RETURN A SERVER ERROR MESSAGE AS A RESPONSEENTITY WITH HTTP STATUS 500
			// (INTERNAL SERVER ERROR) --
			return ResponseEntity.status(500).body(horariosError);
		}
	}

	@RequestMapping(value = "/send/csv-alumnos", consumes = "multipart/form-data")
	public ResponseEntity<?> loadStudents(@RequestPart(name = "csvFile", required = true) MultipartFile csvFile)
	{
		try
		{
			byte[] content = csvFile.getBytes();
			this.students = this.studentOperation.parseStudent(content);
			return ResponseEntity.ok().body(students);
		} catch (HorariosError exception)
		{
			log.error("El fichero introducido no contiene los datos de los alumnos bien formados", exception);
			return ResponseEntity.status(406).body(exception.toMap());
		} catch (Exception exception)
		{
			log.error("Error de servidor", exception);
			return ResponseEntity.status(500).body("Error de servidor " + exception.getStackTrace());
		}
	}

	@RequestMapping(value = "send/csv-planos", consumes = "multipart/form-data")
	public ResponseEntity<?> loadPlanos(@RequestPart(name = "csvFile", required = true) MultipartFile csvFile)
	{
		try
		{
			byte[] content = csvFile.getBytes();
			if (!csvFile.getOriginalFilename().endsWith(".csv"))
			{
				throw new HorariosError(406, "El fichero no es un csv");
			}
			this.aulas = this.util.parseAulasPlano(content);
			return ResponseEntity.ok().body(aulas);
		} catch (HorariosError exception)
		{
			log.error("El fichero introducido no contiene los datos de los planos bien formados", exception);
			return ResponseEntity.status(406).body(exception.toMap());
		} catch (Exception exception)
		{
			log.error("Error de servidor", exception);
			return ResponseEntity.status(500).body("Error de servidor " + exception.getStackTrace());
		}
	}

	@RequestMapping(value = "/get/classroom-planos", produces = "application/json")
	public ResponseEntity<?> getAllClassroom(@RequestParam(required = false) String planta)
	{
		try
		{
			return ResponseEntity.ok().body(this.util.buscarPorPlanta(planta, this.aulas));
		} catch (HorariosError exception)
		{
			log.error("Error al filtrar las aulas", exception);
			return ResponseEntity.status(exception.getCode()).body(exception.toMap());
		} catch (Exception exception)
		{
			log.error("Error de servidor", exception);
			return ResponseEntity.status(500).body("Error de servidor " + exception.getStackTrace());
		}
	}

	@RequestMapping(value = "/send/error-info", consumes = "application/json")
	public ResponseEntity<?> sendErrorInfo(@RequestBody(required = false) InfoError objectError)
	{
		this.infoError = objectError;
		return ResponseEntity.ok().build();
	}

	@RequestMapping(value = "/get/error-info", produces = "application/json")
	public ResponseEntity<?> getInfoError()
	{
		return ResponseEntity.ok().body(this.infoError);
	}

	@RequestMapping(value = "/check-data", produces = "application/json")
	public ResponseEntity<?> checkServerData()
	{
		Map<String, String> errorMap = new HashMap<String, String>();
		if (this.centroPdfs == null)
		{
			errorMap.put("error", "Error de datos en general");
			return ResponseEntity.status(400).body(errorMap);
		} else if (this.students == null || this.students.isEmpty())
		{
			errorMap.put("error", "Error de datos de estudiantes");
			return ResponseEntity.status(400).body(errorMap);
		} else if (this.aulas == null || this.students.isEmpty())
		{
			errorMap.put("error", "Error de datos de planos");
			return ResponseEntity.status(400).body(errorMap);
		} else
		{
			return ResponseEntity.ok().body("Todo correcto");
		}
	}

	@RequestMapping(value = "/get/aula-now", produces = "application/json")
	public ResponseEntity<?> getCurrentClassroom(@RequestParam String numIntAu, @RequestParam String abreviatura,
			@RequestParam String nombre)
	{
		try
		{
			Map<String, Object> infoAula = new HashMap<String, Object>();
			Aula aula = new Aula(numIntAu, abreviatura, nombre);

			// Buscamos el aula
			List<Aula> aulas = this.centroPdfs.getDatos().getAulas().getAula();

			if (!aulas.contains(aula))
			{
				throw new HorariosError(404, "El aula seleccionada no se encuentra en los datos proporcionados");
			}

			// Obtenemos el profesor que se encuentra actualmente en el aula
			Profesor profesor = this.util.searchTeacherAulaNow(this.centroPdfs, aula);
			// Obtenemos la asignatura que se imparte actualmente en el aula
			Map<String, Object> asignaturaActividad = this.util.searchSubjectAulaNow(centroPdfs, profesor);
			// Sacamos la asignatura del mapa
			Asignatura asignatura = (Asignatura) asignaturaActividad.get("asignatura");
			// Sacamos la actividad del mapa
			Actividad actividad = (Actividad) asignaturaActividad.get("actividad");
			// Sacamos el grupo que se encuentra en el aula
			List<Grupo> grupos = this.util.searchGroupAulaNow(centroPdfs, actividad);
			// Sacamos los alumnos que se encuentran en el aula
			List<Student> alumnos = this.util.getAlumnosAulaNow(grupos, this.students);

			infoAula.put("profesor", profesor);
			infoAula.put("asignatura", asignatura);
			infoAula.put("grupo", grupos);
			infoAula.put("alumnos", alumnos);

			return ResponseEntity.ok().body(infoAula);
		} catch (HorariosError exception)
		{
			log.error("Error al mostrar la informacion del aula", exception);
			return ResponseEntity.status(exception.getCode()).body(exception.toMap());
		} catch (Exception exception)
		{
			log.error("Error de servidor", exception);
			return ResponseEntity.status(500).body("Error de servidor " + exception.getStackTrace());
		}
	}

	@RequestMapping(method = RequestMethod.POST, value = "/send/sancion", consumes = "application/json")
	public ResponseEntity<?> sendSancion(@RequestBody(required = true) Student student,
			@RequestParam(name = "value", required = true) Integer value,
			@RequestParam(name = "description", required = true) String description)
	{
		try
		{
			// Busqueda del estudiante
			this.util.findStudent(student, students);

			ActitudePoints points = new ActitudePoints(value, description);

			List<ActitudePoints> puntos = this.util.loadPoints();

			// Busqueda de puntos
			if (!puntos.contains(points))
			{
				throw new HorariosError(404, "Los puntos proporcionados no se encuentran en los datos generales");
			}

			// Guardamos la sancion en la base de datos
			this.operations.ponerSancion(student, points);

			return ResponseEntity.ok().build();
		} catch (HorariosError exception)
		{
			log.error("Los datos proporcionados no son correctos", exception);
			return ResponseEntity.status(exception.getCode()).body(exception.toMap());
		} catch (Exception exception)
		{
			log.error("Error de servidor", exception);
			return ResponseEntity.status(500).body("Error de servidor " + exception.getStackTrace());
		}
	}

	@RequestMapping(method = RequestMethod.GET, value = "/get/parse-course", produces = "application/json")
	public ResponseEntity<?> localizarAlumno(@RequestParam(name = "course", required = true) String studentCourse)
	{
		try
		{
			String course = this.util.parseStudentGroup(studentCourse,
					this.centroPdfs.getDatos().getGrupos().getGrupo());

			// Se coloca un mapa ya que los cursos de los datos generales pueden contener
			// caracteres que el front no lee bien y con el mapa forzamos a mandarlos
			Map<String, String> map = new HashMap<String, String>();
			map.put("curso", course);
			return ResponseEntity.ok().body(map);
		} catch (HorariosError exception)
		{
			log.error("No existe una relacion entre el curso del alumno con los datos generales", exception);
			return ResponseEntity.status(exception.getCode()).body(exception.toMap());
		} catch (Exception exception)
		{
			log.error("Error de servidor", exception);
			return ResponseEntity.status(500).body("Error de servidor " + exception.getStackTrace());
		}
	}

	@RequestMapping(method = RequestMethod.GET, value = "/get/alumnos-bathroom", produces = "application/json")
	public ResponseEntity<?> getAlumnosBathroom()
	{
		try
		{
			List<Student> students = this.operations.findStudentBathroom(this.students);
			students = students.isEmpty() ? null : students;
			return ResponseEntity.ok().body(students);
		} catch (HorariosError exception)
		{
			log.error("Error al encontrar los estudiantes en el baño", exception);
			return ResponseEntity.status(exception.getCode()).body(exception.toMap());
		} catch (Exception exception)
		{
			log.error("Error de servidor", exception);
			return ResponseEntity.status(500).body("Error de servidor " + exception.getStackTrace());
		}
	}

}
