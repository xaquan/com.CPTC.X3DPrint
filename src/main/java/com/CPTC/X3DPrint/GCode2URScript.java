package com.CPTC.X3DPrint;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;

import com.ur.urcap.api.domain.script.ScriptWriter;
import com.ur.urcap.api.domain.tcp.TCP;
import com.ur.urcap.api.domain.value.Pose;
import com.ur.urcap.api.domain.value.Position;
import com.ur.urcap.api.domain.value.Rotation;
import com.ur.urcap.api.domain.value.simple.Length;
import com.ur.urcap.api.domain.value.simple.SimpleValueFactory;
import com.ur.urcap.api.domain.value.simple.Length.Unit;
import com.ur.urcap.api.domain.value.simple.Angle;

public class GCode2URScript {
	
	private HashMap<String, String> gCode2URscriptMap = new HashMap<String, String>();
	private ArrayList<String> urScriptWriter = new ArrayList<String>();
	
	private final String URSCRIPT_POSE = "p[%f,%f,%f,%f,%f,%f]";
	private final String URSCRIPT_MOVEJ = "movej(%s,a=%f,v=%f)\n";
	private final String URSCRIPT_MOVEL = "movel(%s,a=%f,v=%f)\n";
	private final String URSCRIPT_MOVEP = "movep(%s,a=%f,v=%f, r=%f)\n";
	private final String URSCRIPT_ANALOG_OUT = "set_analog_out(%f,%f)\n";
	private final String URSCRIPT_DIGITAL_OUT = "set_digital_out(%d,%s)\n";
	
	private double travel_speed = 250;
	private double travel_acceleration = 1200;
	private double print_speed;
	private double print_acceleration;
	private double print_blend_radius;
	private int trigger_pinNumber;
	private double[] zeroPosition;
	private String path;
	private boolean currentWeldingTriggerState;
	private double lastExtruderPos = 0;
	private boolean useGCodeSpeed = true;
	
	public GCode2URScript(String path, double[] zeroPosition, double print_speed_MM, double print_acceleration_MM, double prit_blend_radius_MM) {
//		this.travel_speed = travel_speed;
//		this.travel_acceleration = travel_acceleration;
		this.print_speed = print_speed_MM/1000;
		this.print_acceleration = print_acceleration_MM/1000;
		this.print_blend_radius = prit_blend_radius_MM/1000;
		this.zeroPosition = zeroPosition;
		this.path = path;
	}
	
	/**
	 * <p>
	 * Set Digital Out pin number to trigger the welder
	 * </p>
	 *
	 * 
	 */
	public void setTriggerPinNumber(int pinNumber) {
		trigger_pinNumber = pinNumber;
	}
	
	/**
	 * <p>
	 * Using speed number in Gcode or from Control panel
	 * Default is true
	 * </p>
	 *
	 * 
	 */
	public void setUseGCodeSpeed(boolean value) {

		useGCodeSpeed = value;
	}
	
	public void setTCPHome(double[] position) {
		zeroPosition = position;
	}
	
	private void convert(File sourceFile) {
		try {
			BufferedReader ncFile = new BufferedReader(new FileReader(sourceFile));
			
			String gCodeLine;
			while((gCodeLine = ncFile.readLine()) != null) {
				if(gCodeLine.trim().length() > 0 && gCodeLine.charAt(0) != ';') {

					String[] gCodeAndComment = gCodeLine.trim().split(";");
					
					if(gCodeAndComment.length > 0) {
						gCodeLine = gCodeAndComment[0].trim();
						gCodeTranslate(gCodeLine);
					}
				}
	        
			}
			ncFile.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	private void gCodeTranslate(String codeLine) {
		String[] codeParts = codeLine.split("\\s+");
		HashMap<String, String> codeAgruments = new HashMap<String, String>();
		String codeCommand = codeParts[0];
		char codeGroup = codeLine.charAt(0);
		
		for (int i = 1; i < codeParts.length; i++) {
			String name = String.valueOf(codeParts[i].charAt(0));
			String value = codeParts[i].substring(1);
			codeAgruments.put(name, value);
		}
		
		if(codeGroup == 'G') {		
			gCommandCodeG(codeCommand, codeAgruments);
		}

		if(codeGroup == 'M') {
			gCommandCodeM(codeParts);
		}
		
		if(codeGroup == 'S') {
			gCommandCodeS(codeParts);
		}
	}
	
	private void gCommandCodeG(String command, HashMap<String, String> agruments) {
		String urScript = "";
		String pose = URSCRIPT_POSE;
		double acceleration = getPrintAcceleration();
		
		if(command.equals("G0")) {
			gCommandCodeE(-1);
			urScript = URSCRIPT_MOVEP;
			acceleration = travel_acceleration;
		}
		
		if(command.equals("G1")) {
			urScript = URSCRIPT_MOVEP;
		}
		
		if(agruments.get("E") != null) {
			gCommandCodeE(Double.parseDouble(agruments.get("E")));
		}
		
		if(agruments.get("F") != null) {
			gCommandCodeF(Double.parseDouble(agruments.get("F")), command);
		}
		
		if(agruments.containsKey("X") || agruments.containsKey("Y") || agruments.containsKey("Z")) {
				
			double X = zeroPosition[0];
			double Y = zeroPosition[1];
			double Z = zeroPosition[2];		
			double Rx = zeroPosition[3];
			double Ry = zeroPosition[4];
			double Rz = zeroPosition[5];
	
			if(agruments.containsKey("X")) {
				X += Double.parseDouble(agruments.get("X")) / 1000;
			}
			
			if(agruments.containsKey("Y")) {
				Y += Double.parseDouble(agruments.get("Y")) / 1000;
			}
			
			if(agruments.containsKey("Z")) {
				Z += Double.parseDouble(agruments.get("Z")) / 1000;
			}
			
			pose =  String.format(pose, X, Y, Z, Rx, Ry, Rz);
			urScript = String.format(urScript, pose, getPrintSpeed(), acceleration, getPrintBlendRadius());
			addURScriptLine(urScript);
		}
	}
	
	private void gCommandCodeG92() {
		
	}
	
	private void gCommandCodeM(String[] codeParts) {
		
	}
	
	private void gCommandCodeS(String[] codeParts) {
		
	}
	
	private void gCommandCodeF(double speed, String command) {
		double speed_M = speed/1000;
		setPrintSpeed(speed_M);
	}

	private void gCommandCodeE(double extruderPos) {
		String urSript_setTrigger;
		boolean isRetractExtruder = extruderPos < lastExtruderPos;
		boolean setTrigger = false;
		String setValue = "False";
		
		if(trigger_pinNumber > -1) {
			if(extruderPos > 0 && !isRetractExtruder) {
				// Trigger on.			
				setValue = "True";
				setTrigger = true;
				//Need to add code to control the wirefeed speed.
			}
			
			urSript_setTrigger = String.format(URSCRIPT_DIGITAL_OUT, trigger_pinNumber, setValue);
			
			if(setTrigger != currentWeldingTriggerState) {
				addURScriptLine(urSript_setTrigger);
			}
			currentWeldingTriggerState = setTrigger;
			lastExtruderPos = extruderPos;
		}
	}
	
	private void setPrintSpeed(double speed) {
		if (useGCodeSpeed) {
			print_speed = speed;
			addLogInRobot("Change speed", String.valueOf(speed));
		}
	}
	
	private void setPrintAcceleration(double acc) {
		if (useGCodeSpeed) {
			print_acceleration = acc;
		}
	}
	
	private double getPrintAcceleration() {
//		if(useGCodeSpeed) {
//			return 1;
//		}else {
			return print_acceleration;
//		}
	}
	
	private double getPrintBlendRadius() {
		return print_blend_radius;
	}
	
	private double getPrintSpeed() {
		return print_speed;
	}
	
	private void addURScriptLine(String script) {
		urScriptWriter.add(script);
	}
	
	public ArrayList<String> generateURScript() {
		File sourceFile = new File(path);
		convert(sourceFile);	
		
		return urScriptWriter;
	}
	
	public void addLogInRobot(String title, String value) {
		addURScriptLine("textmsg(\"" + title + "\",\"" + value + "\")");
	}
}
