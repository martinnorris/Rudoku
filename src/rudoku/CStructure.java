package rudoku;

import java.awt.Component;

public class CStructure
{
	CStructure(DStructure structureDefined)
	{
		m_structureDefined = structureDefined;
	}
	
	DStructure m_structureDefined;
	
	boolean addStructureListener(LStructure listener)
	{
		return m_structureDefined.addStructureListener(listener);
	}
	
	public boolean interpretAction(Component componentSource, AStructure actionPrevious, String scAction) 
	{
		AStructure actionStructure = new AStructure(scAction);
		return actionStructure.actionStructure(componentSource, actionPrevious, m_structureDefined);
	}
	
	public boolean initialSymbolSet()
	{
		// Pass the action along to the controller to create new symbol set and position 0,0
		AStructureInitialSymbol action = new AStructureInitialSymbol("INITIALSYMBOL"); 
		return action.actionStructure(null, null, m_structureDefined);
	}
	
	public boolean addColumn()
	{
		return m_structureDefined.duplicateColumn();
	}
	
	public boolean addRow()
	{
		return m_structureDefined.duplicateRow();
	}
	
	public boolean setPositionSymbol(char cSymbol, int iColumn, int iRow)
	{
		
		return true;
	}
	
	public static void main(String[] args) 
	{
		DStructure structureDefined = new DStructure();
		CStructure structureConfig = new CStructure(structureDefined);
		
		AStructureStandard actionStandard = new AStructureStandard("_default");
		structureConfig.interpretAction(null, actionStandard, "X");
		
		// Fix symbols ...
		
		String ssPreset = ""
/*
 		+ ".8.4.9653"
		+ "6428...7."
		+ "......8.."
		+ "..7..5.42"
		+ "...7.1..."
		+ "85.6..1.."
		+ "..6......"
		+ ".1...4736"
		+ "2735.8.1.";
*/
// Swordfish for 3s
		+ "6...9...2"
		+ ".8.61..9."
		+ "..78..5.."
		+ "......67."
		+ "82.....39"
		+ ".76......"
		+ "..2..14.."
		+ ".3..26.5."
		+ "9...7...1";

/*
		+ "...1...3."
		+ "....8...1"
		+ ".56.37..."
		+ ".....87.."
		+ "2...5.49."
		+ "..79....2"
		+ ".1.3..2.."
		+ "6.9...3.."
		+ "78..6....";
*/
/*
		+ ".......51"
		+ "....1...8"
		+ "..7..64.."
		+ ".2.1.3..7"
		+ "..67.98.."
		+ "8..6.2.4."
		+ "..84..2.."
		+ "3...7...."
		+ "45.......";
*/
/*
        + "...2....."
        + ".6.918.5."
        + "895......"
        + "354......"
        + "2...9...8"
        + "......472"
        + "......627"
        + ".1.329.4."
        + ".....4..."
 */
		AStructureSet actionSet = new AStructureSet(">");
		structureConfig.interpretAction(null, actionSet, ssPreset);
		
		// Some other rules to try ...
		
/*		
		DRuleRestrict ruleRestrictRow = new DRuleRestrictRow();
		structureDefined.addRule(ruleRestrictRow, 0, 1);
		ruleRestrictRow.foundApplication();
		
		DRuleDuplicateSquare ruleDuplicateS = new DRuleDuplicateSquare();
		structureDefined.setRulePosition(ruleDuplicateS, 6, 0);
		ruleDuplicateS.foundUnique();
		
		DRuleDuplicateRow ruleDuplicateR = new DRuleDuplicateRow();
		structureDefined.setRulePosition(ruleDuplicateR, 0, 0);
		ruleDuplicateR.foundUnique();
*/
/*		
		DRulePairs rulePairRows = new DRulePairsInRows();
		DRulePairs rulePairColumns = new DRulePairsInColumns();
		structureDefined.addRule(rulePairRows, 0, 0);
		structureDefined.addRule(rulePairColumns, 0, 0);
*/
/*
		DRulePairsLinked rulePairLinkedRow = new DRulePairsLinkedRow();
		DRulePairsLinked rulePairLinkedColumn = new DRulePairsLinkedColumn();
		structureDefined.addRule(rulePairLinkedRow, 0, 0);
		structureDefined.addRule(rulePairLinkedColumn, 0, 0);
		
		DRuleRestrictLinkedRow ruleRestrictLinkedRow = new DRuleRestrictLinkedRow();
		DRuleRestrictLinkedColumn ruleRestrictLinkedColumn = new DRuleRestrictLinkedColumn();
		structureDefined.addRule(ruleRestrictLinkedRow, 0, 0);
		structureDefined.addRule(ruleRestrictLinkedColumn, 0, 0);
*/
		
		AStructure actionSolve = new AStructureSolve("_solve");
		structureConfig.interpretAction(null, actionSolve, "V");
		
		float fComplete = structureDefined.completePercent();
		System.out.println(String.format("%s\n%2.1f%% complete\n", structureDefined, fComplete));
		
		for (int iIndex = 0; iIndex<9*9; ++iIndex)
		{
			int iRow = (iIndex / 9);
			int iColumn = (iIndex % 9);
			DPosition position = (DPosition)structureDefined.m_dataTable.getValueAt(iRow, iColumn);
			if (position.m_symbolSet.fixedSymbol()) continue;
			System.out.println(String.format("Cell %d,%d [%s]", iColumn+1, iRow+1, position.m_symbolSet.m_scConstrains));
		}
		
	}

}
