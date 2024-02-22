package com.foobar.tools.lockbox;

import java.util.ArrayList;
import java.util.Arrays;

import oshi.SystemInfo;
import oshi.hardware.Baseboard;
import oshi.hardware.CentralProcessor;
import oshi.hardware.ComputerSystem;
import oshi.hardware.Firmware;
import oshi.hardware.GlobalMemory;
import oshi.hardware.HWDiskStore;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.hardware.NetworkIF;
import oshi.software.os.FileSystem;
import oshi.software.os.NetworkParams;
import oshi.software.os.OSFileStore;
import oshi.util.FormatUtil;

public class OSHIValues {
    private HardwareAbstractionLayer hal = null;
    private SystemInfo si = null;
    
    public OSHIValues() {
    	this.si = new SystemInfo();
    	this.hal = si.getHardware();

    }
    
    public byte [][] getValues() {
    	ArrayList<byte []> returnValues = new ArrayList<byte[]>();
    	returnValues.add(this.getOSVersion());
    	returnValues.add(this.getSystemInfo());
    	returnValues.add(this.getFirmwareInfo());
    	returnValues.add(this.getBoardInfo());
    	returnValues.add(this.getProcessorInfo());
    	returnValues.add(this.getMemorySwapInfo());
    	returnValues.add(this.getDiskInfo());
    	returnValues.add(this.getFileSystemInfo());
    	returnValues.add(this.getNetworkIFInfo());
    	returnValues.add(this.getNetworkInfo());
    	return returnValues.toArray(new byte[returnValues.size()][]);
    }
    
	// OS Version
    private byte [] getOSVersion() {
		return si.getOperatingSystem().toString().getBytes();
    }
    
    // Basic system info
    private byte [] getSystemInfo() {
    	ComputerSystem computerSystem= hal.getComputerSystem();
    	StringBuilder sb = new StringBuilder();
    	sb.append(computerSystem.getManufacturer());
        sb.append(computerSystem.getModel());
        sb.append(computerSystem.getSerialNumber());
        return sb.toString().getBytes();
    }
    
    // Firmware version info
    private byte [] getFirmwareInfo() {    
    
    	StringBuilder sb = new StringBuilder();
        Firmware firmware = hal.getComputerSystem().getFirmware();
        sb.append(firmware.getName());
        sb.append(firmware.getDescription());
        sb.append(firmware.getVersion());
        sb.append((firmware.getReleaseDate() == null ? "unknown" : firmware.getReleaseDate() == null ? "unknown" : FormatUtil.formatDate(firmware.getReleaseDate())));
        return sb.toString().getBytes();
    }
    
    // Base board info
    private byte [] getBoardInfo() {
        Baseboard baseboard = hal.getComputerSystem().getBaseboard();
        StringBuilder sb = new StringBuilder();
        sb.append(baseboard.getManufacturer());
        sb.append(baseboard.getModel());
        sb.append(baseboard.getVersion());
        sb.append(baseboard.getSerialNumber());
        return sb.toString().getBytes();
    }

    // Processor Information
    private byte [] getProcessorInfo() {
    	CentralProcessor processor = hal.getProcessor();
    	StringBuilder sb = new StringBuilder();
    	sb.append(processor);
    	sb.append(processor.getIdentifier());
    	sb.append(processor.getProcessorID());
    	return sb.toString().getBytes();
    }
    
    // memory and swap totals.
    private byte [] getMemorySwapInfo() {
    	GlobalMemory memory = hal.getMemory();
    	StringBuilder sb = new StringBuilder();
    	sb.append(memory.getTotal());
    	sb.append(memory.getSwapTotal());
    	return sb.toString().getBytes();
    }
    
    // Disk information
    private byte [] getDiskInfo() {
    	HWDiskStore [] diskStores = hal.getDiskStores();
    	StringBuilder sb = new StringBuilder();
    	for (HWDiskStore disk : diskStores) {
    		sb.append(disk.getModel());
    		sb.append(disk.getSerial());
    		sb.append(disk.getSize());
    	}
    	return sb.toString().getBytes();
    }            
    
    // filesystem information
    private byte [] getFileSystemInfo() {
    	FileSystem fileSystem = si.getOperatingSystem().getFileSystem();
    	StringBuilder sb = new StringBuilder();
    	OSFileStore[] fsArray = fileSystem.getFileStores();
    	for (OSFileStore fs : fsArray) {
    		sb.append(fs.getTotalSpace());
    		if (fs.getLogicalVolume() != null) sb.append(fs.getLogicalVolume());
    		sb.append(fs.getName());
    		sb.append(fs.getDescription());
    		sb.append(fs.getUUID());
    		sb.append(fs.getMount());
    		sb.append(fs.getType());
    		sb.append(fs.getVolume());
    	}
    	return sb.toString().getBytes();
    }
    
    // network interfaces
    private byte [] getNetworkIFInfo() {
    	NetworkIF[] networkIFs = hal.getNetworkIFs();
    	StringBuilder sb = new StringBuilder();
    	for (NetworkIF net : networkIFs) {
    		sb.append(net.getName());
    		sb.append(net.getDisplayName());
    		sb.append(net.getMacaddr());
    		sb.append(Arrays.toString(net.getIPv4addr()));
    		sb.append(Arrays.toString(net.getIPv6addr()));
    	}    	
    	return sb.toString().getBytes();
    }
    
    // Network parameters
    private byte [] getNetworkInfo() {
    	NetworkParams networkParams = si.getOperatingSystem().getNetworkParams();
    	StringBuilder sb = new StringBuilder();
    	sb.append(networkParams.getHostName());
    	sb.append(networkParams.getDomainName());
    	sb.append(Arrays.toString(networkParams.getDnsServers()));
    	sb.append(networkParams.getIpv4DefaultGateway());
    	sb.append(networkParams.getIpv6DefaultGateway());
    	return sb.toString().getBytes();
    }
}
