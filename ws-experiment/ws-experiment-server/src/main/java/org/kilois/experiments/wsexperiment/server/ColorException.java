package org.kilois.experiments.wsexperiment.server;

import javax.xml.ws.WebFault;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@WebFault(name = "color-fault", targetNamespace = "${wsNamespace}")
public class ColorException extends Exception {

    private static final long serialVersionUID = -7352173377005833593L;

    @Getter
    @Setter
    private ColorFault faultInfo;

    public ColorException(String message, ColorFault faultInfo, Throwable cause) {
        super(message, cause);

        this.faultInfo = faultInfo;
    }

    public ColorException(String message, ColorFault faultInfo) {
        super(message);

        this.faultInfo = faultInfo;
    }

    public ColorException(String message, Throwable cause) {
        super(message, cause);

        this.faultInfo = new ColorFault(message);
    }

    public ColorException(ColorFault faultInfo, Throwable cause) {
        super(cause);

        this.faultInfo = faultInfo;
    }

    public ColorException(String message) {
        super(message);

        this.faultInfo = new ColorFault(message);
    }

    public ColorException(ColorFault faultInfo) {
        super();

        this.faultInfo = faultInfo;
    }

    public ColorException(Throwable cause) {
        super(cause);

        this.faultInfo = new ColorFault(cause.getMessage());
    }

    public ColorException() {
        super();

        this.faultInfo = new ColorFault();
    }


    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ColorFault {

        private String message;

    }

}
