package application;

import java.io.IOException;
import org.bson.Document;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class Controller{
	private Stage stage;
	private Scene scene;
	private Parent root;
	@FXML Label CpnrNumber, CtrainName, CpassName, CpassAge, CpassGender, CpassBerth, CpassSource, CpassDest, CpassDate;
	@FXML Button wantContinue;
	String requiredTainName, requiredTrainNo, pberth;
	
	// To display details of the user in confirmation form
	public void displayDetails(String pnr, String name, String age, String gender, String berth, String source, String destination, String date) throws IOException {
		
		CpnrNumber.setText(pnr);
		CpassName.setText(name);
		CpassAge.setText(age + " years");
		if(berth.equals("WaitingList")) {
			CpassBerth.setText("You are in waiting list");
		}
		else if(Integer.parseInt(age) > 60) {
			CpassBerth.setText(berth);
		}
		else {
			CpassBerth.setText(berth + " Berth Given");
		}
		pberth = berth;
		CpassGender.setText(gender);
		CpassSource.setText(source);
		CpassDest.setText(destination);
		CpassDate.setText(date);
		
		requiredTainName = source.substring(0,3) + "-" + destination.substring(0,3) + " Express";
		final String connectionString = "mongodb://localhost:27017";
		Document user;
		
		
		try (com.mongodb.client.MongoClient mongoClient = MongoClients.create(connectionString)) {
			MongoCollection<Document> usersCollection = mongoClient.getDatabase("RailwaySystem").getCollection("trainDetails");
            user = usersCollection.find(new Document("trainName", requiredTainName)).first();
            requiredTrainNo = user.getString("trainNumber");
        }
		
		CtrainName.setText(requiredTrainNo + " : " + requiredTainName);
	}
	
	
	// Want to continue method declaration
	public void wantToContinue(ActionEvent event) throws IOException {
		root = (BorderPane) FXMLLoader.load(getClass().getResource("continue.fxml"));
		stage = (Stage)((Node)event.getSource()).getScene().getWindow();
		scene = new Scene(root);
		stage.setScene(scene);
		stage.show();
	}
}