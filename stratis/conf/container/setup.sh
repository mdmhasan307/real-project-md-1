#!/bin/sh

echo "Setting start script.."

chown tomcat:tomcat /start.sh
chmod 755 /start.sh

# Tomcat base container image sets up all standard volume folders listed below
# /data/storage/local, /data/storage/shared, /data/logs/tomcat, /data/logs/application

#any other folders needed for custom volumes/local in addition to the above can be defined here.
mkdir -p /data/storage/local/barcodes
mkdir -p /data/storage/local/barcodes/img
mkdir -p /data/storage/local/interfaces

chown -R tomcat:tomcat /data/storage/local/barcodes
chown -R tomcat:tomcat /data/storage/local/barcodes/img
chown -R tomcat:tomcat /data/storage/local/interfaces
chmod 777 /data/storage/local/barcodes
chmod 777 /data/storage/local/barcodes/img
chmod 777 /data/storage/local/interfaces

echo "Container Setup Completed."

