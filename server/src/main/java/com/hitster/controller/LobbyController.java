package com.hitster.controller;

import com.hitster.dto.LobbyJoinRequest;
import com.hitster.dto.LobbyJoinResponse;
import com.hitster.model.GameSession;
import com.hitster.model.Player;
import com.hitster.model.Room;
import com.hitster.model.Song;
import com.hitster.service.GameManager;
import com.hitster.service.LobbyManager;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/lobby")
public class LobbyController {

    private final LobbyManager lobbyManager;
    private final GameManager gameManager;

    public LobbyController(LobbyManager lobbyManager, GameManager gameManager) {
        this.lobbyManager = lobbyManager;
        this.gameManager = gameManager;
    }

    @PostMapping("/join")
    public LobbyJoinResponse joinLobby(@RequestBody LobbyJoinRequest request) {
        Player player = new Player(request.getPlayerId(), request.getUsername());

        Room room = lobbyManager.matchPlayerToRoom(player);

        if (room.isFull() && !room.isStarted()) {
            List<Song> songs = createSongsPool();
            GameSession session = gameManager.startGameForRoom(room, songs);

            return new LobbyJoinResponse(
                    room.getId(),
                    true,
                    true,
                    session.getId(),
                    "Room is full. Game started.");
        }

        return new LobbyJoinResponse(
                room.getId(),
                false,
                false,
                -1,
                "Joined waiting room. Waiting for another player.");
    }

    @GetMapping("/room/{roomId}")
    public LobbyJoinResponse getRoomStatus(@PathVariable int roomId) {
        Room room = lobbyManager.getRoomById(roomId);

        if (room == null) {
            throw new IllegalArgumentException("Room not found: " + roomId);
        }

        GameSession session = room.getGameSession();

        return new LobbyJoinResponse(
                room.getId(),
                room.isFull(),
                room.isStarted(),
                session != null ? session.getId() : -1,
                room.isStarted() ? "Game started." : "Waiting for another player.");
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