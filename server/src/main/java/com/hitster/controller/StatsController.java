    //    package com.hitster.controller;
//
    //    import com.hitster.model.MatchHistoryObj;
    //    import com.hitster.model.PlayerScore;
    //    import com.hitster.repository.DatabaseLogic;
    //    import org.springframework.http.ResponseEntity;
    //    import org.springframework.web.bind.annotation.*;
//
    //    import java.util.List;
//
    //    @RestController
    //    @RequestMapping("/stats")
    //    public class StatsController {
//
    //        @GetMapping("/leaderboard")
    //        public ResponseEntity<List<PlayerScore>> getLeaderboard() {
    //            List<PlayerScore> leaderboard = DatabaseLogic.getLeaderboardData();
    //            return ResponseEntity.ok(leaderboard);
    //        }
//
    //        @GetMapping("/history/{userId}")
    //        public ResponseEntity<List<MatchHistoryObj>> getMatchHistory(@PathVariable int userId) {
    //            List<MatchHistoryObj> history = DatabaseLogic.getMatchHistory(userId);
    //            return ResponseEntity.ok(history);
    //        }
    //    }