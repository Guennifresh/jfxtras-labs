package jfxtras.labs.icalendarfx.parameters;

import java.net.URI;

import jfxtras.labs.icalendarfx.properties.component.relationship.Attendee;
import jfxtras.labs.icalendarfx.properties.component.relationship.Organizer;

/**
 * DIR
 * Directory Entry Reference
 * RFC 5545, 3.2.6, page 18
 * 
 * To specify reference to a directory entry associated with
 *     the calendar user specified by the property.
 * 
 * Example:
 * ORGANIZER;DIR="ldap://example.com:6666/o=ABC%20Industries,
 *  c=US???(cn=Jim%20Dolittle)":mailto:jimdo@example.com
 * 
 * @author David Bal
 * @see Attendee
 * @see Organizer
 *
 */
public class DirectoryEntryReference extends ParameterURI<DirectoryEntryReference>
{
    public DirectoryEntryReference(URI uri)
    {
        super(uri);
    }
    
    public DirectoryEntryReference()
    {
        super();
    }

    public DirectoryEntryReference(DirectoryEntryReference source)
    {
        super(source);
    }

    public static DirectoryEntryReference parse(String content)
    {
        DirectoryEntryReference parameter = new DirectoryEntryReference();
        parameter.parseContent(content);
        return parameter;
    }
}
