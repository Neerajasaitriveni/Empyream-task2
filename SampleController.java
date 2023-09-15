package application;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.lang.Math;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.DatePicker;
import javafx.stage.Stage;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import com.mongodb.MongoClient;
import com.mongodb.MongoException;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.PasswordField;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.BorderPane;

public class SampleController implements Initializable {
	private Stage stage;
	private Scene scene;
	private Parent root;
	@FXML private TextField Lname, PassName, PassAge, Sname, Fname, trainName, trainNumber, getPNR, getUsr;
	@FXML private PasswordField Lpass, Fpass, Scpass, Spass;
	@FXML private Button Llogin, ticket, Screate, Freset, confirm, bookedTicketBtn, trainDetailBtn, profileBtn, 
		availTicketsBtn, cancelTicketBtn, bookTicketbtn, yesContinue, noContinue, cancelTBtn;
	@FXML private DatePicker journeydate;
	@FXML private RadioButton Mgender, Fgender, Ogender, Lberth, Mberth, Uberth;
	@FXML private Hyperlink dontAccount, forgetPass;
	@FXML private ToggleGroup genderButtons, berthButtons;
	@FXML private ComboBox<String> source, destination;
	
	// To save information declaring it here
	UserSession user;
	String getDest, getSource, passengerName, berthPreference, gender, fillAge, pnr, tNameNo;
	LocalDate dateofjourney;
	int passengerAge, toCheckOlder;
	
	// Constructor for connecting monogdb
	public SampleController() {
		connect();
	}
	

	// To Connect with Mongodb
	MongoClient mongo;
	MongoDatabase dbconnection;
	MongoCollection<org.bson.Document> collectionSignin, passengerDetails, bookedTickets, trainDetails;
	Document Signindoc;
	
	public void connect() {
		mongo = new MongoClient("localhost", 27017);
		dbconnection = mongo.getDatabase("RailwaySystem");
		collectionSignin = dbconnection.getCollection("signin");
		passengerDetails = dbconnection.getCollection("passengerList");
		bookedTickets = dbconnection.getCollection("bookedTickets");
		trainDetails = dbconnection.getCollection("trainDetails");
	}
	
	// Login authentication method call
	public void loginMenu(ActionEvent event) throws IOException{		
		MongoDBUtil mongoDBUtil = new MongoDBUtil();
		String username = Lname.getText();
        String password = Lpass.getText();
        
        user = new UserSession(username); // For to know who booked tickets
        
        if(mongoDBUtil.authenticateUser(username, password)) {
        	actionsMenu(event);
        	
    	}
    	else {
    		Alert alert = new Alert(AlertType.WARNING);
			alert.setTitle("Error Occured");
			alert.setHeaderText(null);
			alert.setContentText("Entered username or password was incorrect! Check your login credentials before login...");
			alert.showAndWait();
			
   			switchLogin(event);
   		}	
	}
	
	// After creation of account navigate to login page
	public void switchLogin(ActionEvent event) throws IOException {
		root = (BorderPane) FXMLLoader.load(getClass().getResource("login_form.fxml"));
		stage = (Stage)((Node)event.getSource()).getScene().getWindow();
		scene = new Scene(root);
		stage.setScene(scene);
		stage.show();
	}

	// if we click create account navigate to sign up
	public void getToSignup(ActionEvent event) throws IOException{
		root = (BorderPane) FXMLLoader.load(getClass().getResource("signup.fxml"));
		stage = (Stage)((Node)event.getSource()).getScene().getWindow();
		scene = new Scene(root);
		stage.setScene(scene);
		stage.show();
	}
	
	// for creating account
	public void createAccount(ActionEvent event) throws IOException{
		
		if (Sname.getText().matches("^[a-zA-Z0-9@.-_].{6,}$")) {
	    	Signindoc = new Document("Username", Sname.getText());
	    	
	    	if(Spass.getText().matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,}$")) {
	    		
	    		if(Spass.getText() != Scpass.getText()) {
	    			
	    			Signindoc.append("Password", Scpass.getText());
	    			Alert alert = new Alert(AlertType.CONFIRMATION);
	    			alert.setTitle("Confirmation");
	    			alert.setHeaderText(null);
	    			alert.setContentText("You are account was created successfully!");
	    			alert.showAndWait();

	    		}
	    		else {
	    	    	Alert alert = new Alert(AlertType.WARNING);
	    			alert.setTitle("Validating Password");
	    			alert.setHeaderText(null);
	    			alert.setContentText("Please confirm both passwords are same!");
	    			alert.showAndWait();
	    			
	    			getToSignup(event);
	    	    }
				
			}
	    	else {
		    	Alert alert = new Alert(AlertType.WARNING);
				alert.setTitle("Validating Password");
				alert.setHeaderText(null);
				alert.setContentText("Please Provide valid Password!");
				alert.setContentText("Use Lowercase, Uppercase, Digits with the length of atleast 8 characters for password");
				alert.showAndWait();
				getToSignup(event);
		    }
	    } 
		
		else {
	    	Alert alert = new Alert(AlertType.WARNING);
			alert.setTitle("Validating Username");
			alert.setHeaderText(null);
			alert.setContentText("Please Provide valid Username!");
			alert.setContentText("Use Alphabets, Digits, `@`, `.` with 6 characters to create username");
			alert.showAndWait();
			getToSignup(event);
	    }
		
		collectionSignin.insertOne(Signindoc);
		switchLogin(event);
	}		
	
	// if we click forgot password navigate to forgot pass form
	public void getToForgetPass(ActionEvent event) throws IOException{
		root = (BorderPane) FXMLLoader.load(getClass().getResource("forgetPass.fxml"));
		stage = (Stage)((Node)event.getSource()).getScene().getWindow();
		scene = new Scene(root);
		stage.setScene(scene);
		stage.show();
	}
	
	// For changing password
	public void changePassword(ActionEvent event) throws IOException{
		String fPassword = Fpass.getText();
		
		if(fPassword.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,}$")) {
	        Document filter = new Document("Username", Fname.getText());
	        Document update = new Document("$set", new Document("Password", fPassword));
	        collectionSignin.updateOne(filter, update);
	        mongo.close();
	        
	        Alert alert = new Alert(AlertType.CONFIRMATION);
			alert.setTitle("Confirmation");
			alert.setHeaderText(null);
			alert.setContentText("Password changed successfully!");
			alert.showAndWait();
		}
		else {
			Alert alert = new Alert(AlertType.WARNING);
			alert.setTitle("Validating Password");
			alert.setHeaderText(null);
			alert.setContentText("Please Provide valid Password!");
			alert.setContentText("Use Lowercase, Uppercase, Digits with the length of atleast 8 characters for password");
			alert.showAndWait();
		}
        switchLogin(event);
	}
	
	// To show the menus
	public void actionsMenu(ActionEvent event) throws IOException {
		root = (BorderPane) FXMLLoader.load(getClass().getResource("menu.fxml"));
		stage = (Stage)((Node)event.getSource()).getScene().getWindow();
		scene = new Scene(root);
		stage.setScene(scene);
		stage.show();
	}

	// To book ticket
	public void bookTicketMenu(ActionEvent event) throws IOException {
		root = (BorderPane) FXMLLoader.load(getClass().getResource("reservation_form.fxml"));
		stage = (Stage)((Node)event.getSource()).getScene().getWindow();
		scene = new Scene(root);
		stage.setScene(scene);
		stage.show();
	}
		  
	// Reservation form gender selection method		
	public boolean genderSelect(ActionEvent event) throws IOException {
		RadioButton userGender = (RadioButton) genderButtons.getSelectedToggle();
			
	    if (userGender == null) {
	        return true;
	    }
	    gender = userGender.getText();
	    return false;
	}
	
	// Reservation form berth selection method
	public boolean berthSelect(ActionEvent event) throws IOException{
		RadioButton berth = (RadioButton) berthButtons.getSelectedToggle();
			
	    if (berth == null) {
	        return true;
        }
        berthPreference = berth.getText();
	    return false;
	}
	
	// Reservation form Source and Destination selection method
	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {		
		
		ObservableList<String> l1 = FXCollections.observableArrayList("Chennai", "Delhi", "Mumbai", "Kolkata", "Bangalore");
		ObservableList<String> l2 = FXCollections.observableArrayList("Mumbai", "Bangalore", "Kolkata", "Chennai", "Delhi");
		
		try{
			source.setItems(l1);
			destination.setItems(l2);
		}
		catch(Exception e) {
			System.out.print("");
		}
		
	}	
	public void selectSource(ActionEvent event) throws IOException{
		getSource = source.getSelectionModel().getSelectedItem().toString();
	}
	public void selectDest(ActionEvent event) throws IOException{
		getDest = destination.getSelectionModel().getSelectedItem().toString();
	}
	
	// Validates the user inputs before storing in mongodb
	private boolean validate(ActionEvent event) throws IOException {
		passengerName = PassName.getText();
		
		if(PassName.getText().isEmpty() || passengerName.length() <= 2) {
			Alert alert = new Alert(AlertType.WARNING);
			alert.setTitle("Please Provide valid Information");
			alert.setHeaderText(null);
			alert.setContentText("Please enter Passenger's Name correctly!");
			alert.showAndWait();
			
			return false;
		}		
		else if(PassAge.getText().isEmpty() || Integer.parseInt(PassAge.getText()) <= 0 || Integer.parseInt(PassAge.getText()) >= 120) {
			Alert alert = new Alert(AlertType.WARNING);
			alert.setTitle("Please Provide valid Information");
			alert.setHeaderText(null);
			alert.setContentText("Please enter Passenger age correctly!");
			alert.showAndWait();
			
			return false;
		}
		else if(genderSelect(event)) {
			Alert alert = new Alert(AlertType.WARNING);
			alert.setTitle("Please Provide valid Information");
			alert.setHeaderText(null);
			alert.setContentText("Please enter Passenger's gender!");
			alert.showAndWait();
			
			return false;
		}
		else if(berthSelect(event)) {
			Alert alert = new Alert(AlertType.WARNING);
			alert.setTitle("Please Provide valid Information");
			alert.setHeaderText(null);
			alert.setContentText("Please enter Passenger's berth Preference!");
			alert.showAndWait();
			
			return false;
		}
		else if(getSource == null) {
			Alert alert = new Alert(AlertType.WARNING);
			alert.setTitle("Please Provide valid Information");
			alert.setHeaderText(null);
			alert.setContentText("Please enter Passenger's Travel Source!");
			alert.showAndWait();
			
			return false;
		}
		else if(getDest == null) {
			Alert alert = new Alert(AlertType.WARNING);
			alert.setTitle("Please Provide valid Information");
			alert.setHeaderText(null);
			alert.setContentText("Please enter Passenger's Travel Destination!");
			alert.showAndWait();
			
			return false;
		}
		else if(getSource == getDest) {
			Alert alert = new Alert(AlertType.WARNING);
			alert.setTitle("Please Provide valid Information");
			alert.setHeaderText(null);
			alert.setContentText("Passenger's Travel Source and Destination must not be same!");
			alert.showAndWait();
			
			return false;
		}
		else if(journeydate.getEditor().getText().isEmpty()) {
			Alert alert = new Alert(AlertType.WARNING);
			alert.setTitle("Please Provide valid Information");
			alert.setHeaderText(null);
			alert.setContentText("Please enter Passenger's Journey date!");
			alert.showAndWait();
			
			return false;
		}
		
		return true;
	}
	
	// To save the info of the passenger 
	// Store data to the mongodb that got from reservation form
	Document bookTicket, bookDetails, toTName;
	String getBerth, tNumber = null;
	
	public void saveInfo(ActionEvent event) throws IOException {
		
		String tName = getSource.substring(0,3) + "-" + getDest.substring(0,3) + " Express";
		try {
			toTName = trainDetails.find(new Document("trainName", tName)).first();
			tNumber = toTName.getString("trainNumber");
			
            Document filter = trainDetails.find(new Document("trainName", tName)).first();
            
    		try {    			
    			toCheckOlder = Integer.parseInt(PassAge.getText());
    			getBerth = checkAvailability(filter, berthPreference, trainDetails, toCheckOlder, tName);
    		}
    		catch (Exception e) {
				System.out.println(e);
			}
        }
		catch (Exception e) {
			System.out.println(e);
		}
		
		if(getBerth != null && validate(event)) {
			
			pnr = String.valueOf(UserSession.getPnrNumber());
			passengerName = PassName.getText();
			fillAge = PassAge.getText();
			passengerAge = Integer.parseInt(PassAge.getText());
			dateofjourney = journeydate.getValue();
			tNameNo = tNumber + " : " + tName;
			
			docCreateToStore(tNameNo);
		}
		else if(getBerth == null) {
			Alert alert = new Alert(AlertType.WARNING);
			alert.setTitle("No tickets");
			alert.setHeaderText(null);
			alert.setContentText("Ticket for the journey wasn't available. It was sold out!");
			alert.showAndWait();
			
			actionsMenu(event);
		}
		
		confirmation(event);
	}
	
	// To store into database
	public void docCreateToStore(String tNameNo) throws IOException {
		String userName = UserSession.getUserName();
		
		bookTicket = new Document("Ticket_BookerName", userName);
		bookDetails = new Document("PNR_Number", pnr);
		bookDetails.append("Train_Name_Number", tNameNo);
		bookDetails.append("Passenger_Name", passengerName);
	    bookDetails.append("Passenger_Age", passengerAge);
	    bookDetails.append("Passenger_Gender", gender);
	    bookDetails.append("Passenger_Berth", getBerth);
	    bookDetails.append("Travel_Source", getSource);
	    bookDetails.append("Travel_Destination", getDest);
	    bookDetails.append("Passenger_JourneyDate", dateofjourney.toString());
        
		bookTicket.append("Ticket_Details",bookDetails);
		passengerDetails.insertOne(bookTicket);
		
		bookedTicketDetails(pnr, userName);
		
	}
	
	// To store the details in BookedTickets
	public void bookedTicketDetails(String pnr, String userName) throws IOException {

	    Document filter = new Document("Train Info", tNameNo);
	    Document exists = bookedTickets.find(filter).first();

	    if (exists == null) {

	        List<Document> passengers = new ArrayList<>();
	        Document passenger = new Document("PNR_Number", pnr).append("User Name", userName);
	        passengers.add(passenger);

	        Document doc = new Document("Train Info", tNameNo).append("Passengers", passengers);
	        bookedTickets.insertOne(doc);
	    } 
	    else {
	        Document passenger = new Document("PNR_Number", pnr).append("UserName", userName);
	        bookedTickets.updateOne(exists, new Document("$push", new Document("Passengers", passenger)));
	    }
	}

	
	// Confirmation message
	public void confirmation(ActionEvent event) throws IOException {
		FXMLLoader loader = new FXMLLoader(getClass().getResource("confirmation.fxml"));
		root = loader.load();
		
		Controller c = loader.getController();
		c.displayDetails(pnr, passengerName, fillAge, gender, getBerth, getSource, getDest, dateofjourney.toString());
		
		stage = (Stage)((Node)event.getSource()).getScene().getWindow();
		scene = new Scene(root);
		stage.setScene(scene);
		stage.show();
	}
	
	// To check the tickets availability before booking
	public String checkAvailability(Document filter, String berthPreference, MongoCollection<Document> trainDetails, int toCheckOlder, String tName) throws IOException{
		
		if(filter != null && checkTicketAvail(tName)) {
			return null;
		}
		
		else if(filter != null && toCheckOlder > 60 && filter.getInteger("lowerBerth") > 0) {
			berthPreference = "We arrange you Lower berth";
			int currentValue = filter.getInteger("lowerBerth");
			trainDetails.updateOne(filter, Updates.set("lowerBerth", currentValue - 1));
			return berthPreference;
		}
		
		else if(filter != null && ( 
				(berthPreference.equals("Lower") && filter.getInteger("lowerBerth") > 0) ||
				(berthPreference.equals("Middle") && filter.getInteger("middleBerth") > 0) || 
				(berthPreference.equals("Upper") && filter.getInteger("upperBerth") > 0) ) ) {
			
			if(berthPreference.equals("Lower")) {
				int currentValue = filter.getInteger("lowerBerth");
				trainDetails.updateOne(filter, Updates.set("lowerBerth", currentValue - 1));
				berthPreference = "Lower";
				return berthPreference;
			}
			else if(berthPreference.equals("Middle")) {
				int currentValue = filter.getInteger("middleBerth");
				trainDetails.updateOne(filter, Updates.set("middleBerth", currentValue - 1));
				berthPreference = "Middle";
				return berthPreference;
			}
			else if(berthPreference.equals("Upper")) {
				int currentValue = filter.getInteger("upperBerth");
				trainDetails.updateOne(filter, Updates.set("upperBerth", currentValue - 1));
				berthPreference = "Upper";
				return berthPreference;
			}
		}
		
		else if(filter.getInteger("lowerBerth") > 0) {
			int currentValue = filter.getInteger("lowerBerth");
			trainDetails.updateOne(filter, Updates.set("lowerBerth", currentValue - 1));
			berthPreference = "Lower";
			return berthPreference;
		}
		
		else if(filter.getInteger("middleBerth") > 0) {
			int currentValue = filter.getInteger("middleBerth");
			trainDetails.updateOne(filter, Updates.set("middleBerth", currentValue - 1));
			berthPreference = "Middle";
			return berthPreference;
		}
		
		else if(filter.getInteger("upperBerth") > 0) {
			int currentValue = filter.getInteger("upperBerth");
			trainDetails.updateOne(filter, Updates.set("upperBerth", currentValue - 1));
			berthPreference = "Upper";
			return berthPreference;
		}
		
		else if(filter.getInteger("racBerth") > 0) {
			int currentValue = filter.getInteger("racBerth");
			trainDetails.updateOne(filter, Updates.set("racBerth", currentValue - 1));
			berthPreference = "RAC";
			return berthPreference;
		}
		
		else if(filter.getInteger("waitingList") > 0) {
			int currentValue = filter.getInteger("waitingList");
			trainDetails.updateOne(filter, Updates.set("waitingList", currentValue - 1));
			berthPreference = "WaitingList";
			return berthPreference;
		}
		return null;
	}
	
	// Checks ticket availability
	public boolean checkTicketAvail(String tName) throws IOException {
		Document ticketsAvail = trainDetails.find(new Document("trainName", tName)).first();
		
		if(ticketsAvail != null && ticketsAvail.getInteger("lowerBerth") == 0 
								&& ticketsAvail.getInteger("middleBerth") == 0
								&& ticketsAvail.getInteger("upperBerth") == 0
								&& ticketsAvail.getInteger("racBerth") == 0
								&& ticketsAvail.getInteger("waitingList") == 0) return true;
		
		return false;
	}
	
	
	// For Cancel Ticket Form display
	public void cancelTicketMenu(ActionEvent event) throws IOException {
		root = (BorderPane) FXMLLoader.load(getClass().getResource("cancellationForm.fxml"));
		stage = (Stage)((Node)event.getSource()).getScene().getWindow();
		scene = new Scene(root);
		stage.setScene(scene);
		stage.show();
	}
	
	// To cancel ticket by getting document matched with users pnr
	public void cancelTicket(ActionEvent event) throws IOException {
		String usersPnr = getPNR.getText();
		String pname, source, destination;
		int page;
		
		try{
			Document pnrFinder = passengerDetails.find(new Document("Ticket_Details.PNR_Number", usersPnr)).first();
			
			if(pnrFinder != null) {
				
				// get the sub document of the pnrFinder document
				Document ticketDetails = pnrFinder.get("Ticket_Details", Document.class); 
				
				pname = ticketDetails.getString("Passenger_Name");
				page = ticketDetails.getInteger("Passenger_Age");
				source = ticketDetails.getString("Travel_Source");
				destination = ticketDetails.getString("Travel_Destination");
				
				Alert alert = new Alert(AlertType.CONFIRMATION);
				alert.setTitle("Confirmation for Cancel");
				alert.setHeaderText(null);
				alert.setContentText("Are you sure want to cancel ?\n" + "\n\t\tName         : " + pname + "\n\t\tAge            : " + page + "\n\t\tSource       : " + source + "\n\t\tDestination : " + destination);
				alert.showAndWait();

				cancelOperation(usersPnr);
				
				root = (BorderPane) FXMLLoader.load(getClass().getResource("menu.fxml"));
				stage = (Stage)((Node)event.getSource()).getScene().getWindow();
				scene = new Scene(root);
				stage.setScene(scene);
				stage.show();
		        
			}
			else {
				Alert alert = new Alert(AlertType.WARNING);
				alert.setTitle("Error on fetching Information");
				alert.setHeaderText(null);
				alert.setContentText("Enter Your PNR Number Correctly!");
				alert.showAndWait();
				
				cancelTicketMenu(event);
			}
    	}
		catch(Exception e) {
			System.out.print(e);
		}
	}
	
	// To check available to reassign if any cancellation occurs
	public boolean racCheckTicketIfCancel(String tName) throws IOException {
		Document ticketsAvail = trainDetails.find(new Document("trainName", tName)).first();
		
		if(ticketsAvail != null && ticketsAvail.getInteger("lowerBerth") > 0) return true;
		else if(ticketsAvail != null && ticketsAvail.getInteger("middleBerth") > 0) return true;
		else if(ticketsAvail != null && ticketsAvail.getInteger("upperBerth") > 0) return true;
		
		return false;
	}
	
	// To check available to reassign if any cancellation occurs
	public boolean wlCheckTicketIfCancel(String tName) throws IOException {
		Document ticketsAvail = trainDetails.find(new Document("trainName", tName)).first();
		
		if(ticketsAvail != null && ticketsAvail.getInteger("lowerBerth") > 0) return true;
		else if(ticketsAvail != null && ticketsAvail.getInteger("middleBerth") > 0) return true;
		else if(ticketsAvail != null && ticketsAvail.getInteger("upperBerth") > 0) return true;
		else if(ticketsAvail != null && ticketsAvail.getInteger("racBerth") > 0) return true;
		
		return false;
	}
	
	// cancel the ticket and increase seats count which was cancelled
	public void cancelOperation(String usersPnr) throws IOException {

	    String trainInfo = null;
	    MongoCursor<Document> cursor = bookedTickets.find(new Document("Passengers.PNR_Number", usersPnr)).iterator();

	    while (cursor.hasNext()) {
	        Document document = cursor.next();
	        trainInfo = document.getString("Train Info");
	    }

	    Bson query = Filters.eq("Ticket_Details.PNR_Number", usersPnr);
	    Document filter = new Document("Train Info", trainInfo);
	    Document update = new Document("$pull", new Document("Passengers", new Document("PNR_Number", usersPnr)));

	    // To increase seat numbers
	    Document result = passengerDetails.find(new Document("Ticket_Details.PNR_Number", usersPnr)).first();

	    if (result != null) {
	        if (result.containsKey("Ticket_Details") && result.get("Ticket_Details") instanceof Document) {
	            Document trainSeat = (Document) result.get("Ticket_Details");

	            if (trainSeat.containsKey("Passenger_Berth") && trainSeat.containsKey("Train_Name_Number")) {

	                String trainNo = trainSeat.getString("Train_Name_Number");
	                //String trainName = trainNo.substring(7, trainNo.length());
	                trainNo = trainNo.substring(0, 4);
	                String berth = trainSeat.getString("Passenger_Berth");

	                Document seat = trainDetails.find(new Document("trainNumber", trainNo)).first();
	                ObjectId objectId = seat.getObjectId("_id");

	                try {
	                	

	                	// Delete the passengers informations
	                    passengerDetails.deleteOne(query);
	                    bookedTickets.updateOne(filter, update);
	                    
	                		                    
	                    // Increase the count of the canceled berth
	                    if (berth.equals("Lower")) {
	                    	Document lower = new Document("_id", objectId);
	                    	Bson toupdate = Updates.inc("lowerBerth", 1);
	                    	
	                    	trainDetails.updateOne(lower, toupdate);	                    	                  	
	                    } 
	                    
	                    if (berth.equals("Middle")) {
	                    	Document middle = new Document("_id", objectId);
	                    	Bson toupdate = Updates.inc("middleBerth", 1);
	                    	
	                    	trainDetails.updateOne(middle, toupdate);
	                    } 
	                    
	                    if (berth.equals("Upper")) {
	                    	Document upper = new Document("_id", objectId);
	                    	Bson toupdate = Updates.inc("upperBerth", 1);
	                    	
	                    	trainDetails.updateOne(upper, toupdate);
	                    }
	                    
	                    if (berth.equals("RAC")) {
	                    	Document racBerth = new Document("_id", objectId);
	                    	Bson toupdate = Updates.inc("racBerth", 1);
	                    	
	                    	trainDetails.updateOne(racBerth, toupdate);
	                    }
	                    
	                    if (berth.equals("WaitingList")) {
	                    	Document wl = new Document("_id", objectId);
	                    	Bson toupdate = Updates.inc("waitingList", 1);
	                    	
	                    	trainDetails.updateOne(wl, toupdate);
	                    }
	                    
	                    
	                    // Handling RAC berth passengers
	                    // Reassigns cancelled berth to rac passenger
//	                    if(seat.getInteger("racBerth") < 10 && racCheckTicketIfCancel(trainName)) {
//	                    	Document racPassenger = passengerDetails.find(new Document("Ticket_Details.Passenger_Berth", "RAC")).first();
//	                    	
//	                    	rac(racPassenger, seat, objectId);
//	                    	
//	                    	// Handling waiting list passengers
//	                    	if(seat.getInteger("waitingList") < 10 && wlCheckTicketIfCancel(trainName)) {
//	                    		Document wl = passengerDetails.find(new Document("Ticket_Details.Passenger_Berth", "WaitingList")).first();
//	                    		
//	                    		wl(wl, seat, objectId);
//	                    	} 
//	                    	
//	                    }
			                    
	                    
	                    Alert alert = new Alert(AlertType.CONFIRMATION);
	                    alert.setTitle("Confirmation for Cancel");
	                    alert.setHeaderText(null);
	                    alert.setContentText("Your ticket was canceled successfully!");
	                    alert.showAndWait();
	                     
	                }  // try  part
	                
	                catch (MongoException e) {
	                    System.err.println(e);
	                }
	                
	            } // cancelOperation 3rd if part
	        }// cancelOperation 2nd if part
	    } // cancelOperation 1st if part
	    
	    else {
	        Alert alert = new Alert(AlertType.WARNING);
	        alert.setTitle("Error on fetching Information");
	        alert.setHeaderText(null);
	        alert.setContentText("Enter Your PNR Number Correctly!");
	        alert.showAndWait();
	    }

	}
	
	public void wl(Document wl, Document seat, ObjectId objectId) throws IOException {
		if(wl != null && seat.getInteger("lowerBerth") > 0) {
    		passengerDetails.updateOne(wl, Updates.set("Ticket_Details.Passenger_Berth", "Lower"));
    		
    		// To decrement cancelled berth count
        	Document correspondBerth = new Document("_id", objectId);
        	Bson berthUpdate = Updates.inc("lowerBerth", -1);
        	
        	trainDetails.updateOne(correspondBerth, berthUpdate);
        	
        	// To increment waitinglist count
            Document wlChange = new Document("_id", objectId);
        	Bson toupdate = Updates.inc("waitingList", 1);
        	
        	trainDetails.updateOne(wlChange, toupdate);
    	}
    	
    	else if(wl != null && seat.getInteger("middleBerth") > 0) {
    		passengerDetails.updateOne(wl, Updates.set("Ticket_Details.Passenger_Berth", "Middle"));
    		
    		// To decrement cancelled berth count
        	Document correspondBerth = new Document("_id", objectId);
        	Bson berthUpdate = Updates.inc("middleBerth", -1);
        	
        	trainDetails.updateOne(correspondBerth, berthUpdate);
        	
        	// To increment waitinglist count
            Document wlChange = new Document("_id", objectId);
        	Bson toupdate = Updates.inc("waitingList", 1);
        	
        	trainDetails.updateOne(wlChange, toupdate);
    	}
    	
    	else if(wl != null && seat.getInteger("upperBerth") > 0) {
    		passengerDetails.updateOne(wl, Updates.set("Ticket_Details.Passenger_Berth", "Upper"));
    		
    		// To decrement cancelled berth count
        	Document correspondBerth = new Document("_id", objectId);
        	Bson berthUpdate = Updates.inc("upperBerth", -1);
        	
        	trainDetails.updateOne(correspondBerth, berthUpdate);
        	
        	// To increment waitinglist count
            Document wlChange = new Document("_id", objectId);
        	Bson toupdate = Updates.inc("waitingList", 1);
        	
        	trainDetails.updateOne(wlChange, toupdate);
    	}
		
    	else if(wl != null && seat.getInteger("racBerth") > 0) {
    		passengerDetails.updateOne(wl, Updates.set("Ticket_Details.Passenger_Berth", "RAC"));
    		
    		// To decrement cancelled berth count
        	Document correspondBerth = new Document("_id", objectId);
        	Bson berthUpdate = Updates.inc("racBerth", -1);
        	
        	trainDetails.updateOne(correspondBerth, berthUpdate);
        	
        	// To increment waitinglist count
            Document wlChange = new Document("_id", objectId);
        	Bson toupdate = Updates.inc("waitingList", 1);
        	
        	trainDetails.updateOne(wlChange, toupdate);
    	}
	}
	
	public void rac(Document racPassenger, Document seat, ObjectId objectId) throws IOException {
		if(racPassenger != null && seat.getInteger("lowerBerth") > 0) {
    		passengerDetails.updateOne(racPassenger, Updates.set("Ticket_Details.Passenger_Berth", "Lower"));
    		
    		// To decrement cancelled berth count
        	Document correspondBerth = new Document("_id", objectId);
        	Bson berthUpdate = Updates.inc("lowerBerth", -1);
        	
        	trainDetails.updateOne(correspondBerth, berthUpdate);
        	
        	// To increment racberth count
            Document rac = new Document("_id", objectId);
        	Bson toupdate = Updates.inc("racBerth", 1);
        	
        	trainDetails.updateOne(rac, toupdate);
    	}
    	
    	else if(racPassenger != null && seat.getInteger("middleBerth") > 0) {
    		passengerDetails.updateOne(racPassenger, Updates.set("Ticket_Details.Passenger_Berth", "Middle"));
    		
    		// To decrement cancelled berth count
        	Document correspondBerth = new Document("_id", objectId);
        	Bson berthUpdate = Updates.inc("middleBerth", -1);
        	
        	trainDetails.updateOne(correspondBerth, berthUpdate);
        	
        	// To increment racberth count
            Document rac = new Document("_id", objectId);
        	Bson toupdate = Updates.inc("racBerth", 1);
        	
        	trainDetails.updateOne(rac, toupdate);
    	}
    	
    	else if(racPassenger != null && seat.getInteger("upperBerth") > 0) {
    		passengerDetails.updateOne(racPassenger, Updates.set("Ticket_Details.Passenger_Berth", "Upper"));
    		
    		// To decrement cancelled berth count
        	Document correspondBerth = new Document("_id", objectId);
        	Bson berthUpdate = Updates.inc("upperBerth", -1);
        	
        	trainDetails.updateOne(correspondBerth, berthUpdate);
        	
        	// To increment racberth count
            Document rac = new Document("_id", objectId);
        	Bson toupdate = Updates.inc("racBerth", 1);
        	
        	trainDetails.updateOne(rac, toupdate);
    	}
	}
	
	public void bookedTicketMenu(ActionEvent event) throws IOException {
		
	}
	public void trainDetailMenu(ActionEvent event) throws IOException {
			
	}
	public void profileMenu(ActionEvent event) throws IOException {
		
	}
	public void checkTicketMenu(ActionEvent event) throws IOException {
		
	}
		
	
}

// To authenticate the user information
class MongoDBUtil {

    public boolean authenticateUser(String username, String password) {
        try (com.mongodb.client.MongoClient mongoClient = MongoClients.create("mongodb://localhost:27017")) {
            MongoCollection<Document> usersCollection = mongoClient.getDatabase("RailwaySystem").getCollection("signin");

            Document user = usersCollection.find(new Document("Username", username)).first();
            if (user != null && user.getString("Password").equals(password)) {
                return true; 
            }
        }
        return false;
    }
}

// For get the logged in user's name for ticket bookers details
// and for generation pnr number for each user
class UserSession {
	
    private static String userName;
    static int max = Integer.MAX_VALUE, min = 2147123451;
    private static int pnrNumber = 0;
    //private static String pBerth = null, pTrain = null;

    UserSession(String userName) {    	
        UserSession.userName = userName;
    }
    
    public static int getPnrNumber() {
    	UserSession.pnrNumber = (int) (Math.random() * (max - min + 1) + min);
    	return pnrNumber;
    }

    public static String getUserName() {
        return userName;
    }
    
}
