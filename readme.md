# QPUC

## Features

### Home

One can create a game and become the game master, which generate a unique identifier.
You can choose the number of players.
TODO: You can join back as a game master if you are no longer in.

One can join an existing game as a player, from its unique identifier.
You can't join a game that is full.
You can join a game with the same name as an already registered player though, which enable teamwork in a way.

TODO: You can join as a spectator ?

### Lobby

Game master and players wait in a lobby before a question starts.
Everybody sees everyone name and scores.
TODO: The game master can kick players.
TODO: The players & the game master can leave and go back to home

Qualified players, whose score is above 9, are highlighted, and no longer participate to questions.
The game end when 3 players are qualified.

The game master can start a new question.
The points awarded for each question are computed as follow :
When no qualified players, alternatively 1, 2 or 3 points.
When one qualified player, alternatively 2 or 3 points.
When two qualified players, 3 points.

### Question

When a question starts, every players is shown a deactivated buzzer.
The game master is shown a top button.
When clicking on the top button, the question timer starts for 20 secondes, and each player buzzer is lit and enable.
When a player buzz, a player timer of 4 secondes starts, and the game master has two button to validate or invalidate its answer.
TODO : Do not pause the global timer when a player is answering
If the 4 seconds timer runs out before the game master has process the answer, he can still do it.

If the answer is correct, everyone is driven back to the lobby.
If not, and there is time remaining, all the other players can buzz again.
If the time runs out, everyone is driven back to the lobby, with no points awarded.


