package jfxtras.labs.icalendaragenda.internal.scene.control.skin.agenda.base24hour.editors;

import java.time.temporal.Temporal;
import java.util.List;

import jfxtras.labs.icalendarfx.components.DaylightSavingTime;
import jfxtras.labs.icalendarfx.components.StandardTime;
import jfxtras.labs.icalendarfx.components.VAlarm;
import jfxtras.labs.icalendarfx.components.VComponent;
import jfxtras.labs.icalendarfx.components.VEvent;
import jfxtras.labs.icalendarfx.components.VFreeBusy;
import jfxtras.labs.icalendarfx.components.VJournal;
import jfxtras.labs.icalendarfx.components.VTimeZone;
import jfxtras.labs.icalendarfx.components.VTodo;

/**
 * Simple factory to create {@link EditDisplayableScene} objects.  Contains two methods to create scenes.
 * One takes only a VComponent as a parameter and builds an empty {@link EditDisplayableScene}.
 * The second takes a VComponent and an array of parameters required to completely
 * initialize the {@link EditDisplayableScene}.
 * 
 * @author David Bal
 *
 */
public class SimpleEditSceneFactory
{
    /**
     * Create a Stage to edit the type of VComponent passed as a parameter
     * <p>
     * Parameters array must contain the following:<br>
     * (0) VCalendar - parent VCalendar<br>
     * (1) Temporal startRecurrence - start of selected recurrence<br>
     * (2) Temporal endRecurrence - end of selected recurrence<br>
     * (3) String List categories - available category names<br>
     * 
     * @param vComponent - VComponent to be edited
     * @param params - necessary parameters, packed in an array, to edit the VComponent
     * @return
     */
    public static EditDisplayableScene newScene (VComponent vComponent, Object[] params)
    {
        // params[0] is VCalendar, handled below
        Temporal startRecurrence = (Temporal) params[1];       // startRecurrence - start of selected recurrence
        Temporal endRecurrence = (Temporal) params[2];         // endRecurrence - end of selected recurrence
        List<String> categories = (List<String>) params[3];    // categories - available category names

        if (vComponent instanceof VEvent)
        {
            return new EditVEventScene()
                    .setupData(
                        (VEvent) vComponent,                        // vComponent - component to edit
//                        ((VCalendar) params[0]).getVEvents(),       // vComponents - collection of components that vComponent is a member
                        startRecurrence,                            // startRecurrence - start of selected recurrence
                        endRecurrence,                              // endRecurrence - end of selected recurrence
                        categories                                  // categories - available category names
                    );
        } else if (vComponent instanceof VTodo)
        {
            return new EditVTodoScene()
                    .setupData(
                        (VTodo) vComponent,                        // vComponent - component to edit
//                        ((VCalendar) params[0]).getVTodos(),       // vComponents - collection of components that vComponent is a member
                        startRecurrence,                           // startRecurrence - start of selected recurrence
                        endRecurrence,                             // endRecurrence - end of selected recurrence
                        categories                                 // categories - available category names
                    );
           
        } else if (vComponent instanceof VJournal)
        {
            return new EditVJournalScene()
                    .setupData(
                        (VJournal) vComponent,                      // vComponent - component to edit
//                        ((VCalendar) params[0]).getVJournals(),     // vComponents - collection of components that vComponent is a member
                        startRecurrence,                            // startRecurrence - start of selected recurrence
                        endRecurrence,                              // endRecurrence - end of selected recurrence
                        categories                                  // categories - available category names
                    );
        } else if (vComponent instanceof VFreeBusy)
        {
            throw new RuntimeException("not implemented");
        } else if (vComponent instanceof VTimeZone)
        {
            throw new RuntimeException("not implemented");
        } else if (vComponent instanceof VAlarm)
        {
            throw new RuntimeException("not implemented");
        } else if (vComponent instanceof StandardTime)
        {
            throw new RuntimeException("not implemented");
        } else if (vComponent instanceof DaylightSavingTime)
        {
            throw new RuntimeException("not implemented");          
        } else
        {
            throw new RuntimeException("Unsupported VComponent type" + vComponent.getClass());
        }
    }
    
    /** Create an empty {@link EditDisplayableScene} */
    public static EditDisplayableScene newScene (VComponent vComponent)
    {
        if (vComponent instanceof VEvent)
        {
            return new EditVEventScene();
        } else if (vComponent instanceof VTodo)
        {
            return new EditVTodoScene();
        } else if (vComponent instanceof VJournal)
        {
            return new EditVJournalScene();
        } else if (vComponent instanceof VFreeBusy)
        {
            throw new RuntimeException("not implemented");
        } else if (vComponent instanceof VTimeZone)
        {
            throw new RuntimeException("not implemented");
        } else if (vComponent instanceof VAlarm)
        {
            throw new RuntimeException("not implemented");
        } else if (vComponent instanceof StandardTime)
        {
            throw new RuntimeException("not implemented");
        } else if (vComponent instanceof DaylightSavingTime)
        {
            throw new RuntimeException("not implemented");          
        } else
        {
            throw new RuntimeException("Unsupported VComponent type" + vComponent.getClass());
        }
    }
}
