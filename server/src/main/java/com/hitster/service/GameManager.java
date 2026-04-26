package com.hitster.service;

import com.hitster.controller.NotFoundException;
import com.hitster.dto.game.GuessSongRequestDTO;
import com.hitster.dto.game.PlaceSongRequestDTO;
import com.hitster.engine.GameEngine;
import com.hitster.model.GameSession;
import com.hitster.model.Player;
import com.hitster.model.Room;
import com.hitster.model.Song;
import org.springframework.stereotype.Service;

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
        GameSession session = activeGames.get(gameId);

        if (session != null) {
            gameEngine.advanceIfNeeded(session);
        }

        return session;
    }

    public GameSession placeSong(String gameId, String playerId, PlaceSongRequestDTO request) {
        GameSession session = getRequiredGame(gameId);

        if (request == null) {
            throw new IllegalArgumentException("Request body is required.");
        }

        gameEngine.placeSong(
                session,
                playerId,
                request.indexPosition(),
                request.songId()
        );

        return session;
    }

    public GameSession guessSong(String gameId, String playerId, GuessSongRequestDTO request) {
        GameSession session = getRequiredGame(gameId);

        if (request == null) {
            throw new IllegalArgumentException("Request body is required.");
        }

        gameEngine.submitGuess(
                session,
                playerId,
                request.artist(),
                request.title()
        );

        return session;
    }

    public GameSession challengeLastTurn(String gameId, String challengerId, Integer suggestedIndex) {
        GameSession session = getRequiredGame(gameId);

        Player challenger = session.getPlayerById(challengerId);

        if (challenger == null) {
            throw new NotFoundException("Player not found: " + challengerId);
        }

        gameEngine.challengeLastTurn(session, challenger, suggestedIndex);

        return session;
    }

    public GameSession skipChallengeWindow(String gameId) {
        GameSession session = getRequiredGame(gameId);

        gameEngine.skipChallengeWindow(session);

        return session;
    }

    public GameSession quitGame(String gameId, String playerId) {
        GameSession session = getRequiredGame(gameId);

        Player quitter = session.getPlayerById(playerId);

        if (quitter == null) {
            throw new NotFoundException("Player not found: " + playerId);
        }

        gameEngine.finishByForfeit(session, quitter);

        return session;
    }

    private GameSession getRequiredGame(String gameId) {
        GameSession session = activeGames.get(gameId);

        if (session == null) {
            throw new NotFoundException("Game not found: " + gameId);
        }

        gameEngine.advanceIfNeeded(session);

        return session;
    }
}