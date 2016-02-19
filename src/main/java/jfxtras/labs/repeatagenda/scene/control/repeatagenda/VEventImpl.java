package jfxtras.labs.repeatagenda.scene.control.repeatagenda;

import java.time.DateTimeException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAmount;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import javafx.beans.InvalidationListener;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.ObservableList;
import javafx.util.Callback;
import jfxtras.labs.repeatagenda.scene.control.repeatagenda.icalendar.ExDate;
import jfxtras.labs.repeatagenda.scene.control.repeatagenda.icalendar.RDate;
import jfxtras.labs.repeatagenda.scene.control.repeatagenda.icalendar.VComponent;
import jfxtras.labs.repeatagenda.scene.control.repeatagenda.icalendar.VEvent;
import jfxtras.labs.repeatagenda.scene.control.repeatagenda.icalendar.rrule.RRule;
import jfxtras.scene.control.agenda.Agenda;
import jfxtras.scene.control.agenda.Agenda.Appointment;
import jfxtras.scene.control.agenda.Agenda.AppointmentGroup;

/**
 * Concrete class as an example of a VEvent.
 * This class creates and edits appointments for display in Agenda.
 * 
 * 
 * @author David Bal
 *
 */
public class VEventImpl extends VEvent<Appointment>
{
    /*
     *  STATIC CALLBACKS FOR MAKING NEW APPOINTMENTS
     *  One for each of the four date and date-time options in iCalendar:
     *  Date
     *  Date with local time
     *  Date with UTC time
     *  Date with local time and time zone instance
     *  see iCalendar RFC 5545, page 32-33
     *  
     *  Only date-time properties are set in the callbacks.  The other properties
     *  are set in makeInstances method.
     */    
    /** For DATE type (whole-day Appointments) */
    private final static Callback<StartEndPair, Appointment> NEW_DATE_INSTANCE = (p) ->
    {
        LocalDateTime s = LocalDate.from(p.getDateTimeStart()).atStartOfDay();
        LocalDateTime e = LocalDate.from(p.getDateTimeEnd()).atStartOfDay();
        return new Agenda.AppointmentImplLocal()
                .withStartLocalDateTime(s)
                .withEndLocalDateTime(e);
    };

    /** For DATE_WITH_LOCAL_TIME */
    private final static Callback<StartEndPair, Appointment> NEW_DATE_WITH_LOCAL_TIME_INSTANCE = (p) ->
    {
        return new Agenda.AppointmentImplLocal()
                .withStartLocalDateTime(LocalDateTime.from(p.getDateTimeStart()))
                .withEndLocalDateTime(LocalDateTime.from(p.getDateTimeEnd()));
    };

    /** For DATE_WITH_UTC_TIME */
    private final static Callback<StartEndPair, Appointment> NEW_DATE_WITH_UTC_TIME_INSTANCE = (p) ->
    {
        final ZonedDateTime s;
        if (p.getDateTimeStart() instanceof ZonedDateTime)
        {
            s = ((ZonedDateTime) p.getDateTimeStart()).withZoneSameInstant(ZoneId.of("Z"));
        } else
        {
            s = LocalDateTime.from(p.getDateTimeStart()).atZone(ZoneId.of("Z"));
        }

        final ZonedDateTime e;
        if (p.getDateTimeEnd() instanceof ZonedDateTime)
        {
            e = ((ZonedDateTime) p.getDateTimeEnd()).withZoneSameInstant(ZoneId.of("Z"));
        } else
        {
            e = LocalDateTime.from(p.getDateTimeEnd()).atZone(ZoneId.of("Z"));
        }
        return new Agenda.AppointmentImplZoned()
                .withStartZonedDateTime(s)
                .withEndZonedDateTime(e);
    };
    
    /** For DATE_WITH_LOCAL_TIME_AND_TIME_ZONE */
    private final static Callback<StartEndPair, Appointment> NEW_DATE_WITH_LOCAL_TIME_AND_TIME_ZONE_INSTANCE = (p) ->
    {
        return new Agenda.AppointmentImplZoned()
                .withStartZonedDateTime((ZonedDateTime) p.getDateTimeStart())
                .withEndZonedDateTime((ZonedDateTime) p.getDateTimeEnd());
    };

    // Map to match up DateTimeType to Callback;
    private static final Map<DateTimeType, Callback<StartEndPair, Appointment>> DATE_TIME_MAKE_INSTANCE_MAP = defaultDateTimeInstanceMap();
    private static Map<DateTimeType, Callback<StartEndPair, Appointment>> defaultDateTimeInstanceMap()
    {
        Map<DateTimeType, Callback<StartEndPair, Appointment>> map = new HashMap<>();
        map.put(DateTimeType.DATE, NEW_DATE_INSTANCE);
        map.put(DateTimeType.DATE_WITH_LOCAL_TIME, NEW_DATE_WITH_LOCAL_TIME_INSTANCE);
        map.put(DateTimeType.DATE_WITH_UTC_TIME, NEW_DATE_WITH_UTC_TIME_INSTANCE);
        map.put(DateTimeType.DATE_WITH_LOCAL_TIME_AND_TIME_ZONE, NEW_DATE_WITH_LOCAL_TIME_AND_TIME_ZONE_INSTANCE);
        return map;
    }

    
//    // TODO - I THINK I'M GOING TO USE CATEGORIES INSTEAD OF BELOW X CUSTOM ELEMENT
//    /**
//     * X-APPOINTMENT-GROUP
//     * Contains the AppointmentGroup from Agenda.
//     * Non-Standard iCalendar Property (3.8.8.2 in RFC 5545 iCalendar)
//     * The css StyleClass is the value portion of this property outputed by toString.
//     * StyleClass must be unique for each AppointmentGroup.
//     */
//    public StringProperty appointmentGroupStyleClassProperty() { return appointmentGroupStyleClass; }
//    private StringProperty appointmentGroupStyleClass = new SimpleStringProperty(this, "X-APPOINTMENT-GROUP");
//    public void setAppointmentGroupStyleClass(String appointmentGroupStyleClass) { this.appointmentGroupStyleClass.set(appointmentGroupStyleClass); }
//    public String getAppointmentGroupStyleClass() { return appointmentGroupStyleClass.get(); }
//
    public ObjectProperty<AppointmentGroup> appointmentGroupProperty() { return appointmentGroup; }
    private ObjectProperty<AppointmentGroup> appointmentGroup = new SimpleObjectProperty<AppointmentGroup>(this, "CATEGORIES");
    public void setAppointmentGroup(AppointmentGroup appointmentGroup) { this.appointmentGroup.set(appointmentGroup); }
    public AppointmentGroup getAppointmentGroup() { return appointmentGroup.get(); }
    public VEventImpl withAppointmentGroup(AppointmentGroup appointmentGroup) { setAppointmentGroup(appointmentGroup); return this; }

    /** appointmentGroups from Agenda.  It is used to synch categories to appointmentGroup, 
     * which is needed by the makeAppointment method 
     * @see #makeInstances() */
    public List<AppointmentGroup> getAppointmentGroups() { return appointmentGroups; }
    final private List<AppointmentGroup> appointmentGroups;

    /* below listeners ensures appointmentGroup description and categories match.  
     * added to categoriesProperty and appointmentGroups by the constructor.
     * appointmentGroups must be set
     */
    private final InvalidationListener categoriesListener = obs ->
    {
//        System.out.println("finding appointment group:" + getAppointmentGroups().isEmpty() + " " + getCategories());
        if (! getAppointmentGroups().isEmpty() && getCategories() != null)
        {
            Optional<AppointmentGroup> myGroup = getAppointmentGroups()
                    .stream()
//                    .peek(a -> System.out.println(a.getDescription()))
                    .filter(g -> g.getDescription().equals(getCategories()))
                    .findFirst();
//            if (! myGroup.isPresent()) System.out.println("no matched group:");
            if (myGroup.isPresent()) setAppointmentGroup(myGroup.get());                
        }
    };
    private final ChangeListener<? super AppointmentGroup> appointmentGroupListener = 
            (obs, oldValue, newValue) -> setCategories(newValue.getDescription());
    
    /**
     *  VEventImpl doesn't know how to make an appointment.  An appointment factory makes new appointments.  The Class of the appointment
     * is an argument for the AppointmentFactory.  The appointmentClass is set in the constructor.  A RRule object is not valid without
     * the appointmentClass.
     */
    public Class<? extends Appointment> getAppointmentClass() { return appointmentClass; }
    private Class<? extends Appointment> appointmentClass = Agenda.AppointmentImplLocal.class; // default Appointment class
    public void setAppointmentClass(Class<? extends Appointment> appointmentClass) { this.appointmentClass = appointmentClass; }
    public VEventImpl withAppointmentClass(Class<? extends Appointment> appointmentClass) { setAppointmentClass(appointmentClass); return this; }

    /**
     * The currently generated instances of the recurrence set.
     * 3.8.5.2 defines the recurrence set as the complete set of recurrence instances for a
     * calendar component.  As many RRule definitions are infinite sets, a complete representation
     * is not possible.  The set only contains the events inside the bounds of 
     */
    @Override
    public Set<Appointment> instances() { return instances; }
    final private Set<Appointment> instances = new HashSet<>();
//    public VEventImpl withAppointments(Collection<RepeatableAppointment> s) { appointments().addAll(s); return this; }
    public boolean isNewRRule() { return instances().size() == 0; } // new RRule has no appointments
    
    // Fluent methods for chaining
    public VEventImpl withDateTimeCreated(ZonedDateTime t) { setDateTimeCreated(t); return this; }
    public VEventImpl withDateTimeLastModified(ZonedDateTime t) { setDateTimeLastModified(t); return this; }
    public VEventImpl withDateTimeRecurrence(Temporal t) { setDateTimeRecurrence(t); return this; }
    public VEventImpl withDateTimeStamp(ZonedDateTime t) { setDateTimeStamp(t); return this; }
    public VEventImpl withDateTimeStart(Temporal t) { setDateTimeStart(t); return this; }
    public VEventImpl withDateTimeEnd(Temporal t) { setDateTimeEnd(t); return this; }
    public VEventImpl withDescription(String s) { setDescription(s); return this; }
    public VEventImpl withDuration(Duration d) { setDuration(d); return this; }
    public VEventImpl withExDate(ExDate e) { setExDate(e); return this; }
    public VEventImpl withRDate(RDate r) { setRDate(r); return this; }
    public VEventImpl withRelatedTo(String s) { setRelatedTo(s); return this; }
    public VEventImpl withRRule(RRule r) { setRRule(r); return this; }
    public VEventImpl withSequence(int i) { setSequence(i); return this; }
    public VEventImpl withSummary(String s) { setSummary(s); return this; }
    public VEventImpl withUniqueIdentifier(String s) { setUniqueIdentifier(s); return this; }
    
    /*
     * CONSTRUCTORS
     */
    /** Copy constructor */
    public VEventImpl(VEventImpl vevent)
    {
        super(vevent);
        this.appointmentGroups = vevent.getAppointmentGroups();
        categoriesProperty().addListener(categoriesListener);
        appointmentGroup.addListener(appointmentGroupListener);
        copy(vevent, this);
//        assignMakeInstanceCallbacks();
    }
    
    public VEventImpl(List<AppointmentGroup> appointmentGroups)
    {
        super();
        this.appointmentGroups = appointmentGroups;
        categoriesProperty().addListener(categoriesListener);
        appointmentGroup.addListener(appointmentGroupListener);
//        assignMakeInstanceCallbacks();
    }
    
    /**
     * makes new VEventImpl by copying properties from appointment.
     * stores start and end date/times as ZonedDateTime in default time zone
     * 
     * @param appointment - from Agenda
     */
    public VEventImpl(Appointment appointment, ObservableList<AppointmentGroup> appointmentGroups)
    {
        this(appointmentGroups);
        setCategories(appointment.getAppointmentGroup().getDescription());
        if (appointment.isWholeDay())
        {
            setDateTimeEnd(appointment.getEndLocalDateTime().toLocalDate());
            setDateTimeStart(appointment.getStartLocalDateTime().toLocalDate());
        } else
        {
            ZonedDateTime end = ZonedDateTime.of(appointment.getEndLocalDateTime(), ZoneId.systemDefault());
            setDateTimeEnd(end);
            ZonedDateTime start = ZonedDateTime.of(appointment.getStartLocalDateTime(), ZoneId.systemDefault());
            setDateTimeStart(start);
        }
        setDateTimeStamp(ZonedDateTime.now().withZoneSameInstant(ZoneId.of("Z")));
        setDescription(appointment.getDescription());
        setLocation(appointment.getLocation());
        setSummary(appointment.getSummary());
        setUniqueIdentifier(getUidGeneratorCallback().call(null));
        instances().add(appointment);
        if (! errorString().equals("")) throw new IllegalArgumentException(errorString());
    }
    
//    private void assignMakeInstanceCallbacks()
//    {
//        setNewDateInstanceCallback(NEW_DATE_INSTANCE);
//        setNewDateWithLocalTimeInstanceCallback(NEW_DATE_WITH_LOCAL_TIME_INSTANCE);
//        setNewDateWithUTCTimeInstanceCallback(NEW_DATE_WITH_UTC_TIME_INSTANCE);
//        setNewDateWithLocalTimeAndTimeZoneInstanceCallback(NEW_DATE_WITH_LOCAL_TIME_AND_TIME_ZONE_INSTANCE);
//    }    


    /** Deep copy all fields from source to destination 
     * */
    private static void copy(VEventImpl source, VEventImpl destination)
    {
        destination.setAppointmentGroup(source.getAppointmentGroup());
        destination.setAppointmentClass(source.getAppointmentClass());
        if (source.getStartRange() != null) destination.setStartRange(source.getStartRange());
        if (source.getEndRange() != null) destination.setEndRange(source.getEndRange());
        destination.setUidGeneratorCallback(source.getUidGeneratorCallback());
        source.instances().stream().forEach(a -> destination.instances().add(a));
    }
    
    /** Deep copy all fields from source to destination */
    @Override
    public void copyTo(VComponent<Appointment> destination)
    {
        super.copyTo(destination);
        copy(this, (VEventImpl) destination);
    }
    
    @Override
    public String errorString()
    {
        String errors = super.errorString();
//        if (getAppointmentClass() == null) errors += System.lineSeparator() + "Invalid VEventImpl.  appointmentClass must not be null.";
        return errors;
    }
    
    @Override
    public boolean isValid()
    {
        return super.errorString().equals("");
    }
    
    /** Make new VEventImpl and populate properties by parsing a list of property strings 
     * */
    private static VEventImpl parse(List<String> strings, List<AppointmentGroup> appointmentGroups)
    {
        VEventImpl vEvent = new VEventImpl(appointmentGroups);
        VEvent.parseVEvent(vEvent, strings); // parse VEvent properties into vEvent
        return vEvent;
    }
    
    /** Make new VEventImpl and populate properties by parsing a string with properties separated
     * a by lineSeparator (new line) */
    public static VEventImpl parse(String string, List<AppointmentGroup> appointmentGroups)
    {
        List<String> stringsList = Arrays
                .stream(string.split(System.lineSeparator()))
                .collect(Collectors.toList());
        return parse(stringsList, appointmentGroups);
    }

    
            
    /**
     * Returns appointments for Agenda that should exist between dateTimeRangeStart and dateTimeRangeEnd
     * based on VEvent properties.  Uses dateTimeRange previously set in VEvent.
     * @param <Appointment>
     * 
     * @return created appointments
     */
    @Override
    public List<Appointment> makeInstances()
    {
        if ((getStartRange() == null) || (getEndRange() == null)) throw new RuntimeException("Can't make instances without setting date/time range first");
        Callback<StartEndPair, Appointment> newInstanceCallback = DATE_TIME_MAKE_INSTANCE_MAP.get(getTemporalType());
        List<Appointment> madeAppointments = new ArrayList<>();
        Stream<Temporal> removedTooEarly = stream(getStartRange()).filter(d -> ! VComponent.isBefore(d, getStartRange()));
        Stream<Temporal> removedTooLate = takeWhile(removedTooEarly, a -> ! VComponent.isAfter(a, getEndRange()));
        removedTooLate.forEach(temporalStart ->
        {
            final Temporal temporalEnd;
            switch (endPriority())
            {
            case DTEND:
                final TemporalAmount duration;
                if (isWholeDay())
                {
                    duration = Period.between(LocalDate.from(getDateTimeStart()), LocalDate.from(getDateTimeEnd()));
                } else
                {
                    duration = Duration.between(getDateTimeStart(), getDateTimeEnd());
                }
                temporalEnd = temporalStart.plus(duration);
                break;
            case DURATION:
                temporalEnd = temporalStart.plus(getDuration());                
                break;
            default:
                throw new RuntimeException("Unknown EndPriority");
            }
            Appointment appt = newInstanceCallback.call(new StartEndPair(temporalStart, temporalEnd));
            // TODO - PROBLEM - SETTING START AND END TIME IN CALLBACK IS USELESS
            // THOSE VALUES ARE START OF VEVENT, NOT INSTANCE
            // SHOULD I JUST MAKE A NEW OBJECT AND RETURN IT?
            
//            DateTimeType.makeInstance(t)
            
//            Appointment appt2 = DateTimeType.makeInstance(t);
//            Appointment appt = AppointmentFactory.newAppointment(getAppointmentClass());
//            final Temporal startLocalDateTime;
            // TODO - CONSIDER USING DIFFERENT APPOINTMENT CLASSES FOR DIFFERENT TEMPORAL TYPES
//            if (getDateTimeStart() instanceof ZonedDateTime)
//            {
//                appt.setStartZonedDateTime((ZonedDateTime) t);
//            } else
//            {
//                appt.setStartLocalDateTime(VComponent.localDateTimeFromTemporal(t));
//            }
            
//            appt.setStartLocalDateTime(VComponent.localDateTimeFromTemporal(temporalStart));
//            final Duration duration;
//            switch (endPriority())
//            {
//            case DTEND:
//                final Temporal startInclusive;
//                final Temporal endExclusive;
//                if (isWholeDay())
//                {
//                    startInclusive = LocalDate.from(getDateTimeStart()).atStartOfDay();
//                    endExclusive = LocalDate.from(getDateTimeEnd()).atStartOfDay();
//                } else
//                {
//                    startInclusive = getDateTimeStart();
//                    endExclusive = getDateTimeEnd();                    
//                }
//                duration = Duration.between(startInclusive, endExclusive);
//                break;
//            case DURATION:
//                duration = getDuration();
//                break;
//            default:
//                throw new RuntimeException("Unknown EndPriority."); // shoudn't get here - only two EndPriorities defined
//            }           
//            if (getDateTimeStart() instanceof ZonedDateTime)
//            {
//                appt.setEndZonedDateTime((ZonedDateTime) t.plus(duration));
//            } else
//            {
//                appt.setEndLocalDateTime((LocalDateTime) t.plus(duration));
//            }
//            appt.setEndLocalDateTime(appt.getStartLocalDateTime().plus(duration));

            appt.setDescription(getDescription());
            appt.setSummary(getSummary());
            appt.setAppointmentGroup(getAppointmentGroup());
            appt.setWholeDay(isWholeDay());
            madeAppointments.add(appt);   // add appointments to return argument
            instances().add(appt); // add appointments to this object's collection
        });
        return madeAppointments;
    }
    
    /**
     * Returns appointments for Agenda that should exist between dateTimeRangeStart and dateTimeRangeEnd
     * based on VEvent properties.  Uses dateTimeRange previously set in VEvent.
     * @param <Appointment>
     * 
     * @param startRange
     * @param endRange
     * @return created appointments
     */
    @Override
    public List<Appointment> makeInstances(Temporal startRange, Temporal endRange)
    {
        if (VComponent.isAfter(startRange, endRange)) throw new DateTimeException("startTemporal must be after endTemporal");
        setEndRange(endRange);
        setStartRange(startRange);
        return makeInstances();
    }
    
    // takeWhile - From http://stackoverflow.com/questions/20746429/limit-a-stream-by-a-predicate
    // will be obsolete with Java 9
    public static <T> Spliterator<T> takeWhile(
            Spliterator<T> splitr, Predicate<? super T> predicate) {
          return new Spliterators.AbstractSpliterator<T>(splitr.estimateSize(), 0) {
            boolean stillGoing = true;
            @Override public boolean tryAdvance(Consumer<? super T> consumer) {
              if (stillGoing) {
                boolean hadNext = splitr.tryAdvance(elem -> {
                  if (predicate.test(elem)) {
                    consumer.accept(elem);
                  } else {
                    stillGoing = false;
                  }
                });
                return hadNext && stillGoing;
              }
              return false;
            }
          };
        }

        static <T> Stream<T> takeWhile(Stream<T> stream, Predicate<? super T> predicate) {
           return StreamSupport.stream(takeWhile(stream.spliterator(), predicate), false);
        }
}
