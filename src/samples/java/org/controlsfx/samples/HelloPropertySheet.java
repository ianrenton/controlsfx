package org.controlsfx.samples;

import javafx.application.Application;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;

import org.controlsfx.Sample;
import org.controlsfx.control.PropertySheet;
import org.controlsfx.control.property.BeanPropertyUtils;

public class HelloPropertySheet extends Application implements Sample {

    private PropertySheet propertySheet = new PropertySheet();
    
    public static void main(String[] args) {
        launch();
    }
    
    @Override public String getSampleName() {
        return "Property Sheet";
    }
    
    @Override public String getJavaDocURL() {
        return Utils.JAVADOC_BASE + "org/controlsfx/control/PropertySheet.html";
    }

    @Override public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle("Property Sheet");
        propertySheet.getItems().addAll(  BeanPropertyUtils.getProperties( new Button("Title")) );
        Scene scene = new Scene( propertySheet, 400, 800);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    @Override public Node getPanel(Stage stage) {
        return propertySheet;
    }
}