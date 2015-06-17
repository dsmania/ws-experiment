package org.kilois.experiments.wsexperiment.server;

import java.awt.Color;

import javax.validation.constraints.NotNull;

import org.threeten.bp.LocalDateTime;
import org.threeten.bp.format.DateTimeFormatter;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ColorEntry implements Comparable<ColorEntry> {

	protected static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_DATE_TIME;

	@NonNull
	private String id;

	@NonNull
	private Color color;

	@NonNull
	private String session;

	@NonNull
	private LocalDateTime timestamp;

	public ColorEntry(@NotNull String id, @NotNull Color color, @NotNull String session) {
		this(id, color, session, LocalDateTime.now());
	}

	public String getRgb() {
		return "#" + Integer.toHexString(this.color.getRGB()).substring(2);
	}

	public void setRgb(String rgb) {
		this.color = Color.decode((rgb.startsWith("#")) ? rgb.substring(1) : rgb);
	}

	public String getDateTime() {
		return FORMATTER.format(this.timestamp);
	}

	public void setDateTime(String dateTime) {
		this.timestamp = LocalDateTime.parse(dateTime, FORMATTER);
	}

	@Override
	public int compareTo(ColorEntry that) {
		return this.timestamp.compareTo(that.timestamp);
	}

}
