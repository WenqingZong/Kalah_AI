@echo off
cd ../out/production/Kalah_AI
jar cfe AI.jar MKAgent/Main MKAgent/*class
move AI.jar ../../../Test_Agents
cd ../../../Test_Agents
