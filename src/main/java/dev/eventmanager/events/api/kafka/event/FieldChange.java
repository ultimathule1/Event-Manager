package dev.eventmanager.events.api.kafka.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FieldChange<T> {
    private T oldValue;
    private T newValue;

//    private FieldChange(T oldValue, T newValue) {
//        this.oldValue = oldValue;
//        this.newValue = newValue;
//    }
//
//    public static <T> FieldChange<T> of(T oldValue, T newValue) {
//        return new FieldChange<>(oldValue, newValue);
//    }
}