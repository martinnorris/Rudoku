package rudoku;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.Border;

public class VStructure extends JPanel implements LStructure
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1717057026106220739L;

	VStructure(CStructure structureControlled)
	{
		m_structureControlled = structureControlled;
		m_structureControlled.addStructureListener(this);
	}
	
	CStructure m_structureControlled;
	
	JPanel createPanel()
	{
		setLayout(new BorderLayout());
		
		// Add buttons to duplicate row/column East and South
		Font fontButtons = new Font(Font.DIALOG, Font.PLAIN, 20);
		
		JButton buttonColumn = new JButton("+");
		buttonColumn.setFont(fontButtons);
		buttonColumn.setActionCommand("ADDCOLUMN");
		
		ActionAdapter actionAddColumn = new ActionAdapter()
		{
			@Override
			public void actionPerformed(ActionEvent actionEvent) 
			{
				// Pass the action along to the controller
				m_structureControlled.addColumn();
				return;
			}
		};
		
		buttonColumn.addActionListener(actionAddColumn);
		
		JButton buttonRow = new JButton("+");
		buttonRow.setFont(fontButtons);
		buttonRow.setActionCommand("ADDROW");
		
		ActionAdapter actionAddRow = new ActionAdapter()
		{
			@Override
			public void actionPerformed(ActionEvent actionEvent) 
			{
				// Pass the action along to the controller
				m_structureControlled.addRow();
				return;
			}
		};
		
		buttonRow.addActionListener(actionAddRow);
		
		Border borderPanels = BorderFactory.createLineBorder(Color.DARK_GRAY);
		Color colorPanels = new Color(0xFF, 0xCC, 0x99);
		
		JPanel panelEast = new JPanel();
		panelEast.setBorder(borderPanels);
		panelEast.setBackground(colorPanels);
		panelEast.add(buttonColumn);
		add(panelEast, BorderLayout.EAST);
		
		JPanel panelSouth = new JPanel();
		panelSouth.setBorder(borderPanels);
		panelSouth.setBackground(colorPanels);
		panelSouth.add(buttonRow);
		add(panelSouth, BorderLayout.SOUTH);
		
		ClassLoader loader = getClass().getClassLoader();		
		ImageIcon imageCross = new ImageIcon(loader.getResource("X-small.png"));
		JLabel labelCross = new JLabel(imageCross);
		
		MouseAdapter adapterLabel = new MouseAdapter()
		{
			@Override
			public void mouseClicked(MouseEvent eventMouse) 
			{
				// Pass the action along to the controller to create new symbol set and position 0,0
				m_structureControlled.initialSymbolSet();
				return;
			}			
		};
		
		labelCross.addMouseListener(adapterLabel);
		
		m_panelCenter = new JPanel();
		m_panelCenter.setBorder(borderPanels);
		m_panelCenter.setBackground(new Color(0xFF, 0xDD, 0xAA));
		m_panelCenter.add(labelCross);
		
		add(m_panelCenter, BorderLayout.CENTER);
		
		return this;
	}
	
	JPanel m_panelCenter;
	
	boolean setTableView()
	{
		Component[] acontent = m_panelCenter.getComponents();
		for (Component component : acontent) m_panelCenter.remove(component);
		
		m_tableView = new JTable(m_structureControlled.m_structureDefined.m_dataTable);
		
		KeyAdapter actionKeys = new KeyAdapter()
		{
			@Override
			public void keyTyped(KeyEvent typedKey)
			{
				setPositionSymbol(typedKey.getKeyChar());
				return;
			}
		};
		m_tableView.addKeyListener(actionKeys);
		
		JScrollPane scrollpane = new JScrollPane(m_tableView);
		m_panelCenter.add(scrollpane);
		
		return true;
	}
	
	JTable m_tableView;
	
	boolean setPositionSymbol(char cSymbol)
	{
		int iRow = 0, iColumn = 0;
		
		iRow = m_tableView.getSelectedRow();
		if (0>iRow) return false;
		
		iColumn = m_tableView.getSelectedColumn();
		if (0>iColumn) return false;
		
		return m_structureControlled.setPositionSymbol(cSymbol, iColumn, iRow);
	}
	
	@Override
	public boolean eventStructure(EStructure enumAction, DStructure structure) 
	{
		return false;
	}
	
	@Override
	public boolean eventPosition(EPosition enumAction, DPosition position, DSymbol symbol) 
	{
		return false;
	}
	@Override
	public boolean eventSymbol(ESymbol enumAction, DSymbol symbol)
	{
		if (ESymbol._INITIAL_SET==enumAction)
		{
			return setTableView();
		}
		return false;
	}
	
	@Override
	public boolean eventRule(ERule enumAction, DRule rule, DPosition position, DSymbol symbol) 
	{
		return true;
	}
	
	public static void main(String[] args) 
	{
		VStructure structureConfig = new VStructure(new CStructure(new DStructure()));
		structureConfig.createPanel();
		Helper.frameView(structureConfig, "Add squares by columns or rows");
	}
}

class ActionAdapter implements ActionListener
{
	@Override
	public void actionPerformed(ActionEvent eventAction) 
	{
		return;
	}
}
