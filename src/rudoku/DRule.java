package rudoku;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import rudoku.LStructure.ERule;

public class DRule implements Serializable
{
	private static final long serialVersionUID = 7147867871613652413L;

	DRule()
	{
		m_iOffsetCount = 0;
		m_listenerStructure = new LStructureAdapter();
		m_scName = super.toString();
	}
	
	int m_iOffsetCount;
	transient LStructure m_listenerStructure;
	String m_scName;
	
	boolean addRuleListener(LStructure listener)
	{
		m_listenerStructure = listener;
		return true;
	}
	
	LStructure removeRuleListener()
	{
		LStructure listener = m_listenerStructure;
		m_listenerStructure = new LStructureAdapter();
		return listener;
	}
	
	int getOffsetCount()
	{
		return m_iOffsetCount;
	}
	
	int getOffsetColumn(int iIndex, int iAbsolute)
	{
		return 0;
	}
	
	int getOffsetRow(int iIndex, int iAbsolute)
	{
		return 0;
	}
	
	boolean setReferencePosition(int iIndex, DPosition positionReference)
	{
		return true;
	}
	
	boolean findIntersectingRules(List<DRule> listRules)
	{
		return true;
	}
	
	boolean foundApplication()
	{
		return false;
	}
	
	String setName(int iColumn, int iRow)
	{
		m_scName = String.format("%s at %d,%d in %c ", super.toString(), iColumn, iRow, DPosition.getSquare(iColumn, iRow));
		return m_scName;
	}
	
	@Override
	public String toString()
	{
		return m_scName;
	}
}

class DRuleContains extends DRule
{
	private static final long serialVersionUID = -5479698330806775456L;

	DRuleContains()
	{
		m_ruleConfig = null;
	}
	
	DRuleSquare m_ruleConfig;
	
	@Override
	int getOffsetCount()
	{
		return m_ruleConfig.m_iOffsetCount;
	}	
	
	@Override
	int getOffsetColumn(int iIndex, int iAbsolute)
	{
		return m_ruleConfig.getOffsetColumn(iIndex, iAbsolute);
	}
	
	@Override
	int getOffsetRow(int iIndex, int iAbsolute)
	{
		return m_ruleConfig.getOffsetRow(iIndex, iAbsolute);
	}

	@Override
	boolean setReferencePosition(int iIndex, DPosition positionReference)
	{
		return m_ruleConfig.setReferencePosition(iIndex, positionReference);
	}
	
	@Override
	String setName(int iColumn, int iRow)
	{
		m_scName = m_ruleConfig.setName(iColumn, iRow);
		return m_scName;
	}
}

/**
 * Excludes fixed symbol from other places in the same Square/Row/Column
 * e.g. [123] [123] [123] [123456789] [123456789] [123456789] [123456789] [123456789] <9>
 * where <9> is fixed, it is removed from the list of symbols for all the other cells
 */
 
class DRuleSquare extends DRule
{
	private static final long serialVersionUID = 7200027120467382603L;

	DRuleSquare()
	{
		m_iOffsetCount = 9;
		m_aiColumnOffset = new int[] {0, 1, 2, 0, 1, 2, 0, 1, 2};
		m_aiRowOffset = new int[] {0, 0, 0, 1, 1, 1, 2, 2, 2};
		m_apositionReference = new DPosition[m_iOffsetCount];
	}
	
	int[] m_aiColumnOffset;
	int[] m_aiRowOffset;
	
	@Override
	int getOffsetColumn(int iIndex, int iAbsolute)
	{
		return iAbsolute+m_aiColumnOffset[iIndex];
	}
	
	@Override
	int getOffsetRow(int iIndex, int iAbsolute)
	{
		return iAbsolute+m_aiRowOffset[iIndex];
	}
	
	@Override
	boolean setReferencePosition(int iIndex, DPosition positionReference)
	{
		m_apositionReference[iIndex] = positionReference;
		return true;
	}
	
	DPosition[] m_apositionReference;
	
	@Override
	boolean foundApplication()
	{
		return foundFixedSymbol(false, 0);
	}
	
	boolean foundFixedSymbol(boolean zReturn, int iIndex)
	{
		for (; iIndex<m_iOffsetCount; ++iIndex)
		{
			if (!m_apositionReference[iIndex].fixedSymbol()) continue;
			// Exclude fixed symbol in all other members of the rule
			DSymbol symbolFixed = m_apositionReference[iIndex].getSymbolSet();
			if (!excludeSymbol(false, symbolFixed, 0)) continue;
			return foundFixedSymbol(true, ++iIndex);
		}
		return zReturn;		
	}
	
	boolean excludeSymbol(boolean zReturn, DSymbol symbolFixed, int iIndex)
	{
		for (; iIndex<m_iOffsetCount; ++iIndex)
		{
			DPosition position = m_apositionReference[iIndex];
			if (position.fixedSymbol()) continue;
			if (!position.excludeSymbol(symbolFixed)) continue;
			if (!m_listenerStructure.eventRule(ERule._APPLY_REMOVE, this, position, symbolFixed)) continue;
			return excludeSymbol(true, symbolFixed, ++iIndex);
		}
		return zReturn;
	}
	
	@Override
	String setName(int iColumn, int iRow)
	{
		m_scName = String.format("Square '%c' ", DPosition.getSquare(iColumn, iRow));
		return m_scName;
	}
}

class DRuleRow extends DRuleSquare
{
	private static final long serialVersionUID = -7481985170186875886L;

	DRuleRow()
	{
		m_iOffsetCount = 9;
		m_aiColumnOffset = new int[] {0, 1, 2, 3, 4, 5, 6, 7, 8};
		m_aiRowOffset = new int[] {0, 0, 0, 0, 0, 0, 0, 0, 0};
		m_apositionReference = new DPosition[m_iOffsetCount];
	}
	
	@Override
	String setName(int iColumn, int iRow)
	{
		m_scName = String.format("Row %d ", iRow+1);
		return m_scName;
	}
}

class DRuleColumn extends DRuleSquare
{
	private static final long serialVersionUID = 1120153146162943844L;

	DRuleColumn()
	{
		m_iOffsetCount = 9;
		m_aiColumnOffset = new int[] {0, 0, 0, 0, 0, 0, 0, 0, 0};
		m_aiRowOffset = new int[] {0, 1, 2, 3, 4, 5, 6, 7, 8};
		m_apositionReference = new DPosition[m_iOffsetCount];
	}
	
	@Override
	String setName(int iColumn, int iRow)
	{
		m_scName = String.format("Column %d ", iColumn+1);
		return m_scName;
	}
}

/**
 * Where a symbol is the only occurrence in a group of cells then all other symbols in that cell are removed
 * e.g. [123] [123] [123] [45678] [5678] [5678] [5678] [5678] <9>
 * cell containing [4] set as only symbol for position
 */
 
class DRuleUnique extends DRuleContains
{
	private static final long serialVersionUID = 5890071532589648269L;

	@Override
	boolean foundApplication()
	{
		DPosition[] aposition = m_ruleConfig.m_apositionReference;
		int iCount = m_ruleConfig.m_iOffsetCount;
		
		if (null==aposition) return false;
		if (null==aposition[0]) return false;
		DSymbol symbolCheck = aposition[0].getSymbolSet().allSymbols();
		
		return foundUniqueSymbol(false, aposition, iCount, symbolCheck);
	}
	
	boolean foundUniqueSymbol(boolean zReturn, DPosition[] aposition, int iCount, DSymbol symbolCheck)
	{
		for (; null!=symbolCheck; symbolCheck = symbolCheck.nextSymbol())
		{
			if (foundUniquePosition(aposition, iCount, symbolCheck, null)) return foundUniqueSymbol(true, aposition, iCount, symbolCheck.nextSymbol());
		}
		return zReturn;
	}
	
	boolean foundUniquePosition(DPosition[] aposition, int iCount, DSymbol symbolCheck, DPosition positionUnique)
	{
		for (int iIndexConstrained = 0; iIndexConstrained<iCount; ++iIndexConstrained)
		{
			if (!aposition[iIndexConstrained].possibleSymbol(symbolCheck)) continue;
			if (aposition[iIndexConstrained].fixedSymbol()) continue;
			
			// When already found a position for the symbol then finding a second excludes unique possibility
			if (null!=positionUnique) return false;
			
			// Found at least one position for the symbol
			positionUnique = aposition[iIndexConstrained];
		}
		
		return constrainUniquePosition(false, aposition, iCount, symbolCheck, positionUnique);
	}
	
	boolean constrainUniquePosition(boolean zReturn, DPosition[] aposition, int iCount, DSymbol symbolCheck, DPosition positionUnique)
	{
		if (null==positionUnique) return false;

		zReturn |= excludeSymbol(false, aposition, iCount, symbolCheck, positionUnique, 0);
		zReturn |= constrainSymbol(positionUnique, symbolCheck);
		
		return zReturn;
	}
	
	boolean constrainSymbol(DPosition position, DSymbol symbol)
	{
		if (!position.constrainSymbol(symbol)) return false;
		return m_listenerStructure.eventRule(ERule._APPLY_CONSTRAIN, this, position, symbol);
	}
	
	boolean excludeSymbol(boolean zReturn, DPosition[] aposition, int iCount, DSymbol symbolCheck, DPosition positionUnique, int iIndex)
	{
		for (; iIndex<iCount; ++iIndex)
		{
			DPosition position = aposition[iIndex];
			if (positionUnique==position) continue;
			if (position.fixedSymbol()) continue;
			if (!position.excludeSymbol(symbolCheck)) continue;
			if (!m_listenerStructure.eventRule(ERule._APPLY_REMOVE, this, position, symbolCheck)) continue;
			return excludeSymbol(true, aposition, iCount, symbolCheck, positionUnique, ++iIndex);
		}
		return zReturn;
	}
	
	@Override
	String setName(int iColumn, int iRow)
	{
		String scConfigName = m_ruleConfig.setName(iColumn, iRow);
		m_scName = String.format("Unique %s", scConfigName);
		return m_scName;
	}	
}

class DRuleUniqueSquare extends DRuleUnique
{
	private static final long serialVersionUID = -6024270866388067508L;

	public DRuleUniqueSquare()
	{
		m_ruleConfig = new DRuleSquare();
	}			
}

class DRuleUniqueRow extends DRuleUnique
{
	private static final long serialVersionUID = 1006044397963285549L;

	public DRuleUniqueRow()
	{
		m_ruleConfig = new DRuleRow();
	}			
}

class DRuleUniqueColumn extends DRuleUnique
{
	private static final long serialVersionUID = -1216779490419911676L;

	public DRuleUniqueColumn()
	{
		m_ruleConfig = new DRuleColumn();
	}			
}

/**
 * Where a symbol appears in a group of 3 but not in other place in the same area then the symbol can be removed from
 * another area that shares the same group
 * e.g. [123]  [123]  [123] [45678] [45678] [45678] [45678] [45678] <9>
 *      [1234] [1235] [1236]
 *      [1237] [1238] [1239]
 * means that [123] can be removed from the square because the symbols are constrained in group shared with the row
 */
class DRuleSquareAndRemove extends DRuleSquare
{
	private static final long serialVersionUID = 2055393430194515584L;

	DRuleSquareAndRemove()
	{
		// First 3 are where symbol appears - next 6 where should not appear - next 6 constrain symbol removed
		m_iOffsetCount = 3+6+6;		
	}

	@Override
	int getOffsetColumn(int iIndex, int iAbsolute)
	{
		// Column offsets are simply 0 - 8 but the absolute position can be 0, 3, or 6 - so need to % 9 the column
		return super.getOffsetColumn(iIndex, iAbsolute) % 9; 
	}
	
	@Override
	int getOffsetRow(int iIndex, int iAbsolute)
	{
		return super.getOffsetRow(iIndex, iAbsolute) % 9;
	}
	
	@Override
	boolean foundApplication()
	{
		if (null==m_apositionReference) return false;
		if (null==m_apositionReference[0]) return false;
		DSymbol symbolCheck = m_apositionReference[0].m_symbolSet.allSymbols();
		
		return foundPossibleSymbol(false, symbolCheck);
	}
	
	boolean foundPossibleSymbol(boolean zReturn, DSymbol symbolCheck)
	{
		for (; null!=symbolCheck; symbolCheck = symbolCheck.nextSymbol())
		{
			if (foundConstrainedPosition_HAS(zReturn, symbolCheck)) return foundPossibleSymbol(true, symbolCheck.nextSymbol());
		}
		return zReturn;
	}
	
	boolean foundConstrainedPosition_HAS(boolean zReturn, DSymbol symbolCheck)
	{
		// Symbol must be found in the first 3 indexed positions
		for (int iIndexFixed = 0; iIndexFixed<3; ++iIndexFixed)
		{
			if (m_apositionReference[iIndexFixed].fixedSymbol()) continue;
			if (m_apositionReference[iIndexFixed].possibleSymbol(symbolCheck)) return foundConstrainedPosition_HASNOT(zReturn, symbolCheck);
		}		
		return zReturn;
	}
	
	boolean foundConstrainedPosition_HASNOT(boolean zReturn, DSymbol symbolCheck)
	{
		// ... but not next 6
		for (int iIndexFixed = 3; iIndexFixed<3+6; ++iIndexFixed)
		{
			if (m_apositionReference[iIndexFixed].fixedSymbol()) continue;
			if (m_apositionReference[iIndexFixed].possibleSymbol(symbolCheck)) return zReturn;
		}
		// None of those positions checked have the symbol so can remove it from the constrained positions
		return excludeSymbol(zReturn, symbolCheck, 3+6);	
	}	
}

class DRuleSquareAndRow extends DRuleSquareAndRemove
{
	private static final long serialVersionUID = -9065200857448867433L;

	DRuleSquareAndRow(int iRA, int iRB)
	{
		// First 3 are where symbol appears - next 6 where should not appear - next 6 constrain symbol removed
		m_aiColumnOffset = new int[] {0, 1, 2,   0,   1,   2,   0,   1,   2, 3, 4, 5, 6, 7, 8, }; // Value %9 when offset returned
		m_aiRowOffset =    new int[] {0, 0, 0, iRA, iRA, iRA, iRB, iRB, iRB, 0, 0, 0, 0, 0, 0, };
		m_apositionReference = new DPosition[m_iOffsetCount];
	}
	
	@Override
	String setName(int iColumn, int iRow)
	{
		m_scName = String.format("Square '%c' row %d ", DPosition.getSquare(iColumn, iRow), iRow+1);
		return m_scName;
	}	
}

class DRuleRowAndSquare extends DRuleSquareAndRemove
{
	private static final long serialVersionUID = 8091345674573143313L;

	DRuleRowAndSquare(int iRA, int iRB)
	{
		// First 3 are where symbol appears - next 6 where should not appear - next 6 constrain symbol removed
		m_aiColumnOffset = new int[] {0, 1, 2, 3, 4, 5, 6, 7, 8,   0,   1,   2,   0,   1,   2, }; // Value %9 when offset returned
		m_aiRowOffset =    new int[] {0, 0, 0, 0, 0, 0, 0, 0, 0, iRA, iRA, iRA, iRB, iRB, iRB,  };
		m_apositionReference = new DPosition[m_iOffsetCount];
	}
	
	@Override
	String setName(int iColumn, int iRow)
	{
		m_scName = String.format("Row %d square '%c' ", iRow+1, DPosition.getSquare(iColumn, iRow));
		return m_scName;
	}	
}

class DRuleSquareAndColumn extends DRuleSquareAndRemove
{
	private static final long serialVersionUID = -4530502479521563551L;

	DRuleSquareAndColumn(int iCA, int iCB)
	{
		// First 3 are where symbol appears - next 6 where should not appear - next 6 constrain symbol removed
		m_aiColumnOffset = new int[] {0, 0, 0, iCA, iCA, iCA, iCB, iCB, iCB, 0, 0, 0, 0, 0, 0, }; // Value %9 when offset returned
		m_aiRowOffset = new int[]    {0, 1, 2,   0,   1,   2,   0,   1,   2, 3, 4, 5, 6, 7, 8, };
		m_apositionReference = new DPosition[m_iOffsetCount];
	}
	
	@Override
	String setName(int iColumn, int iRow)
	{
		m_scName = String.format("Square '%c' column %d ", DPosition.getSquare(iColumn, iRow), iColumn+1);
		return m_scName;
	}	
}

class DRuleColumnAndSquare extends DRuleSquareAndRemove
{
	private static final long serialVersionUID = -6142193423928830379L;

	DRuleColumnAndSquare(int iCA, int iCB)
	{
		// First 3 are where symbol appears - next 6 where should not appear - next 6 constrain symbol removed
		m_aiColumnOffset = new int[] {0, 0, 0, 0, 0, 0, 0, 0, 0, iCA, iCA, iCA, iCB, iCB, iCB, }; // Value %9 when offset returned
		m_aiRowOffset = new int[]    {0, 1, 2, 3, 4, 5, 6, 7, 8,   0,   1,   2,   0,   1,   2, };
		m_apositionReference = new DPosition[m_iOffsetCount];
	}
	
	@Override
	String setName(int iColumn, int iRow)
	{
		m_scName = String.format("Column %d square '%c' ", iColumn+1, DPosition.getSquare(iColumn, iRow));
		return m_scName;
	}	
}

/**
 * Place holder that contains multiple rules
 */
class DRuleMulti extends DRule
{
	private static final long serialVersionUID = 6355390345896283871L;

	DRuleMulti()
	{
		// Requests ALL possible squares from caller and then provides to contained rules
		m_iOffsetCount = 81;
		m_listRules = new ArrayList<DRule>();
		m_listConfig = new ArrayList<DRuleMultiConfig>();
	}
	
	List<DRule> m_listRules;
	
	class DRuleMultiConfig
	{
		DRuleMultiConfig(DRule rule, int iColumn, int iRow)
		{
			m_rule = rule;
			m_iColumn = iColumn;
			m_iRow = iRow;
		}
		
		DRule m_rule;
		int m_iColumn;
		int m_iRow;
		
		@Override
		public String toString()
		{
			return m_rule.toString();
		}
	}
	
	List<DRuleMultiConfig> m_listConfig;
	
	@Override
	boolean addRuleListener(LStructure listener)
	{
		boolean zReturn = true;
		
		for (DRule rule : m_listRules)
		{
			zReturn &= rule.addRuleListener(listener);
		}
		
		return zReturn;
	}
	
	@Override
	LStructure removeRuleListener()
	{
		LStructure listenerReturn = null;
		
		for (DRule rule : m_listRules)
		{
			LStructure listenerRule = rule.removeRuleListener();
			if (null==listenerRule) continue;
			listenerReturn = listenerRule;
		}
		
		return listenerReturn;
	}
	
	boolean addRule(DRule rule, int iColumn, int iRow)
	{
		m_listRules.add(rule);
		m_listConfig.add(new DRuleMultiConfig(rule, iColumn, iRow));
		rule.setName(iColumn, iRow);
		return true;
	}
	
	@Override
	int getOffsetColumn(int iIndex, int iAbsolute)
	{
		return iAbsolute + (iIndex % 9);
	}
	
	@Override
	int getOffsetRow(int iIndex, int iAbsolute)
	{
		return iAbsolute + (iIndex / 9);
	}
	
	@Override
	boolean setReferencePosition(int iIndex, DPosition positionReference)
	{
		int iRow = iIndex / 9;
		int iColumn = iIndex % 9;
		
		for (DRuleMultiConfig ruleConfig : m_listConfig)
		{
			hasReferencePosition(false, positionReference, ruleConfig, iColumn, iRow, 0);
		}
		
		return true;
	}
	
	boolean hasReferencePosition(boolean zReturn, DPosition positionReference, DRuleMultiConfig ruleConfig, int iColumn, int iRow, int iIndex)
	{
		DRule rule = ruleConfig.m_rule;
		
		for (int iCount = rule.getOffsetCount(); iIndex<iCount; ++iIndex)
		{
			int iColumnOffset = rule.getOffsetColumn(iIndex, ruleConfig.m_iColumn);
			if (iColumnOffset!=iColumn) continue;
			
			int iRowOffset = rule.getOffsetRow(iIndex, ruleConfig.m_iRow);
			if (iRowOffset!=iRow) continue;
			
			rule.setReferencePosition(iIndex, positionReference);
			
			return hasReferencePosition(true, positionReference, ruleConfig, iColumn, iRow, ++iIndex);
		}
		
		return zReturn;
	}
	
	@Override
	boolean foundApplication()
	{
		m_ruleUnique = null;
		
		for (DRule rule : m_listRules)
		{
			if (!rule.foundApplication()) continue;
			m_ruleUnique = rule;
		}
		
		return null!=m_ruleUnique;
	}
	
	DRule m_ruleUnique;
	
	@Override
	String setName(int iColumn, int iRow)
	{
		m_scName = "Rule set: ";	
		return m_scName;
	}
}

/**
 * Creates 3 SquareAndRemove rules for a common square and 3 rows
 */
class DRuleSquareAndRows extends DRuleMulti
{
	private static final long serialVersionUID = -5088397774606142200L;

	DRuleSquareAndRows()
	{
		m_listRules = new ArrayList<DRule>(3*3*3);
		
		for (int iRow = 0; iRow<9; iRow += 3)
		{
			for (int iColumn = 0; iColumn<9; iColumn += 3)
			{
				addRule(new DRuleSquareAndRow(+1, +2), iColumn, iRow+0);
				addRule(new DRuleSquareAndRow(-1, +1), iColumn, iRow+1);
				addRule(new DRuleSquareAndRow(-2, -1), iColumn, iRow+2);
			}
		}
	}
}

/**
 * Creates 3 SquareAndRemove rules for a common row and 3 squares
 */
class DRuleRowAndSquares extends DRuleMulti
{
	private static final long serialVersionUID = 1721703235523330261L;

	DRuleRowAndSquares()
	{
		m_listRules = new ArrayList<DRule>(3*3*3);
		
		for (int iRow = 0; iRow<9; iRow += 3)
		{
			for (int iColumn = 0; iColumn<9; iColumn += 3)
			{
				addRule(new DRuleRowAndSquare(+1, +2), iColumn, iRow+0);
				addRule(new DRuleRowAndSquare(-1, +1), iColumn, iRow+1);
				addRule(new DRuleRowAndSquare(-2, -1), iColumn, iRow+2);
			}
		}
	}
}

/**
 * Creates 3 SquareAndRemove rules for a common square and 3 columns
 */
class DRuleSquareAndColumns extends DRuleMulti
{
	private static final long serialVersionUID = -8419526861183431997L;

	DRuleSquareAndColumns()
	{
		m_listRules = new ArrayList<DRule>(3*3*3);

		for (int iRow = 0; iRow<9; iRow += 3)
		{
			for (int iColumn = 0; iColumn<9; iColumn += 3)
			{
				addRule(new DRuleSquareAndColumn(+1, +2), iColumn+0, iRow);
				addRule(new DRuleSquareAndColumn(-1, +1), iColumn+1, iRow);
				addRule(new DRuleSquareAndColumn(-2, -1), iColumn+2, iRow);
			}
		}
	}
}

/**
 * Creates 3 SquareAndRemove rules for a common column and 3 squares
 */
class DRuleColumnAndSquares extends DRuleMulti
{
	private static final long serialVersionUID = 1201844734183700019L;

	DRuleColumnAndSquares()
	{
		m_listRules = new ArrayList<DRule>(3*3*3);

		for (int iRow = 0; iRow<9; iRow += 3)
		{
			for (int iColumn = 0; iColumn<9; iColumn += 3)
			{
				addRule(new DRuleColumnAndSquare(+1, +2), iColumn+0, iRow);
				addRule(new DRuleColumnAndSquare(-1, +1), iColumn+1, iRow);
				addRule(new DRuleColumnAndSquare(-2, -1), iColumn+2, iRow);
			}
		}
	}
}

class DRuleComplete extends DRule
{
	private static final long serialVersionUID = -5859687289950234590L;

	public DRuleComplete()
	{
		m_listRulesCheck = new ArrayList<DRuleSquare>();
		m_listRulesConstrain = new ArrayList<DRuleSquare>();
		m_symbolCheck = null;
	}
	
	ArrayList<DRuleSquare> m_listRulesCheck;
	ArrayList<DRuleSquare> m_listRulesConstrain;	
	DSymbol m_symbolCheck;
	
	@Override
	boolean foundApplication()
	{
		if (0==m_listRulesCheck.size()) return false;
		
		DRuleSquare ruleSquare = m_listRulesCheck.get(0);
		
		if (null==m_symbolCheck)
		{
			if (null==ruleSquare.m_apositionReference) return false;
			if (null==ruleSquare.m_apositionReference[0]) return false;
			m_symbolCheck = ruleSquare.m_apositionReference[0].m_symbolSet;
			m_symbolCheck = m_symbolCheck.allSymbols();
			
			return foundSymbol();	
		}

		DSymbol symbolLast = m_symbolCheck.duplicateSymbol();
		m_symbolCheck = m_symbolCheck.nextSymbol();
		
		if (foundSymbol()) return true;
		m_symbolCheck = symbolLast.allSymbols();
		
		return foundSymbol(symbolLast);
	}	
	
	boolean foundSymbol()
	{
		for (; null!=m_symbolCheck; m_symbolCheck = m_symbolCheck.nextSymbol())
		{
			// Rule is so obscure that best not to check multiple symbols
			if (foundRuleSymbol(m_symbolCheck)) return true;
		}
		return false;
	}
	
	boolean foundSymbol(DSymbol symbolLast)
	{
		for (; m_symbolCheck.sameSymbol(symbolLast); m_symbolCheck = m_symbolCheck.nextSymbol())
		{
			// Rule is so obscure that best not to check multiple symbols
			if (foundRuleSymbol(m_symbolCheck)) return true;
		}
		return false;
	}
	
	boolean foundRuleSymbol(DSymbol symbolCheck)
	{
		return false;
	}
}

/**
 * Search for cells that contain limited symbol sets
 * e.g. [12] [23] [13] [12345678] [12345678] [12345678] [12345678] [12345678] <9> 
 * since [123] are constrained in 3 cells they can be removed from the others
 */
 
class DRuleRestrict extends DRuleContains
{
	private static final long serialVersionUID = 4139286046871353494L;

	@Override
	boolean foundApplication()
	{
		DPosition[] aposition = m_ruleConfig.m_apositionReference;
		int iCount = m_ruleConfig.m_iOffsetCount;
		
		return analysePositions(aposition, iCount);
	}
	
	/**
	 * Create analysis of symbol in cells
	 * e.g. [12] [23] [13] [12345678] [12345678] [12345678] [12345678] [12345678] <9> 
	 * For each symbol => list < number of symbols in cell [list of other symbols] >
	 * 1 => 2 [123], 8 [12345678]
	 * 2 => 2 [123], 8 [12345678]
	 * 3 => 2 [123], 8 [12345678]
	 * 4 => 4 [12345678]
	 * and etc.,.
	 */ 
	class DRuleResult
	{
		DRuleResult(int iCount)
		{
			m_iCount = iCount;
			m_mapAnalysis = new HashMap<Character, DRuleResultPositions>();
			m_mapConstrained = new HashMap<DPosition, List<Character>>();
		}
		
		int m_iCount;
		Map<Character, DRuleResultPositions> m_mapAnalysis;
		Map<DPosition, List<Character>> m_mapConstrained;
		
		/**
		 * Creates the breakdown of the symbol use with a map of symbol => list < number of symbols in cell [other symbols] >
		 * The list < number of symbols in cell [other symbols] > is another class
		 */
		boolean addPosition(DSymbol symbol, DSymbol symbolSet, int iCount)
		{
			DRuleResultPositions apositions = m_mapAnalysis.get(symbol.m_cFixedSymbol);
			
			if (null==apositions)
			{
				apositions = new DRuleResultPositions();
				m_mapAnalysis.put(symbol.m_cFixedSymbol, apositions);
			}
			
			return apositions.addPosition(iCount, symbolSet);
		}
		
		/**
		 * A symbol is limited if the number of places that the symbol appears
		 * is equal to the number of alternative symbols
		 * e.g. 1 => 2 [123], 8 [12345678]
		 * is limited to appear in places where there are 2 symbols because there are only 2 alternative symbols
		 *
		 */
		boolean isLimited(DSymbol symbol, int iCount)
		{
			DRuleResultPositions apositions = m_mapAnalysis.get(symbol.m_cFixedSymbol);
			if (null==apositions) return false;
			DRuleResultPosition position = apositions.getPosition(iCount);
			if (null==position) return false;
			return position.isLimited(iCount);
		}
		
		boolean setConstrain(DPosition position, DSymbol symbolSet, DSymbol symbol)
		{
			if (!symbolSet.containsSymbol(symbol)) return false;
			
			List<Character> listSymbols = m_mapConstrained.get(position);
			if (null==listSymbols)
			{
				listSymbols = new ArrayList<Character>();
				m_mapConstrained.put(position, listSymbols);
			}
			
			if (listSymbols.contains(symbol.m_cFixedSymbol)) return false;
			
			listSymbols.add(symbol.m_cFixedSymbol);
			return true;
		}
		
		DSymbol getConstrain(DPosition position)
		{
			List<Character> listSymbols = m_mapConstrained.get(position);
			if (null==listSymbols) return null;
			
			if (listSymbols.isEmpty())
			{
				m_mapConstrained.remove(position);
				return null;
			}
			
			DSymbol symbolReturn = new DSymbol(listSymbols.get(0));
			listSymbols.remove(0);
			
			return symbolReturn;
		}
		
		@Override
		public String toString()
		{
			return String.format("Symbols %s - positions %s ", m_mapAnalysis.toString(), m_mapConstrained.toString());
		}		
	}
	
	/**
	 * A single result that has the number of symbols and the other symbols
	 */
	class DRuleResultPosition
	{
		DRuleResultPosition()
		{
			m_iNumber = 0;
			m_symbolSet = new DSymbol("");
		}
		
		int m_iNumber;
		DSymbol m_symbolSet;
		
		boolean addPosition(DSymbol symbol)
		{
			m_iNumber += 1;
			m_symbolSet.mergeSymbol(symbol);
			return true;
		}
		
		boolean isLimited(int iCount)
		{
			// Check against number of alternative symbols
			int iAlternative = m_symbolSet.countSymbol();
			// Symbol limited when there are the count of less symbols
			if (iAlternative>m_iNumber) return false; 
			return true; //Split over 2 lines to debug
		}
		
		@Override
		public String toString()
		{
			return String.format("[%d in '%s']", m_iNumber, m_symbolSet.m_scConstrains);
		}
	}
	
	/**
	 * An array of results for a symbol that has multiple number of symbols and the other symbols that appear
	 */
	class DRuleResultPositions
	{
		DRuleResultPositions()
		{
			m_iCount = 9;
			m_resultPositions = new DRuleResultPosition[m_iCount];
		}
		
		DRuleResultPosition[] m_resultPositions;
		int m_iCount;
		
		boolean addPosition(int iCount, DSymbol symbolSet)
		{
			DRuleResultPosition position = m_resultPositions[iCount-1];
			
			if (null==position)
			{
				position = new DRuleResultPosition();
				m_resultPositions[iCount-1] = position;
			}
			
			return position.addPosition(symbolSet);
		}
		
		DRuleResultPosition getPosition(int iCount)
		{
			return m_resultPositions[iCount-1];
		}
		
		@Override
		public String toString()
		{
			StringBuilder sb = new StringBuilder();
			String scSeparator = "";
			for (int iIndex = 0; iIndex<m_iCount; ++iIndex)
			{
				if (null==m_resultPositions[iIndex]) continue;
				sb.append(scSeparator);
				sb.append(iIndex);
				sb.append(": ");
				sb.append(m_resultPositions[iIndex]);
				scSeparator = ", ";
			}
			return sb.toString();
		}
	}
	
	boolean analysePositions(DPosition[] aposition, int iCount)
	{
		DRuleResult resultRule = new DRuleResult(iCount);
				
		for (int iIndex = 0; iIndex<iCount; ++iIndex)
		{
			if (aposition[iIndex].fixedSymbol()) continue;
			int iCountSymbol = aposition[iIndex].m_symbolSet.countSymbol();
			DSymbol symbol = aposition[iIndex].m_symbolSet.iterateSymbol();
			if (!analyseSymbol(resultRule, aposition[iIndex].m_symbolSet, iCountSymbol, symbol)) return false;
		}
		
		if (!foundRestrictedPosition(false, resultRule, aposition, iCount, 0)) return false;
		
		return excludeSymbol(false, resultRule, aposition, iCount, 0);
	}
	
	boolean analyseSymbol(DRuleResult resultRule, DSymbol symbolSet, int iCountSymbol, DSymbol symbol)
	{
		for (; null!=symbol; symbol = symbol.nextSymbol())
		{
			resultRule.addPosition(symbol, symbolSet, iCountSymbol);
		}
		return true;
	}
	
	/**
	 * Check the results (I) compiled to find a constraint - 
	 * for each position check the symbols present
	 */
	boolean foundRestrictedPosition(boolean zReturn, DRuleResult resultRule, DPosition[] aposition, int iCount, int iIndex)
	{
		for (; iIndex<iCount; ++iIndex)
		{
			if (aposition[iIndex].fixedSymbol()) continue;
			int iCountSymbol = aposition[iIndex].m_symbolSet.countSymbol();
			if (1==iCountSymbol) continue;
			DSymbol symbol = aposition[iIndex].m_symbolSet.iterateSymbol();
			if (foundRestrictedSymbol(false, resultRule, aposition, iCount, symbol, iCountSymbol)) return foundRestrictedPosition(true, resultRule, aposition, iCountSymbol, ++iIndex);
		}
		
		return zReturn;
	}
	
	/**
	 * Check the results (II) compiled to find a constraint - 
	 * for each symbol in a position
	 */
	boolean foundRestrictedSymbol(boolean zReturn, DRuleResult resultRule, DPosition[] aposition, int iCount, DSymbol symbol, int iNumber)
	{
		for (; null!=symbol; symbol = symbol.nextSymbol())
		{
			if (!resultRule.isLimited(symbol, iNumber)) continue;
			if (foundConstrainedSymbol(false, resultRule, aposition, iCount, 0, symbol, iNumber)) return foundRestrictedSymbol(true, resultRule, aposition, iCount, symbol.nextSymbol(), iNumber);
		}
		
		return zReturn;
	}
	
	boolean foundConstrainedSymbol(boolean zReturn, DRuleResult resultRule, DPosition[] aposition, int iCount, int iIndex, DSymbol symbol, int iNumber)
	{
		for (; iIndex<iCount; ++iIndex)
		{
			if (aposition[iIndex].fixedSymbol()) continue;
			DSymbol symbolSet = aposition[iIndex].m_symbolSet;
			int iCountSymbol = symbolSet.countSymbol();
			if (iCountSymbol<=iNumber) continue;
			// If constrain symbol here, this breaks the symbol count for other symbols
			if (!resultRule.setConstrain(aposition[iIndex], symbolSet, symbol)) continue;
			return foundConstrainedSymbol(true, resultRule, aposition, iCount, iIndex, symbol, iNumber);
		}
		
		return zReturn;
	}
	
	boolean excludeSymbol(boolean zReturn, DRuleResult resultRule, DPosition[] aposition, int iCount, int iIndex)
	{
		for (; iIndex<iCount; ++iIndex)
		{
			DPosition position = aposition[iIndex];
			if (position.fixedSymbol()) continue;
			DSymbol symbol = resultRule.getConstrain(position);
			if (null==symbol) continue;
			if (!position.excludeSymbol(symbol)) continue;
			if (!m_listenerStructure.eventRule(ERule._APPLY_REMOVE, this, position, symbol)) continue;
			return excludeSymbol(zReturn, resultRule, aposition, iCount, ++iIndex);
		}
		return zReturn;
	}
	
	@Override
	String setName(int iColumn, int iRow)
	{
		String scConfigName = m_ruleConfig.setName(iColumn, iRow);
		m_scName = String.format("Restricted %s", scConfigName);
		return m_scName;
	}	
}

class DRuleRestrictSquare extends DRuleRestrict
{
	private static final long serialVersionUID = 465369276826337245L;

	public DRuleRestrictSquare()
	{
		m_ruleConfig = new DRuleSquare();
	}
}

class DRuleRestrictRow extends DRuleRestrict
{
	private static final long serialVersionUID = -5705858507350764936L;

	public DRuleRestrictRow()
	{
		m_ruleConfig = new DRuleRow();
	}
}

class DRuleRestrictColumn extends DRuleRestrict
{
	private static final long serialVersionUID = 6474298231846227649L;

	public DRuleRestrictColumn()
	{
		m_ruleConfig = new DRuleColumn();
	}
}

/**
 * Search for cells that contain identical symbol sets in square/row/column
 * e.g. [123] [123] [123] [12345678] [12345678] [12345678] [12345678] [12345678] <9> 
 * since [123] is constrained in 3 cells it can be removed from the others
 */

class DRuleDuplicate extends DRuleContains
{
	private static final long serialVersionUID = -611645223028226187L;

	@Override
	boolean foundApplication()
	{
		DPosition[] aposition = m_ruleConfig.m_apositionReference;
		int iCount = m_ruleConfig.m_iOffsetCount;
		
		return analysePositions(aposition, iCount);
	}
	
	boolean analysePositions(DPosition[] aposition, int iCount)
	{
		for (int iIndex = 0; iIndex<iCount-1; ++iIndex)
		{
			if (aposition[iIndex].fixedSymbol()) continue;
			DSymbol symbolTarget = aposition[iIndex].m_symbolSet;
			int iSymbolCount = symbolTarget.countSymbol();
			return analysePosition(false, aposition, iCount, symbolTarget, iSymbolCount, iIndex+1);
		}
		
		return false;
	}
	
	boolean analysePosition(boolean zReturn, DPosition[] aposition, int iCount, DSymbol symbolTarget, int iTargetSymbolCount, int iIndex)
	{
		int iMatch = 1;
		
		for (; iIndex<iCount; ++iIndex)
		{
			if (aposition[iIndex].fixedSymbol()) continue;
			DSymbol symbol = aposition[iIndex].m_symbolSet;
			int iSymbolCount = symbol.countSymbol();
			if (iSymbolCount!=iTargetSymbolCount)
			{
				zReturn |= analysePosition(zReturn, aposition, iCount, symbol, iSymbolCount, iIndex+1);
				continue;
			}
			if (!symbolTarget.sameSymbolSet(symbol)) 
			{
				zReturn |= analysePosition(zReturn, aposition, iCount, symbol, iSymbolCount, iIndex+1);
				continue;
			}
			if (++iMatch<iTargetSymbolCount) continue;
			
			return excludeSymbols(zReturn, aposition, iCount, symbolTarget, 0);
		}	
		return zReturn;
	}
	
	boolean excludeSymbols(boolean zReturn, DPosition[] aposition, int iCount, DSymbol symbolTarget, int iIndex)
	{
		for (; iIndex<iCount; ++iIndex)
		{
			if (aposition[iIndex].fixedSymbol()) continue;
			if (aposition[iIndex].m_symbolSet.sameSymbolSet(symbolTarget)) continue;
			if (!excludeSymbol(zReturn, aposition[iIndex], symbolTarget.iterateSymbol())) continue;
			return excludeSymbols(true, aposition, iCount, symbolTarget, iIndex+1);
		}
		
		return zReturn;
	}
	
	boolean excludeSymbol(boolean zReturn, DPosition position, DSymbol symbolTarget)
	{
		for (; null!=symbolTarget; symbolTarget = symbolTarget.nextSymbol())
		{
			if (!position.excludeSymbol(symbolTarget)) continue;
			if (!m_listenerStructure.eventRule(ERule._APPLY_REMOVE, this, position, symbolTarget)) continue;
			return excludeSymbol(true, position, symbolTarget.nextSymbol());
		}
		return zReturn;
	}
	
	@Override
	String setName(int iColumn, int iRow)
	{
		String scConfigName = m_ruleConfig.setName(iColumn, iRow);
		m_scName = String.format("Duplicates in %s", scConfigName);
		return m_scName;
	}	
}

class DRuleDuplicateSquare extends DRuleDuplicate
{
	private static final long serialVersionUID = -2552638706187440415L;

	public DRuleDuplicateSquare()
	{
		m_ruleConfig = new DRuleSquare();
	}	
}

class DRuleDuplicateColumn extends DRuleDuplicate
{
	private static final long serialVersionUID = -467411870671937951L;

	public DRuleDuplicateColumn()
	{
		m_ruleConfig = new DRuleColumn();
	}	
}

class DRuleDuplicateRow extends DRuleDuplicate
{
	private static final long serialVersionUID = 1246235641610250257L;

	public DRuleDuplicateRow()
	{
		m_ruleConfig = new DRuleRow();
	}	
}

/**
 * Find 2 intersecting pairs of numbers
 * e.g. [123] [123] [123] [45]    [12345678] [56] [12345678] [12345678] <9> 
 *      [123] [123] [123] [45678] [12345678] [46] [12345678] [12345678] <9> 
 * whatever the value of [56] in row [4x] is fixed in row or column hence removing [4] from [45678]
 *
 * Sequencing ...
 *
    +---------------------------rule--+----------+
    |                                 |          |
    |   +------+                      |position  |
    |   |      |position/rule         |          |
    V   v      |                      v          |
+--------+     |              +--------+         |
| 1 XZ   |-----+----found---->| 2 XY   |---------+
+--------+                    +--------+         |
        ^                         ^              |
   rules|                         |              |
        +------+                  +---------+    |
    rule|      |                            |    |
        v      |                            |    |
+--------+     |              +--------+    |    |
| 3 Z    |-----+----found---->| 4 YZ   |----+    |
+--------+                    +--------+         |
        ^                                        |
        |                                        |
        +-----------found------------------------+
 *
 */
class DRulePairsLinked extends DRuleComplete
{
	private static final long serialVersionUID = 8422571103065537468L;

	// Need public constructor to build from reflection
	public DRulePairsLinked()
	{
	}
	
	enum ERuleSequence 
	{
		_BEGIN, _FIRST_XZ, _SECOND_XY, _THIRD_Z, _FOURTH_YZ, _EXCLUDE, _END;
		
		ERuleSequence next()
		{
			switch (this)
			{
			case _BEGIN:
				return _FIRST_XZ;
			case _FIRST_XZ:
				return _SECOND_XY;
			case _SECOND_XY:
				return _THIRD_Z;
			case _THIRD_Z:
				return _FOURTH_YZ;
			case _FOURTH_YZ:
				return _EXCLUDE;
			case _EXCLUDE:
				return _END;
			case _END:
				break;
			}
			return _END;
		}
	};
	
	class DRuleResult
	{
		DRuleResult()
		{
			m_enumSequencing = ERuleSequence._BEGIN;
		}
		
		ERuleSequence m_enumSequencing;
		
		Iterator<DRuleSquare> m_iterateRules;
		DRuleSquare m_ruleCheck;
		int m_iIndex;

		DRuleSquare m_ruleFirst;
		int m_iFirstIndex;	
		DSymbol m_symbolZ;
		
		int m_iSecondIndex;
		DSymbol m_symbolY;

		boolean resetRule()
		{
			m_iterateRules = m_listRulesCheck.listIterator();
			m_ruleCheck = m_iterateRules.next();
			m_iIndex = 0;
			return true;
		}
		
		boolean nextRule()
		{
			// When no more rules to check for symbol
			if (!m_iterateRules.hasNext()) return false;
			m_ruleCheck = m_iterateRules.next();
			m_iIndex = 0;
			return true;
		}
		
		boolean resetRule(DRule rule, int iIndex)
		{
			resetRule();
			
			while (m_ruleCheck!=rule)
			{
				if (!nextRule()) return false;
			}
			m_iIndex = iIndex;
			
			return true;
		}
		
		boolean foundFirst(int iIndex, DSymbol symbolZ)
		{
			m_iIndex = 0; // start looking for symbol again in same rule
			
			m_ruleFirst = m_ruleCheck;
			m_iFirstIndex = iIndex;
			m_symbolZ = symbolZ;
			return true;
		}
		
		boolean foundSecond(int iIndex, DSymbol symbolY)
		{
			m_iSecondIndex = iIndex;
			m_symbolY = symbolY;
			return true;
		}

		@Override
		public String toString()
		{
			StringBuilder sb = new StringBuilder();
			sb.append(m_enumSequencing);
			sb.append(' ');
			if (null!=m_ruleFirst) 
			{
				sb.append(m_ruleFirst.toString());
				sb.append(DPosition.getPositions(m_ruleFirst.m_apositionReference));
				sb.append(' ');
			}
			sb.append(m_iFirstIndex);
			sb.append(" [");
			if (null!=m_symbolZ) sb.append(m_symbolZ);
			sb.append("] ");
			sb.append(m_iSecondIndex);
			sb.append(" [");
			if (null!=m_symbolY) sb.append(m_symbolY);
			sb.append("] ");
			if (null!=m_ruleCheck) sb.append(m_ruleCheck.toString());
			return sb.toString();
		}
	}
	
	@Override
	boolean foundRuleSymbol(DSymbol symbolCheck)
	{
		// Perform sequencing with subclass
		DRuleResult resultRule = new DRuleResult();
		
		for (; ERuleSequence._END!=resultRule.m_enumSequencing;)
		{
			switch (resultRule.m_enumSequencing)
			{
			case _BEGIN:
				// Set first rule to check at first position
				resultRule.resetRule();
				break;
				
			case _FIRST_XZ:
				// When find a pair with the symbol to check break to go to next step
				if (foundPosition(resultRule, symbolCheck, resultRule.m_iIndex)) break;
				// Try the next rule
				if (!resultRule.nextRule()) return false;
				// Continue with same step
				continue;
				
			case _SECOND_XY:
				// When find a pair with the symbol again break to go looking for intersection
				if (foundPosition(resultRule, symbolCheck, resultRule.m_iIndex)) break;
				// Failed to find the symbol again - try next rule for first step
				if (!resultRule.nextRule()) return false;
				// Return to _FIRST_XZ
				resultRule.resetRule(resultRule.m_ruleFirst, resultRule.m_iFirstIndex+1);
				resultRule.m_enumSequencing = ERuleSequence._FIRST_XZ;
				continue;
				
			case _THIRD_Z:
				if (!resultRule.nextRule()) 
				{
					// When no more rules then try finding the same symbol from the first step but different place
					resultRule.resetRule(resultRule.m_ruleFirst, resultRule.m_iFirstIndex+1);
					resultRule.m_enumSequencing = ERuleSequence._FIRST_XZ;
					continue;					
				}
				// Need to find symbol same place in another rule
				if (foundSymbol(resultRule, resultRule.m_symbolZ, resultRule.m_iFirstIndex)) break;
				// Continue other rules
				continue;
				
			case _FOURTH_YZ:
				if (foundSymbol(resultRule, resultRule.m_symbolY, resultRule.m_iSecondIndex)) break;
				// No good, try another position with the second step {third will find same Z}
				resultRule.resetRule(resultRule.m_ruleFirst, resultRule.m_iSecondIndex+1);
				resultRule.m_enumSequencing = ERuleSequence._SECOND_XY;
				continue;
				
			case _EXCLUDE:
				return excludeSymbol(resultRule);
				
			case _END:
				return false;
			}

			// Next step
			resultRule.m_enumSequencing = resultRule.m_enumSequencing.next();
		}
		
		return false;
	}
	
	boolean foundPosition(DRuleResult resultRule, DSymbol symbolCheck, int iIndex)
	{
		DRuleSquare rule = resultRule.m_ruleCheck;
		int iIndexCount = rule.m_iOffsetCount;
		
		for (; iIndex<iIndexCount; ++iIndex)
		{
			if (foundSymbol(resultRule, symbolCheck, iIndex)) return true;
		}
		
		return false;
	}
	
	boolean foundSymbol(DRuleResult resultRule, DSymbol symbolCheck, int iIndex)
	{
		DRuleSquare rule = resultRule.m_ruleCheck;
		DPosition position = rule.m_apositionReference[iIndex];
		
		if (position.fixedSymbol()) return false; // Position is already fixed
		DSymbol symbolFound = position.getSymbolSet();
		if (!symbolFound.containsSymbol(symbolCheck)) return false; // Don't have symbol
	
		int iCountSymbols = symbolFound.countSymbol();
		// Check what to do when found a pair
			
		switch (resultRule.m_enumSequencing)
		{
		case _BEGIN:
			throw new RuntimeException("Cannot execute with this state");
			
		case _FIRST_XZ:
			if (2!=iCountSymbols) return false; // Position does not have 2 symbols
			DSymbol symbolZ = symbolFound.duplicateSymbol();
			symbolZ.excludeSymbol(symbolCheck);
			return resultRule.foundFirst(iIndex, symbolZ);
			
		case _SECOND_XY:
			if (2!=iCountSymbols) return false; // Position does not have 2 symbols
			if (iIndex==resultRule.m_iFirstIndex) return false; // Second checks from beginning of rule so skip over pair found first
			DSymbol symbolY = symbolFound.duplicateSymbol();
			symbolY.excludeSymbol(symbolCheck);
			if (symbolY.sameSymbolSet(resultRule.m_symbolZ)) return false; // Symbol was not paired correctly
			return resultRule.foundSecond(iIndex, symbolY);
			
		case _THIRD_Z:
			// Nothing special when found symbol
			return true;
			
		case _FOURTH_YZ:
			if (2!=iCountSymbols) return false; // Position does not have 2 symbols
			return true;
			
		case _EXCLUDE:
		case _END:
			throw new RuntimeException("Cannot execute with this state");
		}
		
		return false;
	}
	
	boolean excludeSymbol(DRuleResult resultRule)
	{
		// Can remove Z from the same column as XZ found and row where YZ found
		DRuleSquare rule = resultRule.m_ruleCheck;
		DPosition position = rule.m_apositionReference[resultRule.m_iFirstIndex];
		if (!position.excludeSymbol(resultRule.m_symbolZ)) return false;
		return m_listenerStructure.eventRule(ERule._APPLY_REMOVE, this, position, resultRule.m_symbolZ);
	}
}

class DRulePairsLinkedRow extends DRulePairsLinked
{
	private static final long serialVersionUID = -1559055847140840861L;

	public DRulePairsLinkedRow() // Public constructor so can be created by reflection
	{
		super();
	}
	
	@Override
	boolean findIntersectingRules(List<DRule> listRules)
	{
		for (DRule rule : listRules)
		{
			if (rule instanceof DRuleRow) m_listRulesCheck.add((DRuleSquare)rule);
		}
		return true;
	}
	
	@Override
	String setName(int iColumn, int iRow)
	{
		m_scName = "XY XZ row YZ Z column ";
		return m_scName;
	}	
}

class DRulePairsLinkedColumn extends DRulePairsLinked
{
	private static final long serialVersionUID = 6466241420574737486L;

	public DRulePairsLinkedColumn() // Public constructor so can be created by reflection
	{
		super();
	}
	
	@Override
	boolean findIntersectingRules(List<DRule> listRules)
	{
		for (DRule rule : listRules)
		{
			if (rule instanceof DRuleColumn) m_listRulesCheck.add((DRuleSquare)rule);
		}
		return true;
	}
	
	@Override
	String setName(int iColumn, int iRow)
	{
		m_scName = "XY XZ column YZ Z row ";
		return m_scName;
	}	
}

/*
 * Find intersecting occurences of numbers
 * e.g. [123] [123] [123] [12345678] [12345678] [1235678] [1235678] [1235678] <9> 
 *      [123] [123] [123] [12345679] [12345679] [1235679] [1235679] [1235679] <8>
 *                            V          V 
 * can remove [4] from all other rows in the same columns (2 in 2 rows)
 * also applies for 3 in 3 rows and 4 in 4 rows etc.,.
 */
class DRuleRestrictLinked extends DRuleComplete
{
	private static final long serialVersionUID = -5461899064988705242L;

	// Need public constructor to build from reflection
	public DRuleRestrictLinked()
	{
	}
	
	class DRuleResult
	{
		DRuleResult()
		{
			m_listRulePositions = new ArrayList<DRuleResultOccurrence>(9);
		}
		
		List<DRuleResultOccurrence> m_listRulePositions;
		
		int addResult(int iCount, DRuleSquare rule, int[] aiPositions)
		{
			DRuleResultOccurrence occurrence;
			
			for (int iCurrent = m_listRulePositions.size(); iCurrent<=iCount; ++iCurrent)
			{
				occurrence = new DRuleResultOccurrence();
				m_listRulePositions.add(occurrence);				
			}
			
			occurrence = m_listRulePositions.get(iCount);
			
			occurrence.m_iOccurrences++;
			occurrence.m_listRules.add(rule);
			occurrence.m_listPositions.add(aiPositions);
			
			return occurrence.m_iOccurrences;
		}
		
		boolean removeResult(DRuleResultOccurrence occurrence)
		{
			int iIndex = m_listRulePositions.indexOf(occurrence);
			m_listRulePositions.add(iIndex, null);
			return true;
		}
		
		boolean refineResult(List<DRuleResultOccurrence> listRulePositions)
		{
			m_listRulePositions = listRulePositions;
			return 0<m_listRulePositions.size();
		}

		public Iterator<DRuleResultOccurrence> getIterator() 
		{
			return m_listRulePositions.listIterator();
		}
	}
	
	class DRuleResultOccurrence
	{
		DRuleResultOccurrence()
		{
			m_iOccurrences = 0;
			m_listRules = new ArrayList<DRuleSquare>();
			m_listPositions = new ArrayList<int[]>();
		}
		
		int m_iOccurrences;
		List<DRuleSquare> m_listRules;
		List<int[]> m_listPositions;
		
		boolean notLimiting()
		{
			if (2>m_iOccurrences) return true; // 0 or 1 occurrences in the rule are irrelevant
			if (7<m_iOccurrences) return true; // more than 7 occurrences in the rule are irrelevant
			
			int[] aiPositions = m_listPositions.get(0);
			if (m_iOccurrences<aiPositions.length) return true;
			
			return false;
		}

		boolean tiedPosition()
		{
			int[] aiPositionsCompare = m_listPositions.get(0);
				
			for (int iPosition = 0; iPosition<m_iOccurrences; ++iPosition)
			{
				for (int[] aiPositions : m_listPositions)
				{
					if (aiPositions[iPosition]!=aiPositionsCompare[iPosition]) return false;
				}				
			}
			
			return true;
		}
		
		boolean containsRule(DRuleSquare rule)
		{
			return m_listRules.contains(rule);
		}

		public int[] getPositions() 
		{
			return m_listPositions.get(0);
		}
	}
	
	@Override
	boolean foundRuleSymbol(DSymbol symbolCheck)
	{
		DRuleResult resultRule = new DRuleResult();

		// Create synthesis of how many times symbol appears and in how many rules
		if (!analyseSymbol(resultRule, symbolCheck)) return false;
		
		// For every number of occurrence where number times appears = number of rules possible constraint
		if (!analysePosition(resultRule, symbolCheck)) return false;
		
		// Can remove symbol from same position in all other rules
		return excludeSymbol(resultRule, symbolCheck);
	}

	boolean analyseSymbol(DRuleResult resultRule, DSymbol symbolCheck)
	{
		int iMultiples = 0;
		
		for (DRuleSquare rule : m_listRulesCheck)
		{
			int iCount = countSymbol(rule, resultRule, symbolCheck);
			if (iCount>iMultiples) iMultiples = iCount;
		}
		
		return 1<iMultiples;
	}
	
	int countSymbol(DRuleSquare rule, DRuleResult resultRule, DSymbol symbolCheck)
	{
		int iIndexCount = rule.m_iOffsetCount;
		int[] aiPositions = new int[iIndexCount];
		
		int iCount = 0;
		
		for (int iIndex = 0; iIndex<iIndexCount; ++iIndex)
		{
			DPosition position = rule.m_apositionReference[iIndex];
			if (position.fixedSymbol()) continue;
			DSymbol symbolFound = position.getSymbolSet();
			if (!symbolFound.containsSymbol(symbolCheck)) continue;
			aiPositions[iCount++] = iIndex;
		}
		
		// Ignore unique occurrences because symbol will be fixed and return length of rule {which will ignore count}
		if (2>iCount) return iIndexCount;
		
		int[] aiPositions_count = new int[iCount];
		System.arraycopy(aiPositions, 0, aiPositions_count, 0, iCount);
		
		return resultRule.addResult(iCount, rule, aiPositions_count);
	}
	
	boolean analysePosition(DRuleResult resultRule, DSymbol symbolCheck)
	{
		List<DRuleResultOccurrence> listConsistent = new ArrayList<DRuleResultOccurrence>();

		for (Iterator<DRuleResultOccurrence> iterateResult = resultRule.getIterator(); iterateResult.hasNext();)
		{
			DRuleResultOccurrence occurrence = iterateResult.next();
			if (null==occurrence) continue;
			if (occurrence.notLimiting()) continue;
			if (!occurrence.tiedPosition()) continue;
			listConsistent.add(occurrence);
		}
		
		return resultRule.refineResult(listConsistent);
	}
	
	boolean excludeSymbol(DRuleResult resultRule, DSymbol symbolCheck)
	{
		return excludeSymbol(false, symbolCheck, resultRule.getIterator());
	}
	
	boolean excludeSymbol(boolean zReturn, DSymbol symbolCheck, Iterator<DRuleResultOccurrence> iterateResult)
	{
		for (;iterateResult.hasNext();)
		{
			DRuleResultOccurrence occurrence = iterateResult.next();
			if (excludeSymbol(false, occurrence, symbolCheck)) return excludeSymbol(true, symbolCheck, iterateResult);
		}
		return zReturn;
	}
	
	boolean excludeSymbol(boolean zReturn, DRuleResultOccurrence occurrence, DSymbol symbolCheck)
	{
		for (DRuleSquare rule : m_listRulesCheck)
		{
			if (occurrence.containsRule(rule)) continue;
			zReturn |= excludeSymbol(false, occurrence.getPositions(), symbolCheck, rule, 0);
		}
		return zReturn;
	}
	
	boolean excludeSymbol(boolean zReturn, int[] aiPositions, DSymbol symbolCheck, DRuleSquare rule, int iIndex)
	{
		for (int iCount = aiPositions.length; iIndex<iCount; ++iIndex)
		{
			int iPosition = aiPositions[iIndex];
			DPosition position = rule.m_apositionReference[iPosition];
			if (position.fixedSymbol()) continue;
			if (!position.excludeSymbol(symbolCheck)) continue;
			if (!m_listenerStructure.eventRule(ERule._APPLY_REMOVE, this, position, symbolCheck)) continue;
			return excludeSymbol(true, aiPositions, symbolCheck, rule, ++iIndex);
		}
		return zReturn;
	}	
}

class DRuleRestrictLinkedRow extends DRuleRestrictLinked
{
	private static final long serialVersionUID = 306099972876320806L;

	public DRuleRestrictLinkedRow() // Public constructor so can be created by reflection
	{
		super();
	}
	
	@Override
	boolean findIntersectingRules(List<DRule> listRules)
	{
		for (DRule rule : listRules)
		{
			if (rule instanceof DRuleRow) m_listRulesCheck.add((DRuleSquare)rule);
		}
		return true;
	}
	
	@Override
	String setName(int iColumn, int iRow)
	{
		m_scName = "X*X in rows ";
		return m_scName;
	}	
}

class DRuleRestrictLinkedColumn extends DRuleRestrictLinked
{
	private static final long serialVersionUID = -2025145312485203656L;

	public DRuleRestrictLinkedColumn() // Public constructor so can be created by reflection
	{
		super();
	}
	
	@Override
	boolean findIntersectingRules(List<DRule> listRules)
	{
		for (DRule rule : listRules)
		{
			if (rule instanceof DRuleColumn) m_listRulesCheck.add((DRuleSquare)rule);
		}
		return true;
	}
	
	@Override
	String setName(int iColumn, int iRow)
	{
		m_scName = "X*X in columns ";
		return m_scName;
	}	
}

class DRuleRestrictAnywhere extends DRuleComplete
{
	private static final long serialVersionUID = 8265440960397541037L;

	// Need public constructor to build from reflection
	public DRuleRestrictAnywhere()
	{
	}
	
	class DRuleResult
	{
		boolean pairsExist() 
		{
			// TODO Auto-generated method stub
			return false;
		}
		
		boolean integrateLinks(DRuleResultLinks resultLinks) 
		{
			// TODO Auto-generated method stub
			return false;
		}
	}
	
	class DRuleResultLinks
	{
		boolean foundPosition(int iIndex, DPosition position) 
		{
			// TODO Auto-generated method stub
			return false;
		}

		boolean foundPair() 
		{
			// TODO Auto-generated method stub
			return false;
		}
		
	}
	
	@Override
	boolean foundRuleSymbol(DSymbol symbolCheck)
	{
		DRuleResult resultRule = new DRuleResult();

		// Create synthesis of symbol pairs containing target symbol
		if (!analyseSymbol(resultRule, symbolCheck)) return false;
		
		// Try and link symbols
		if (!analysePosition(resultRule, symbolCheck)) return false;
		
		// Can remove symbol from same position in all other rules
		return excludeSymbol(false, resultRule, symbolCheck);
	}
	
	boolean analyseSymbol(DRuleResult resultRule, DSymbol symbolCheck)
	{
		for (DRuleSquare rule : m_listRulesCheck)
		{
			DRuleResultLinks resultLinks = new DRuleResultLinks();
			if (!analyseSymbol(resultLinks, symbolCheck, rule)) continue;
			resultRule.integrateLinks(resultLinks);
		}
		return resultRule.pairsExist();
	}
	
	boolean analyseSymbol(DRuleResultLinks resultLinks, DSymbol symbolCheck, DRuleSquare rule)
	{
		DPosition[] aposition = rule.m_apositionReference;
		int iCount = rule.m_iOffsetCount;
		
		for (int iIndex = 0; iIndex<iCount; ++iIndex)
		{
			DPosition position = aposition[iIndex];
			if (position.fixedSymbol()) continue;
			DSymbol symbolSet = position.m_symbolSet;
			int iCountSymbol = symbolSet.countSymbol();
			if (iCountSymbol!=2) continue;
			if (!resultLinks.foundPosition(iIndex, position)) return false;
		}
		
		return resultLinks.foundPair();
	}
	
	boolean analysePosition(DRuleResult resultRule, DSymbol symbolCheck)
	{
		// TODO
		return false;
	}
	
	boolean excludeSymbol(boolean zReturn, DRuleResult resultRule, DSymbol symbolCheck)
	{
		// TODO
		return zReturn;
	}
	
}
