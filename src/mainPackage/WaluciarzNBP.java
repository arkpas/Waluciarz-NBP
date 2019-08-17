
package mainPackage;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.UnknownHostException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class WaluciarzNBP extends Application {
	
	public static final double HEIGHT = 500;
	public static final double WIDTH = 500;
	
	private ComboBox<String> currencies;
	private Button applyButton;
	private Button clearButton;
	private DatePicker datePicker;
	private DatePicker toDatePicker;
	private RadioButton toDateCheckbox;
	private TextField errorDisplay;
	private ObservableList<Currency> currentCurrencies;
	private TableView<Currency> table;
	private String[] currenciesArr = {
			"Wszystkie",
			"THB",
			"USD",
			"AUD",
			"HKD",
			"CAD",
			"NZD",
			"SGD",
			"EUR",
			"HUF",
			"CHF",
			"GBP",
			"UAH",
			"JPY",
			"CZK",
			"DKK",
			"ISK",
			"NOK",
			"SEK",
			"HRK",
			"RON",
			"BGN",
			"TRY",
			"ILS",
			"CLP",
			"PHP",
			"MXN",
			"ZAR",
			"BRL",
			"MYR",
			"RUB",
			"IDR",
			"INR",
			"KRW",
			"CNY",
			"XDR"
	};
	
	public static void main (String[] args) {
		launch(args);
	}
	
	public void start (Stage primaryStage) {
		
		BorderPane mainPane = new BorderPane();
		
		VBox displayBox = new VBox();
		displayBox.setFillWidth(false);
		mainPane.setLeft(displayBox);
		
		currentCurrencies = FXCollections.observableArrayList();
		table = getTableView();
		displayBox.getChildren().add(table);
		
		VBox bottomPanel = new VBox();
		bottomPanel.setPrefSize(400, 70);
		mainPane.setBottom(bottomPanel);
		
		HBox userInputPanel = new HBox();
		userInputPanel.setPrefSize(500, 70);
		userInputPanel.setPadding(new Insets(10));
		
		HBox preferencesBox = new HBox(8);
		preferencesBox.setPrefSize(400, 70);
		preferencesBox.setAlignment(Pos.CENTER_LEFT);
		
		currencies = new ComboBox<>();
		Arrays.sort(currenciesArr);
		currencies.getItems().addAll(currenciesArr);
		currencies.setValue("Wszystkie");
		
		Text fromText = new Text("Od:");
		
		datePicker = new DatePicker();
		datePicker.setPrefWidth(100);
		datePicker.setValue(LocalDate.now());
		datePicker.setOnKeyPressed(event -> {
			if (event.getCode().equals(KeyCode.ENTER))
				applyButton.fire();
			
		});
		
		toDateCheckbox = new RadioButton("Do:");
		toDateCheckbox.pressedProperty().addListener(listener -> {
			if(toDateCheckbox.isPressed()) {
				toDatePicker.setDisable(!toDatePicker.isDisabled());
			}
		});
		
		toDatePicker = new DatePicker();
		toDatePicker.setPrefWidth(100);
		toDatePicker.setDisable(true);
		toDatePicker.setValue(LocalDate.now());
		toDatePicker.setOnKeyPressed(event -> {
			if (event.getCode().equals(KeyCode.ENTER))
				applyButton.fire();
			
		});
		
		preferencesBox.getChildren().addAll(currencies, fromText, datePicker, toDateCheckbox, toDatePicker);
		
		VBox applyBox = new VBox(3);
		applyBox.setPrefWidth(100);
		applyBox.setAlignment(Pos.CENTER_RIGHT);
		
		EventHandler<ActionEvent> applyPress = event -> {
			errorDisplay.setText("Brak b³êdów");
			String code = currencies.getValue();
			LocalDate date = datePicker.getValue();
			LocalDate toDate = toDatePicker.getValue();
			List<Currency> tempCurrList = null;
			
			if (toDatePicker.isDisabled())
				toDate = date;
			else if (code.equals("Wszystkie")) {
				errorDisplay.setText("W przypadku zakresu dat nale¿y wybraæ konkretn¹ walutê.");
				code = currencies.getItems().get(0);
				currencies.setValue(code);
			}
			
			if (checkDates(date, toDate)) {	
				if (code.equals("Wszystkie"))
					tempCurrList = getCurrencies(date);
				else 
					tempCurrList = getCurrencies(date, toDate, code);
			}
		
			if (tempCurrList != null)
				currentCurrencies.addAll(tempCurrList);
			
			table.scrollTo(currentCurrencies.size()-1);
		}; 
		
		applyButton = new Button("PotwierdŸ");
		applyButton.setOnAction(applyPress);
		clearButton = new Button("Wyczyœæ");
		clearButton.setOnAction(event -> { 
			currentCurrencies = FXCollections.observableArrayList();
			table.setItems(currentCurrencies);
			
		});
		applyBox.getChildren().addAll(applyButton, clearButton);
		
		userInputPanel.getChildren().addAll(preferencesBox, applyBox);
		
		HBox errorDisplayBox = new HBox();
		errorDisplayBox.setPrefSize(400, 30);
		errorDisplayBox.setAlignment(Pos.CENTER);
		
		errorDisplay = new TextField("Brak b³êdów");
		errorDisplay.setPrefSize(350, 10);
		errorDisplay.setEditable(false);
		errorDisplayBox.getChildren().add(errorDisplay);
		
		
		bottomPanel.getChildren().addAll(userInputPanel, errorDisplayBox);
		
		try {
			Image icon;
			icon = new Image(new FileInputStream("src/mainPackage/dollar.png"));
			primaryStage.getIcons().add(icon);
			icon = new Image(new FileInputStream("src/mainPackage/dollar2.png"));
			primaryStage.getIcons().add(icon);
		}
		catch (FileNotFoundException e) {System.out.println("File not found");}
	
		Scene mainScene = new Scene(mainPane, HEIGHT, WIDTH);
		primaryStage.setScene(mainScene);
		primaryStage.setTitle("Waluciarz NBP");
		primaryStage.setResizable(false);
		primaryStage.setOnCloseRequest(event -> Platform.exit());
		primaryStage.show();
		
	}
	
	private List<Currency> getCurrencies(LocalDate date) {
		
		List<Currency> currencyList = null;
		String urlAdress = "http://api.nbp.pl/api/exchangerates/tables/a/" + date.toString() + "?format=json";
		String jsonString = null;
		JSONObject jsonObj = null;
		JSONArray jsonArr = null;
		
		if (!connectToURL(urlAdress))
			return null;

		try (InputStream input = new URL(urlAdress).openStream();
				BufferedReader reader = new BufferedReader(new InputStreamReader(input, "utf-8"));)
		{
			jsonString = readAll(reader);
			jsonObj = new JSONArray(jsonString).getJSONObject(0);
			jsonArr = jsonObj.getJSONArray("rates");
			currencyList = new ArrayList<>();
			for (int i = 0; i < jsonArr.length(); i++) {
				JSONObject tempJsonObj = jsonArr.getJSONObject(i);
				String name = tempJsonObj.optString("currency", tempJsonObj.optString("country"));
				String code = tempJsonObj.getString("code");
				double midValue = tempJsonObj.getDouble("mid");
				currencyList.add(new Currency(name, code, date, midValue));
			}	
		}
		catch (IOException e) {System.out.println(e.toString());}
		catch (JSONException e) {System.out.println("Getting data from JSONObject failed");}
		
		return currencyList;
	}
	
	private List<Currency> getCurrencies (LocalDate from, LocalDate to, String code) {
		
		if (code == null)
			return null;
		
		List<Currency> currencyList = null;
		String urlAdress = "http://api.nbp.pl/api/exchangerates/rates/a/" + code + "/" + from + "/" + to + "/?format=json";
		String jsonString = null;
		JSONObject jsonObj = null;
		JSONArray jsonArr = null;
		
		if (!connectToURL(urlAdress))
			return null;
		
		try (	InputStream input = new URL(urlAdress).openStream();
				BufferedReader reader = new BufferedReader(new InputStreamReader(input, "UTF-8"));) 
		{
			jsonString = readAll(reader);
			jsonObj = new JSONObject(jsonString);
			jsonArr = jsonObj.getJSONArray("rates");
			
			currencyList = new ArrayList<>();
			String name = jsonObj.optString("currency", jsonObj.optString("country"));
			
			for (int i = 0; i < jsonArr.length(); i++) {
				JSONObject tempJsonObj = jsonArr.getJSONObject(i);
				LocalDate date = getLocalDate(tempJsonObj.getString("effectiveDate"));
				double midValue = tempJsonObj.getDouble("mid");
				currencyList.add(new Currency(name, code, date, midValue));
			}
		}
		catch (IOException e) {System.out.println(e.getLocalizedMessage()); e.printStackTrace(); }
		catch (JSONException e) {System.out.println("Getting data from JSONObject failed"); e.printStackTrace(); }
		
		return currencyList;
	}
	
	private String readAll(BufferedReader reader) throws IOException {
		
		StringBuilder strBuilder = new StringBuilder();
		String input = "";
		while ((input = reader.readLine()) != null) {
			strBuilder.append(input);
		}
		
		return strBuilder.toString();
	}
	
	private LocalDate getLocalDate (String input) {
		
		LocalDate date = null;
		try {
			date = LocalDate.parse(input);
		}
		catch (DateTimeParseException e) {System.out.println("Blad podczas konwertowania daty."); e.printStackTrace();}
		return date;
		
	}
	
	@SuppressWarnings("unchecked")
	private TableView<Currency> getTableView () {
		
		TableView<Currency> table = new TableView<>();
		table.setPrefSize(400, 400);
		table.setItems(currentCurrencies);
		
		TableColumn<Currency, String> nameCol = new TableColumn<>("Nazwa");
		nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
		nameCol.setPrefWidth(200);
		
		TableColumn<Currency, String> codeCol = new TableColumn<>("Kod"); 
		codeCol.setCellValueFactory(new PropertyValueFactory<>("code"));
		codeCol.setPrefWidth(40);
		
		TableColumn<Currency, String> midValueCol = new TableColumn<>("PLN"); 
		midValueCol.setCellValueFactory(new PropertyValueFactory<>("midValue"));
		midValueCol.setPrefWidth(60);
		
		TableColumn<Currency, String> dateCol = new TableColumn<>("Data");
		dateCol.setCellValueFactory(new PropertyValueFactory<>("date"));
		dateCol.setPrefWidth(70);
		
		table.getColumns().addAll(nameCol, codeCol, midValueCol, dateCol);
		
		return table;
	}
	
	private boolean connectToURL (String urlAdress) {
		HttpURLConnection connection = null;
		URL url = null;
		int code = 0;
		
		try {
			url = new URL(urlAdress);
			connection = (HttpURLConnection)url.openConnection();
			connection.setConnectTimeout(5000);
			connection.connect();
			code = connection.getResponseCode();
			
		}
		catch (UnknownHostException e) {errorDisplay.setText("Blad polaczenia ze stron¹ NBP!");}
		catch (SocketTimeoutException e) {errorDisplay.setText("Czas na wykonanie ¿¹dania up³yn¹³, operacja przerwana.");}
		catch (IOException e) {System.out.println(e.getMessage());}
		
		return checkHTTPcode(code);	
	}
	
	private boolean checkHTTPcode (int code) {
		boolean result = false;
		
		switch (code) {
		case 400: { errorDisplay.setText("B³¹d! Zakres dat nie mo¿e byæ wiêkszy ni¿ 93 dni!"); break; }
		case 404: { errorDisplay.setText("Brak danych z NBP dla podanych wartoœci!"); break; }
		case 200: { result = true; break; }
		default: break;
		}
		
		return result;
	}
	
	private boolean checkDates (LocalDate date1, LocalDate date2) {
		
		if (date1 == null || date2 == null)
			return false;
		
		if (date1.isAfter(LocalDate.now()) || date2.isAfter(LocalDate.now())) {
			errorDisplay.setText("Niestety, nie potrafiê patrzeæ w przysz³oœæ :(");
			return false;
		}
		return true;
	}
	
}
