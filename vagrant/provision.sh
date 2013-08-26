#!/usr/bin/env bash

# Vagrantfile is configured to use this to provision
# a virtual Linux server for development

# Install 10gen's GPG key for MongoDB
sudo apt-key adv --keyserver hkp://keyserver.ubuntu.com:80 --recv 7F0CEB10
echo 'deb http://downloads-distro.mongodb.org/repo/ubuntu-upstart dist 10gen' | sudo tee /etc/apt/sources.list.d/10gen.list

apt-get update
apt-get install -y openjdk-7-jdk
apt-get install -y mongodb-10gen

