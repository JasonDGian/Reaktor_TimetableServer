cp timetableserver.service /etc/systemd/system
cp timetableserver.timer /etc/systemd/system

chmod 777 /etc/systemd/system/timetableserver.service
chmod 777 reaktor_timetableserver.sh
chmod 777 /etc/systemd/system/timetableserver.timer

# Desabilitar si existe el servicio
sudo systemctl disabled timetableserver.service

# Parar el servicio
sudo systemctl stop timetableserver.service

# Añadir al arranque del servidor
sudo systemctl start timetableserver.service

# Arrancar el servicio
sudo systemctl enable timetableserver.service

# Ver el estado del servicio
sudo systemctl status timetableserver.service

# Recargar systemd para que reconozca los nuevos archivos de configuración
sudo systemctl daemon-reload

# Habilitar y comenzar el temporizador
sudo systemctl enable --now timetableserver.timer