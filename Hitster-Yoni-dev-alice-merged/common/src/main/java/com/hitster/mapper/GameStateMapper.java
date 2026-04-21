package com.hitster.mapper;

import com.hitster.dto.game.CardDTO;
import com.hitster.dto.game.GameStateDTO;
import com.hitster.dto.game.SongDTO;
import com.hitster.model.GameSession;
import com.hitster.model.Player;
import com.hitster.model.Song;
import com.hitster.model.SongCard;

import java.util.ArrayList;
import java.util.List;

public class GameStateMapper {

    public static GameStateDTO toDTO(GameSession session) {
        Player p1 = session.getPlayer1();
        Player p2 = session.getPlayer2();

        GameStateDTO dto = new GameStateDTO();

        dto.setGameId(session.getId());
        dto.setGameStatus(session.getStatus() != null ? session.getStatus().name() : null);
        dto.setTurnNumber(session.getTurnNumber());
        dto.setTimeLeftSeconds(0);

        dto.setCurrentPlayerId(
                session.getCurrentTurnPlayer() != null
                        ? parseLongOrNull(session.getCurrentTurnPlayer().getId())
                        : null
        );

        dto.setCurrentSong(
                session.getCurrentSong() != null
                        ? toSongDTO(session.getCurrentSong())
                        : null
        );

        dto.setWinnerName(
                session.getWinner() != null
                        ? session.getWinner().getUsername()
                        : null
        );

        dto.setPlayer1Id(parseLongOrNull(p1.getId()));
        dto.setPlayer1Name(p1.getUsername());
        dto.setPlayer1Score(p1.getScore());
        dto.setPlayer1Tokens(p1.getTokens());
        dto.setPlayer1Timeline(toCardDTOList(session.getPlayer1Timeline()));

        dto.setPlayer2Id(parseLongOrNull(p2.getId()));
        dto.setPlayer2Name(p2.getUsername());
        dto.setPlayer2Score(p2.getScore());
        dto.setPlayer2Tokens(p2.getTokens());
        dto.setPlayer2Timeline(toCardDTOList(session.getPlayer2Timeline()));

        return dto;
    }

    private static SongDTO toSongDTO(Song song) {
        return new SongDTO(
                parseLongOrNull(song.getId()),
                song.getTitle(),
                song.getArtist(),
                song.getYear(),
                song.getAudioUrl()
        );
    }

    private static List<CardDTO> toCardDTOList(List<SongCard> timeline) {
        List<CardDTO> result = new ArrayList<>();
        for (SongCard card : timeline) {
            Song song = card.getSong();
            result.add(new CardDTO(
                    parseLongOrNull(song.getId()),
                    song.getYear(),
                    song.getArtist(),
                    song.getTitle()
            ));
        }
        return result;
    }

    private static Long parseLongOrNull(String value) {
        try {
            return value == null ? null : Long.parseLong(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}