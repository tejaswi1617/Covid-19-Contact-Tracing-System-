



import java.io.*;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;



public class MobileDevice {
	public static Map<String,List<String>> testResult=new HashMap<String,List<String>>(); //sotre testHash of current mobile device
	//store mobile device and its contacts
	public static Map<String,List<contactDeviceInfo>> mobileDeviceInfo=new HashMap<String,List<contactDeviceInfo>>();
	private String address="";//store device address
	private String deviceName="";////store device name
	private static String currentMobileDevice="";//keep track of current mobile device
	private static Government governmentObj=null; //Government class object to access That class
	private File fileXML=null; //file object to create xml file
	
	
	public MobileDevice(String configureFile, Government contactTracer) throws Exception{
		try 
		{
			//give reference of government class object
			governmentObj=contactTracer;
		}
		catch(Exception e)
		{
			throw new Exception();
		}
		
		//file is empty
		if(configureFile.isEmpty()) 
		{
			System.out.println("Invalid File Name");
			throw new Exception();
		}
		//buffere reader to read data line by line
		BufferedReader readFileInfo=null;
		try 
		{
			readFileInfo=new BufferedReader(new FileReader(configureFile));
		
			//to store read line from the file
			String line="";
			
			while((line=readFileInfo.readLine())!=null) {
				//split line and store in database
				if(line.isEmpty()) {
					continue;
				}
				String data[]=line.split("=",2);
				if(data.length!=2) {
					System.out.println("Invalid file content");
					throw new Exception();
				}
				if(data[0].compareToIgnoreCase("address")==0 && !data[1].isEmpty() ) {
					//keyword in line is address
					//store address of mobile device
					address=data[1];
				}else if(data[0].compareToIgnoreCase("deviceName")==0 && !data[1].isEmpty() ) {
					//keyword in line is device name
					//store device name
					deviceName=data[1];
				}else {
					//file contains keyword that is not valid
					System.out.println("Invalid file content");
					throw new Exception();
				}
			}
			
			//concatenate address and device name to generate hashcode of device
			if(!address.isEmpty() && !deviceName.isEmpty()) {
				String fileConfigValue=address+deviceName;
				//generate hashcode of mobile device
				
				//store as current mobile device
				currentMobileDevice=Integer.toString(fileConfigValue.hashCode());
				//if mobile device is not already stored class  
				if(mobileDeviceInfo.containsKey(currentMobileDevice)==false || testResult.containsKey(currentMobileDevice)==false) {
					//store mobile device as key in map
					mobileDeviceInfo.put(currentMobileDevice, new ArrayList<contactDeviceInfo>());
					//store mobile device as key in map
					testResult.put(currentMobileDevice, new ArrayList<String>());
				}
			}else {
				//file contains invalid data
				System.out.println("Invalid file content");
				
			}
		}catch(Exception e) {
			System.out.println("File Not Found");
			throw new Exception();
		}

	}
	
	
	public boolean recordContact( String individual, int date, int duration ) {
		//validate input data
		if(individual.isEmpty() || date<=0 || duration<=0 ||  duration>1140 ||individual.compareTo(currentMobileDevice)==0) {
			System.out.println("Invalid input data");
			return false;
		}
		//store another device hash,date and duration of contact in contactDeviceInfo object
		String otherDevice=individual.trim();
		contactDeviceInfo obj=new contactDeviceInfo(otherDevice,date,duration);
		
		//store contact information as value of current mobile device
		mobileDeviceInfo.get(currentMobileDevice).add(obj);
		return true;
	}
	
	public boolean positiveTest( String testHash ) {
		
		//validate testHash
		if(testHash.isEmpty()) {
			System.out.println("Invalid input data");
		}
		int flag=0;
		if(testHash.matches("^(?=.*[a-zA-Z])(?=.*[0-9])[A-Za-z0-9]+$")){
			//testHash is in the form of alphanumeric
			//store as value of current mobile device
			//check whther current testHash is stored 
			for(int i=0;i<testResult.size();i++) {
				if(testResult.get(currentMobileDevice).contains(testHash)) {
					flag=1;
				}
			}
			//testHash is not stored before
			if(flag==0) {
				//store testHash
				testResult.get(currentMobileDevice).add(testHash);
				return true;
			}
			else
			{
				//testHash has been already recorded(deplicate record)
				return false;
			}
				
		}
		else 
		{
			//testHash is not in the form of alphanumeric
			return false;
		}
	}
	
	
	public boolean synchronizeData( ) {
			
		//method call to store contacts information in xml formate
		String xmlString=xmlFormate(currentMobileDevice);
		//call Government class method to store it in database
		try {
			boolean result=governmentObj.mobileContact(currentMobileDevice,xmlString);
			
			//return true if mobileContact return true
			//initiatior has been in contact with covid-19 positive
			if(result==true) {
				//clear locally stored data
				mobileDeviceInfo.get(currentMobileDevice).clear();
				testResult.get(currentMobileDevice).clear();
				return true;
			}else {
				//return true if mobileContact return true
				//initiatior has been not in contact with covid-19 positive
				//clear locally stored data
				mobileDeviceInfo.get(currentMobileDevice).clear();
				testResult.get(currentMobileDevice).clear();
				return false;
			}
				
		}catch(Exception e) {
			System.out.println("Failed to load data in database");
			return false;
		}
				
	}
	
	private String xmlFormate(String currentDevice) {
		
		String xmlString="";
		//represents XML version
		xmlString=xmlString+"<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
		//list of all contacts,date and duration of contacts
		xmlString=xmlString+"\n"+"<details>";
		
		
		//write list of all contacts(otherdevice,date and duration) with current mobile device in xml format 
		xmlString=xmlString+"\n\t"+"<otherContacts_list>";
		for(int j=0;j<mobileDeviceInfo.get(currentDevice).size();j++) {
			xmlString=xmlString+"\n\t\t"+"<contactInfo>";
			xmlString=xmlString+"\n\t\t\t"+"<Device>"+mobileDeviceInfo.get(currentDevice).get(j).individual+"</Device>";
			xmlString=xmlString+"\n\t\t\t"+"<Date>"+mobileDeviceInfo.get(currentDevice).get(j).date+"</Date>";
			xmlString=xmlString+"\n\t\t\t"+"<Duration>"+mobileDeviceInfo.get(currentDevice).get(j).duration+"</Duration>";
			xmlString=xmlString+"\n\t\t"+"</contactInfo>";
		}
		xmlString=xmlString+"\n\t"+"</otherContacts_list>";
			
		//write list of all testHash for current mobile device in xml format
		xmlString=xmlString+"\n\t"+"<deviceTest>";
		for(int j=0;j<testResult.get(currentDevice).size();j++) {
			xmlString=xmlString+"\n\t\t"+"<test>";
			xmlString=xmlString+"\n\t\t\t"+"<DeviceTeshHash>"+testResult.get(currentDevice).get(j)+"</DeviceTeshHash>" ;
			xmlString=xmlString+"\n\t\t"+"</test>";
		}		
		xmlString=xmlString+"\n\t"+"</deviceTest>";
		xmlString=xmlString+"\n"+"</details>";
		 
			
		//return String formated in XML formate
		return xmlString;
	}
	
	
}
		




