package com.hitster.service;

import com.hitster.model.Player;
import com.hitster.model.Room;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.stereotype.Service;

@Service
public class LobbyManager {
    private final Map<Integer, Room> rooms = new HashMap<>();

    private final AtomicInteger roomCounter = new AtomicInteger(1);

    public Room createRoom(Player hostPlayer) {
        int roomId = roomCounter.getAndIncrement();
        Room room = new Room(roomId, hostPlayer);
        rooms.put(roomId, room);
        return room;
    }

    public Room getRoomById(int roomId) {
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

    public boolean joinRoom(int roomId, Player player) {
        Room room = rooms.get(roomId);
        if (room == null || room.isStarted()) {
            return false;
        }
        return room.addPlayer(player);
    }

    public Room matchPlayerToRoom(Player player) {
        Room waitingRoom = findWaitingRoom();

        if (waitingRoom != null) {
            boolean joined = waitingRoom.addPlayer(player);
            if (joined) {
                return waitingRoom;
            }
        }

        return createRoom(player);
    }

    public boolean removeRoom(int roomId) {
        return rooms.remove(roomId) != null;
    }
}