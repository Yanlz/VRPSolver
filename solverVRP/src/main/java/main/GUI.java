package main;

import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SpringLayout;

public class GUI extends JPanel implements ActionListener{
	
	private JFrame frame;
	
	private JTabbedPane tabbedPane;
	private JPanel filePanel, otherPanel, firstPanel, secondPanel, thirdPanel, firstStrategy, 
		secondStrategy, firstParameters, secondParameters;
	private JComboBox filesBox, ruinsBox1, ruinsBox2, selectorBox1, selectorBox2,
		insertionBox1, insertionBox2, acceptorBox1, acceptorBox2;
	private JTextField memoryText, iterationsText, shareStart1, shareStart2, shareEnd1, 
	 	shareEnd2, alphaStart1, alphaStart2, alphaEnd1, alphaEnd2,warmupStart1, warmupStart2, 
	 	warmupEnd1, warmupEnd2, probabilityStart1, probabilityStart2, probabilityEnd1, probabilityEnd2;
	private JRadioButton chooseShare1, chooseShare2, chooseProbability1, chooseProbability2, 
    	chooseAlpha1, chooseAlpha2, chooseWarmup1, chooseWarmup2;
	private JButton	button;
	
	private ArrayList<JRadioButton> radioList;
	private ArrayList<JTextField> parametersEndList, parametersStartList;
	private VariableTestGenerator generator;
    
	public GUI(VariableTestGenerator generator, ArrayList<String> instanceFiles, ArrayList<String> ruins, ArrayList<String> selectors,
			ArrayList<String> insertions, ArrayList<String> acceptors) {
		
		super(new GridLayout(1, 1));
		this.generator = generator;
		
		initialize(instanceFiles,ruins,selectors,insertions,acceptors);
		
		tabbedPane = new JTabbedPane();
		add(tabbedPane);
		
		firstPanel = new JPanel(new GridLayout(2,1));
		secondPanel = new JPanel(new GridLayout(2,1));		
		thirdPanel = new JPanel(new GridLayout(0,2));
		tabbedPane.addTab("First Strategy", firstPanel);
		tabbedPane.addTab("Second Strategy", secondPanel);
		tabbedPane.addTab("File", thirdPanel);
		
		firstStrategy = new JPanel(new FlowLayout());
		secondStrategy = new JPanel(new FlowLayout());
		firstPanel.add(firstStrategy);
		secondPanel.add(secondStrategy);
		
		firstParameters = new JPanel(new GridLayout(0,4));
		secondParameters = new JPanel(new GridLayout(0,4));
		firstPanel.add(firstParameters);
		secondPanel.add(secondParameters);
		
		filePanel = new JPanel(new FlowLayout());
		otherPanel = new JPanel(new GridLayout(2,2));
		
		otherPanel.add(new JLabel("Iterations"));
		otherPanel.add(iterationsText);
		otherPanel.add(new JLabel("Memory"));
		otherPanel.add(memoryText);
		filePanel.add(filesBox);
		filePanel.add(button);
		
		thirdPanel.add(otherPanel);
		thirdPanel.add(filePanel);
		
        firstStrategy.add(ruinsBox1);
        firstStrategy.add(selectorBox1);
        firstStrategy.add(insertionBox1);
        firstStrategy.add(acceptorBox1);
        
        secondStrategy.add(ruinsBox2);
        secondStrategy.add(selectorBox2);
        secondStrategy.add(insertionBox2);
        secondStrategy.add(acceptorBox2);   
        
        firstParameters.add(new JLabel("Share"));
        firstParameters.add(shareStart1);      
        firstParameters.add(shareEnd1);
        firstParameters.add(chooseShare1);
        firstParameters.add(new JLabel("Probability"));
        firstParameters.add(probabilityStart1);
        firstParameters.add(probabilityEnd1);
        firstParameters.add(chooseProbability1);
        firstParameters.add(new JLabel("Alpha"));
        firstParameters.add(alphaStart1);
        firstParameters.add(alphaEnd1);
        firstParameters.add(chooseAlpha1);
        firstParameters.add(new JLabel("Warmup"));
        firstParameters.add(warmupStart1);
        firstParameters.add(warmupEnd1);
        firstParameters.add(chooseWarmup1);
 
        secondParameters.add(new JLabel("Share"));
        secondParameters.add(shareStart2);      
        secondParameters.add(shareEnd2);
        secondParameters.add(chooseShare2);
        secondParameters.add(new JLabel("Probability"));
        secondParameters.add(probabilityStart2);
        secondParameters.add(probabilityEnd2);
        secondParameters.add(chooseProbability2);
        secondParameters.add(new JLabel("Alpha"));
        secondParameters.add(alphaStart2);
        secondParameters.add(alphaEnd2);
        secondParameters.add(chooseAlpha2);
        secondParameters.add(new JLabel("Warmup"));
        secondParameters.add(warmupStart2);
        secondParameters.add(warmupEnd2);
        secondParameters.add(chooseWarmup2);
	}

	public void initialize(ArrayList<String> instanceFiles, ArrayList<String> ruins, ArrayList<String> selectors, ArrayList<String> insertions, ArrayList<String> acceptors){
		radioList = new ArrayList<JRadioButton>();
		parametersStartList = new ArrayList<JTextField>();
		parametersEndList = new ArrayList<JTextField>();
		
		memoryText = new JTextField("5");
		iterationsText = new JTextField("1024");
		
		filesBox = new JComboBox(instanceFiles.toArray());
		button = new JButton("Go");
		button.setActionCommand("Go");
		button.addActionListener(this);
		
        ruinsBox1 = new JComboBox(ruins.toArray());
        selectorBox1 = new JComboBox(selectors.toArray());
        insertionBox1 = new JComboBox(insertions.toArray());
        acceptorBox1 = new JComboBox(acceptors.toArray());
        shareStart1 = new JTextField("0.2");
        shareEnd1 = new JTextField("0.8");
        alphaStart1 = new JTextField("0.1");
        alphaEnd1 = new JTextField("0.5");
        warmupStart1 = new JTextField("100");
        warmupEnd1 = new JTextField("1000");
        probabilityStart1 = new JTextField("0.5");
        probabilityEnd1 = new JTextField("1.0");
        chooseShare1 = new JRadioButton();
        chooseProbability1 = new JRadioButton();
        chooseAlpha1 = new JRadioButton();       
        chooseWarmup1 = new JRadioButton();
        
        parametersStartList.add(shareStart1);
        parametersStartList.add(probabilityStart1);
        parametersStartList.add(alphaStart1);
        parametersStartList.add(warmupStart1);
        parametersEndList.add(shareEnd1);
        parametersEndList.add(probabilityEnd1);
        parametersEndList.add(alphaEnd1);
        parametersEndList.add(warmupEnd1);
        radioList.add(chooseShare1);
        radioList.add(chooseProbability1);
        radioList.add(chooseAlpha1);
        radioList.add(chooseWarmup1);
        
        ruinsBox2 = new JComboBox(ruins.toArray());
        ruinsBox2.setSelectedIndex(1);
        selectorBox2 = new JComboBox(selectors.toArray());
        insertionBox2 = new JComboBox(insertions.toArray());
        acceptorBox2 = new JComboBox(acceptors.toArray());
        shareStart2 = new JTextField("0.2");
        shareEnd2 = new JTextField("0.8");
        alphaStart2 = new JTextField("0.1");
        alphaEnd2 = new JTextField("0.5");
        warmupStart2 = new JTextField("100");
        warmupEnd2 = new JTextField("1000");
        probabilityStart2 = new JTextField("0.5");
        probabilityEnd2 = new JTextField("1.0");
        chooseShare2 = new JRadioButton();
        chooseProbability2 = new JRadioButton();      
        chooseAlpha2 = new JRadioButton();      
        chooseWarmup2 = new JRadioButton();
     
        parametersStartList.add(shareStart2);
        parametersStartList.add(probabilityStart2);
        parametersStartList.add(alphaStart2);
        parametersStartList.add(warmupStart2);
        parametersEndList.add(shareEnd2);
        parametersEndList.add(probabilityEnd2);
        parametersEndList.add(alphaEnd2);
        parametersEndList.add(warmupEnd2);
        radioList.add(chooseShare2);
        radioList.add(chooseProbability2);
        radioList.add(chooseAlpha2);
        radioList.add(chooseWarmup2);
        
        acceptorBox1.setActionCommand("schrimpf1");
        acceptorBox1.addActionListener(this);
        
        acceptorBox2.setActionCommand("schrimpf2");
        acceptorBox2.addActionListener(this);
        
        alphaStart1.setEnabled(false);
        alphaStart2.setEnabled(false);
        warmupStart1.setEnabled(false);
        warmupStart2.setEnabled(false);
        chooseAlpha1.setEnabled(false);
        chooseAlpha2.setEnabled(false);
        chooseWarmup1.setEnabled(false);
        chooseWarmup2.setEnabled(false);
        
        for (JRadioButton r: radioList){
        	r.setActionCommand("" + radioList.indexOf(r));
        	r.addActionListener(this);
        }
        
        for (JTextField t: parametersEndList)
        	t.setEnabled(false);      
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		switch (e.getActionCommand()) {
		case "Go":
			Strategy strategy1 = new Strategy((String) selectorBox1.getSelectedItem(), 
					(String) acceptorBox1.getSelectedItem(), (String) ruinsBox1.getSelectedItem(),
					(String) insertionBox1.getSelectedItem(), Double.parseDouble(shareStart1.getText()),
					Double.parseDouble(probabilityStart1.getText()), Double.parseDouble(alphaStart1.getText()),
					Double.parseDouble(warmupStart1.getText()));
			
			Strategy strategy2 = new Strategy((String) selectorBox2.getSelectedItem(), 
					(String) acceptorBox2.getSelectedItem(), (String) ruinsBox2.getSelectedItem(),
					(String) insertionBox2.getSelectedItem(), Double.parseDouble(shareStart2.getText()),
					Double.parseDouble(probabilityStart2.getText()), Double.parseDouble(alphaStart2.getText()),
					Double.parseDouble(warmupStart2.getText()));
			
			int i = 0;
			while (!radioList.get(i).isSelected() && i < radioList.size())
				 i++;
			
			int variable = (i % 4);
			if (i / 4 == 0)
				strategy1.setVariable(variable);
			else
				strategy2.setVariable(variable);
			
			double step = (variable == 3)? 100 : 0.1;
			double start = Double.parseDouble(parametersStartList.get(i).getText());
			double end = Double.parseDouble(parametersEndList.get(i).getText());
			
			int nSteps = (int) ((end - start)/ step) + 1;
			ArrayList<Double> range = new ArrayList<Double>();
			for (int j = 0; j < nSteps; j++) {
				range.add((double)Math.round((start + step * (double)j) * 10d) / 10d);
			}
			
			generator.generate((String) filesBox.getSelectedItem(), iterationsText.getText(), memoryText.getText(), 
					strategy1, strategy2, range);
			break;
		case "schrimpf1":
			if (acceptorBox1.getSelectedItem().equals("schrimpfAcceptance")) {
				alphaStart1.setEnabled(true);
				warmupStart1.setEnabled(true);
				chooseAlpha1.setEnabled(true);
				chooseWarmup1.setEnabled(true);
			} else {
				alphaStart1.setEnabled(false);
				warmupStart1.setEnabled(false);
				chooseAlpha1.setEnabled(false);
				chooseWarmup1.setEnabled(false);
				chooseAlpha1.setSelected(false);
				chooseWarmup1.setSelected(false);
				alphaEnd1.setEnabled(false);
				warmupEnd1.setEnabled(false);
			}
			break;
		case "schrimpf2":
			if (acceptorBox2.getSelectedItem().equals("schrimpfAcceptance")) {
				alphaStart2.setEnabled(true);
				warmupStart2.setEnabled(true);
				chooseAlpha2.setEnabled(true);
				chooseWarmup2.setEnabled(true);
			} else {
				alphaStart2.setEnabled(false);
				warmupStart2.setEnabled(false);
				chooseAlpha2.setSelected(false);
				chooseAlpha2.setEnabled(false);
				chooseWarmup2.setSelected(false);
				chooseWarmup2.setEnabled(false);
				alphaEnd2.setEnabled(false);
				warmupEnd2.setEnabled(false);
			}
			break;
		default:
			int selected = Integer.parseInt(e.getActionCommand());

			for (int j = 0; j < parametersEndList.size(); j++)
				if (j == selected)
					parametersEndList.get(j).setEnabled(!parametersEndList.get(j).isEnabled());
				else {
					parametersEndList.get(j).setEnabled(false);
					radioList.get(j).setSelected(false);
				}
			break;
		}
	}
	
	public void show() {
        //Create and set up the window.
        frame = new JFrame("Jesoo");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        //Create and set up the content pane.
        this.setOpaque(true); //content panes must be opaque
        frame.setContentPane(this);

        //Display the window.
        frame.pack();
        frame.setVisible(true);
	}

	public void dispose() {
		frame.dispose();
	}

}
