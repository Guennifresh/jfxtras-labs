package jfxtras.labs.icalendar.properties.calendar;

import jfxtras.labs.icalendar.VCalendar;
import jfxtras.labs.icalendar.properties.PropertyBase;

/**
 * VERSION
 * RFC 5545, 3.7.4, page 79
 * 
 * This property specifies the identifier corresponding to the
 * highest version number or the minimum and maximum range of the
 * iCalendar specification that is required in order to interpret the
 * iCalendar object. 
 * 
 * A value of "2.0" corresponds to this software.
 * 
 * Example:
 * VERSION:2.0
 * 
 * @author David Bal
 * @see VCalendar
 */
public class Version extends PropertyBase<Version, String>
{
    public Version(CharSequence propertyString)
    {
        super(propertyString);
    }
    
    public Version(Version source)
    {
        super(source);
    }
    
    /** Set version to default value of 2.0 */
    public Version()
    {
        super("2.0");
    }
}
