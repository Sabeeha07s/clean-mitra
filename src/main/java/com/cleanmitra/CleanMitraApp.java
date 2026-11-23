package com.cleanmitra;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import org.bson.Document;

import java.io.InputStream;
import java.util.Date;
import java.util.Properties;

import static com.mongodb.client.model.Sorts.descending;

public class CleanMitraApp extends Application {
    private MongoClient mongoClient;
    private MongoCollection<Document> issues;
    private final ObservableList<String> rows = FXCollections.observableArrayList();
    private ListView<String> issueList;
    private Label status;

    @Override
    public void start(Stage stage) {
        connectDatabase();
        Label title = new Label("Clean Mitra");
        title.getStyleClass().add("title");
        Label subtitle = new Label("Smart Waste Management · Admin / Driver / User workspace");

        TextField reporter = new TextField();
        reporter.setPromptText("Reporter / user name");
        TextField location = new TextField();
        location.setPromptText("Collection location");
        TextArea details = new TextArea();
        details.setPromptText("Describe the waste collection issue");
        details.setPrefRowCount(3);
        TextField driver = new TextField();
        driver.setPromptText("Assigned driver (optional)");
        ComboBox<String> state = new ComboBox<>(FXCollections.observableArrayList("REPORTED", "ASSIGNED", "RESOLVED"));
        state.setValue("REPORTED");

        Button add = new Button("Report issue");
        add.setOnAction(e -> {
            if (reporter.getText().isBlank() || location.getText().isBlank() || details.getText().isBlank()) {
                status.setText("Please enter reporter, location and issue details.");
                return;
            }
            try {
                Document issue = new Document("reporter", reporter.getText().trim())
                        .append("location", location.getText().trim())
                        .append("details", details.getText().trim())
                        .append("driver", driver.getText().trim())
                        .append("status", state.getValue())
                        .append("createdAt", new Date());
                issues.insertOne(issue);
                reporter.clear(); location.clear(); details.clear(); driver.clear();
                refresh();
                status.setText("Issue saved successfully.");
            } catch (Exception ex) {
                status.setText("Database save failed: " + ex.getMessage());
            }
        });

        Button refreshButton = new Button("Refresh list");
        refreshButton.setOnAction(e -> refresh());
        issueList = new ListView<>(rows);
        issueList.setPrefHeight(250);
        status = new Label("Ready");
        VBox form = new VBox(10, new Label("Report a collection issue"), reporter, location, details, driver, state, add);
        form.getStyleClass().add("panel");
        VBox list = new VBox(10, new Label("Recent reports"), refreshButton, issueList);
        list.getStyleClass().add("panel");
        HBox content = new HBox(16, form, list);
        HBox.setHgrow(form, Priority.ALWAYS); HBox.setHgrow(list, Priority.ALWAYS);
        form.setPrefWidth(330); list.setPrefWidth(430);
        VBox root = new VBox(16, title, subtitle, content, status);
        root.setPadding(new Insets(24));
        root.getStyleClass().add("root");
        stage.setTitle("Clean Mitra – Smart Waste Management");
        Scene scene = new Scene(root, 850, 650);
        var css = getClass().getResource("/styles.css");
        if (css != null) scene.getStylesheets().add(css.toExternalForm());
        stage.setScene(scene);
        stage.show();
        refresh();
    }

    private void connectDatabase() {
        Properties properties = new Properties();
        try (InputStream in = getClass().getResourceAsStream("/application.properties")) {
            if (in != null) properties.load(in);
        } catch (Exception ignored) { }
        String uri = properties.getProperty("mongodb.uri", "mongodb://127.0.0.1:27017");
        String database = properties.getProperty("mongodb.database", "clean_mitra");
        mongoClient = MongoClients.create(uri);
        MongoDatabase db = mongoClient.getDatabase(database);
        issues = db.getCollection("issues");
    }

    private void refresh() {
        if (issues == null || issueList == null) return;
        rows.clear();
        for (Document d : issues.find().sort(descending("createdAt")).limit(100)) {
            rows.add(String.format("%s | %s | %s | %s",
                    d.getString("status"), d.getString("location"),
                    d.getString("reporter"), d.getString("driver")));
        }
    }

    @Override
    public void stop() {
        if (mongoClient != null) mongoClient.close();
    }

    public static void main(String[] args) { launch(args); }
}
