package com.hitster.model;

import com.hitster.dto.game.ChallengeResultDTO;
import com.hitster.dto.game.ChallengeStateDTO;
import com.hitster.dto.game.GamePhase;
import com.hitster.dto.game.TurnResultDTO;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class GameSession {
    private final String id;
    private final Player player1;
    private final Player player2;

    private final List<SongCard> player1Timeline;
    private final List<SongCard> player2Timeline;
    private final Queue<Song> remainingSongs;

    private Song currentSong;
    private Player currentTurnPlayer;
    private GameStatus status;
    private GamePhase phase;
    private Player winner;
    private int turnNumber;

    private LastTurnData lastTurnData;
    private TurnResultDTO lastTurnResult;
    private ChallengeStateDTO challengeState;
    private ChallengeResultDTO lastChallengeResult;

    private String pendingGuessedArtist;
    private String pendingGuessedTitle;
    private Integer pendingInsertPosition;
    private Song pendingPlacedSong;
    private Player pendingActingPlayer;
    private boolean pendingPlacementCorrect;

    private int player1MissedTurns;
    private int player2MissedTurns;

    private long phaseEndsAtMillis;

    public GameSession(String id, Player player1, Player player2, List<Song> songsPool) {
        this.id = id;
        this.player1 = player1;
        this.player2 = player2;
        this.player1Timeline = new ArrayList<>();
        this.player2Timeline = new ArrayList<>();
        this.remainingSongs = new LinkedList<>(songsPool);
        this.status = GameStatus.WAITING_FOR_PLAYERS;
        this.phase = GamePhase.WAITING_FOR_PLAYERS;
        this.turnNumber = 0;
        this.phaseEndsAtMillis = 0;
        this.player1MissedTurns = 0;
        this.player2MissedTurns = 0;
    }

    public String getId() { return id; }
    public Player getPlayer1() { return player1; }
    public Player getPlayer2() { return player2; }
    public List<SongCard> getPlayer1Timeline() { return player1Timeline; }
    public List<SongCard> getPlayer2Timeline() { return player2Timeline; }
    public Queue<Song> getRemainingSongs() { return remainingSongs; }

    public Song getCurrentSong() { return currentSong; }
    public void setCurrentSong(Song currentSong) { this.currentSong = currentSong; }

    public Player getCurrentTurnPlayer() { return currentTurnPlayer; }
    public void setCurrentTurnPlayer(Player currentTurnPlayer) { this.currentTurnPlayer = currentTurnPlayer; }

    public GameStatus getStatus() { return status; }
    public void setStatus(GameStatus status) { this.status = status; }

    public GamePhase getPhase() { return phase; }
    public void setPhase(GamePhase phase) { this.phase = phase; }

    public void startPhase(GamePhase newPhase, int durationSeconds) {
        this.phase = newPhase;
        this.phaseEndsAtMillis = durationSeconds > 0
                ? System.currentTimeMillis() + durationSeconds * 1000L
                : 0;
    }

    public boolean isPhaseExpired() {
        return phaseEndsAtMillis > 0 && System.currentTimeMillis() >= phaseEndsAtMillis;
    }

    public int getTimeLeftSeconds() {
        if (phaseEndsAtMillis <= 0) return 0;
        long leftMillis = phaseEndsAtMillis - System.currentTimeMillis();
        return Math.max(0, (int) Math.ceil(leftMillis / 1000.0));
    }

    public Player getWinner() { return winner; }
    public void setWinner(Player winner) { this.winner = winner; }

    public LastTurnData getLastTurnData() { return lastTurnData; }
    public void setLastTurnData(LastTurnData lastTurnData) { this.lastTurnData = lastTurnData; }

    public TurnResultDTO getLastTurnResult() { return lastTurnResult; }
    public void setLastTurnResult(TurnResultDTO lastTurnResult) { this.lastTurnResult = lastTurnResult; }

    public ChallengeStateDTO getChallengeState() { return challengeState; }
    public void setChallengeState(ChallengeStateDTO challengeState) { this.challengeState = challengeState; }

    public ChallengeResultDTO getLastChallengeResult() { return lastChallengeResult; }
    public void setLastChallengeResult(ChallengeResultDTO lastChallengeResult) { this.lastChallengeResult = lastChallengeResult; }

    public String getPendingGuessedArtist() { return pendingGuessedArtist; }
    public void setPendingGuessedArtist(String pendingGuessedArtist) { this.pendingGuessedArtist = pendingGuessedArtist; }

    public String getPendingGuessedTitle() { return pendingGuessedTitle; }
    public void setPendingGuessedTitle(String pendingGuessedTitle) { this.pendingGuessedTitle = pendingGuessedTitle; }

    public Integer getPendingInsertPosition() { return pendingInsertPosition; }
    public void setPendingInsertPosition(Integer pendingInsertPosition) { this.pendingInsertPosition = pendingInsertPosition; }

    public Song getPendingPlacedSong() { return pendingPlacedSong; }
    public void setPendingPlacedSong(Song pendingPlacedSong) { this.pendingPlacedSong = pendingPlacedSong; }

    public Player getPendingActingPlayer() { return pendingActingPlayer; }
    public void setPendingActingPlayer(Player pendingActingPlayer) { this.pendingActingPlayer = pendingActingPlayer; }

    public boolean isPendingPlacementCorrect() { return pendingPlacementCorrect; }
    public void setPendingPlacementCorrect(boolean pendingPlacementCorrect) {
        this.pendingPlacementCorrect = pendingPlacementCorrect;
    }

    public void clearPendingTurnInput() {
        this.pendingGuessedArtist = null;
        this.pendingGuessedTitle = null;
        this.pendingInsertPosition = null;
        this.pendingPlacedSong = null;
        this.pendingActingPlayer = null;
        this.pendingPlacementCorrect = false;
    }

    public int getTurnNumber() { return turnNumber; }
    public void incrementTurnNumber() { this.turnNumber++; }

    public void incrementMissedTurn(Player player) {
        if (player == null) return;

        if (player.getId().equals(player1.getId())) {
            player1MissedTurns++;
        } else if (player.getId().equals(player2.getId())) {
            player2MissedTurns++;
        }
    }

    public int getMissedTurns(Player player) {
        if (player == null) return 0;

        if (player.getId().equals(player1.getId())) return player1MissedTurns;
        if (player.getId().equals(player2.getId())) return player2MissedTurns;

        return 0;
    }

    public void resetMissedTurns(Player player) {
        if (player == null) return;

        if (player.getId().equals(player1.getId())) {
            player1MissedTurns = 0;
        } else if (player.getId().equals(player2.getId())) {
            player2MissedTurns = 0;
        }
    }

    public List<SongCard> getTimelineOfPlayer(Player player) {
        if (player.getId().equals(player1.getId())) {
            return player1Timeline;
        }
        return player2Timeline;
    }

    public Player getOpponent(Player player) {
        if (player.getId().equals(player1.getId())) {
            return player2;
        }
        return player1;
    }

    public Player getPlayerById(String playerId) {
        if (player1.getId().equals(playerId)) return player1;
        if (player2.getId().equals(playerId)) return player2;
        return null;
    }

    public boolean isFinished() {
        return status == GameStatus.FINISHED || phase == GamePhase.FINISHED;
    }
}