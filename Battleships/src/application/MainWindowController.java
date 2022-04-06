package application;

import application.ships.*;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import server.BotPlayer;
import server.MainServer;
import server.messages.MessageChat;
import server.messages.MessageLobbyCreate;
import server.messages.MessageLobbyJoin;
import java.util.Random;

import java.io.IOException;
import java.util.List;

//import /*java.awt.Label;*/ javafx.scene.control.Label;
//import /*java.awt.TextField;*/ javafx.scene.control.TextField;

public class MainWindowController {

    /** declare game */
    private Game game;

    /** initialise selectedShipIndex */
    private int selectedShipIndex = -1;

    /** initialise shipDirection */
    private ShipDirection shipDirection = null;

    /** initialise english */
    private boolean english = true;

    /** initialise darkMode */
    private boolean darkMode = false;

    /** declare Node-Array for gameFieldNodes */
    private Node[][] gameFieldNodes;

    /** create ObservableList for the choiceBox for thw language */
    ObservableList<String> cmbList = FXCollections.observableArrayList("Deutsch", "English");

    //Views
    @FXML ImageView myImageView;
    @FXML Image myImage = new Image(getClass().getResourceAsStream("vierschiff.jpg"));
    @FXML ImageView imageFirstPlaceView;
    @FXML Image imageFirstPlace = new Image(getClass().getResourceAsStream("first_place.png"));
    @FXML ImageView imageSecondPlaceView;
    @FXML Image imageSecondPlace = new Image(getClass().getResourceAsStream("second_place.png"));
    //Battleships.jpg:
    //Fit Width: 2000, Fit Height: 1400
    //Layout X: -240, Layout Y: 0
    //vierschiff.jpg:
    //Quelle: https://en.wikipedia.org/wiki/Royal_New_Zealand_Navy
    //Fit Width: 1400, Fit Height: 800
    //Layout X: -50, Layout Y: 0

    @FXML private GridPane shipGridHorizontal, shipGridVertical;

    @FXML private Label label;
    @FXML private Label label1;
    @FXML private Label lblTitle;
    @FXML private Label lblvertical;
    @FXML private TextField fieldname;
    @FXML private TextField txtLobbyID;
    @FXML private GridPane gamefieldpane;
    @FXML private GridPane enemygamefieldpane;
    @FXML private ComboBox cmbLanguage;
    @FXML private TextFlow textFlowGameRules;
    @FXML private Tab shipsTab;
    @FXML private Tab gameRulesTab;
    @FXML private Tab settingsTab;
    @FXML private TabPane tabPane;
    @FXML private Button btncreate;
    @FXML private Button btnsingle;
    @FXML private Button btnjoin;
    @FXML private Button saveSettings;
    @FXML private Button btnClearAllShips;
    @FXML private Button btnRestart;
    @FXML private VBox vName;
    @FXML private VBox vPlay;
    @FXML private RadioButton rdDarkMode;
    @FXML private RadioButton rdLightMode;
    @FXML private RadioButton rdVibrantMode;

    @FXML private TextArea chathistory;
    @FXML private TextField chatmassage;
    @FXML private Pane panePregame;
    @FXML private AnchorPane paneIngame;

    @FXML private Button btnReady;
    private boolean ready = false;

    public Main main;
    private String playerName;

    /** This method build up an initial game  */
    public void initGame(){
        game = new Game();
        game.setMainWindowController(this);
    }

    /**
     * This method sets main
     *
     * @param main
     */
    public void setMain(Main main){
        this.main = main;
    }

    /** This method handle the name in the game */
    @FXML
    public void handlename(){
        String name = fieldname.getText();
        if (name.length() < 1) {
            int random = new Random().nextInt(BotPlayer.getBotNames().length);
            fieldname.setText(BotPlayer.getBotNames()[random]);
            return;
        }
        label.setText(name);
        playerName = name;
        fieldname.clear();
        btnsingle.setVisible(true);
        btncreate.setVisible(true);
        btnjoin.setVisible(true);
        txtLobbyID.setVisible(true);
        vName.setVisible(false);
    }

    /**
     * This method handle the nome of the opponent
     *
     * @param name given name
     */
    public void handleenemyname(String name){
        Platform.runLater(() -> {
            label1.setText(name);
        });
    }

    /** This method creates a new lobby for multiplayer */
    @FXML
    public void createLobby(){
        handleenemyname("Enemy");
        try {
            game.connectToServer(Main.SERVER_IP, 5544);
            game.getConnection().sendMessage(new MessageLobbyCreate(label.getText()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /** This method let you join into a created lobby */
    @FXML
    public void joinLobby(){
        try {
            game.connectToServer(Main.SERVER_IP, 5544);
            game.getConnection().sendMessage(new MessageLobbyJoin(txtLobbyID.getText(), label.getText()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /** This method handle the "Ready" button */
    @FXML
    public void handleReadyButton() {
        if (game.getConnection() != null) {
            if (!game.allShipsPlaced()) {
                if (english) {
                    addChatMessage("Not all ships set !");
                } else {
                    addChatMessage("Nicht alle Schiffe gesetzt !");
                }
                return;
            }
            ready = !ready;
            System.out.println("Ready ? " + ready);
            game.getGameField().sendShipsToServer();
            btnReady.setVisible(false);
            btnClearAllShips.setVisible(false);
        }
    }

    /** This method handle the "Clear all Ships" button */
    @FXML
    public void clearAllShips(){
        if(game.isReady() || ready)
            return;
        game.getGameField().clearShips();
        selectedShipIndex = -1;
        shipDirection = null;
        repaintGamefields();
        paintAvailableShips();
        //setWinnerImage(true);
        //setViewToAftergame();
    }

    /** This method sets all the necessary views in game */
    public void setViewToIngame(){
        paneIngame.setVisible(true);
        panePregame.setVisible(false);
        chatmassage.setText("");
        shipsTab.setDisable(false);
        tabPane.getSelectionModel().select(0);
        myImageView.setVisible(false);
        vPlay.setVisible(false);
        lblTitle.setVisible(false);
        rdLightMode.setSelected(true);
        btnReady.setVisible(true);
        tabPane.setVisible(true);
    }

    /** This method sets all the necessary views pregame */
    public void setViewToPregame(){
        paneIngame.setVisible(false);
        panePregame.setVisible(true);
        btnsingle.setVisible(false);
        btncreate.setVisible(false);
        btnjoin.setVisible(false);
        txtLobbyID.setVisible(false);
        chatmassage.setText("");
        shipsTab.setDisable(true);
        tabPane.getSelectionModel().select(1);
        myImageView.setImage(myImage);
        imageFirstPlaceView.setImage(imageFirstPlace);
        imageSecondPlaceView.setImage(imageSecondPlace);
        imageFirstPlaceView.setVisible(false);
        imageSecondPlaceView.setVisible(false);
        vName.setVisible(true);
        vPlay.setVisible(true);
        chathistory.clear();

        myImageView.setVisible(true);
        btnRestart.setVisible(false);
        btnReady.setVisible(true);
    }

    /** This method sets all the necessary views after game */
    public void setViewToAftergame(){
        tabPane.setVisible(false);
        //btnRestart.setVisible(true);
        vName.setVisible(true);
        vPlay.setVisible(true);
    }

    /** This method restart the game */
    @FXML
    public void restartGame(){
        setViewToPregame();
    }

    /** This method sets the winner image */
    @FXML
    public void setWinnerImage(boolean won){
        if(won){
            imageFirstPlaceView.setVisible(true);
            setViewToAftergame();
        }else{
            imageSecondPlaceView.setVisible(true);
            setViewToAftergame();
        }
    }

    /** This method sends all the sending chat messages */
    @FXML
    public void sendChatMessage(){
        String message = chatmassage.getText();
        if (message.length() < 0){
            return;
        }
        chatmassage.setText("");
       // System.out.println("Sending Chat Message: " + message);
        if (game.getConnection() != null)
            game.getConnection().sendMessage(new MessageChat(message));
        else
            addChatMessage(message);
    }

    /** This method add all the chat messages */
    public void addChatMessage(String message) {
        Platform.runLater(() -> {
            chathistory.appendText(message + "\n");
            chathistory.setScrollTop(Double.MAX_VALUE);
            chathistory.setScrollLeft(Double.MAX_VALUE);
        });
    }

    /** This method create new server for singleplaver */
    @FXML
    public void playsingle(){
        MainServer server = new MainServer(null);
        server.start();
        try {
            game.connectToServer("127.0.0.1", server.getPort());
            game.getConnection().sendMessage(new MessageLobbyCreate(label.getText()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /** This method sets a lobby code */
    public void setLobbyCode(String lobbyID) {
        addChatMessage("Joined Lobby " + lobbyID);
    }

    /** This method saves all theactual settings in the game */
    @FXML
    public void saveSettings(){
        darkMode = rdDarkMode.isSelected() || rdVibrantMode.isSelected();
        System.out.println(darkMode);
        if (darkMode) {
            main.getScene().getStylesheets().remove("dark-theme.css");
            main.getScene().getStylesheets().add("dark-theme.css");
        } else {
            main.getScene().getStylesheets().remove("dark-theme.css");
        }
        int index = cmbLanguage.getSelectionModel().getSelectedIndex();
        if (index == 0) {
            english = false;
            updateUILanguage();
        } else {
            english = true;
            updateUILanguage();
        }
        repaintGamefields();
        paintAvailableShips();
    }

    /** This method sets the chosen language */
    public void setLanguageChoice(){
        cmbLanguage.setItems(cmbList);
    }

    /** This method updates the rules of the game in the chosen language */
    public void updateUILanguage() {
        textFlowGameRules.getChildren().clear();
        if (!english) {
            Text text1 = new Text("Wähle zu Beginn einen Namen und anschließend den Einzel- oder Mehrspielermodus unten rechts aus.\n");
            text1.setFont(Font.font("Arial"));
            Text text2 = new Text("Schiffe setzen:\n");
            text2.setUnderline(true);
            text2.setFont(Font.font("Arial", FontWeight.EXTRA_BOLD, 12));
            text1.setStyle("-fx-text-base-color: white");
            Text text3 = new Text("Setze die 10 vorgegebenen Schiffe horizontal und vertikal auf das untere Spielfeld. Diese dürfen nicht direkt nebeneinander gesetzt werden. ");
            text3.setFont(Font.font("Arial"));
            Text text4 = new Text("Wähle dazu das Schiff, welches gesetzt werden soll, im Schiffpool oben rechts per Mausklick aus und klicke im unteren Spielfeld auf die nördlichste (bei vertikalem Schiff) bzw. westlichste (bei horizontalem Schiff) Koordinate bei der das Schiff beginnen soll.\n");
            text4.setFont(Font.font("Arial"));
            Text text5 = new Text("Schießen:\n");
            text5.setUnderline(true);
            text5.setFont(Font.font("Arial", FontWeight.EXTRA_BOLD, 12));
            Text text6 = new Text("Nachdem beide Seiten ihre Schiffe gesetzt haben, wird oben auf das gegnerische Spielfeld geschossen, indem das jeweilige Feld mit der Maus angeklickt wird. Es wird immer abwechselnd geschossen.\n\n");
            text6.setFont(Font.font("Arial"));
            Text text7 = new Text("Die Schiffe werden zu Beginn grün dargestellt. Sobald ein Schiff getroffen wurde, wird es orange und wenn es versenkt wurde, wird es rot.");
            text7.setFont(Font.font("Arial"));
            textFlowGameRules.getChildren().addAll(text1, text2, text3, text4, text5, text6, text7);
            gameRulesTab.setText("Spielregeln");
            settingsTab.setText("Einstellungen");
            shipsTab.setText("Schiffe");
            rdLightMode.setText("Tagmodus");
            rdDarkMode.setText("Nachtmodus");
            rdVibrantMode.setText("Leuchtender Nachtmodus");
            saveSettings.setText("Einstellungen speichern");
            lblvertical.setText("Vertikal:");
            btnClearAllShips.setText("Alle Schiffe löschen");
            btnReady.setText("Fertig");
        }
        else {
            Text text1 = new Text("At the beginning choose a name and then choose the single or multiplayer mode on the bottom right.\n");
            text1.setFont(Font.font("Arial"));
            Text text2 = new Text("Set Ships:\n");
            text2.setUnderline(true);
            text2.setFont(Font.font("Arial", FontWeight.EXTRA_BOLD, 12));
            Text text3 = new Text("Place the 10 given ships horizontally and vertically on the lower playing field. These may not be placed directly next to each other. ");
            text3.setFont(Font.font("Arial"));
            Text text4 = new Text("To do this, select the ship that is to be placed from the ship pool on the top right with a mouse click and click in the lower playing field on the northernmost (for a vertical ship) or westernmost (for a horizontal ship) coordinate at which the ship should begin.\n");
            text4.setFont(Font.font("Arial"));
            Text text5 = new Text("Shoot:\n");
            text5.setUnderline(true);
            text5.setFont(Font.font("Arial", FontWeight.EXTRA_BOLD, 12));
            Text text6 = new Text("After both sides have set their ships, the opposing playing field above is shot by clicking on the respective field with the mouse. There is always alternating shooting.\n\n");
            text6.setFont(Font.font("Arial"));
            Text text7 = new Text("The ships are shown in green at the beginning. As soon as a ship is hit, it turns orange and when it is sunk, it turns red.");
            text7.setFont(Font.font("Arial"));
            textFlowGameRules.getChildren().addAll(text1, text2, text3, text4, text5, text6, text7);
            gameRulesTab.setText("Game Rules");
            settingsTab.setText("Settings");
            shipsTab.setText("Ships");
            rdLightMode.setText("Light Mode");
            rdDarkMode.setText("Dark Mode");
            rdVibrantMode.setText("Vibrant Dark Mode");
            saveSettings.setText("Save Settings");
            lblvertical.setText("Vertical:");
            btnClearAllShips.setText("Clear all Ships");
            btnReady.setText("Ready");
        }
        for (Node node: textFlowGameRules.getChildren()) {
            if (node instanceof Text) {
                Text text = (Text)node;
                text.setFill(darkMode ? Color.WHITE : Color.BLACK);
            }
        }
    }

    /** This method repaints all the saved settings */
    public void repaintGamefieldsSafe() {
        Platform.runLater(() -> {
                repaintGamefields();
            }
        );
    }

    /** This method repaints the actual gameField */
    public void repaintGamefields() {
        paintGameField(game.getGameField(), gamefieldpane, false);
        paintGameField(game.getEnemyGameField(), enemygamefieldpane, true);
    }

    /** This method handle the initial gameField panes */
    public void initGameFieldPanes() {
        initGameFieldPane(gamefieldpane, false);
        initGameFieldPane(enemygamefieldpane, true);
        gamefieldpane.addEventFilter(MouseEvent.MOUSE_EXITED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                paintGameField(game.getGameField(), gamefieldpane, false);
            }
        });
    }

    /** This method handle initGameFieldPane */
    private void initGameFieldPane(GridPane pane, boolean enemy) {
        Integer colI, rowI;
        Font font = new Font(enemy ? 20:38);
        if(!enemy)
            gameFieldNodes = new Node[10][10];
        for(Node node:pane.getChildren()){
            if (node == null || !(node instanceof Pane)) continue;
            colI = pane.getColumnIndex(node);
            rowI = pane.getRowIndex(node);
            if(colI == null || rowI == null || colI < 1 || rowI < 1) {
                if(!enemy)
                node.addEventFilter(MouseEvent.MOUSE_ENTERED, new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent mouseEvent) {
                        paintGameField(game.getGameField(), gamefieldpane, false);
                    }
                });
                continue;
            }
            int row = rowI - 1;
            int col = colI - 1;
            Pane nodepane = (Pane) node;
            if(!enemy)
                gameFieldNodes[row][col] = nodepane;
            nodepane.getChildren().clear();
            Label label = new Label();
            label.setText("");
            label.setFont(font);
            label.setStyle("-fx-text-fill: black");
            nodepane.getChildren().add(label);
            label.layoutXProperty().bind(nodepane.widthProperty().subtract(label.widthProperty()).divide(2));
            label.layoutYProperty().bind(nodepane.heightProperty().subtract(label.heightProperty()).divide(2));
            nodepane.addEventFilter(MouseEvent.MOUSE_PRESSED, new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent mouseEvent) {
                    Coordinate coord = new Coordinate(col, row);
                    if (game.isReady()) {
                        if (enemy) {
                            Entity entity = game.getEnemyGameField().getEntityAt(coord);
                            if (entity != null && !entity.isHit())
                                game.getEnemyGameField().shoot(coord);
                        }
                    } else if (!enemy) {
                        System.out.println("Placing Ship " + selectedShipIndex + " " + shipDirection);
                        if (selectedShipIndex != -1 && shipDirection != null) {
                            ShipPart selectedShip = game.getGameField().getRemainingShipsToPlace().get(selectedShipIndex);
                            if (selectedShip != null && game.getGameField().addShip(coord, selectedShip, shipDirection)) {
                                game.getGameField().getRemainingShipsToPlace().set(selectedShipIndex, null);
                                selectedShipIndex = -1;
                                shipDirection = null;
                                paintAvailableShips();
                                repaintGamefields();
                            }
                        }

                    }
                }
            });
            nodepane.addEventFilter(MouseEvent.MOUSE_ENTERED, new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent mouseEvent) {
                    if (enemy || selectedShipIndex < 0) {
                        return;
                    }
                    ShipPart selectedShip = game.getGameField().getRemainingShipsToPlace().get(selectedShipIndex);
                    if (selectedShip == null) {
                        return;
                    }
                    int length = selectedShip.getShipLength();
                    paintGameField(game.getGameField(), pane, !enemy);
                    if (shipDirection == ShipDirection.EAST) {
                        for (int i = 0; i < length; i++) {
                            if (col + i >= 10) {
                                break;
                            }
                           // System.out.println("row: " + row + "col: " + col);
                            if(length + col <= 10 && game.getGameField().canAddShip(new Coordinate(col, row), selectedShip, shipDirection)) {
                                gameFieldNodes[row][col + i].setStyle("-fx-background-color: lightgreen");
                            }else{
                                gameFieldNodes[row][col + i].setStyle("-fx-background-color: #f05959");
                            }
                        }
                    } else {
                        for (int i = 0; i < length; i++) {
                            if (row + i >= 10) {
                                break;
                            }
                            if(length + row <= 10 && game.getGameField().canAddShip(new Coordinate(col, row), selectedShip, shipDirection)) {
                                gameFieldNodes[row + i][col].setStyle("-fx-background-color: lightgreen");
                            }else{
                                gameFieldNodes[row + i][col].setStyle("-fx-background-color: #f05959");
                            }
                        }
                    }
                }
            });
        }
    }

    /** This method paints all the available ships */
    public void paintAvailableShips() {
        shipGridHorizontal.getChildren().clear();
        shipGridVertical.getChildren().clear();
        int row = 0;
        int col = 0;
        List<ShipPart> parts = ShipCollection.getAvailableShips();
        for (int i = 0; i < parts.size(); i++) {
            ShipPart part = parts.get(i);
            boolean available = game.getGameField().getRemainingShipsToPlace().get(i) != null;
            addShipToPane(shipGridHorizontal, row, col, part, true, available, i);
            addShipToPane(shipGridVertical, row, col, part, false, available, i);
            row++;
            if (row >= 5) {
                row = 0;
                col++;
            }
        }
    }

    /**
     * This method add all ships to the given pane
     *
     * @param parent
     * @param row
     * @param col
     * @param shipPart
     * @param horizontal
     * @param available
     * @param index
     */
    private void addShipToPane(GridPane parent, int row, int col, ShipPart shipPart, boolean horizontal, boolean available, int index) {
        Pane pane = new Pane();
        parent.getChildren().add(pane);
        GridPane.setColumnIndex(pane, col);
        GridPane.setRowIndex(pane, row);

        GridPane grid = new GridPane();
        RowConstraints rowConstraints = new RowConstraints();
        rowConstraints.setMaxHeight(20);
        rowConstraints.setMinHeight(20);
        grid.getRowConstraints().add(rowConstraints);
        pane.getChildren().add(grid);
        for (int i = 0; i < shipPart.getShipLength(); i++) {
            ColumnConstraints columnConstraints = new ColumnConstraints();
            columnConstraints.setMaxWidth(20);
            columnConstraints.setMinWidth(20);
            grid.getColumnConstraints().add(columnConstraints);
        }
        for (int i = 0; i < shipPart.getShipLength(); i++) {
            Pane shipTile = new Pane();
            grid.getChildren().add(shipTile);
            GridPane.setRowIndex(shipTile, 0);
            GridPane.setColumnIndex(shipTile, i);
            if(darkMode){
                if(rdVibrantMode.isSelected()){
                    shipTile.setStyle("-fx-background-color: " + (selectedShipIndex == index && (horizontal == (shipDirection == ShipDirection.EAST)) ? "#8dff84" : (available ? "gray" : "lightgray")));
                }else{
                    shipTile.setStyle("-fx-background-color: " + (selectedShipIndex == index && (horizontal == (shipDirection == ShipDirection.EAST)) ? "#089f23" : (available ? "gray" : "lightgray")));
                }
            }else {
                shipTile.setStyle("-fx-background-color: " + (selectedShipIndex == index && (horizontal == (shipDirection == ShipDirection.EAST)) ? "#56d154" : (available ? "gray" : "lightgray")));
            }
            shipTile.resize(20,20);
        }
        grid.setGridLinesVisible(true);

        grid.addEventFilter(MouseEvent.MOUSE_PRESSED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                if (!available || (selectedShipIndex == index && (horizontal ? ShipDirection.EAST : ShipDirection.SOUTH) == shipDirection)) {
                    selectedShipIndex = -1;
                    shipDirection = null;
                } else {
                    selectedShipIndex = index;
                    shipDirection = horizontal ? ShipDirection.EAST : ShipDirection.SOUTH;
                }
                paintAvailableShips();
            }
        });
    }

    /** This method paints the gameField */
    private void paintGameField(GameField field, GridPane pane, boolean enemy){
        int col = 0;
        int row = 0;
        Integer colI, rowI;
        Coordinate coordinate = new Coordinate(col, row);
        for(Node node : pane.getChildren()) {
            if (node == null) continue;
            colI = pane.getColumnIndex(node);
            rowI = pane.getRowIndex(node);
            col = (colI == null ? 0 : colI) - 1;
            row = (rowI == null ? 0 : rowI) - 1;
            coordinate.setCoordinate(col, row);
            if(!(node instanceof Pane))continue;
            Pane nodepane = (Pane) node;
            if (nodepane.getChildren().size() <= 0)
                continue;
            Label label = (Label) nodepane.getChildren().get(0);
            Entity entity = field.getEntityAt(coordinate);
            if (entity == null){
                continue;
            }
            label.setText(entity.isHit() ? "X":"");
            label.layoutXProperty().bind(nodepane.widthProperty().subtract(label.widthProperty()).divide(2));
            label.layoutYProperty().bind(nodepane.heightProperty().subtract(label.heightProperty()).divide(2));

            if(entity instanceof Water){
                if(darkMode) {
                    if (rdVibrantMode.isSelected()){
                        nodepane.setStyle("-fx-background-color:#85f7ff");//blau
                    }else {
                        nodepane.setStyle("-fx-background-color:#0093c2");//blau
                    }
                }else {
                    nodepane.setStyle("-fx-background-color:#47c4f5");//blau
                }
            }else if(entity instanceof ShipPart){
                ShipPart part = (ShipPart) entity;
                Ship ship = part.getParent();
                if(ship != null && ship.isSunk()){
                    if(darkMode){
                        if (rdVibrantMode.isSelected()){
                            nodepane.setStyle("-fx-background-color:#ff7369");//rot
                        }else {
                            nodepane.setStyle("-fx-background-color: #ad0017");//rot
                        }
                    }else {
                        nodepane.setStyle("-fx-background-color: #e63e3e");//rot
                    }
                }else if((ship != null && ship.isHit()) || part.isHit()){
                    if(darkMode){
                        if (rdVibrantMode.isSelected()){
                            nodepane.setStyle("-fx-background-color:#ffeb6b");//orange
                        }else {
                            nodepane.setStyle("-fx-background-color: #be8900");//orange
                        }
                    }else {
                        nodepane.setStyle("-fx-background-color:#f5b938");//orange
                    }
                }else{
                    if(darkMode){
                        if (rdVibrantMode.isSelected()){
                            nodepane.setStyle("-fx-background-color:#8dff84");//gruen
                        }else {
                            nodepane.setStyle("-fx-background-color: #089f23");//gruen
                        }
                    }else {
                        nodepane.setStyle("-fx-background-color:#56d154");//gruen
                    }
                }
            } else {
                nodepane.setStyle("-fx-background-color:#ffffff");//weiß
            }
        }
    }
}