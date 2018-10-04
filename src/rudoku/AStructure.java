package rudoku;

import java.awt.Component;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Scanner;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

public class AStructure 
{
	AStructure(String scAction)
	{
		m_scAction = scAction;
	}
	
	String m_scAction;
	
	boolean actionStructure(Component componentParent, AStructure actionPrevious, DStructure structureDefined)
	{
		if ("X".contains(m_scAction))
		{
			AStructure actionInitial = new AStructureStandard("");
			return actionInitial.actionStructure(componentParent, actionPrevious, structureDefined);
		}
		if ("S".contains(m_scAction))
		{
			AStructure actionSave = new AStructureSave("");
			return actionSave.actionStructure(componentParent, actionPrevious, structureDefined);
		}
		if ("L".contains(m_scAction))
		{
			AStructure actionLoad = new AStructureLoad("");
			return actionLoad.actionStructure(componentParent, actionPrevious, structureDefined);
		}
		if (containsEachCharacter(m_scAction, ".123456789|>"))
		{
			AStructureSet setPrevious = (AStructureSet)actionPrevious;
			setPrevious.m_scAction = m_scAction;
			return setPrevious.actionStructure(componentParent, actionPrevious, structureDefined);
		}
		if ("V".contains(m_scAction))
		{
			AStructure actionSolve = new AStructureSolve("");
			if (!actionSolve.actionStructure(componentParent, actionPrevious, structureDefined)) return true;
			System.out.println(structureDefined);
			return true;
		}
		if (m_scAction.startsWith("C"))
		{
			AStructure actionCursor = new AStructureCursor(m_scAction);
			return actionCursor.actionStructure(componentParent, actionPrevious, structureDefined);
		}
		if (m_scAction.startsWith("R"))
		{
			AStructure actionRule = new AStructureRule(m_scAction);
			return actionRule.actionStructure(componentParent, actionPrevious, structureDefined);
		}
		
		if ("HELP"==m_scAction)
		{
			System.out.println("other commands 'X' 'L' 'S' '.123456789|>'");
			System.out.println(structureDefined);			
			return true;
		}
		
		JOptionPane.showMessageDialog(componentParent, "Unrecognised command", "AStructure", JOptionPane.WARNING_MESSAGE);
		// Always return true since false stops interaction
		return true;
	}
	
	boolean dialogFail(Component componentSource, String scReason)
	{
		JOptionPane.showMessageDialog(componentSource, scReason, "AStructure", JOptionPane.WARNING_MESSAGE);
		return false;
	}
	
	boolean dialogContinue(Component componentSource, String scReason)
	{
		JOptionPane.showMessageDialog(componentSource, scReason, "AStructure", JOptionPane.INFORMATION_MESSAGE);
		return true;
	}
	
	boolean containsEachCharacter(String scTest, String scCharacters)
	{
		HashMap<Character, Character> mapCharacters = new HashMap<Character, Character>();
		
		for (int iIndex = 0, iCount = scCharacters.length(); iIndex<iCount; ++iIndex)
		{
			mapCharacters.put(scCharacters.charAt(iIndex), null);
		}
		
		for (int iIndex = 0, iCount = scTest.length(); iIndex<iCount; ++iIndex)
		{
			if (mapCharacters.containsKey(scTest.charAt(iIndex))) continue;
			return false;
		}
		
		return true;
	}
}

class AStructureInitialSymbol extends AStructure
{
	AStructureInitialSymbol(String scAction)
	{
		super(scAction);
	}
	
	@Override
	boolean actionStructure(Component componentParent, AStructure actionPrevious, DStructure structureDefined)
	{
		// Create first position and symbol set
		DPosition position_0_0 = new DPosition();
		
		DSymbol symbolSet = new DSymbol();
		if (!position_0_0.setSymbol(symbolSet)) return dialogFail(componentParent, "Could not set symbols");
		
		if (!structureDefined.addPosition(position_0_0)) return dialogFail(componentParent, "Could not add first position");
		
		return true;
	}
}

class AStructureStandard extends AStructure
{
	AStructureStandard(String scAction)
	{
		super(scAction);
	}
	
	@Override
	boolean actionStructure(Component componentParent, AStructure actionPrevious, DStructure structureDefined)
	{
		int iColumns = structureDefined.m_dataTable.getColumnCount();
		if (0==iColumns) return createStructure(componentParent, actionPrevious, structureDefined);
		
		int iRows = structureDefined.m_dataTable.getRowCount();
		if (0==iRows) return createStructure(componentParent, actionPrevious, structureDefined);
		
		if (!structureDefined.dropRules()) return dialogFail(componentParent, "Could not reset rules");
		return createRules(structureDefined);		
	}
	
	boolean createStructure(Component componentParent, AStructure actionPrevious, DStructure structureDefined)
	{
		AStructureInitialSymbol initialSymbols = new AStructureInitialSymbol(m_scAction);
		if (!initialSymbols.actionStructure(componentParent, actionPrevious, structureDefined)) return dialogFail(componentParent, "Could not create structure");
		
		for (int iIndex = 1; iIndex<9; ++iIndex)
		{
			// Duplicate x8 for the other columns
			if (!structureDefined.duplicateColumn()) return dialogFail(null, String.format("Could not add column %d", iIndex));		
			// Duplicate x8 for the other rows
			if (!structureDefined.duplicateRow()) return dialogFail(null, String.format("Could not add row %d", iIndex));
		}
		
		return createRules(structureDefined);
	}
	
	boolean createRules(DStructure structureDefined)
	{
		for (int iIndex = 0; iIndex<9; ++iIndex)
		{
			int iRow = (iIndex / 3) * 3; // 0, 0, 0, 3, 3, 3, 6, 6, 6
			int iColumn = (iIndex % 3) * 3; // 0, 3, 6, 0, 3, 6, 0, 3, 6
			
			// Add group 9 rule
			DRuleSquare ruleSquare = new DRuleSquare();
			structureDefined.addRule(ruleSquare, iRow, iColumn);
			
			DRuleUnique ruleUniqueSquare = new DRuleUniqueSquare();
			structureDefined.addRule(ruleUniqueSquare, iRow, iColumn);
			
			DRuleDuplicate ruleSameSquare = new DRuleDuplicateSquare();
			structureDefined.addRule(ruleSameSquare, iRow, iColumn);
			
			DRuleRestrict ruleRestrictSquare = new DRuleRestrictSquare();
			structureDefined.addRule(ruleRestrictSquare, iRow, iColumn);
		
			// Add row 9 rule
			DRuleRow ruleRow = new DRuleRow();
			structureDefined.addRule(ruleRow, 0, iIndex);
			
			DRuleUnique ruleUniqueRow = new DRuleUniqueRow();
			structureDefined.addRule(ruleUniqueRow, 0, iIndex);
			
			DRuleDuplicate ruleSameRow = new DRuleDuplicateRow();
			structureDefined.addRule(ruleSameRow, 0, iIndex);
			
			DRuleRestrict ruleRestrictRow = new DRuleRestrictRow();
			structureDefined.addRule(ruleRestrictRow, 0, iIndex);
			
			// Add column 9 rule
			DRuleColumn ruleColumn = new DRuleColumn();
			structureDefined.addRule(ruleColumn, iIndex, 0);
			
			DRuleUnique ruleUniqueColumn = new DRuleUniqueColumn();
			structureDefined.addRule(ruleUniqueColumn, iIndex, 0);
			
			DRuleDuplicate ruleSameColumn = new DRuleDuplicateColumn();
			structureDefined.addRule(ruleSameColumn, iIndex, 0);

			DRuleRestrict ruleRestrictColumn = new DRuleRestrictColumn();
			structureDefined.addRule(ruleRestrictColumn, iIndex, 0);
		}
		
		// Add constraint restriction
		
		DRule ruleSquaresAndRows = new DRuleSquareAndRows();
		structureDefined.addRule(ruleSquaresAndRows, 0, 0);
		
		DRule ruleRowAndSquares = new DRuleRowAndSquares();
		structureDefined.addRule(ruleRowAndSquares, 0, 0);
		
		DRule ruleSquaresAndColumns = new DRuleSquareAndColumns();
		structureDefined.addRule(ruleSquaresAndColumns, 0, 0);
		
		DRule ruleColumnAndSquares = new DRuleColumnAndSquares();
		structureDefined.addRule(ruleColumnAndSquares, 0, 0);

		// Add XZ -> XY -> YZ restriction
		
		DRulePairsLinked rulePairLinkedRow = new DRulePairsLinkedRow();
		structureDefined.addRule(rulePairLinkedRow, 0, 0);

		DRulePairsLinked rulePairLinkedColumn = new DRulePairsLinkedColumn();
		structureDefined.addRule(rulePairLinkedColumn, 0, 0);

		// Add 2x2 3x3 restrictions

		DRuleRestrictLinkedRow ruleRestrictLinkedRow = new DRuleRestrictLinkedRow();
		structureDefined.addRule(ruleRestrictLinkedRow, 0, 0);

		DRuleRestrictLinkedColumn ruleRestrictLinkedColumn = new DRuleRestrictLinkedColumn();
		structureDefined.addRule(ruleRestrictLinkedColumn, 0, 0);
		
		return true;
	}
}

class AStructureSolve extends AStructure
{
	AStructureSolve(String scAction)
	{
		super(scAction);
	}
	
	@Override
	boolean actionStructure(Component componentParent, AStructure actionPrevious, DStructure structureDefined)
	{
		while (foundRule(structureDefined))
		{
			commitPosition(structureDefined);
			if (structureDefined.isComplete()) return true;
		}
		return true;
	}
	
	boolean foundRule(DStructure structureDefined)
	{
		for (boolean zRuleReady = false;; zRuleReady = true)
		{
			if (!structureDefined.foundConstrainedRule()) return zRuleReady;
		}
	}
	
	boolean commitPosition(DStructure structureDefined)
	{
		for (boolean zPositionReady = false;; zPositionReady = true)
		{
			if (!structureDefined.commitUnique()) return zPositionReady;
		}		
	}
}

class AStructureFile extends AStructure
{
	AStructureFile(String scAction) 
	{
		super(scAction);
		if (null==m_chooseFile) setFileChooser();
	}
	
	static JFileChooser m_chooseFile = null;
	
	static void setFileChooser()
	{
		if (null!=m_chooseFile) return;
		
		m_chooseFile = new JFileChooser();

		File fileCurrentDirectory = new File(".");
		m_chooseFile.setCurrentDirectory(fileCurrentDirectory);
		
		return;
	}
}

class AStructureSave extends AStructureFile
{
	AStructureSave(String scAction) 
	{
		super(scAction);
	}

	@Override
	boolean actionStructure(Component componentParent, AStructure actionPrevious, DStructure structureDefined)
	{
		int iResponse = m_chooseFile.showSaveDialog(componentParent);
		if (JFileChooser.APPROVE_OPTION==iResponse) return saveFileAndClose(structureDefined);
		return false;
	}
	
	boolean saveFileAndClose(DStructure structureDefined)
	{
		try
		{
			saveFile(structureDefined);
		}
		catch (IOException x)
		{
			System.err.println("Failed to save to file");
			return false;
		}
		
		return true;
	}
	
	boolean saveFile(DStructure structureDefined) throws IOException
	{
		File file = m_chooseFile.getSelectedFile();
		
		ObjectOutputStream outObject = new ObjectOutputStream(new FileOutputStream(file));
		outObject.writeObject(structureDefined);
		outObject.close();
		
		return true;
	}
}

class AStructureLoad extends AStructureFile
{
	AStructureLoad(String scAction) 
	{
		super(scAction);
	}

	@Override
	boolean actionStructure(Component componentParent, AStructure actionPrevious, DStructure structureDefined)
	{
		int iResponse = m_chooseFile.showOpenDialog(componentParent);
		if (JFileChooser.APPROVE_OPTION==iResponse) return loadFileAndClose(structureDefined);
		return false;
	}
	
	boolean loadFileAndClose(DStructure structureDefined)
	{
		try
		{
			loadFile(structureDefined);
		}
		catch (IOException x)
		{
			System.err.println("Failed to save to file");
			return false;
		}
		
		return true;
	}
	
	boolean loadFile(DStructure structureDefined) throws IOException
	{
		File file = m_chooseFile.getSelectedFile();

		ObjectInputStream inObject = new ObjectInputStream(new FileInputStream(file));
		
		try 
		{
			Object oLoaded = inObject.readObject();
			DStructure structureLoaded = (DStructure)oLoaded;
			structureDefined.setLoaded(structureLoaded);
			System.out.println(String.format("Loaded\n%s", structureDefined));
		} 
		catch (ClassNotFoundException e) 
		{
			return false;
		}
		finally
		{
			inObject.close();
		}
		
		return true;		
	}
}
	
class AStructureSet extends AStructure
{
	AStructureSet(String scAction) 
	{
		super(scAction);
		m_iRow = m_iColumn = 0;
	}
	
	int m_iRow;
	int m_iColumn;
	
	@Override
	boolean actionStructure(Component componentParent, AStructure actionPrevious, DStructure structureDefined)
	{
		for (int iIndex = 0, iCount = m_scAction.length(); iIndex<iCount; ++iIndex)
		{
			if (!actionSymbol(m_scAction.charAt(iIndex), structureDefined)) return false;
		}
		return true;
	}
	
	boolean actionSymbol(char cSymbol, DStructure structureDefined)
	{
		if ('.'==cSymbol) return nextAction();
		if ('|'==cSymbol) return resetAction();
		if ('>'==cSymbol) return resetAction();
		
		DSymbol symbolFix = new DSymbol(cSymbol);
		if (!structureDefined.fixSymbol(symbolFix, m_iColumn, m_iRow)) return false;
		
		return nextAction();		
	}
	
	boolean nextAction()
	{
		m_iColumn = m_iColumn+1;
		m_iRow = m_iRow+(m_iColumn/9);
		
		m_iColumn %= 9;
		m_iRow %= 9;
		
		return true;
	}
	
	boolean nextRow()
	{
		m_iColumn = 0;
		m_iRow = m_iRow+1;
		m_iRow %= 9;
		
		return true;
	}
	
	boolean resetAction()
	{
		m_iRow = m_iColumn = 0;
		return true;
	}
}

class AStructureCursor extends AStructureSet
{
	AStructureCursor(String scAction) 
	{
		super(scAction);
	}
	
	@Override
	boolean actionStructure(Component componentParent, AStructure actionPrevious, DStructure structureDefined)
	{
		AStructureSet actionSet = (AStructureSet)actionPrevious;
		
		Scanner scanner = new Scanner(m_scAction);
		
		try
		{
			scanner.next(); // Strip the 'C'
			actionSet.m_iColumn = scanner.nextInt() - 1;
			actionSet.m_iRow = scanner.nextInt() - 1;
		}
		finally
		{
			scanner.close();
		}
		
		return true;
	}
}

class AStructureRule extends AStructure
{
	AStructureRule(String scAction) 
	{
		super(scAction);
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	boolean actionStructure(Component componentParent, AStructure actionPrevious, DStructure structureDefined)
	{
		AStructureSet actionSet = (AStructureSet)actionPrevious;
		
		Scanner scanner = new Scanner(m_scAction);
		
		try
		{
			scanner.next(); // Strip the 'R'
			m_scAction = "rudoku." + scanner.next(); // Just want the naked class name
			Class clazz = Class.forName(m_scAction);
			Class[] parameterTypes = {};
			Constructor constructor = clazz.getConstructor(parameterTypes);
			Object[] parameterValues = {};
			DRule rule = (DRule)constructor.newInstance(parameterValues);
			
			if (!structureDefined.addRule(rule, actionSet.m_iColumn, actionSet.m_iRow)) return dialogContinue(componentParent, "Error adding rule");
		} 
		catch (ClassNotFoundException e) 
		{
			return dialogContinue(componentParent, String.format("Could not find rule '%s'", m_scAction));
		} 
		catch (InstantiationException e) 
		{
			return dialogContinue(componentParent, String.format("Could not create rule '%s'", m_scAction));
		} 
		catch (IllegalAccessException e) 
		{
			return dialogContinue(componentParent, String.format("Rule '%s' not accessable", m_scAction));
		} 
		catch (NoSuchMethodException e) 
		{
			return dialogContinue(componentParent, String.format("Rule '%s' wrong arguments", m_scAction));
		} 
		catch (SecurityException e) 
		{
			return dialogContinue(componentParent, String.format("Rule '%s' is secured", m_scAction));
		} 
		catch (IllegalArgumentException e) 
		{
			return dialogContinue(componentParent, String.format("Rule '%s' bad arguments", m_scAction));
		} 
		catch (InvocationTargetException e) 
		{
			return dialogContinue(componentParent, String.format("Rule '%s' cannot be created", m_scAction));
		}
		finally
		{
			scanner.close();
		}
		
		return true;
	}
}
