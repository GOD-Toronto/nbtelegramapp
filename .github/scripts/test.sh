#!/bin/bash

kiperf=$(pidof java)

cd namabhiksha

if [[ -z $kiperf ]]; then
  echo "java process not running..."
else
  echo "JAVA process is running..."
fi