package io.github.timemachinelab.service.model;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Emits structured import-agent stream events.
 */
@FunctionalInterface
public interface ImportAgentStreamEmitter {

    void emit(ImportAgentStreamEvent event);

    default void status(String phase, String message) {
        emit(ImportAgentStreamEvent.status(phase, message));
    }

    default void thinking(String stage, String title, String summary) {
        thinking(stage, title, summary, null);
    }

    default void thinking(String stage, String title, String summary, String detail) {
        emit(ImportAgentStreamEvent.thinking(stage, title, summary, detail, 0));
    }

    default void message(ImportAgentActorType actorType, String delta) {
        emit(ImportAgentStreamEvent.message(actorType, delta));
    }

    static ImportAgentStreamEmitter noop() {
        return event -> {
        };
    }

    static ImportAgentStreamEmitter withSequence(ImportAgentStreamEmitter delegate) {
        if (delegate == null) {
            return noop();
        }
        AtomicLong sequence = new AtomicLong(0);
        return new ImportAgentStreamEmitter() {
            @Override
            public void emit(ImportAgentStreamEvent event) {
                delegate.emit(event);
            }

            @Override
            public void thinking(String stage, String title, String summary, String detail) {
                delegate.emit(ImportAgentStreamEvent.thinking(stage, title, summary, detail, sequence.incrementAndGet()));
            }
        };
    }
}
