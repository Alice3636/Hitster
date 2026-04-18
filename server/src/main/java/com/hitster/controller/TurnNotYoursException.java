package com.hitster.controller;

public class TurnNotYoursException extends RuntimeException {
    public TurnNotYoursException(String message) {
        super(message);
    }
}