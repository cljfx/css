#!/usr/bin/env bash
clj -A:build:compile
clj -Spom
clj -A:depstar css.jar
printf "Clojars Username: "
read -r username
stty -echo
printf "Clojars Password: "
read -r password
printf "\n"
stty echo
CLOJARS_USERNAME=${username} CLOJARS_PASSWORD=${password} clj -A:deploy css.jar
rm css.jar