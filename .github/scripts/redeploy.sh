#!/bin/bash
# echo "------ Deleting Jar and log files in /home/ec2-user/namabhiksha/"
# cd /home/ec2-user/namabhiksha/
# rm -f *.jar
# rm -f *.log

# cd /home/ec2-user/namabhiksha/logs
# rm -f nbtelegramapp.log

echo "------ Fetch the latest Jar file name and copy the file to namabhiksha folder"

cd /home/ec2-user/namabhiksha/superceded
fileName=$(ls -r | head -1)
echo "filename ---> "$fileName
cp $fileName ../
DateTimeStamp=$(date +%Y-%m-%d_%H-%M-%S)
logFileName=log_$DateTimeStamp.log

echo "------ List the files to namabhiksha folder"
cd /home/ec2-user/namabhiksha/
ls -l

echo "------ Bring the app the app"
kiperf=$(pidof java)

if [[ -z $kiperf ]]; then
  echo "java process not running..."
  # sudo nohup java -jar nbtelegramapp-0.0.1-SNAPSHOT_*.jar --spring.profiles.active=seva > $logFileName 2>&1 &
  # sleep 15
else
  echo "JAVA process is running..."
  # kill -9 $(pidof java)
  # sudo nohup java -jar nbtelegramapp-0.0.1-SNAPSHOT_*.jar --spring.profiles.active=seva > $logFileName 2>&1 &
  # sleep 15
fi