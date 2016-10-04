//Simulation for particle interaction
//Written by: Tom Bock
//Finished: 22/09/15
import java.awt.EventQueue;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.*;

public class Simulation extends JFrame implements ActionListener, ChangeListener {
	private int fps = 60;
	private int frameCount = 0;
	private double hydro = 0;
	private JButton startButton = new JButton("Start");
	private JButton quitButton = new JButton("Quit");
	private JButton pauseButton = new JButton("Pause");
	private JButton setTempButton = new JButton("Set Temp");
	private JButton saveButton = new JButton("Save");
	private JButton loadButton = new JButton("Load");
	private boolean fastRun = false;
	private JTextField beadTextField = new JTextField("50",3);
	private JTextField chainTextField = new JTextField("4",3);
	private JTextField tempTextField = new JTextField("0.4",3);
	private JTextField patternTextField = new JTextField("1110",3);
	private JLabel beadLabel = new JLabel("Molecules");
	private JLabel chainLabel = new JLabel("Chain Length");
	private JLabel tempLabel = new JLabel("Temperature");
	private JLabel patternLabel = new JLabel("Pattern");
	private JSlider bigPHydro = new JSlider(JSlider.HORIZONTAL, -5, 5, 0);
	private JComboBox saveList = new JComboBox(new String [] {"System 1","System 2","System 3","System 4","System 5"});
	private Box box = Box.createVerticalBox();
	private boolean running = false;
	private boolean paused = false;
	private String chainPattern;
	private boolean load = false;
	Surface surface = new Surface();
	Thread loop;
	JFrame frame; 
	public Simulation() {
		initUI();
	}

	private void initUI() {
		Container cp = getContentPane();
		cp.setLayout(new BorderLayout());
		JPanel p = new JPanel();
		//Various size constraints so that these things all fit nicely into the box
		tempTextField.setMaximumSize(new Dimension(Integer.MAX_VALUE,tempTextField.getPreferredSize().height));
		beadTextField.setMaximumSize(new Dimension(Integer.MAX_VALUE,beadTextField.getPreferredSize().height));
		chainTextField.setMaximumSize(new Dimension(Integer.MAX_VALUE,chainTextField.getPreferredSize().height));
		patternTextField.setMaximumSize(new Dimension(Integer.MAX_VALUE,patternTextField.getPreferredSize().height));
		bigPHydro.setMajorTickSpacing(5);
		bigPHydro.setMinorTickSpacing(1);
		bigPHydro.setPaintTicks(true);
		bigPHydro.setPaintLabels(true);
		//Needs to be played around with
		bigPHydro.setPreferredSize(new Dimension (10,50));
		box.add(Box.createVerticalGlue());
		box.add(tempLabel);
		box.add(tempTextField);
		box.add(setTempButton);
		box.add(beadLabel);
		box.add(beadTextField);
		box.add(chainLabel);
		box.add(chainTextField);
		box.add(patternLabel);
		box.add(patternTextField);
		box.add(bigPHydro);
		box.add(saveList);
		box.add(loadButton);
		box.add(Box.createVerticalGlue());
		p.setLayout(new GridLayout(1,2));
		p.add(startButton);
		p.add(pauseButton);
		p.add(quitButton);
		//p.add(saveButton);
		cp.add(surface, BorderLayout.CENTER);
		cp.add(p, BorderLayout.SOUTH);
		cp.add(box, BorderLayout.EAST);
		setTitle("Simulation");
		setSize(600, 550);
		setResizable(false);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		//ALL YOUR LISTENERS ARE BELONG TO US
		loadButton.addActionListener(this);
		startButton.addActionListener(this);
		quitButton.addActionListener(this);
		pauseButton.addActionListener(this);
		setTempButton.addActionListener(this);
		saveButton.addActionListener(this);
		bigPHydro.addChangeListener(this);
		saveList.addActionListener(this);
	}

	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {

				Simulation ex = new Simulation();            	
				ex.setVisible(true);
				ex.runSimulationLoop();
			}
		});
	}

	public void actionPerformed(ActionEvent e){
		Object s = e.getSource();
		//Start button being pressed will also automatically take in the inputs of
		//all the JTextFields so as to provide starting conditions
		if (s == loadButton){
			if(!running){
				load = true;
			}
		}
		if (s == startButton){
			//System.out.println("");
			running = !running;
			if (running){
				//Firstly make most textfields uneditable so they cannot be changed
				//while the simulation is running as this would cause problems
				startButton.setText("Stop");
				chainTextField.setEditable(false);
				beadTextField.setEditable(false);
				patternTextField.setEditable(false);
				if(!load){
					int c = 0;
					int b = 0;
					String p = patternTextField.getText();
					chainPattern = p;
					//Simply disallows anything but a number to be given in the temperature field by
					//essentially just "Stop"ing the simulation if this happens and displaying a message
					try{
						double t = Double.parseDouble(tempTextField.getText());
						surface.setTemperature(t);
					}
					catch (NumberFormatException ignore){
						JOptionPane.showMessageDialog(frame,
								"Temperature must be a number (decimals allowed).");
						running = !running;
						startButton.setText("Start");
						chainTextField.setEditable(true);
						beadTextField.setEditable(true);
						patternTextField.setEditable(true);
					}
					//Disallows anything but integers to be given as a chain length by 
					//essentially just "Stop"ing the simulation if this happens and displaying a message
					try{
						c = Integer.parseInt(chainTextField.getText());
					}
					catch (NumberFormatException ignore){
						JOptionPane.showMessageDialog(frame,
								"Chain length must be a integer.");
						running = !running;
						startButton.setText("Start");
						chainTextField.setEditable(true);
						beadTextField.setEditable(true);
						patternTextField.setEditable(true);
					}
					//Disallows anything but integers to be given as the number of molecules by 
					//essentially just "Stop"ing the simulation if this happens and displaying a message
					try{
						b = Integer.parseInt(beadTextField.getText());
					}
					catch (NumberFormatException ignore){
						JOptionPane.showMessageDialog(frame,
								"Bead number must be a integer.");
						running = !running;
						startButton.setText("Start");
						chainTextField.setEditable(true);
						beadTextField.setEditable(true);
						patternTextField.setEditable(true);
					}

					//Ensures that the chain length is not below 0
					if (c > -1){
						surface.setChainLength(c);
					}
					else{
						JOptionPane.showMessageDialog(frame,
								"Error: Negative chain length not allowed!");
						running = !running;
						startButton.setText("Start");
						chainTextField.setEditable(true);
						beadTextField.setEditable(true);
						patternTextField.setEditable(true);
					}
					//Ensures that the chain length cannot be higher than the length of the pattern
					if ((p.length() >= c) && (p.length() > 0)){
						surface.setChainPattern(p);
					}
					else{
						JOptionPane.showMessageDialog(frame,
								"Error: Pattern length is smaller than chain length!");
						running = !running;
						startButton.setText("Start");
						chainTextField.setEditable(true);
						beadTextField.setEditable(true);
						patternTextField.setEditable(true);
					}
					//This is mostly just about letting the user know about things that could have been
					//entered wrong but that do not necessarily affect the running of the program
					surface.setBeads((b*c));
				}
				load = false;
				runSimulationLoop();
			}
			//If the button was used as a "Stop" command
			else{
				startButton.setText("Start");
				chainTextField.setEditable(true);
				beadTextField.setEditable(true);
				patternTextField.setEditable(true);
			}
		}

		else if (s == pauseButton){
			paused = !paused;
			if (paused){
				pauseButton.setText("Unpause");
			}
			else{
				pauseButton.setText("Pause");
			}
		}
		else if (s == quitButton){
			System.exit(0);
		}
		else if (s == setTempButton){
			double t = Double.parseDouble(tempTextField.getText());
			surface.setTemperature(t);
		}
		else if (s == saveButton){
			JOptionPane.showMessageDialog(frame,
					"Positions Saved");
			surface.saveLoadOut(chainPattern);
		}
		String saveFile = (String)saveList.getSelectedItem();
		if (saveFile.equals("System 1")){
			if(load&&(!running)){
				surface.loadSave1();
				tempTextField.setText(surface.getTemperature()+"");
				beadTextField.setText(surface.getMolecules()+"");
				chainTextField.setText(surface.getChainLength()+"");
				patternTextField.setText(surface.getPatternString());
				bigPHydro.setValue(surface.getBigParticleInteraction());
				surface.repaint();
			}
		}
		if (saveFile.equals("System 2")){
			if(load&&(!running)){
				surface.loadSave2();
				tempTextField.setText(surface.getTemperature()+"");
				beadTextField.setText(surface.getMolecules()+"");
				chainTextField.setText(surface.getChainLength()+"");
				patternTextField.setText(surface.getPatternString());
				bigPHydro.setValue(surface.getBigParticleInteraction());
				surface.repaint();
			}
		}
		if (saveFile.equals("System 3")){
			if(load&&(!running)){
				surface.loadSave3();
				tempTextField.setText(surface.getTemperature()+"");
				beadTextField.setText(surface.getMolecules()+"");
				chainTextField.setText(surface.getChainLength()+"");
				patternTextField.setText(surface.getPatternString());
				bigPHydro.setValue(surface.getBigParticleInteraction());
				surface.repaint();
			}
		}
		if (saveFile.equals("System 4")){
			if(load&&(!running)){
				surface.loadSave4();
				tempTextField.setText(surface.getTemperature()+"");
				beadTextField.setText(surface.getMolecules()+"");
				chainTextField.setText(surface.getChainLength()+"");
				patternTextField.setText(surface.getPatternString());
				bigPHydro.setValue(surface.getBigParticleInteraction());
				surface.repaint();
			}
		}
		if (saveFile.equals("System 5")){
			if(load&&(!running)){
				surface.loadSave5();
				tempTextField.setText(surface.getTemperature()+"");
				beadTextField.setText(surface.getMolecules()+"");
				chainTextField.setText(surface.getChainLength()+"");
				patternTextField.setText(surface.getPatternString());
				bigPHydro.setValue(surface.getBigParticleInteraction());
				surface.repaint();
			}
		}

	}

	//Runs sim loop in another thread
	public void runSimulationLoop(){
		loop = new Thread(){
			public void run(){
				if (fastRun){
					badSimulationLoop();
				}
				else{
					simulationLoop();
				}
			}
		};
		loop.start();
	}

	//run-as-fast-as-possible loop 
	private void badSimulationLoop(){
		while (running){
			if (!paused){
				updateSim();
				drawSim();

			}
		}
	}
	//run at fixed timestep loop
	private void simulationLoop(){
		final double SIMULATION_HERTZ = 300.0;
		//Calculate how many ns each frame should take for our target game hertz.
		final double TIME_BETWEEN_UPDATES = 1000000000 / SIMULATION_HERTZ;
		//At the very most we will update the simulation this many times before a new render.
		final int MAX_UPDATES_BEFORE_RENDER = 5;
		double lastUpdateTime = System.nanoTime();
		double lastRenderTime = System.nanoTime();
		final double TARGET_FPS = 160;
		final double TARGET_TIME_BETWEEN_RENDERS = 1000000000 / TARGET_FPS;

		//Simple way of finding FPS.
		int lastSecondTime = (int) (lastUpdateTime / 1000000000);

		while (running){
			double now = System.nanoTime();;
			int updateCount = 0;

			if (!paused){
				//Do as many updates as we need to, potentially playing catchup.
				while( now - lastUpdateTime > TIME_BETWEEN_UPDATES && updateCount < MAX_UPDATES_BEFORE_RENDER ){
					updateSim();
					lastUpdateTime += TIME_BETWEEN_UPDATES;
					updateCount++;
				}
				//If for some reason an update takes forever, we don't want to do an insane number of catchups.
				if ( now - lastUpdateTime > TIME_BETWEEN_UPDATES){
					lastUpdateTime = now - TIME_BETWEEN_UPDATES;
				}
				//Draw (or Render if you prefer)
				drawSim();
				lastRenderTime = now;
				//Update the frames we got.
				int thisSecond = (int) (lastUpdateTime / 1000000000);
				if (thisSecond > lastSecondTime){
					fps = frameCount;
					frameCount = 0;
					lastSecondTime = thisSecond;
				}

				//Yield until it has been at least the target time between renders
				while ( now - lastRenderTime < TARGET_TIME_BETWEEN_RENDERS && now - lastUpdateTime < TIME_BETWEEN_UPDATES){
					Thread.yield();            
					now = System.nanoTime();
				}
			}
			else if(paused){
				Thread.yield();
			}
		}
	}
	private void updateSim(){
		surface.update();
	}

	private void drawSim(){
		surface.repaint();
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		JSlider source = (JSlider)e.getSource();
		if (!source.getValueIsAdjusting()) {
			hydro = (double)source.getValue();
			surface.setBigParticleInteraction(hydro);
		}
	}

}