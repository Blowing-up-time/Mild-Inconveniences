@echo off
powershell Start-Process -FilePath "cmd" -ArgumentList "/s","/k","pushd","'%cd%'" -Verb RunAs
EXIT