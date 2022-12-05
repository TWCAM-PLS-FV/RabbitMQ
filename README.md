After cloning the project run the script `setup.sh`.

This script:
-   Creates some directories (used in the containers)
-   Downloads the images for the experiments
-   Generates the script to perform HTTP requests
-   Creates the containers

The workflow is:

-   Edit the source code in one of the projects under `src` folder
-   Deploy artifacts (jar files to deploy subdirs) using `deploy_artifacts.sh` passing
    the name of the project to deploy (without arguments it deploys all projects).
-   Restart containers using `containerctl.sh restart container_name`
-   Check application and debug using `containerctl.sh logs container_name`

When the projects in the `src` folder are updated you must go through all that workflow

The most important script is `containerctl.sh`:

    containerctl.sh action container
      action:
          create: crea e inicia el contenedor pasado como argumento
          start: inicia el contenedor pasado como segundo argumento
          restart: reinicia el contenedor pasado como segundo argumento
          stop:  detiene el contenedor pasado como segundo argumento
          list: lista los contenedores en ejecución
          logs:  muestra el log del contedor pasado como segundo argumento
          top :  muestra procesos del contedor pasado como segundo argumento
          shell:  abre una shell en el contedor pasado como segundo argumento

      container: {upload-server,rabbitmq-broker,worker1,worker2,stats,nginx-internal,nginx-external}

Enjoy and learn!