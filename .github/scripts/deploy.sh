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

DateTimeStamp=$(date +%Y-%m-%d_%H-%M-%S)

cd /home/ec2-user/namabhiksha
# nohup java -jar nbtelegramapp-0.0.1-SNAPSHOT.jar --spring.profiles.active=seva &

sudo java -jar nbtelegramapp-0.0.1-SNAPSHOT.jar --spring.profiles.active=seva > log+$DateTimeStamp+.log 2>&1 &

sleep 10
javapid=$(pidof java)

echo "java pid: $javapid"

ps -aux|grep java


if [[ -z $javapid ]]; then
  echo "java process did not start"
else
  echo "Application deployed and running...."
fi