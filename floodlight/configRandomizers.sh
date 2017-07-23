#!/usr/bin/env bash
CONTROLLER_1_IP=localhost
CONTROLLER_2_IP=localhost
CONTROLLER_1_REST_PORT=8090
CONTROLLER_2_REST_PORT=8100

curl http://$CONTROLLER_1_IP:$CONTROLLER_1_REST_PORT/wm/randomizer/config/json -X POST -d '{"randomize":"false", "localport":"1", "wanport":"2"}' | python -m json.tool
curl http://$CONTROLLER_2_IP:$CONTROLLER_2_REST_PORT/wm/randomizer/config/json -X POST -d '{"randomize":"true", "localport":"1", "wanport":"2"}' | python -m json.tool

curl http://$CONTROLLER_1_IP:$CONTROLLER_1_REST_PORT/wm/randomizer/module/enable/json -X POST | python -m json.tool
curl http://$CONTROLLER_2_IP:$CONTROLLER_2_REST_PORT/wm/randomizer/module/enable/json -X POST | python -m json.tool