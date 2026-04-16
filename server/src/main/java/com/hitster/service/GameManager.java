package com.hitster.service;

import org.springframework.stereotype.Service;

import com.hitster.engine.GameEngine;
import com.hitster.model.GameSession;
import com.hitster.model.GameStatus;
import com.hitster.model.Player;
import com.hitster.model.Room;
import com.hitster.model.Song;
import com.hitster.model.TurnAction;
import com.hitster.model.TurnResult;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class GameManager {

    private final Map<Integer, GameSession> activeGames = new HashMap<>();
    private final GameEngine gameEngine = new GameEngine();

    public GameSession startGameForRoom(Room room, List<Song> songsPool) {
        if (room == null || !room.isFull()) {
            throw new IllegalStateException("Cannot start game: room is invalid or not full.");
        }

        Player player1 = room.getPlayer1();
        Player player2 = room.getPlayer2();

        int dbGameId = DatabaseService.startNewGame(player1.getId(), player2.getId(), songsPool.size());

        if (dbGameId <= 0) {
            throw new RuntimeException("Failed to create game in the database.");
        }

        // FIX: Passed the primitive int dbGameId
        GameSession session = new GameSession(
                dbGameId,
                player1,
                player2,
                songsPool);

        gameEngine.startGame(session);
        room.startGame(session);
        activeGames.put(dbGameId, session);

        return session;
    }

    public GameSession getGameById(String gameId) {
        return activeGames.get(Integer.parseInt(gameId));
    }

    public TurnResult submitTurn(String gameId, TurnAction action) {
        int dbGameId = Integer.parseInt(gameId);
        GameSession session = activeGames.get(dbGameId);

        if (session == null) {
            throw new IllegalArgumentException("Game not found: " + gameId);
        }

        Player currentPlayer = session.getCurrentTurnPlayer();
        TurnResult result = gameEngine.submitTurn(session, action);

        int pointsEarned = result.isPlacementCorrect() ? 1 : 0;
        int nextPlayerId = session.getCurrentTurnPlayer().getId();

        DatabaseService.updateScore(
                dbGameId,
                currentPlayer.getId(),
                pointsEarned,
                nextPlayerId);

        if (session.getStatus() == GameStatus.FINISHED) {
            DatabaseService.endGame(dbGameId, session.getWinner().getId(), 50.0);
            activeGames.remove(dbGameId);
        }

        return result;
    }

    public boolean challengeLastTurn(String gameId, String challengerId) {
        GameSession session = activeGames.get(Integer.parseInt(gameId));
        if (session == null) {
            throw new IllegalArgumentException("Game not found: " + gameId);
        }

        Player challenger = session.getPlayerById(Integer.parseInt(challengerId));
        if (challenger == null) {
            throw new IllegalArgumentException("Player not found: " + challengerId);
        }

        return gameEngine.challengeLastTurn(session, challenger);
    }

    public void skipChallengeWindow(String gameId) {
        GameSession session = activeGames.get(Integer.parseInt(gameId));
        if (session == null) {
            throw new IllegalArgumentException("Game not found: " + gameId);
        }
        gameEngine.skipChallengeWindow(session);
    }
}