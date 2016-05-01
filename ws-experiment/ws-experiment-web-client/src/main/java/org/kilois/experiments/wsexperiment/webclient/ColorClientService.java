package org.kilois.experiments.wsexperiment.webclient;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;
import javax.validation.constraints.Pattern;
import experiments.kilois.org.Color;
import experiments.kilois.org.ColorService;
import experiments.kilois.org.ColorsWs;
import experiments.kilois.org.Exception_Exception;
import lombok.Getter;
import lombok.Setter;

@Named("colorClientService")
@ApplicationScoped
public class ColorClientService implements Serializable {

    private static final long serialVersionUID = -2364624475416285960L;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

    @Getter
    @Setter
    private int colorCount = 3;

    @Getter
    @Setter
    @Pattern(regexp = "\\d\\d\\d\\d-\\d\\d-\\d\\dT\\d\\d:\\d\\d:\\d\\d\\.\\d\\d\\d\\+\\d\\d:\\d\\d")
    private String startDateTime = FORMATTER.format(OffsetDateTime.now().minusMinutes(1));

    @Getter
    @Setter
    @Pattern(regexp = "\\d\\d\\d\\d-\\d\\d-\\d\\dT\\d\\d:\\d\\d:\\d\\d\\.\\d\\d\\d\\+\\d\\d:\\d\\d")
    private String endDateTime = FORMATTER.format(OffsetDateTime.now());

    @Getter
    private final List<Color> colors = new ArrayList<Color>();

    private static ColorService COLOR_SERVICE = new ColorsWs().getColorsWsPort();

    public ColorClientService() {
        super();
    }

    public String readColors() {
        this.colors.clear();
        try {
            this.colors.addAll(COLOR_SERVICE.getColorsByCount(this.colorCount).getColor());
        } catch (Exception_Exception e) {
            e.printStackTrace();
        }

        return "index";
    }

    public String readColorsByDate() {
        this.colors.clear();
        try {
            this.colors.addAll(COLOR_SERVICE.getColorsByDateTime(startDateTime, endDateTime).getColor());
        } catch (Exception_Exception e) {
            e.printStackTrace();
        }

        return "index";
    }

}
