#!/bin/bash

if [ ! -d "$DIRECTORIO" ]; then
    mkdir /root/servicios
fi

cp reaktor_timetableserver.sh /root/servicios
chmod 777 /root/servicios/reaktor_timetableserver.sh

cp timetableserver.service /etc/systemd/system
cp timetableserver.timer /etc/systemd/system

# Deshabilitar si existe el temporizador
sudo systemctl disable timetableserver.timer

# Parar el timer
sudo systemctl stop timetableserver.timer

# Desabilitar si existe el servicio
sudo systemctl disable timetableserver.service

# Parar el servicio
sudo systemctl stop timetableserver.service

# Añadir al arranque del servidor
sudo systemctl start timetableserver.service

# Arrancar el servicio
sudo systemctl enable timetableserver.service

# Recargar systemd para que reconozca los nuevos archivos de configuración
sudo systemctl daemon-reload

# Habilitar y comenzar el temporizador
sudo systemctl enable --now timetableserver.timer