package application;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.ResourceBundle;
import java.util.Scanner;

import javax.swing.JOptionPane;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;

public class Controller implements Initializable {

	@FXML
	private Label coordinates;
	@FXML
	private Label showCity;
	@FXML
	private AnchorPane pane = new AnchorPane();

	@FXML
	private ScrollPane parent = new ScrollPane();
	@FXML
	private ChoiceBox<String> source;
	@FXML
	private ChoiceBox<String> target;
	@FXML
	private ListView<String> pathListView;
	@FXML
	private TextField distanceView;
	private ObservableList<String> pathCities = FXCollections.observableArrayList();
	private ObservableList<String> citiesList = FXCollections.observableArrayList();
	private ArrayList<City> cities = new ArrayList<>();
	private ArrayList<Edge> edges = new ArrayList<>();
	private ArrayList<Line> allLines = new ArrayList<>();
	private ArrayList<Line> lines = new ArrayList<>();
	private HashMap<String, Node> table = new HashMap<String, Node>();
	private String sor = "none";

	@FXML
	void clear(ActionEvent event) {
		pathListView.getItems().clear();
		distanceView.setText("");
		source.setValue("none");
		target.setValue("none");


		for (int i = 0; i < lines.size(); i++) {
			pane.getChildren().remove(lines.get(i));
		}
		if (!(sor == "none"))
			getCity(sor).getCircle().setFill(Color.web("#008CBA"));

		for (int i = 0; i < pathCities.size(); i++) {
			getCity(pathCities.get(i)).getCircle().setFill(Color.web("#008CBA"));

		}

		for (int i = 0; i < allLines.size(); i++) {
			pane.getChildren().remove(allLines.get(i));
		}
		allLines.clear();

		// clear lines Array List
		pathCities.clear();
		lines.clear();
		sor = "none";

		// pathCities.clear();

	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {

		try {
			getData();

			coordinates.setText("Longitude: " + "   , Latitude: ");

			final double MAX_FONT_SIZE = 20.0;

			citiesList.add("none");
			source.setValue("none");
			target.setValue("none");
			for (int i = 0; i < cities.size(); i++) {
				citiesList.add(cities.get(i).getName());
			}
			source.setItems(citiesList);
			target.setItems(citiesList);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		initalizeMap();
	}

	/*
	 * Reset the table
	 */

	/*
	 * fill the map with cities by putting each city on its right coordinates. each
	 * city with its name and a red circle that represents it
	 */
	public void initalizeMap() {
		for (int i = 0; i < cities.size(); i++) {
			// the circle that represents the city on the map
			Circle point = new Circle(5);

			// a label that hold the city name
			Label cityName = new Label(cities.get(i).getName());

			final double MAX_FONT_SIZE = 11;
			cityName.setStyle("-fx-font-width: bold;");
			cityName.setFont(new Font(MAX_FONT_SIZE));

			// set circle coordinates
			// 801/(36-34)
			//1876/(33.333333-29.333333)
			point.setCenterX(((cities.get(i).getCoordinateY() - 34) * 400.5));
			point.setCenterY(1876 - ((cities.get(i).getCoordinateX() - 29.333333) * 469));


			// set label beside the circle
			cityName.setLayoutX(((cities.get(i).getCoordinateY() - 34) * 400.5) + 4);
			cityName.setLayoutY(1876 - ((cities.get(i).getCoordinateX() - 29.333333) * 469) - 11);

			point.setFill(Color.web("#008CBA"));

			point.setStroke(Color.BLACK);
			point.setStrokeWidth(.3);

			Tooltip tooltip = new Tooltip(
					cities.get(i).toString().replaceAll("_", " ").replaceAll("Y", "X").replaceFirst("X", " Y"));
			tooltip.setAutoFix(true);
			Tooltip.install(point, tooltip);

			// setting city circle to the circle above
			cities.get(i).setCircle(point);

			// add the circle and the label to the scene
			pane.getChildren().addAll(cities.get(i).getCircle(), cityName);
			String temp = cities.get(i).toString();

			/*
			 * Get city name and coordinates, and choose it in the choice box(if it is null)
			 * when clicking on the circle
			 */
			point.setOnMouseClicked(e -> {
				String cityInfo = temp.replaceAll("_", " ").replaceAll("Y", "X").replaceFirst("X", " Y");

				showCity.setText(cityInfo);
				String[] sp = temp.split("[-]");
				if (source.getValue() == "none") {
					source.setValue(sp[0].trim());
				} else if (target.getValue() == "none") {
					target.setValue(sp[0].trim());
				}
			});

			point.setOnMouseEntered(e -> {
				point.setFill(Color.LIGHTGREEN);
			});
			point.setOnMouseExited(e -> {
				point.setFill(Color.web("#008CBA"));
			});
		}
	}

	/*
	 * Run the program by clicking on the (Run) button and get the shortest path
	 */
	@FXML
	void run(ActionEvent event) {
		// if all choice boxes are not null

		if (source.getValue() != "none" && target.getValue() != "none") {

			String[] sourceXY = getCityCoordinates(source.getValue()).trim().split("[,]");
			String[] targetXY = getCityCoordinates(target.getValue()).trim().split("[,]");

			/*
			 * ----> run "shortest path"
			 */

			if (!getCity(source.getValue()).equals(getCity(target.getValue()))) {
				for (int i = 0; i < pathCities.size(); i++) {
					getCity(pathCities.get(i)).getCircle().setFill(Color.web("#008CBA"));

				}
				if (!(sor == "none"))
					getCity(sor).getCircle().setFill(Color.web("#008CBA"));
				sor = source.getValue();
				getShortestPath(getCity(source.getValue()), getCity(target.getValue()));
				for (int i = 0; i < lines.size(); i++) {
					lines.get(i).setStrokeWidth(2);
					pane.getChildren().add(lines.get(i));

				}
			} else {

				Alert alert = new Alert(AlertType.WARNING);
				alert.setTitle("heeeey!");
				alert.setContentText("You are already in the city");
				alert.show();
			}

		} else {
			JOptionPane.showMessageDialog(null, "You have to choose two cities", "Worning!", JOptionPane.PLAIN_MESSAGE);
		}
		source.setValue("none");
		target.setValue("none");

	}

	/*
	 * Get shortest path between to cities
	 */
	private void getShortestPath(City sourceCity, City targetCity) {
		// reset the table to start new one

		buildTable(sourceCity, targetCity);

		// remove previous lines
		for (int i = 0; i < lines.size(); i++) {
			pane.getChildren().remove(lines.get(i));
		}
		// clear lines Array List
		lines.clear();

		// clear the Observable List that holds all cities between the target and source
		// cities
		pathCities.clear();
		// clear the list view that shows all cities between the target and source
		// cities
		pathListView.getItems().clear();
		// reset the total distance Text Field
		distanceView.setText("0.0");

		// check if there is path
		if (table.get(targetCity.getName()).getDistance() != Double.POSITIVE_INFINITY
				&& table.get(targetCity.getName()).getDistance() != 0) {
			shortestPath(sourceCity, targetCity);
			DecimalFormat df = new DecimalFormat("###.##");
			Node t = table.get(targetCity.getName());
			distanceView.setText(df.format(t.getDistance()));
			sourceCity.getCircle().setFill(Color.web("#1ACF26"));
			targetCity.getCircle().setFill(Color.web("#DB241A"));
			/*
			 * Add all the cities that were found between the source and target cities
			 */DecimalFormat ds = new DecimalFormat("###.##");
			double path = getDistance(getCity(sourceCity.getName()).getCoordinateX(),
					getCity(sourceCity.getName()).getCoordinateY(),
					getCity(pathCities.get(pathCities.size() - 1)).getCoordinateX(),
					getCity(pathCities.get(pathCities.size() - 1)).getCoordinateY());
			pathListView.getItems().add("(start)");
			pathListView.getItems().add(sourceCity.getName() + " ---> " + pathCities.get(pathCities.size() - 1) + " ="
					+ ds.format(path) + "km");

			for (int i = pathCities.size() - 1; i >= 0; i--) {

				if (i == 0) {
					pathListView.getItems().add(" (End)");
				} else {
					path = getDistance(getCity(pathCities.get(i)).getCoordinateX(),
							getCity(pathCities.get(i)).getCoordinateY(),
							getCity(pathCities.get(i - 1)).getCoordinateX(),
							getCity(pathCities.get(i - 1)).getCoordinateY());

					pathListView.getItems()
							.add(pathCities.get(i) + " ---> " + pathCities.get(i - 1) + " =" + ds.format(path) + "km");
				}
			}
		} else {

			Alert alert = new Alert(AlertType.WARNING);
			alert.setTitle("Ther is no path!");
			alert.setContentText("Missing Edges!");
			alert.show();
		}

	}

	/*
	 * A recursive method to trace the path between two cities
	 */
	private void shortestPath(City sourceCity, City targetCity) {
		pathCities.add(targetCity.getName());

		Node t = table.get(targetCity.getName());

		if (t.getSourceCity() == null) {
			return;
		}
		if (t.getSourceCity() == sourceCity) {

			if (sourceCity != targetCity) {
				t.getSourceCity().getCircle().setFill(Color.web("#E66325"));
				targetCity.getCircle().setFill(Color.web("#E66325"));

				lines.add(new Line(((t.getSourceCity().getCoordinateY() - 34) * 400.5),
						1876 - ((t.getSourceCity().getCoordinateX() - 29.333333) * 469),
						((targetCity.getCoordinateY() - 34) * 400.5),
						1876 - ((targetCity.getCoordinateX() - 29.333333) * 469)));
			}
			return;
		}

		t.getSourceCity().getCircle().setFill(Color.web("#E66325"));
		targetCity.getCircle().setFill(Color.web("#E66325"));
		lines.add(new Line(((t.getSourceCity().getCoordinateY() - 34) * 400.5),
				1876 - ((t.getSourceCity().getCoordinateX() - 29.333333) * 469),
				((targetCity.getCoordinateY() - 34) * 400.5),
				1876 - ((targetCity.getCoordinateX() - 29.333333) * 469)));
		shortestPath(sourceCity, t.getSourceCity());
	}

	/*
	 * Build the hash table by applying the dijkstra algorithm
	 */
	private void buildTable(City source, City targetCity) {

		table.clear();
		for (City i : cities) {
			table.put(i.getName(), new Node(i, false, Double.POSITIVE_INFINITY, null));
		}
		

		TableCompare comp = new TableCompare();
		Queue<Node> q = new PriorityQueue<Node>(8,comp);

		Node node = table.get(source.getName());
		node.setDistance(0.0);
		node.setKnown(true);
		q.add(node);

		while (!q.isEmpty()) {

			
			Node temp = q.poll();

			temp.setKnown(true);

			if (temp.getCurrentCity() == targetCity) {
				break;
			}
			ArrayList<Adjacent> adj = temp.getCurrentCity().getAdjacent();

			for (Adjacent c : adj) {
				Node t = table.get(c.getCity().getName());
				if (t.isKnown()) {
					continue;
				}

				// && its distance >= the distance between it and the current source city + all
				// previous path distance
				// && are adjacent
				double newDis = c.getDistance() + temp.getDistance();
				if (newDis < t.getDistance()) {
					t.setSourceCity(temp.getCurrentCity());
					t.setDistance(newDis);
				}
				q.add(t);
			}

		}
	}

	public double getDistance(double fromLatitude, double fromLongitude, double toLatitude, double toLongitude) {

		// Convert latitudes and longitude to radians

		double dLat = Math.toRadians(toLatitude - fromLatitude);
		double dLon = Math.toRadians(toLongitude - fromLongitude);

		// convert to radians
		fromLatitude = Math.toRadians(fromLatitude);
		toLatitude = Math.toRadians(toLatitude);

		// apply formulae
		double a = Math.pow(Math.sin(dLat / 2), 2)
				+ Math.pow(Math.sin(dLon / 2), 2) * Math.cos(fromLatitude) * Math.cos(toLatitude);
		double rad = 6371;
		double c = 2 * Math.asin(Math.sqrt(a));
		return rad * c;

		// Haversine formula

	}

	/*
	 * Get City coordinates by its name from the cities Array List
	 */
	private String getCityCoordinates(String cityName) {
		for (int i = 0; i < citiesList.size(); i++) {
			if (cities.get(i).getName() == cityName) {
				return cities.get(i).getCoordinateX() + "," + cities.get(i).getCoordinateY();
			}
		}
		return null;
	}

	/*
	 * Get the City by its name from the cities Array List
	 */
	private City getCity(String cityName) {

		for (int i = 0; i < cities.size(); i++) {
			if (cities.get(i).getName().equalsIgnoreCase(cityName)) {
				return cities.get(i);
			}
		}
		return null;
	}

	private void getData() throws FileNotFoundException {

		getCities();
		getDistances();
		addAdjacents();

	}

	/*
	 * Read Cities file and store its content to the cities Array List
	 */
	private void getCities() throws FileNotFoundException {
		File file = new File("cities1");
		Scanner input = new Scanner(file);
		while (input.hasNextLine()) {
			String str = input.nextLine();
			String[] spStr = str.trim().replaceAll("\\s", " ").split("[ ]");

			cities.add(new City(Double.parseDouble(spStr[1]), Double.parseDouble(spStr[2]), spStr[0], new Circle()));
			System.out.println(Double.parseDouble(spStr[1]));

		}
		input.close();

	}

	/*
	 * Read Distances file and store its content to the Edges Array List
	 */
	private void getDistances() throws FileNotFoundException {
		File file = new File("distance");
		Scanner input = new Scanner(file);

		while (input.hasNextLine()) {
			String[] spStr = input.nextLine().trim().split("[ ]");
			edges.add(new Edge(getCity(spStr[0]), getCity(spStr[1]),
					getDistance(getCity(spStr[0]).getCoordinateX(), getCity(spStr[0]).getCoordinateY(),
							getCity(spStr[1]).getCoordinateX(), getCity(spStr[1]).getCoordinateY())));
			System.out.println(spStr[0] + spStr[1]);
		}

		input.close();
	}

	/*
	 * Get each city and set all its adjacent cities
	 */
	private void addAdjacents() {
		for (int i = 0; i < cities.size(); i++) {
			for (int j = 0; j < edges.size(); j++) {
				if (cities.get(i).getName().equalsIgnoreCase(edges.get(j).getSourceCity().getName())) {
					City c = edges.get(j).getTargetCity();
					Adjacent n = new Adjacent(c, edges.get(j).getDistance());
					cities.get(i).getAdjacent().add(n);

				} else if (cities.get(i).getName().equalsIgnoreCase(edges.get(j).getTargetCity().getName())) {
					City c = edges.get(j).getSourceCity();
					Adjacent n = new Adjacent(c, edges.get(j).getDistance());
					cities.get(i).getAdjacent().add(n);
				}

			}
		}
	}

	/*
	 * Remove all the paths on the map
	 */
	@FXML
	void hidePaths(ActionEvent event) {
		try {
			for (int i = 0; i < edges.size(); i++) {
				pane.getChildren().remove(allLines.get(i));
			}
			allLines.clear();

		} catch (Exception e) {
			Alert alert = new Alert(AlertType.WARNING);
			alert.setTitle("Worning!");
			alert.setContentText("Ther are no paths!");
			alert.show();
		}
	}

	/*
	 * Show all the paths on the map
	 */
	@FXML
	void showPaths(ActionEvent event) throws FileNotFoundException {
		try {
			for (int i = 0; i < edges.size(); i++) {

				double startX = ((edges.get(i).getSourceCity().getCoordinateY() - 34) * 400.5);
				double startY = 1876 - ((edges.get(i).getSourceCity().getCoordinateX() - 29.333333) * 469);
				double endX = ((edges.get(i).getTargetCity().getCoordinateY() - 34) * 400.5);
				double endY = 1876 - ((edges.get(i).getTargetCity().getCoordinateX() - 29.333333) * 469);
				System.out.println("SX " + startX + "SY " + startY + "EX " + endX + "EY " + endY);
				allLines.add(new Line(startX, startY, endX, endY));
				pane.getChildren().add(allLines.get(i));
			}
		} catch (Exception e) {
			Alert alert = new Alert(AlertType.INFORMATION);
			alert.setTitle("Worning!");
			alert.setContentText("Paths already shown!");
			alert.show();

		}
	}

	/*
	 * Display mouse coordinates
	 */
	@FXML
	void mouseCoordinates(MouseEvent event) {
		// 1876-((cities.get(i).getCoordinateX()-29.333333)*469)
		coordinates.setText("");
	//	"Longitude: " + ((event.getX() / 400.5) + 34) + " * Latitude: "
	//	+ (((1876 - event.getY()) / 469) + 29.333333)
	}

	/*
	 * Exit the Window
	 */
	@FXML
	void close(ActionEvent event) {
		System.exit(0);
	}

	@FXML
	void help(ActionEvent event) {
		String aboutString = "Palestine Map";
		Alert alert = new Alert(AlertType.INFORMATION);
		alert.setTitle("About...");
		alert.setContentText(aboutString);
		alert.show();
	}

}
