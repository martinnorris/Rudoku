package rudoku;

import java.io.Serializable;

import rudoku.LStructure.EPosition;

public class DPosition implements Serializable  
{
	private static final long serialVersionUID = 149448076119244819L;

	DPosition()
	{
		m_listenerStructure = new LStructureAdapter();		
	}
	
	DSymbol m_symbolSet;	
	transient LStructure m_listenerStructure;
	
	DPosition duplicatePosition()
	{
		DPosition positionReturn = new DPosition();
		positionReturn.m_symbolSet = m_symbolSet.duplicateSymbol();
		positionReturn.m_listenerStructure = m_listenerStructure;
		return positionReturn;
	}
	
	boolean addPositionListener(LStructure listener)
	{
		m_listenerStructure = listener;
		return true;
	}
	
	LStructure removePositionListener()
	{
		LStructure listener = m_listenerStructure;
		m_listenerStructure = new LStructureAdapter();
		return listener;
	}
	
	boolean setSymbol(DSymbol symbolSet)
	{
		m_symbolSet = symbolSet;
		return true;
	}
	
	DSymbol getSymbolSet()
	{
		return m_symbolSet;		
	}
	
	boolean fixSymbol(DSymbol symbolFix)
	{
		if (!m_symbolSet.fixSymbol(symbolFix)) return false;
		return m_listenerStructure.eventPosition(EPosition._FIX_SYMBOL, this, symbolFix);
	}
	
	boolean fixedSymbol()
	{
		return m_symbolSet.fixedSymbol();
	}
	
	DSymbol getUniqueSymbol()
	{
		return m_symbolSet.getUniqueSymbol();		
	}
	
	boolean constrainSymbol(DSymbol symbolConstrain)
	{
		if (m_symbolSet.uniqueSymbol()) return false;
		if (!m_symbolSet.containsSymbol(symbolConstrain)) return false;
		if (!m_symbolSet.constrainSymbol(symbolConstrain)) return false;
		return m_listenerStructure.eventPosition(EPosition._CONSTRAIN_SYMBOL, this, symbolConstrain);
	}
	
	boolean uniqueSymbol()
	{
		return m_symbolSet.uniqueSymbol();
	}
	
	boolean possibleSymbol(DSymbol symbolCheck)
	{
		return m_symbolSet.containsSymbol(symbolCheck);
	}
	
	boolean excludeSymbol(DSymbol symbolConstrain)
	{
		if (!m_symbolSet.containsSymbol(symbolConstrain)) return false;		
		if (!m_symbolSet.excludeSymbol(symbolConstrain)) return false;
		return m_listenerStructure.eventPosition(EPosition._EXCLUDE_SYMBOL, this, symbolConstrain);
	}
	
	static char getSquare(int iColumn, int iRow)
	{
		final char[][] aacSquare = {{'A', 'B', 'C'}, {'D', 'E', 'F'}, {'G', 'H', 'I'}};
		return aacSquare[iRow/3][iColumn/3];
	}
	
	static String getPositions(DPosition[] apositions)
	{
		StringBuilder sb = new StringBuilder();
		sb.append('<');
		String scSeparator = "";
		
		for (int iIndex = 0, iLength = apositions.length; iIndex<iLength; ++iIndex)
		{
			sb.append(scSeparator);
			sb.append(apositions[iIndex].toString());
			scSeparator = ", ";
		}
		
		sb.append('>');
		return sb.toString();
	}
	
	@Override
	public String toString()
	{
		return m_symbolSet.toString();
	}
}
