#!/bin/bash

cd ./build/libs/

kiperf=$(pidof java)

if [[ -z $kiperf ]]; then
  echo "java process not running..."
else
  echo "Killing java process..."  
  kill -9 $kiperf
fi

echo "Delete the old log file"
rm -v *.out

echo "Bringing the App up"

pwd

nohup java -jar nbtelegramapp-0.0.1-SNAPSHOT.jar --spring.profiles.active=seva &

sleep 5

javapid=$(pidof java)

echo "java pid: $javapid"

if [[ -z $javapid ]]; then
  echo "java process did not start"
else
  echo "Application deployed and running...."
fi