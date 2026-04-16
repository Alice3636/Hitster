package com.hitster.mapper;

import com.hitster.dto.GameStateDTO;
import com.hitster.model.GameSession;
import com.hitster.model.Player;
import com.hitster.model.Song;
import com.hitster.model.SongCard;

import java.util.ArrayList;
import java.util.List;

public class GameStateMapper {

    public static GameStateDTO toDTO(GameSession session) {
        if (session == null) {
            return null;
        }

        GameStateDTO dto = new GameStateDTO();
        dto.setGameId((long) session.getId());
        dto.setGameStatus(session.getStatus() != null ? session.getStatus().name() : null);
        dto.setTurnNumber(session.getTurnNumber());
        dto.setTimeLeftSeconds(0);

        if (session.getCurrentTurnPlayer() != null) {

            dto.setCurrentPlayerId((long) session.getCurrentTurnPlayer().getId());
        }
        dto.setCurrentSong(toCurrentSongDTO(session.getCurrentSong()));
        dto.setWinnerName(session.getWinner() != null ? session.getWinner().getUsername() : null);

        Player p1 = session.getPlayer1();
        if (p1 != null) {
            dto.setPlayer1Id((long) p1.getId());
            dto.setPlayer1Name(p1.getUsername());
            dto.setPlayer1Score(p1.getScore());
            dto.setPlayer1Tokens(p1.getTokens());
            dto.setPlayer1Timeline(toCardDTOList(session.getPlayer1Timeline()));
        }

        Player p2 = session.getPlayer2();
        if (p2 != null) {
            dto.setPlayer2Id((long) p2.getId());
            dto.setPlayer2Name(p2.getUsername());
            dto.setPlayer2Score(p2.getScore());
            dto.setPlayer2Tokens(p2.getTokens());
            dto.setPlayer2Timeline(toCardDTOList(session.getPlayer2Timeline()));
        }

        return dto;
    }

    private static GameStateDTO.CurrentSongDTO toCurrentSongDTO(Song song) {
        if (song == null) {
            return null;
        }

        GameStateDTO.CurrentSongDTO songDto = new GameStateDTO.CurrentSongDTO();

        // FIX: Now using the exact method from your Song.java model!
        songDto.setAudioUrl(song.getAudioUrl());

        songDto.setDetailsHidden(true);
        songDto.setTitle(song.getTitle());
        songDto.setArtist(song.getArtist());
        songDto.setYear(song.getYear());

        return songDto;
    }

    private static List<GameStateDTO.CardDTO> toCardDTOList(List<SongCard> timeline) {
        List<GameStateDTO.CardDTO> result = new ArrayList<>();

        if (timeline == null) {
            return result;
        }

        for (SongCard card : timeline) {
            if (card != null && card.getSong() != null) {
                Song song = card.getSong();
                GameStateDTO.CardDTO cardDto = new GameStateDTO.CardDTO();

                // FIX: Cast int to long
                cardDto.setSongId((long) Integer.parseInt(song.getId()));
                cardDto.setYear(song.getYear());
                cardDto.setTitle(song.getTitle());
                cardDto.setArtist(song.getArtist());

                result.add(cardDto);
            }
        }

        return result;
    }
}