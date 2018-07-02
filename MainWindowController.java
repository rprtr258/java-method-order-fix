package com.rprtr258.client;

import com.rprtr258.network.Client;
import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

public class MainWindowController {
    public Button button00;
    public Button button01;
    public Button button02;
    public Button button10;
    public Button button11;
    public Button button12;
    public Button button20;
    public Button button21;
    public Button button22;
    public Label gameStatusLabel;
    public Label playerNameLabel;
    public Button restartButton;

    private String mark = null;
    private Button buttons[][] = null;
    private boolean isWaitingForOpponentTurn = false;
    private Client thisClient = null;

    /**
     * Initializes game window elements with init state and listeners.
     */
    public void initialize() {
        buttons = new Button[][]{{button00, button01, button02},
                                 {button10, button11, button12},
                                 {button20, button21, button22}};
        for (int i = 0; i < buttons.length; i++) {
            for (int j = 0; j < buttons[i].length; j++) {
                final int finalI = i;
                final int finalJ = j;
                buttons[i][j].setOnAction((actionEvent) -> makeMove(finalI, finalJ));
            }
        }
        restartButton.setOnAction((actionEvent) -> onRestartRequest());
        playerNameLabel.setText("");
        setButtonsDisable(true);
        restartButton.setDisable(true);
    }

    /**
     * Configures controller with required objects.
     *
     * @param playerName player name(mark).
     * @param client connected client.
     */
    public void configure(String playerName, Client client) {
        this.thisClient = client;
        thisClient.configure(() -> Platform.runLater(this::onLostServerConnection), () -> Platform.runLater(this::onDisconnect));
        mark = playerName;
        playerNameLabel.setText("You are player " + playerName);
        setButtonsDisable(false);
        if ("O".equals(mark)) {
            onTurnWaiting();
        } else {
            gameStatusLabel.setText("Waiting for your turn.");
        }
    }

    /**
     * Makes turn in game.
     *
     * @param row row coordinate of cell.
     * @param column column coordinate of cell.
     */
    private void makeMove(int row, int column) {
        if (isWaitingForOpponentTurn)
            return;
        thisClient.makeMove(row, column, () -> Platform.runLater(() -> onSuccessTurn(row, column)),
                                         () -> Platform.runLater(this::onGameContinuing),
                                         (winner) -> Platform.runLater(() -> this.onGameEnd(winner)));
    }

    /**
     * Called when turn was made successfully.
     *
     * @param row row coordinate of cell.
     * @param column column coordinate of cell.
     */
    private void onSuccessTurn(int row, int column) {
        setButtonText(row, column, mark);
    }

    /**
     * Called when game is continuing after turn.
     */
    private void onGameContinuing() {
        onTurnWaiting();
    }

    /**
     * Called when player has to wait opponent turn.
     */
    private void onTurnWaiting() {
        gameStatusLabel.setText("Waiting for " + ("X".equals(mark) ? "O" : "X") + "'s turn.");
        isWaitingForOpponentTurn = true;
        thisClient.waitGameChanges((i, j) -> Platform.runLater(() -> this.onOpponentTurn(i, j)),
                                   (winner) -> Platform.runLater(() -> this.onGameEnd(winner)));
    }

    /**
     * Called when opponent turn was made.
     *
     * @param row row coordinate of cell.
     * @param column column coordinate of cell.
     */
    private void onOpponentTurn(int row, int column) {
        isWaitingForOpponentTurn = false;
        setButtonText(row, column, "X".equals(mark) ? "O" : "X");
        gameStatusLabel.setText("Waiting for your turn.");
    }

    /**
     * Called when game ended.
     *
     * @param winner game winner.
     */
    private void onGameEnd(String winner) {
        if ("draw".equals(winner)) {
            gameStatusLabel.setText("Draw!");
        } else if (mark.equals(winner)) {
            gameStatusLabel.setText("You won!");
        } else {
            gameStatusLabel.setText("You lost!");
        }
        restartButton.setDisable(false);
    }

    /**
     * Called when requested restart.
     */
    private void onRestartRequest() {
        for (Button[] buttonsRow : buttons) {
            for (Button button : buttonsRow) {
                button.setText("");
            }
        }
        playerNameLabel.setText("");
        gameStatusLabel.setText("Waiting for other player to confirm");
        setButtonsDisable(true);
        restartButton.setDisable(true);
        thisClient.restart((playerName) -> Platform.runLater(() -> this.onRestart(playerName)));
    }

    /**
     * Called when game restarted.
     *
     * @param playerName new player name(mark) given by server.
     */
    private void onRestart(String playerName) {
        mark = playerName;
        playerNameLabel.setText("You are player " + playerName);
        setButtonsDisable(false);
        if ("O".equals(mark)) {
            onTurnWaiting();
        } else {
            gameStatusLabel.setText("Waiting for your turn.");
        }
    }

    /**
     * Called when opponent disconnected.
     */
    private void onDisconnect() {
        gameStatusLabel.setText("Opponent has disconnected");
        setButtonsDisable(true);
        restartButton.setDisable(true);
    }

    /**
     * Called when lost connection to server.
     */
    private void onLostServerConnection() {
        setButtonsDisable(true);
        gameStatusLabel.setText("Lost connection to server :C");
    }

    /**
     * Sets button text.
     *
     * @param i row coordinate of button.
     * @param j column coordinate of button.
     * @param s string to set in button.
     */
    private void setButtonText(int i, int j, String s) {
        buttons[i][j].setText(s);
    }

    /**
     * Sets disable status on game buttons.
     *
     * @param value new disable status.
     */
    private void setButtonsDisable(boolean value) {
        for (Button[] buttonsRow : buttons)
            for (Button button : buttonsRow)
                button.setDisable(value);
    }
}
