/*use databaseName*/

/*create database*/
CREATE DATABASE `FinalProject`;
Use FinalProject;

/*table is created to store initiator device*/
DROP TABLE IF EXISTS `mobiledevice`;
create table mobiledevice(device varchar(50) ,primary key(device));

/* This table stores Covid test result*/
DROP TABLE IF EXISTS `covidtest`;
create table covidtest(testHash varchar(50) not null, dateoftest int not null , testResult varchar(20) not null, primary key(testHash));

/*This table stores information of alret that is sent to initiator
it stores for whome alert is sent to initiator*/
DROP TABLE IF EXISTS `alertContact`;
create table alertContact(device varchar(50) not null, otherdevice varchar(50) not null,testHash varchar(50) not null,dateofcontact int not null,
FOREIGN KEY(`device`) REFERENCES `mobiledevice` (`device`),FOREIGN KEY(`testHash`) REFERENCES `covidtest` (`testHash`));

/*This table stores  information of contact between two devices*/
DROP TABLE IF EXISTS `contact`;
create table contact(device varchar(50) not null, otherdevice varchar(50) not null , dateofcontact int not null, durationofcontact int not null,
FOREIGN KEY(`device`) REFERENCES `mobiledevice` (`device`));

/*This table stores device and its testHash code*/
DROP TABLE IF EXISTS `devicetestinfo`;
create table devicetestinfo(device varchar(50) not null,testHash varchar(50) not null,FOREIGN KEY(`device`) REFERENCES `mobiledevice` (`device`),
FOREIGN KEY(`testHash`) REFERENCES `covidtest` (`testHash`));

