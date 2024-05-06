package es.iesjandula.reaktor.timetable_server;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.transaction.annotation.Transactional;



/**
 * @author David Martinez
 *
 */
@SpringBootApplication
@ComponentScan( basePackages = "es.iesjandula.reaktor.timetable_server")
public class TimetableApplication implements CommandLineRunner
{
	/**
	 * Method main to run spring app
	 * @param args main arguments
	 */
	public static void main(String[] args)
	{
		SpringApplication.run(TimetableApplication.class, args);
	}

	@Transactional( readOnly = false)
	public void run(String... args) throws Exception 
	{
		
		
	}

}
