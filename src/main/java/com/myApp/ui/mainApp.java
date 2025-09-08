package com.myApp.ui;
import com.myApp.logicaDB.Schema;
import com.myApp.modelos.*;
import com.myApp.logicaDeNegocio.*;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;


public class mainApp extends Application{

    public static void main(String[] args) {
        Schema.init();

        Persona persona = new Persona(12, "Steven", "Perez", "steven@sd.com" );
        InscripcionesPersonas inscripcionesPersonas = new InscripcionesPersonas();
        //inscripcionesPersonas.guardarInformacion(persona);
        inscripcionesPersonas.cargarDatosDesdeBD();
        System.out.println(inscripcionesPersonas.getListaPersonas());

        launch(args);

    }

    @Override
    public void start(Stage stage) {
        FXMLLoader load = new FXMLLoader(getClass().getResource("/MenuView.fxml"));
        try {
            Scene scene = new Scene(load.load());
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
