


import java.util.Scanner;

public class mainUI {
	 
	//This method is taken from Asssignment-1 Main.java file
	private static String getEndingString(Scanner userInput ) {
		String userArgument = null;

		userArgument = userInput.nextLine();
		userArgument = userArgument.trim();


		if (userArgument.equalsIgnoreCase("empty")) {
			userArgument = "";
		} else if (userArgument.equalsIgnoreCase("null")) {
			userArgument = null;
		}

		return userArgument;
	}
	
	//main method
	public static void main(String args[]) throws Exception{
		String readFileCommand = "read";
		String recordCommand = "recordContact";
		String syncDataCommand="Sync";
		String userPosTestCommand="usertest";
		String newMobileDeviceCommand="newDevice";
		String findGatheringCommand="find";
		String testRecordCommand="testresult";
		String quitCommand="quit";
		
		String UserCommand="";
		Scanner userInput=new Scanner(System.in);
		//user option to perfrom different operations
		System.out.println("Commands available:");
		System.out.println("  " + readFileCommand + " <File name>" );
		System.out.println("  " + recordCommand);
		System.out.println("  " + userPosTestCommand+"<TestHash>");
		System.out.println("  " + syncDataCommand);
		System.out.println("  " + newMobileDeviceCommand+"<file name>");
		System.out.println("  " + findGatheringCommand);
		System.out.println("  " + testRecordCommand);
		System.out.println("  " + quitCommand);
		
		MobileDevice itemToHandle=null;
		Government dataHandle=null;
		
		while(true){
			UserCommand=userInput.next();
			String Command= getEndingString( userInput );
			//load mobile device
			if(UserCommand.equalsIgnoreCase(readFileCommand)) {
				
				
				try {
					//call constructor 
					itemToHandle=new MobileDevice(Command,new Government("config"));
					
				}
				catch(Exception e) {
					//handle exception
					e.printStackTrace();
				}
			}
			
			//record contacts with other device
			else if(UserCommand.equalsIgnoreCase(recordCommand))
			{
				try {
					String address="",deviceName="";
					int date=0,duration=0;
					System.out.println("Address:");
					address=userInput.nextLine();
					System.out.println("deviceName:");
					deviceName=userInput.nextLine();
					System.out.println("date:");
					date=userInput.nextInt();
					System.out.println("duration:");
					duration=userInput.nextInt();
					String deviceInfo="";
					if(!address.isEmpty() && !deviceName.isEmpty()) {
						deviceInfo=address+deviceName;
					}else {
						System.out.println("Invalid device config");	
						continue;
					}
					
					
					try {
						String hashOfDevice=Integer.toString(deviceInfo.hashCode());
						//record contact
						boolean result=itemToHandle.recordContact(hashOfDevice,date,duration);
						System.out.println(result);
					}catch(Exception ex) 
					{
						//exception handle
						System.out.println("invalid data");
					}
				}catch(Exception e) {
					System.out.println("enter valid data");
				}
				
			}
			//store user testHash provided by Test Agency
			else if(UserCommand.equalsIgnoreCase(userPosTestCommand)) {
				
				try {
					boolean result=itemToHandle.positiveTest(Command);
					System.out.println(result);
				}
				catch(Exception e) {
					System.out.println("enter valid data");
				}
			}
			//synchronize data
			else if(UserCommand.equalsIgnoreCase(syncDataCommand)) {
				try {
					boolean result=itemToHandle.synchronizeData();
					System.out.println(result);	
				}catch(Exception e) {
					System.out.println("Error in sync");
				}
				
				
			}
			//record test result provided by Test Agency
			else if(UserCommand.equalsIgnoreCase(testRecordCommand)) {
				try {
					String testHash="";
					int date=0;
					boolean result=false;
					System.out.println("testHash:");
					testHash=userInput.nextLine();
					System.out.println("date:");
					date=userInput.nextInt();
					System.out.println("report result:");
					result=userInput.nextBoolean();
					try {
						dataHandle=new Government("config");
						result=dataHandle.recordTestResult(testHash,date,result);
						System.out.println(result);
					}catch(Exception e) {
						System.out.println("not successfull");
					}
				}catch(Exception e) {
					
					System.out.println("enter valid data");
				}
				
			}
			//find large gathering
			else if(UserCommand.equalsIgnoreCase(findGatheringCommand)) {
				try {
					int date=0,minSize=0,minTime=0;
					float density=0;
					System.out.println("date:");
					date=userInput.nextInt();
					System.out.println("minSize:");
					minSize=userInput.nextInt();
					System.out.println("minTime:");
					minTime=userInput.nextInt();
					System.out.println("density:");	
					density=userInput.nextFloat();
					
					try {
						dataHandle=new Government("config");
						int result=dataHandle.findGatherings(date, minSize, minTime, density);
						System.out.println("result:" + result);
					}catch(Exception e) {
						e.printStackTrace();
					}
				}catch(Exception e) {
					System.out.println("enter valid data");
				}
				
			}
			//new mobile device
			else if(UserCommand.equalsIgnoreCase(newMobileDeviceCommand)) {
				try {
					//call constructor 
					itemToHandle=new MobileDevice(Command,new Government("config"));
					
				}
				catch(Exception e) {
					//handle exception
					System.out.println(e);
				}
			}
			//quit
			else if(UserCommand.equalsIgnoreCase(quitCommand)) {
				break;
			}
			else {
				System.out.println("Bad Command");
			}		
		}
	}


}
