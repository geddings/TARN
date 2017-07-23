package net.floodlightcontroller.tarn;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by geddingsbarrineau on 6/19/17.
 * j
 */
public class HMM {
    final protected BigDecimal complexity;
    final protected BigDecimal entropyRate;

    final protected List<State> states;
    final protected List<Event> events;

    HMM(String complexity, String entropyRate, List<State> states, List<Event> events) {
        this.complexity = new BigDecimal(complexity);
        this.entropyRate = new BigDecimal(entropyRate);
        this.states = new ArrayList<>(states);
        this.events = new ArrayList<>(events);
    }

    protected static class State {
        final int id;

        public State(int id) {
            this.id = id;
        }

    }

    protected static class Event {
        final int id;
        final String name;
        final BigDecimal value;

        public Event(int id, String name, String value) {
            this.id = id;
            this.name = name;
            this.value = new BigDecimal(value);
        }
    }
}
