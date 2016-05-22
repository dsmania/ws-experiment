package org.kilois.experiments.wsexperiment.desktopclient;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import javax.swing.ActionMap;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSpinner;
import javax.swing.JTextPane;
import javax.swing.SpinnerDateModel;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import org.jdesktop.application.Action;
import org.jdesktop.application.FrameView;
import org.jdesktop.application.SingleFrameApplication;
import org.jdesktop.swingx.JXDatePicker;
import org.jdesktop.swingx.JXErrorPane;
import org.jdesktop.swingx.JXPanel;
import org.kilois.experiments.Color;
import org.kilois.experiments.ColorException;
import org.kilois.experiments.Colors;
import org.kilois.experiments.ColorsWs;
import net.miginfocom.swing.MigLayout;

public class WsExperimentDesktopClient extends SingleFrameApplication {

    protected static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_OFFSET_DATE_TIME;
    protected static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    protected static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm:ss.SSS");

    private JTextPane entriesEditor;
    private JFormattedTextField countField;
    private JXDatePicker startDatePicker;
    private JSpinner startTimeSpinner;
    private JComboBox<ZoneOffset> startOffsetCombo;
    private JXDatePicker endDatePicker;
    private JSpinner endTimeSpinner;
    private JComboBox<ZoneOffset> endOffsetCombo;

    private static Colors COLOR_SERVICE;

    public WsExperimentDesktopClient() {
        super();
    }

    protected void initialize(String[] args) {
        super.initialize(args);

        COLOR_SERVICE = new ColorsWs().getColorsPort();
    }

    @Override
    protected void startup() {
        ActionMap actions = getContext().getActionMap();

        Date endDate = new Date();
        Date startDate = new Date(endDate.getTime() - 60000);

        SortedSet<ZoneOffset> offsetsSet = new TreeSet<ZoneOffset>();
        LocalDateTime someDateTime = LocalDateTime.now();
        for (String id : ZoneId.getAvailableZoneIds()) {
            offsetsSet.add(someDateTime.atZone(ZoneId.of(id)).getOffset());
        }
        ZoneOffset[] offsets = offsetsSet.toArray(new ZoneOffset[offsetsSet.size()]);
        ZoneOffset currentOffset = ZonedDateTime.now().getOffset();

        FrameView view = getMainView();

        JXPanel contentPane = new JXPanel(new MigLayout("fill, ins 0, gap 0", "[grow][grow][][]", "[grow][][][]"));

        this.entriesEditor = new JTextPane();
        this.entriesEditor.setEditable(false);
        JScrollPane entriesEditorScroll = new JScrollPane(this.entriesEditor);
        entriesEditorScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        contentPane.add(entriesEditorScroll, "span 4, grow, wrap");

        this.countField = new JFormattedTextField(NumberFormat.getIntegerInstance());
        this.countField.setValue(Integer.valueOf(3));
        contentPane.add(this.countField, "span 3, grow");
        contentPane.add(new JButton(actions.get("byCount")), "grow, wrap");

        contentPane.add(new JSeparator(JSeparator.HORIZONTAL), "span 4, grow, wrap");

        this.startDatePicker = new JXDatePicker();
        this.startDatePicker.setFormats(DATE_FORMAT);
        this.startDatePicker.setDate(startDate);
        contentPane.add(this.startDatePicker, "grow");
        this.startTimeSpinner = new JSpinner(new SpinnerDateModel());
        this.startTimeSpinner.setEditor(new JSpinner.DateEditor(startTimeSpinner, TIME_FORMAT.toPattern()));
        this.startTimeSpinner.setValue(startDate);
        contentPane.add(this.startTimeSpinner, "grow");
        this.startOffsetCombo = new JComboBox<ZoneOffset>(offsets);
        this.startOffsetCombo.setSelectedItem(currentOffset);
        contentPane.add(this.startOffsetCombo, "grow, wrap");

        this.endDatePicker = new JXDatePicker();
        this.endDatePicker.setFormats(DATE_FORMAT);
        this.endDatePicker.setDate(endDate);
        contentPane.add(this.endDatePicker, "grow");
        this.endTimeSpinner = new JSpinner(new SpinnerDateModel());
        this.endTimeSpinner.setEditor(new JSpinner.DateEditor(endTimeSpinner, TIME_FORMAT.toPattern()));
        this.endTimeSpinner.setValue(endDate);
        contentPane.add(this.endTimeSpinner, "grow");
        this.endOffsetCombo = new JComboBox<ZoneOffset>(offsets);
        this.endOffsetCombo.setSelectedItem(currentOffset);
        contentPane.add(this.endOffsetCombo, "grow");

        contentPane.add(new JButton(actions.get("byDates")), "cell 3 3, span 1 2, grow, wrap");

        view.setComponent(contentPane);
    }

    protected void ready() {
        super.ready();

        show(getMainView());
    }

    @Action
    public void byCount() {
        try {
            updateEntries(COLOR_SERVICE.getColorsByCount(((Integer) this.countField.getValue()).intValue()).getColor());
        } catch (BadLocationException | ColorException e) {
            JXErrorPane.showDialog(e);
        }
    }

    @Action
    public void byDates() {
        ZoneOffset startOffset = this.endOffsetCombo.getItemAt(this.startOffsetCombo.getSelectedIndex());
        OffsetDateTime start = OffsetDateTime.of(
                LocalDate.from(this.startDatePicker.getDate().toInstant().atOffset(startOffset)),
                LocalTime.from(((Date) this.startTimeSpinner.getValue()).toInstant().atOffset(startOffset)),
                startOffset);
        ZoneOffset endOffset = this.endOffsetCombo.getItemAt(this.endOffsetCombo.getSelectedIndex());
        OffsetDateTime end = OffsetDateTime.of(
                LocalDate.from(this.endDatePicker.getDate().toInstant().atOffset(endOffset)),
                LocalTime.from(((Date) this.endTimeSpinner.getValue()).toInstant().atOffset(endOffset)),
                endOffset);
        if (start.compareTo(end) > 0) {
            OffsetDateTime temp = start;
            start = end;
            end = temp;
        }
        try {
            updateEntries(COLOR_SERVICE.getColorsByDateTime(start.format(FORMATTER), end.format(FORMATTER)).getColor());
        } catch (BadLocationException | ColorException e) {
            JXErrorPane.showDialog(e);
        }
    }

    private void updateEntries(List<Color> colors) throws BadLocationException {
        Document entriesDocument = this.entriesEditor.getDocument();
        entriesDocument.remove(0, entriesDocument.getLength());
        int offset = 0;
        for (Color color : colors) {
            MutableAttributeSet attributes =  new SimpleAttributeSet();
            StyleConstants.setForeground(attributes, java.awt.Color.decode(color.getRgb()));
            StyleConstants.setBold(attributes, true);
            String text = color.getId() + "\n";
            entriesDocument.insertString(offset, text, attributes);
            offset += text.length();
        }
    }

    public static void main(String... args) {
        launch(WsExperimentDesktopClient.class, args);
    }

}
