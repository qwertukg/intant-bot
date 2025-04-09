#!/usr/bin/env bash

export HOST="asdf"
export USER="asdf"
export PASS="asdf"
export TELEGRAM_BOT_NAME="asdf"
export TELEGRAM_BOT_TOKEN="asdf"

nohup java -jar bot.jar > bot.log 2>&1 &

echo "Started on PID $!"
