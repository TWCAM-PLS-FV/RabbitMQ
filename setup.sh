#!/bin/bash

echo "Creating directories..."

# Directories to put the app for each container
mkdir -p deploy/upload-server/app
mkdir -p deploy/stats/app
mkdir -p deploy/worker1/app
mkdir -p deploy/worker2/app

# Directories to put and download images (used by upload-server and nginx)
mkdir -p deploy/data/blur
mkdir -p deploy/data/edges
mkdir -p deploy/data/gray
mkdir -p deploy/data/procesadas

# Directory to store de time (used by stats container)
mkdir -p deploy/stats/times

# Directory to store images (used to make HTTP request)
mkdir images

# Directory to store the times after each experiment
mkdir results

echo "Downloading images..."
cd images
wget https://routerdi1315.uv.es/public/pls/images.tgz
tar xzvf images.tgz
cd ..

echo "Installing opencv jar in local maven repo (to use as dependency)"
cd src/worker
mvn install:install-file \
   -Dfile=lib/opencv-342.jar \
   -DgroupId=opencv \
   -DartifactId=opencv \
   -Dversion=3.4.2 \
   -Dpackaging=jar
cd ../..

cd scripts
echo "Generating script/peticiones.sh that make POST resquests.."
# Time to wait before making another POST request (in seconds)
time=0.01
rm peticiones.sh
declare -a ops
ops=(edges gray blur)
imgDir=../images
[[ -z $(ls $imgDir/*.png 2> /dev/null) ]] && tar xzvf $imgDir/images.tgz -C $imgDir
for i in $(seq 0 99); do
    indice=$i%3;
    # Pasamos el parámetro bucket y la imagen en una petición multipart
    echo "curl -H 'Expect:' -F \"bucket=/${ops[$indice]}\" -F \"IMAGE=@$imgDir/images-$i.png\" http://localhost:9000/upload &" >> peticiones.sh
    echo "sleep $time" >> peticiones.sh
done
cd ..


echo "Creating containers (this may take some time)..."
cd deploy
docker-compose up -d
sleep 10
docker-compose stop
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
