package jfxtras.labs.icalendarfx.components;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.temporal.Temporal;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.util.Callback;
import jfxtras.labs.icalendarfx.VCalendar;
import jfxtras.labs.icalendarfx.properties.PropertyType;
import jfxtras.labs.icalendarfx.properties.component.change.DateTimeCreated;
import jfxtras.labs.icalendarfx.properties.component.change.LastModified;
import jfxtras.labs.icalendarfx.properties.component.change.Sequence;
import jfxtras.labs.icalendarfx.properties.component.descriptive.Attachment;
import jfxtras.labs.icalendarfx.properties.component.descriptive.Categories;
import jfxtras.labs.icalendarfx.properties.component.descriptive.Classification;
import jfxtras.labs.icalendarfx.properties.component.descriptive.Classification.ClassificationType;
import jfxtras.labs.icalendarfx.properties.component.descriptive.Status;
import jfxtras.labs.icalendarfx.properties.component.descriptive.Status.StatusType;
import jfxtras.labs.icalendarfx.properties.component.descriptive.Summary;
import jfxtras.labs.icalendarfx.properties.component.recurrence.ExceptionDates;
import jfxtras.labs.icalendarfx.properties.component.recurrence.RecurrenceDates;
import jfxtras.labs.icalendarfx.properties.component.recurrence.RecurrenceRule;
import jfxtras.labs.icalendarfx.properties.component.recurrence.RecurrenceRuleCache;
import jfxtras.labs.icalendarfx.properties.component.relationship.Contact;
import jfxtras.labs.icalendarfx.properties.component.relationship.RecurrenceId;
import jfxtras.labs.icalendarfx.properties.component.relationship.RelatedTo;
import jfxtras.labs.icalendarfx.utilities.DateTimeUtilities;
import jfxtras.labs.icalendarfx.utilities.DateTimeUtilities.DateTimeType;

public abstract class VComponentDisplayableBase<T> extends VComponentPersonalBase<T> implements VComponentRepeatable<T>, VComponentDescribable<T>, VComponentLastModified<T>
{
    /**
     * This property provides the capability to associate a document object with a calendar component.
     */
    @Override
    public ObjectProperty<ObservableList<Attachment<?>>> attachmentsProperty()
    {
        if (attachments == null)
        {
            attachments = new SimpleObjectProperty<>(this, PropertyType.ATTACHMENT.toString());
        }
        return attachments;
    }
    @Override
    public ObservableList<Attachment<?>> getAttachments()
    {
        return (attachments == null) ? null : attachments.get();
    }
    private ObjectProperty<ObservableList<Attachment<?>>> attachments;
    @Override
    public void setAttachments(ObservableList<Attachment<?>> attachments)
    {
        if (attachments != null)
        {
            orderer().registerSortOrderProperty(attachments);
        } else
        {
            orderer().unregisterSortOrderProperty(attachmentsProperty().get());
        }
        attachmentsProperty().set(attachments);
    }
    
    /**
     * CATEGORIES:
     * RFC 5545 iCalendar 3.8.1.12. page 81
     * This property defines the categories for a calendar component.
     * Example:
     * CATEGORIES:APPOINTMENT,EDUCATION
     * CATEGORIES:MEETING
     */
    public ObservableList<Categories> getCategories() { return categories; }
    private ObservableList<Categories> categories;
    public void setCategories(ObservableList<Categories> categories)
    {
        if (categories != null)
        {
            orderer().registerSortOrderProperty(categories);
        } else
        {
            orderer().unregisterSortOrderProperty(this.categories);
        }
        this.categories = categories;
    }
    public T withCategories(ObservableList<Categories> categories) { setCategories(categories); return (T) this; }
    public T withCategories(String...categories)
    {
        if (categories != null)
        {
            String c = Arrays.stream(categories).collect(Collectors.joining(","));
            PropertyType.CATEGORIES.parse(this, c);
        }
        return (T) this;
    }
    public T withCategories(Categories...categories)
    {
        if (getCategories() == null)
        {
            setCategories(FXCollections.observableArrayList(categories));
        } else
        {
            getCategories().addAll(categories);
        }
        return (T) this;
    }
    
    /**
     * CLASS
     * Classification
     * RFC 5545, 3.8.1.3, page 82
     * 
     * This property defines the access classification for a calendar component.
     * 
     * Example:
     * CLASS:PUBLIC
     */
    public ObjectProperty<Classification> classificationProperty()
    {
        if (classification == null)
        {
            classification = new SimpleObjectProperty<>(this, PropertyType.CLASSIFICATION.toString());
            orderer().registerSortOrderProperty(classification);
        }
        return classification;
    }
    public Classification getClassification() { return (classification == null) ? null : classificationProperty().get(); }
    private ObjectProperty<Classification> classification;
    public void setClassification(String classification)
    {
        if (getClassification() == null)
        {
            setClassification(Classification.parse(classification));
        } else
        {
            Classification temp = Classification.parse(classification);
            getClassification().setValue(temp.getValue());
        }
    }
    public void setClassification(Classification classification) { classificationProperty().set(classification); }
    public void setClassification(ClassificationType classification)
    {
        if (getClassification() == null)
        {
            setClassification(new Classification(classification));            
        } else
        {
            getClassification().setValue(classification);
        }
    }
    public T withClassification(Classification classification)
    {
        if (getClassification() == null)
        {
            setClassification(classification);
            return (T) this;
        } else
        {
            throw new IllegalArgumentException("Property can only occur once in the calendar component");
        }
    }
    public T withClassification(ClassificationType classification)
    {
        if (getClassification() == null)
        {
            setClassification(classification);
            return (T) this;
        } else
        {
            throw new IllegalArgumentException("Property can only occur once in the calendar component");
        }
    }
    public T withClassification(String classification)
    {
        if (getClassification() == null)
        {
            setClassification(classification);
            return (T) this;
        } else
        {
            throw new IllegalArgumentException("Property can only occur once in the calendar component");
        }
    }
    
    /**
     * CONTACT:
     * RFC 5545 iCalendar 3.8.4.2. page 109
     * This property is used to represent contact information or
     * alternately a reference to contact information associated with the
     * calendar component.
     * 
     * Example:
     * CONTACT;ALTREP="ldap://example.com:6666/o=ABC%20Industries\,
     *  c=US???(cn=Jim%20Dolittle)":Jim Dolittle\, ABC Industries\,
     *  +1-919-555-1234
     */
    public ObservableList<Contact> getContacts() { return contacts; }
    private ObservableList<Contact> contacts;
    public void setContacts(ObservableList<Contact> contacts)
    {
        if (contacts != null)
        {
            orderer().registerSortOrderProperty(contacts);
        } else
        {
            orderer().unregisterSortOrderProperty(this.contacts);
        }
        this.contacts = contacts;
    }
    public T withContacts(ObservableList<Contact> contacts) { setContacts(contacts); return (T) this; }
    public T withContacts(String...contacts)
    {
        Arrays.stream(contacts).forEach(c -> PropertyType.CONTACT.parse(this, c));
        return (T) this;
    }
    public T withContacts(Contact...contacts)
    {
        if (getContacts() == null)
        {
            setContacts(FXCollections.observableArrayList(contacts));
        } else
        {
            getContacts().addAll(contacts);
        }
        return (T) this;
    }
    
    
    /**
     * CREATED: Date-Time Created
     * RFC 5545 iCalendar 3.8.7.1 page 136
     * This property specifies the date and time that the calendar information was created.
     * This is analogous to the creation date and time for a file in the file system.
     * The value MUST be specified in the UTC time format.
     * 
     * Example:
     * CREATED:19960329T133000Z
     */
    public ObjectProperty<DateTimeCreated> dateTimeCreatedProperty()
    {
        if (dateTimeCreated == null)
        {
            dateTimeCreated = new SimpleObjectProperty<>(this, PropertyType.DATE_TIME_CREATED.toString());
            orderer().registerSortOrderProperty(dateTimeCreated);
        }
        return dateTimeCreated;
    }
    public DateTimeCreated getDateTimeCreated() { return (dateTimeCreated == null) ? null : dateTimeCreatedProperty().get(); }
    private ObjectProperty<DateTimeCreated> dateTimeCreated;
    public void setDateTimeCreated(String dtCreated)
    {
        if (getDateTimeCreated() == null)
        {
            setDateTimeCreated(DateTimeCreated.parse(dtCreated));
        } else
        {
            DateTimeCreated temp = DateTimeCreated.parse(dtCreated);
            setDateTimeCreated(temp);
        }
    }
    public void setDateTimeCreated(DateTimeCreated dtCreated) { dateTimeCreatedProperty().set(dtCreated); }
    public void setDateTimeCreated(ZonedDateTime dtCreated)
    {
        if (getDateTimeCreated() == null)
        {
            setDateTimeCreated(new DateTimeCreated(dtCreated));
        } else
        {
            getDateTimeCreated().setValue(dtCreated);
        }
    }
    public T withDateTimeCreated(ZonedDateTime dtCreated)
    {
        if (getDateTimeCreated() == null)
        {
            setDateTimeCreated(dtCreated);
            return (T) this;
        } else
        {
            throw new IllegalArgumentException("Property can only occur once in the calendar component");
        }
    }
    public T withDateTimeCreated(String dtCreated)
    {
        if (getDateTimeCreated() == null)
        {
            setDateTimeCreated(dtCreated);
            return (T) this;
        } else
        {
            throw new IllegalArgumentException("Property can only occur once in the calendar component");
        }
    }
    public T withDateTimeCreated(DateTimeCreated dtCreated)
    {
        if (getDateTimeCreated() == null)
        {
            setDateTimeCreated(dtCreated);
            return (T) this;
        } else
        {
            throw new IllegalArgumentException("Property can only occur once in the calendar component");
        }
    }
    
   /** 
    * EXDATE
    * Exception Date-Times
    * RFC 5545 iCalendar 3.8.5.1, page 117.
    * 
    * This property defines the list of DATE-TIME exceptions for
    * recurring events, to-dos, journal entries, or time zone definitions.
    */ 
    public ObservableList<ExceptionDates> getExceptionDates()
    {
        return exceptions;
    }
    private ObservableList<ExceptionDates> exceptions;
    public void setExceptionDates(ObservableList<ExceptionDates> exceptions)
    {
        if (exceptions != null)
        {
            orderer().registerSortOrderProperty(exceptions);
            exceptions.addListener(getRecurrencesConsistencyWithDateTimeStartListener());
//            String error = VComponentRepeatable.checkRecurrencesConsistency(exceptions);
            String error = checkRecurrencesConsistency(exceptions);
            if (error != null) throw new DateTimeException(error);
        } else
        {
            orderer().unregisterSortOrderProperty(this.exceptions);
        }
        this.exceptions = exceptions;
    }
    public T withExceptionDates(ObservableList<ExceptionDates> exceptions)
    {
        setExceptionDates(exceptions);
        return (T) this;
    }
    public T withExceptionDates(String...exceptions)
    {
        Arrays.stream(exceptions).forEach(s -> PropertyType.EXCEPTION_DATE_TIMES.parse(this, s));   
        return (T) this;
    }
    public T withExceptionDates(Temporal...exceptions)
    {
        final ObservableList<ExceptionDates> list;
        if (getExceptionDates() == null)
        {
            list = FXCollections.observableArrayList();
            setExceptionDates(list);
        } else
        {
            list = getExceptionDates();
        }
        list.add(new ExceptionDates(exceptions));
        return (T) this;
    }
    public T withExceptionDates(ExceptionDates...exceptions)
    {
        if (getExceptionDates() == null)
        {
            setExceptionDates(FXCollections.observableArrayList());
            Arrays.stream(exceptions).forEach(e -> getExceptionDates().add(e)); // add one at a time to ensure date-time type compliance
        } else
        {
            getExceptionDates().addAll(exceptions);
        }
        return (T) this;
    }
    
    /**
    * LAST-MODIFIED
    * RFC 5545, 3.8.7.3, page 138
    * 
    * This property specifies the date and time that the
    * information associated with the calendar component was last
    * revised in the calendar store.
    *
    * Note: This is analogous to the modification date and time for a
    * file in the file system.
    * 
    * The value MUST be specified as a date with UTC time.
    * 
    * Example:
    * LAST-MODIFIED:19960817T133000Z
    */
    @Override
    public ObjectProperty<LastModified> dateTimeLastModifiedProperty()
    {
        if (lastModified == null)
        {
            lastModified = new SimpleObjectProperty<>(this, PropertyType.LAST_MODIFIED.toString());
            orderer().registerSortOrderProperty(lastModified);
        }
        return lastModified;
    }
    @Override
    public LastModified getDateTimeLastModified() { return (lastModified == null) ? null : dateTimeLastModifiedProperty().get(); }
    private ObjectProperty<LastModified> lastModified;
    
    /**
     * RDATE
     * Recurrence Date-Times
     * RFC 5545 iCalendar 3.8.5.2, page 120.
     * 
     * This property defines the list of DATE-TIME values for
     * recurring events, to-dos, journal entries, or time zone definitions.
     * 
     * NOTE: DOESN'T CURRENTLY SUPPORT PERIOD VALUE TYPE
     * */
    @Override
    public ObservableList<RecurrenceDates> getRecurrenceDates() { return recurrenceDates; }
    private ObservableList<RecurrenceDates> recurrenceDates;
    @Override
    public void setRecurrenceDates(ObservableList<RecurrenceDates> recurrenceDates)
    {
        if (recurrenceDates != null)
        {
            orderer().registerSortOrderProperty(recurrenceDates);
            recurrenceDates.addListener(getRecurrencesConsistencyWithDateTimeStartListener());
            String error = checkRecurrencesConsistency(exceptions);
//            String error = VComponentRepeatable.checkRecurrencesConsistency(recurrenceDates);
            if (error != null) throw new DateTimeException(error);
        } else
        {
            orderer().unregisterSortOrderProperty(this.recurrenceDates);
        }
        this.recurrenceDates = recurrenceDates;
    }

    /**
     * RECURRENCE-ID: Recurrence Identifier
     * RFC 5545 iCalendar 3.8.4.4 page 112
     * The property value is the original value of the "DTSTART" property of the 
     * recurrence instance before an edit that changed the value.
     * 
     * Example:
     * RECURRENCE-ID;VALUE=DATE:19960401
     */
    public ObjectProperty<RecurrenceId> recurrenceIdProperty()
    {
        if (recurrenceId == null)
        {
            recurrenceId = new SimpleObjectProperty<>(this, PropertyType.RECURRENCE_IDENTIFIER.toString());
            orderer().registerSortOrderProperty(recurrenceId);
            recurrenceId.addListener((obs) -> 
            {
                String error = checkRecurrenceIdConsistency();
                if (error != null)
                {
                    throw new DateTimeException(error);
                }
            });
        }
        return recurrenceId;
    }
    public RecurrenceId getRecurrenceId() { return (recurrenceId == null) ? null : recurrenceIdProperty().get(); }
    private ObjectProperty<RecurrenceId> recurrenceId;
    public void setRecurrenceId(RecurrenceId recurrenceId) { recurrenceIdProperty().set(recurrenceId); }
    public void setRecurrenceId(String recurrenceId)
    {
        if (getRecurrenceId() == null)
        {
            setRecurrenceId(RecurrenceId.parse(recurrenceId));
        } else
        {
            RecurrenceId temp = RecurrenceId.parse(recurrenceId);
            if (temp.getValue().getClass().equals(getRecurrenceId().getValue().getClass()))
            {
                getRecurrenceId().setValue(temp.getValue());
            } else
            {
                setRecurrenceId(temp);
            }
        }
    }
    public void setRecurrenceId(Temporal temporal)
    {
        if ((getRecurrenceId() == null) || ! getRecurrenceId().getValue().getClass().equals(temporal.getClass()))
        {
            if ((temporal instanceof LocalDate) || (temporal instanceof LocalDateTime) || (temporal instanceof ZonedDateTime))
            {
                if (getRecurrenceId() == null)
                {
                    setRecurrenceId(new RecurrenceId(temporal));
                } else
                {
                    getRecurrenceId().setValue(temporal);
                }
            } else
            {
                throw new DateTimeException("Only LocalDate, LocalDateTime and ZonedDateTime supported. "
                        + temporal.getClass().getSimpleName() + " is not supported");
            }
        } else
        {
            getRecurrenceId().setValue(temporal);
        }
    }
    public T withRecurrenceId(RecurrenceId recurrenceId)
    {
        System.out.println("set rid:");
        if (getRecurrenceId() == null)
        {
            setRecurrenceId(recurrenceId);
            return (T) this;
        } else
        {
            throw new IllegalArgumentException("Property can only occur once in the calendar component");
        }
    }
    public T withRecurrenceId(String recurrenceId)
    {
        if (getRecurrenceId() == null)
        {
            setRecurrenceId(recurrenceId);
            return (T) this;
        } else
        {
            throw new IllegalArgumentException("Property can only occur once in the calendar component");
        }
    }
    public T withRecurrenceId(Temporal recurrenceId)
    {
        if (getRecurrenceId() == null)
        {
            setRecurrenceId(recurrenceId);
            return (T) this;
        } else
        {
            throw new IllegalArgumentException("Property can only occur once in the calendar component");
        }
    }
    
//    /** Ensures RecurrenceId has same date-time type as DateTimeStart.  Should be put in listener
//     *  after recurrenceIdProperty() is initialized */
//    void checkRecurrenceIdConsistency()
//    {
//        System.out.println("test22:" + getRecurrenceId() + " " + getDateTimeStart());
//        if ((getRecurrenceId() != null) && (getDateTimeStart() != null))
//        {
//            DateTimeType recurrenceIdType = DateTimeUtilities.DateTimeType.of(getRecurrenceId().getValue());
//            if (getParent() != null)
//            {
//                List<VComponentDisplayableBase<?>> relatedComponents = ((VCalendar) getParent()).uidComponentsMap().get(getUniqueIdentifier().getValue());
//                VComponentDisplayableBase<?> parentComponent = relatedComponents.stream()
//                        .filter(v -> v.getRecurrenceId() == null)
//                        .findFirst()
//                        .orElseThrow(() -> new RuntimeException("no parent component found"));
//                DateTimeType dateTimeStartType = DateTimeUtilities.DateTimeType.of(parentComponent.getDateTimeStart().getValue());
//                if (recurrenceIdType != dateTimeStartType)
//                {
//                    throw new DateTimeException("RecurrenceId DateTimeType (" + recurrenceIdType +
//                            ") must be same as the DateTimeType of DateTimeStart (" + dateTimeStartType + ")");
//                }
//            }
//        }
//    }
    
    /** Checks if RecurrenceId has same date-time type as DateTimeStart.  Returns String containing
     * error message if there is a problem, null otherwise. */
    String checkRecurrenceIdConsistency()
    {
        if (getRecurrenceId() != null && recurrenceParent() != null)
        {
            DateTimeType recurrenceIdType = DateTimeUtilities.DateTimeType.of(getRecurrenceId().getValue());
            DateTimeType parentDateTimeStartType = DateTimeUtilities.DateTimeType.of(recurrenceParent().getDateTimeStart().getValue());
            if (recurrenceIdType != parentDateTimeStartType)
            {
                return PropertyType.RECURRENCE_IDENTIFIER.toString() + ":RecurrenceId DateTimeType (" + recurrenceIdType +
                        ") must be same as the type of its parent's DateTimeStart (" + parentDateTimeStartType + ")";
            }
        }
        return null;
    }

    /**
     * RELATED-TO:
     * 3.8.4.5, RFC 5545 iCalendar, page 115
     * This property is used to represent a relationship or reference between
     * one calendar component and another.  By default, the property value points to another
     * calendar component's UID that has a PARENT relationship to the referencing object.
     * This field is null unless the object contains as RECURRENCE-ID value.
     * 
     * Example:
     * RELATED-TO:19960401-080045-4000F192713-0052@example.com
     */
    public ObservableList<RelatedTo> getRelatedTo() { return relatedTo; }
    private ObservableList<RelatedTo> relatedTo;
    public void setRelatedTo(ObservableList<RelatedTo> relatedTo)
    {
        if (relatedTo != null)
        {
            orderer().registerSortOrderProperty(relatedTo);
        } else
        {
            orderer().unregisterSortOrderProperty(this.relatedTo);
        }
        this.relatedTo = relatedTo;
    }
    public T withRelatedTo(ObservableList<RelatedTo> relatedTo) { setRelatedTo(relatedTo); return (T) this; }
    public T withRelatedTo(String...relatedTo)
    {
        Arrays.stream(relatedTo).forEach(c -> PropertyType.RELATED_TO.parse(this, c));
        return (T) this;
    }
    public T withRelatedTo(RelatedTo...relatedTo)
    {
        if (getRelatedTo() == null)
        {
            setRelatedTo(FXCollections.observableArrayList(relatedTo));
        } else
        {
            getRelatedTo().addAll(relatedTo);
        }
        return (T) this;
    }
    
    /**
     * RRULE, Recurrence Rule
     * RFC 5545 iCalendar 3.8.5.3, page 122.
     * This property defines a rule or repeating pattern for recurring events, 
     * to-dos, journal entries, or time zone definitions
     * If component is not repeating the value is null.
     * 
     * Examples:
     * RRULE:FREQ=DAILY;COUNT=10
     * RRULE:FREQ=WEEKLY;UNTIL=19971007T000000Z;WKST=SU;BYDAY=TU,TH
     */
    @Override public ObjectProperty<RecurrenceRule> recurrenceRuleProperty()
    {
        if (recurrenceRule == null)
        {
            recurrenceRule = new SimpleObjectProperty<>(this, PropertyType.UNIQUE_IDENTIFIER.toString());
            orderer().registerSortOrderProperty(recurrenceRule);
        }
        return recurrenceRule;
    }
    @Override
    public RecurrenceRule getRecurrenceRule() { return (recurrenceRule == null) ? null : recurrenceRuleProperty().get(); }
    private ObjectProperty<RecurrenceRule> recurrenceRule;
 
    /**
     * SEQUENCE:
     * RFC 5545 iCalendar 3.8.7.4. page 138
     * This property defines the revision sequence number of the calendar component within a sequence of revisions.
     * Example:  The following is an example of this property for a calendar
     * component that was just created by the "Organizer":
     *
     * SEQUENCE:0
     *
     * The following is an example of this property for a calendar
     * component that has been revised two different times by the
     * "Organizer":
     *
     * SEQUENCE:2
     */
    public ObjectProperty<Sequence> sequenceProperty()
    {
        if (sequence == null)
        {
            sequence = new SimpleObjectProperty<>(this, PropertyType.SEQUENCE.toString());
            orderer().registerSortOrderProperty(sequence);
        }
        return sequence;
    }
    public Sequence getSequence() { return (sequence == null) ? null : sequenceProperty().get(); }
    private ObjectProperty<Sequence> sequence;
    public void setSequence(String sequence)
    {
        if (getSequence() == null)
        {
            setSequence(Sequence.parse(sequence));
        } else
        {
            Sequence temp = Sequence.parse(sequence);
            getSequence().setValue(temp.getValue());
        }
    }
    public void setSequence(Integer sequence)
    {
        if (getSequence() == null)
        {
            setSequence(new Sequence(sequence));
        } else
        {
            getSequence().setValue(sequence);
        }
    }
    public void setSequence(Sequence sequence) { sequenceProperty().set(sequence); }
    public T withSequence(Sequence sequence)
    {
        if (getSequence() == null)
        {
            setSequence(sequence);
            return (T) this;
        } else
        {
            throw new IllegalArgumentException("Property can only occur once in the calendar component");
        }
    }
    public T withSequence(Integer sequence)
    {
        if (getSequence() == null)
        {
            setSequence(sequence);
            return (T) this;
        } else
        {
            throw new IllegalArgumentException("Property can only occur once in the calendar component");
        }
    }
    public T withSequence(String sequence)
    {
        if (getSequence() == null)
        {
            setSequence(sequence);
            return (T) this;
        } else
        {
            throw new IllegalArgumentException("Property can only occur once in the calendar component");
        }
    }
    public void incrementSequence()
    {
        if (getSequence() != null)
        {
            setSequence(getSequence().getValue()+1);            
        } else
        {
            setSequence(1);            
        }
    }

    /**
     * SUMMARY
     * RFC 5545 iCalendar 3.8.1.12. page 93
     * 
     * This property defines a short summary or subject for the calendar component.
     * 
     * Example:
     * SUMMARY:Department Party
     */
    @Override public ObjectProperty<Summary> summaryProperty()
    {
        if (summary == null)
        {
            summary = new SimpleObjectProperty<>(this, PropertyType.SUMMARY.toString());
            orderer().registerSortOrderProperty(summary);
        }
        return summary;
    }
    @Override
    public Summary getSummary() { return (summary == null) ? null : summaryProperty().get(); }
    private ObjectProperty<Summary> summary;
    
    /**
     * STATUS
     * RFC 5545 iCalendar 3.8.1.11. page 92
     * 
     * This property defines the overall status or confirmation for the calendar component.
     * 
     * Example:
     * STATUS:TENTATIVE
     */
    public ObjectProperty<Status> statusProperty()
    {
        if (status == null)
        {
            status = new SimpleObjectProperty<>(this, PropertyType.STATUS.toString());
            orderer().registerSortOrderProperty(status);
        }
        return status;
    }
    public Status getStatus() { return (status == null) ? null : statusProperty().get(); }
    private ObjectProperty<Status> status;
    public void setStatus(String status)
    {
        if (getStatus() == null)
        {
            setStatus(Status.parse(status));
        } else
        {
            Status temp = Status.parse(status);
            getStatus().setValue(temp.getValue());
        }
    }
    public void setStatus(Status status) { statusProperty().set(status); }
    public void setStatus(StatusType status)
    {
        if (getStatus() == null)
        {
            setStatus(new Status(status));
        } else
        {
            getStatus().setValue(status);
        }
    }
    public T withStatus(Status status)
    {
        if (getStatus() == null)
        {
            setStatus(status);
            return (T) this;
        } else
        {
            throw new IllegalArgumentException("Property can only occur once in the calendar component");
        }
    }
    public T withStatus(StatusType status)
    {
        if (getStatus() == null)
        {
            setStatus(status);
            return (T) this;
        } else
        {
            throw new IllegalArgumentException("Property can only occur once in the calendar component");
        }
    }
    public T withStatus(String status)
    {
        if (getStatus() == null)
        {
            setStatus(status);
            return (T) this;
        } else
        {
            throw new IllegalArgumentException("Property can only occur once in the calendar component");
        }
    }

//    @Override
//    public void checkDateTimeStartConsistency()
//    {
//        VComponentRepeatable.super.checkDateTimeStartConsistency();
//        if ((getExceptionDates() != null) && (getDateTimeStart() != null))
//        {
//            Temporal firstException = getExceptionDates().get(0).getValue().iterator().next();
//            DateTimeType exceptionType = DateTimeUtilities.DateTimeType.of(firstException);
//            DateTimeType dateTimeStartType = DateTimeUtilities.DateTimeType.of(getDateTimeStart().getValue());
//            if (exceptionType != dateTimeStartType)
//            {
//                throw new DateTimeException("Exceptions DateTimeType (" + exceptionType +
//                        ") must be same as the DateTimeType of DateTimeStart (" + dateTimeStartType + ")");
//            }
//        }
//        checkRecurrenceIdConsistency();
////        if ((getRecurrenceId() != null) && (getDateTimeStart() != null))
////        {
////            DateTimeType recurrenceIdType = DateTimeUtilities.DateTimeType.of(getRecurrenceId().getValue());
////            DateTimeType dateTimeStartType = DateTimeUtilities.DateTimeType.of(getDateTimeStart().getValue());
////            if (recurrenceIdType != dateTimeStartType)
////            {
////                throw new DateTimeException("RecurrenceId DateTimeType (" + recurrenceIdType +
////                        ") must be same as the DateTimeType of DateTimeStart (" + dateTimeStartType + ")");
////            }
////        }        
//    }
    
    @Override
    void dateTimeStartListenerHook()
    {
        super.dateTimeStartListenerHook();
        String reccurenceIDErrorString = checkRecurrenceIdConsistency();
        if (reccurenceIDErrorString != null)
        {
            throw new RuntimeException(reccurenceIDErrorString);
        }
    }
    
    /*
     * CONSTRUCTORS
     */
    public VComponentDisplayableBase() { super(); }
    
//    public VComponentDisplayableBase(String contentLines)
//    {
//        super(contentLines);
//    }
    
    public VComponentDisplayableBase(VComponentDisplayableBase<T> source)
    {
        super(source);
    }
    
    @Override
    public Stream<Temporal> streamRecurrences(Temporal start)
    {
        // get stream with recurrence rule (RRULE) and recurrence date (RDATE)
        Stream<Temporal> inStream = VComponentRepeatable.super.streamRecurrences(start);

        // assign temporal comparator to match start type
        final Comparator<Temporal> temporalComparator = DateTimeUtilities.getTemporalComparator(start);
        
        // Handle Recurrence IDs
        final Stream<Temporal> stream2;
        List<VComponentDisplayableBase<?>> children = recurrenceChildren();
        if (children != null)
        {
            // If present, remove recurrence ID original values
            List<Temporal> recurrenceIDTemporals = recurrenceChildren()
                    .stream()
                    .map(c -> c.getRecurrenceId().getValue())
                    .collect(Collectors.toList());
            stream2 = inStream.filter(t -> ! recurrenceIDTemporals.contains(t));
        } else
        {
            stream2 = inStream;
        }
        
        // If present, remove exceptions
        final Stream<Temporal> stream3;
        if (getExceptionDates() != null)
        {
            List<Temporal> exceptions = getExceptionDates()
                    .stream()
                    .flatMap(r -> r.getValue().stream())
                    .map(v -> v)
                    .sorted(temporalComparator)
                    .collect(Collectors.toList());
            stream3 = stream2.filter(d -> ! exceptions.contains(d));
        } else
        {
            stream3 = stream2;
        }
        
        if (getRecurrenceRule() == null)
        {
            return stream3; // no cache is no recurrence rule
        }
        return recurrenceStreamer().makeCache(stream3);  // make cache of start date/times
    }

    /*
     *  RECURRENCE STREAMER
     *  produces recurrence set
     */
    private RecurrenceRuleCache streamer = new RecurrenceRuleCache(this);
    @Override
    public RecurrenceRuleCache recurrenceStreamer() { return streamer; }

    /*
     * RECURRENCE CHILDREN - (RECURRENCE-IDs AND MATCHING UID)
     */
    /**  Callback to make list of child components (those with RECURRENCE-ID and same UID)
     * Callback assigned in {@link VCalendar#displayableListChangeListener } */
    private Callback<VComponentDisplayableBase<?>, List<VComponentDisplayableBase<?>>> makeRecurrenceChildrenListCallBack;
//    @Override
//    public Callback<VComponentDisplayableBase<?>, List<VComponentDisplayableBase<?>>> getChildComponentsListCallBack()
//    {
//        return makeChildComponentsListCallBack;
//    }
    public void setRecurrenceChildrenListCallBack(Callback<VComponentDisplayableBase<?>, List<VComponentDisplayableBase<?>>> makeRecurrenceChildrenListCallBack)
    {
        this.makeRecurrenceChildrenListCallBack = makeRecurrenceChildrenListCallBack;
    }

    public List<VComponentDisplayableBase<?>> recurrenceChildren()
    {
        if ((getRecurrenceId() == null) && (makeRecurrenceChildrenListCallBack != null))
        {
            return Collections.unmodifiableList(makeRecurrenceChildrenListCallBack.call(this));
        }
        return Collections.unmodifiableList(Collections.emptyList());
    }
    
    /*
     * RECURRENCE PARENT - (the VComponent with matching UID and no RECURRENCEID)
     */
    private Callback<VComponentDisplayableBase<?>, VComponentDisplayableBase<?>> recurrenceParentCallBack;

    public void setRecurrenceParentListCallBack(Callback<VComponentDisplayableBase<?>, VComponentDisplayableBase<?>> recurrenceParentCallBack)
    {
        this.recurrenceParentCallBack = recurrenceParentCallBack;
    }
    
    public VComponentDisplayableBase<?> recurrenceParent()
    {
        if ((getRecurrenceId() != null) && (recurrenceParentCallBack != null))
        {
            return recurrenceParentCallBack.call(this);
        }
        return null;
    }

    


    @Override
    public List<String> errors()
    {
        List<String> errors = super.errors();
        String reccurenceIDErrorString = checkRecurrenceIdConsistency();
        if (reccurenceIDErrorString != null)
        {
            errors.add(reccurenceIDErrorString);
        }
        
        if (getDateTimeStart() != null)
        {
            DateTimeType startType = DateTimeUtilities.DateTimeType.of(getDateTimeStart().getValue());
            if (getExceptionDates() != null)
            {
                // assumes all exceptions are same Temporal type.  There is a listener to guarantee that assumption.
                Temporal e1 = getExceptionDates().get(0).getValue().iterator().next();
                DateTimeType exceptionType = DateTimeUtilities.DateTimeType.of(e1);
                boolean isExceptionTypeMatch = startType == exceptionType;
                if (! isExceptionTypeMatch)
                {
                    errors.add("DTSTART, EXDATE: The value type of EXDATE elements MUST be the same as the DTSTART property (" + exceptionType + ", " + startType + ")");
                }
            }
            

//            if (getRecurrenceId() != null && getParent() != null)
//            {
//                DateTimeType recurrenceIdType = DateTimeUtilities.DateTimeType.of(getRecurrenceId().getValue());
//                List<VComponentDisplayableBase<?>> relatedComponents = ((VCalendar) getParent()).uidComponentsMap().get(getUniqueIdentifier().getValue());
//                VComponentDisplayableBase<?> parentComponent = relatedComponents.stream()
//                        .filter(v -> v.getRecurrenceId() == null)
//                        .findFirst()
//                        .orElseGet(() -> null);
//                if (parentComponent != null)
//                {
//                    DateTimeType dateTimeStartType = DateTimeUtilities.DateTimeType.of(parentComponent.getDateTimeStart().getValue());
//                    if (recurrenceIdType != dateTimeStartType)
//                    {
//                        errors.add("The value type of RECURRENCE-ID MUST be the same as the parent's DTSTART property (" + recurrenceIdType + ", " + dateTimeStartType);
//                    }
//                } else
//                {
//                    errors.add("Parent of this component with RECURRENCE-ID can't be found.");                    
//                }
//            }
            
            // Tests from Repeatable
            errors.addAll(VComponentRepeatable.errorsRepeatable(this));
        }
    
        return errors;
    }
}
