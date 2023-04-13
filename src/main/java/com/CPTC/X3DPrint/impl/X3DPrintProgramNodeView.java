package com.CPTC.X3DPrint.impl;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.text.DecimalFormat;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.ur.urcap.api.contribution.ContributionProvider;
import com.ur.urcap.api.contribution.ViewAPIProvider;
import com.ur.urcap.api.contribution.program.swing.SwingProgramNodeView;
import com.ur.urcap.api.domain.userinteraction.keyboard.KeyboardInputCallback;
import com.ur.urcap.api.domain.userinteraction.keyboard.KeyboardNumberInput;

public class X3DPrintProgramNodeView implements SwingProgramNodeView<X3DPrintProgramNodeContribution>
{
	private JComboBox<String> cbxTrigger = new JComboBox<String>();
	private JComboBox<String> cbxWirefeed = new JComboBox<String>();
	private JSlider sliderWirefeedRate = new JSlider();
	private JLabel lblFilename = new JLabel();	
	private JTextField txtSpeed = new JTextField();
	private JTextField txtAcceleration = new JTextField();
	private JTextField txtBlendRadius = new JTextField();
	private JButton btnSetHome = new JButton("Set Home Position");
	private JLabel lblHomePosition = new JLabel();
	private JCheckBox cbEnableControlSpeed = new JCheckBox();
	private Box speedBox = Box.createVerticalBox();
	
	private final ViewAPIProvider apiProvider;
	private ContributionProvider<X3DPrintProgramNodeContribution> provider;
	
	public X3DPrintProgramNodeView(ViewAPIProvider apiProvider) {
		this.apiProvider = apiProvider;
		
		cbxTrigger.addItemListener(triggerCbItemListener);
		cbxWirefeed.addItemListener(wirefeedCbItemListener);
		cbEnableControlSpeed.addChangeListener(cbEnableControlSpeedChangeListener);
	}
	
	public void setsliderWirefeedRate(int rate) {
		sliderWirefeedRate.setValue(rate);
	}
	
	public void setTriggerSelectedItem(int index) {
		cbxTrigger.setSelectedIndex(index);
	}
	
	public void setTriggerComboItems(String[] items) {
		cbxTrigger.removeAllItems();
		cbxTrigger.setModel(new DefaultComboBoxModel<String>(items));
	}
	
	public void setWirefeedSelectedItem(int index) {
		cbxWirefeed.setSelectedIndex(index);
	}
	
	public void setWirefeedComboItems(String[] items) {
		cbxWirefeed.removeAllItems();
		cbxWirefeed.setModel(new DefaultComboBoxModel<String>(items));
	}
	
	public void setEnableControlSpeed(boolean value) {
		cbEnableControlSpeed.setSelected(value);
		txtSpeed.setEnabled(value);
	}
	
	public void setSpeed(double speed) {
		txtSpeed.setText(String.valueOf(speed));
	}
	
	public void setAcceleration(double accel) {
		txtAcceleration.setText(String.valueOf(accel));
	}
	
	public void setBlendRadius(double radius) {
		txtBlendRadius.setText(String.valueOf(radius));
	}
	
	public void setSelectedFileNameLabel(String filename) {
		lblFilename.setText(filename);
	}
	
	public void setlblHomePosition(double[] positions) {
		String lbl = "Home is not set!";
		if(positions != null) {
			lbl = "[";
			for (double d : positions) {
				DecimalFormat df = new DecimalFormat("#.00");
				lbl+=String.valueOf(df.format(d*1000)) + ", ";
			}
			lbl += "]";
		}
		
		lblHomePosition.setText(lbl);
	}

	@Override
	public void buildUI(JPanel panel, final ContributionProvider<X3DPrintProgramNodeContribution> provider) {
		this.provider = provider;		
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
//		panel.add(createIOSettingBar());
		Box triggerBox = createDropBox(cbxTrigger, "Welder trigger Digital");
		Box wirefeedBox = createDropBox(cbxWirefeed, "Wirefeed Analog");
		
		panel.add(createbtnSetHome());
		panel.add(triggerBox);
		panel.add(wirefeedBox);
		panel.add(createSlider(sliderWirefeedRate));
		panel.add(createCheckBoxEnableSpeedControl(cbEnableControlSpeed));
		
		panel.add(createSpeedBox());
		
		panel.add(createFileChooser());
		panel.add(lblFilename);
	}
	
	private Box createTextbox(final JTextField txt, String label) {
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
	
	private Box createSpeedBox() {
		speedBox.setAlignmentX(Component.LEFT_ALIGNMENT); 
		speedBox.setBorder(BorderFactory.createTitledBorder("Speed control"));
		speedBox.add(createTextbox(txtSpeed, "Tool Speed mm/s"));
		speedBox.add(createTextbox(txtAcceleration, "Tool Acceleration mm/s^2"));
		speedBox.add(createTextbox(txtBlendRadius, "Blend with radius mm"));
		txtSpeed.setEnabled(false);
//		txtAcceleration.setEnabled(false);
		return speedBox;
	}
	
	private Box createCheckBoxEnableSpeedControl(JCheckBox cb) {
		Box box = Box.createHorizontalBox();
		box.setAlignmentX(Component.LEFT_ALIGNMENT);
		
		JLabel lbl = new JLabel("Control speed");
		
		box.add(cb);
		box.add(lbl);
		
		return box;
	}
	
	private Box createLabel(String text, JLabel label) {
		Box box = Box.createHorizontalBox();
		box.setAlignmentX(Component.LEFT_ALIGNMENT);
		
		JLabel lbl = label;
		
		return box;
	}
	
	private Box createbtnSetHome() {
		Box box = Box.createHorizontalBox();
		box.setAlignmentX(Component.LEFT_ALIGNMENT);
		
		btnSetHome.addMouseListener(new MouseListener() {
			
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
				provider.get().onSetHome();
				
			}
		});
		
		box.add(btnSetHome);
		box.add(lblHomePosition);
		
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
				provider.get().onWireFeedRateChange(slider.getValue());
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
					lblFilename.setText(selectedFile.getName());
				}
			}
		});
		
		return box;
	}
	
	
	private void showNumberalKeyBoard(final JTextField txt) {
		KeyboardNumberInput<Double> kb = apiProvider.getUserInterfaceAPI().getUserInteraction().getKeyboardInputFactory().createDoubleKeypadInput();
		kb.setInitialValue(Double.valueOf(txt.getText()));
		if(txt == txtSpeed && cbEnableControlSpeed.isSelected() == false) {
			return;
		}
		kb.show(txt, new KeyboardInputCallback<Double>() {
			
			@Override
			public void onOk(Double value) {
				if(value != 0) {
					txt.setText(Double.toString(value));
					if(txt == txtSpeed) {
						provider.get().onSpeedTxtChanged(value);
					}
					
					if(txt == txtAcceleration) {
	
						provider.get().onAccelerationTxtChanged(value);
					}
					
					if(txt == txtBlendRadius) {
						
						provider.get().onBlendRadiusTxtChanged(value);
					}
				}				
			}
		});
	}
	
	
	//EVENTS
	
	
	private ItemListener triggerCbItemListener = new ItemListener() {
		
		@Override
		public void itemStateChanged(ItemEvent e) {
			provider.get().onTriggerPinChanged(cbxTrigger.getSelectedIndex());
		}
	};
	
	private ItemListener wirefeedCbItemListener = new ItemListener() {
		
		@Override
		public void itemStateChanged(ItemEvent e) {

			provider.get().onWirefeedPinChanged(cbxWirefeed.getSelectedIndex());
		}
	};
	
	private ChangeListener cbEnableControlSpeedChangeListener = new ChangeListener() {
		
		@Override
		public void stateChanged(ChangeEvent e) {
			boolean res = cbEnableControlSpeed.isSelected();
//			txtAcceleration.setEnabled(res);
			txtSpeed.setEnabled(res);
			provider.get().onEnableControlSpeed(res);
		}
	};
}