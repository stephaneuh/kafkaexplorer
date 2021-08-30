package com.kafkaexplorer;

import com.kafkaexplorer.config.TerminalConfig;
import com.kafkaexplorer.utils.TerminalBuilder;
import com.kafkaexplorer.utils.TerminalTab;
import javafx.fxml.Initializable;
import javafx.scene.control.TabPane;
import javafx.scene.paint.Color;

import java.net.URL;
import java.util.ResourceBundle;

public class ksqlDBcliController implements Initializable {

    public TabPane tabPane;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {


//        Default Config
        TerminalConfig defaultConfig = new TerminalConfig();

        TerminalBuilder terminalBuilder = new TerminalBuilder(defaultConfig);
        TerminalTab terminal = terminalBuilder.newTerminal();
//        terminal.onTerminalFxReady(() -> {
//            terminal.getTerminal().command("java -version\r");
//        });

        tabPane.getTabs().add(terminal);
    }
}
