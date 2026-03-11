package com.hitster.service;

import org.springframework.stereotype.Service;

import com.hitster.engine.GameEngine;
import com.hitster.model.GameSession;
import com.hitster.model.Player;
import com.hitster.model.Room;
import com.hitster.model.Song;
import com.hitster.model.TurnAction;
import com.hitster.model.TurnResult;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class GameManager {

    private final Map<String, GameSession> activeGames = new HashMap<>();
    private final GameEngine gameEngine = new GameEngine();

    public GameSession startGameForRoom(Room room, List<Song> songsPool) {
        if (room == null) {
            throw new IllegalArgumentException("Room cannot be null.");
        }

        if (!room.isFull()) {
            throw new IllegalStateException("Cannot start game: room is not full.");
        }

        Player player1 = room.getPlayer1();
        Player player2 = room.getPlayer2();

        String gameId = UUID.randomUUID().toString();

        GameSession session = new GameSession(
                gameId,
                player1,
                player2,
                songsPool
        );

        gameEngine.startGame(session);
        room.startGame(session);
        activeGames.put(gameId, session);

        return session;
    }

    public GameSession getGameById(String gameId) {
        return activeGames.get(gameId);
    }

    public TurnResult submitTurn(String gameId, TurnAction action) {
        GameSession session = activeGames.get(gameId);

        if (session == null) {
            throw new IllegalArgumentException("Game not found: " + gameId);
        }

        return gameEngine.submitTurn(session, action);
    }

    public boolean challengeLastTurn(String gameId, String challengerId) {
        GameSession session = activeGames.get(gameId);

        if (session == null) {
            throw new IllegalArgumentException("Game not found: " + gameId);
        }

        Player challenger = session.getPlayerById(challengerId);

        if (challenger == null) {
            throw new IllegalArgumentException("Player not found: " + challengerId);
        }

        return gameEngine.challengeLastTurn(session, challenger);
    }
    public void skipChallengeWindow(String gameId) {
        GameSession session = activeGames.get(gameId);

        if (session == null) {
            throw new IllegalArgumentException("Game not found: " + gameId);
        }

        gameEngine.skipChallengeWindow(session);
    }
}