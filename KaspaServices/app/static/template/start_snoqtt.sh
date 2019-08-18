#!/bin/bash

echo "Starting snoqtt docker images"
sudo docker run -d \
--rm --net=host \
--env-file env-conf.conf \
--name snoqtt-pcap \
-v /etc/snoqtt/pcap:/root/pcap \
snoqtt-pcap:5.9