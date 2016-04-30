package org.kilois.experiments.wsexperiment.server;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
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
import javax.xml.bind.annotation.XmlElement;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Named("colorService")
@ApplicationScoped
@WebService(serviceName = "colors-ws", portName = "colors-ws-port", targetNamespace = "org.kilois.experiments")
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

    @WebMethod(exclude = true)
    public java.awt.Color submitColor(@NotNull String hexRgb) {
        String rgb = (hexRgb.startsWith("#")) ? hexRgb.substring(1) : hexRgb;
        java.awt.Color color = java.awt.Color.decode("0x" + rgb);
        ENTRIES.add(new ColorEntry((COLOR_IDS.containsKey(color)) ? COLOR_IDS.get(color): "#" + rgb, color,
                ((HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(false)).getId()));

        return color;
    }

    @WebMethod(exclude = true)
    public List<ColorEntry> getLastColors(String count) {
        return getLastColors(Integer.parseInt(count));
    }

    @WebMethod(exclude = true)
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
    @WebResult(name = "colors")
    public Colors getColorsByCount(int count) {
        Colors response = new Colors();

        int entriesSize = ENTRIES.size();
        int minIndex = entriesSize - count;
        if (minIndex < 0) {
            minIndex = 0;
        }
        List<Color> colors = new ArrayList<Color>();
        for (int i = entriesSize - 1; i >= minIndex; i--) {
            ColorEntry entry = ENTRIES.get(i);
            colors.add(new Color(entry.getId(), entry.getRgb()));
            response.setStart(entry.getDateTime());
            if (response.getEnd() == null) {
                response.setEnd(entry.getDateTime());
            }
        }
        response.setColors(colors);

        return response;
    }

    @WebMethod
    @WebResult(name = "colors")
    public Colors getColorsByDateTime(@WebParam(name = "start") String start, @WebParam(name = "end") String end) {
        Colors response = new Colors();

        if (start == null) {
            response.setError("Start date-time not specified");
            return response;
        }
        LocalDateTime startDateTime;
        try {
            startDateTime = LocalDateTime.parse(start, ColorEntry.FORMATTER);
        } catch (DateTimeParseException e) {
            response.setError("Bad start date-time format");
            return response;
        }
        if (end == null) {
            response.setError("End date-time not specified");
            return response;
        }
        LocalDateTime endDateTime;
        try {
            endDateTime = LocalDateTime.parse(end, ColorEntry.FORMATTER);
        } catch (DateTimeParseException e) {
            response.setError("Bad end date-time format");
            return response;
        }

        response.setStart(start);
        response.setEnd(end);

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
        response.setColors(colors);

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
    @EqualsAndHashCode(of = { "start", "end" }, callSuper = false)
    public static class Colors implements Serializable {

        private static final long serialVersionUID = -1494087812886577950L;

        private String start;

        private String end;

        private String error;

        //@Getter(onMethod = @__({@XmlElement(name = "color")})) // Not working
        private List<Color> colors;

        @XmlElement(name = "color")
        public List<Color> getColors() {
            return this.colors;
        }

    }

    @Data
    @AllArgsConstructor
    public static class Color implements Serializable {

        private static final long serialVersionUID = 4597065001678452182L;

        private String id;

        private String rgb;

    }

}
