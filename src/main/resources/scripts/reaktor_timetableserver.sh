#!/bin/bash

# Ruta al directorio donde se encuentra el JAR
JAR_PATH="/root/Reaktor_TimetableServer/actions-runner/_work/Reaktor_TimetableServer/Reaktor_TimetableServer/target"

# Nombre del JAR
JAR_NAME="TimetableServer-0.0.1-SNAPSHOT-jar-with-dependencies.jar"

# Archivo de marca para almacenar la última fecha de modificación del JAR
LAST_MODIFIED_FILE="$JAR_PATH/last_modified.txt"

# Nombre del servicio
SERVICE_NAME="timetableserver.service"

# Archivo de bloqueo
LOCK_FILE="/var/lock/$SERVICE_NAME.lock"

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

		# Bloquear el archivo para evitar que se ejecute el reinicio más de una vez simultáneamente
        if flock -n 9; then

            systemctl restart "$SERVICE_NAME"

        else

            echo "No se pudo bloquear el reinicio del servicio. Otro reinicio ya está en curso."

        fi
        
    fi

    # Actualizar la marca con la nueva fecha de modificación
    echo "$current_modified" > "$LAST_MODIFIED_FILE"

    fi
}

# Función para verificar si el proceso está en ejecución
check_process() {

    if ! pgrep -f "$JAR_NAME" > /dev/null; then

        echo "El proceso no está en ejecución. Reiniciando el servicio..."

        # Bloquear el archivo para evitar que se ejecute el reinicio más de una vez simultáneamente
        if flock -n 9; then

            systemctl restart "$SERVICE_NAME"

        else

            echo "No se pudo bloquear el reinicio del servicio. Otro reinicio ya está en curso."

        fi
    fi
}

# Verificar cambios en el JAR y estado del proceso
check_jar_changes

# Redirigir el descriptor de archivo para el bloqueo, evitando que se produzcan conflictos entre las ejecuciones de check_jar_changes y check_process
check_process 9>"$LOCK_FILE"