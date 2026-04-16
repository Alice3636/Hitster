package com.hitster.engine;

import com.hitster.model.GamePhase;
import com.hitster.model.GameSession;
import com.hitster.model.GameStatus;
import com.hitster.model.LastTurnData;
import com.hitster.model.Player;
import com.hitster.model.Song;
import com.hitster.model.SongCard;
import com.hitster.model.TurnAction;
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

    public TurnResult submitTurn(GameSession session, TurnAction action) {
        if (session.getStatus() != GameStatus.IN_PROGRESS) {
            throw new IllegalStateException("Game is not in progress.");
        }

        if (session.getPhase() != GamePhase.PLAYER_TURN) {
            throw new IllegalStateException("It is not currently a player-turn phase.");
        }

        session.incrementTurnNumber();

        Player currentPlayer = session.getCurrentTurnPlayer();
        Song currentSong = session.getCurrentSong();

        if (currentSong == null) {
            throw new IllegalStateException("No current song for this turn.");
        }

        List<SongCard> timeline = session.getTimelineOfPlayer(currentPlayer);

        if (action.getInsertPosition() < 0 || action.getInsertPosition() > timeline.size()) {
            throw new IllegalArgumentException("Invalid insert position: " + action.getInsertPosition());
        }

        boolean placementCorrect = isCorrectPlacement(timeline, currentSong, action.getInsertPosition());
        boolean titleArtistCorrect = isCorrectGuess(currentSong, action.getGuessedTitle(), action.getGuessedArtist());

        SongCard newCard = new SongCard(currentSong);
        timeline.add(action.getInsertPosition(), newCard);

        session.setLastTurnData(
                new LastTurnData(
                        currentPlayer,
                        currentSong,
                        action.getInsertPosition(),
                        placementCorrect));

        if (placementCorrect) {
            currentPlayer.addPoint();
        }

        if (titleArtistCorrect) {
            currentPlayer.addToken();
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

        String message = buildMessage(placementCorrect, titleArtistCorrect);

        return new TurnResult(
                placementCorrect,
                titleArtistCorrect,
                placementCorrect,
                titleArtistCorrect,
                message);
    }

    public boolean challengeLastTurn(GameSession session, Player challenger) {
        if (session.getPhase() != GamePhase.CHALLENGE_WINDOW) {
            throw new IllegalStateException("Challenge is not allowed in the current phase.");
        }

        LastTurnData lastTurn = session.getLastTurnData();

        if (lastTurn == null) {
            throw new IllegalStateException("No last turn to challenge.");
        }

        if (lastTurn.getActingPlayer().getId() == challenger.getId()) {
            throw new IllegalArgumentException("Player cannot challenge their own turn.");
        }

        if (!challenger.useToken()) {
            throw new IllegalStateException("Player has no token.");
        }

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

        if (lastTurn.isPlacementCorrect()) {
            session.setLastTurnData(null);
            session.setPhase(GamePhase.TURN_RESOLVED);
            moveToNextTurnPhase(session);
            return false;
        }

        actingTimeline.remove(cardToMove);
        challengerTimeline.add(new SongCard(playedSong));
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

    private boolean isCorrectGuess(Song song, String guessedTitle, String guessedArtist) {
        if (guessedTitle == null || guessedArtist == null) {
            return false;
        }

        return song.getTitle().trim().equalsIgnoreCase(guessedTitle.trim())
                && song.getArtist().trim().equalsIgnoreCase(guessedArtist.trim());
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

    private String buildMessage(boolean placementCorrect, boolean titleArtistCorrect) {
        if (placementCorrect && titleArtistCorrect) {
            return "Correct placement and correct guess. Point and token awarded.";
        }
        if (placementCorrect) {
            return "Correct placement. Point awarded.";
        }
        if (titleArtistCorrect) {
            return "Incorrect placement, but correct song guess. Token awarded.";
        }
        return "Incorrect placement and incorrect guess.";
    }
}