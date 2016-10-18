package jfxtras.labs.icalendaragenda.scene.control.agenda.icalendar.editors.revisors;

import java.util.List;

import jfxtras.labs.icalendaragenda.scene.control.agenda.icalendar.ICalendarAgenda;
import jfxtras.labs.icalendaragenda.scene.control.agenda.icalendar.editors.ChangeDialogOption;
import jfxtras.labs.icalendarfx.VCalendar;
import jfxtras.labs.icalendarfx.properties.calendar.Method.MethodType;
import jfxtras.labs.icalendarfx.properties.calendar.Version;

/**
 * Interface for the edit behavior of a VComponent
 * 
 * <p>Reviser options from {@link ChangeDialogOption} include:
 * <ul>
 * <li>One
 * <li>All
 * <li>All and ignore recurrences
 * <li>This-and-Future
 * <li>This-and-Future and ignore recurrences 
 * </ul>
 * </p>
 * 
 * @author David Bal
 *
 */
public interface Reviser
{
    /** Revise list of iTIP VCalendar components that represent the changes. */
    List<VCalendar> revise();
    
    // TODO - ADD A INITIALIZE METHOD FOR AN ARRAY OF INPUT OBJECT PARAMETERS

    /* EMPTY iTIP VCALENDAR MESSAGES 
     * These convenience factory methods return an empty VCalendar with the
     * necessary properties set for various types if iTIP messages including
     * PUBLISH, REQUEST and CANCEL */
    public static VCalendar emptyPublishiTIPMessage()
    {
        return new VCalendar()
                .withMethod(MethodType.PUBLISH)
                .withProductIdentifier(ICalendarAgenda.DEFAULT_PRODUCT_IDENTIFIER)
                .withVersion(new Version());
    }

    public static VCalendar emptyRequestiTIPMessage()
    {
        return new VCalendar()
                .withMethod(MethodType.REQUEST)
                .withProductIdentifier(ICalendarAgenda.DEFAULT_PRODUCT_IDENTIFIER)
                .withVersion(new Version());
    }
}
