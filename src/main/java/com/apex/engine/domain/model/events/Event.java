package com.apex.engine.domain.model.events;

public abstract class Event {
    private int value;

    public Event() {
    }

    public Event(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }
}
