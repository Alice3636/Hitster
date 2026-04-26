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

    private static final int WINNING_SCORE = 5;
    private static final int PLAYER_TURN_SECONDS = 60;
    private static final int CHALLENGE_SECONDS = 10;
    private static final int RESOLVE_SECONDS = 2;
    private static final int STARTING_TOKENS = 2;
    private static final int MAX_MISSED_TURNS = 3;

    public void startGame(GameSession session) {
        if (session.getRemainingSongs().size() < 3) {
            throw new IllegalStateException("Not enough songs to start the game.");
        }

        session.setStatus(GameStatus.IN_PROGRESS);

        session.getPlayer1Timeline().add(new SongCard(session.getRemainingSongs().poll()));
        session.getPlayer2Timeline().add(new SongCard(session.getRemainingSongs().poll()));

        giveStartingTokens(session.getPlayer1());
        giveStartingTokens(session.getPlayer2());

        session.setCurrentTurnPlayer(session.getPlayer1());
        session.setLastTurnResult(null);
        session.setLastChallengeResult(null);
        session.setChallengeState(null);
        session.setLastTurnData(null);
        session.clearPendingTurnInput();

        drawNextSongOrFinish(session);

        if (!session.isFinished()) {
            session.startPhase(GamePhase.PLAYER_TURN, PLAYER_TURN_SECONDS);
        }
    }

    public void advanceIfNeeded(GameSession session) {
        if (session == null || session.isFinished() || !session.isPhaseExpired()) {
            return;
        }

        switch (session.getPhase()) {
            case PLAYER_TURN -> handlePlayerTurnTimeout(session);
            case CHALLENGE_WINDOW -> skipChallengeWindow(session);
            case TURN_RESOLVED -> moveToNextTurn(session);
            default -> { }
        }
    }

    public void placeSong(GameSession session, String playerId, int indexPosition, Long songId) {
        requirePlayerTurn(session, playerId);

        if (songId == null) {
            throw new IllegalArgumentException("songId is required.");
        }

        if (session.getPendingPlacedSong() != null) {
            throw new IllegalStateException("Song was already placed this turn.");
        }

        Song currentSong = session.getCurrentSong();
        if (currentSong == null) {
            throw new IllegalStateException("No current song for this turn.");
        }

        Long currentSongId = parseLongOrNull(currentSong.getId());
        if (currentSongId == null || !currentSongId.equals(songId)) {
            throw new IllegalArgumentException("Submitted songId does not match current song.");
        }

        Player actingPlayer = session.getCurrentTurnPlayer();
        List<SongCard> timeline = session.getTimelineOfPlayer(actingPlayer);

        if (indexPosition < 0 || indexPosition > timeline.size()) {
            throw new IllegalArgumentException("Invalid insert position: " + indexPosition);
        }

        boolean placementCorrect = isCorrectPlacement(timeline, currentSong, indexPosition);

        // Do not add card to timeline here.
        session.setPendingActingPlayer(actingPlayer);
        session.setPendingPlacedSong(currentSong);
        session.setPendingInsertPosition(indexPosition);
        session.setPendingPlacementCorrect(placementCorrect);

        tryResolveTurn(session);
    }

    public void submitGuess(GameSession session, String playerId, String artist, String title) {
        requirePlayerTurn(session, playerId);

        if (session.getPendingGuessedArtist() != null || session.getPendingGuessedTitle() != null) {
            throw new IllegalStateException("Guess was already submitted this turn.");
        }

        if (artist == null || artist.trim().isEmpty() || title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Artist and title are required.");
        }

        session.setPendingGuessedArtist(artist.trim());
        session.setPendingGuessedTitle(title.trim());

        tryResolveTurn(session);
    }

    public void challengeLastTurn(GameSession session, Player challenger, Integer suggestedIndex) {
        if (session.getPhase() != GamePhase.CHALLENGE_WINDOW) {
            throw new IllegalStateException("Challenge is not allowed in the current phase.");
        }

        if (challenger == null) {
            throw new IllegalArgumentException("Challenger is required.");
        }

        if (suggestedIndex == null || suggestedIndex < 0) {
            throw new IllegalArgumentException("suggestedIndex must be a non-negative integer.");
        }

        ChallengeStateDTO challengeState = session.getChallengeState();
        if (challengeState == null || !challengeState.challengeAvailable()) {
            throw new IllegalStateException("Challenge is not available.");
        }

        if (!String.valueOf(challengeState.challengerPlayerId()).equals(challenger.getId())) {
            throw new TurnNotYoursException("You are not the challenger for this turn.");
        }

        LastTurnData lastTurn = session.getLastTurnData();
        if (lastTurn == null) {
            throw new IllegalStateException("No last turn available to challenge.");
        }

        if (!challenger.useToken()) {
            throw new IllegalStateException("Player has no token.");
        }

        Song playedSong = lastTurn.getPlayedSong();
        Player challengedPlayer = lastTurn.getActingPlayer();
        List<SongCard> challengerTimeline = session.getTimelineOfPlayer(challenger);

        if (suggestedIndex > challengerTimeline.size()) {
            throw new IllegalArgumentException("Invalid suggested index: " + suggestedIndex);
        }

        boolean challengeCorrect = isCorrectPlacement(challengerTimeline, playedSong, suggestedIndex);
        boolean cardTransferred = false;

        if (challengeCorrect) {
            challengerTimeline.add(suggestedIndex, new SongCard(playedSong));
            challenger.addPoint();
            cardTransferred = true;
        }

        session.setLastChallengeResult(new ChallengeResultDTO(
                parseLongOrNull(challenger.getId()),
                parseLongOrNull(challengedPlayer.getId()),
                parseLongOrNull(playedSong.getId()),
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

    public void finishByForfeit(GameSession session, Player quitter) {
        if (session == null || quitter == null || session.isFinished()) {
            return;
        }

        Player winner = session.getOpponent(quitter);
        finishGame(session, winner);
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

        session.resetMissedTurns(actingPlayer);

        boolean artistCorrect = playedSong.getArtist()
                .trim()
                .equalsIgnoreCase(session.getPendingGuessedArtist().trim());

        boolean titleCorrect = playedSong.getTitle()
                .trim()
                .equalsIgnoreCase(session.getPendingGuessedTitle().trim());

        boolean placementCorrect = session.isPendingPlacementCorrect();
        boolean earnedToken = artistCorrect && titleCorrect && placementCorrect;

        if (earnedToken) {
            actingPlayer.addToken();
        }

        session.setLastTurnResult(new TurnResultDTO(
                parseLongOrNull(actingPlayer.getId()),
                parseLongOrNull(playedSong.getId()),
                session.getPendingInsertPosition(),
                titleCorrect,
                artistCorrect,
                placementCorrect,
                earnedToken
        ));

        session.setLastTurnData(new LastTurnData(
                actingPlayer,
                playedSong,
                session.getPendingInsertPosition(),
                placementCorrect
        ));

        session.setLastChallengeResult(null);

        Player challenger = session.getOpponent(actingPlayer);

        if (challenger.getTokens() <= 0) {
            resolveTurn(session);
            return;
        }

        session.setChallengeState(new ChallengeStateDTO(
                parseLongOrNull(challenger.getId()),
                parseLongOrNull(actingPlayer.getId()),
                parseLongOrNull(playedSong.getId()),
                session.getPendingInsertPosition(),
                CHALLENGE_SECONDS,
                true
        ));

        session.startPhase(GamePhase.CHALLENGE_WINDOW, CHALLENGE_SECONDS);
    }

    private void resolveTurn(GameSession session) {
        applyPendingCardResolution(session);

        if (session.isFinished()) {
            return;
        }

        session.setCurrentSong(null);
        session.setChallengeState(null);
        session.clearPendingTurnInput();
        session.incrementTurnNumber();
        session.startPhase(GamePhase.TURN_RESOLVED, RESOLVE_SECONDS);
    }

    private void applyPendingCardResolution(GameSession session) {
        LastTurnData lastTurn = session.getLastTurnData();
        if (lastTurn == null) {
            return;
        }

        Player actingPlayer = lastTurn.getActingPlayer();
        Song playedSong = lastTurn.getPlayedSong();
        int originalIndex = lastTurn.getInsertPosition();
        boolean originalPlacementCorrect = lastTurn.isPlacementCorrect();

        ChallengeResultDTO challengeResult = session.getLastChallengeResult();
        boolean challengeSucceeded = challengeResult != null && challengeResult.challengeCorrect();

        if (challengeSucceeded) {
            return;
        }

        if (!originalPlacementCorrect) {
            return; // burned card
        }

        List<SongCard> actingTimeline = session.getTimelineOfPlayer(actingPlayer);
        int safeIndex = Math.max(0, Math.min(originalIndex, actingTimeline.size()));

        actingTimeline.add(safeIndex, new SongCard(playedSong));
        actingPlayer.addPoint();

        if (actingPlayer.getScore() >= WINNING_SCORE) {
            finishGame(session, actingPlayer);
        }
    }

    private void moveToNextTurn(GameSession session) {
        if (session.isFinished()) {
            return;
        }

        Player lastActor = session.getLastTurnData() != null
                ? session.getLastTurnData().getActingPlayer()
                : session.getCurrentTurnPlayer();

        session.setCurrentTurnPlayer(session.getOpponent(lastActor));

        session.setLastTurnResult(null);
        session.setLastChallengeResult(null);
        session.setChallengeState(null);
        session.setLastTurnData(null);
        session.clearPendingTurnInput();

        drawNextSongOrFinish(session);

        if (!session.isFinished()) {
            session.startPhase(GamePhase.PLAYER_TURN, PLAYER_TURN_SECONDS);
        }
    }

    private void handlePlayerTurnTimeout(GameSession session) {
        Player player = session.getCurrentTurnPlayer();
        Song song = session.getCurrentSong();

        session.incrementMissedTurn(player);

        if (session.getMissedTurns(player) >= MAX_MISSED_TURNS) {
            finishGame(session, session.getOpponent(player));
            return;
        }

        session.setLastTurnResult(new TurnResultDTO(
                player != null ? parseLongOrNull(player.getId()) : null,
                song != null ? parseLongOrNull(song.getId()) : null,
                -1,
                false,
                false,
                false,
                false
        ));

        session.setCurrentSong(null);
        session.setLastChallengeResult(null);
        session.setChallengeState(null);
        session.setLastTurnData(null);
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
        session.setCurrentTurnPlayer(null);
        session.setCurrentSong(null);
        session.setChallengeState(null);
        session.clearPendingTurnInput();
        session.startPhase(GamePhase.FINISHED, 0);
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

    private void giveStartingTokens(Player player) {
        for (int i = 0; i < STARTING_TOKENS; i++) {
            player.addToken();
        }
    }

    private boolean isCorrectPlacement(List<SongCard> timeline, Song song, int insertPosition) {
        if (insertPosition < 0 || insertPosition > timeline.size()) {
            return false;
        }

        int songYear = song.getYear();

        Integer leftYear = insertPosition > 0
                ? timeline.get(insertPosition - 1).getSong().getYear()
                : null;

        Integer rightYear = insertPosition < timeline.size()
                ? timeline.get(insertPosition).getSong().getYear()
                : null;

        return (leftYear == null || songYear >= leftYear)
                && (rightYear == null || songYear <= rightYear);
    }

    private Long parseLongOrNull(String value) {
        try {
            return value == null ? null : Long.parseLong(value);
        } catch (Exception e) {
            return null;
        }
    }
}