#!/bin/bash

echo "Welcome to snoqtt snort installation script!"
echo "--------------------------------------------"

username=""
password=""
device_id=""
sensor_key=""
netint=""

echo "To begin the installation you need to provide your credential, device_id, & sensor_key"
echo "--------------------------------------------"
echo "Username : "
read username
echo "Password : "
read -s password
echo "Device ID : "
read device_id
echo "Sensor Key : "
read sensor_key 
echo "Network Interface For Listen : "
read netint

curl -u ${username}:${password} -X POST \
http://103.24.56.244:5000/api/sensors/v1.0/verifysensorkey \
-H 'content-type: application/json' \
-d '{"device_id" : "'"${device_id}"'", "sensor_key" : "'"${sensor_key}"'", "netint" : "'"${netint}"'"}' \
-o package.tar.gz

mkdir -p /etc/snoqtt
tar xvzf package.tar.gz -C /etc/snoqtt

chmod a+x /etc/snoqtt/build_snoqtt.sh
chmod a+x /etc/snoqtt/remove_snoqtt.sh
chmod a+x /etc/snoqtt/start_snoqtt.sh
chmod a+x /etc/snoqtt/stop_snoqtt.sh

echo "Checking docker installation..."
if [ -x "$(command -v)" ]; then
    echo "Docker Already Installed"
else
    echo "Installing docker..."
    curl -fsSL https://download.docker.com/linux/debian/gpg | sudo apt-key add -
    sudo add-apt-repository "deb [arch=amd64] https://download.docker.com/linux/debian $(lsb_release -cs) stable"
    apt-get update
    apt-get install -y docker-ce
    service docker start
    echo "Docker installed"

echo "Installation Done"
echo "Please run build_snoqtt.sh script in /etc/snoqtt directory"