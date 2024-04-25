package es.iesjandula.reaktor.timetable_server.service;

import java.io.File;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

@Service
@Configuration
@EnableScheduling
@Slf4j
public class JarUpdateService
{
    private long lastModified;

    @PostConstruct
    public void init()
    {
        // Obtener la ruta del archivo JAR en ejecución
        String jarFilePath = getClass().getProtectionDomain().getCodeSource().getLocation().getPath() ;
        File jarFile = new File(jarFilePath);

        // Obtener la fecha de modificación del archivo JAR
        this.lastModified = jarFile.lastModified() ;
    }

    @Scheduled(fixedRate = 5000) // Ejecutar cada 5 segundos
    public void checkJarUpdate()
    {
        // Obtener la ruta del archivo JAR en ejecución
        String jarFilePath = getClass().getProtectionDomain().getCodeSource().getLocation().getPath() ;
        File jarFile = new File(jarFilePath) ;
        
        log.info("JAR: {}, {}, {}", jarFilePath, jarFile.lastModified(), this.lastModified) ;

        // Verificar si el archivo JAR ha sido actualizado
        if (jarFile.lastModified() > this.lastModified)
        {
            log.info("¡El JAR ha sido actualizado! Finalizando la aplicación...") ;

            // Salir de la aplicación
            System.exit(0) ;
        }
    }
}
