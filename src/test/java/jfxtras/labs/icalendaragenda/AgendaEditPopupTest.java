package jfxtras.labs.icalendaragenda;

import static org.junit.Assert.assertEquals;

import java.time.LocalDateTime;
import java.time.temporal.Temporal;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Test;

import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.ComboBox;
import javafx.scene.input.MouseButton;
import jfxtras.labs.icalendarfx.properties.component.recurrence.rrule.FrequencyType;
import jfxtras.test.AssertNode;
import jfxtras.test.TestUtil;

/**
 * Tests adding and removing VComponents outside the ICalendarAgenda implementation.
 * Inside ICalendarAgenda adding and removing VComponents is handled by removing
 * instances (Appointments) by Agenda through the popups.
 * 
 * The change vComponentsListener should only fire by changes made outside the implementation.
 *
 * @author David Bal
 */
public class AgendaEditPopupTest extends AgendaTestAbstract
{
    @Override
    public Parent getRootNode()
    {
        return super.getRootNode();
    }
        
    @Test
    public void canProduceEditPopup()
    {
        TestUtil.runThenWaitForPaintPulse( () -> agenda.getVCalendar().getVEvents().add(ICalendarStaticComponents.getDaily1()));

        // Open edit popup
        move("#hourLine11");
        press(MouseButton.SECONDARY);
        release(MouseButton.SECONDARY);
        
//        TestUtil.sleep(10000);
        Node n = find("#editDisplayableTabPane");
//        AssertNode.generateSource("n", n, null, false, jfxtras.test.AssertNode.A.XYWH);
        new AssertNode(n).assertXYWH(0.0, 0.0, 400.0, 570.0, 0.01);
        closeCurrentWindow();
    }
    
    @Test
    public void canMakeRepeatableAppointments()
    {
        TestUtil.runThenWaitForPaintPulse( () -> agenda.getVCalendar().getVEvents().add(ICalendarStaticComponents.getIndividual1()));
        
        // Open edit popup
        move("#hourLine11");
        press(MouseButton.SECONDARY);
        release(MouseButton.SECONDARY);
        
        click("#recurrenceRuleTab");
        click("#repeatableCheckBox");
        ComboBox<FrequencyType> frequencyComboBox = find("#frequencyComboBox");
        TestUtil.runThenWaitForPaintPulse( () -> frequencyComboBox.setValue(FrequencyType.DAILY));
        click("#saveRepeatButton");

        List<Temporal> expectedStarts = Arrays.asList(
                LocalDateTime.of(2015, 11, 11, 10, 30),
                LocalDateTime.of(2015, 11, 12, 10, 30),
                LocalDateTime.of(2015, 11, 13, 10, 30),
                LocalDateTime.of(2015, 11, 14, 10, 30)
                );
        List<Temporal> starts = agenda.appointments().stream()
            .map(a -> a.getStartTemporal())
            .collect(Collectors.toList());
        assertEquals(expectedStarts, starts);

        List<Temporal> expectedEnds = Arrays.asList(
                LocalDateTime.of(2015, 11, 11, 11, 30),
                LocalDateTime.of(2015, 11, 12, 11, 30),
                LocalDateTime.of(2015, 11, 13, 11, 30),
                LocalDateTime.of(2015, 11, 14, 11, 30)
                );
        List<Temporal> ends = agenda.appointments().stream()
            .map(a -> a.getEndTemporal())
            .collect(Collectors.toList());
        assertEquals(expectedEnds, ends);
    }

//    @Test
//    public void canToggleRepeatableCheckBox()
//    {
//        TestUtil.runThenWaitForPaintPulse( () -> agenda.vComponents().add(ICalendarStaticVEvents.getDaily1()));
//        VEventImpl v = (VEventImpl) agenda.vComponents().get(0);
//
//        // Open edit popup
//        move("#hourLine11");
//        press(MouseButton.SECONDARY);
//        release(MouseButton.SECONDARY);
//        click("#repeatableTab");
//
//        // Get properties        
//        CheckBox repeatableCheckBox = find("#repeatableCheckBox");
//
//        // Check initial state
//        assertTrue(repeatableCheckBox.isSelected());
//        assertTrue(v.getRRule() != null);
//        
//        // Change property and verify state change
//        TestUtil.runThenWaitForPaintPulse( () -> repeatableCheckBox.setSelected(false));
//        assertTrue(v.getRRule() == null);
//        
//        closeCurrentWindow();
//    }
//    
//    @Test
//    public void canChangeFrequency()
//    {
//        TestUtil.runThenWaitForPaintPulse( () -> agenda.vComponents().add(ICalendarStaticVEvents.getDaily1()));
//        VEventImpl v = (VEventImpl) agenda.vComponents().get(0);
//
//        // Open edit popup
//        move("#hourLine11");
//        press(MouseButton.SECONDARY);
//        release(MouseButton.SECONDARY);
//        click("#repeatableTab");
//
//        // Get properties
//        ComboBox<FrequencyType> frequencyComboBox = find("#frequencyComboBox");
//
//        // Check initial state
//        assertEquals(FrequencyType.DAILY, frequencyComboBox.getSelectionModel().getSelectedItem());
//        assertEquals(FrequencyType.DAILY, v.getRRule().getFrequency().frequencyType());
//        
//        // Change property and verify state change
//        // WEEKLY
//        {
//        TestUtil.runThenWaitForPaintPulse(() -> frequencyComboBox.getSelectionModel().select(FrequencyType.WEEKLY));
//
//        // Days of the week properties
//        CheckBox su = (CheckBox) find("#sundayCheckBox");
//        CheckBox mo = (CheckBox) find("#mondayCheckBox");
//        CheckBox tu = (CheckBox) find("#tuesdayCheckBox");
//        CheckBox we = (CheckBox) find("#wednesdayCheckBox");
//        CheckBox th = (CheckBox) find("#thursdayCheckBox");
//        CheckBox fr = (CheckBox) find("#fridayCheckBox");
//        CheckBox sa = (CheckBox) find("#saturdayCheckBox");
//        
//        // Get weekly properties
//        Frequency f = v.getRRule().getFrequency();
//        assertEquals(FrequencyType.WEEKLY, f.frequencyType());
//        assertEquals(1, f.byRules().size());
//        ByDay rule = (ByDay) f.lookupByRule(ByRuleType.BY_DAY);
//
//        // Check initial state
//        List<DayOfWeek> expectedDOW = Arrays.asList(DayOfWeek.WEDNESDAY);
//        assertEquals(expectedDOW, rule.dayOfWeekWithoutOrdinalList());
//        HBox weeklyHBox = find("#weeklyHBox");
//        assertTrue(weeklyHBox.isVisible());       
//        assertFalse(su.isSelected());
//        assertFalse(mo.isSelected());
//        assertFalse(tu.isSelected());
//        assertTrue(we.isSelected());
//        assertFalse(th.isSelected());
//        assertFalse(fr.isSelected());
//        assertFalse(sa.isSelected());
//        
//        // Toggle each day of week and check
//        TestUtil.runThenWaitForPaintPulse( () -> su.setSelected(true));
//        assertTrue(su.isSelected());
//        assertTrue(rule.dayOfWeekWithoutOrdinalList().contains(DayOfWeek.SUNDAY));
//        TestUtil.runThenWaitForPaintPulse( () -> mo.setSelected(true));
//        assertTrue(mo.isSelected());
//        assertTrue(rule.dayOfWeekWithoutOrdinalList().contains(DayOfWeek.MONDAY));
//        TestUtil.runThenWaitForPaintPulse( () -> tu.setSelected(true));
//        assertTrue(tu.isSelected());
//        assertTrue(rule.dayOfWeekWithoutOrdinalList().contains(DayOfWeek.TUESDAY));
//        // Wednesday already selected
//        TestUtil.runThenWaitForPaintPulse( () -> th.setSelected(true));
//        assertTrue(th.isSelected());
//        assertTrue(rule.dayOfWeekWithoutOrdinalList().contains(DayOfWeek.THURSDAY));
//        TestUtil.runThenWaitForPaintPulse( () -> fr.setSelected(true));
//        assertTrue(fr.isSelected());
//        assertTrue(rule.dayOfWeekWithoutOrdinalList().contains(DayOfWeek.FRIDAY));
//        TestUtil.runThenWaitForPaintPulse( () -> sa.setSelected(true));
//        assertTrue(sa.isSelected());
//        assertTrue(rule.dayOfWeekWithoutOrdinalList().contains(DayOfWeek.SATURDAY));
//        List<DayOfWeek> allDaysOfWeek = Arrays.asList(DayOfWeek.values());
//        List<DayOfWeek> daysOfWeek = rule.dayOfWeekWithoutOrdinalList();
//        Collections.sort(daysOfWeek);
//        assertEquals(allDaysOfWeek, daysOfWeek);
//        }
//
//        // MONTHLY
//        {
//        TestUtil.runThenWaitForPaintPulse(() -> frequencyComboBox.getSelectionModel().select(FrequencyType.MONTHLY));
//        
//        // Monthly properties
//        RadioButton dayOfMonth = find("#dayOfMonthRadioButton");
//        RadioButton dayOfWeek = find("#dayOfWeekRadioButton");
//        Frequency f = v.getRRule().getFrequency(); // refresh reference
//        
//        // Check initial state
//        assertTrue(dayOfMonth.isSelected());
//        assertFalse(dayOfWeek.isSelected());
//        assertEquals(FrequencyType.MONTHLY, f.frequencyType());
//        assertEquals(0, f.byRules().size());
//        
//        // Toggle monthly options and check
//        TestUtil.runThenWaitForPaintPulse(() -> dayOfWeek.setSelected(true));
//        assertFalse(dayOfMonth.isSelected());
//        assertTrue(dayOfWeek.isSelected());
//        assertEquals(1, f.byRules().size());
//        ByDay rule = (ByDay) f.lookupByRule(ByRuleType.BY_DAY);
//        assertEquals("BYDAY=2WE", rule.toString()); // 2nd Wednesday of the month
//        }
//        
//        // YEARLY
//        {
//        TestUtil.runThenWaitForPaintPulse(() -> frequencyComboBox.getSelectionModel().select(FrequencyType.YEARLY));
//        Frequency f = v.getRRule().getFrequency(); // refresh reference
//
//        // Check initial state
//        assertEquals(FrequencyType.YEARLY, f.frequencyType());
//        assertEquals(0, f.byRules().size());
//        }
//        closeCurrentWindow();
//    }
//
//    @Test
//    public void canChangeInterval()
//    {
//        TestUtil.runThenWaitForPaintPulse( () -> agenda.vComponents().add(ICalendarStaticVEvents.getDaily1()));
//        VEventImpl v = (VEventImpl) agenda.vComponents().get(0);
//
//        // Open edit popup
//        move("#hourLine11");
//        press(MouseButton.SECONDARY);
//        release(MouseButton.SECONDARY);
//        click("#repeatableTab");
//
//        // Get properties
//        Spinner<Integer> intervalSpinner = find("#intervalSpinner");
//
//        // Check initial state
//        assertEquals((Integer) 1, intervalSpinner.getValue());
//        assertEquals((Integer) 1, v.getRRule().getFrequency().getInterval());
//        
//        // Change property and verify state change
//        TestUtil.runThenWaitForPaintPulse( () -> intervalSpinner.getValueFactory().increment(3));       
//        assertEquals((Integer) 4, intervalSpinner.getValue());
//        assertEquals((Integer) 4, v.getRRule().getFrequency().getInterval());
//        
//        closeCurrentWindow();
//    }
//
//    @Test
//    public void canSetUntilDate()
//    {
//        TestUtil.runThenWaitForPaintPulse( () -> agenda.vComponents().add(ICalendarStaticVEvents.getDaily1()));
//        VEventImpl v = (VEventImpl) agenda.vComponents().get(0);
//
//        // Open edit popup
//        move("#hourLine11");
//        press(MouseButton.SECONDARY);
//        release(MouseButton.SECONDARY);
//        click("#repeatableTab");
//
//        // Set until date
//        click("#untilRadioButton");
//        DatePicker untilDatePicker = find("#untilDatePicker");
//        TestUtil.runThenWaitForPaintPulse( () -> untilDatePicker.setValue(LocalDate.of(2015, 11, 13)));
//
//        // save change
//        click("#saveRepeatButton");
//
//        // Verify state change
//        assertEquals(5, agenda.appointments().size());
//        
////        closeCurrentWindow();
//    }
//    
//    @Test
//    public void canChangeStartDate()
//    {
//        TestUtil.runThenWaitForPaintPulse( () -> agenda.vComponents().add(ICalendarStaticVEvents.getDaily1()));
//        VEventImpl v = (VEventImpl) agenda.vComponents().get(0);
//
//        // Open edit popup
//        move("#hourLine11");
//        press(MouseButton.SECONDARY);
//        release(MouseButton.SECONDARY);
//        click("#repeatableTab");
//
//        // Get properties        
//        DatePicker startDatePicker = find("#startDatePicker");
//
//        // Check initial state
//        assertEquals(LocalDate.of(2015, 11, 9), startDatePicker.getValue());
//        assertEquals(LocalDateTime.of(2015, 11, 9, 10, 0), v.getDateTimeStart());
//        
//        // Change property and verify state change
//        TestUtil.runThenWaitForPaintPulse( () -> startDatePicker.setValue(LocalDate.of(2015, 11, 10)));
//        assertEquals(LocalDateTime.of(2015, 11, 10, 10, 0), v.getDateTimeStart());
//        
//        // Change property and verify state change
//        // Frequency - Weekly
//        ComboBox<FrequencyType> frequencyComboBox = find("#frequencyComboBox");
//        TestUtil.runThenWaitForPaintPulse(() -> frequencyComboBox.getSelectionModel().select(FrequencyType.WEEKLY));
//        click("#saveRepeatButton");
//        ComboBox<ChangeDialogOption> c = find("#changeDialogComboBox");
//        TestUtil.runThenWaitForPaintPulse(() -> c.getSelectionModel().select(ChangeDialogOption.ALL));
//        click("#changeDialogOkButton");
//        List<LocalDateTime> dates = agenda.appointments()
//                .stream()
//                .map(a -> a.getStartLocalDateTime())
//                .collect(Collectors.toList());        
//        List<LocalDateTime> expectedDates = new ArrayList<LocalDateTime>(Arrays.asList(
//                LocalDateTime.of(2015, 11, 11, 10, 0)
//                ));
//        Assert.assertEquals(expectedDates, dates);
//    }
//
//    @Test
//    public void canChangeEndsCriteria()
//    {
//        TestUtil.runThenWaitForPaintPulse( () -> agenda.vComponents().add(ICalendarStaticVEvents.getDaily1()));
//        VEventImpl v = (VEventImpl) agenda.vComponents().get(0);
//
//        // Open edit popup
//        move("#hourLine11");
//        press(MouseButton.SECONDARY);
//        release(MouseButton.SECONDARY);
//        click("#repeatableTab");
//
//        // Get properties
//        RadioButton endNeverRadioButton = find("#endNeverRadioButton");
//        RadioButton endAfterRadioButton = find("#endAfterRadioButton");
//        Spinner<Integer> endAfterEventsSpinner = find("#endAfterEventsSpinner");
//        RadioButton untilRadioButton = find("#untilRadioButton");
//        DatePicker untilDatePicker = find("#untilDatePicker");
//
//        // Check initial state
//        assertTrue(endNeverRadioButton.isSelected());
//        assertFalse(endAfterRadioButton.isSelected());
//        assertFalse(untilRadioButton.isSelected());
//        assertEquals((Integer) 0, v.getRRule().getCount());
//        assertNull(v.getRRule().getUntil());
//        
//        // Change property and verify state change
//        // Ends After (COUNT)
//        TestUtil.runThenWaitForPaintPulse( () -> endAfterRadioButton.setSelected(true));
//        assertFalse(endNeverRadioButton.isSelected());
//        assertTrue(endAfterRadioButton.isSelected());
//        assertFalse(untilRadioButton.isSelected());
//        assertEquals((Integer) RepeatableController.INITIAL_COUNT, v.getRRule().getCount()); // default value is 10
//        TestUtil.runThenWaitForPaintPulse( () -> endAfterEventsSpinner.getValueFactory().increment(5));
//        assertEquals((Integer) 15, v.getRRule().getCount());
//
//        // Ends On (UNTIL)
//        TestUtil.runThenWaitForPaintPulse( () -> untilRadioButton.setSelected(true));
//        assertFalse(endNeverRadioButton.isSelected());
//        assertFalse(endAfterRadioButton.isSelected());
//        assertTrue(untilRadioButton.isSelected());
//        LocalDateTime expectedUntil = LocalDateTime.of(2015, 11, 9, 10, 0).plus(RepeatableController.DEFAULT_UNTIL_PERIOD);
//        assertEquals(expectedUntil, v.getRRule().getUntil());
//        TestUtil.runThenWaitForPaintPulse( () -> untilDatePicker.setValue(LocalDate.of(2016, 1, 1)));
//        assertEquals(LocalDateTime.of(2016, 1, 1, 10, 0), v.getRRule().getUntil());
//        closeCurrentWindow();
//    }
//    
//    @Test
//    //@Ignore
//    public void canMakeExceptionList()
//    {
//        TestUtil.runThenWaitForPaintPulse( () -> agenda.vComponents().add(ICalendarStaticVEvents.getDaily1()));
//        VEventImpl v = (VEventImpl) agenda.vComponents().get(0);
//
//        // Open edit popup
//        move("#hourLine11");
//        press(MouseButton.SECONDARY);
//        release(MouseButton.SECONDARY);
//        click("#repeatableTab");
//        
//        // Get properties
//        ComboBox<Temporal> exceptionComboBox = find("#exceptionComboBox");
//
//        // Check initial state
//        List<Temporal> exceptions = exceptionComboBox.getItems().stream().collect(Collectors.toList());
//        LocalDateTime seed = LocalDateTime.of(2015, 11, 9, 10, 0);
//        List<LocalDateTime> expectedDates = Stream
//                .iterate(seed, a -> a.plus(1, ChronoUnit.DAYS))
//                .limit(RepeatableController.EXCEPTION_CHOICE_LIMIT)
//                .collect(Collectors.toList());
//        assertEquals(expectedDates, exceptions);
//        closeCurrentWindow();
//    }
//    
//    @Test
//    public void canMakeExceptionListWholeDay() // Whole day appointments
//    {
//        TestUtil.runThenWaitForPaintPulse( () -> agenda.vComponents().add(ICalendarStaticVEvents.getDaily1()));
//        VEventImpl v = (VEventImpl) agenda.vComponents().get(0);
//
//        // Open edit popup
//        move("#hourLine11");
//        press(MouseButton.SECONDARY);
//        release(MouseButton.SECONDARY);
//        
//        // Make whole day
//        click("#wholeDayCheckBox");
//        
//        // check whole day fields
//        LocalDateTimeTextField startTextField = find("#startTextField");
//        LocalDateTimeTextField endTextField = find("#endTextField");
//        assertEquals(LocalDateTime.of(2015, 11, 11, 0, 0), startTextField.getLocalDateTime());
//        assertEquals(LocalDateTime.of(2015, 11, 12, 0, 0), endTextField.getLocalDateTime());
//        assertEquals(LocalDate.of(2015, 11, 9), v.getDateTimeStart());
//        assertEquals(LocalDate.of(2015, 11, 10), v.getDateTimeEnd());
//
//        // go back to time based
//        click("#wholeDayCheckBox");
//        assertEquals(LocalDateTime.of(2015, 11, 11, 10, 0), startTextField.getLocalDateTime());
//        assertEquals(LocalDateTime.of(2015, 11, 11, 11, 0), endTextField.getLocalDateTime());
//        assertEquals(LocalDateTime.of(2015, 11, 9, 10, 0), v.getDateTimeStart());
//        assertEquals(LocalDateTime.of(2015, 11, 9, 11, 0), v.getDateTimeEnd());
//
//        // Make whole day again
//        click("#wholeDayCheckBox");
//        
//        // Go to repeatable tab
//        click("#repeatableTab");
//        
//        // Get properties
//        ComboBox<Temporal> exceptionComboBox = find("#exceptionComboBox");
//
//        // Check initial state
//        List<Temporal> exceptions = exceptionComboBox.getItems().stream().collect(Collectors.toList());
//        LocalDate seed = LocalDate.of(2015, 11, 9);
//        List<LocalDate> expectedDates = Stream
//                .iterate(seed, a -> a.plus(1, ChronoUnit.DAYS))
//                .limit(RepeatableController.EXCEPTION_CHOICE_LIMIT)
//                .collect(Collectors.toList());
//        assertEquals(expectedDates, exceptions);
//        closeCurrentWindow();
//    }
//    
//    @Test
//    public void canMakeExceptionListWeekly()
//    {
//        TestUtil.runThenWaitForPaintPulse( () -> agenda.vComponents().add(ICalendarStaticVEvents.getDaily1()));
//        VEventImpl v = (VEventImpl) agenda.vComponents().get(0);
//
//        // Open edit popup
//        move("#hourLine11");
//        press(MouseButton.SECONDARY);
//        release(MouseButton.SECONDARY);
//        click("#repeatableTab");
//        
//        // Get properties
//        ComboBox<Temporal> exceptionComboBox = find("#exceptionComboBox");
//        ComboBox<FrequencyType> frequencyComboBox = find("#frequencyComboBox");
//
//        // Change property and verify state change
//        // Frequency - Weekly
//        TestUtil.runThenWaitForPaintPulse(() -> frequencyComboBox.getSelectionModel().select(FrequencyType.WEEKLY));
//        {
//            List<Temporal> exceptions = exceptionComboBox.getItems()
//                    .stream()
//                    .limit(5)
//                    .collect(Collectors.toList());
//            List<LocalDateTime> expectedDates = new ArrayList<>(Arrays.asList(
//                    LocalDateTime.of(2015, 11, 11, 10, 0)
//                  , LocalDateTime.of(2015, 11, 18, 10, 0)
//                  , LocalDateTime.of(2015, 11, 25, 10, 0)
//                  , LocalDateTime.of(2015, 12, 2, 10, 0)
//                  , LocalDateTime.of(2015, 12, 9, 10, 0)
//                    ));
//            assertEquals(expectedDates, exceptions);
//        }
//        
//        // Days of the week properties
//        CheckBox su = (CheckBox) find("#sundayCheckBox");
//        CheckBox mo = (CheckBox) find("#mondayCheckBox");
//        CheckBox tu = (CheckBox) find("#tuesdayCheckBox");
//        CheckBox we = (CheckBox) find("#wednesdayCheckBox");
//        CheckBox th = (CheckBox) find("#thursdayCheckBox");
//        CheckBox fr = (CheckBox) find("#fridayCheckBox");
//        CheckBox sa = (CheckBox) find("#saturdayCheckBox");
//        
//        // Get weekly properties
//        Frequency f = v.getRRule().getFrequency();
//        assertEquals(FrequencyType.WEEKLY, f.frequencyType());
//        assertEquals(1, f.byRules().size());
//        ByDay rule = (ByDay) f.lookupByRule(ByRuleType.BY_DAY);
//
//        // Check initial state
//        List<DayOfWeek> expectedDOW = Arrays.asList(DayOfWeek.WEDNESDAY);
//        assertEquals(expectedDOW, rule.dayOfWeekWithoutOrdinalList());
//        HBox weeklyHBox = find("#weeklyHBox");
//        assertTrue(weeklyHBox.isVisible());       
//        assertFalse(su.isSelected());
//        assertFalse(mo.isSelected());
//        assertFalse(tu.isSelected());
//        assertTrue(we.isSelected());
//        assertFalse(th.isSelected());
//        assertFalse(fr.isSelected());
//        assertFalse(sa.isSelected());
//        
//        // Toggle each day of week and check
//        TestUtil.runThenWaitForPaintPulse( () -> su.setSelected(true));
//        {
//            List<Temporal> exceptions = exceptionComboBox.getItems()
//                    .stream()
//                    .limit(5)
//                    .collect(Collectors.toList());
//            List<LocalDateTime> expectedDates = new ArrayList<>(Arrays.asList(
//                   LocalDateTime.of(2015, 11, 11, 10, 0)
//                 , LocalDateTime.of(2015, 11, 15, 10, 0)
//                 , LocalDateTime.of(2015, 11, 18, 10, 0)
//                 , LocalDateTime.of(2015, 11, 22, 10, 0)
//                 , LocalDateTime.of(2015, 11, 25, 10, 0)
//                    ));
//            assertEquals(expectedDates, exceptions);
//        }
//
//        TestUtil.runThenWaitForPaintPulse( () -> mo.setSelected(true));
//        {
//            List<Temporal> exceptions = exceptionComboBox.getItems()
//                    .stream()
//                    .limit(5)
//                    .collect(Collectors.toList());
//            List<LocalDateTime> expectedDates = new ArrayList<>(Arrays.asList(
//                    LocalDateTime.of(2015, 11, 9, 10, 0)
//                  , LocalDateTime.of(2015, 11, 11, 10, 0)
//                  , LocalDateTime.of(2015, 11, 15, 10, 0)
//                  , LocalDateTime.of(2015, 11, 16, 10, 0)
//                  , LocalDateTime.of(2015, 11, 18, 10, 0)
//                    ));
//            assertEquals(expectedDates, exceptions);
//        }
//
//        TestUtil.runThenWaitForPaintPulse( () -> tu.setSelected(true));
//        {
//            List<Temporal> exceptions = exceptionComboBox.getItems()
//                    .stream()
//                    .limit(5)
//                    .collect(Collectors.toList());
//            List<LocalDateTime> expectedDates = new ArrayList<>(Arrays.asList(
//                    LocalDateTime.of(2015, 11, 9, 10, 0)
//                  , LocalDateTime.of(2015, 11, 10, 10, 0)
//                  , LocalDateTime.of(2015, 11, 11, 10, 0)
//                  , LocalDateTime.of(2015, 11, 15, 10, 0)
//                  , LocalDateTime.of(2015, 11, 16, 10, 0)
//                    ));
//            assertEquals(expectedDates, exceptions);
//        }
//        
//        TestUtil.runThenWaitForPaintPulse( () -> we.setSelected(false)); // turn Wednesday off (initially on)
//        {
//            List<Temporal> exceptions = exceptionComboBox.getItems()
//                    .stream()
//                    .limit(5)
//                    .collect(Collectors.toList());
//            List<LocalDateTime> expectedDates = new ArrayList<>(Arrays.asList(
//                    LocalDateTime.of(2015, 11, 9, 10, 0)
//                  , LocalDateTime.of(2015, 11, 10, 10, 0)
//                  , LocalDateTime.of(2015, 11, 15, 10, 0)
//                  , LocalDateTime.of(2015, 11, 16, 10, 0)
//                  , LocalDateTime.of(2015, 11, 17, 10, 0)
//                    ));
//            assertEquals(expectedDates, exceptions);
//        }
//        
//        TestUtil.runThenWaitForPaintPulse( () -> th.setSelected(true));
//        {
//            List<Temporal> exceptions = exceptionComboBox.getItems()
//                    .stream()
//                    .limit(5)
//                    .collect(Collectors.toList());
//            List<LocalDateTime> expectedDates = new ArrayList<>(Arrays.asList(
//                    LocalDateTime.of(2015, 11, 9, 10, 0)
//                  , LocalDateTime.of(2015, 11, 10, 10, 0)
//                  , LocalDateTime.of(2015, 11, 12, 10, 0)
//                  , LocalDateTime.of(2015, 11, 15, 10, 0)
//                  , LocalDateTime.of(2015, 11, 16, 10, 0)
//                    ));
//            assertEquals(expectedDates, exceptions);
//        }
//        
//        TestUtil.runThenWaitForPaintPulse( () -> fr.setSelected(true));
//        {
//            List<Temporal> exceptions = exceptionComboBox.getItems()
//                    .stream()
//                    .limit(5)
//                    .collect(Collectors.toList());
//            List<LocalDateTime> expectedDates = new ArrayList<>(Arrays.asList(
//                    LocalDateTime.of(2015, 11, 9, 10, 0)
//                  , LocalDateTime.of(2015, 11, 10, 10, 0)
//                  , LocalDateTime.of(2015, 11, 12, 10, 0)
//                  , LocalDateTime.of(2015, 11, 13, 10, 0)
//                  , LocalDateTime.of(2015, 11, 15, 10, 0)
//                    ));
//            assertEquals(expectedDates, exceptions);
//        }
//        
//        TestUtil.runThenWaitForPaintPulse( () -> sa.setSelected(true));
//        {
//            List<Temporal> exceptions = exceptionComboBox.getItems()
//                    .stream()
//                    .limit(5)
//                    .collect(Collectors.toList());
//            List<LocalDateTime> expectedDates = new ArrayList<>(Arrays.asList(
//                    LocalDateTime.of(2015, 11, 9, 10, 0)
//                  , LocalDateTime.of(2015, 11, 10, 10, 0)
//                  , LocalDateTime.of(2015, 11, 12, 10, 0)
//                  , LocalDateTime.of(2015, 11, 13, 10, 0)
//                  , LocalDateTime.of(2015, 11, 14, 10, 0)
//                    ));
//            assertEquals(expectedDates, exceptions);
//        }
//        
//        List<DayOfWeek> expectedDaysOfWeek = Arrays.asList(
//                DayOfWeek.MONDAY
//              , DayOfWeek.TUESDAY
//              , DayOfWeek.THURSDAY
//              , DayOfWeek.FRIDAY
//              , DayOfWeek.SATURDAY
//              , DayOfWeek.SUNDAY
//              );
//        List<DayOfWeek> daysOfWeek = rule.dayOfWeekWithoutOrdinalList();
//        Collections.sort(daysOfWeek);
//        assertEquals(expectedDaysOfWeek, daysOfWeek);        
//        
//        closeCurrentWindow();
//        }
//
//    @Test
//    //@Ignore
//    public void canMakeExceptionListMonthly()
//    {
//        TestUtil.runThenWaitForPaintPulse( () -> agenda.vComponents().add(ICalendarStaticVEvents.getDaily1()));
//        VEventImpl v = (VEventImpl) agenda.vComponents().get(0);
//
//        // Open edit popup
//        move("#hourLine11");
//        press(MouseButton.SECONDARY);
//        release(MouseButton.SECONDARY);
//        click("#repeatableTab");
//        
//        // Get properties
//        ComboBox<Temporal> exceptionComboBox = find("#exceptionComboBox");
//        ComboBox<FrequencyType> frequencyComboBox = find("#frequencyComboBox");
//        
//        // Change property and verify state change
//        // Frequency - Monthly
//        TestUtil.runThenWaitForPaintPulse(() -> frequencyComboBox.getSelectionModel().select(FrequencyType.MONTHLY));
//        RadioButton dayOfMonthRadioButton = find("#dayOfMonthRadioButton");
//        RadioButton dayOfWeekRadioButton = find("#dayOfWeekRadioButton");
//        
//        // Check initial state
//        assertTrue(dayOfMonthRadioButton.isSelected());
//        assertFalse(dayOfWeekRadioButton.isSelected());
//        {
//            List<Temporal> exceptions = exceptionComboBox.getItems().stream().limit(5)
//                    .collect(Collectors.toList());
//            List<LocalDateTime> expectedDates = new ArrayList<>(Arrays.asList(
//                    LocalDateTime.of(2015, 11, 9, 10, 0)
//                  , LocalDateTime.of(2015, 12, 9, 10, 0)
//                  , LocalDateTime.of(2016, 1, 9, 10, 0)
//                  , LocalDateTime.of(2016, 2, 9, 10, 0)
//                  , LocalDateTime.of(2016, 3, 9, 10, 0)
//                    ));
//            assertEquals(expectedDates, exceptions);
//        }
//
//        // check dayOfWeekRadioButton and check state
//        TestUtil.runThenWaitForPaintPulse(() -> dayOfWeekRadioButton.setSelected(true));
//        assertFalse(dayOfMonthRadioButton.isSelected());
//        assertTrue(dayOfWeekRadioButton.isSelected());
//        {
//            List<Temporal> exceptions = exceptionComboBox.getItems().stream().limit(5)
//                    .collect(Collectors.toList());
//            List<LocalDateTime> expectedDates = new ArrayList<>(Arrays.asList(
//                    LocalDateTime.of(2015, 11, 11, 10, 0)
//                  , LocalDateTime.of(2015, 12, 9, 10, 0)
//                  , LocalDateTime.of(2016, 1, 13, 10, 0)
//                  , LocalDateTime.of(2016, 2, 10, 10, 0)
//                  , LocalDateTime.of(2016, 3, 9, 10, 0)
//                    ));
//            assertEquals(expectedDates, exceptions);
//        }
//        closeCurrentWindow();
//    }
//    
//    @Test
//    public void canMakeExceptionListYearly()
//    {
//        TestUtil.runThenWaitForPaintPulse( () -> agenda.vComponents().add(ICalendarStaticVEvents.getDaily1()));
//        VEventImpl v = (VEventImpl) agenda.vComponents().get(0);
//
//        // Open edit popup
//        move("#hourLine11");
//        press(MouseButton.SECONDARY);
//        release(MouseButton.SECONDARY);
//        click("#repeatableTab");
//        
//        // Get properties
//        ComboBox<Temporal> exceptionComboBox = find("#exceptionComboBox");
//        ComboBox<FrequencyType> frequencyComboBox = find("#frequencyComboBox");
//        
//        // Change property and verify state change
//        TestUtil.runThenWaitForPaintPulse(() -> frequencyComboBox.getSelectionModel().select(FrequencyType.YEARLY));
//        
//        // Check initial state
//        List<Temporal> exceptions = exceptionComboBox.getItems().stream().limit(5)
//                .collect(Collectors.toList());
//        List<LocalDateTime> expectedDates = new ArrayList<>(Arrays.asList(
//                LocalDateTime.of(2015, 11, 9, 10, 0)
//              , LocalDateTime.of(2016, 11, 9, 10, 0)
//              , LocalDateTime.of(2017, 11, 9, 10, 0)
//              , LocalDateTime.of(2018, 11, 9, 10, 0)
//              , LocalDateTime.of(2019, 11, 9, 10, 0)
//                ));
//        assertEquals(expectedDates, exceptions);
//        closeCurrentWindow();
//    }
//    
//    @Test
//    public void canMakeExceptionListInitial()
//    {
//        TestUtil.runThenWaitForPaintPulse( () -> agenda.vComponents().add(ICalendarStaticVEvents.getDaily1()));
//        VEventImpl v = (VEventImpl) agenda.vComponents().get(0);
//
//        // Open edit popup
//        move("#hourLine11");
//        press(MouseButton.SECONDARY);
//        release(MouseButton.SECONDARY);
//        click("#repeatableTab");
//        
//        // Get properties
//        DatePicker startDatePicker = find("#startDatePicker");
//        ComboBox<Temporal> exceptionComboBox = find("#exceptionComboBox");
//        
//        // Change property and verify state change
//        TestUtil.runThenWaitForPaintPulse( () -> startDatePicker.setValue(LocalDate.of(2015, 11, 10)));
//        
//        // Check initial state
//        List<Temporal> exceptions = exceptionComboBox.getItems().stream().limit(5)
//                .collect(Collectors.toList());
//        List<LocalDateTime> expectedDates = new ArrayList<>(Arrays.asList(
//                LocalDateTime.of(2015, 11, 10, 10, 0)
//              , LocalDateTime.of(2015, 11, 11, 10, 0)
//              , LocalDateTime.of(2015, 11, 12, 10, 0)
//              , LocalDateTime.of(2015, 11, 13, 10, 0)
//              , LocalDateTime.of(2015, 11, 14, 10, 0)
//                ));
//        assertEquals(expectedDates, exceptions);
////        TestUtil.sleep(3000);
//        closeCurrentWindow();
//    }
//    
//    @Test
//    public void canMakeExceptionListEndsCriteria()
//    {
//        TestUtil.runThenWaitForPaintPulse( () -> agenda.vComponents().add(ICalendarStaticVEvents.getDaily1()));
//        VEventImpl v = (VEventImpl) agenda.vComponents().get(0);
//
//        // Open edit popup
//        move("#hourLine11");
//        press(MouseButton.SECONDARY);
//        release(MouseButton.SECONDARY);
//        click("#repeatableTab");
//
//        // Get properties
//        ComboBox<Temporal> exceptionComboBox = find("#exceptionComboBox");
//        RadioButton endNeverRadioButton = find("#endNeverRadioButton");
//        RadioButton endAfterRadioButton = find("#endAfterRadioButton");
//        Spinner<Integer> endAfterEventsSpinner = find("#endAfterEventsSpinner");
//        RadioButton untilRadioButton = find("#untilRadioButton");
//        DatePicker untilDatePicker = find("#untilDatePicker");
//
//        // Change property and verify state change
//        // Ends After (COUNT)
//        TestUtil.runThenWaitForPaintPulse( () -> endAfterRadioButton.setSelected(true) );
//        {
//            List<Temporal> exceptions = exceptionComboBox.getItems().stream().collect(Collectors.toList());
//            LocalDateTime seed = LocalDateTime.of(2015, 11, 9, 10, 0);
//            List<LocalDateTime> expectedDates = Stream
//                    .iterate(seed, a -> a.plus(1, ChronoUnit.DAYS))
//                    .limit(RepeatableController.INITIAL_COUNT)
//                    .collect(Collectors.toList());
//            assertEquals(expectedDates, exceptions);
//        }
//        TestUtil.runThenWaitForPaintPulse( () -> endAfterEventsSpinner.getValueFactory().decrement(5) );
//        {
//            List<Temporal> exceptions = exceptionComboBox.getItems().stream().collect(Collectors.toList());
//            LocalDateTime seed = LocalDateTime.of(2015, 11, 9, 10, 0);
//            List<LocalDateTime> expectedDates = Stream
//                    .iterate(seed, a -> a.plus(1, ChronoUnit.DAYS))
//                    .limit(RepeatableController.INITIAL_COUNT-5)
//                    .collect(Collectors.toList());
//            assertEquals(expectedDates, exceptions);
//        }
//        
//        // Ends On (UNTIL)
//        TestUtil.runThenWaitForPaintPulse( () -> untilRadioButton.setSelected(true));
//        {
//            LocalDateTime expectedUntil = LocalDateTime.of(2015, 11, 9, 10, 0).plus(RepeatableController.DEFAULT_UNTIL_PERIOD);
//            List<Temporal> exceptions = exceptionComboBox.getItems().stream().collect(Collectors.toList());
//            LocalDateTime seed = LocalDateTime.of(2015, 11, 9, 10, 0);
//            Iterator<LocalDateTime> i = Stream.iterate(seed, a -> a.plus(1, ChronoUnit.DAYS)).iterator();
//            List<LocalDateTime> expectedDates = new ArrayList<>();
//            while (i.hasNext())
//            {
//                LocalDateTime d = i.next();
//                if (d.isAfter(expectedUntil)) break;
//                expectedDates.add(d);
//            }            
//            assertEquals(expectedDates, exceptions);
//        }
//        TestUtil.runThenWaitForPaintPulse( () -> untilDatePicker.setValue(LocalDate.of(2015, 11, 13)) );
//        {
//            List<Temporal> exceptions = exceptionComboBox.getItems().stream().collect(Collectors.toList());
//            List<LocalDateTime> expectedDates = new ArrayList<>(Arrays.asList(
//                    LocalDateTime.of(2015, 11, 9, 10, 0)
//                  , LocalDateTime.of(2015, 11, 10, 10, 0)
//                  , LocalDateTime.of(2015, 11, 11, 10, 0)
//                  , LocalDateTime.of(2015, 11, 12, 10, 0)
//                  , LocalDateTime.of(2015, 11, 13, 10, 0)
//                    ));      
//            assertEquals(expectedDates, exceptions);
//        }
//        
//        // Ends Never
//        TestUtil.runThenWaitForPaintPulse( () -> endNeverRadioButton.setSelected(true));
//        {
//            List<Temporal> exceptions = exceptionComboBox.getItems().stream().collect(Collectors.toList());
//            LocalDateTime seed = LocalDateTime.of(2015, 11, 9, 10, 0);
//            List<LocalDateTime> expectedDates = Stream
//                    .iterate(seed, a -> a.plus(1, ChronoUnit.DAYS))
//                    .limit(RepeatableController.EXCEPTION_CHOICE_LIMIT)
//                    .collect(Collectors.toList());
//            assertEquals(expectedDates, exceptions);
//        }
//        closeCurrentWindow();
//    }
//    
//    @Test
//    //@Ignore
//    public void canAddException2()
//    {
//        TestUtil.runThenWaitForPaintPulse( () -> agenda.vComponents().add(ICalendarStaticVEvents.getDaily1()));
//        VEventImpl v = (VEventImpl) agenda.vComponents().get(0);
//
//        // Open edit popup
//        move("#hourLine11");
//        press(MouseButton.SECONDARY);
//        release(MouseButton.SECONDARY);
//        click("#repeatableTab");
//        
//        // Get properties
//        ComboBox<Temporal> exceptionComboBox = find("#exceptionComboBox");
//        ListView<Temporal> exceptionsListView = find("#exceptionsListView");
//        
//        // Add exceptions and check
//        Temporal e1 = exceptionComboBox.getItems().get(2);
//        TestUtil.runThenWaitForPaintPulse( () -> exceptionComboBox.getSelectionModel().select(e1) );
//        click("#addExceptionButton");
//        {
//            Set<Temporal> vExceptions = v.getExDate().getTemporals();
//            assertEquals(1, vExceptions.size());
//            List<Temporal> exceptions = vExceptions
//                    .stream()
//                    .map(a -> (LocalDateTime) a).sorted()
//                    .collect(Collectors.toList());
//            List<Temporal> expectedExceptions = new ArrayList<>(Arrays.asList(
//                    LocalDateTime.of(2015, 11, 11, 10, 0)
//                    ));
//            assertEquals(expectedExceptions, exceptions);
//            assertEquals(expectedExceptions, exceptionsListView.getItems());            
//        }
//
//        { // verify added exception is not in exceptionComboBox list
//            List<Temporal> exceptions = exceptionComboBox.getItems().stream().limit(5)
//                    .collect(Collectors.toList());
//            List<LocalDateTime> expectedDates = new ArrayList<>(Arrays.asList(
//                    LocalDateTime.of(2015, 11, 9, 10, 0)
//                  , LocalDateTime.of(2015, 11, 10, 10, 0)
//                  , LocalDateTime.of(2015, 11, 12, 10, 0)
//                  , LocalDateTime.of(2015, 11, 13, 10, 0)
//                  , LocalDateTime.of(2015, 11, 14, 10, 0)
//                    ));
//            assertEquals(expectedDates, exceptions);
//        }
//
//        // Add another exceptions and check
//        Temporal e2 = exceptionComboBox.getItems().get(0);
//        TestUtil.runThenWaitForPaintPulse( () -> exceptionComboBox.getSelectionModel().select(e2) );
//        click("#addExceptionButton");
//        {
//            Set<Temporal> vExceptions = v.getExDate().getTemporals();
//            assertEquals(2, vExceptions.size());
//            List<LocalDateTime> exceptions = vExceptions
//                    .stream()
//                    .map(a -> (LocalDateTime) a).sorted()
//                    .collect(Collectors.toList());
//            List<LocalDateTime> expectedExceptions = new ArrayList<>(Arrays.asList(
//                    LocalDateTime.of(2015, 11, 9, 10, 0)
//                  , LocalDateTime.of(2015, 11, 11, 10, 0)
//                    ));
//            assertEquals(expectedExceptions, exceptions);
//            assertEquals(expectedExceptions, exceptionsListView.getItems()); 
//        }
//
//        { // verify added exceptions are not in exceptionComboBox list
//            List<Temporal> exceptions = exceptionComboBox.getItems().stream().limit(5)
//                    .collect(Collectors.toList());
//            List<LocalDateTime> expectedDates = new ArrayList<>(Arrays.asList(
//                    LocalDateTime.of(2015, 11, 10, 10, 0)
//                  , LocalDateTime.of(2015, 11, 12, 10, 0)
//                  , LocalDateTime.of(2015, 11, 13, 10, 0)
//                  , LocalDateTime.of(2015, 11, 14, 10, 0)
//                  , LocalDateTime.of(2015, 11, 15, 10, 0)
//                    ));
//            assertEquals(expectedDates, exceptions);
//        }
//        closeCurrentWindow();
//    }
//    
//    @Test
//    public void canRemoveException2()
//    {
//        TestUtil.runThenWaitForPaintPulse( () -> agenda.vComponents().add(ICalendarStaticVEvents.getDailyWithException1()));
//        VEventImpl v = (VEventImpl) agenda.vComponents().get(0);
//
//        // Open edit popup
//        move("#AppointmentRegularBodyPane2015-11-09/0"); 
//        press(MouseButton.SECONDARY);
//        release(MouseButton.SECONDARY);
//        click("#repeatableTab");
//
//        // Get properties
//        ComboBox<Temporal> exceptionComboBox = find("#exceptionComboBox");
//        ListView<Temporal> exceptionsListView = find("#exceptionsListView");
//        
//        { // verify initial state
//            Set<Temporal> vExceptions = v.getExDate().getTemporals();
//            List<Temporal> exceptions = vExceptions
//                    .stream()
//                    .map(a -> (LocalDateTime) a).sorted()
//                    .collect(Collectors.toList());
//            List<Temporal> expectedExceptions = new ArrayList<>(Arrays.asList(
//                    LocalDateTime.of(2015, 11, 12, 10, 0)
//                  , LocalDateTime.of(2015, 11, 15, 10, 0)
//                    ));
//            assertEquals(expectedExceptions, exceptions);
//            assertEquals(expectedExceptions, exceptionsListView.getItems());            
//        }
//
//        { // verify added exception is not in exceptionComboBox list
//            List<Temporal> exceptions = exceptionComboBox.getItems().stream()
//                    .collect(Collectors.toList());
//            List<LocalDateTime> expectedDates = new ArrayList<>(Arrays.asList(
//                      LocalDateTime.of(2015, 11, 9, 10, 0)
//                    , LocalDateTime.of(2015, 11, 18, 10, 0)
//                    , LocalDateTime.of(2015, 11, 21, 10, 0)
//                    , LocalDateTime.of(2015, 11, 24, 10, 0)
//                    ));
//            assertEquals(expectedDates, exceptions);
//        }
//
//        // remove Exceptions one at a time, confirm gone and returned to exceptionComboBox
//        TestUtil.runThenWaitForPaintPulse( () -> exceptionsListView.getSelectionModel().select(LocalDateTime.of(2015, 11, 12, 10, 0)));
//        click ("#removeExceptionButton");
//        { // verify new state
//            Set<Temporal> vExceptions = v.getExDate().getTemporals();
//            List<Temporal> exceptions = vExceptions
//                    .stream()
//                    .map(a -> (LocalDateTime) a).sorted()
//                    .collect(Collectors.toList());
//            List<Temporal> expectedExceptions = new ArrayList<>(Arrays.asList(
//                    LocalDateTime.of(2015, 11, 15, 10, 0)
//                    ));
//            assertEquals(expectedExceptions, exceptions);
//            assertEquals(expectedExceptions, exceptionsListView.getItems());            
//        }
//        { // verify added exception is not in exceptionComboBox list
//            List<Temporal> exceptions = exceptionComboBox.getItems().stream().limit(5)
//                    .collect(Collectors.toList());
//            List<LocalDateTime> expectedDates = new ArrayList<>(Arrays.asList(
//                      LocalDateTime.of(2015, 11, 9, 10, 0)
//                    , LocalDateTime.of(2015, 11, 12, 10, 0)
//                    , LocalDateTime.of(2015, 11, 18, 10, 0)
//                    , LocalDateTime.of(2015, 11, 21, 10, 0)
//                    , LocalDateTime.of(2015, 11, 24, 10, 0)
//                    ));
//            assertEquals(expectedDates, exceptions);
//        }
//        
//        TestUtil.runThenWaitForPaintPulse( () -> exceptionsListView.getSelectionModel().select(LocalDateTime.of(2015, 11, 15, 10, 0)));
//        click ("#removeExceptionButton");
//        { // verify new state
//            Set<Temporal> vExceptions = v.getExDate().getTemporals();
//            List<Temporal> exceptions = vExceptions
//                    .stream()
//                    .map(a -> (LocalDateTime) a).sorted()
//                    .collect(Collectors.toList());
//            List<Temporal> expectedExceptions = new ArrayList<>(); // empty list
//            assertEquals(expectedExceptions, exceptions);
//            assertEquals(expectedExceptions, exceptionsListView.getItems());            
//        }
//        { // verify added exception is not in exceptionComboBox list
//            List<Temporal> exceptions = exceptionComboBox.getItems().stream().limit(5)
//                    .collect(Collectors.toList());
//            List<LocalDateTime> expectedDates = new ArrayList<>(Arrays.asList(
//                      LocalDateTime.of(2015, 11, 9, 10, 0)
//                    , LocalDateTime.of(2015, 11, 12, 10, 0)
//                    , LocalDateTime.of(2015, 11, 15, 10, 0)
//                    , LocalDateTime.of(2015, 11, 18, 10, 0)
//                    , LocalDateTime.of(2015, 11, 21, 10, 0)
//                    ));
//            assertEquals(expectedDates, exceptions);
//        }
//        closeCurrentWindow();
//    }
//    
//    @Test
//    public void canEditThisAndFuture()
//    {
//       TestUtil.runThenWaitForPaintPulse( () -> agenda.vComponents().add(ICalendarStaticVEvents.getDaily1()));       
//       
//       // Open edit popup
//       move("#hourLine11"); // open edit popup
//       press(MouseButton.SECONDARY);
//       release(MouseButton.SECONDARY);
//
//       // edit property
//       TextField summaryTextField = find("#summaryTextField");
//       summaryTextField.setText("new summary");
//
//       // save changes to THIS AND FUTURE
//       click("#saveAppointmentButton");
//       ComboBox<ChangeDialogOption> c = find("#changeDialogComboBox");
//       TestUtil.runThenWaitForPaintPulse( () -> c.getSelectionModel().select(ChangeDialogOption.THIS_AND_FUTURE));
//       click("#changeDialogOkButton");
//
//       // verify VComponent changes
//       assertEquals(2, agenda.vComponents().size());
//       agenda.vComponents().sort(VComponent.VCOMPONENT_COMPARATOR);
//
//       VEventImpl v0 = (VEventImpl) agenda.vComponents().get(0);
//       VEventImpl expectedV0 = ICalendarStaticVEvents.getDaily1();
//       expectedV0.getRRule().setUntil(ZonedDateTime.of(LocalDateTime.of(2015, 11, 10, 10, 0), ZoneId.systemDefault())
//               .withZoneSameInstant(ZoneId.of("Z")));
//       assertTrue(VEventImpl.isEqualTo(expectedV0, v0));
//
//       VEventImpl v1 = (VEventImpl) agenda.vComponents().get(1);
//       VEventImpl expectedV1 = ICalendarStaticVEvents.getDaily1()
//               .withSummary(Summary.parse("new summary"))
//               .withDateTimeStart(LocalDateTime.of(2015, 11, 11, 10, 0))
//               .withDateTimeEnd(LocalDateTime.of(2015, 11, 11, 11, 0))
//               .withRelatedTo("20150110T080000-0@jfxtras.org")
//               .withUniqueIdentifier(v1.getUniqueIdentifier()) // uid is time-based so copy it to guarantee equality.
//               .withDateTimeStamp(v1.getDateTimeStamp()) // time stamp is time-based so copy it to guarantee equality.
//               .withSequence(1);
//       assertTrue(VEventImpl.isEqualTo(expectedV1, v1));
//
//       // verify Appointment changes
//       { // verify added exceptions are not in exceptionComboBox list
//           List<String> summaries = agenda.appointments()
//                   .stream()
//                   .map(a -> a.getSummary())
//                   .collect(Collectors.toList());
//           List<String> expectedSummaries = new ArrayList<>(Arrays.asList(
//                   "Daily1 Summary"
//                 , "Daily1 Summary"
//                 , "new summary"
//                 , "new summary"
//                 , "new summary"
//                 , "new summary"
//                   ));
//           assertEquals(expectedSummaries, summaries);
//       }
//    }
//    
//    @Test
//    public void canEditOne()
//    {
//        TestUtil.runThenWaitForPaintPulse( () -> agenda.vComponents().add(ICalendarStaticVEvents.getDaily1()));
//        
//        // Open edit popup
//        move("#hourLine11"); // open edit popup
//        press(MouseButton.SECONDARY);
//        release(MouseButton.SECONDARY);
//
//        // edit property
//        TextField summaryTextField = find("#summaryTextField");
//        summaryTextField.setText("new summary");
//
//        // save changes
//        click("#saveAppointmentButton");
//        ComboBox<ChangeDialogOption> c = find("#changeDialogComboBox");
//        TestUtil.runThenWaitForPaintPulse( () -> c.getSelectionModel().select(ChangeDialogOption.ONE));
//        click("#changeDialogOkButton");
//        
//        // verify VComponent changes
//        assertEquals(2, agenda.vComponents().size());
//        agenda.vComponents().sort(VComponent.VCOMPONENT_COMPARATOR);
//        VEventImpl v0 = (VEventImpl) agenda.vComponents().get(0);
//        VEventImpl v1 = (VEventImpl) agenda.vComponents().get(1);
//        VEventImpl expectedV0 = ICalendarStaticVEvents.getDaily1();
//        expectedV0.getRRule().recurrences().add(v1);
//        assertTrue(VEventImpl.isEqualTo(expectedV0, v0));
//
//        VEventImpl expectedV1 = ICalendarStaticVEvents.getDaily1()
//                .withSummary(Summary.parse("new summary"))
//                .withDateTimeStart(LocalDateTime.of(2015, 11, 11, 10, 0))
//                .withDateTimeEnd(LocalDateTime.of(2015, 11, 11, 11, 0))
//                .withDateTimeStamp(v1.getDateTimeStamp()) // time stamp is time-based so copy it to guarantee equality.
//                .withDateTimeRecurrence(LocalDateTime.of(2015, 11, 11, 10, 0))
//                .withRRule(null)
//                .withSequence(1);
//        assertTrue(VEventImpl.isEqualTo(expectedV1, v1));
//    }
//    
//    @Test
//    public void canCancelEdit()
//    {
//        TestUtil.runThenWaitForPaintPulse( () -> agenda.vComponents().add(ICalendarStaticVEvents.getDaily1()));       
//        VEventImpl v0 = (VEventImpl) agenda.vComponents().get(0);
//        
//        // Open edit popup
//        move("#hourLine11");
//        press(MouseButton.SECONDARY);
//        release(MouseButton.SECONDARY);
//
//        // edit properties
//        TextField summaryTextField = find("#summaryTextField");
//        summaryTextField.setText("new summary");
//        LocalDateTimeTextField startTextField = find("#startTextField");
//        startTextField.setLocalDateTime(LocalDateTime.of(2015, 11, 11, 10, 30));
//
//        // cancel changes
//        click("#saveAppointmentButton");
//        click("#changeDialogCancelButton");
//        
//        // check return to original state
//        VEventImpl expectedV0 = ICalendarStaticVEvents.getDaily1();
//        assertTrue(VEventImpl.isEqualTo(expectedV0, v0));
//        assertEquals("Daily1 Summary", v0.getSummary());
//        assertEquals(LocalDateTime.of(2015, 11, 9, 10, 0), v0.getDateTimeStart());
//    }
//    
//    @Test
//    public void canDeleteIndividualEdit()
//    {
//        TestUtil.runThenWaitForPaintPulse( () -> agenda.vComponents().add(ICalendarStaticVEvents.getIndividual1()));
//        
//        // Open edit popup
//        move("#hourLine11");
//        press(MouseButton.SECONDARY);
//        release(MouseButton.SECONDARY);
//
//        // delete VComponent
//        click("#deleteAppointmentButton");
//
//        // check return to original state
//        assertEquals(0, agenda.vComponents().size());
//    }
//    
//    @Test
//    public void canDeleteSeriesEdit()
//    {
//        TestUtil.runThenWaitForPaintPulse( () -> agenda.vComponents().add(ICalendarStaticVEvents.getDaily1()));
//        
//        // Open edit popup
//        move("#hourLine11");
//        press(MouseButton.SECONDARY);
//        release(MouseButton.SECONDARY);
//
//        // delete VComponent
//        click("#deleteAppointmentButton");
//        ComboBox<ChangeDialogOption> c = find("#changeDialogComboBox");
//        TestUtil.runThenWaitForPaintPulse( () -> c.getSelectionModel().select(ChangeDialogOption.ALL));
//        click("#changeDialogOkButton");
//
//        // check return to original state
//        assertEquals(0, agenda.vComponents().size());
//    }
//    
//    @Test
//    public void canDeleteSeriesThisAndFutureEdit()
//    {
//        TestUtil.runThenWaitForPaintPulse( () -> agenda.vComponents().add(ICalendarStaticVEvents.getDaily1()));
//        VEventImpl v = (VEventImpl) agenda.vComponents().get(0);
//        
//        // Open edit popup
//        move("#hourLine11");
//        press(MouseButton.SECONDARY);
//        release(MouseButton.SECONDARY);
//
//        // delete VComponent
//        click("#deleteAppointmentButton");
//        ComboBox<ChangeDialogOption> c = find("#changeDialogComboBox");
//        TestUtil.runThenWaitForPaintPulse( () -> c.getSelectionModel().select(ChangeDialogOption.THIS_AND_FUTURE));
//        click("#changeDialogOkButton");
//        
//        // verify VComponent changes
//        assertEquals(1, agenda.vComponents().size());
//        VEventImpl expectedV = ICalendarStaticVEvents.getDaily1();
//        expectedV.getRRule().setUntil( ZonedDateTime.of(LocalDateTime.of(2015, 11, 10, 10, 0), ZoneId.systemDefault())
//                .withZoneSameInstant(ZoneId.of("Z")) );
//        assertTrue(VEventImpl.isEqualTo(expectedV, v));
//    }
//    
//    @Test
//    public void canUpdateAppointmentTimeWhenMadeInvalidByRRuleChange()
//    {
//        TestUtil.runThenWaitForPaintPulse( () -> agenda.vComponents().add(ICalendarStaticVEvents.getWeeklyZoned()));
//        VEventImpl vEvent = (VEventImpl) agenda.vComponents().get(0);
//        
//        // Open edit popup
//        move("#AppointmentRegularBodyPane2015-11-09/0");
//        press(MouseButton.SECONDARY);
//        release(MouseButton.SECONDARY);
//        click("#repeatableTab");
//
//        // make changes
//        click("#mondayCheckBox"); // shut off monday
//
//        // verify alert opens
//        click("#appointmentTab");
//        find("#startInstanceChangedAlert");
//        closeCurrentWindow();
//        
//        // Get properties
//        LocalDateTimeTextField startTextField = find("#startTextField");
//        LocalDateTimeTextField endTextField = find("#endTextField");
//        
//        // verify properties changed
//        assertEquals(LocalDateTime.of(2015, 11, 11, 10, 0), startTextField.getLocalDateTime());
//        assertEquals(LocalDateTime.of(2015, 11, 11, 10, 45), endTextField.getLocalDateTime());     
//        
//        // verify VComponent changed
//        click("#saveAppointmentButton");
//        VEventImpl expectedVEvent = ICalendarStaticVEvents.getWeeklyZoned()
//                .withDateTimeStart(ZonedDateTime.of(LocalDateTime.of(2015, 11, 11, 10, 0), ZoneId.of("America/Los_Angeles")))
//                .withDateTimeEnd(ZonedDateTime.of(LocalDateTime.of(2015, 11, 11, 10, 45), ZoneId.of("America/Los_Angeles")))
//                .withSequence(1)
//                .withRRule(new RecurrenceRule2()
//                        .withFrequency(new Weekly()
//                                .withByRules(new ByDay(DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY))));
//        
//        assertTrue(VEventImpl.isEqualTo(expectedVEvent, vEvent));  
//    }
//    
//    @Test
//    public void canEditVComponent2()
//    {
//        TestUtil.runThenWaitForPaintPulse( () -> agenda.vComponents().add(ICalendarStaticVEvents.getDaily1()));
//        VEventImpl v = (VEventImpl) agenda.vComponents().get(0);
//        
//        // Open edit popup
//        move("#hourLine11");
//        press(MouseButton.SECONDARY);
//        release(MouseButton.SECONDARY);
//        click("#repeatableTab");
//
//        // Get properties
//        CheckBox repeatableCheckBox = find("#repeatableCheckBox");
//        ComboBox<FrequencyType> frequencyComboBox = find("#frequencyComboBox");
//        RadioButton endNeverRadioButton = find("#endNeverRadioButton");
//        
//        // Check initial state
//        assertTrue(repeatableCheckBox.isSelected());
//        assertEquals(FrequencyType.DAILY, frequencyComboBox.getSelectionModel().getSelectedItem());
//        assertTrue(endNeverRadioButton.isSelected());
//        assertEquals((Integer) 1, v.getRRule().getFrequency().getInterval());
//        assertEquals((Integer) 0, v.getRRule().getCount());
//        
//        closeCurrentWindow();
//    }
//    
//    // create appointment made by drawing on Agenda
//    private void renderAppointmentByDragging()
//    {;
//        Assert.assertEquals(0, agenda.appointments().size());
//        move("#hourLine10");
//        press(MouseButton.PRIMARY);
//        move("#hourLine12");
//        release(MouseButton.PRIMARY);
//        
//        Assert.assertEquals("2015-11-11T10:00", agenda.appointments().get(0).getStartLocalDateTime().toString() );
//        Assert.assertEquals("2015-11-11T12:00", agenda.appointments().get(0).getEndLocalDateTime().toString() );
//        assertFind("#AppointmentRegularBodyPane2015-11-11/0"); // validate that the pane has the expected id
//    }
//        
//    @Test
//    public void canCreateAppointmentByDraggingAndMakeRepeatable()
//    {
//        renderAppointmentByDragging();
//        // Open edit popup
//        move("#hourLine11"); // open edit popup
//        press(MouseButton.SECONDARY);
//        release(MouseButton.SECONDARY);
//        
//        // make selection in new appointment popup
//        click("#editButton");
//        
//        // select repeat options (weekly by default)
//        click("#repeatableTab");
//        click("#repeatableCheckBox");
//        click("#fridayCheckBox");
//        click("#mondayCheckBox");
//        click("#saveRepeatButton");
//                
//        Assert.assertEquals(2, agenda.appointments().size());
//        VComponent<Appointment> v = agenda.vComponents().get(0);
//        String zone = ZoneId.systemDefault().toString();
//        String expectedString = "BEGIN:VEVENT" + System.lineSeparator()
//                + "CATEGORIES:group00" + System.lineSeparator()
//                + "CREATED:" + DateTimeUtilities.temporalToString(v.getDateTimeCreated()) + System.lineSeparator()
//                + "DTEND;TZID=" + zone + ":20151111T120000" + System.lineSeparator()
//                + "DTSTAMP:" + DateTimeUtilities.temporalToString(v.getDateTimeStamp()) + System.lineSeparator()
//                + "DTSTART;TZID=" + zone + ":20151111T100000" + System.lineSeparator()
//                + "RRULE:FREQ=WEEKLY;BYDAY=WE,FR,MO" + System.lineSeparator()
//                + "SEQUENCE:1" + System.lineSeparator()
//                + "SUMMARY:New" + System.lineSeparator()
//                + "UID:20151108T000000-0jfxtras.org" + System.lineSeparator()
//                + "END:VEVENT";
//        Assert.assertEquals(expectedString, v.toComponentText());
//        
//        List<LocalDateTime> dates = agenda.appointments()
//                .stream()
//                .map(a -> a.getStartLocalDateTime())
//                .collect(Collectors.toList());
//        
//        List<LocalDateTime> expectedDates = new ArrayList<LocalDateTime>(Arrays.asList(
//                LocalDateTime.of(2015, 11, 11, 10, 0)
//              , LocalDateTime.of(2015, 11, 13, 10, 0)
//                ));
//        Assert.assertEquals(expectedDates, dates);
//    }
//    
//    @Test
//    public void canCreateAppointmentByDraggingAndMakeRepeatable2()
//    {
//        renderAppointmentByDragging();
//        move("#hourLine11");
//        press(MouseButton.SECONDARY);
//        release(MouseButton.SECONDARY);
//        
//        // make selection in new appointment popup
//        click("#editButton");
//
//        // type value
//        click("#repeatableTab");
//        click("#repeatableCheckBox");
//        click("#frequencyComboBox");
//        click("Daily");
//        click("#intervalSpinner");
//        Spinner<Integer> interval = find("#intervalSpinner");
//        interval.getEditor().selectAll();
//        type("3");
//        click("#endAfterRadioButton");
//        click("#endAfterEventsSpinner");
//        Spinner<Integer> after = find("#endAfterEventsSpinner");
//        after.getEditor().selectAll();
//        type("6");
//        click("#saveRepeatButton");
//        Assert.assertEquals(2, agenda.appointments().size());
//        VComponent<Appointment> v = agenda.vComponents().get(0);
//        String zone = ZoneId.systemDefault().toString();
//        String expectedString = "BEGIN:VEVENT" + System.lineSeparator()
//                + "CATEGORIES:group00" + System.lineSeparator()
//                + "CREATED:" + DateTimeUtilities.temporalToString(v.getDateTimeCreated()) + System.lineSeparator()
//                + "DTEND;TZID=" + zone + ":20151111T120000" + System.lineSeparator()
//                + "DTSTAMP:" + DateTimeUtilities.temporalToString(v.getDateTimeStamp()) + System.lineSeparator()
//                + "DTSTART;TZID=" + zone + ":20151111T100000" + System.lineSeparator()
//                + "RRULE:FREQ=DAILY;INTERVAL=3;COUNT=6" + System.lineSeparator()
//                + "SEQUENCE:1" + System.lineSeparator()
//                + "SUMMARY:New" + System.lineSeparator()
//                + "UID:20151108T000000-0jfxtras.org" + System.lineSeparator()
//                + "END:VEVENT";
//        Assert.assertEquals(expectedString, v.toComponentText());
//        
//        List<LocalDateTime> dates = agenda.appointments()
//                .stream()
//                .map(a -> a.getStartLocalDateTime())
//                .collect(Collectors.toList());        
//        List<LocalDateTime> expectedDates = new ArrayList<LocalDateTime>(Arrays.asList(
//                LocalDateTime.of(2015, 11, 11, 10, 0)
//              , LocalDateTime.of(2015, 11, 14, 10, 0)
//                ));
//        Assert.assertEquals(expectedDates, dates);
//    }
}
