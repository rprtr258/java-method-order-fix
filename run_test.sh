g++ main.cpp
./a.exe MainWindowController.java > out
printf "initialize\r\n" > correct
printf "onRestartRequest\r\n" >> correct
printf "onRestart\r\n" >> correct
printf "makeMove\r\n" >> correct
printf "onSuccessTurn\r\n" >> correct
printf "onGameContinuing\r\n" >> correct
printf "configure\r\n" >> correct
printf "onTurnWaiting\r\n" >> correct
printf "onOpponentTurn\r\n" >> correct
printf "setButtonText\r\n" >> correct
printf "onGameEnd\r\n" >> correct
printf "onLostServerConnection\r\n" >> correct
printf "onDisconnect\r\n" >> correct
printf "setButtonsDisable\r\n" >> correct
cmp out correct
rm out correct
rm a.exe
