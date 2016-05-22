package org.kilois.experiments.wsexperiment.webclient;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;
import javax.validation.constraints.Pattern;
import javax.xml.ws.WebServiceRef;
import org.kilois.experiments.Color;
import org.kilois.experiments.ColorException;
import org.kilois.experiments.Colors;
import org.kilois.experiments.ColorsWs;
import lombok.Getter;
import lombok.Setter;

@Named("colorsClient")
@ApplicationScoped
public class ColorClientService implements Serializable {

    private static final long serialVersionUID = 3656197095359459158L;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_TIME;

    @Getter
    @Setter
    private int colorCount = 3;

    @Getter
    @Setter
    @Pattern(regexp = "\\d\\d\\d\\d-\\d\\d-\\d\\d")
    private String startDate = DATE_FORMATTER.format(OffsetDateTime.now().minusMinutes(1));

    @Getter
    @Setter
    @Pattern(regexp = "\\d\\d:\\d\\d:\\d\\d[\\.\\d+]?")
    private String startTime = TIME_FORMATTER.format(OffsetDateTime.now().minusMinutes(1));

    @Getter
    @Setter
    private String startOffset = ZonedDateTime.now().minusMinutes(1).getOffset().getId();

    @Getter
    @Setter
    @Pattern(regexp = "\\d\\d\\d\\d-\\d\\d-\\d\\d")
    private String endDate = DATE_FORMATTER.format(OffsetDateTime.now());

    @Getter
    @Setter
    @Pattern(regexp = "\\d\\d:\\d\\d:\\d\\d[\\.\\d+]?")
    private String endTime = TIME_FORMATTER.format(OffsetDateTime.now());

    @Getter
    @Setter
    private String endOffset = OffsetDateTime.now().getOffset().getId();

    @Getter
    private final List<Color> colors = new ArrayList<Color>();

    @Getter
    private final List<String> offsets;

    @WebServiceRef(ColorsWs.class)
    private Colors colorsService;

    public ColorClientService() {
        super();

        SortedSet<ZoneOffset> offsetsSet = new TreeSet<ZoneOffset>();
        LocalDateTime someDateTime = LocalDateTime.now();
        for (String id : ZoneId.getAvailableZoneIds()) {
            offsetsSet.add(someDateTime.atZone(ZoneId.of(id)).getOffset());
        }
        List<String> offsetsList = new ArrayList<String>();
        for (ZoneOffset offset : offsetsSet) {
            offsetsList.add(offset.getId());
        }
        this.offsets = Collections.unmodifiableList(offsetsList);
    }

    public String readColors() {
        this.colors.clear();
        try {
            this.colors.addAll(this.colorsService.getColorsByCount(this.colorCount).getColor());
        } catch (ColorException e) {
            e.printStackTrace();
        }

        return "index";
    }

    public String readColorsByDate() {
        this.colors.clear();
        try {
            this.colors.addAll(this.colorsService.getColorsByDateTime(this.startDate + "T" + this.startTime
                    + this.startOffset, this.endDate + "T" + this.endTime + this.endOffset).getColor());
        } catch (ColorException e) {
            e.printStackTrace();
        }

        return "index";
    }

}
