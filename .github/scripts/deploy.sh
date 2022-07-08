#!/bin/bash

cd ./build/libs/

cp nbtelegramapp-0.0.1-SNAPSHOT.jar /home/ec2-user/namabhiksha

kiperf=$(pidof java)

if [[ -z $kiperf ]]; then
  echo "java process not running..."
else
  echo "Killing java process..."  
  sudo kill -9 $kiperf
fi

echo "Delete the old log file"
rm -v *.out

echo "Bringing the App up"

pwd

cd /home/ec2-user/namabhiksha
nohup java -jar nbtelegramapp-0.0.1-SNAPSHOT.jar --spring.profiles.active=seva &

sleep 10
javapid=$(pidof java)

echo "java pid: $javapid"

ps -ef|grep java

if [[ -z $javapid ]]; then
  echo "java process did not start"
else
  echo "Application deployed and running...."
fi