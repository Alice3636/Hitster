package com.hitster.mapper;

import com.hitster.dto.GameStateDTO;
import com.hitster.model.GameSession;
import com.hitster.model.GameStatus;
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
                        ? toCurrentSongDTO(session.getCurrentSong(), shouldHideCurrentSongDetails(session))
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

    private static boolean shouldHideCurrentSongDetails(GameSession session) {
        return session.getStatus() == GameStatus.IN_PROGRESS;
    }

    private static GameStateDTO.CurrentSongDTO toCurrentSongDTO(Song song, boolean hideDetails) {
        GameStateDTO.CurrentSongDTO dto = new GameStateDTO.CurrentSongDTO();
        dto.setAudioUrl(song.getAudioUrl());
        dto.setDetailsHidden(hideDetails);

        if (hideDetails) {
            dto.setTitle(null);
            dto.setArtist(null);
            dto.setYear(null);
        } else {
            dto.setTitle(song.getTitle());
            dto.setArtist(song.getArtist());
            dto.setYear(song.getYear());
        }

        return dto;
    }

    private static List<GameStateDTO.CardDTO> toCardDTOList(List<SongCard> timeline) {
        List<GameStateDTO.CardDTO> result = new ArrayList<>();

        for (SongCard card : timeline) {
            Song song = card.getSong();

            GameStateDTO.CardDTO dto = new GameStateDTO.CardDTO();
            dto.setSongId(parseLongOrNull(song.getId()));
            dto.setYear(song.getYear());
            dto.setArtist(song.getArtist());
            dto.setTitle(song.getTitle());

            result.add(dto);
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