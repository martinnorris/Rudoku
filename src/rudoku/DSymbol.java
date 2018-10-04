package rudoku;

import java.io.Serializable;

public class DSymbol implements Serializable
{
	private static final long serialVersionUID = 485633488495581954L;

	DSymbol()
	{
		m_cFixedSymbol = '.';
		m_zFixed = false;
		m_scConstrains = "123456789";
		m_scExcludes = "";
	}
	
	DSymbol(char cSymbol)
	{
		m_cFixedSymbol = cSymbol;
		m_zFixed = true;
		m_scConstrains = "";
		m_scExcludes = null;
	}
	
	DSymbol(String scConstrains)
	{
		m_cFixedSymbol = ',';
		m_zFixed = false;
		m_scConstrains = scConstrains;
		m_scExcludes = null;
	}
	
	char m_cFixedSymbol;
	boolean m_zFixed;
	String m_scConstrains;
	String m_scExcludes;
	
	DSymbol duplicateSymbol()
	{
		DSymbol symbolReturn = new DSymbol();
		
		symbolReturn.m_cFixedSymbol = m_cFixedSymbol;
		symbolReturn.m_zFixed = m_zFixed;
		symbolReturn.m_scConstrains = new String(m_scConstrains);
		symbolReturn.m_scExcludes = new String(m_scExcludes);

		return symbolReturn;
	}

	boolean fixSymbol(DSymbol symbolFix)
	{
		m_cFixedSymbol = symbolFix.m_cFixedSymbol;
		m_zFixed = true;
		return true;
	}
	
	boolean fixedSymbol()
	{
		return m_zFixed;
	}
	
	int countSymbol()
	{
		if (m_zFixed) return 0;
		return m_scConstrains.length();
	}
	
	boolean uniqueSymbol()
	{
		return m_scConstrains.length()==1;
	}
	
	DSymbol getUniqueSymbol()
	{
		return new DSymbol(m_scConstrains.charAt(0));
	}
	
	boolean excludeSymbol(DSymbol symbolConstrain)
	{
		if (1==m_scConstrains.length()) throw new RuntimeException("Rule failed");
		
		char cCompare = symbolConstrain.m_cFixedSymbol;
		
		StringBuilder sbConstrains = new StringBuilder();
		for (int iIndex = 0, iLength = m_scConstrains.length(); iIndex<iLength; ++iIndex)
			if (m_scConstrains.charAt(iIndex)!=cCompare) sbConstrains.append(m_scConstrains.charAt(iIndex));
		m_scConstrains = sbConstrains.toString();
		
		StringBuilder sbExcludes = new StringBuilder(m_scExcludes);
		sbExcludes.append(cCompare);
		m_scExcludes = sbExcludes.toString();
		
		return true;		
	}
	
	boolean constrainSymbol(DSymbol symbolConstrain)
	{
		char cCompare = symbolConstrain.m_cFixedSymbol;
		
		StringBuilder sbExcludes = new StringBuilder(m_scExcludes);
		for (int iIndex = 0, iLength = m_scConstrains.length(); iIndex<iLength; ++iIndex)
			if (m_scConstrains.charAt(iIndex)!=cCompare) sbExcludes.append(m_scConstrains.charAt(iIndex));
		m_scExcludes = sbExcludes.toString();
		
		m_scConstrains = Character.toString(cCompare);
		return true;
	}
	
	boolean containsSymbol(DSymbol symbolCompare)
	{
		char cCompare = symbolCompare.m_cFixedSymbol;
		for (int iIndex = 0, iLength = m_scConstrains.length(); iIndex<iLength; ++iIndex)
			if (m_scConstrains.charAt(iIndex)==cCompare) return true;
		return false;
	}
	
	boolean mergeSymbol(DSymbol symbolAdd)
	{
		StringBuilder sbConstrains = new StringBuilder(m_scConstrains);
		
		for (DSymbol symbolTest = symbolAdd.iterateSymbol(); null!=symbolTest; symbolTest = symbolTest.nextSymbol())
		{
			if (containsSymbol(symbolTest)) continue;
			sbConstrains.append(symbolTest.m_cFixedSymbol);
		}
		
		m_scConstrains = sbConstrains.toString();
		return true;		
	}
	
	DSymbol allSymbols()
	{
		DSymbol symbolReturn = new DSymbol();
		return symbolReturn.nextSymbol();
	}
	
	DSymbol iterateSymbol()
	{
		DSymbol symbolReturn = duplicateSymbol();
		
		if (m_zFixed)
		{
			symbolReturn.m_scConstrains = "";
			return symbolReturn;
		}
		
		return symbolReturn.nextSymbol();		
	}
	
	DSymbol nextSymbol()
	{
		if (0==m_scConstrains.length()) return null;
		m_cFixedSymbol = m_scConstrains.charAt(0);
		m_scConstrains = m_scConstrains.substring(1);
		return this;
	}
	
	boolean sameSymbol(DSymbol symbolCheck)
	{
		return symbolCheck.m_cFixedSymbol==m_cFixedSymbol;
	}
	
	boolean sameSymbolSet(DSymbol symbolCheck)
	{
		if (m_scConstrains.length()!=symbolCheck.m_scConstrains.length()) return false;
		
		for (int iIndex = 0, iLength = m_scConstrains.length(); iIndex<iLength; ++iIndex)
			if (m_scConstrains.charAt(iIndex)!=symbolCheck.m_scConstrains.charAt(iIndex)) return false;
		
		return true;
	}
	
	@Override
	public String toString()
	{
		if (m_zFixed) return String.format("<%c>", m_cFixedSymbol);
		return m_scConstrains;
	}
}