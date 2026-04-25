package com.hitster.controller;

import com.hitster.dto.game.ChallengeRequestDTO;
import com.hitster.dto.game.GameQuitResponseDTO;
import com.hitster.dto.game.GameStateDTO;
import com.hitster.dto.game.GuessSongRequestDTO;
import com.hitster.dto.game.PlaceSongRequestDTO;
import com.hitster.mapper.GameStateMapper;
import com.hitster.model.GameSession;
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
    public GameStateDTO placeSong(@PathVariable String gameId,
                                  @RequestBody PlaceSongRequestDTO request,
                                  HttpServletRequest httpRequest) {
        String playerId = getAuthenticatedPlayerId(httpRequest);

        GameSession session = gameManager.placeSong(gameId, playerId, request);

        return GameStateMapper.toDTO(session);
    }

    @PostMapping("/{gameId}/guess")
    public GameStateDTO guessSong(@PathVariable String gameId,
                                  @RequestBody GuessSongRequestDTO request,
                                  HttpServletRequest httpRequest) {
        String playerId = getAuthenticatedPlayerId(httpRequest);

        GameSession session = gameManager.guessSong(gameId, playerId, request);

        return GameStateMapper.toDTO(session);
    }

    @PostMapping("/{gameId}/challenge")
    public GameStateDTO challengeLastTurn(@PathVariable String gameId,
                                          @RequestBody ChallengeRequestDTO request,
                                          HttpServletRequest httpRequest) {
        String challengerId = getAuthenticatedPlayerId(httpRequest);

        if (request == null) {
            throw new IllegalArgumentException("Request body is required.");
        }

        GameSession session = gameManager.challengeLastTurn(
                gameId,
                challengerId,
                request.suggestedIndex()
        );

        return GameStateMapper.toDTO(session);
    }

    @PostMapping("/{gameId}/challenge/skip")
    public GameStateDTO skipChallengeWindow(@PathVariable String gameId) {
        GameSession session = gameManager.skipChallengeWindow(gameId);

        return GameStateMapper.toDTO(session);
    }

    @PostMapping("/{gameId}/quit")
    public GameQuitResponseDTO quitGame(@PathVariable String gameId,
                                        HttpServletRequest httpRequest) {
        String playerId = getAuthenticatedPlayerId(httpRequest);

        GameSession session = gameManager.quitGame(gameId, playerId);

        return new GameQuitResponseDTO(
                session.getId(),
                session.getStatus().name(),
                session.getWinner() != null ? session.getWinner().getUsername() : null,
                "Player forfeited the match."
        );
    }

    private String getAuthenticatedPlayerId(HttpServletRequest request) {
        Object jwtUserIdObj = request.getAttribute("jwtUserId");

        if (jwtUserIdObj == null) {
            throw new IllegalArgumentException("Missing authenticated user.");
        }

        return String.valueOf(jwtUserIdObj);
    }
}