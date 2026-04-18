package com.hitster.engine;

import com.hitster.controller.TurnNotYoursException;
import com.hitster.model.GamePhase;
import com.hitster.model.GameSession;
import com.hitster.model.GameStatus;
import com.hitster.model.LastTurnData;
import com.hitster.model.Player;
import com.hitster.model.Song;
import com.hitster.model.SongCard;
import com.hitster.model.TurnResult;

import java.util.List;

public class GameEngine {
    private static final int WINNING_SCORE = 10;

    public void startGame(GameSession session) {
        if (session.getRemainingSongs().size() < 3) {
            throw new IllegalStateException("Not enough songs to start the game.");
        }

        session.setStatus(GameStatus.IN_PROGRESS);
        session.setPhase(GamePhase.PLAYER_TURN);

        Song firstSongPlayer1 = session.getRemainingSongs().poll();
        Song firstSongPlayer2 = session.getRemainingSongs().poll();

        session.getPlayer1Timeline().add(new SongCard(firstSongPlayer1));
        session.getPlayer2Timeline().add(new SongCard(firstSongPlayer2));

        session.setCurrentTurnPlayer(session.getPlayer1());
        drawNextSong(session);
    }

    public void drawNextSong(GameSession session) {
        if (session.getRemainingSongs().isEmpty()) {
            finishGameByScore(session);
            return;
        }

        Song nextSong = session.getRemainingSongs().poll();
        session.setCurrentSong(nextSong);
    }

    public TurnResult placeSong(GameSession session, String playerId, int insertPosition, Long songId) {
        if (session.getStatus() != GameStatus.IN_PROGRESS) {
            throw new IllegalStateException("Game is not in progress.");
        }

        if (session.getPhase() != GamePhase.PLAYER_TURN) {
            throw new IllegalStateException("It is not currently a player-turn phase.");
        }

        Player currentPlayer = session.getCurrentTurnPlayer();
        if (!currentPlayer.getId().equals(playerId)) {
            throw new TurnNotYoursException("It is not your turn.");
        }

        Song currentSong = session.getCurrentSong();
        if (currentSong == null) {
            throw new IllegalStateException("No current song for this turn.");
        }

        Long currentSongId = tryParseLong(currentSong.getId());
        if (currentSongId == null || !currentSongId.equals(songId)) {
            throw new IllegalArgumentException("Submitted songId does not match current song.");
        }

        List<SongCard> timeline = session.getTimelineOfPlayer(currentPlayer);

        if (insertPosition < 0 || insertPosition > timeline.size()) {
            throw new IllegalArgumentException("Invalid insert position: " + insertPosition);
        }

        session.incrementTurnNumber();

        boolean placementCorrect = isCorrectPlacement(timeline, currentSong, insertPosition);

        SongCard newCard = new SongCard(currentSong);
        timeline.add(insertPosition, newCard);

        session.setLastTurnData(new LastTurnData(
                currentPlayer,
                currentSong,
                insertPosition,
                placementCorrect
        ));

        if (placementCorrect) {
            currentPlayer.addPoint();
        }

        if (currentPlayer.getScore() >= WINNING_SCORE) {
            session.setWinner(currentPlayer);
            session.setStatus(GameStatus.FINISHED);
            session.setPhase(GamePhase.GAME_FINISHED);
        } else {
            session.setPhase(GamePhase.CHALLENGE_WINDOW);
            switchTurn(session);
            drawNextSong(session);
        }

        String message = placementCorrect
                ? "Song placed successfully. Placement is correct."
                : "Song placed successfully. Placement is incorrect.";

        return new TurnResult(
                placementCorrect,
                false,
                placementCorrect,
                false,
                message
        );
    }

    public TurnResult guessSong(GameSession session, String playerId, String artist, String songName) {
        if (session.getStatus() != GameStatus.IN_PROGRESS) {
            throw new IllegalStateException("Game is not in progress.");
        }

        if (session.getPhase() != GamePhase.CHALLENGE_WINDOW) {
            throw new IllegalStateException("Guess is only allowed after placement.");
        }

        LastTurnData lastTurn = session.getLastTurnData();
        if (lastTurn == null) {
            throw new IllegalStateException("No last turn available for guessing.");
        }

        Player actingPlayer = lastTurn.getActingPlayer();
        if (!actingPlayer.getId().equals(playerId)) {
            throw new TurnNotYoursException("Only the player who placed the song can guess.");
        }

        if (lastTurn.isGuessSubmitted()) {
            throw new IllegalStateException("Guess already submitted.");
        }

        if (artist == null || artist.trim().isEmpty() || songName == null || songName.trim().isEmpty()) {
            throw new IllegalArgumentException("Artist and song name are required.");
        }

        Song playedSong = lastTurn.getPlayedSong();

        boolean guessCorrect =
                playedSong.getArtist().trim().equalsIgnoreCase(artist.trim()) &&
                        playedSong.getTitle().trim().equalsIgnoreCase(songName.trim());

        lastTurn.setGuessSubmitted(true);
        lastTurn.setGuessCorrect(guessCorrect);

        if (guessCorrect) {
            actingPlayer.addToken();
        }

        String message = guessCorrect
                ? "Correct guess. Token awarded."
                : "Incorrect guess.";

        return new TurnResult(
                lastTurn.isPlacementCorrect(),
                guessCorrect,
                false,
                guessCorrect,
                message
        );
    }

    public boolean challengeLastTurn(GameSession session, Player challenger, Integer suggestedIndex) {
        if (session.getPhase() != GamePhase.CHALLENGE_WINDOW) {
            throw new IllegalStateException("Challenge is not allowed in the current phase.");
        }

        if (suggestedIndex == null || suggestedIndex < 0) {
            throw new IllegalArgumentException("suggested_index must be a non-negative integer.");
        }

        LastTurnData lastTurn = session.getLastTurnData();

        if (lastTurn == null) {
            throw new IllegalStateException("No last turn to challenge.");
        }

        if (lastTurn.getActingPlayer().getId().equals(challenger.getId())) {
            throw new IllegalArgumentException("Player cannot challenge their own turn.");
        }

        if (!challenger.useToken()) {
            throw new IllegalStateException("Player has no token.");
        }

        lastTurn.setChallengedSuggestedIndex(suggestedIndex);

        Player actingPlayer = lastTurn.getActingPlayer();
        Song playedSong = lastTurn.getPlayedSong();

        List<SongCard> actingTimeline = session.getTimelineOfPlayer(actingPlayer);
        List<SongCard> challengerTimeline = session.getTimelineOfPlayer(challenger);

        SongCard cardToMove = null;

        for (SongCard card : actingTimeline) {
            if (card.getSong().getId().equals(playedSong.getId())) {
                cardToMove = card;
                break;
            }
        }

        if (cardToMove == null) {
            session.setLastTurnData(null);
            session.setPhase(GamePhase.TURN_RESOLVED);
            moveToNextTurnPhase(session);
            return false;
        }

        boolean challengeSucceeded =
                !lastTurn.isPlacementCorrect() &&
                        suggestedIndex != lastTurn.getInsertPosition();

        if (!challengeSucceeded) {
            session.setLastTurnData(null);
            session.setPhase(GamePhase.TURN_RESOLVED);
            moveToNextTurnPhase(session);
            return false;
        }

        actingTimeline.remove(cardToMove);

        int safeIndex = Math.min(suggestedIndex, challengerTimeline.size());
        challengerTimeline.add(safeIndex, new SongCard(playedSong));
        challenger.addPoint();

        if (challenger.getScore() >= WINNING_SCORE) {
            session.setWinner(challenger);
            session.setStatus(GameStatus.FINISHED);
            session.setPhase(GamePhase.GAME_FINISHED);
            session.setLastTurnData(null);
            return true;
        }

        session.setLastTurnData(null);
        session.setPhase(GamePhase.TURN_RESOLVED);
        moveToNextTurnPhase(session);
        return true;
    }

    public void skipChallengeWindow(GameSession session) {
        if (session.getPhase() != GamePhase.CHALLENGE_WINDOW) {
            throw new IllegalStateException("Game is not in challenge window.");
        }

        session.setLastTurnData(null);
        session.setPhase(GamePhase.TURN_RESOLVED);
        moveToNextTurnPhase(session);
    }

    private void moveToNextTurnPhase(GameSession session) {
        if (session.getStatus() == GameStatus.FINISHED) {
            session.setPhase(GamePhase.GAME_FINISHED);
            return;
        }

        session.setPhase(GamePhase.PLAYER_TURN);
    }

    private boolean isCorrectPlacement(List<SongCard> timeline, Song song, int insertPosition) {
        if (insertPosition < 0 || insertPosition > timeline.size()) {
            return false;
        }

        int songYear = song.getYear();

        if (timeline.isEmpty()) {
            return insertPosition == 0;
        }

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

    private void switchTurn(GameSession session) {
        Player current = session.getCurrentTurnPlayer();
        Player next = session.getOpponent(current);
        session.setCurrentTurnPlayer(next);
    }

    private void finishGameByScore(GameSession session) {
        Player p1 = session.getPlayer1();
        Player p2 = session.getPlayer2();

        if (p1.getScore() > p2.getScore()) {
            session.setWinner(p1);
        } else if (p2.getScore() > p1.getScore()) {
            session.setWinner(p2);
        } else {
            session.setWinner(null);
        }

        session.setStatus(GameStatus.FINISHED);
        session.setPhase(GamePhase.GAME_FINISHED);
    }

    private Long tryParseLong(String value) {
        try {
            return Long.parseLong(value);
        } catch (Exception e) {
            return null;
        }
    }
}