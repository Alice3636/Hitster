package com.hitster.controller;

import com.hitster.dto.game.ChallengeRequestDTO;
import com.hitster.dto.game.GameQuitResponseDTO;
import com.hitster.dto.game.GameStateDTO;
import com.hitster.dto.game.GuessSongRequestDTO;
import com.hitster.dto.game.PlaceSongRequestDTO;
import com.hitster.mapper.GameStateMapper;
import com.hitster.model.GameSession;
import com.hitster.model.TurnResult;
import com.hitster.service.GameManager;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/games")
public class GameController {

    private final GameManager gameManager;

    public GameController(GameManager gameManager) {
        this.gameManager = gameManager;
    }

    @GetMapping("/{gameId}/state")
    public GameStateDTO getGameState(@PathVariable String gameId) {
        GameSession session = gameManager.getGameById(gameId);

        if (session == null) {
            throw new NotFoundException("Game not found: " + gameId);
        }

        return GameStateMapper.toDTO(session);
    }

    @PostMapping("/{gameId}/place")
    public TurnResult placeSong(@PathVariable String gameId,
                                @RequestBody PlaceSongRequestDTO request,
                                HttpServletRequest httpRequest) {
        Object jwtUserIdObj = httpRequest.getAttribute("jwtUserId");

        if (jwtUserIdObj == null) {
            throw new IllegalArgumentException("Missing authenticated user.");
        }

        String playerId = String.valueOf(jwtUserIdObj);
        // שימוש בגישה של Record: request.indexPosition() ו-request.songId()
        return gameManager.placeSong(gameId, playerId, request);
    }

    @PostMapping("/{gameId}/guess")
    public TurnResult guessSong(@PathVariable String gameId,
                                @RequestBody GuessSongRequestDTO request,
                                HttpServletRequest httpRequest) {
        Object jwtUserIdObj = httpRequest.getAttribute("jwtUserId");

        if (jwtUserIdObj == null) {
            throw new IllegalArgumentException("Missing authenticated user.");
        }

        String playerId = String.valueOf(jwtUserIdObj);
        return gameManager.guessSong(gameId, playerId, request);
    }

    @PostMapping("/{gameId}/challenge")
    public boolean challengeLastTurn(@PathVariable String gameId,
                                     @RequestBody ChallengeRequestDTO request,
                                     HttpServletRequest httpRequest) {
        Object jwtUserIdObj = httpRequest.getAttribute("jwtUserId");

        if (jwtUserIdObj == null) {
            throw new IllegalArgumentException("Missing authenticated user.");
        }

        if (request == null) {
            throw new IllegalArgumentException("Request body is required.");
        }

        String challengerId = String.valueOf(jwtUserIdObj);
        
        // תיקון השגיאה מצילום המסך: ב-Record משתמשים ב-suggestedIndex() במקום ב-getSuggestedIndex()
        return gameManager.challengeLastTurn(gameId, challengerId, request.suggestedIndex());
    }

    @PostMapping("/{gameId}/challenge/skip")
    public void skipChallengeWindow(@PathVariable String gameId) {
        gameManager.skipChallengeWindow(gameId);
    }

    @PostMapping("/{gameId}/quit")
    public GameQuitResponseDTO quitGame(@PathVariable String gameId,
                                        HttpServletRequest httpRequest) {
        Object jwtUserIdObj = httpRequest.getAttribute("jwtUserId");

        if (jwtUserIdObj == null) {
            throw new IllegalArgumentException("Missing authenticated user.");
        }

        String playerId = String.valueOf(jwtUserIdObj);
        GameSession session = gameManager.quitGame(gameId, playerId);

        return new GameQuitResponseDTO(
                session.getId(),
                session.getStatus().name(),
                session.getWinner() != null ? session.getWinner().getUsername() : null,
                "Player forfeited the match."
        );
    }
}