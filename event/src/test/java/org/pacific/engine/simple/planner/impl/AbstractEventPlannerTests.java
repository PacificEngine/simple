package org.pacific.engine.simple.planner.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.pacific.engine.simple.event.Event;
import org.pacific.engine.simple.event.impl.EventImpl;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

public class AbstractEventPlannerTests {
    AbstractEventPlanner planner;

    @BeforeEach
    void setup() {
        planner = new AbstractEventPlanner() {
            @Override
            public boolean isShutdown() {
                return false;
            }

            @Override
            public boolean shutdown() {
                return false;
            }
        };
    }

    @Test
    void noFirstEvent() {
        assertEquals(null, planner.nextEvent());
    }

    @Test
    void singleEvent() {
        Event event = new EventImpl(0, null);
        assertTrue(planner.sendEvent(null, event));
        assertEquals(event.getIdentifier(), Optional.ofNullable(planner.nextEvent()).map(AbstractEventPlanner.PendingEvent::getIdentifier).orElse(null));
        assertEquals(null, planner.nextEvent());
    }

    @Test
    void duplicateEvent() {
        Event event = new EventImpl(0, null);
        assertTrue(planner.sendEvent(null, event));
        assertFalse(planner.sendEvent(null, event));
        assertEquals(event.getIdentifier(), Optional.ofNullable(planner.nextEvent()).map(AbstractEventPlanner.PendingEvent::getIdentifier).orElse(null));
        assertEquals(null, planner.nextEvent());
    }

    @Test
    void nullPriority() {
        Event event1 = new EventImpl(1, null);
        Event event2 = new EventImpl(null, null);
        Event event3 = new EventImpl(0, null);
        assertTrue(planner.sendEvent(null, event1));
        assertTrue(planner.sendEvent(null, event2));
        assertTrue( planner.sendEvent(null, event3));
        assertEquals(event3.getIdentifier(), Optional.ofNullable(planner.nextEvent()).map(AbstractEventPlanner.PendingEvent::getIdentifier).orElse(null));
        assertEquals(event1.getIdentifier(), Optional.ofNullable(planner.nextEvent()).map(AbstractEventPlanner.PendingEvent::getIdentifier).orElse(null));
        assertEquals(event2.getIdentifier(), Optional.ofNullable(planner.nextEvent()).map(AbstractEventPlanner.PendingEvent::getIdentifier).orElse(null));
        assertEquals(null, planner.nextEvent());
    }

    @Test
    void verifyPriority() {
        List<Event> events = IntStream.range(0, 1000).mapToObj(val -> new EventImpl(1000 - val, null)).collect(Collectors.toList());
        events.forEach(event -> assertTrue(planner.sendEvent(null, event)));
        Collections.reverse(events);
        events.forEach(event -> assertEquals(event.getIdentifier(), Optional.ofNullable(planner.nextEvent()).map(AbstractEventPlanner.PendingEvent::getIdentifier).orElse(null)));
        assertEquals(null, planner.nextEvent());
    }

    @Test
    void verifyFIFO() {
        List<Event> events = IntStream.range(0, 1000).mapToObj(val -> new EventImpl(0, null)).collect(Collectors.toList());
        events.forEach(event -> assertTrue(planner.sendEvent(null, event)));
        events.forEach(event -> assertEquals(event.getIdentifier(), Optional.ofNullable(planner.nextEvent()).map(AbstractEventPlanner.PendingEvent::getIdentifier).orElse(null)));
        assertEquals(null, planner.nextEvent());
    }

    @Test
    void cancelEvent() {
        Event event = new EventImpl(0, null);
        assertTrue(planner.sendEvent(null, event));
        assertTrue(planner.cancelEvent(event.getIdentifier()));
        assertEquals(null, planner.nextEvent());
    }

    @Test
    void cancelNonExistent() {
        Event event = new EventImpl(0, null);
        assertFalse(planner.cancelEvent(event.getIdentifier()));
        assertEquals(null, planner.nextEvent());
    }

    @Test
    void cancelCompleted() {
        Event event = new EventImpl(0, null);
        assertTrue(planner.sendEvent(null, event));
        assertEquals(event.getIdentifier(), Optional.ofNullable(planner.nextEvent()).map(AbstractEventPlanner.PendingEvent::getIdentifier).orElse(null));
        assertFalse(planner.cancelEvent(event.getIdentifier()));
        assertEquals(null, planner.nextEvent());
    }

    @Test
    void cancelInCollection() {
        Event event1 = new EventImpl(1, null);
        Event event2 = new EventImpl(null, null);
        Event event3 = new EventImpl(0, null);
        assertTrue(planner.sendEvent(null, event1));
        assertTrue(planner.sendEvent(null, event2));
        assertTrue(planner.sendEvent(null, event3));
        assertTrue(planner.cancelEvent(event1.getIdentifier()));
        assertFalse(planner.cancelEvent(event1.getIdentifier()));
        assertFalse(planner.cancelEvent(event1.getIdentifier()));
        assertFalse(planner.cancelEvent(event1.getIdentifier()));
        assertEquals(event3.getIdentifier(), Optional.ofNullable(planner.nextEvent()).map(AbstractEventPlanner.PendingEvent::getIdentifier).orElse(null));
        assertEquals(event2.getIdentifier(), Optional.ofNullable(planner.nextEvent()).map(AbstractEventPlanner.PendingEvent::getIdentifier).orElse(null));
        assertEquals(null, planner.nextEvent());
    }
}