# TIMETABLE: Proyecto encargado de gestionar los horarios de los profesores y las incidencias de los alumnos en el centro IES Jandula

# Descripcion

Timetable server es un servidor que se lanzará dentro del propio instituto IES Jandula que 
guarda los datos del centro y alumnado, que se encarga de localizar a profesores y alumnos en tiempo real por distintos medios como sus datos, curso, aula que se encuentran, etc.

Posee una base de datos en MySql que por ahora guarda las visitas del alumnado al baño y las incidencias cometidas, con esto se controla qué alumno visita o no al baño evitando que se produzcan fraudes en los baños del centro. Además de que con el registro de incidencias se evita perder mucho tiempo de clase en poner una incidencia con el clásico sistema de "partes o incidencias".

El proyecto está realizado en java utilizando la tecnología de Spring Boot que actúa como servidor para recibir peticiones del cliente y servicio web. Además se utiliza el framework de JPA para las operaciones CRUD (Create, Read, Update, Delete) sobre la base de datos.

El servidor se comunica con el proyecto [Timetable Web](https://github.com/IESJandula/IJandula_TimeTableWeb) que representa la web que llama al servidor y muestra toda la información que ofrece.
# Manual de instalación

Necesitamos tener instalado [MySql](https://dev.mysql.com/downloads/file/?id=528489) para la base de datos, una vez que lo tengamos instalado abrimos MySql 8.0 Command Line Client y ejecutamos:

```
create database reaktor_timetableserver
```

La base de datos al crearla no se debe añadir ninguna tabla, procedimiento, función o trigger ya que lo gestionará JPA y es posible que haya errores a la hora de tratarla.

Una vez tengamos MySql instalado para compilar el proyecto y arrancarlo necesitamos la [JDK 17](https://download.oracle.com/java/17/archive/jdk-17.0.10_windows-x64_bin.exe) que es el compilador del proyecto.

Una vez con la JDK 17 instalada abrimos el proyecto, nos dirigimos al pom.xml y tendremos que controlar que existan las siguientes dependencias:

<ul>
    <li>Spring-Boot-Starter-Web: Herramientas básicas de Spring-Boot que habilitan más anotaciones de las que ofrece su dependencia padre.
    </li>
    <br>
    <li>Lombok: Dependencia que contiene anotaciones que ayudan que las clases sean compactas debido a que generan constructores y métodos Get y Set.</li>
    <br>
    <li>ItextPdf: dependencia que permite generar un pdf y personalizarlo con código Java.</li>
    <br>
    <li>Springdoc-Openapi-Starter-Webmvc-UI: dependencia que genera un swagger en la web de swagger.ui con la cual se pueden ejecutar los endpoints creados.</li>
    <br>
    <li>Springdoc-Openapi-Starter-Webflux-UI: dependencia que genera un swagger en un fichero yaml.</li>
    <br>
    <li>Commons-Lang3: dependencia que permite el método StackTrace en las excepciones.</li>
    <br>
    <li>Spring-Boot-Starter-Data-Jpa: Contiene el framework JPA con el cual se pueden generar operaciones CRUD para interactuar con la base de datos.</li>
    <br>
    <li>Mysql-Connector-J: Driver para conectar java con MySql. </li>
</ul>

Una vez que hemos verificado que se encuentran todas las dependencias tendremos que instalar la dependencia Lombok en nuestro [IDE Eclipse](https://www.eclipse.org/downloads/) el cual deberemos de tener instalado para que esta dependencia funcione, para ello nos dirigimos a nuetsra carpeta de usuario y ubicamos la carpeta .m2, una vez ubicada seguimos esta ruta:

```
.m2\repository\org\projectlombok\lombok\1.18.30
```

Antes de instalar lombok hay que asegurarse de que el IDE esté cerrado, si no la instalación no funcionará.

En caso de que tengamos una versión superior se selecciona la versión superior, ejecutamos el jar que se llama ```lombok-1.18.30```, se nos abrirá una ventana emergente con la cual seleccionaremos nuestro IDE y posteriormente seleccionamos install/update con esto tendremos lombok instalado.

El siguiente paso es abrir el IDE y sobre el proyecto dar click izquierdo Run As/Maven Build, y en el apartado de goals escribimos clean install, pulsamos sobre run y se limpiarán y se instalarán las dependencias pendientes, una vez termine le proceso tendremos lombok instalado.

Por último tendremos que tener una dependencia llamada BaseServer que es otro proyecto del centro que contiene todos los módulos de Reaktor y contiene también utilidades de los mismos, al principio en el pom nos saldrá que la dependencia no existe, para ello nos dirigimos al proyecto BaseServer hacemos click izquierdo sobre el proyecto y seleccionamos Run As/Maven Build, y en el apartado de goals escribimos clean install, con esto conseguimos tener la dependencia instalada en nuestro repositorio local y se nos quitará el error del pom.xml.

# Arranque del proyecto

Tenemos 2 formas de arrancar el proyecto, la primera es a través del jar generado al realizar el clean install al instalar BaseServer, el jar se encuentra en la carpeta target, para ejecutarlo abrimos la cmd en la carpeta target y ejecutamos:

```
java -jar TimetableServer-0.0.1-SNAPSHOT
```

La otra forma es en nuestro IDE Eclipse ejecutar la clase TimetableApplication.java

# Peticiones del servidor

Este proyecto guarda muchas peticiones ya sea por parte de administración como por parte del profesorado:

<ul>
    <li>Envío de fichero xml con información del profesorado, este fichero ( con estructura reglada ) contiene la información de los profesores, qué horas disponen diariamente, las asignaturas que imparten en esas horas y los días qye las imparten, esta petición solo pueden acceder administradores del centro</li>
    <br>
    <li>Envío de fichero csv con información de los alumnos, se guarda su nombre y apellido, dni, email y telefono del tutor esta petición solo pueden acceder administradores del centro</li>
    <br>
    <li>Localización de un profesor mediante sus datos, el curso que imparte clase o el aula que se encuentra en tiempo real</li>
    <br>
    <li>Localización de un alumno mediante sus datos, el curso al que pertenece o el aula que se encuentra en tiempo real</li>
    <br>
    <li>Registro en la base de datos de la visita de un alumno al baño, esto previene los fraudes que se cometen en el baño durante el horario de clase</li>
    <br>
    <li>Registro en la base de datos de una incidencia cometida por un alumno</li>
    <br>
    <li>Obtención de un pdf con el horario de la semana de un profesor o grupo</li>
    <br>
</ul>

# Créditos

El proyecto fue comenzado por los alumnos de 2 DAM supervisado por el profesor:

- [Francisco Benítez Chico](https://www.linkedin.com/in/franciscobenitezchico/)

La base de datos y la continuación del proyecto fue realizada por el alumno:

- [Pablo Elías Ruiz Cánovas](https://www.linkedin.com/in/pablo-el%C3%ADas-ruiz-c%C3%A1novas-315a66267/)

Supervisado por el profesor [Francisco Benítez Chico](https://www.linkedin.com/in/franciscobenitezchico/) .
