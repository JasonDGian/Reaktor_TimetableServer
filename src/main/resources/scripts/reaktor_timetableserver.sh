#!/bin/bash

# Ruta al directorio donde se encuentra el JAR
JAR_PATH="/home/github_runners/Reaktor_TimetableServer/actions-runner/_work/Reaktor_TimetableServer/Reaktor_TimetableServer/target"

# Nombre del JAR
JAR_NAME="TimetableServer-0.0.1-SNAPSHOT-jar-with-dependencies.jar"

# Archivo de marca para almacenar la última fecha de modificación del JAR
LAST_MODIFIED_FILE="$JAR_PATH/last_modified.txt"

# Función para obtener la fecha de modificación actual del JAR
get_jar_last_modified() {
    stat -c %Y "$JAR_PATH/$JAR_NAME"
}

# Función para verificar si el JAR ha cambiado
check_jar_changes() {

	if [ -f "$LAST_MODIFIED_FILE" ]; then
	
        # Si el archivo de marca existe, leer la fecha de modificación almacenada
        last_modified=$(cat "$LAST_MODIFIED_FILE")

    else

        # Si el archivo de marca no existe, obtener la fecha de modificación actual del JAR
        last_modified=$(get_jar_last_modified)

    fi

    # Obtener la fecha de modificación actual del JAR
    current_modified=$(get_jar_last_modified)

    if [ "$current_modified" -gt "$last_modified" ]; then
        
        echo "El JAR ha cambiado. Reiniciando el servicio..."
        
        # Detener el proceso si está en ejecución
        pkill -f "$JAR_NAME"
        
        # Iniciar el proceso nuevamente
        java -jar "$JAR_PATH/$JAR_NAME" --spring.profiles.active=VPS &
        
        # Actualizar la marca con la nueva fecha de modificación
        echo "$current_modified" > "$LAST_MODIFIED_FILE"
        
    fi
}

# Función para verificar si el proceso está en ejecución
check_process() {

	if ! pgrep -f "$JAR_NAME" > /dev/null; then

        echo "El proceso no está en ejecución. Iniciando el servicio..."

        # Iniciar el proceso si no está en ejecución
        java -jar "$JAR_PATH/$JAR_NAME" --spring.profiles.active=VPS &
        
        echo "Se ha tratado de lanzar el proceso..."

    fi
}

# Verificar cambios en el JAR y estado del proceso
check_jar_changes
check_process