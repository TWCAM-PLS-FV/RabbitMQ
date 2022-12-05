
option=$1

function help {
    echo "deploy_artifacts.sh"
    echo " Provide ONE of the following arguments:"
    echo "   upload:      generates jar and deploy it to upload-server"
    echo "   workers:  generates jar and deploy it to workers"
    echo "   stats:    generates jar and deploy it to stats"
    echo ""
    echo "With no argument: deploys the three projects"
    echo ""
}


function deploy_upload {
    # Generación y copia de la aplicación que ejecuta la aplicación con el servidor embebido
    cd ../src/upload-server
    mvn clean package
    cp target/*.jar ../../deploy/upload-server/app/
    cd ../../scripts
}

function deploy_workers {
    # Generación y copia de la aplicación que se ejecutará en los workers
    cd ../src/worker
    mvn clean package
    cp target/*.jar ../../deploy/worker1/app/
    cp target/*.jar ../../deploy/worker2/app/
    cd ../../scripts
}

function deploy_stats {
    # Generación y copia de la aplicación que toma las estadísticas de tiempos
    cd ../src/stats
    mvn clean package
    cp target/*.jar ../../deploy/stats/app/
    cd ../../scripts
}

function deploy_all {
    deploy_upload
    deploy_workers
    deploy_stats
}


if [ -z "$option" ]; then
    option="all"
fi

if [ "$option" = "upload-server" ]; then
    deploy_upload
elif [ "$option" = "workers" ]; then
    deploy_workers
elif [ "$option" = "stats" ]; then
    deploy_stats
elif [ "$option" = "all" ]; then
    deploy_all
else
    help
fi
