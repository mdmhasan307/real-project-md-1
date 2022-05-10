#!/bin/bash

if fgrep "CACFLAG" application/public_html/WEB-INF/web.xml -A2 | grep "<env-entry-value>1</env-entry-value>"
then
  echo web.xml CACFLAG is set to 1 as required
else
  echo BUILD FAILURE: web.xml CACFLAG is not set to 1 as required
  exit 1 
fi
