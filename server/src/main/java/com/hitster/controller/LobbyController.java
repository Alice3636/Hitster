package com.hitster.controller;

import com.hitster.dto.lobby.LobbyStatusResponseDTO;
import com.hitster.model.Player;
import com.hitster.model.Room;
import com.hitster.model.Song;
import com.hitster.service.GameManager;
import com.hitster.service.LobbyManager;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/lobby")
public class LobbyController {

    private final LobbyManager lobbyManager;
    private final GameManager gameManager;

    public LobbyController(LobbyManager lobbyManager, GameManager gameManager) {
        this.lobbyManager = lobbyManager;
        this.gameManager = gameManager;
    }

    @PostMapping("/join")
    public String joinLobby(HttpServletRequest httpRequest) {
        Object jwtUserIdObj = httpRequest.getAttribute("jwtUserId");
        Object jwtUsernameObj = httpRequest.getAttribute("jwtUsername");

        if (jwtUserIdObj == null || jwtUsernameObj == null) {
            throw new IllegalArgumentException("Missing authenticated user.");
        }

        String playerId = String.valueOf(jwtUserIdObj);
        String username = String.valueOf(jwtUsernameObj);

        Player player = new Player(playerId, username);
        Room room = lobbyManager.matchPlayerToRoom(player);

        if (room.isFull() && !room.isStarted()) {
            List<Song> songs = createSongsPool();
            gameManager.startGameForRoom(room, songs);
            return "Game started";
        }

        return "Joined waiting room";
    }

    @GetMapping("/status")
    public LobbyStatusResponseDTO getLobbyStatus(HttpServletRequest request) {
        Object jwtUserIdObj = request.getAttribute("jwtUserId");

        if (jwtUserIdObj == null) {
            throw new IllegalArgumentException("Missing authenticated user.");
        }

        String playerId = String.valueOf(jwtUserIdObj);
        Room room = lobbyManager.getRoomByPlayerId(playerId);

        if (room == null) {
            return new LobbyStatusResponseDTO("NOT_FOUND", null);
        }

        if (room.isStarted() && room.getGameSession() != null) {
            String gameId = room.getGameSession().getId();
            return new LobbyStatusResponseDTO("FOUND", gameId);
        }

        return new LobbyStatusResponseDTO("WAITING", null);
    }

    @PostMapping("/leave")
    public String leaveLobby(HttpServletRequest request) {
        Object jwtUserIdObj = request.getAttribute("jwtUserId");

        if (jwtUserIdObj == null) {
            throw new IllegalArgumentException("Missing authenticated user.");
        }

        String playerId = String.valueOf(jwtUserIdObj);
        boolean left = lobbyManager.leaveLobby(playerId);

        if (!left) {
            throw new IllegalArgumentException("Player is not in a waiting lobby.");
        }

        return "OK";
    }

    private List<Song> createSongsPool() {
        List<Song> songs = new ArrayList<>();
        songs.add(new Song("1", "Song A", "Artist A", 1990, ""));
        songs.add(new Song("2", "Song B", "Artist B", 1995, ""));
        songs.add(new Song("3", "Song C", "Artist C", 2000, ""));
        songs.add(new Song("4", "Song D", "Artist D", 2010, ""));
        songs.add(new Song("5", "Song E", "Artist E", 2020, ""));
        songs.add(new Song("6", "Song F", "Artist F", 1980, ""));
        songs.add(new Song("7", "Song G", "Artist G", 2005, ""));
        return songs;
    }
}