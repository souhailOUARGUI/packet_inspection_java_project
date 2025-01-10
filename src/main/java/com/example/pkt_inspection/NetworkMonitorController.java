package com.example.pkt_inspection;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.pcap4j.core.*;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class NetworkMonitorController implements Initializable {
    @FXML private ComboBox<String> deviceSelector;
    @FXML private TextField filterField;
    @FXML private Button startButton;
    @FXML private Button stopButton;
    @FXML private TableView<PacketInfo> packetTable;
    @FXML private TableColumn<PacketInfo, String> timeColumn;
    @FXML private TableColumn<PacketInfo, String> sourceColumn;
    @FXML private TableColumn<PacketInfo, String> destColumn;
    @FXML private TableColumn<PacketInfo, String> protocolColumn;
    @FXML private TableColumn<PacketInfo, Integer> lengthColumn;
    @FXML private TextArea packetDetails;
    @FXML private ListView<String> alertList;
    @FXML private Label statusLabel;
    @FXML private Label packetsCountLabel;

    private NetworkCaptureService captureService;
    private List<PcapNetworkInterface> devices;
    private DateTimeFormatter timeFormatter;
    private ExecutorService executorService;
    private volatile boolean isCapturing;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            captureService = new NetworkCaptureService();
            timeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            executorService = Executors.newSingleThreadExecutor(r -> {
                Thread thread = new Thread(r, "PacketUpdateThread");
                thread.setDaemon(true);
                return thread;
            });
            isCapturing = false;

            initializeTableColumns();
            initializeNetworkDevices();
            setupPacketTableSelection();
            setupAlertCallback();
            
            // Initialize UI state
            stopButton.setDisable(true);
            statusLabel.setText("Ready");
            packetsCountLabel.setText("Packets: 0");
            
        } catch (Exception e) {
            showError("Initialization Error", "Failed to initialize application: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void setupAlertCallback() {
        if (captureService != null && captureService.getIds() != null) {
            captureService.getIds().setAlertCallback(message -> 
                Platform.runLater(() -> {
                    alertList.getItems().add(message);
                    System.out.println("Alert received: " + message);
                })
            );
        }
    }

    private void initializeTableColumns() {
        timeColumn.setCellValueFactory(data -> 
            javafx.beans.binding.Bindings.createStringBinding(
                () -> data.getValue().getTimestamp().format(timeFormatter)
            )
        );

        sourceColumn.setCellValueFactory(data ->
            javafx.beans.binding.Bindings.createStringBinding(
                () -> data.getValue().getSourceIP() + ":" + data.getValue().getSourcePort()
            )
        );

        destColumn.setCellValueFactory(data ->
            javafx.beans.binding.Bindings.createStringBinding(
                () -> data.getValue().getDestinationIP() + ":" + data.getValue().getDestinationPort()
            )
        );

        protocolColumn.setCellValueFactory(data ->
            javafx.beans.binding.Bindings.createStringBinding(
                () -> data.getValue().getProtocol()
            )
        );

        lengthColumn.setCellValueFactory(new PropertyValueFactory<>("size"));
    }

    private void setupPacketTableSelection() {
        packetTable.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldSelection, newSelection) -> {
                if (newSelection != null) {
                    packetDetails.setText(newSelection.getDetailedInfo());
                }
            }
        );
    }

    private void initializeNetworkDevices() {
        try {
            devices = Pcaps.findAllDevs();
            if (devices.isEmpty()) {
                showError("No Network Interfaces", "No network interfaces found. Please check your network configuration.");
                return;
            }
            
            for (PcapNetworkInterface dev : devices) {
                deviceSelector.getItems().add(getDeviceDescription(dev));
            }
            deviceSelector.getSelectionModel().select(0);
        } catch (PcapNativeException e) {
            showError("Error", "Failed to load network devices: " + e.getMessage());
        }
    }

    private String getDeviceDescription(PcapNetworkInterface dev) {
        String description = dev.getDescription();
        String name = dev.getName();
        return description != null && !description.isEmpty() ? description : name;
    }

    @FXML
    private void startCapture() {
        int selectedIndex = deviceSelector.getSelectionModel().getSelectedIndex();
        if (selectedIndex < 0) {
            showError("Error", "Please select a network interface");
            return;
        }

        try {
            PcapNetworkInterface device = devices.get(selectedIndex);
            String filter = filterField.getText();
            
            captureService.startCapture(device);
            if (!filter.isEmpty()) {
                captureService.configureFilters(filter);
            }

            isCapturing = true;
            updateUIState(true);
            startPacketUpdates();

        } catch (Exception e) {
            showError("Error", "Failed to start capture: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void stopCapture() {
        try {
            isCapturing = false;
            if (captureService != null) {
                captureService.stopCapture();
            }
            updateUIState(false);
        } catch (NotOpenException e) {
            showError("Error", "Failed to stop capture: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void updateUIState(boolean capturing) {
        Platform.runLater(() -> {
            startButton.setDisable(capturing);
            stopButton.setDisable(!capturing);
            deviceSelector.setDisable(capturing);
            filterField.setDisable(capturing);
            statusLabel.setText(capturing ? "Capturing..." : "Ready");
            if (!capturing) {
                packetsCountLabel.setText("Packets: 0");
            }
        });
    }

    private void startPacketUpdates() {
        executorService.submit(() -> {
            while (isCapturing && !Thread.currentThread().isInterrupted()) {
                try {
                    Thread.sleep(1000);
                    updatePacketDisplay();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });
    }

    private void updatePacketDisplay() {
        if (!isCapturing) return;
        
        Platform.runLater(() -> {
            try {
                List<PacketInfo> packets = captureService.getPacketHistory();
                packetTable.getItems().clear();
                packetTable.getItems().addAll(packets);
                packetsCountLabel.setText("Packets: " + packets.size());
            } catch (Exception e) {
                System.err.println("Error updating packet display: " + e.getMessage());
            }
        });
    }

    private void showError(String title, String content) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(content);
            alert.showAndWait();
        });
    }

    public void shutdown() {
        isCapturing = false;
        
        if (executorService != null) {
            executorService.shutdownNow();
            try {
                if (!executorService.awaitTermination(2, TimeUnit.SECONDS)) {
                    System.err.println("Executor did not terminate in the specified time.");
                }
            } catch (InterruptedException e) {
                System.err.println("Shutdown interrupted: " + e.getMessage());
            }
        }
        
        if (captureService != null) {
            try {
                captureService.stopCapture();
            } catch (NotOpenException e) {
                System.err.println("Error stopping capture: " + e.getMessage());
            }
        }
    }
}
