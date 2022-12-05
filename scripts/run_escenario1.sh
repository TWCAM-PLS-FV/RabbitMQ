# Borrar datos
rm -f ../deploy/data/subidas/*.png
rm -f ../deploy/data/procesadas/*.png

# Iniciar los contenedores para el experimento
./containerctl.sh start rabbitmq-broker
sleep 5
./containerctl.sh start worker1
sleep 5
./containerctl.sh start stats
sleep 5
./containerctl.sh start upload-server
sleep 5

# Realizar las peticiones POST a la aplicación Web
#   copiar el fichero o llamarlo ./peticiones.sh

# Al finalizar obtener el fichero con los tiempos que se encuentra en deploy/stats
#     y copiarlo a ../results/escenario1_tiempos.txt
