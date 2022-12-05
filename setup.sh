#!/bin/bash
echo "Creating containers (this may take some time)..."
cd deploy
docker compose up -d
sleep 10
docker compose stop
cd ../scripts

echo "The workflow is:"
echo " - Edit source files in the projects in src folder"
echo " - Deploy artifacts (jar files to deploy subdirs) using deploy_artifacts.sh"
echo " - Restart containers using containerctl.sh restart <container-name>"
echo " - Check application and debug using containerctl.sh logs container"
echo " "
echo " When the src folder is updated, you must go through all this workflow"
echo " "
echo " Enjoy and learn!"
