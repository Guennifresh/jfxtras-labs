package jfxtras.labs.icalendarfx.itip;

import jfxtras.labs.icalendarfx.properties.calendar.Method.MethodType;

/**
 * Abstract class for iCalendar Transport-Independent Interoperability Protocol (iTIP) RFC 5546
 * 
 * @author David Bal
 *
 */
public abstract class AbstractITIPFactory
{
    public abstract Processable getITIPMessageProcess(MethodType methodType);
}
