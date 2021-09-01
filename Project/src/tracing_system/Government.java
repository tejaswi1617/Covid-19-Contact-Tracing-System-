import java.io.*;
import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.regex.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xml.sax.InputSource;


public class Government {
	private String database=""; //domain name of database
	private String user=""; //user name of database
	private String password=""; //password to access database
	private Connection connect = null; //to connect with database
	private Statement statement = null; //statement interface to execute SQL query
	private ResultSet resultSet= null; //tostore result of executed query
	private SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy"); //format of date
	private Calendar calobj = Calendar.getInstance(); //calendar object 
	private Date firstDate=null;//to store starting date to count days
	private Date secondDate=null;//to store current system date for finding days from starting date
	//private int currentday=0; // to store current day
	private  long todayDay=0;  // to store current day
	
	public Government(String configurationFile) throws Exception{
		
		
		if(configurationFile.isEmpty()) {
			//file is empty
			System.out.println("file is empty");
			throw new Exception();
		}
		
		//file object to get file
		File file=null;
		FileReader fileReader=null;
		BufferedReader readFileInfo=null;
		String filePath="";
		try {
			file=new File(configurationFile);
			//store path of the configurationFile
			filePath=file.getPath();
			//fileReader to read configurationFile
			fileReader = new FileReader(filePath);
			//buffer reader to read data of file line by line
			readFileInfo=new BufferedReader(fileReader);
		 	
			//string to store read line
			String line="";
			//read file till end of the file
			while((line=readFileInfo.readLine())!=null) {
				
				//blank line between two configuration values in file
				if(line.isEmpty()) {
					continue;
				}
				//split line and store in array of string
				String data[]=line.split("=",2);
				if(data.length!=2) {
					System.out.println("Invalid data");
					throw new Exception();
				}
				if(data[0].compareToIgnoreCase("database")==0 && !data[1].isEmpty() ) {
					//line contains domain of database
					//store domain of database
					database=data[1];
				}
				//represents line contains domain of database
				else if(data[0].compareToIgnoreCase("user")==0 && !data[1].isEmpty()) {
					//line contains user name of database
					//store suer name of database
					user=data[1];
				}else if(data[0].compareToIgnoreCase("password")==0 && !data[1].isEmpty()) {
					//line contains password of database
					//store password of user
					password=data[1];
				}
				else {
					//if any invalid data come except database,user or and password as keyword
					System.out.println("Invalid input data");
					throw new Exception();
				}
			}
			//one of the configuration value is not exist in file
			if(database.isEmpty() || user.isEmpty() || password.isEmpty()) {
				System.out.println("Invalid input data");
			}
		}
		catch(Exception e) {
			System.out.println("Invalid File");
			throw new Exception();
		} 
	}
	
	boolean mobileContact( String initiator, String contactInfo ) throws Exception {
		if(initiator.equals("") || contactInfo.equals("")|| initiator==null || contactInfo==null) {
			System.out.println("Invalid values");
			return false;
		}
		boolean resultAlert=false;//variable to return to synchronizeData method
		//Keep track of current connected otherdevices with mobile device
		ArrayList<String> contactedDevice_list=new ArrayList<String>();
		
		//logic to find current day(day when user connected to server) since January 1, 2020
		firstDate =sdf.parse("01/01/2020");
		secondDate=sdf.parse(sdf.format(calobj.getTime()));
		long diffInMillies = Math.abs(secondDate.getTime() - firstDate.getTime());
		todayDay = TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS);
	    
	    
	    //establish database connection
	    Class.forName("com.mysql.jdbc.Driver");
		connect = DriverManager.getConnection(database, user, password);
		statement = connect.createStatement();
		
		String otherdevice=null;//to store other connected device hashcode
		int date=0;//store contact date
		int duration=0;//duration of contact
		
		//Parser that produces DOM object trees from XML content  
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();  
		 //API to obtain DOM Document instance  and Create DocumentBuilder with default configuration
		DocumentBuilder db = dbf.newDocumentBuilder();  
		 //Parse the content to Document object
		Document doc = db.parse(new InputSource(new StringReader(contactInfo)));  
		doc.getDocumentElement().normalize();  
		//get all the elements tag inside given tag name
		NodeList nodeList = doc.getElementsByTagName("contactInfo");  
		// nodeList is not iterate, so we are using for loop  
		for (int itr = 0; itr < nodeList.getLength(); itr++)   
		{
			//get nodeName
			Node node = nodeList.item(itr);  
			
			if (node.getNodeType() == Node.ELEMENT_NODE)   
			{  
				//current node has elements
				Element eElement = (Element) node;  
				
				//store device,date and duration element's value to variable of current element
				otherdevice=eElement.getElementsByTagName("Device").item(0).getTextContent();
				contactedDevice_list.add(otherdevice);
				date=Integer.parseInt(eElement.getElementsByTagName("Date").item(0).getTextContent());
				duration=Integer.parseInt( eElement.getElementsByTagName("Duration").item(0).getTextContent());
				
				//call method to record initiators contacts with other mobile device
				boolean result=SQLcontact(initiator,otherdevice,date,duration);
				if(result==false) {
					System.out.println("Fail to load contacts in Databse");
				}
			}  
		}
		
		//get elements inside test tag
		nodeList = doc.getElementsByTagName("test");  
		String testHash="";
		for (int itr = 0; itr < nodeList.getLength(); itr++)   
		{  
			//get node name
			Node node = nodeList.item(itr);  
			
			if (node.getNodeType() == Node.ELEMENT_NODE)   
			{  
				//current node tag has value
				Element eElement = (Element) node;  
				//store value to local variable
				testHash=eElement.getElementsByTagName("DeviceTeshHash").item(0).getTextContent();
				
				//call method to store initiator testhashes in database
				boolean result=SQLCovidTest(initiator,testHash);
				if(result==false) {
					System.out.println("Fail to load testHash in Databse");
				}
			}  
		}
			
		//check for covid positive person who had been in contact with last 14 days
	     resultAlert=findPositiveContact(initiator,contactedDevice_list);
		
	    //close database connection
		connect.close();
		statement.close();
		
		//return result to synchronizeData method
		return resultAlert;
		
	}

	
	

	private boolean SQLcontact(String initiator, String otherdevice, int date, int duration) {
		
		try
		{
			//check initiator exist as it work as forieng key in contact table 
			resultSet= statement.executeQuery("select * from mobiledevice where device= '"+initiator+"';");
			if(!resultSet.next()) {
				//record initiator device in mobiledevice table
				statement.executeUpdate("insert into mobiledevice values('"+initiator+"'); ");
				//record initiatore contact details with other device in contact table
				statement.executeUpdate("insert into contact values('"+initiator+"','"+otherdevice+"', '"+date+"','"+duration+"'); ");
				
			}else {
				//if initiatore comes in contact with other device exist for particular day and again comes in contact with same device at a same day
				//add previous contact duration with contact current duration and update that row with new added duration
				int totalDuration=0,flag=0;
				
				resultSet= statement.executeQuery("select durationofcontact from contact where device= '"+initiator+"' and "
						+ "otherdevice='"+otherdevice+"' and dateofcontact='"+date+"'; ");
				if(resultSet.next()) {
						flag=1;
						//add new duration with old duration
						totalDuration=resultSet.getInt("durationofcontact")+duration;
						//update new duration in table
						statement.executeUpdate("update contact set durationofcontact='"+totalDuration+"' where device='"+initiator+"' "
								+ "and otherdevice='"+otherdevice+"' and dateofcontact='"+date+"'; ");			
				}
				//if initiator comes with in contact with other device first time in day then add as new record to table
				if(flag==0) {
					statement.executeUpdate("insert into contact values('"+initiator+"','"+otherdevice+"', '"+date+"','"+duration+"'); ");
				}
			}	
			return true;
		}
		catch(Exception e) 
		{
			//error while storing data to table
			System.out.println("Failed to record contact");
			return false;
		}
		
	}

	private boolean SQLCovidTest(String initiator, String testHash) {
		// TODO Auto-generated method stub
		try 
		{
			//check whether its duplicate entry or other user already has current testHash
			//one testHash has must has only one user
			resultSet=statement.executeQuery("select * from devicetestinfo where testHash= '"+testHash+"';");
			if(resultSet.next()) 
			{
				return false;
			}
			
			//Match testHash with test result stored by Government
			resultSet=statement.executeQuery("select * from covidtest where testHash= '"+testHash+"';");
			if(!resultSet.next()) 
			{
				return false;
			}
			
			//check initiator exist as it work as forieng key in devicetestinfo table which stores device hashcode and it's testHash
			resultSet= statement.executeQuery("select * from mobiledevice where device= '"+initiator+"';");
			if(!resultSet.next()) 
			{
				//record initiator device in mobiledevice table
				statement.executeUpdate("insert into mobiledevice values('"+initiator+"'); ");
				//record device(initiator) and it's test hash in the devicetestinfo table
				statement.executeUpdate("insert into devicetestinfo values('"+initiator+"','"+testHash+"'); ");
			}
			else 
			{
				//record device(initiator) and it's test hash in database
				statement.executeUpdate("insert into devicetestinfo values('"+initiator+"','"+testHash+"'); ");
			}
			//recorded data in table successfully
			return true;
		}
		catch(Exception e) 
		{
			//error while storing data to table
			System.out.println("Failed to record testHash");
			return false;
		}
		
	}
	
	private boolean findPositiveContact(String initiator, ArrayList<String> contacts_list) {

		try 
		{
			int flag=0;//alert user that he has been in contact with someone covid positive
			//to execute query to retrive contacts of last 14 days
			Statement st1 = connect.createStatement();
			//last 14th day from today
			long dayBefore14Days=todayDay-14;
			//retrive all the contacts who has been in contact with last 14 days
			resultSet=st1.executeQuery("select otherdevice,dateofcontact from contact where device= '"+initiator+"' "
					+ "and dateofcontact>='"+dayBefore14Days+"'; ");
			
			while(resultSet.next()) 
			{
					//Store other contacted device			
					String otherdevice=resultSet.getString("otherdevice");
					//store date of contact
					int contactDay=resultSet.getInt("dateofcontact");
					
					//find all the testHash and date of test taken for current otherdevice 
					//only if result of subtraction of dateofcontact and dateoftest is between -13 and 13 
					ResultSet resultSet1=statement.executeQuery("select dateoftest,testHash from covidtest where testResult=true and"
							+ " testHash in (select testHash from devicetestinfo where "
							+ "device='"+otherdevice+"') and '"+contactDay+"'-dateoftest between -13 and 13 and testResult=1;");
					
					while(resultSet1.next()) 
					{
						
							String othercontact=resultSet.getString("otherdevice");
							String testHash=resultSet1.getString("testHash");
							int dateoftest=resultSet1.getInt("dateoftest");
							
							Statement st2 = connect.createStatement();
							//query to check whether alert for current otherdevice is already sent to initiatior
							ResultSet resultSet2=st2.executeQuery("select * from alertContact where device='"+initiator+"' "
									+ "and otherdevice='"+othercontact+"'"
									+ "and testHash='"+testHash+"' and dateofcontact='"+dateoftest+"';");
							
							if(resultSet2.next()==false) 
							{
								//send alert to initiator for current other mobile device 
								flag=1;
								//record entry that alert is send to initiator for current mobile device for given testHash and dateoftest
								st2.executeUpdate("insert into alertContact values('"+initiator+"','"+otherdevice+"',"
										+ " '"+testHash+"','"+dateoftest+"'); ");
							}
							else if(contacts_list.contains(othercontact))
							{
								//send alet again for same testHash of other device if it comes in contact with inititator again
								flag=1;
							}						
					}	
				}
							
			if(flag==1)
			{
				//initiator had been contact with someone who test positive for covid within 14 days
				return true;
			}else 
			{
				//initiator had not been contact with any Covid positivewithin 14 days
				return false;	
			}
			
		}catch(Exception e) 
		{
			//Error
			System.out.println("Failed to alert initiator");
			return false;
			
		}
		
	}

	
	public boolean recordTestResult(String testHash, int date, boolean result) {
		//input validation
		if(testHash.isEmpty() || date<=0) 
		{
			System.out.println("Invalid input data");
			return false;
		}
		
		//check whther testHash is in the form of alphanumeric string or not
		if(testHash.matches("^(?=.*[a-zA-Z])(?=.*[0-9])[A-Za-z0-9]+$"))
		{
			//establish database connection
			try
			{
				Class.forName("com.mysql.jdbc.Driver");
				connect = DriverManager.getConnection(database, user, password);
				statement = connect.createStatement();
				//check whether current test result is already exists in database 
				//table only accept unique testHash
				resultSet=statement.executeQuery("select * from covidtest where testHash= '"+testHash+"'");
				if(!resultSet.next()) {
					//add test result in covidtest table
					statement.executeUpdate("insert into covidtest values('"+testHash+"','"+date+"', "+result+"); ");
					//close database connection
					connect.close();
					//return true - test is recorded in the table successfully
					return true;
				}else {
					
					//close database connection
					connect.close();
					//return false if current testHash is already stores in table
					return false;
				}
			}
			catch(Exception e) 
			{
				//error
				System.out.println("Error: Failed to record result in table");
				//return false- test is not recorded in the table successfully
				return false;
			}		
		}
		else
		{
			//return false- test is not recorded in the table successfully
			return false;
		}
	}

	int findGatherings( int date, int minSize, int minTime, float density ) {
		
		int countPair=0;//count number of large gathering
		//input validation
		if(date<1 || minSize<2 ||minTime<1 || minTime > 1140 || density<=0) {
			System.out.println("Invalid input data");
			return countPair;
		}
		
		Map<String,List<String>> connectedPairs=new HashMap<String,List<String>>(); //work as adjacency list and store all the connected devices 
		ArrayList<String> totalDevice=new ArrayList<String>();//store individual devices to find pairs of two devices
		//Store pairs of devices whose density is greater then given user density
		Map<ArrayList<String>, Float> densityOfPair=new HashMap<ArrayList<String>,Float>();
		
		try {
			//database connection
			Class.forName("com.mysql.jdbc.Driver");
			connect = DriverManager.getConnection(database, user, password);
			statement = connect.createStatement();
			
			//retrieve all the contacts from contact table that come in contact on given date and for given minTime
			resultSet=statement.executeQuery("select device,otherdevice from contact where dateofcontact='"+date+"' and "
					+ "durationofcontact>='"+minTime+"';");
		
			//Store devices's pair in adjacency list  
			while(resultSet.next()) {
				
				String device=resultSet.getString("device");
				String otherdevice=resultSet.getString("otherdevice");
				//store device
				if(!totalDevice.contains(device)) {
					totalDevice.add(device);
				}
				if(!totalDevice.contains(otherdevice)) {
					totalDevice.add(otherdevice);
				}
				
				//store device and its connection with otherdevice in HashMap
				if(!connectedPairs.containsKey(device)){
					connectedPairs.put(device, new ArrayList<String>());
					connectedPairs.get(device).add(otherdevice);
				}else {
					if(!connectedPairs.get(device).contains(otherdevice)) {
						connectedPairs.get(device).add(otherdevice);
					}
				}
				
				//visa-a-versa store otherdevice and its connection with device in HashMap
				//Example A-B, B-A
				if(!connectedPairs.containsKey(otherdevice)){
					connectedPairs.put(otherdevice, new ArrayList<String>());
					connectedPairs.get(otherdevice).add(device);
				}else {
					if(!connectedPairs.get(otherdevice).contains(device)) {
						connectedPairs.get(otherdevice).add(device);
					}
				}
			}
		}
		catch(Exception e) {
			//server side error
			System.out.println("error in DB");
			return countPair;
		}
	
		int totalElements=totalDevice.size();
		//store set of connected devices for each pair of devices	
		Set<ArrayList<String>> setsofpairs=new HashSet<ArrayList<String>>();
		
		
		for(int i=0;i<totalDevice.size();i++) {
			for(int j=i+1;j<totalDevice.size();j++) {
					
					ArrayList<String> people=new ArrayList<String>();
					//store pair of devices example: A-B 
					people.add(totalDevice.get(i));
					people.add(totalDevice.get(j));
					
					for(int k=0;k<totalDevice.size();k++) {
						//find devices which is connected with both devices A and B(find neighbor)
						if(!people.contains(totalDevice.get(k))) {
							//add element in set if it exists in both devices's list
							if(connectedPairs.get(totalDevice.get(i)).contains(totalDevice.get(k)) &&
							      connectedPairs.get(totalDevice.get(j)).contains(totalDevice.get(k))) {
								people.add(totalDevice.get(k));
								
							}
						}
					}
					//sort ArrayList(set)
					Collections.sort(people);
					
					//consider a set if it has atleast minZise people
					//sencond condition in if is for removing duplicate sets
					if(people.size()>=minSize && setsofpairs.contains(people)==false) {
						//add a set for later use to remove subsets
						setsofpairs.add(people);
						//find maximum possible connection for current set
						float max=(people.size()*(people.size()-1))/2;
						//find out actuall connection exists between elemets of current set
						float connect=findConnection(people,connectedPairs);
						//find out result 				
						float result=connect/max;
						if(result>density) {
							//cuurent pair density is grater than user given density
							int index=0;
							//logic to remove subset from the result
							for(ArrayList<String> subset:setsofpairs) {
								
								//compare current set with all the stored sets for each pair
								if(people.containsAll(subset) && people.equals(subset)==false) {
									//if current pair's subset exist in subset and that subset is considered for large gathering
									//remove subset and store current set
									if(densityOfPair.containsKey(subset)) {
										densityOfPair.remove(subset);
										densityOfPair.put(people,result);
									}else {
										//current pair's subset is not counted for large gathering then add current set
										densityOfPair.put(people,result);
									}
								}
								else if(subset.containsAll(people) && people.equals(subset)==false) {
									//current set is a subset of other stored set in subset
									if(densityOfPair.containsKey(subset)) {
										//remove subset if counted for large gathering
										if(densityOfPair.containsKey(people)) {
											densityOfPair.remove(people);
										}
										break;
									}else {
										//if superset of current subset is not counted for large gathering then count subset for large gathering
										densityOfPair.put(people,result);
									}
									
								}else {
									//if new set count it for large gathering
									densityOfPair.put(people,result);
								}						
							}
						}
					}
			}
		}
		//densityOfPair stores valid large gathering
		countPair=densityOfPair.size();
		//return number of large gathering
		return countPair;
	}

	private int findConnection(ArrayList<String> people, Map<String, List<String>> connectedPairs) {
		//find all possible connection  for current set
		int connection=0;
		for(int i=0;i<people.size();i++) {
			for(int j=i+1;j<people.size();j++) {
				if(connectedPairs.get(people.get(i)).contains(people.get(j))) {
					//count connection if ith elemets has jth elements in his list
					connection++;
				}
			}
		}
		//return number of total connection
		return connection++;
	}
}

