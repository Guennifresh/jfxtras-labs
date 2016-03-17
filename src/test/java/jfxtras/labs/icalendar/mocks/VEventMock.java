package jfxtras.labs.icalendar.mocks;

import java.time.DateTimeException;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAmount;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javafx.util.Callback;
import javafx.util.Pair;
import jfxtras.labs.icalendar.DateTimeUtilities;
import jfxtras.labs.icalendar.ICalendarUtilities;
import jfxtras.labs.icalendar.components.VComponentUtilities;
import jfxtras.labs.icalendar.components.VEvent;
import jfxtras.labs.icalendar.components.VEventUtilities;

/**
 * Mock VEvent class for testing
 * 
 * @author David Bal
 *
 */
public class VEventMock extends VEvent<InstanceMock, VEventMock>
{
    private final static Callback<StartEndRange, InstanceMock> NEW_INSTANCE = (p) ->
    {
        return new InstanceMock()
                .withStartTemporal(p.getDateTimeStart())
                .withEndTemporal(p.getDateTimeEnd());
    };
    
    @Override
    public List<InstanceMock> makeInstances(Temporal startRange, Temporal endRange)
    {
        if (DateTimeUtilities.isAfter(startRange, endRange)) throw new DateTimeException("endRange must be after startRange");
        setEndRange(endRange);
        setStartRange(startRange);
        return makeInstances();
    }

    @Override
    public List<InstanceMock> makeInstances()
    {
        List<InstanceMock> madeInstances = new ArrayList<>();
        streamLimitedByRange().forEach(temporalStart ->
        {
            TemporalAmount duration = endType().getDuration(this);
            Temporal temporalEnd = temporalStart.plus(duration);
            InstanceMock instance = new InstanceMock()
                .withStartTemporal(temporalStart)
                .withEndTemporal(temporalEnd)
                .withSummary(getSummary());
            madeInstances.add(instance);
            instances().add(instance);
      });
      return madeInstances;
    }

    /*
     * CONSTRUCTORS
     */
    public VEventMock() { }
    
    /** Copy constructor */
    public VEventMock(VEventMock vevent)
    {
        super(vevent);
    }
    
    /** Make new VEventMock and populate properties by parsing a string of line-separated
     * content lines
     */
    public static VEventMock parse(String vEventString)
    {
        VEventMock vEvent = new VEventMock();
        Iterator<Pair<String, String>> i = ICalendarUtilities.componentStringToPropertyList(vEventString).iterator();
        // TODO - MAYBE MAKE ITERATOR<STRING> AND LEAVE PROPERTY NAME ON
        while (i.hasNext())
        {
            Pair<String, String> propertyValuePair = i.next();
            
            // parse each property-value pair by all associated property enums
            VEventUtilities.parse(vEvent, propertyValuePair);
            // RETURN BOOLEAN IF PARSE IS SUCCESSFUL SO I CAN SKIP NEXT ONES
            // TODO - INSTEAD PARSE COMPLETE LINE - USE propertyLineToParameterMap2
            VComponentUtilities.parse(vEvent, propertyValuePair);
        }
        return vEvent;
    }
    
    public static boolean isEqualTo(VEventMock v1, VEventMock v2)
    {
        return VEventUtilities.isEqualTo(v1, v2, true);
    }
    public static boolean isEqualTo(VEventMock v1, VEventMock v2, boolean verbose)
    {
        return VEventUtilities.isEqualTo(v1, v2, verbose);
    }
}
