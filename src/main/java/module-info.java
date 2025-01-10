module com.example.pkt_inspection {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.pcap4j.core;
    
    opens com.example.pkt_inspection to javafx.fxml;
    exports com.example.pkt_inspection;
}