package com.example.pkt_inspection;

import org.pcap4j.core.*;
import org.pcap4j.packet.*;
import org.pcap4j.core.PcapNetworkInterface.PromiscuousMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class NetworkCaptureService {
    private PcapHandle handle;
    private PacketListener listener;
    private List<PacketInfo> packetHistory;
    private volatile boolean isRunning;
    private IntrusionDetectionSystem ids;

    public NetworkCaptureService() {
        this.packetHistory = new CopyOnWriteArrayList<>();
        this.ids = new IntrusionDetectionSystem();
        this.isRunning = false;
    }

    public void startCapture(PcapNetworkInterface device) throws PcapNativeException {
        if (isRunning) return;

        int snapshotLength = 65536;
        int readTimeout = 50;
        handle = device.openLive(snapshotLength, PromiscuousMode.PROMISCUOUS, readTimeout);

        listener = packet -> {
            PacketInfo packetInfo = createPacketInfo(packet);
            packetHistory.add(packetInfo);
            ids.analyzePacket(packetInfo);
        };

        isRunning = true;
        new Thread(() -> {
            try {
                handle.loop(-1, listener);
            } catch (PcapNativeException | InterruptedException | NotOpenException e) {
                e.printStackTrace();
            }
        }).start();
    }

    public void stopCapture() throws NotOpenException {
        if (!isRunning) return;
        isRunning = false;
        if (handle != null) {
            handle.breakLoop();
            handle.close();
        }
    }

    public List<PacketInfo> getPacketHistory() {
        return new ArrayList<>(packetHistory);
    }

    public void configureFilters(String filter) throws PcapNativeException, NotOpenException {
        if (handle != null) {
            handle.setFilter(filter, BpfProgram.BpfCompileMode.OPTIMIZE);
        }
    }
    public IntrusionDetectionSystem getIds() {
        return ids;
    }

    private PacketInfo createPacketInfo(Packet packet) {
        IpV4Packet ipV4Packet = packet.get(IpV4Packet.class);
        TcpPacket tcpPacket = packet.get(TcpPacket.class);

        String sourceIP = "";
        String destIP = "";
        int sourcePort = 0;
        int destPort = 0;
        String protocol = "UNKNOWN";

        if (ipV4Packet != null) {
            sourceIP = ipV4Packet.getHeader().getSrcAddr().getHostAddress();
            destIP = ipV4Packet.getHeader().getDstAddr().getHostAddress();
            protocol = "IPv4";
        }

        if (tcpPacket != null) {
            sourcePort = tcpPacket.getHeader().getSrcPort().valueAsInt();
            destPort = tcpPacket.getHeader().getDstPort().valueAsInt();
            protocol = "TCP";
        }

        return new PacketInfo(
                packet,
                LocalDateTime.now(),
                sourceIP,
                destIP,
                sourcePort,
                destPort,
                protocol,
                packet.length()
        );
    }
}