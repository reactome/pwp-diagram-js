package org.reactome.web.client.handlers;

import org.timepedia.exporter.client.ExportClosure;
import org.timepedia.exporter.client.Exportable;

/**
 * @author Antonio Fabregat (fabregat@ebi.ac.uk)
 */
@ExportClosure
public interface JsDiagramLoadedHandler extends Exportable {
    void loaded(String identifier);
}
