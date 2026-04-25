package com.hitster.engine;

import com.hitster.controller.TurnNotYoursException;
import com.hitster.dto.game.ChallengeResultDTO;
import com.hitster.dto.game.ChallengeStateDTO;
import com.hitster.dto.game.GamePhase;
import com.hitster.dto.game.TurnResultDTO;
import com.hitster.model.GameSession;
import com.hitster.model.GameStatus;
import com.hitster.model.LastTurnData;
import com.hitster.model.Player;
import com.hitster.model.Song;
import com.hitster.model.SongCard;

import java.util.List;

public class GameEngine {

    private static final int WINNING_SCORE = 10;
    private static final int PLAYER_TURN_SECONDS = 180; // 3 minutes
    private static final int CHALLENGE_SECONDS = 60;    // optional - יותר זמן גם לצ'אלנג'
    private static final int RESOLVE_SECONDS = 2;

    public void startGame(GameSession session) {
        if (session.getRemainingSongs().size() < 3) {
            throw new IllegalStateException("Not enough songs to start the game.");
        }

        session.setStatus(GameStatus.IN_PROGRESS);

        Song firstSongPlayer1 = session.getRemainingSongs().poll();
        Song firstSongPlayer2 = session.getRemainingSongs().poll();

        session.getPlayer1Timeline().add(new SongCard(firstSongPlayer1));
        session.getPlayer2Timeline().add(new SongCard(firstSongPlayer2));

        session.setCurrentTurnPlayer(session.getPlayer1());
        session.clearPendingTurnInput();
        session.setLastTurnResult(null);
        session.setLastChallengeResult(null);
        session.setChallengeState(null);

        drawNextSongOrFinish(session);
        session.startPhase(GamePhase.PLAYER_TURN, PLAYER_TURN_SECONDS);
    }

    public void advanceIfNeeded(GameSession session) {
        if (session == null || session.isFinished()) {
            return;
        }

        if (!session.isPhaseExpired()) {
            return;
        }

        if (session.getPhase() == GamePhase.PLAYER_TURN) {
            handlePlayerTurnTimeout(session);
            return;
        }

        if (session.getPhase() == GamePhase.CHALLENGE_WINDOW) {
            skipChallengeWindow(session);
            return;
        }

        if (session.getPhase() == GamePhase.TURN_RESOLVED) {
            moveToNextTurn(session);
        }
    }

    public void submitGuess(GameSession session, String playerId, String artist, String title) {
        requirePlayerTurn(session, playerId);

        if (artist == null || artist.trim().isEmpty() || title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Artist and title are required.");
        }

        session.setPendingGuessedArtist(artist.trim());
        session.setPendingGuessedTitle(title.trim());

        tryResolveTurn(session);
    }

    public void placeSong(GameSession session, String playerId, int insertPosition, Long songId) {
        requirePlayerTurn(session, playerId);

        if (session.getPendingPlacedSong() != null) {
            throw new IllegalStateException("Song was already placed this turn.");
        }

        Song currentSong = session.getCurrentSong();
        if (currentSong == null) {
            throw new IllegalStateException("No current song for this turn.");
        }

        Long currentSongId = tryParseLong(currentSong.getId());
        if (currentSongId == null || !currentSongId.equals(songId)) {
            throw new IllegalArgumentException("Submitted songId does not match current song.");
        }

        Player actingPlayer = session.getCurrentTurnPlayer();
        List<SongCard> timeline = session.getTimelineOfPlayer(actingPlayer);

        if (insertPosition < 0 || insertPosition > timeline.size()) {
            throw new IllegalArgumentException("Invalid insert position: " + insertPosition);
        }

        boolean placementCorrect = isCorrectPlacement(timeline, currentSong, insertPosition);

        timeline.add(insertPosition, new SongCard(currentSong));

        session.setPendingActingPlayer(actingPlayer);
        session.setPendingPlacedSong(currentSong);
        session.setPendingInsertPosition(insertPosition);
        session.setPendingPlacementCorrect(placementCorrect);

        tryResolveTurn(session);
    }

    public void challengeLastTurn(GameSession session, Player challenger, Integer suggestedIndex) {
        if (session.getPhase() != GamePhase.CHALLENGE_WINDOW) {
            throw new IllegalStateException("Challenge is not allowed in the current phase.");
        }

        if (session.getChallengeState() == null) {
            throw new IllegalStateException("No challenge state is available.");
        }

        if (suggestedIndex == null || suggestedIndex < 0) {
            throw new IllegalArgumentException("suggestedIndex must be a non-negative integer.");
        }

        ChallengeStateDTO challengeState = session.getChallengeState();

        if (!String.valueOf(challengeState.challengerPlayerId()).equals(challenger.getId())) {
            throw new TurnNotYoursException("You are not the challenger for this turn.");
        }

        if (!challengeState.challengeAvailable()) {
            throw new IllegalStateException("Challenge is not available.");
        }

        if (!challenger.useToken()) {
            throw new IllegalStateException("Player has no token.");
        }

        LastTurnData lastTurn = session.getLastTurnData();
        if (lastTurn == null) {
            throw new IllegalStateException("No last turn to challenge.");
        }

        Player challengedPlayer = lastTurn.getActingPlayer();
        Song playedSong = lastTurn.getPlayedSong();

        List<SongCard> challengedTimeline = session.getTimelineOfPlayer(challengedPlayer);
        List<SongCard> challengerTimeline = session.getTimelineOfPlayer(challenger);

        boolean challengeCorrect = isCorrectPlacement(challengerTimeline, playedSong, suggestedIndex);

        boolean cardTransferred = false;

        if (challengeCorrect) {
            SongCard cardToMove = findCardBySongId(challengedTimeline, playedSong.getId());

            if (cardToMove != null) {
                challengedTimeline.remove(cardToMove);

                int safeIndex = Math.min(suggestedIndex, challengerTimeline.size());
                challengerTimeline.add(safeIndex, new SongCard(playedSong));

                challenger.addPoint();
                cardTransferred = true;
            }
        }

        session.setLastChallengeResult(new ChallengeResultDTO(
                tryParseLong(challenger.getId()),
                tryParseLong(challengedPlayer.getId()),
                tryParseLong(playedSong.getId()),
                suggestedIndex,
                challengeCorrect,
                true,
                cardTransferred
        ));

        if (challenger.getScore() >= WINNING_SCORE) {
            finishGame(session, challenger);
            return;
        }

        resolveTurn(session);
    }

    public void skipChallengeWindow(GameSession session) {
        if (session.getPhase() != GamePhase.CHALLENGE_WINDOW) {
            throw new IllegalStateException("Game is not in challenge window.");
        }

        session.setLastChallengeResult(null);
        resolveTurn(session);
    }

    private void requirePlayerTurn(GameSession session, String playerId) {
        if (session.getStatus() != GameStatus.IN_PROGRESS) {
            throw new IllegalStateException("Game is not in progress.");
        }

        if (session.getPhase() != GamePhase.PLAYER_TURN) {
            throw new IllegalStateException("It is not currently a player-turn phase.");
        }

        Player currentPlayer = session.getCurrentTurnPlayer();
        if (currentPlayer == null || !currentPlayer.getId().equals(playerId)) {
            throw new TurnNotYoursException("It is not your turn.");
        }
    }

    private void tryResolveTurn(GameSession session) {
        if (session.getPendingGuessedArtist() == null ||
                session.getPendingGuessedTitle() == null ||
                session.getPendingPlacedSong() == null ||
                session.getPendingInsertPosition() == null ||
                session.getPendingActingPlayer() == null) {
            return;
        }

        Player actingPlayer = session.getPendingActingPlayer();
        Song playedSong = session.getPendingPlacedSong();

        boolean artistCorrect = playedSong.getArtist()
                .trim()
                .equalsIgnoreCase(session.getPendingGuessedArtist().trim());

        boolean titleCorrect = playedSong.getTitle()
                .trim()
                .equalsIgnoreCase(session.getPendingGuessedTitle().trim());

        boolean placementCorrect = session.isPendingPlacementCorrect();
        boolean earnedToken = artistCorrect && titleCorrect && placementCorrect;

        if (placementCorrect) {
            actingPlayer.addPoint();
        }

        if (earnedToken) {
            actingPlayer.addToken();
        }

        TurnResultDTO turnResult = new TurnResultDTO(
                tryParseLong(actingPlayer.getId()),
                tryParseLong(playedSong.getId()),
                session.getPendingInsertPosition(),
                titleCorrect,
                artistCorrect,
                placementCorrect,
                earnedToken
        );

        session.setLastTurnResult(turnResult);
        session.setLastChallengeResult(null);

        session.setLastTurnData(new LastTurnData(
                actingPlayer,
                playedSong,
                session.getPendingInsertPosition(),
                placementCorrect
        ));

        if (actingPlayer.getScore() >= WINNING_SCORE) {
            finishGame(session, actingPlayer);
            return;
        }

        Player challenger = session.getOpponent(actingPlayer);
        boolean challengeAvailable = challenger.getTokens() > 0;

        session.setChallengeState(new ChallengeStateDTO(
                tryParseLong(challenger.getId()),
                tryParseLong(actingPlayer.getId()),
                tryParseLong(playedSong.getId()),
                session.getPendingInsertPosition(),
                CHALLENGE_SECONDS,
                challengeAvailable
        ));

        session.startPhase(GamePhase.CHALLENGE_WINDOW, CHALLENGE_SECONDS);
    }

    private void resolveTurn(GameSession session) {
        session.setChallengeState(null);
        session.clearPendingTurnInput();
        session.incrementTurnNumber();
        session.startPhase(GamePhase.TURN_RESOLVED, RESOLVE_SECONDS);
    }

    private void moveToNextTurn(GameSession session) {
        if (session.getStatus() == GameStatus.FINISHED) {
            session.startPhase(GamePhase.FINISHED, 0);
            return;
        }

        Player current = session.getCurrentTurnPlayer();
        session.setCurrentTurnPlayer(session.getOpponent(current));

        session.setLastTurnResult(null);
        session.setLastChallengeResult(null);
        session.setChallengeState(null);
        session.clearPendingTurnInput();

        drawNextSongOrFinish(session);

        if (!session.isFinished()) {
            session.startPhase(GamePhase.PLAYER_TURN, PLAYER_TURN_SECONDS);
        }
    }

    private void handlePlayerTurnTimeout(GameSession session) {
        Player player = session.getCurrentTurnPlayer();
        Song song = session.getCurrentSong();

        session.setLastTurnResult(new TurnResultDTO(
                player != null ? tryParseLong(player.getId()) : null,
                song != null ? tryParseLong(song.getId()) : null,
                -1,
                false,
                false,
                false,
                false
        ));

        session.setLastChallengeResult(null);
        session.setChallengeState(null);
        session.clearPendingTurnInput();
        session.incrementTurnNumber();
        session.startPhase(GamePhase.TURN_RESOLVED, RESOLVE_SECONDS);
    }

    private void drawNextSongOrFinish(GameSession session) {
        if (session.getRemainingSongs().isEmpty()) {
            finishGameByScore(session);
            return;
        }

        session.setCurrentSong(session.getRemainingSongs().poll());
    }

    private void finishGameByScore(GameSession session) {
        Player p1 = session.getPlayer1();
        Player p2 = session.getPlayer2();

        if (p1.getScore() > p2.getScore()) {
            finishGame(session, p1);
        } else if (p2.getScore() > p1.getScore()) {
            finishGame(session, p2);
        } else {
            finishGame(session, null);
        }
    }

    private void finishGame(GameSession session, Player winner) {
        session.setWinner(winner);
        session.setStatus(GameStatus.FINISHED);
        session.startPhase(GamePhase.FINISHED, 0);
        session.setCurrentSong(null);
        session.setChallengeState(null);
        session.clearPendingTurnInput();
    }

    private boolean isCorrectPlacement(List<SongCard> timeline, Song song, int insertPosition) {
        if (insertPosition < 0 || insertPosition > timeline.size()) {
            return false;
        }

        int songYear = song.getYear();

        Integer leftYear = null;
        Integer rightYear = null;

        if (insertPosition > 0) {
            leftYear = timeline.get(insertPosition - 1).getSong().getYear();
        }

        if (insertPosition < timeline.size()) {
            rightYear = timeline.get(insertPosition).getSong().getYear();
        }

        boolean validLeft = leftYear == null || songYear >= leftYear;
        boolean validRight = rightYear == null || songYear <= rightYear;

        return validLeft && validRight;
    }

    private SongCard findCardBySongId(List<SongCard> timeline, String songId) {
        for (SongCard card : timeline) {
            if (card.getSong().getId().equals(songId)) {
                return card;
            }
        }

        return null;
    }

    private Long tryParseLong(String value) {
        try {
            return value == null ? null : Long.parseLong(value);
        } catch (Exception e) {
            return null;
        }
    }
}