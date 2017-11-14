package com.trustev;

import com.jarektoro.responsivelayout.ResponsiveColumn;
import com.jarektoro.responsivelayout.ResponsiveLayout;
import com.jarektoro.responsivelayout.ResponsiveRow;
import com.vaadin.navigator.View;
import com.vaadin.server.Page;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class PublisherLayout extends VerticalSplitPanel implements View {
    private static KafkaProducer kafka;
    private static PropertyHelper props;
    private List<String> kafkaTopics, kafkaBrokers, docTypes;
    private boolean resourcesMade, layoutBuilt, layoutConfigured;

    private ThemeResource themeResource;
    private Image image;

    private ComboBox cbTopic, cbDocument;
    private String message;
    private TextArea taDoc;
    private TextField tfBrokers, tfCount;
    private Button btSetBrokers, btPublish;
    private Panel panel;
    private VerticalLayout topLeftFirstChild, bottomLeftFirstChild;
    private ResponsiveLayout responsiveLayout;
    private ResponsiveRow rowTop, rowBottom;

    public PublisherLayout() {

        //*********************************************************************************
        //*********************          MAKE RESOURCES         ***************************
        //*********************************************************************************
        resourcesMade = false;
        layoutBuilt = false;
        layoutConfigured = false;
        makeResources();


        //*********************************************************************************
        //*********************            BUILD PAGE           ***************************
        //*********************************************************************************
        buildLayoutResponsive();


        //*********************************************************************************
        //*********************      SET VALUES & SETTINGS      ***************************
        //*********************************************************************************
        configureLayoutResponsive();

    }

    private void buildLayoutResponsive() {
        responsiveLayout = new ResponsiveLayout();

        if (!resourcesMade || (kafkaTopics == null || docTypes == null)) {
            makeResources();
        }

        cbTopic = new ComboBox("Choose Kafka Topic:", kafkaTopics);
        cbDocument = new ComboBox("Use preset Document:", docTypes);

        rowTop = createNestedRowTop(responsiveLayout);

        tfBrokers = new TextField("Set Kafka Brokers:");
        btSetBrokers = new Button("Set Brokers");
        rowBottom = createNestedRowBottom(responsiveLayout);

        tfCount = new TextField("Message count:");
        topLeftFirstChild = new VerticalLayout(image, responsiveLayout, tfCount);

        panel = new Panel(topLeftFirstChild);

        taDoc = new TextArea();
        btPublish = new Button("Publish Doc", clickEvent -> onButtonClick());
        bottomLeftFirstChild = new VerticalLayout(taDoc, btPublish);
        this.addComponents(panel, bottomLeftFirstChild);

        layoutBuilt = true;
    }

    public ResponsiveRow createNestedRowTop(ResponsiveLayout layout) {
        ResponsiveRow nestedRow = layout.addRow().withAlignment(Alignment.MIDDLE_CENTER);
        nestedRow.setSizeFull();

        nestedRow.addColumn().withDisplayRules(12, 12, 6, 6).withComponent(createInnerNestedLayout(cbTopic, false, true)).withGrow(true).setAlignment(ResponsiveColumn.ColumnComponentAlignment.CENTER);
        nestedRow.addColumn().withDisplayRules(12, 12, 6, 6).withComponent(createInnerNestedLayout(cbDocument, true, true)).withGrow(true).setAlignment(ResponsiveColumn.ColumnComponentAlignment.CENTER);
        nestedRow.setDefaultComponentAlignment(Alignment.MIDDLE_CENTER);
        nestedRow.setSpacing(true);

        return nestedRow;
    }

    public ResponsiveRow createNestedRowBottom(ResponsiveLayout layout) {
        ResponsiveRow nestedRow = layout.addRow().withAlignment(Alignment.MIDDLE_CENTER);
        nestedRow.setSizeFull();

        nestedRow.addColumn().withDisplayRules(12, 12, 6, 6).withComponent(createInnerNestedLayout(tfBrokers, false, false)).withGrow(true).setAlignment(ResponsiveColumn.ColumnComponentAlignment.CENTER);
        nestedRow.addColumn().withDisplayRules(12, 12, 6, 6).withComponent(createInnerNestedLayout(btSetBrokers, true, false)).withGrow(true).setAlignment(ResponsiveColumn.ColumnComponentAlignment.CENTER);
        nestedRow.setSizeFull();
        nestedRow.setDefaultComponentAlignment(Alignment.MIDDLE_CENTER);
        nestedRow.setSpacing(true);

        return nestedRow;
    }

    public ResponsiveLayout createInnerNestedLayout(Component c, boolean isFirst, boolean isTop) {
        ResponsiveLayout nestedLayout = new ResponsiveLayout();
        nestedLayout.setSizeFull();
        ResponsiveRow nestedLayoutRow = nestedLayout.addRow().withAlignment(Alignment.MIDDLE_CENTER);
        nestedLayoutRow.setSizeFull();

        if (isFirst) {
            nestedLayoutRow.addColumn().withDisplayRules(12, 12, 6, 4).withComponent(c).withGrow(true);
            nestedLayoutRow.addColumn().withDisplayRules(0, 0, 6, 4).withGrow(true);
            nestedLayoutRow.addColumn().withDisplayRules(0, 0, 0, 4).withGrow(true);
        } else {
            nestedLayoutRow.addColumn().withDisplayRules(0, 0, 0, 4).withGrow(true);
            nestedLayoutRow.addColumn().withDisplayRules(0, 0, 6, 4).withGrow(true);
            nestedLayoutRow.addColumn().withDisplayRules(12, 12, 6, 4).withGrow(true).withComponent(c).withGrow(true);
        }

//        c.setSizeFull();
        nestedLayout.setSpacing();

        return nestedLayout;
    }

    private void configureLayoutResponsive() {

        if (!layoutBuilt) {
            buildLayoutResponsive();
        }

        cbDocument.setEmptySelectionCaption("Choose Doc type");
        cbDocument.addValueChangeListener(e -> loadDocument((String) cbDocument.getValue()));
        cbDocument.setStyleName(ValoTheme.COMBOBOX_TINY);
        cbDocument.setSizeUndefined();

        cbTopic.setStyleName(ValoTheme.COMBOBOX_TINY);
        cbTopic.setValue(kafkaTopics.get(0));

        tfCount.setValue("1");
        tfCount.setStyleName(ValoTheme.TEXTFIELD_TINY);
        tfCount.addStyleName("light_bg");

        tfBrokers.setValue(props.reconnectString(kafkaBrokers));
        tfBrokers.setStyleName(ValoTheme.TEXTFIELD_TINY);
        tfBrokers.addStyleName("light_bg");
        tfBrokers.addValueChangeListener(e -> nullValueCheck(tfBrokers.getValue()));

        btSetBrokers.addClickListener(e -> resetBrokers(tfBrokers.getValue()));
        btSetBrokers.setStyleName(ValoTheme.BUTTON_TINY);
        btSetBrokers.addStyleName(ValoTheme.BUTTON_PRIMARY);

        configureTextArea();

        responsiveLayout.setSpacing();
//        responsiveLayout.setSizeFull();

        this.setSplitPosition(37, Unit.PERCENTAGE);
        this.setSizeFull();

        image.setHeight(75, Unit.PERCENTAGE);

        topLeftFirstChild.setSpacing(true);
        topLeftFirstChild.setSizeFull();
        topLeftFirstChild.setComponentAlignment(image, Alignment.TOP_CENTER);
        topLeftFirstChild.setComponentAlignment(responsiveLayout, Alignment.MIDDLE_CENTER);
        topLeftFirstChild.setComponentAlignment(tfCount, Alignment.BOTTOM_CENTER);

        panel.setSizeFull();
        panel.setSizeUndefined();
        panel.setWidth(100, Unit.PERCENTAGE);
        panel.setStyleName(ValoTheme.PANEL_BORDERLESS);

        bottomLeftFirstChild.setSizeFull();
        bottomLeftFirstChild.setMargin(true);
        bottomLeftFirstChild.setSpacing(true);
        bottomLeftFirstChild.setExpandRatio(taDoc, 8);
        bottomLeftFirstChild.setComponentAlignment(taDoc, Alignment.MIDDLE_CENTER);
        bottomLeftFirstChild.setComponentAlignment(btPublish, Alignment.MIDDLE_CENTER);

        btPublish.addStyleName(ValoTheme.BUTTON_FRIENDLY);

        layoutConfigured = true;
    }

    private void makeResources() {

        try {
            if (kafka == null) {
                kafka = new KafkaProducer();
                kafka.init();
            }

            if (props == null) {
                props = new PropertyHelper("kafka.properties");
            }

            kafkaTopics = props.loadProperty("kafka.topics");
            kafkaBrokers = props.loadProperty("kafka.brokers");
            docTypes = props.loadProperty("document.type");

            themeResource = new ThemeResource("trustev-logo-white.png");
            image = new Image("", themeResource);

            resourcesMade = true;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    private void configureTextArea() {
        try {
            message = readFile("blank.json");
        } catch (IOException e) {

        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        taDoc.setCaption("Input Document:");
        taDoc.setValue(message);
        taDoc.setSizeFull();
        taDoc.setWidth(90, Unit.PERCENTAGE);
    }

    private void onButtonClick() {
        int count = Integer.parseInt(tfCount.getValue());

        if (count > 1) {
            for (int i = 0; i < count; i++) {
                if (i == (count - 1)) {
                    publishDoc((String) cbTopic.getValue(), i + 1 + " " + taDoc.getValue(), true);
                } else {
                    publishDoc((String) cbTopic.getValue(), i + 1 + " " + taDoc.getValue(), false);
                }
            }
        } else {
            publishDoc((String) cbTopic.getValue(), taDoc.getValue(), true);
        }
    }

    private void resetBrokers(String value) {
        kafka.makeNewProducer(value);
        Notification notifcation = new Notification("Brokers Reset", "Broker strings reset to " + value + ". Esure that broker strings are valid, or messages will not be published", Notification.Type.TRAY_NOTIFICATION);
        notifcation.show(Page.getCurrent());
    }

    private void nullValueCheck(String value) {
        if (value == null || value.equals("")) {
            btSetBrokers.setEnabled(false);
        } else {
            btSetBrokers.setEnabled(true);
        }
    }

    private void loadDocument(String value) {
        try {
            if (value.equals("Session")) {
                message = readFile("sessionDocForKafka.json");
            } else if (value.equals("CustomerIdentification")) {
                message = readFile("customerIdentificationDocForKafka.json");
            } else if (value.equals("Blank")) {
                message = readFile("blank.json");
            }

            taDoc.setValue(message);
        } catch (IOException e) {
            e.printStackTrace();
            Notification notifcation = new Notification("Document not loaded", "IOException: " + e.getMessage(), Notification.Type.TRAY_NOTIFICATION);
            notifcation.show(Page.getCurrent());
        } catch (URISyntaxException e) {
            e.printStackTrace();
            Notification notifcation = new Notification("Document not loaded", "URISytaxException: " + e.getMessage(), Notification.Type.TRAY_NOTIFICATION);
            notifcation.show(Page.getCurrent());
        }
    }

    private void publishDoc(String topic, String value, boolean notify) {

        if (kafka.isProducerClosed()) {
            try {
                kafka.init();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        }

        boolean success = kafka.produceSync(topic, value);

        Notification notifcation;
        if (success && notify) {
            notifcation = new Notification("Document(s) Published", "If content is valid, then the document has been published to the " + (String) cbTopic.getValue() + " topic", Notification.Type.TRAY_NOTIFICATION);
            notifcation.show(Page.getCurrent());
        } else if (!success && notify) {
            notifcation = new Notification("Document not Published", "Something went wrong", Notification.Type.TRAY_NOTIFICATION);
            notifcation.show(Page.getCurrent());
        }
    }

    private String readFile(String fileName) throws IOException, URISyntaxException {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        String returnFile = new String(Files.readAllBytes(Paths.get(classLoader.getResource(fileName).toURI())));
        return returnFile;
    }
}
