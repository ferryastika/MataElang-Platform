#!/bin/bash

echo "Building Docker Image for snoqtt"
sudo docker build -t snoqtt-pcap:5.9 \
--build-arg PROTECTED_SUBNET={protected_subnet} \
--build-arg EXTERNAL_SUBNET={external_subnet} \
--build-arg OINKCODE={oinkcode} \
https://github.com/dimasmamot/snoqtt-alpine.git#master
