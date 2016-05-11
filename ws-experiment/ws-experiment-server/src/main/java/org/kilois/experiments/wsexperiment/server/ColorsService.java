package org.kilois.experiments.wsexperiment.server;

import java.io.Serializable;
import java.time.OffsetDateTime;
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
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Named("colorsService")
@ApplicationScoped
@WebService(serviceName = "colors-ws", portName = "colors-ws-port", targetNamespace = "http://kilois.org/experiments")
public class ColorsService implements Serializable {

    private static final long serialVersionUID = 6085152634656601410L;

    private static final Map<java.awt.Color, String> COLOR_IDS = new HashMap<java.awt.Color, String>();
    static {
        COLOR_IDS.put(java.awt.Color.RED, "red");
        COLOR_IDS.put(java.awt.Color.GREEN, "green");
        COLOR_IDS.put(java.awt.Color.BLUE, "blue");
    }

    private static final List<ColorEntry> ENTRIES = new ArrayList<ColorEntry>();

    public ColorsService() {
        super();
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

    @WebMethod(exclude = true)
    public java.awt.Color submitColor(@NotNull String hexRgb) {
        String rgb = (hexRgb.startsWith("#")) ? hexRgb.substring(1) : hexRgb;
        java.awt.Color color = java.awt.Color.decode("0x" + rgb);
        ENTRIES.add(new ColorEntry((COLOR_IDS.containsKey(color)) ? COLOR_IDS.get(color): "#" + rgb, color,
                ((HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(false)).getId()));

        return color;
    }

    @WebMethod
    public @WebResult(name = "colors") ColorResponse getColorsByCount(
            @WebParam(name = "count") int count)
            throws Exception {
        ColorResponse response = new ColorResponse();

        int entriesSize = ENTRIES.size();
        int minIndex = entriesSize - count;
        if (minIndex < 0) {
            minIndex = 0;
        }
        List<Color> colors = new ArrayList<Color>();
        response.setColors(colors);
        for (int i = entriesSize - 1; i >= minIndex; i--) {
            ColorEntry entry = ENTRIES.get(i);
            colors.add(new Color(entry.getId(), entry.getRgb()));
            response.setStart(OffsetDateTime.parse(entry.getDateTime(), ColorEntry.FORMATTER));
            if (response.getEnd() == null) {
                response.setEnd(OffsetDateTime.parse(entry.getDateTime(), ColorEntry.FORMATTER));
            }
        }

        return response;
    }

    @WebMethod
    public @WebResult(name = "colors") ColorResponse getColorsByDateTime(
            @WebParam(name = "start") @XmlJavaTypeAdapter(OffsetDateTimeStringAdapter.class) @NotNull OffsetDateTime start,
            @WebParam(name = "end") @XmlJavaTypeAdapter(OffsetDateTimeStringAdapter.class) @NotNull OffsetDateTime end)
                    throws Exception {
        ColorResponse response = new ColorResponse();

        response.setStart(start);
        response.setEnd(end);

        List<Color> colors = new ArrayList<Color>();
        for (ColorEntry entry : ENTRIES) {
            if (entry.getTimestamp().compareTo(start) < 0) {
                continue;
            }
            if (entry.getTimestamp().compareTo(end) > 0) {
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
    public static class ColorResponse implements Serializable {

        private static final long serialVersionUID = -1494087812886577950L;

        //@Getter(onMethod = @__({@XmlAttribute})) // Not working
        private OffsetDateTime start;

        //@Getter(onMethod = @__({@XmlAttribute})) // Not working
        private OffsetDateTime end;

        //@Getter(onMethod = @__({@XmlElement(name = "color")})) // Not working
        private List<Color> colors;

        @XmlAttribute
        @XmlJavaTypeAdapter(OffsetDateTimeStringAdapter.class)
        public OffsetDateTime getStart() {
            return this.start;
        }

        @XmlAttribute
        @XmlJavaTypeAdapter(OffsetDateTimeStringAdapter.class)
        public OffsetDateTime getEnd() {
            return this.end;
        }

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


    protected static class OffsetDateTimeStringAdapter extends XmlAdapter<String, OffsetDateTime> {

        @Override
        public OffsetDateTime unmarshal(String text) throws Exception {
            if (text == null) {
                return null;
            }
            return OffsetDateTime.parse(text, ColorEntry.FORMATTER);
        }

        @Override
        public String marshal(OffsetDateTime dateTime) throws Exception {
            if (dateTime == null) {
                return null;
            }
            return dateTime.format(ColorEntry.FORMATTER);
        }

    }

}
