#!/bin/bash
echo "Present PWD ---> " 
pwd

cd ./build/libs/
rm nbtelegramapp-0.0.1-SNAPSHOT_*-plain.jar
cp nbtelegramapp-0.0.1-SNAPSHOT_*.jar /home/ec2-user/namabhiksha

kiperf=$(pidof java)

if [[ -z $kiperf ]]; then
  echo "java process not running..."
else
  echo "Killing java process..."  
  sudo kill -9 $kiperf
fi

echo "Bringing the App up"

cd /home/ec2-user/namabhiksha

DateTimeStamp=$(date +%Y-%m-%d_%H-%M-%S)
logFileName=log_$DateTimeStamp.log
sudo nohup java -jar nbtelegramapp-0.0.1-SNAPSHOT_*.jar --spring.profiles.active=seva > $logFileName 2>&1 &

sleep 15

javapid=$(pidof java)
echo "java pid: $javapid"

ps -aux|grep java

if [[ -z $javapid ]]; then
  echo "java process did not start"
else
  echo "Application deployed and running...."
fi

echo "************** ALL DONE... JAI G!! **************"
