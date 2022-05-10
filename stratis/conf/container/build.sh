#!/bin/sh

echo "Building Tomcat.."

docker build -t stratis_tomcat:test -f Dockerfile .

