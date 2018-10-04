package rudoku;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;

import rudoku.LStructure.EStructure;
import rudoku.LStructure.ESymbol;

public class DStructure implements Serializable, TableModelListener 
{
	private static final long serialVersionUID = -1752251417145265969L;

	public DStructure()
	{
		m_dataTable = new DefaultTableModel();
		m_listRule = new ArrayList<DRule>();
		m_listenerStructure = new LStructureAdapter();
	}
	
	DefaultTableModel m_dataTable;
	List<DRule> m_listRule;
	transient LStructure m_listenerStructure;
	
	void setLoaded(DStructure structureLoaded)
	{
		LStructure listener = removeStructureListener();
		m_dataTable = structureLoaded.m_dataTable;
		m_listRule = structureLoaded.m_listRule;
		addStructureListener(listener);
		return;
	}
		
	boolean addStructureListener(LStructure listener)
	{
		m_dataTable.addTableModelListener(this);
		
		for (int iColumn = 0, iColumnCount = m_dataTable.getColumnCount(); iColumn<iColumnCount; ++iColumn)
		{
			for (int iRow = 0, iRowCount = m_dataTable.getRowCount(); iRow<iRowCount; ++iRow)
			{
				DPosition position = (DPosition)m_dataTable.getValueAt(iRow,  iColumn);
				position.addPositionListener(listener);
			}
		}
		
		for (DRule rule : m_listRule)
		{
			rule.addRuleListener(listener);
		}
		
		m_listenerStructure = listener;
		return true;
	}
	
	LStructure removeStructureListener()
	{
		LStructure listener = m_listenerStructure;
		m_dataTable.removeTableModelListener(this);
		
		for (int iColumn = 0, iColumnCount = m_dataTable.getColumnCount(); iColumn<iColumnCount; ++iColumn)
		{
			for (int iRow = 0, iRowCount = m_dataTable.getRowCount(); iRow<iRowCount; ++iRow)
			{
				DPosition position = (DPosition)m_dataTable.getValueAt(iRow,  iColumn);
				position.removePositionListener();
			}
		}
		
		for (DRule rule : m_listRule)
		{
			rule.removeRuleListener();
		}
		
		m_listenerStructure = null;
		return listener;
	}
	
	boolean addPosition(DPosition positionAdd)
	{
		if (0==m_dataTable.getColumnCount())
		{
			m_dataTable.addColumn("1");
			Object[] aoRowData = {positionAdd};
			m_dataTable.addRow(aoRowData);
			return m_listenerStructure.eventSymbol(ESymbol._INITIAL_SET, positionAdd.getSymbolSet());
		}
		return false;
	}
	
	int getColumn(DPosition positionFind)
	{
		int iRowCount = m_dataTable.getRowCount();
		int iColumnCount = m_dataTable.getColumnCount();
		
		for (int iRowIndex = 0; iRowIndex<iRowCount; ++iRowIndex)
		{
			for (int iColumnIndex = 0; iColumnIndex<iColumnCount; ++iColumnIndex)
			{
				DPosition positionReference = (DPosition)m_dataTable.getValueAt(iRowIndex, iColumnIndex);
				if (positionFind==positionReference) return iColumnIndex;
			}
		}
		
		throw new ArrayIndexOutOfBoundsException();
	}
	
	int getRow(DPosition positionFind)
	{
		int iRowCount = m_dataTable.getRowCount();
		int iColumnCount = m_dataTable.getColumnCount();
		
		for (int iRowIndex = 0; iRowIndex<iRowCount; ++iRowIndex)
		{
			for (int iColumnIndex = 0; iColumnIndex<iColumnCount; ++iColumnIndex)
			{
				DPosition positionReference = (DPosition)m_dataTable.getValueAt(iRowIndex, iColumnIndex);
				if (positionFind==positionReference) return iRowIndex;
			}
		}
		
		throw new ArrayIndexOutOfBoundsException();
	}
	
	boolean duplicateColumn()
	{
		int iReferenceColumn = m_dataTable.getColumnCount();
		if (0==iReferenceColumn) return false;
		int iRowCount = m_dataTable.getRowCount();
		if (0==iRowCount) return false;
		
		m_dataTable.addColumn(String.format("Column %d", iReferenceColumn+1));
		
		for (int iRowIndex = 0; iRowIndex<iRowCount; ++iRowIndex)
		{
			DPosition positionReference = (DPosition)m_dataTable.getValueAt(iRowIndex, iReferenceColumn-1);
			DPosition positionDuplicate = positionReference.duplicatePosition();
			m_dataTable.setValueAt(positionDuplicate, iRowIndex, iReferenceColumn);
		}
		
		return m_listenerStructure.eventStructure(EStructure._CHANGE_STRUCTURE, this);
	}
	
	boolean duplicateRow()
	{
		int iReferenceRow = m_dataTable.getRowCount();
		if (0==iReferenceRow) return false;
		int iColumnCount = m_dataTable.getColumnCount();
		if (0==iColumnCount) return false;
		
		Object[] aoRow = new Object[iColumnCount];
		
		for (int iColumnIndex = 0; iColumnIndex<iColumnCount; ++iColumnIndex)
		{
			DPosition positionReference = (DPosition)m_dataTable.getValueAt(iReferenceRow-1, iColumnIndex);
			aoRow[iColumnIndex] = positionReference.duplicatePosition();
		}		
		
		m_dataTable.addRow(aoRow);
		
		return true;
	}
	
	boolean setRulePosition(DRule rule, int iColumn, int iRow)
	{
		int iOffsets = rule.getOffsetCount();
		
		for (int iIndex = 0; iIndex<iOffsets; ++iIndex)
		{
			int iColumnOffset = rule.getOffsetColumn(iIndex, iColumn);
			int iRowOffset = rule.getOffsetRow(iIndex, iRow);
			
			DPosition positionReference = (DPosition)m_dataTable.getValueAt(iRowOffset, iColumnOffset);
			if (null==positionReference) return false;
			
			rule.setReferencePosition(iIndex, positionReference);
		}

		rule.setName(iColumn, iRow);
		
		return true;
	}
	
	boolean addRule(DRule rule, int iColumn, int iRow)
	{
		if (!setRulePosition(rule, iColumn, iRow)) return false;
		if (!rule.findIntersectingRules(m_listRule)) return false;
		m_listRule.add(rule);
		rule.addRuleListener(m_listenerStructure);
		return true;
	}
	
	boolean dropRules()
	{
		for (DRule rule : m_listRule)
		{
			rule.removeRuleListener();
		}
		
		m_listRule.clear();
		
		return true;
	}
	
	DPosition getUniquePosition()
	{
		int iRowCount = m_dataTable.getRowCount();
		int iColumnCount = m_dataTable.getColumnCount();
		
		for (int iRowIndex = 0; iRowIndex<iRowCount; ++iRowIndex)
		{
			for (int iColumnIndex = 0; iColumnIndex<iColumnCount; ++iColumnIndex)
			{
				DPosition positionReference = (DPosition)m_dataTable.getValueAt(iRowIndex, iColumnIndex);
				if (null==positionReference) continue;
				
				if (positionReference.fixedSymbol()) continue;
				if (positionReference.uniqueSymbol()) return positionReference;
			}
		}
		
		return null;		
	}
		
	boolean commitUnique()
	{
		DPosition positionUnique = getUniquePosition();
		if (null==positionUnique) return false;
		
		DSymbol symbolFix = positionUnique.getUniqueSymbol();
		int iColumn = getColumn(positionUnique);
		int iRow = getRow(positionUnique);
		return fixSymbol(symbolFix, iColumn, iRow);
	}
	
	boolean foundConstrainedRule()
	{
		for (DRule rule : m_listRule)
		{
			if (rule.foundApplication()) return true;
		}
		
		return false;
	}
	
	boolean fixSymbol(DSymbol symbolFix, int iColumn, int iRow)
	{
		DPosition positionReference = (DPosition)m_dataTable.getValueAt(iRow, iColumn);
		if (null==positionReference) return false;
		return positionReference.fixSymbol(symbolFix);
	}
	
	boolean isComplete()
	{
		int iRowCount = m_dataTable.getRowCount();
		int iColumnCount = m_dataTable.getColumnCount();
		
		for (int iRowIndex = 0; iRowIndex<iRowCount; ++iRowIndex)
		{
			for (int iColumnIndex = 0; iColumnIndex<iColumnCount; ++iColumnIndex)
			{
				DPosition positionReference = (DPosition)m_dataTable.getValueAt(iRowIndex, iColumnIndex);
				if (null==positionReference) continue;
				// Any unfixed symbol means not complete
				if (!positionReference.fixedSymbol()) return false;
			}
		}
		
		return true;
	}
	
	float completePercent()
	{
		float fComplete = 0;
		
		int iRowCount = m_dataTable.getRowCount();
		int iColumnCount = m_dataTable.getColumnCount();
		
		for (int iRowIndex = 0; iRowIndex<iRowCount; ++iRowIndex)
		{
			for (int iColumnIndex = 0; iColumnIndex<iColumnCount; ++iColumnIndex)
			{
				DPosition positionReference = (DPosition)m_dataTable.getValueAt(iRowIndex, iColumnIndex);
				if (null==positionReference) continue;
				if (positionReference.fixedSymbol()) fComplete += 1;
			}
		}
		
		return (100f*fComplete)/(iRowCount*iColumnCount);
	}
	
	@Override
	public void tableChanged(TableModelEvent arg0) 
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public String toString()
	{
		StringBuilder sbTrace = new StringBuilder();
		
		int iRowCount = m_dataTable.getRowCount();
		int iColumnCount = m_dataTable.getColumnCount();
		
		for (int iRowIndex = 0; iRowIndex<iRowCount; ++iRowIndex)
		{
			for (int iColumnIndex = 0; iColumnIndex<iColumnCount; ++iColumnIndex)
			{
				DPosition positionReference = (DPosition)m_dataTable.getValueAt(iRowIndex, iColumnIndex);
				if (null==positionReference) continue;
				// Any unfixed symbol means not complete
				if (positionReference.fixedSymbol()) 
					sbTrace.append(positionReference.toString());
				else
					sbTrace.append(" ? ");
				sbTrace.append(' ');
			}
			sbTrace.append("\n");
		}
		
		return sbTrace.toString();
	}
	
	public static void main(String[] args) 
	{
		DStructure structureMain = new DStructure();
		
		structureMain.addPosition(new DPosition());
		structureMain.duplicateColumn();
		structureMain.duplicateColumn();
		structureMain.duplicateRow();
		structureMain.duplicateRow();
		
		float fComplete = structureMain.completePercent();
		System.out.println(String.format("%s\n\n%2.1f%% complete", structureMain, fComplete));
		
		return;
	}
}
