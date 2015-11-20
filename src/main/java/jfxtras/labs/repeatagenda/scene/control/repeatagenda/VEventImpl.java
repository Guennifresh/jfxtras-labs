package jfxtras.labs.repeatagenda.scene.control.repeatagenda;

import java.security.InvalidParameterException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javafx.util.Callback;
import jfxtras.labs.repeatagenda.scene.control.repeatagenda.ICalendarUtilities.ChangeDialogOptions;
import jfxtras.labs.repeatagenda.scene.control.repeatagenda.ICalendarUtilities.RRuleType;
import jfxtras.labs.repeatagenda.scene.control.repeatagenda.ICalendarUtilities.WindowCloseType;
import jfxtras.labs.repeatagenda.scene.control.repeatagenda.RepeatableAgenda.AppointmentFactory;
import jfxtras.labs.repeatagenda.scene.control.repeatagenda.RepeatableAgenda.RepeatableAppointment;
import jfxtras.labs.repeatagenda.scene.control.repeatagenda.icalendar.VEvent;
import jfxtras.labs.repeatagenda.scene.control.repeatagenda.icalendar.rrule.RRule;
import jfxtras.scene.control.agenda.Agenda.Appointment;
import jfxtras.scene.control.agenda.Agenda.AppointmentGroup;

// TODO - Needs to know how to make Appointments for agenda
// Will be like Repeat class
// Needs a makeAppointments method
// Needs to have copy methods to copy from VEvent to appointment and visa-versa
// Should this implement Appointment?

/**
 * Concrete class as an example of VEvent.
 * This class creates and edits appointments for display in Agenda.
 * 
 * Special use:
 * 3.8.1.2.  Categories: contains data for Appointment.appointmentGroup
 * 
 * @author David Bal
 *
 */

public class VEventImpl extends VEvent
{

    private AppointmentGroup appointmentGroup;
    public void setAppointmentGroup(AppointmentGroup appointmentGroup) { this.appointmentGroup = appointmentGroup; this.setCategories(appointmentGroup.getStyleClass()); }
    public AppointmentGroup getAppointmentGroup() { return appointmentGroup; }
    
    /**
     *  VEventImpl doesn't know how to make an appointment.  An appointment factory makes new appointments.  The Class of the appointment
     * is an argument for the AppointmentFactory.  The appointmentClass is set in the constructor.  A RRule object is not valid without
     * the appointmentClass.
     */
    public Class<? extends RepeatableAppointment> getAppointmentClass() { return appointmentClass; }
    private Class<? extends RepeatableAppointment> appointmentClass;
    public void setAppointmentClass(Class<? extends RepeatableAppointment> appointmentClass) { this.appointmentClass = appointmentClass; }
    public VEventImpl withAppointmentClass(Class<? extends RepeatableAppointment> appointmentClass) { setAppointmentClass(appointmentClass); return this; }

    /**
     * The currently generated events of the recurrence set.
     * 3.8.5.2 defines the recurrence set as the complete set of recurrence instances for a
     * calendar component.  As many RRule definitions are infinite sets, a complete representation
     * is not possible.  The set only contains the events inside the bounds of 
     */
    public Set<RepeatableAppointment> appointments() { return myAppointments; }
    final private Set<RepeatableAppointment> myAppointments = new HashSet<RepeatableAppointment>();
//    public VEventImpl withAppointments(Collection<RepeatableAppointment> s) { appointments().addAll(s); return this; }
    public boolean isNewRRule() { return appointments().size() == 0; } // new RRule has no appointments
    
    // CONSTRUCTORS
    /** Copy constructor */
    public VEventImpl(VEventImpl vevent) {
        super(vevent);
        copy(vevent, this);
    }
    
    public VEventImpl() { }

    /** Deep copy all fields from source to destination */
    public static void copy(VEventImpl source, VEventImpl destination)
    {
        if (source.getAppointmentGroup() != null) destination.setAppointmentGroup(source.getAppointmentGroup());
        if (source.getAppointmentClass() != null) destination.setAppointmentClass(source.getAppointmentClass());
        source.appointments().stream().forEach(a -> destination.appointments().add(a));
    }

    /**
     * Returns appointments that should exist between dateTimeRangeStart and dateTimeRangeEnd based on VEvent.
     * For convenience, sets VEvent dateTimeRangeStart and dateTimeRangeEnd prior to making appointments.
     * 
     * @param dateTimeRangeStart
     * @param dateTimeRangeEnd
     * @return
     */
    public Collection<Appointment> makeAppointments(
            LocalDateTime dateTimeRangeStart
          , LocalDateTime dateTimeRangeEnd)
    {
        setDateTimeRangeStart(dateTimeRangeStart);
        setDateTimeRangeEnd(dateTimeRangeEnd);
        return makeAppointments();
    }
    /**
     * Returns appointments that should exist between dateTimeRangeStart and dateTimeRangeEnd based on VEvent.
     * Uses dateTimeRange previously set in VEvent.
     * 
     * @return created appointments
     */
    public Collection<Appointment> makeAppointments()
    {
        List<Appointment> madeAppointments = new ArrayList<Appointment>();
        stream(getDateTimeStart())
                .forEach(d -> {
                    System.out.println("getAppointmentClass: " + getAppointmentClass());
                    RepeatableAppointment appt = AppointmentFactory.newAppointment(getAppointmentClass());
                    appt.setStartLocalDateTime(d);
                    appt.setEndLocalDateTime(d.plusSeconds(getDurationInSeconds()));
                    appt.setRepeatMade(true);
                    appt.setDescription(getDescription());
                    appt.setSummary(getSummary());
                    appt.setAppointmentGroup(getAppointmentGroup());
                    madeAppointments.add(appt);   // add appointments to main collection
                    appointments().add(appt); // add appointments to this repeat's collection
                });

        return madeAppointments;
    }
 
    /**
     * Returns next valid date time starting with inputed date.  If inputed date is valid it is returned.
     * Iterates from first date until it passes the inputDate.  This make take a long time if the date
     * is far in the future.
     * 
     * @param inputDate
     * @return
     */
    // TODO - If this method is necessary consider using cache of dates for faster retrieval
    // TODO - it may not be necessary, remove if possible for improved efficiency
    public LocalDateTime nextValidDateSlow(LocalDateTime inputDate)
    {
        if (inputDate.isBefore(getDateTimeStart())) return getDateTimeStart();
        final Iterator<LocalDateTime> i = getRRule().stream(inputDate).iterator();                                                            // make iterator
        while (i.hasNext())
        { // find date
            LocalDateTime s = i.next();
            if (s.isAfter(inputDate)) return s; // exit loop when beyond date without match
        }
        throw new InvalidParameterException("Can't find valid date starting at " + inputDate);
    }
    

    // its already edited by RepeatableController
    // changes to be made if ONE or FUTURE is selected.
    // change back if CANCEL
    public WindowCloseType edit(
              VEventImpl vEventOld // change back if cancel
            , Collection<Appointment> appointments // remove affected appointments
            , Collection<VEvent> vevents // add new VEvents if change to one or future
            , Callback<ChangeDialogOptions[], ChangeDialogOptions> changeDialogCallback // force change selection for testing
            , Callback<Collection<VEvent>, Void> writeVEventsCallback) // I/O callback
    {
        if (this.equals(vEventOld)) return WindowCloseType.CLOSE_WITHOUT_CHANGE;
        final RRuleType rruleType = getVEventType(vEventOld.getRRule());
        System.out.println("rruleType " + rruleType);
        boolean editedFlag = false;
        switch (rruleType)
        {
        case HAD_REPEAT_BECOMING_INDIVIDUAL:
            this.setRRule(null);
        case WITH_NEW_REPEAT:
        case INDIVIDUAL:
            editedFlag = true;
            break;
        case WITH_EXISTING_REPEAT:
            // Check if changes between vEvent and vEventOld exist apart from RRule
            VEventImpl tempVEvent = new VEventImpl(vEventOld);
            tempVEvent.setRRule(getRRule());
            boolean onlyRRuleChanged = this.equals(tempVEvent);

            ChangeDialogOptions[] choices = null;
            if (onlyRRuleChanged) choices = new ChangeDialogOptions[] {ChangeDialogOptions.ALL, ChangeDialogOptions.FUTURE};
            ChangeDialogOptions changeResponse = changeDialogCallback.call(choices);
            switch (changeResponse)
            {
            case ALL:
                break;
            case CANCEL:
                break;
            case FUTURE:
                break;
            case ONE:
                break;
            default:
                break;
            }
            break;
        default:
            break;
        }
        
        if (editedFlag) // make these changes as long as CANCEL is not selected
        { // remove appointments from mail collection made by VEvent
            Iterator<Appointment> i = appointments.iterator();
            while (i.hasNext())
            {
                Appointment a = i.next();
                if (appointments().contains(a)) i.remove();
            }
            appointments().clear(); // clear VEvent's collection of appointments
            appointments.addAll(makeAppointments()); // make new appointments and add to main collection (added to VEvent's collection in makeAppointments)
            return WindowCloseType.CLOSE_WITH_CHANGE;
        } else
        {
            return WindowCloseType.CLOSE_WITHOUT_CHANGE;
        }
    }
    
    private RRuleType getVEventType(RRule rruleOld)
    {

        if (getRRule() == null)
        {
            if (rruleOld == null)
            { // doesn't have repeat or have old repeat either
                return RRuleType.INDIVIDUAL;
            } else {
                return RRuleType.HAD_REPEAT_BECOMING_INDIVIDUAL;
            }
        } else
        {
            if (isNewRRule())
            {
                return RRuleType.WITH_NEW_REPEAT;                
            } else {
                return RRuleType.WITH_EXISTING_REPEAT;
            }
        }
    }
    
}