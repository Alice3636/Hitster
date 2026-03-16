package com.hitster.demo;

import com.hitster.model.GameSession;
import com.hitster.model.Player;
import com.hitster.model.Room;
import com.hitster.model.Song;
import com.hitster.model.TurnAction;
import com.hitster.model.TurnResult;
import com.hitster.service.GameManager;
import com.hitster.service.LobbyManager;
import com.hitster.dto.GameStateDTO;
import com.hitster.mapper.GameStateMapper;
import java.util.ArrayList;
import java.util.List;

public class GameSimulation {

    public static void main(String[] args) {

        Player p1 = new Player("1", "Alice");
        Player p2 = new Player("2", "Bob");

        LobbyManager lobbyManager = new LobbyManager();
        GameManager gameManager = new GameManager();

        Room room = lobbyManager.matchPlayerToRoom(p1);
        room = lobbyManager.matchPlayerToRoom(p2);

        System.out.println("Room matched:");
        System.out.println(room);

        List<Song> songs = new ArrayList<>();

        songs.add(new Song("1", "Song A", "Artist A", 1990, ""));
        songs.add(new Song("2", "Song B", "Artist B", 1995, ""));
        songs.add(new Song("3", "Song C", "Artist C", 2000, ""));
        songs.add(new Song("4", "Song D", "Artist D", 2010, ""));
        songs.add(new Song("5", "Song E", "Artist E", 2020, ""));

        GameSession session = gameManager.startGameForRoom(room, songs);

        System.out.println("Game started!");
        System.out.println("Game ID: " + session.getId());
        System.out.println("Current player: " + session.getCurrentTurnPlayer().getUsername());
        System.out.println("Current song: " + session.getCurrentSong().getTitle());
        System.out.println("Room started: " + room.isStarted());

        TurnAction action1 = new TurnAction(
                1,
                session.getCurrentSong().getTitle(),
                session.getCurrentSong().getArtist()
        );

        TurnResult result1 = gameManager.submitTurn(session.getId(), action1);

        System.out.println("First turn result:");
        System.out.println(result1);

        System.out.println("Next player: " + session.getCurrentTurnPlayer().getUsername());
        System.out.println("Alice score: " + p1.getScore() + ", tokens: " + p1.getTokens());
        System.out.println("Bob score: " + p2.getScore() + ", tokens: " + p2.getTokens());
        System.out.println("Next current song: " + session.getCurrentSong().getTitle());

        TurnAction action2 = new TurnAction(
                1,
                session.getCurrentSong().getTitle(),
                session.getCurrentSong().getArtist()
        );

        TurnResult result2 = gameManager.submitTurn(session.getId(), action2);

        System.out.println("Second turn result:");
        System.out.println(result2);

        System.out.println("Next player after Bob: " + session.getCurrentTurnPlayer().getUsername());
        System.out.println("Alice score: " + p1.getScore() + ", tokens: " + p1.getTokens());
        System.out.println("Bob score: " + p2.getScore() + ", tokens: " + p2.getTokens());
        System.out.println("Next current song: " + session.getCurrentSong().getTitle());

        GameStateDTO dto = GameStateMapper.toDTO(session);

        System.out.println("Game state DTO:");
        System.out.println("Game ID: " + dto.getGameId());
        System.out.println("Current player: " + dto.getCurrentPlayer());
        System.out.println("Current song: " + dto.getCurrentSongTitle());
        System.out.println("Player1 score: " + dto.getPlayer1Score());
        System.out.println("Player2 score: " + dto.getPlayer2Score());
    }
}