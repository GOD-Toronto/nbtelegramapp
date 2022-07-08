#!/bin/bash

cd namabhiksha

kiperf=$(pidof java)

if [[ -z $kiperf ]]; then
  echo "java process not running..."
else
  echo "Killing java process..."  
  kill -9 $kiperf
fi

echo "Removing the old jar"
rm -rf *.jar 

echo "Downloading latest jar from S3"
aws s3 cp s3://nama-bhiksha/nbtelegramapp-0.0.1-SNAPSHOT.jar .

echo "Bringing the App up"
nohup java -jar nbtelegramapp-0.0.1-SNAPSHOT.jar --spring.profiles.active=seva &

sleep 10

javapid=$(pidof java)

echo "java pid: $javapid"

if [[ -z $javapid ]]; then
  echo "java process did not start"
else
  echo "Application deployed and running...."
fi