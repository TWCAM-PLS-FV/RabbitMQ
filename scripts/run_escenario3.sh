# Borrar datos
rm -f ../deploy/data/blur/*.png
rm -f ../deploy/data/edges/*.png
rm -f ../deploy/data/gray/*.png
rm -f ../deploy/data/procesadas/*.png

# Iniciar los contenedores para el experimento
./containerctl.sh start rabbitmq-server
sleep 20
./containerctl.sh start upload-server
sleep 5
./containerctl.sh start stats
sleep 5
./containerctl.sh start worker1
sleep 5
echo "$(./peticiones.sh)"
sleep 60
./containerctl.sh start worker2
sleep 5
