package es.iesjandula.reaktor.timetable_server.service;

import java.io.File;
import java.net.URISyntaxException;

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
	private String jarFilePath;
    private long lastModified;

    @PostConstruct
    public void init() {
        try {
            // Obtener la ruta completa donde se está ejecutando el archivo JAR
            jarFilePath = new File(getClass().getProtectionDomain().getCodeSource().getLocation().toURI().getPath()).getAbsolutePath();
        } catch (URISyntaxException e) {
            // Manejar excepción si no se puede obtener la ruta del archivo JAR
            e.printStackTrace();
        }

        // Obtener la fecha de modificación del archivo JAR
        lastModified = new File(jarFilePath).lastModified();
    }

    @Scheduled(fixedRate = 5000) // Ejecutar cada 5 segundos
    public void checkJarUpdate() {
        // Verificar si el archivo JAR ha sido actualizado
        if (new File(jarFilePath).lastModified() > lastModified) {
            System.out.println("¡El JAR ha sido actualizado! Finalizando la aplicación...");
            // Realizar cualquier limpieza o liberación de recursos necesaria antes de salir
            // ...

            // Salir de la aplicación
            System.exit(0);
        }
    }
}
