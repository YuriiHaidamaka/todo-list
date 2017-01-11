package org.spine3.examples.todolist;

import com.beust.jcommander.IStringConverter;
import com.google.protobuf.Timestamp;
import com.google.protobuf.util.Timestamps;
import org.spine3.util.Exceptions;

import java.text.ParseException;

/**
 * @author Illia Shepilov
 */
public class TimeConverter implements IStringConverter<Timestamp> {
    @Override
    public Timestamp convert(String value) {
        try {
            final Timestamp result = Timestamps.parse(value);
            return result;
        } catch (ParseException e) {
            throw Exceptions.wrappedCause(e);
        }
    }
}
