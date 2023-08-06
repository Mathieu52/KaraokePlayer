package net.xz3ra.www.karaokeplayer;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.util.Duration;
import net.xz3ra.www.karaokeplayer.manager.TimeManager;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class FpsControlledService extends Service<Void> {
    private final TimeManager timeManager;
    private final int targetFps;
    private final Duration frameDuration;

    private final List<Consumer<Long>> callbacks = new ArrayList<>();

    public FpsControlledService(TimeManager timeManager, int targetFps) {
        this.timeManager = timeManager;
        this.targetFps = targetFps;
        this.frameDuration = Duration.millis(1000.0 / targetFps);
    }

    // Method to add a callback function to the loop
    public void addCallback(Consumer<Long> callback) {
        callbacks.add(callback);
    }

    @Override
    protected Task<Void> createTask() {
        return new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                Timeline timeline = new Timeline(
                        new KeyFrame(Duration.ZERO, event -> {
                            // Update TimeManager and notify listeners of the new perceived time
                            timeManager.update();
                            notifyListeners(timeManager.currentTimeMillis());
                        }),
                        new KeyFrame(frameDuration) // Controlled frame rate
                );
                timeline.setCycleCount(Timeline.INDEFINITE);
                timeline.play();
                return null;
            }
        };
    }

    // Method to notify registered callback functions about the new perceived time
    private void notifyListeners(long timeInMillis) {
        for (Consumer<Long> callback : callbacks) {
            callback.accept(timeInMillis);
        }
    }
}
