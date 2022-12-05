#!/bin/bash
function help(){
   echo "containerctl.sh action container"
   echo "  action:"
   echo "      create: crea e inicia el contenedor pasado como argumento"
   echo "      start: inicia el contenedor pasado como segundo argumento"
   echo "      restart: reinicia el contenedor pasado como segundo argumento"
   echo "      stop:  detiene el contenedor pasado como segundo argumento"
   echo "      list: lista los contenedores en ejecución"
   echo "      logs:  muestra el log del contedor pasado como segundo argumento"
   echo "      top :  muestra procesos del contedor pasado como segundo argumento"
   echo "      shell:  abre una shell en el contedor pasado como segundo argumento"
   echo ""
   echo "  container: {upload-server,rabbitmq-broker,worker1,worker2,stats,nginx-internal, nginx-external}"
   exit 0
}

if [ -z "$1" ]; then
   help
fi

action=$1
container=$2

cd ../deploy
if [ "$action" = "create" ]; then
  docker compose up -d "$container"
elif [ "$action" = "list" ]; then
  docker compose ps
elif [ "$action" = "shell" ]; then
  docker compose exec "$container" /bin/sh
elif [ "$action" = "restart" ]; then
    docker compose stop "$container"
    docker compose rm -f "$container"
    docker compose up -d "$container"
else
  docker compose "$action" "$container"
fi
cd ../scripts
