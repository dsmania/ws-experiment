package org.kilois.experiments.wsexperiment.server;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.enterprise.context.ApplicationScoped;
import javax.faces.context.FacesContext;
import javax.inject.Named;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.servlet.http.HttpSession;
import javax.validation.constraints.NotNull;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.format.DateTimeParseException;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Named("colorService")
@ApplicationScoped
@WebService(serviceName = "ws-experiment-ws", portName = "ws-experiment-ws-port", targetNamespace = "wsexperiment")
public class ColorService implements Serializable {

    private static final long serialVersionUID = 6085152634656601410L;

    private static final Map<java.awt.Color, String> COLOR_IDS = new HashMap<java.awt.Color, String>();
    static {
        COLOR_IDS.put(java.awt.Color.RED, "red");
        COLOR_IDS.put(java.awt.Color.GREEN, "green");
        COLOR_IDS.put(java.awt.Color.BLUE, "blue");
    }

    private static final List<ColorEntry> ENTRIES = new ArrayList<ColorEntry>();

    public ColorService() {
        super();
    }

    @WebMethod(exclude = true) // Not required by Java EE. Due to GlassFish bug
    public java.awt.Color submitColor(@NotNull String hexRgb) {
        String rgb = (hexRgb.startsWith("#")) ? hexRgb.substring(1) : hexRgb;
        java.awt.Color color = java.awt.Color.decode("0x" + rgb);
        ENTRIES.add(new ColorEntry((COLOR_IDS.containsKey(color)) ? COLOR_IDS.get(color): "#" + rgb, color,
                ((HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(false)).getId()));

        return color;
    }

    @WebMethod(exclude = true) // Not required by Java EE. Due to GlassFish bug
    public List<ColorEntry> getLastColors(String count) {
        return getLastColors(Integer.parseInt(count));
    }

    @WebMethod(exclude = true) // Not required by Java EE. Due to GlassFish bug
    public List<ColorEntry> getLastColors(int count) {
        List<ColorEntry> lastColors = new ArrayList<ColorEntry>();
        int entriesSize = ENTRIES.size();
        int minIndex = entriesSize - count;
        if (minIndex < 0) {
            minIndex = 0;
        }
        for (int i = entriesSize - 1; i >= minIndex; i--) {
            lastColors.add(ENTRIES.get(i));
        }

        return lastColors;
    }

    @WebMethod
    @WebResult(name = "resultsResponse")
    public ColorResponse getColorsByCount(@WebParam(name = "resultsRequest") ColorRequest request) {
        ColorResponse response = new ColorResponse();

        if (request == null) {
//          request = new ColorRequest();
//          request.setCount(3);
            response.setError("Request is null");
            return response;
        }

        Integer requestCount = request.getCount();
        if (requestCount == null) {
            response.setError("Count not specified");
            return response;
        }

        int entriesSize = ENTRIES.size();
        int minIndex = entriesSize - requestCount.intValue();
        if (minIndex < 0) {
            minIndex = 0;
        }
        List<Color> colors = new ArrayList<Color>();
        for (int i = entriesSize - 1; i >= minIndex; i--) {
            ColorEntry entry = ENTRIES.get(i);
            colors.add(new Color(entry.getId(), entry.getRgb()));
            response.setStartDateTime(entry.getDateTime());
            if (response.getEndDateTime() == null) {
                response.setEndDateTime(entry.getDateTime());
            }
        }
        response.setColor(colors);

        return response;
    }

    @WebMethod
    @WebResult(name = "resultsResponse")
    public ColorResponse getColorsByDateTime(@WebParam(name = "resultsRequest") ColorRequest request) {
        ColorResponse response = new ColorResponse();

        if (request == null) {
//          request = new ColorRequest();
//          request.setStartDateTime(ColorEntry.FORMATTER.format(LocalDateTime.now().minusMinutes(5)));
//          request.setEndDateTime(ColorEntry.FORMATTER.format(LocalDateTime.now()));
            response.setError("Request is null");
            return response;
        }

        String requestStartDateTime = request.getStartDateTime();
        if (requestStartDateTime == null) {
            response.setError("Start date time not specified");
            return response;
        }
        LocalDateTime startDateTime;
        try {
            startDateTime = LocalDateTime.parse(requestStartDateTime, ColorEntry.FORMATTER);
        } catch (DateTimeParseException e) {
            response.setError("Bad start date time format");
            return response;
        }
        String requestEndDateTime = request.getEndDateTime();
        if (requestEndDateTime == null) {
            response.setError("End date time not specified");
            return response;
        }
        LocalDateTime endDateTime;
        try {
            endDateTime = LocalDateTime.parse(requestEndDateTime, ColorEntry.FORMATTER);
        } catch (DateTimeParseException e) {
            response.setError("Bad end date time format");
            return response;
        }

        response.setStartDateTime(requestStartDateTime);
        response.setEndDateTime(requestEndDateTime);

        List<Color> colors = new ArrayList<Color>();
        for (ColorEntry entry : ENTRIES) {
            if (entry.getTimestamp().compareTo(startDateTime) < 0) {
                continue;
            }
            if (entry.getTimestamp().compareTo(endDateTime) > 0) {
                break;
            }

            colors.add(new Color(entry.getId(), entry.getRgb()));
        }
        response.setColor(colors);

        return response;
    }


    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ColorRequest implements Serializable  {

        private static final long serialVersionUID = 7543888201462751101L;

        private String startDateTime;

        private String endDateTime;

        private Integer count;

    }


    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @EqualsAndHashCode(of = { "startDateTime", "endDateTime" }, callSuper = false)
    public static class ColorResponse implements Serializable  {

        private static final long serialVersionUID = -1494087812886577950L;

        private String startDateTime;

        private String endDateTime;

        private String error;

        private List<Color> color; // Properly, it should be "colors". Changed to simplify transform by JAX-WS

    }

    @Data
    @AllArgsConstructor
    public static class Color implements Serializable {

        private static final long serialVersionUID = 4597065001678452182L;

        private String id;

        private String rgb;

    }

}
