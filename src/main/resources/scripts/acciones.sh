cp timetableserver.service /etc/systemd/system

chmod 777 /etc/systemd/system/timetableserver.service
chmod 777 reaktor_timetableserver.sh

# Añadir al arranque del servidor
sudo systemctl start timetableserver.service

# Arrancar el servicio
sudo systemctl enable timetableserver.service

# Ver el estado del servicio
sudo systemctl status timetableserver.service