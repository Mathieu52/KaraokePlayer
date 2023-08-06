package net.xz3ra.www.karaokeplayer.converter;

import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.util.Duration;

import java.util.function.Function;

public class DurationSecondsToDoubleConverter {

    public static ReadOnlyObjectProperty<Double> convert (
            ReadOnlyObjectProperty<Duration> durationProperty,
            Function<Double, Double> conversionFunction
    ) {
        // Create a new wrapper for the Double value
        ReadOnlyObjectWrapper<Double> doubleProperty = new ReadOnlyObjectWrapper<>();

        // Bind the Double property to the Duration property and apply the conversion logic
        durationProperty.addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                doubleProperty.set(conversionFunction.apply(newValue.toSeconds()));
            }
        });

        // Initial conversion for the current value (if not null)
        if (durationProperty.get() != null) {
            doubleProperty.set(conversionFunction.apply(durationProperty.get().toSeconds()));
        }

        return doubleProperty.getReadOnlyProperty();
    }

    // Other utility functions or class members can be added here as needed
}
