package com.hitster.mapper;

import com.hitster.dto.game.CardDTO;
import com.hitster.dto.game.ChallengeStateDTO;
import com.hitster.dto.game.CurrentSongDTO;
import com.hitster.dto.game.GameStateDTO;
import com.hitster.dto.game.PlayerGameStateDTO;
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

        Long winnerPlayerId = session.getWinner() != null
                ? parseLongOrNull(session.getWinner().getId())
                : null;

        String winnerName = session.getWinner() != null
                ? session.getWinner().getUsername()
                : null;

        Long activePlayerId = session.getCurrentTurnPlayer() != null
                ? parseLongOrNull(session.getCurrentTurnPlayer().getId())
                : null;

        List<PlayerGameStateDTO> players = List.of(
                toPlayerStateDTO(p1, session.getPlayer1Timeline()),
                toPlayerStateDTO(p2, session.getPlayer2Timeline())
        );

        int timeLeftSeconds = session.getTimeLeftSeconds();

        return new GameStateDTO(
                session.getPhase(),
                session.getTurnNumber(),
                timeLeftSeconds,
                activePlayerId,
                winnerPlayerId,
                winnerName,
                toCurrentSongDTO(session.getCurrentSong()),
                players,
                session.getLastTurnResult(),
                toChallengeStateDTO(session.getChallengeState(), timeLeftSeconds),
                session.getLastChallengeResult()
        );
    }

    private static ChallengeStateDTO toChallengeStateDTO(ChallengeStateDTO challengeState, int timeLeftSeconds) {
        if (challengeState == null) {
            return null;
        }

        return new ChallengeStateDTO(
                challengeState.challengerPlayerId(),
                challengeState.challengedPlayerId(),
                challengeState.songId(),
                challengeState.originalPlacedIndex(),
                timeLeftSeconds,
                challengeState.challengeAvailable()
        );
    }

    private static PlayerGameStateDTO toPlayerStateDTO(Player player, List<SongCard> timeline) {
        return new PlayerGameStateDTO(
                parseLongOrNull(player.getId()),
                player.getUsername(),
                player.getScore(),
                player.getTokens(),
                toCardDTOList(timeline)
        );
    }

    private static CurrentSongDTO toCurrentSongDTO(Song song) {
        if (song == null) {
            return null;
        }

        return new CurrentSongDTO(
                parseLongOrNull(song.getId()),
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
