package com.fxc;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import java.io.IOException;
import java.net.ConnectException;
import java.util.ArrayList;
import java.util.concurrent.*;

import static java.lang.System.exit;

public class Main extends Application {

    private final ObservableMap<String, String> usersMap = FXCollections.observableHashMap();
    private final ObservableMap<String, String> usersIP = FXCollections.observableHashMap();
    private ObservableMap<String, String> chatMSG;
    private ObservableMap<String, ArrayList<String>> oldMSG;

    @Override
    public void start(Stage primaryStage) throws Exception{

        primaryStage.setTitle("Chat");
        primaryStage.setScene(logInScene(primaryStage));
        primaryStage.show();

    }

    public Scene logInScene(Stage primaryStage){
        //Setup from root pane
        GridPane rootPane = new GridPane();
        rootPane.setPadding(new Insets(5));
        rootPane.setVgap(5);
        rootPane.setHgap(5);
        rootPane.setAlignment(Pos.CENTER);
        //Adding blocks
        Label welcomeLabel = new Label("Enter username");
        TextField userNickname = new TextField();
        Button okButton = new Button("Ok");
        okButton.setWrapText(false);
        okButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                if(!userNickname.getText().isEmpty()) {
                    primaryStage.setScene(menuScene(primaryStage, userNickname.getText().replaceAll(" ","")));
                }
            }
        });

        rootPane.add(welcomeLabel, 0,0);
        rootPane.add(userNickname, 0,1);
        rootPane.add(okButton,1,1);

        return new Scene(rootPane, 200, 100);

    }

    public Scene menuScene(Stage primaryStage, String userNickname){
        //Multicast Networking setup
        MulticastReceiverThread runMcrThread = new MulticastReceiverThread();
        Thread mcrThread = new Thread(runMcrThread, "MCRthread");

        MulticastPublisherThread runMcpThread = new MulticastPublisherThread(userNickname);
        Thread mcpThread = new Thread(runMcpThread, "MCPthread");

        mcpThread.start();
        mcrThread.start();
        //Scene creation
        TreeItem<String> root = new TreeItem<String>("Users");
        root.setExpanded(true);
        TreeView<String> treeView = new TreeView<String>(root);
        //TreeItem observer for clicks
        treeView.getSelectionModel()
                .selectedItemProperty()
                .addListener((observable, oldValue, newValue) -> {
                    if(newValue.getValue() != "Users") {
                        createClient(newValue.getValue(),Integer.toString(runMcpThread.getRandomPort()));
                    }
                });


        //Listener to add new network users
        usersMap.addListener(new InvalidationListener() {
            @Override
            public void invalidated(Observable observable) {
                //TODO - remove people when logged out and add notification
                root.getChildren().clear();
                for(String i : usersMap.keySet())
                {
                    if(!i.equals(Integer.toString(runMcpThread.getRandomPort()))) {
                        root.getChildren().addAll(new TreeItem(usersMap.get(i)));

                    }
                }
            }
        });


        //Startup of server receiver
        ServerThread serverThread = new ServerThread(runMcpThread.getRandomPort());
        Thread t1 = new Thread(serverThread);
        t1.start();
        chatMSG = ServerThread.chatMsg;
        oldMSG = ServerThread.oldMsg;

        //Download values from threads every second
        ScheduledExecutorService exec = Executors.newSingleThreadScheduledExecutor();
        exec.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                usersMap.putAll(runMcrThread.getNetworkUsers());
                usersIP.putAll(runMcrThread.getUserIP());
            }
        }, 0, 1, TimeUnit.SECONDS);



        return new Scene(treeView, 200, 300);

    }

    public void createClient(String selectedName, String myName) {
        String hostName = "";
        String port = "";
        for(String i : usersMap.keySet())
        {
            if(usersMap.get(i).equals(selectedName))
            {
                port = i;
                hostName = usersIP.get(i);
            }
        }

        //Setup of client thread
        ClientThread clientThread;
        try{
            clientThread = new ClientThread(Integer.parseInt(port),hostName);
            Thread tC = new Thread(clientThread);
            tC.setDaemon(true);
            tC.start();
            createNewStage(clientThread,port, myName, selectedName);
        }
        catch(ConnectException e)
        {
            System.out.println("Wrong port");
        }
        catch(IOException e)
        {
            System.out.println("IOException");
        }


    }

    public void createNewStage(ClientThread clientThread, String portNumber, String myName, String selectedName){

        //new window
        GridPane root = new GridPane();
        root.setPadding(new Insets(5));
        root.setVgap(5);
        root.setHgap(5);
        root.setAlignment(Pos.CENTER);

        TextArea textArea = new TextArea();
        textArea.setEditable(false);
        TextField textField = new TextField();
        Button button1 = new Button("Send");

        button1.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                textArea.appendText(textField.getText()+"\n");
                clientThread.writeToServer(myName+"-"+usersMap.get(myName)+": "+textField.getText());
                textField.clear();
            }
        });

        //Printing old messages from container
        ArrayList<String> values;
        if(oldMSG != null){

            for(String i : oldMSG.keySet())
            {
                if(i.equals(portNumber))
                {
                    values = oldMSG.get(i);
                    for(String j : values)
                    {
                        textArea.appendText(j+"\n");
                    }
                }
            }
        }

        //Listener for new values in main msg container
        chatMSG.addListener(new InvalidationListener() {
            String oldMsgTemp = "";
            @Override
            public void invalidated(Observable observable) {

                for(String i : chatMSG.keySet())
                {
                    if(i.equals(portNumber) && !oldMsgTemp.contains(chatMSG.get(i)))
                    {
                        oldMsgTemp = chatMSG.get(i);
                        textArea.appendText(chatMSG.get(i)+"\n");
                    }
                }

            }
        });

        //simple filling
        root.add(textArea, 0,0);
        root.add(textField,0,1);
        root.add(button1, 1,1);
        Scene scene = new Scene(root, 300, 200);
        Stage stage = new Stage();
        stage.setTitle(selectedName);
        stage.setScene(scene);
        stage.show();
    }


    public static void main(String[] args) {
        launch(args);
        exit(0);
    }
}

