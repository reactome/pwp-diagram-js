package org.reactome.web.client;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.ui.HTMLPanel;
import org.reactome.web.analysis.client.AnalysisClient;
import org.reactome.web.analysis.client.filter.ResultFilter;
import org.reactome.web.client.handlers.*;
import org.reactome.web.client.model.DiagramObject;
import org.reactome.web.client.model.JsProperties;
import org.reactome.web.client.model.JsSearchArguments;
import org.reactome.web.diagram.client.DiagramFactory;
import org.reactome.web.diagram.client.DiagramViewer;
import org.reactome.web.diagram.client.OptionalWidget;
import org.reactome.web.diagram.data.graph.model.GraphObject;
import org.reactome.web.pwp.model.client.content.ContentClient;
import org.timepedia.exporter.client.Export;
import org.timepedia.exporter.client.ExportPackage;
import org.timepedia.exporter.client.Exportable;

import java.util.Collections;
import java.util.List;


/**
 * @author Antonio Fabregat (fabregat@ebi.ac.uk)
 */
@SuppressWarnings({"unused", "WeakerAccess"})
@ExportPackage("Reactome")
@Export("Diagram")
public class Diagram implements Exportable {

    private static final String SERVER = "https://reactome.org";

    private static Diagram viewer;

    private static DiagramViewer diagram;
    private static DiagramLoader loader;

    private boolean diagramLoaded = false;
    private JsDiagramLoadedHandler loadedHandler;
    String analysisToken;
    ResultFilter analysisResultFilter;

    private Diagram() {
        diagram.addAnalysisResetHandler(event -> {
            analysisToken = null;
            analysisResultFilter = null;
        });
        diagram.addDiagramLoadedHandler(event -> {
            if (event.getContext() != null) {
                diagramLoaded = true;
                if (analysisToken != null && analysisResultFilter != null) {
                    diagram.setAnalysisToken(analysisToken, analysisResultFilter);
                }
            }
        });
    }

    public static Diagram create(JavaScriptObject input) {
        JsProperties jsProp = new JsProperties(input);
        return create(
                jsProp.get("placeHolder"),
                jsProp.get("proxyPrefix", SERVER),
                jsProp.getInt("width", 500),
                jsProp.getInt("height", 400),
                jsProp.getArray("toHide")
        );
    }

    public static Diagram create(String placeHolder, int width, int height) {
        return create(placeHolder, SERVER, width, height, Collections.emptyList());
    }

    public static Diagram create(String placeHolder, String server, final int width, final int height, List<String> toHide) {
        final Element element = Document.get().getElementById(placeHolder);
        if (element == null)
            throw new RuntimeException("Reactome diagram cannot be initialised. Please provide a valid 'placeHolder' (\"" + placeHolder + "\" invalid place holder).");

        if (viewer == null) {
            OptionalWidget.FIREWORKS.setVisible(false);
            toHide.forEach((id) -> OptionalWidget.findById(id).setVisible(false));

            ContentClient.SERVER = server;
            AnalysisClient.SERVER = server;
            DiagramFactory.SERVER = server;
            DiagramFactory.WIDGET_JS = true; // DO NOT CHANGE TO FALSE.
            DiagramFactory.ILLUSTRATION_SERVER = SERVER;
            DiagramFactory.RESPOND_TO_SEARCH_SHORTCUT = false;
            DiagramFactory.SCROLL_SENSITIVITY = 500;
            diagram = DiagramFactory.createDiagramViewer();
            diagram.asWidget().getElement().getStyle().setProperty("height", "inherit");

            loader = new DiagramLoader(diagram);
            viewer = new Diagram();
        } else {
            viewer.detach();
        }

        HTMLPanel container = HTMLPanel.wrap(element);
        container.clear();
        container.add(diagram);
        Scheduler.get().scheduleDeferred(() -> viewer.resize(width, height));

        return viewer;
    }

    public void detach() {
        if (diagram.asWidget().isAttached()) {
            diagram.asWidget().removeFromParent();
        }
    }

    @Deprecated
    public void flagItems(String term) {
        if (diagramLoaded) diagram.flagItems(term, false);
    }

    public void flagItems(String term, boolean includeInteractors) {
        if (diagramLoaded) diagram.flagItems(term, includeInteractors);
    }

    public void highlightItem(String stableIdentifier) {
        if (diagramLoaded) diagram.highlightItem(stableIdentifier);
    }

    public void highlightItem(long dbIdentifier) {
        if (diagramLoaded) diagram.highlightItem(dbIdentifier);
    }

    public void loadDiagram(String stId) {
        diagramLoaded = false;
        loader.load(stId);
    }

    public void onSearchPerformed(final JsSearchPerformedHandler handler) {
        diagram.addSearchPerformedHandler(event -> Scheduler.get().scheduleDeferred(() -> handler.onSearchPerformed(JsSearchArguments.create(event.getSearchArguments()))));
    }

    public void onAnalysisReset(final JsAnalysisResetHandler handler) {
        diagram.addAnalysisResetHandler(event -> Scheduler.get().scheduleDeferred(handler::analysisReset));
    }

    public void onCanvasNotSupported(final JsCanvasNotSupported handler) {
        diagram.addCanvasNotSupportedEventHandler(event -> Scheduler.get().scheduleDeferred(handler::canvasNotSupported));
    }

    public void onDiagramLoaded(final JsDiagramLoadedHandler handler) {
        loader.addSubpathwaySelectedHandler(handler::loaded);
        diagram.addDiagramLoadedHandler(event -> {
            if (loader.getTarget() == null) {
                Scheduler.get().scheduleDeferred(() -> handler.loaded(event.getContext().getContent().getStableId()));
            }
        });
    }

    public void onFlagsReset(final JsFlagsResetHandler handler) {
        diagram.addDiagramObjectsFlagResetHandler(event -> Scheduler.get().scheduleDeferred(handler::flagsReset));
    }

    public void onObjectSelected(final JsGraphObjectSelectedHandler handler) {
        diagram.addDatabaseObjectSelectedHandler(event -> {
            GraphObject object = event.getGraphObject();
            Scheduler.get().scheduleDeferred(() -> handler.selected(object == null ? null : DiagramObject.create(object)));
        });
    }

    public void onObjectHovered(final JsGraphObjectHoveredHandler handler) {
        diagram.addDatabaseObjectHoveredHandler(event -> {
            GraphObject object = event.getGraphObject();
            Scheduler.get().scheduleDeferred(() -> handler.hovered(object == null ? null : DiagramObject.create(object)));
        });
    }

    public void resetAnalysis() {
        analysisToken = null;
        analysisResultFilter = null;
        if (diagramLoaded) diagram.resetAnalysis();
    }

    public void resetFlaggedItems() {
        if (diagramLoaded) diagram.resetFlaggedItems();
    }

    public void resetHighlight() {
        if (diagramLoaded) diagram.resetHighlight();
    }

    public void resetSelection() {
        if (diagramLoaded) diagram.resetSelection();
    }

    public void resize(int width, int height) {
        diagram.asWidget().setWidth(width + "px");
        diagram.asWidget().setHeight(height + "px");
        diagram.onResize();
    }


    public void selectItem(String stableIdentifier) {
        if (diagramLoaded) diagram.selectItem(stableIdentifier);
    }


    public void selectItem(long dbIdentifier) {
        if (diagramLoaded) diagram.selectItem(dbIdentifier);
    }

    public void setAnalysisToken(String token, JavaScriptObject filter) {
        JsProperties filterProp = new JsProperties(filter);
        ResultFilter resultFilter = new ResultFilter(
                filterProp.get("resource"),
                Double.valueOf(filterProp.get("pValue", "1")),
                Boolean.parseBoolean(filterProp.get("includeDisease", "true")),
                filterProp.getInt("min"),
                filterProp.getInt("max"),
                filterProp.getArray("speciesList")
        );

        if (diagramLoaded) diagram.setAnalysisToken(token, resultFilter);

        analysisToken = token;
        analysisResultFilter = resultFilter;
    }

    private static native void _error(String message)/*-{
        if ($wnd.console) {
            $wnd.console.error(message);
        }
    }-*/;
}
