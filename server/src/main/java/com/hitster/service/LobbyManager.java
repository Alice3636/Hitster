package com.hitster.service;

import com.hitster.model.Player;
import com.hitster.model.Room;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class LobbyManager {
    private final Map<String, Room> rooms = new HashMap<>();
    private final Map<String, String> playerToRoom = new HashMap<>();

    public Room createRoom(Player hostPlayer) {
        String roomId = UUID.randomUUID().toString();
        Room room = new Room(roomId, hostPlayer);
        rooms.put(roomId, room);
        playerToRoom.put(hostPlayer.getId(), roomId);
        return room;
    }

    public Room getRoomById(String roomId) {
        return rooms.get(roomId);
    }

    public Collection<Room> getAllRooms() {
        return rooms.values();
    }

    public Room findWaitingRoom() {
        for (Room room : rooms.values()) {
            if (!room.isStarted() && !room.isFull()) {
                return room;
            }
        }
        return null;
    }

    public boolean joinRoom(String roomId, Player player) {
        Room room = rooms.get(roomId);
        if (room == null) {
            return false;
        }
        if (room.isStarted()) {
            return false;
        }

        boolean joined = room.addPlayer(player);
        if (joined) {
            playerToRoom.put(player.getId(), roomId);
        }
        return joined;
    }

    public Room matchPlayerToRoom(Player player) {
        Room existingRoom = getRoomByPlayerId(player.getId());
        if (existingRoom != null) {
            return existingRoom;
        }

        Room waitingRoom = findWaitingRoom();

        if (waitingRoom != null) {
            boolean joined = waitingRoom.addPlayer(player);
            if (joined) {
                playerToRoom.put(player.getId(), waitingRoom.getId());
                return waitingRoom;
            }
        }

        return createRoom(player);
    }

    public boolean removeRoom(String roomId) {
        Room room = rooms.remove(roomId);
        if (room == null) {
            return false;
        }

        if (room.getPlayer1() != null) {
            playerToRoom.remove(room.getPlayer1().getId());
        }
        if (room.getPlayer2() != null) {
            playerToRoom.remove(room.getPlayer2().getId());
        }

        return true;
    }

    public Room getRoomByPlayerId(String playerId) {
        String roomId = playerToRoom.get(playerId);
        if (roomId == null) {
            return null;
        }
        return rooms.get(roomId);
    }

    public boolean leaveLobby(String playerId) {
        Room room = getRoomByPlayerId(playerId);

        if (room == null) {
            return false;
        }

        if (room.isStarted()) {
            return false;
        }

        boolean isPlayer1 = room.getPlayer1() != null && room.getPlayer1().getId().equals(playerId);
        boolean isPlayer2 = room.getPlayer2() != null && room.getPlayer2().getId().equals(playerId);

        if (!isPlayer1 && !isPlayer2) {
            return false;
        }

        playerToRoom.remove(playerId);

        if (isPlayer2) {
            // player2 leaves, room returns to waiting with player1 only
            Room newRoom = new Room(room.getId(), room.getPlayer1());
            rooms.put(room.getId(), newRoom);
            playerToRoom.put(room.getPlayer1().getId(), room.getId());
            return true;
        }

        if (isPlayer1 && room.getPlayer2() != null) {
            // player1 leaves, keep room with player2 as waiting host
            Player remaining = room.getPlayer2();
            Room newRoom = new Room(room.getId(), remaining);
            rooms.put(room.getId(), newRoom);
            playerToRoom.put(remaining.getId(), room.getId());
            return true;
        }

        // only player1 existed
        rooms.remove(room.getId());
        return true;
    }
}