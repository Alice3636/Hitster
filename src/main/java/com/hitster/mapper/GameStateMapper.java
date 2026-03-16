package com.hitster.mapper;

import com.hitster.dto.GameStateDTO;
import com.hitster.model.GameSession;
import com.hitster.model.Player;
import com.hitster.model.SongCard;

import java.util.ArrayList;
import java.util.List;

public class GameStateMapper {

    public static GameStateDTO toDTO(GameSession session) {
        Player p1 = session.getPlayer1();
        Player p2 = session.getPlayer2();

        return new GameStateDTO(
                session.getId(),
                session.getStatus() != null ? session.getStatus().name() : null,
                session.getTurnNumber(),
                session.getCurrentTurnPlayer() != null ? session.getCurrentTurnPlayer().getUsername() : null,
                session.getCurrentSong() != null ? session.getCurrentSong().getTitle() : null,
                session.getWinner() != null ? session.getWinner().getUsername() : null,
                p1.getUsername(),
                p2.getUsername(),
                p1.getScore(),
                p2.getScore(),
                p1.getTokens(),
                p2.getTokens(),
                toTimelineStrings(session.getPlayer1Timeline()),
                toTimelineStrings(session.getPlayer2Timeline())
        );
    }

    private static List<String> toTimelineStrings(List<SongCard> timeline) {
        List<String> result = new ArrayList<>();

        for (SongCard card : timeline) {
            result.add(
                    card.getSong().getYear() +
                            " - " +
                            card.getSong().getTitle() +
                            " - " +
                            card.getSong().getArtist()
            );
        }

        return result;
    }
}