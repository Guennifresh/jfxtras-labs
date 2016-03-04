package jfxtras.labs.icalendar;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Test;

import jfxtras.labs.icalendar.ICalendarUtilities.ChangeDialogOption;
import jfxtras.labs.icalendar.mocks.InstanceMock;
import jfxtras.labs.icalendar.mocks.VEventMock;
import jfxtras.labs.icalendar.rrule.RRule;
import jfxtras.labs.icalendar.rrule.freq.Daily;

public class ICalendarEditTest extends ICalendarTestAbstract
{
    /**
     * Tests editing start and end time of ALL events
     */
    @Test
    public void canEditAll1()
    {
        VEventMock vEvent = getDaily2();
        LocalDateTime start = LocalDateTime.of(2015, 11, 15, 0, 0);
        LocalDateTime end = LocalDateTime.of(2015, 11, 22, 0, 0);
        List<VComponent<InstanceMock>> vComponents = new ArrayList<>(Arrays.asList(vEvent));
        List<InstanceMock> instances = new ArrayList<InstanceMock>();
        Collection<InstanceMock> newInstances = vEvent.makeInstances(start, end);
        instances.addAll(newInstances);
        assertEquals(3, instances.size()); // check if there are only 3 instances
        VEventMock vComponentOriginal = new VEventMock(vEvent);
               
        // select InstanceMock and apply changes
        Iterator<InstanceMock> instanceIterator = instances.iterator();
        InstanceMock selectedInstance = instanceIterator.next();
        Temporal startOriginalInstance = selectedInstance.getStartTemporal();
        Temporal startInstance = selectedInstance.getStartTemporal().with(LocalTime.of(9, 45));
        Temporal endInstance = selectedInstance.getEndTemporal().with(LocalTime.of(11, 00));
        
        vEvent.handleEdit(
                  vComponentOriginal
                , vComponents
                , startOriginalInstance
                , startInstance
                , endInstance
                , instances
                , (m) -> ChangeDialogOption.ALL);
        
        // Check start date/times
        List<Temporal> madeDates = instances.stream().map(a -> a.getStartTemporal()).collect(Collectors.toList());
        List<Temporal> expectedDates = new ArrayList<Temporal>(Arrays.asList(
                LocalDateTime.of(2015, 11, 15, 9, 45)
              , LocalDateTime.of(2015, 11, 18, 9, 45)
              , LocalDateTime.of(2015, 11, 21, 9, 45)
                ));
        assertEquals(expectedDates, madeDates);

        // Check edited VEvent
        VEventMock expectedVEvent = getDaily2()
                .withDateTimeStart(LocalDateTime.of(2015, 11, 9, 9, 45))
                .withDuration(Duration.ofMinutes(75))
                .withSequence(1);

        assertTrue(VEventMock.isEqualTo(expectedVEvent, vEvent)); // check to see if repeat rule changed correctly
        assertEquals(3, instances.size()); // check if there are only two instances
    }

    /**
     * Tests ONE event of a daily repeat event changing date and time
     */
    @Test
    public void canEditOne1()
    {
        // Individual InstanceMock
        VEventMock vEvent = getDaily2();
        List<VComponent<InstanceMock>> vComponents = new ArrayList<>(Arrays.asList(vEvent));
        LocalDateTime start = LocalDateTime.of(2015, 11, 15, 0, 0);
        LocalDateTime end = LocalDateTime.of(2015, 11, 22, 0, 0);
        List<InstanceMock> instances = new ArrayList<InstanceMock>();
        Collection<InstanceMock> newInstances = vEvent.makeInstances(start, end);
        instances.addAll(newInstances);
        assertEquals(3, instances.size()); // check if there are only 3 instances
        VEventMock vEventOriginal = new VEventMock(vEvent);
        
        // select InstanceMock and apply changes
        Iterator<InstanceMock> instanceIterator = instances.iterator();
        InstanceMock selectedInstance = instanceIterator.next();
        Temporal startOriginalInstance = selectedInstance.getStartTemporal();
        LocalDate date = LocalDate.from(selectedInstance.getStartTemporal()).plus(1, ChronoUnit.DAYS);
        selectedInstance.setStartTemporal(date.atTime(9, 45)); // change start time
        selectedInstance.setEndTemporal(date.atTime(11, 0)); // change end time
        Temporal startInstance = selectedInstance.getStartTemporal();
        Temporal endInstance = selectedInstance.getEndTemporal();
        
        vEvent.handleEdit(
                vEventOriginal
              , vComponents
              , startOriginalInstance
              , startInstance
              , endInstance
              , instances
              , (m) -> ChangeDialogOption.ONE);

        assertEquals(2, vComponents.size());

        List<Temporal> madeDates = instances.stream()
                .map(a -> a.getStartTemporal())
                .sorted()
                .collect(Collectors.toList());
        List<Temporal> expectedDates = new ArrayList<Temporal>(Arrays.asList(
                LocalDateTime.of(2015, 11, 16, 9, 45)
              , LocalDateTime.of(2015, 11, 18, 10, 0)
              , LocalDateTime.of(2015, 11, 21, 10, 0)
                ));
        assertEquals(expectedDates, madeDates);

        Collections.sort(vComponents, VComponent.VCOMPONENT_COMPARATOR);
        VEventMock vEvent0 = (VEventMock) vComponents.get(0);
        VEventMock vEvent1 = (VEventMock) vComponents.get(1);
        VEventMock expectedVEvent0 = getDaily2()
                .withRRule(new RRule()
                        .withCount(6)
                        .withFrequency(new Daily().withInterval(3))
                        .withRecurrences(vEvent1));
        assertTrue(VEventMock.isEqualTo(expectedVEvent0, vEvent0));
        
        VEventMock expectedVEvent1 = getDaily2()
                .withDateTimeRecurrence(LocalDateTime.of(2015, 11, 15, 10, 0))
                .withDateTimeStart(LocalDateTime.of(2015, 11, 16, 9, 45))
                .withDateTimeStamp(vEvent1.getDateTimeStamp())
                .withDuration(Duration.ofMinutes(75))
                .withRRule(null)
                .withSequence(1);
        assertTrue(VEventMock.isEqualTo(expectedVEvent1, vEvent1));
    }

    @Test
    public void canEditThisAndFuture1()
    {
        VEventMock vEvent = getDaily1();
        List<VComponent<InstanceMock>> vComponents = new ArrayList<>(Arrays.asList(vEvent));
        LocalDateTime start = LocalDateTime.of(2015, 11, 15, 0, 0);
        LocalDateTime end = LocalDateTime.of(2015, 11, 22, 0, 0);
        List<InstanceMock> instances = new ArrayList<InstanceMock>();
        Collection<InstanceMock> newInstances = vEvent.makeInstances(start, end);
        instances.addAll(newInstances);
        assertEquals(7, instances.size()); // check if there are only 3 instances
        VEventMock vEventOriginal = new VEventMock(vEvent);
        
        // select InstanceMock (get recurrence date)
        Iterator<InstanceMock> instanceIterator = instances.iterator();
        instanceIterator.next(); // skip first
        instanceIterator.next(); // skip second
        InstanceMock selectedInstance = instanceIterator.next();
        Temporal startOriginalInstance = selectedInstance.getStartTemporal();
        
        // apply changes
        LocalDate newDate = LocalDate.from(selectedInstance.getStartTemporal());
        selectedInstance.setStartTemporal(newDate.atTime(9, 45)); // change start time
        selectedInstance.setEndTemporal(newDate.atTime(10, 30)); // change end time
        Temporal startInstance = selectedInstance.getStartTemporal();
        Temporal endInstance = selectedInstance.getEndTemporal();
        long startShift = ChronoUnit.NANOS.between(startOriginalInstance, startInstance);
        Temporal dtStart = vEvent.getDateTimeStart().plus(startShift, ChronoUnit.NANOS);
        long duration = ChronoUnit.NANOS.between(selectedInstance.getStartTemporal(), selectedInstance.getEndTemporal());
        Temporal dtEnd = dtStart.plus(duration, ChronoUnit.NANOS);
        vEvent.setDateTimeStart(dtStart);
        vEvent.setDateTimeEnd(dtEnd);

        vEvent.handleEdit(
                vEventOriginal
              , vComponents
              , startOriginalInstance
              , startInstance
              , endInstance
              , instances
              , (m) -> ChangeDialogOption.THIS_AND_FUTURE);
        
        assertEquals(2, vComponents.size());
        
        List<Temporal> madeDates = instances.stream()
                .map(a -> a.getStartTemporal())
                .sorted()
                .collect(Collectors.toList());
        List<Temporal> expectedDates = new ArrayList<Temporal>(Arrays.asList(
                LocalDateTime.of(2015, 11, 15, 10, 0)
              , LocalDateTime.of(2015, 11, 16, 10, 0)
              , LocalDateTime.of(2015, 11, 17, 9, 45)
              , LocalDateTime.of(2015, 11, 18, 9, 45)
              , LocalDateTime.of(2015, 11, 19, 9, 45)
              , LocalDateTime.of(2015, 11, 20, 9, 45)
              , LocalDateTime.of(2015, 11, 21, 9, 45)
                ));
        assertEquals(expectedDates, madeDates);
        
        Collections.sort(vComponents, VComponent.VCOMPONENT_COMPARATOR);
        VEventMock vEvent0 = (VEventMock) vComponents.get(0);
        VEventMock vEvent1 = (VEventMock) vComponents.get(1);
        VEventMock expectedVEvent0 = getDaily1()
                .withRRule(new RRule()
                        .withFrequency(new Daily())
                        .withUntil(LocalDateTime.of(2015, 11, 16, 23, 59, 59)));
        assertTrue(VEventMock.isEqualTo(expectedVEvent0, vEvent0));
        
        VEventMock expectedVEvent1 = getDaily1()
                .withDateTimeEnd(LocalDateTime.of(2015, 11, 17, 10, 30))
                .withDateTimeStamp(vEvent1.getDateTimeStamp())
                .withDateTimeStart(LocalDateTime.of(2015, 11, 17, 9, 45))
                .withRelatedTo("20150110T080000-0@jfxtras.org")
                .withSequence(1)
                .withUniqueIdentifier(vEvent1.getUniqueIdentifier());
        assertTrue(VEventMock.isEqualTo(expectedVEvent1, vEvent1));
    }

    /**
     * Tests changing a repeating event to an individual one
     */
    @Test
    public void canEditAll2()
    {
        // Individual InstanceMock
        VEventMock vEvent = getDaily2();
        List<VComponent<InstanceMock>> vComponents = new ArrayList<>(Arrays.asList(vEvent));
        LocalDateTime start = LocalDateTime.of(2015, 11, 15, 0, 0);
        LocalDateTime end = LocalDateTime.of(2015, 11, 22, 0, 0);
        List<InstanceMock> instances = new ArrayList<InstanceMock>();
        Collection<InstanceMock> newInstances = vEvent.makeInstances(start, end);
        instances.addAll(newInstances);
        assertEquals(3, instances.size()); // check if there are only 3 instances
        VEventMock vEventOriginal = new VEventMock(vEvent);

        // select InstanceMock (get recurrence date)
        Iterator<InstanceMock> instanceIterator = instances.iterator();
        instanceIterator.next(); // skip first
        InstanceMock selectedInstance = instanceIterator.next();
        
        // apply changes
        vEvent.setRRule(null);
        Temporal startOriginalInstance = selectedInstance.getStartTemporal();
        Temporal startInstance = selectedInstance.getStartTemporal();
        Temporal endInstance = selectedInstance.getEndTemporal();

        // Edit
        vEvent.handleEdit(
                vEventOriginal
              , vComponents
              , startOriginalInstance
              , startInstance
              , endInstance
              , instances
              , (m) -> ChangeDialogOption.ALL);

        List<Temporal> madeDates = instances.stream()
                .map(a -> a.getStartTemporal())
                .sorted()
                .collect(Collectors.toList());
        List<Temporal> expectedDates = new ArrayList<Temporal>(Arrays.asList(
                LocalDateTime.of(2015, 11, 18, 10, 0)
                ));
        assertEquals(expectedDates, madeDates);
        
        VEventMock expectedVEvent = getDaily2()
                .withDateTimeStart(LocalDateTime.of(2015, 11, 18, 10, 0))
                .withRRule(null)
                .withSequence(1);
        assertTrue(VEventMock.isEqualTo(expectedVEvent, vEvent));
    }
    
    /**
     * Tests changing a ZonedDateTime repeating event
     */
    @Test
    public void canEditOne2()
    {
        // Individual InstanceMock
        VEventMock vEvent = getWeeklyZoned();
        List<VComponent<InstanceMock>> vComponents = new ArrayList<>(Arrays.asList(vEvent));
        LocalDateTime start = LocalDateTime.of(2015, 11, 15, 0, 0);
        LocalDateTime end = LocalDateTime.of(2015, 11, 22, 0, 0);
        List<InstanceMock> instances = new ArrayList<InstanceMock>();
        Collection<InstanceMock> newInstances = vEvent.makeInstances(start, end);
        instances.addAll(newInstances);
        assertEquals(3, instances.size()); // check if there are only 3 instances
        VEventMock vEventOriginal = new VEventMock(vEvent);

        // select InstanceMock (get recurrence date)
        Iterator<InstanceMock> instanceIterator = instances.iterator();
        instanceIterator.next(); // skip first
        InstanceMock selectedInstance = instanceIterator.next();
        
        // apply changes
        Temporal startOriginalInstance = selectedInstance.getStartTemporal();
        Temporal startInstance = selectedInstance.getStartTemporal().with(LocalTime.of(9, 45)).minus(1, ChronoUnit.DAYS);
        Temporal endInstance = selectedInstance.getEndTemporal().with(LocalTime.of(10, 30)).minus(1, ChronoUnit.DAYS);

        // Edit
        vEvent.handleEdit(
                vEventOriginal
              , vComponents
              , startOriginalInstance
              , startInstance
              , endInstance
              , instances
              , (m) -> ChangeDialogOption.ONE);

        List<Temporal> madeDates = instances.stream()
                .map(a -> a.getStartTemporal())
                .sorted()
                .collect(Collectors.toList());

        List<Temporal> expectedDates = new ArrayList<Temporal>(Arrays.asList(
                ZonedDateTime.of(LocalDateTime.of(2015, 11, 16, 10, 0), ZoneId.of("America/Los_Angeles"))
              , ZonedDateTime.of(LocalDateTime.of(2015, 11, 17, 9, 45), ZoneId.of("America/Los_Angeles"))
              , ZonedDateTime.of(LocalDateTime.of(2015, 11, 20, 10, 0), ZoneId.of("America/Los_Angeles"))
                ));
        assertEquals(expectedDates, madeDates);

        VEventMock expectedVEvent = getWeeklyZoned()
                .withRRule(null)
                .withDateTimeStart(ZonedDateTime.of(LocalDateTime.of(2015, 11, 17, 9, 45), ZoneId.of("America/Los_Angeles")))
                .withDateTimeEnd(ZonedDateTime.of(LocalDateTime.of(2015, 11, 17, 10, 30), ZoneId.of("America/Los_Angeles")))
                .withDateTimeRecurrence(ZonedDateTime.of(LocalDateTime.of(2015, 11, 18, 10, 0), ZoneId.of("America/Los_Angeles")))
                .withDateTimeStamp(vEvent.getDateTimeStamp())
                .withSequence(1);
        assertTrue(VEventMock.isEqualTo(expectedVEvent, vEvent));

        VEventMock expectedVEvent2 = getWeeklyZoned();
        expectedVEvent2.getRRule().recurrences().add(vEvent);
        assertTrue(VEventMock.isEqualTo(expectedVEvent2, vEventOriginal));
    }
    
    /**
     * Changing a event with an exception to wholeday
     */
    @Test
    public void canChangeToWholeDay()
    {
        // Individual InstanceMock
        VEventMock vEvent = getGoogleWithExDates();
        List<VComponent<InstanceMock>> vComponents = new ArrayList<>(Arrays.asList(vEvent));
        LocalDateTime start = LocalDateTime.of(2016, 2, 7, 0, 0);
        LocalDateTime end = LocalDateTime.of(2016, 2, 14, 0, 0);
        List<InstanceMock> instances = new ArrayList<InstanceMock>();
        Collection<InstanceMock> newInstances = vEvent.makeInstances(start, end);
        instances.addAll(newInstances);
        assertEquals(4, instances.size()); // check if there are only 3 instances
        VEventMock vEventOriginal = new VEventMock(vEvent);

        // apply changes
        Temporal startOriginalInstance = ZonedDateTime.of(LocalDateTime.of(2016, 2, 8, 12, 30), ZoneId.of("America/Los_Angeles"));
        Temporal startInstance = LocalDate.of(2016, 2, 8);
        Temporal endInstance = LocalDate.of(2016, 2, 9);

        // Edit
        vEvent.handleEdit(
                vEventOriginal
              , vComponents
              , startOriginalInstance
              , startInstance
              , endInstance
              , instances
              , (m) -> ChangeDialogOption.ONE);

        List<Temporal> madeDates = instances.stream()
                .map(a -> a.getStartTemporal())
                .sorted(VComponent.TEMPORAL_COMPARATOR)
                .collect(Collectors.toList());

        List<Temporal> expectedDates = new ArrayList<>(Arrays.asList(
                ZonedDateTime.of(LocalDateTime.of(2016, 2, 7, 12, 30), ZoneId.of("America/Los_Angeles"))
              , LocalDate.of(2016, 2, 8)
              , ZonedDateTime.of(LocalDateTime.of(2016, 2, 11, 12, 30), ZoneId.of("America/Los_Angeles"))
              , ZonedDateTime.of(LocalDateTime.of(2016, 2, 13, 12, 30), ZoneId.of("America/Los_Angeles"))
                ));
        assertEquals(expectedDates, madeDates);

        VEventMock expectedVEvent = getGoogleWithExDates()
                .withRRule(null)
                .withDateTimeStart(LocalDate.of(2016, 2, 8))
                .withDateTimeEnd(LocalDate.of(2016, 2, 9))
                .withDateTimeStamp(vEvent.getDateTimeStamp())
                .withDateTimeRecurrence(ZonedDateTime.of(LocalDateTime.of(2016, 2, 8, 12, 30), ZoneId.of("America/Los_Angeles")))
                .withSequence(1);
        assertTrue(VEventMock.isEqualTo(expectedVEvent, vEvent));

        VEventMock expectedVEvent2 = getGoogleWithExDates()
                .withDateTimeCreated(vEventOriginal.getDateTimeCreated()); // set by system time, so need to copy
        expectedVEvent2.getRRule().recurrences().add(vEvent);
        assertTrue(VEventMock.isEqualTo(expectedVEvent2, vEventOriginal));
    }
    
    /**
     * Changing two instances to wholeday
     * Tests keeping RECURRENCE-ID as parent DateTimeType
     */
    @Test
    public void canChangeTwoWholeDay()
    {
        VEventMock vEvent = getGoogleRepeatable();
        List<VComponent<InstanceMock>> vComponents = new ArrayList<>(Arrays.asList(vEvent));
        Temporal start = ZonedDateTime.of(LocalDateTime.of(2016, 2, 21, 0, 0), ZoneId.of("America/Los_Angeles"));
        Temporal end = ZonedDateTime.of(LocalDateTime.of(2016, 2, 28, 0, 0), ZoneId.of("America/Los_Angeles"));
        List<InstanceMock> instances = new ArrayList<InstanceMock>();
        Collection<InstanceMock> newInstances = vEvent.makeInstances(start, end);
        instances.addAll(newInstances);
        assertEquals(3, instances.size()); // check if there are only 3 instances
        VEventMock vEventOriginal = new VEventMock(vEvent);
        
        // apply changes
        Temporal startOriginalInstance = ZonedDateTime.of(LocalDateTime.of(2016, 2, 23, 8, 0), ZoneId.of("America/Los_Angeles"));
        Temporal startInstance = LocalDate.of(2016, 2, 22);
        Temporal endInstance = LocalDate.of(2016, 2, 23);

        // Edit
        vEvent.handleEdit(
                vEventOriginal
              , vComponents
              , startOriginalInstance
              , startInstance
              , endInstance
              , instances
              , (m) -> ChangeDialogOption.ONE);

        List<Temporal> madeDates = instances.stream()
                .map(a -> a.getStartTemporal())
                .sorted(VComponent.TEMPORAL_COMPARATOR)
                .collect(Collectors.toList());
        List<Temporal> expectedDates = new ArrayList<Temporal>(Arrays.asList(
                ZonedDateTime.of(LocalDateTime.of(2016, 2, 21, 8, 0), ZoneId.of("America/Los_Angeles"))
              , LocalDate.of(2016, 2, 22)
              , ZonedDateTime.of(LocalDateTime.of(2016, 2, 26, 8, 0), ZoneId.of("America/Los_Angeles"))
                ));
        assertEquals(expectedDates, madeDates);

        VEventMock expectedVEvent = getGoogleRepeatable()
                .withRRule(null)
                .withDateTimeStart(LocalDate.of(2016, 2, 22))
                .withDateTimeEnd(LocalDate.of(2016, 2, 23))
                .withDateTimeRecurrence(ZonedDateTime.of(LocalDateTime.of(2016, 2, 23, 8, 0), ZoneId.of("America/Los_Angeles")))
                .withDateTimeStamp(vEvent.getDateTimeStamp())
                .withSequence(1);
        assertTrue(VEventMock.isEqualTo(expectedVEvent, vEvent));

        VEventMock expectedVEvent2 = getGoogleRepeatable();
        expectedVEvent2.getRRule().recurrences().add(vEvent);
        assertTrue(VEventMock.isEqualTo(expectedVEvent2, vEventOriginal));
        
        // apply changes
        VEventMock vEventOriginal2 = new VEventMock(vEventOriginal);
        Temporal startOriginalInstance2 = ZonedDateTime.of(LocalDateTime.of(2016, 2, 26, 8, 0), ZoneId.of("America/Los_Angeles"));
        Temporal startInstance2 = LocalDate.of(2016, 2, 26);
        Temporal endInstance2 = LocalDate.of(2016, 2, 27);       
        
        // Edit
        vEventOriginal.handleEdit(
                vEventOriginal2
              , vComponents
              , startOriginalInstance2
              , startInstance2
              , endInstance2
              , instances
              , (m) -> ChangeDialogOption.ONE);
        
        List<Temporal> madeDates2 = instances.stream()
                .map(a -> a.getStartTemporal())
                .sorted(VComponent.TEMPORAL_COMPARATOR)
                .collect(Collectors.toList());
        List<Temporal> expectedDates2 = new ArrayList<>(Arrays.asList(
                ZonedDateTime.of(LocalDateTime.of(2016, 2, 21, 8, 0), ZoneId.of("America/Los_Angeles"))
              , LocalDate.of(2016, 2, 22)
              , LocalDate.of(2016, 2, 26)
                ));
        
        vComponents.stream().forEach(System.out::println);
        assertEquals(expectedDates2, madeDates2);
    }
    
    
    /**
     * Changing two instances to wholeday
     * Tests keeping RECURRENCE-ID as parent DateTimeType
     */
    @Test
    public void canChangeOneWholeDayToDateTime()
    {
        VEventMock vEvent = getWholeDayDaily2();
        List<VComponent<InstanceMock>> vComponents = new ArrayList<>(Arrays.asList(vEvent));
        Temporal start = LocalDate.of(2015, 11, 15);
        Temporal end = LocalDate.of(2015, 11, 22);
        List<InstanceMock> instances = new ArrayList<InstanceMock>();
        Collection<InstanceMock> newInstances = vEvent.makeInstances(start, end);
        instances.addAll(newInstances);
        assertEquals(3, instances.size()); // check if there are only 3 instances
        VEventMock vEventOriginal = new VEventMock(vEvent);
        
        // apply changes
        Temporal startOriginalInstance = LocalDate.of(2015, 11, 18);
        Temporal startInstance = ZonedDateTime.of(LocalDateTime.of(2015, 11, 18, 8, 0), ZoneId.of("America/New_York"));
        Temporal endInstance = ZonedDateTime.of(LocalDateTime.of(2015, 11, 18, 10, 0), ZoneId.of("America/New_York"));
        
        // Edit
        vEvent.handleEdit(
                vEventOriginal
              , vComponents
              , startOriginalInstance
              , startInstance
              , endInstance
              , instances
              , (m) -> ChangeDialogOption.ONE);
        
        List<Temporal> madeDates = instances.stream()
                .map(a -> a.getStartTemporal())
                .sorted(VComponent.TEMPORAL_COMPARATOR)
                .collect(Collectors.toList());
        List<Temporal> expectedDates = new ArrayList<Temporal>(Arrays.asList(
                  LocalDate.of(2015, 11, 15)
                , ZonedDateTime.of(LocalDateTime.of(2015, 11, 18, 8, 0), ZoneId.of("America/New_York"))
                , LocalDate.of(2015, 11, 21)
                ));
        assertEquals(expectedDates, madeDates);
    }

    /**
     * For a recurrence set comprising of 2 VComponents, one the parent with a RRULE and
     * a individual child with a RECURRENCE-ID, can the parent DTSTART be edited,
     * with the ALL option, and have the RECURRENCE-ID of the child be updated appropriately.
     */
    @Test
    public void canUpdateRecurrenceIdWhenEditAll()
    {
        List<VComponent<InstanceMock>> vComponents = getRecurrenceSetDaily1();
        VEventMock vEvent = (VEventMock) vComponents.get(0);
        Temporal start = LocalDateTime.of(2015, 11, 8, 0, 0);
        Temporal end = LocalDateTime.of(2015, 11, 15, 0, 0);
        List<InstanceMock> instances = new ArrayList<InstanceMock>();
        vComponents.stream().forEach(v -> instances.addAll(v.makeInstances(start, end)));
        assertEquals(6, instances.size());
        VEventMock vEventOriginal = new VEventMock(vEvent);
        
        // apply changes
        Temporal startOriginalInstance = LocalDateTime.of(2015, 11, 10, 10, 0);
        Temporal startInstance = LocalDateTime.of(2015, 11, 10, 9, 30);
        Temporal endInstance = LocalDateTime.of(2015, 11, 10, 10, 30);
        
        // Edit
        vEvent.handleEdit(
                vEventOriginal
              , vComponents
              , startOriginalInstance
              , startInstance
              , endInstance
              , instances
              , (m) -> ChangeDialogOption.ALL);
        
        // verify changes
        List<Temporal> madeDates = instances.stream()
                .map(a -> a.getStartTemporal())
                .sorted(VComponent.TEMPORAL_COMPARATOR)
                .collect(Collectors.toList());
        List<Temporal> expectedDates = new ArrayList<Temporal>(Arrays.asList(
                LocalDateTime.of(2015, 11, 9, 9, 30)
              , LocalDateTime.of(2015, 11, 10, 15, 0)
              , LocalDateTime.of(2015, 11, 11, 9, 30)
              , LocalDateTime.of(2015, 11, 13, 6, 0)
              , LocalDateTime.of(2015, 11, 13, 9, 30)
              , LocalDateTime.of(2015, 11, 14, 9, 30)
                ));
        assertEquals(expectedDates, madeDates);
    }
}
