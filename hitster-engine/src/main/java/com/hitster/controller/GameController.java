package com.hitster.controller;

import com.hitster.dto.GameStateDTO;
import com.hitster.mapper.GameStateMapper;
import com.hitster.model.GameSession;
import com.hitster.model.TurnAction;
import com.hitster.model.TurnResult;
import com.hitster.service.GameManager;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/game")
public class GameController {

    private final GameManager gameManager;

    public GameController(GameManager gameManager) {
        this.gameManager = gameManager;
    }

    @GetMapping("/{gameId}/state")
    public GameStateDTO getGameState(@PathVariable String gameId) {
        GameSession session = gameManager.getGameById(gameId);

        if (session == null) {
            throw new IllegalArgumentException("Game not found: " + gameId);
        }

        return GameStateMapper.toDTO(session);
    }

    @PostMapping("/{gameId}/turn")
    public TurnResult submitTurn(
            @PathVariable String gameId,
            @RequestBody TurnAction action
    ) {
        return gameManager.submitTurn(gameId, action);
    }

    @PostMapping("/{gameId}/challenge")
    public boolean challenge(
            @PathVariable String gameId,
            @RequestParam String challengerId
    ) {
        return gameManager.challengeLastTurn(gameId, challengerId);
    }
    @PostMapping("/{gameId}/challenge/skip")
    public void skipChallengeWindow(@PathVariable String gameId) {
        gameManager.skipChallengeWindow(gameId);
    }
}