package jfxtras.labs.icalendar.properties.component.time;

import java.time.LocalDateTime;

import jfxtras.labs.icalendar.properties.PropertyBase;
import jfxtras.labs.icalendar.properties.component.time.end.DTEndLocalDateTime;
import jfxtras.labs.icalendar.properties.component.time.start.DTStartLocalDateTime;
import jfxtras.labs.icalendar.utilities.DateTimeUtilities;

/**
 * Abstract class for all local-date-time classes
 * 
 * @author David Bal
 *
 * @param <T> - implementation class
 * @see DTStartLocalDateTime
 * @see DTEndLocalDateTime
 */
public abstract class DateTimeAbstract<T> extends PropertyBase<T, LocalDateTime> implements DateTime<LocalDateTime>
{
    public DateTimeAbstract(LocalDateTime temporal)
    {
        super(temporal);
    }

    public DateTimeAbstract(CharSequence contentLine)
    {
        super(contentLine);
    }
    
    public DateTimeAbstract(DateTimeAbstract<T> source)
    {
        super(source);
    }
    
    @Override
    protected LocalDateTime valueFromString(String propertyValueString)
    {
        return LocalDateTime.parse(propertyValueString, DateTimeUtilities.LOCAL_DATE_TIME_FORMATTER);
    }
    
    @Override
    protected String valueToString(LocalDateTime value)
    {
        return DateTimeUtilities.LOCAL_DATE_TIME_FORMATTER.format(value);  
    }
}
