package com.CPTC.X3DPrint.impl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;
import java.io.StringWriter;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

import com.CPTC.X3DPrint.GCode2URScript;
import com.ur.urcap.api.contribution.ProgramNodeContribution;
import com.ur.urcap.api.contribution.program.ProgramAPIProvider;
import com.ur.urcap.api.domain.data.DataModel;
import com.ur.urcap.api.domain.io.AnalogIO;
import com.ur.urcap.api.domain.io.DigitalIO;
import com.ur.urcap.api.domain.script.ScriptWriter;
import com.ur.urcap.api.domain.tcp.TCP;
import com.ur.urcap.api.domain.undoredo.UndoRedoManager;
import com.ur.urcap.api.domain.undoredo.UndoableChanges;
import com.ur.urcap.api.domain.value.Pose;
import com.ur.urcap.api.domain.value.simple.Angle;
import com.ur.urcap.api.domain.value.simple.Length;

public class X3DPrintProgramNodeContribution implements ProgramNodeContribution {
	
	private final String PRINT_SPEED_KEY = "print_Speed";
	private final String PRINT_ACCELERATION_KEY = "print_Acceleration";
	private final String TRIGGER_PIN_KEY = "trigger_pin";
	private final String WIREFEED_PIN_KEY = "wirefeed_pin";
	private final String ZERO_POSITION_KEY = "home_position";
	private final String WIREFEED_RATE_KEY = "wirefeed_rate";
	private final String GCODE_FILE_PATH_KEY = "gcode_file";
	
	private final double DEFAULT_PRINT_SPEED_MM = 3;
	private final double DEFAULT_PRINT_ACCELERATION_MM = 5;
	private final int DEFAULT_TRIGGER_PIN = -1;
	private final int DEFAULT_WIREFEED_PIN = -1;
	private final int DEFAULT_WIREFEED_RATE = 10;
	private final String DEFAULT_GCODE_FILE_PATH = "";
		
	private final ProgramAPIProvider apiProvider;
	private final X3DPrintProgramNodeView view;
	private final DataModel model;
	private final UndoRedoManager undoRedoManager;
	
	private String[] collectionDOs;
	private String[] collectionAOs;
	
	private final String URCAP_TITLE = "3D Print";
	private boolean isGCodeLoaded = false;
	private boolean isInitial = true;
	
	private ArrayList<String> URSCRIPT = new ArrayList<String>();
	
	public X3DPrintProgramNodeContribution(ProgramAPIProvider apiProvider, X3DPrintProgramNodeView view, 
			DataModel model) {
		this.apiProvider = apiProvider;
		this.view = view;
		this.model = model;
		this.undoRedoManager = this.apiProvider.getProgramAPI().getUndoRedoManager();
		getCurrentTPC();
		getIOList();
	}

	@Override
	public void openView() {
		if(isInitial) {
			view.setTriggerComboItems(collectionDOs);
			view.setWirefeedComboItems(collectionAOs);
			view.setHomePositionLabel(null);
			isInitial = false;
		}
		
		view.setTriggerSelectedItem(getTriggerPin());
		view.setWirefeedSelectedItem(getWirefeedPin());
		view.setSpeed(getPrintSpeed());
		view.setAcceleration(getPrintAcceleration());		
		view.setWirefeedRateSlider(getWirefeedRate());
		
		String filename = "";
		if(getGcodeFile() != null) {
			filename = getGcodeFile().getName();
		}
		view.setSelectedFileNameLabel(filename);
		
	}

	@Override
	public void closeView() {

	}

	@Override
	public String getTitle() {		
		return URCAP_TITLE;
	}

	@Override
	public boolean isDefined() {
		if(isGCodeLoaded)
		{
			return true;
		}
		return false;
	}
	
	public void onSelectedFile(final File file) {
		undoRedoManager.recordChanges(new UndoableChanges() {
			
			@Override
			public void executeChanges() {
				model.set(GCODE_FILE_PATH_KEY, file.getPath());
				setGCodeLoaded(true);
			}
		});
	}
	
	public void onSpeedTxtChanged(final double value) {
		undoRedoManager.recordChanges(new UndoableChanges() {
			
			@Override
			public void executeChanges() {
				model.set(PRINT_SPEED_KEY, value);
			}
		});
	}
	
	public void onAccelerationTxtChanged(final double value) {
		undoRedoManager.recordChanges(new UndoableChanges() {
			
			@Override
			public void executeChanges() {
				model.set(PRINT_ACCELERATION_KEY, value);
			}
		});
		

	}
	
	public void onTriggerPinChanged(final int value) {
		undoRedoManager.recordChanges(new UndoableChanges() {
			
			@Override
			public void executeChanges() {
				model.set(TRIGGER_PIN_KEY, value);
			}
		});
		
	}
	
	public void onWirefeedPinChanged(final int value) {
		undoRedoManager.recordChanges(new UndoableChanges() {
			
			@Override
			public void executeChanges() {
				model.set(WIREFEED_PIN_KEY, value);
			}
		});
	}
	
	public void onSetHome() {
		undoRedoManager.recordChanges(new UndoableChanges() {
			
			@Override
			public void executeChanges() {
				
				model.set(ZERO_POSITION_KEY, getCurrentTPC());
			}
		});		
	}
	
	public void onWireFeedRateChange(final int value) {
		undoRedoManager.recordChanges(new UndoableChanges() {
			
			@Override
			public void executeChanges() {
				model.set(WIREFEED_RATE_KEY, value);
			}
		});
	}

	@Override
	public void generateScript(ScriptWriter writer) {

		setURScriptTranslated(translateGCode(getGcodeFile()));
		
		for (String scriptLine : getTranlatedURScript()) {
			writer.appendLine(scriptLine);
		}
		
	}
	
	private ArrayList<String> translateGCode(File gcodeFile) {
		GCode2URScript translator = new GCode2URScript(gcodeFile.getPath(), getHomePosition(), getPrintSpeed(), getPrintAcceleration());
		translator.setTriggerPin(getTriggerPin());
		return translator.generateURScript();
	}
	
	private double[] getCurrentTPC() {
		Pose pose = apiProvider.getProgramAPI().getFeatureModel().getToolFeature().getPose();
		return pose.toArray(Length.Unit.M, Angle.Unit.RAD);
	}
	
	private void getIOList() {
		Collection<DigitalIO> colDIOs = apiProvider.getProgramAPI().getIOModel().getIOs(DigitalIO.class);
		Collection<AnalogIO> colAIOs = apiProvider.getProgramAPI().getIOModel().getIOs(AnalogIO.class);
		
		ArrayList<String> tmpAOs = new ArrayList<String>();
		ArrayList<String> tmpDOs = new ArrayList<String>();
		
		for (AnalogIO aIO : colAIOs) {
			if (!aIO.isInput()) {
				tmpAOs.add(aIO.getName());
			}
		}
		
		for (DigitalIO dIO : colDIOs) {
			if (!dIO.isInput()) {
				tmpDOs.add(dIO.getName());
			}
		}
		
		collectionAOs = tmpAOs.toArray(new String[0]);
		collectionDOs = tmpDOs.toArray(new String[0]);
	}
	
	private int getTriggerPin() {
			
		return model.get(TRIGGER_PIN_KEY, DEFAULT_TRIGGER_PIN);
	}
	
	private int getWirefeedPin() {
		
		return model.get(WIREFEED_PIN_KEY, DEFAULT_WIREFEED_PIN);
	}
	
	private double getPrintSpeed() {
		
		return model.get(PRINT_SPEED_KEY, DEFAULT_PRINT_SPEED_MM);
	}
	
	private double getPrintAcceleration() {
		
		return model.get(PRINT_ACCELERATION_KEY, DEFAULT_PRINT_ACCELERATION_MM);
	}
	
	private int getWirefeedRate() {
		return model.get(WIREFEED_RATE_KEY, DEFAULT_WIREFEED_RATE);
	}
	
	private ArrayList<String> getTranlatedURScript() {
		return URSCRIPT;
	}
	
	private void setGCodeLoaded(boolean value) {
		isGCodeLoaded = value;
	}
	
	private void setURScriptTranslated(ArrayList<String> script) {
		URSCRIPT = script;
	}
	
	public double[] getHomePosition() {
		return model.get(ZERO_POSITION_KEY, getCurrentTPC());
	}
	
	private File getGcodeFile() {
		File file = null;
		String path = model.get(GCODE_FILE_PATH_KEY, DEFAULT_GCODE_FILE_PATH);
		if(path.equals(DEFAULT_GCODE_FILE_PATH)) {
			return file;
		}
		
		file = new File(path);
		
		return file; 
	}


}
