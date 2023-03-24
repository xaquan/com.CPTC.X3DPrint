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
	
	private final double DEFAULT_PRINT_SPEED_MM = 250;
	private final double DEFAULT_PRINT_ACCELERATION_MM = 1200;
	private final int DEFAULT_TRIGGER_PIN = -1;
	private final int DEFAULT_WIREFEED_PIN = -1;
		
	private final ProgramAPIProvider apiProvider;
	private final X3DPrintProgramNodeView view;
	private final DataModel model;
	private final UndoRedoManager undoRedoManager;
	
	private String[] collectionDOs;
	private String[] collectionAOs;
	
	private final String URCAP_TITLE = "3D Print";
	private File gCodeFile;
	private boolean isDefined = false;
	
	private ArrayList<String> URSCRIPT = new ArrayList<String>();
	
	public X3DPrintProgramNodeContribution(ProgramAPIProvider apiProvider, X3DPrintProgramNodeView view, 
			DataModel model) {
		this.apiProvider = apiProvider;
		this.view = view;
		this.model = model;
		this.undoRedoManager = this.apiProvider.getProgramAPI().getUndoRedoManager();
		getCurrentTPC();
		getIOList();		

		view.setTriggerComboItems(collectionDOs);
		view.setWirefeedComboItems(collectionAOs);
	}

	@Override
	public void openView() {
		view.setTriggerSelectedItem(getTriggerPin());
		view.setWirefeedSelectedItem(getWirefeedPin());
		view.setSpeed(getPrintSpeed());
		view.setAcceleration(getPrintAcceleration());
		if(gCodeFile != null) {
			view.setSelectedFileNameLabel(gCodeFile.getName());
		}
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
		return false;
	}
	
	public void onSelectedFile(final File file) {
		gCodeFile = file;
		setTranlatedURScript(translateGCode(file));
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
				System.out.println(value);
				model.set(PRINT_ACCELERATION_KEY, value);
				System.out.println(getPrintAcceleration());
			}
		});
		

	}
	
	public void onTriggerPinChanged(final int value) {
		System.out.println(value);
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

	@Override
	public void generateScript(ScriptWriter writer) {
//		writer.assign("isRunWelder", "True");		
//		writer.defineThread("X3DPrintThread");
//		
//		writer.end();
		
		for (String scriptLine : getTranlatedURScript()) {
			writer.appendLine(scriptLine);
		}
		
	}
	
	private ArrayList<String> translateGCode(File gcodeFile) {
		GCode2URScript translator = new GCode2URScript(gcodeFile.getPath(), getCurrentTPC(), getPrintSpeed(), getPrintAcceleration());
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
//		collectionAOs.add("Select output");
//		collectionDOs.add("Select output");
		
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
	
	private ArrayList<String> getTranlatedURScript() {
		return URSCRIPT;
	}
	
	private void setTranlatedURScript(ArrayList<String> scripts) {
		URSCRIPT = scripts;
	}


}
