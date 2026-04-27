package com.hitster.service;

import com.hitster.model.Player;
import com.hitster.model.Room;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class LobbyManager {
    private final Map<String, Room> rooms = new HashMap<>();
    private final Map<String, String> playerToRoom = new HashMap<>();

    public synchronized Room createRoom(Player hostPlayer) {
        cleanupFinishedRooms();

        String roomId = UUID.randomUUID().toString();
        Room room = new Room(roomId, hostPlayer);

        rooms.put(roomId, room);
        playerToRoom.put(hostPlayer.getId(), roomId);

        return room;
    }

    public synchronized Room getRoomById(String roomId) {
        cleanupFinishedRooms();
        return rooms.get(roomId);
    }

    public synchronized Collection<Room> getAllRooms() {
        cleanupFinishedRooms();
        return new ArrayList<>(rooms.values());
    }

    public synchronized Room findWaitingRoom() {
        cleanupFinishedRooms();

        for (Room room : rooms.values()) {
            if (!room.isStarted() && !room.isFull()) {
                return room;
            }
        }

        return null;
    }

    public synchronized boolean joinRoom(String roomId, Player player) {
        cleanupFinishedRooms();

        Room room = rooms.get(roomId);
        if (room == null || room.isStarted()) {
            return false;
        }

        boolean joined = room.addPlayer(player);
        if (joined) {
            playerToRoom.put(player.getId(), roomId);
        }

        return joined;
    }

    public synchronized Room matchPlayerToRoom(Player player) {
        cleanupFinishedRooms();

        Room existingRoom = getRoomByPlayerIdInternal(player.getId());

        if (existingRoom != null) {
            return existingRoom;
        }

        Room waitingRoom = findWaitingRoomInternal();

        if (waitingRoom != null) {
            boolean joined = waitingRoom.addPlayer(player);
            if (joined) {
                playerToRoom.put(player.getId(), waitingRoom.getId());
                return waitingRoom;
            }
        }

        String roomId = UUID.randomUUID().toString();
        Room newRoom = new Room(roomId, player);

        rooms.put(roomId, newRoom);
        playerToRoom.put(player.getId(), roomId);

        return newRoom;
    }

    public synchronized boolean removeRoom(String roomId) {
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

    public synchronized Room getRoomByPlayerId(String playerId) {
        cleanupFinishedRooms();
        return getRoomByPlayerIdInternal(playerId);
    }

    public synchronized boolean leaveLobby(String playerId) {
        cleanupFinishedRooms();

        Room room = getRoomByPlayerIdInternal(playerId);

        if (room == null || room.isStarted()) {
            return false;
        }

        boolean isPlayer1 = room.getPlayer1() != null && room.getPlayer1().getId().equals(playerId);
        boolean isPlayer2 = room.getPlayer2() != null && room.getPlayer2().getId().equals(playerId);

        if (!isPlayer1 && !isPlayer2) {
            return false;
        }

        playerToRoom.remove(playerId);

        if (isPlayer2) {
            Room newRoom = new Room(room.getId(), room.getPlayer1());
            rooms.put(room.getId(), newRoom);
            playerToRoom.put(room.getPlayer1().getId(), room.getId());
            return true;
        }

        if (isPlayer1 && room.getPlayer2() != null) {
            Player remaining = room.getPlayer2();
            Room newRoom = new Room(room.getId(), remaining);
            rooms.put(room.getId(), newRoom);
            playerToRoom.put(remaining.getId(), room.getId());
            return true;
        }

        rooms.remove(room.getId());
        return true;
    }

    private Room getRoomByPlayerIdInternal(String playerId) {
        String roomId = playerToRoom.get(playerId);
        if (roomId == null) {
            return null;
        }

        Room room = rooms.get(roomId);
        if (room == null) {
            playerToRoom.remove(playerId);
            return null;
        }

        return room;
    }

    private Room findWaitingRoomInternal() {
        for (Room room : rooms.values()) {
            if (!room.isStarted() && !room.isFull()) {
                return room;
            }
        }

        return null;
    }

    private void cleanupFinishedRooms() {
        ArrayList<String> finishedRoomIds = new ArrayList<>();

        for (Room room : rooms.values()) {
            if (isFinishedRoom(room)) {
                finishedRoomIds.add(room.getId());
            }
        }

        for (String roomId : finishedRoomIds) {
            removeRoomInternal(roomId);
        }
    }

    private void removeRoomInternal(String roomId) {
        Room room = rooms.remove(roomId);
        if (room == null) {
            return;
        }

        if (room.getPlayer1() != null) {
            playerToRoom.remove(room.getPlayer1().getId());
        }

        if (room.getPlayer2() != null) {
            playerToRoom.remove(room.getPlayer2().getId());
        }
    }

    private boolean isFinishedRoom(Room room) {
        return room != null
                && room.isStarted()
                && room.getGameSession() != null
                && room.getGameSession().isFinished();
    }
}