package com.CPTC.X3DPrint.impl;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.InputMethodEvent;
import java.awt.event.InputMethodListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeListener;
import java.io.File;

import javax.swing.Action;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.ur.urcap.api.contribution.ContributionProvider;
import com.ur.urcap.api.contribution.ViewAPIProvider;
import com.ur.urcap.api.contribution.program.swing.SwingProgramNodeView;
import com.ur.urcap.api.domain.userinteraction.keyboard.KeyboardInputCallback;
import com.ur.urcap.api.domain.userinteraction.keyboard.KeyboardNumberInput;

public class X3DPrintProgramNodeView implements SwingProgramNodeView<X3DPrintProgramNodeContribution>
{
	private JComboBox<String> triggerComboBox = new JComboBox<String>();
	private JComboBox<String> wirefeedComboBox = new JComboBox<String>();
	private JSlider wirefeedRateSlider = new JSlider();
	private JLabel filenameLabel = new JLabel();
	private JTextField speedTextField = new JTextField();
	private JTextField accelerationTextField = new JTextField();
	
	private final ViewAPIProvider apiProvider;
	private ContributionProvider<X3DPrintProgramNodeContribution> provider;
	
	public X3DPrintProgramNodeView(ViewAPIProvider apiProvider) {
		this.apiProvider = apiProvider;
		
		triggerComboBox.addItemListener(triggerCbItemListener);
		wirefeedComboBox.addItemListener(wirefeedCbItemListener);
	}
	
	public void setWirefeedRateSlider() {
		wirefeedRateSlider.setValue(30);
	}
	
	public void setTriggerSelectedItem(int index) {
		triggerComboBox.setSelectedIndex(index);
	}
	
	public void setTriggerComboItems(String[] items) {
		triggerComboBox.removeAllItems();
		triggerComboBox.setModel(new DefaultComboBoxModel<String>(items));
	}
	
	public void setWirefeedSelectedItem(int index) {
		wirefeedComboBox.setSelectedIndex(index);
	}
	
	public void setWirefeedComboItems(String[] items) {
		wirefeedComboBox.removeAllItems();
		wirefeedComboBox.setModel(new DefaultComboBoxModel<String>(items));
	}
	
	public void setSpeed(double speed) {
		speedTextField.setText(String.valueOf(speed));
	}
	
	public void setAcceleration(double accel) {
		accelerationTextField.setText(String.valueOf(accel));
	}
	
	public void setSelectedFileNameLabel(String filename) {
		filenameLabel.setText(filename);
	}

	@Override
	public void buildUI(JPanel panel, final ContributionProvider<X3DPrintProgramNodeContribution> provider) {
		this.provider = provider;		
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
//		panel.add(createIOSettingBar());
		Box triggerBox = createDropBox(triggerComboBox, "Welder trigger Digital");
		Box wirefeedBox = createDropBox(wirefeedComboBox, "Wirefeed Analog");
		
		
		panel.add(triggerBox);
		panel.add(wirefeedBox);
		panel.add(createSlider(wirefeedRateSlider));
		
		panel.add(createTexbox(speedTextField, "Speed mm/s"));
		panel.add(createTexbox(accelerationTextField, "Speed mm/s^2"));
		
		panel.add(createFileChooser());
		panel.add(filenameLabel);
	}
	
	private Box createTexbox(final JTextField txt, String label) {
		Box box = Box.createHorizontalBox();
		box.setAlignmentX(Component.LEFT_ALIGNMENT);
		txt.setPreferredSize(new Dimension(204, 30));
		txt.setMaximumSize(txt.getPreferredSize());
		
		txt.addMouseListener(new MouseListener() {
			
			@Override
			public void mouseReleased(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void mousePressed(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void mouseExited(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void mouseEntered(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void mouseClicked(MouseEvent e) {
				showNumberalKeyBoard(txt);
			}
		});
		
		JLabel lbl = new JLabel(label);
		box.add(lbl);
		box.add(txt);
		
		return box;
	}
	
	private Box createLabel(String text, JLabel label) {
		Box box = Box.createHorizontalBox();
		box.setAlignmentX(Component.LEFT_ALIGNMENT);
		
		JLabel lbl = label;
		
		return box;
	}
	
	private Box createSlider(final JSlider slider) {
		Box box = Box.createHorizontalBox();
		box.setAlignmentX(Component.LEFT_ALIGNMENT);
		slider.setPreferredSize(new Dimension(204, 30));
		slider.setMaximumSize(slider.getPreferredSize());
		slider.setMinimum(0);
		slider.setMaximum(100);
		slider.setValue(10);

		JLabel title = new JLabel("Wirefeed rate");
		final JLabel endLabel = new JLabel(String.valueOf(slider.getValue()));
		box.add(title);
		box.add(slider);		
		box.add(endLabel);
		
		slider.addChangeListener(new ChangeListener() {
			
			@Override
			public void stateChanged(ChangeEvent e) {
				endLabel.setText(String.valueOf(slider.getValue()));
			}
		});		
		
		return box;
	}
	
	private Box createDropBox(JComboBox<String> combo, String title) {
		Box box = Box.createHorizontalBox();
		box.setAlignmentX(Component.LEFT_ALIGNMENT);
		combo.setPreferredSize(new Dimension(204, 30));
		combo.setMaximumSize(combo.getPreferredSize());
		
		JLabel label = new JLabel(title);
		box.add(label);
		box.add(combo);
		
		return box;
	}	

	private Box createFileChooser() {
		final Box box = Box.createHorizontalBox();
		box.setAlignmentX(Component.LEFT_ALIGNMENT);
		JButton btnFile = new JButton("Choose 3D File");
		final JFileChooser fileChooser = new JFileChooser();
		FileNameExtensionFilter gcodeFilter = new FileNameExtensionFilter("Gcode", "gcode");
		fileChooser.setFileFilter(gcodeFilter);
		fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("Numberal Code", "nc"));
		fileChooser.setCurrentDirectory(new File("/programs"));
		box.add(btnFile);
		
		btnFile.addMouseListener(new MouseListener() {
			
			@Override
			public void mouseReleased(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void mousePressed(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void mouseExited(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void mouseEntered(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void mouseClicked(MouseEvent e) {
				int result = fileChooser.showDialog(box.getParent(), null);
				if (result == JFileChooser.APPROVE_OPTION) {
					File selectedFile = fileChooser.getSelectedFile();
					provider.get().onSelectedFile(selectedFile);
					filenameLabel.setText(selectedFile.getName());
				}
			}
		});
		
		return box;
	}
	
	
	private void showNumberalKeyBoard(final JTextField txt) {
		KeyboardNumberInput<Double> kb = apiProvider.getUserInterfaceAPI().getUserInteraction().getKeyboardInputFactory().createDoubleKeypadInput();
		kb.setInitialValue(Double.valueOf(txt.getText()));
		kb.show(txt, new KeyboardInputCallback<Double>() {
			
			@Override
			public void onOk(Double value) {
				if(value != 0) {
					txt.setText(Double.toString(value));
					if(txt == speedTextField) {
	
						provider.get().onSpeedTxtChanged(value);
					}
					
					if(txt == accelerationTextField) {
	
						provider.get().onAccelerationTxtChanged(value);
					}
				}				
			}
		});
	}
	
	
	//EVENTS
	
	
	private ItemListener triggerCbItemListener = new ItemListener() {
		
		@Override
		public void itemStateChanged(ItemEvent e) {
			provider.get().onTriggerPinChanged(triggerComboBox.getSelectedIndex());
		}
	};
	
	private ItemListener wirefeedCbItemListener = new ItemListener() {
		
		@Override
		public void itemStateChanged(ItemEvent e) {

			provider.get().onWirefeedPinChanged(wirefeedComboBox.getSelectedIndex());
		}
	};
}