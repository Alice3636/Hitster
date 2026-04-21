package com.hitster.service;

import com.hitster.controller.NotFoundException;
import com.hitster.dto.game.GuessSongRequestDTO;
import com.hitster.dto.game.PlaceSongRequestDTO;
import com.hitster.engine.GameEngine;
import com.hitster.model.GamePhase;
import com.hitster.model.GameSession;
import com.hitster.model.GameStatus;
import com.hitster.model.Player;
import com.hitster.model.Room;
import com.hitster.model.Song;
import com.hitster.model.TurnResult;
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
        return activeGames.get(gameId);
    }

    public TurnResult placeSong(String gameId, String playerId, PlaceSongRequestDTO request) {
        GameSession session = activeGames.get(gameId);

        if (session == null) {
            throw new NotFoundException("Game not found: " + gameId);
        }

        return gameEngine.placeSong(
                session,
                playerId,
                request.indexPosition(), // תיקון שמות השדות ב-Record
                request.songId()
        );
    }

    public TurnResult guessSong(String gameId, String playerId, GuessSongRequestDTO request) {
        GameSession session = activeGames.get(gameId);

        if (session == null) {
            throw new NotFoundException("Game not found: " + gameId);
        }

        return gameEngine.guessSong(
                session,
                playerId,
                request.artist(),
                request.songName() // תיקון שמות השדות ב-Record
        );
    }

    public boolean challengeLastTurn(String gameId, String challengerId, Integer suggestedIndex) {
        GameSession session = activeGames.get(gameId);

        if (session == null) {
            throw new NotFoundException("Game not found: " + gameId);
        }

        Player challenger = session.getPlayerById(challengerId);

        if (challenger == null) {
            throw new NotFoundException("Player not found: " + challengerId);
        }

        return gameEngine.challengeLastTurn(session, challenger, suggestedIndex);
    }

    public void skipChallengeWindow(String gameId) {
        GameSession session = activeGames.get(gameId);

        if (session == null) {
            throw new NotFoundException("Game not found: " + gameId);
        }

        gameEngine.skipChallengeWindow(session);
    }

    public GameSession quitGame(String gameId, String playerId) {
        GameSession session = activeGames.get(gameId);

        if (session == null) {
            throw new NotFoundException("Game not found: " + gameId);
        }

        Player quitter = session.getPlayerById(playerId);

        if (quitter == null) {
            throw new NotFoundException("Player not found: " + playerId);
        }

        Player winner = session.getOpponent(quitter);
        session.setWinner(winner);
        session.setStatus(GameStatus.FINISHED);
        session.setPhase(GamePhase.GAME_FINISHED);

        return session;
    }
}