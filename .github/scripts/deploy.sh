#!/bin/bash

kiperf=$(pidof java)

cd namabhiksha

if [[ -z $kiperf ]]; then
  echo "java process not running..."
else
  kill -9 $kiperf
fi

rm -rf *.jar 

aws s3 cp s3://nama-bhiksha/nbtelegramapp-0.0.1-SNAPSHOT.jar .

nohup java -jar nbtelegramapp-0.0.1-SNAPSHOT.jar --spring.profiles.active=seva &

sleep 10

javapid=$(pidof java)

if [[ -z $javapid ]]; then
  echo "java process did not start"
else
  echo "Application deployed and running...."
fi