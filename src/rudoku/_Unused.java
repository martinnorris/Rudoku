package rudoku;

import java.util.List;

import rudoku.LStructure.ERule;

/**
 * Classic X-Wing - N-fish with N=2
 * Where a symbol is limited in two rows/columns intersection columns/rows have symbol constrained
 * e.g. [123] [123] [123] [45678] [678] [678] [45678] [678] <9>
 *      [123] [123] [123] [45678] [678] <9>   [45678] [678] [678]
 *                           V                   V
 * Symbols [45] are restricted in intersecting rules
 */
class DRulePairs extends DRuleComplete
{
	private static final long serialVersionUID = -4517177320361491439L;

	// Need public constructor to build from reflection
	public DRulePairs()
	{
	}
	
	@Override
	boolean foundRuleSymbol(DSymbol symbolCheck)
	{
		// Perform sequencing with subclass
		DRuleResult resultRule = new DRuleResult();
		
		// Go through the list of rules finding where two positions are the same
		for (DRuleSquare rule : m_listRulesCheck)
		{
			if (sequenceRule(resultRule, symbolCheck, rule)) return applyConstrain(false, resultRule);
		}
		
		return false;
	}
	
	enum ERuleSequence {_A_FIRST, _A_SECOND, _A_NO_MORE, _B_FIRST, _B_SECOND, _B_NO_MORE};
	
	class DRuleResult
	{
		ERuleSequence m_qRule = ERuleSequence._A_FIRST;
		
		DSymbol m_symbol;
		DRuleSquare m_ruleA;
		DRuleSquare m_ruleB;
		int m_iFirst;
		int m_iSecond;
		
		int m_iOffsetCount;
		
		boolean setResult(boolean zFound, DRuleSquare rule, int iIndex)
		{
			switch (m_qRule)
			{
			case _A_FIRST:
				if (!zFound) return resetResult();
				m_qRule = ERuleSequence._A_SECOND;
				m_iFirst = iIndex;
				break;
			case _A_SECOND:
				if (!zFound) return resetResult();
				m_qRule = ERuleSequence._A_NO_MORE;
				m_iSecond = iIndex;
				break;
			case _A_NO_MORE:
				if (zFound) return resetResult();
				m_ruleA = rule;
				break;
				
			case _B_FIRST:
				if (!zFound) return resetResult();
				if (m_iFirst!=iIndex) return resetResult();
				m_qRule = ERuleSequence._B_SECOND;
				break;
			case _B_SECOND:
				if (!zFound) return resetResult();
				if (m_iSecond!=iIndex) return resetResult();
				m_qRule = ERuleSequence._B_NO_MORE;
				break;
			case _B_NO_MORE:
				if (zFound) return resetResult();
				m_ruleB = rule;
				break;				
			}
			return true;
		}
		
		boolean resetResult()
		{
			switch (m_qRule)
			{
			case _A_FIRST:
			case _A_SECOND:
			case _A_NO_MORE:
				m_qRule = ERuleSequence._A_FIRST;
				break;
				
			case _B_FIRST:
			case _B_SECOND:
			case _B_NO_MORE:
				m_qRule = ERuleSequence._B_FIRST;
				break;
				
			}
			return false;
		}
		
		boolean nextRule(DSymbol symbolCheck)
		{
			switch (m_qRule)
			{
			case _A_NO_MORE:
				m_qRule = ERuleSequence._B_FIRST;
				return false; // check next rule
			case _B_NO_MORE:
				m_symbol = symbolCheck;
				return true; // complete
			default:
				break;
			}
			
			throw new RuntimeException("Should never get here");
		}
	}

	boolean sequenceRule(DRuleResult resultRule, DSymbol symbolCheck, DRuleSquare rule)
	{
		// First symbol must be found as a constraint
		if (!foundSymbol(resultRule, symbolCheck, rule, 0, rule.m_iOffsetCount)) return false;
		// Second symbol must be found as a constraint again
		if (!foundSymbol(resultRule, symbolCheck, rule, resultRule.m_iFirst+1, rule.m_iOffsetCount)) return false;
		// But not in any place after
		if (!foundSymbol(resultRule, symbolCheck, rule, resultRule.m_iSecond+1, rule.m_iOffsetCount)) return false;

		return resultRule.nextRule(symbolCheck);
	}
	
	boolean foundSymbol(DRuleResult resultRule, DSymbol symbolCheck, DRuleSquare rule, int iIndexCheck, int iIndexCount)
	{
		DPosition[] aPositions = rule.m_apositionReference;
		
		for (; iIndexCheck<iIndexCount; ++iIndexCheck)
		{
			if (aPositions[iIndexCheck].fixedSymbol()) continue;
			if (aPositions[iIndexCheck].possibleSymbol(symbolCheck)) return resultRule.setResult(true, rule, iIndexCheck);
		}		
		return resultRule.setResult(false, rule, iIndexCount);		
	}
	
	boolean applyConstrain(boolean zReturn, DRuleResult resultRule)
	{
		// Go through the list of rules finding where two positions are the same
		for (DRuleSquare rule : m_listRulesConstrain)
		{
			zReturn |= intersectionResult(resultRule, rule);
		}
		
		return zReturn;
	}
	
	boolean intersectionResult(DRuleResult resultRule, DRuleSquare ruleCheck)
	{
		DPosition[] apositionComplementary = ruleCheck.m_apositionReference;
		DPosition[] apositionResult = resultRule.m_ruleA.m_apositionReference;
		
		for (int iIndexCheck = 0; iIndexCheck<ruleCheck.m_iOffsetCount; ++iIndexCheck)
		{
			// Any intersection is sufficient to apply constrain
			if (apositionComplementary[iIndexCheck]==apositionResult[resultRule.m_iFirst]) return excludeSymbol(false, resultRule, ruleCheck, 0);
			if (apositionComplementary[iIndexCheck]==apositionResult[resultRule.m_iSecond]) return excludeSymbol(false, resultRule, ruleCheck, 0);
		}
		
		return false;
	}
	
	boolean excludeSymbol(boolean zReturn, DRuleResult resultRule, DRuleSquare ruleCheck, int iIndex)
	{
		DPosition[] apositionComplementary = ruleCheck.m_apositionReference;
		DPosition[] apositionResultA = resultRule.m_ruleA.m_apositionReference;
		DPosition[] apositionResultB = resultRule.m_ruleB.m_apositionReference;
		
		for (; iIndex<ruleCheck.m_iOffsetCount; ++iIndex)
		{
			DPosition position = apositionComplementary[iIndex];
			// Skip any intersection
			if (position==apositionResultA[resultRule.m_iFirst]) continue;
			if (position==apositionResultA[resultRule.m_iSecond]) continue;
			if (position==apositionResultB[resultRule.m_iFirst]) continue;
			if (position==apositionResultB[resultRule.m_iSecond]) continue;
			// Apply any constrain
			if (!position.excludeSymbol(resultRule.m_symbol)) continue;
			if (!m_listenerStructure.eventRule(ERule._APPLY_REMOVE, this, position, resultRule.m_symbol)) continue;
			return excludeSymbol(true, resultRule, ruleCheck, ++iIndex);
		}
		
		return zReturn;
	}	
}

class DRulePairsInColumns extends DRulePairs
{
	private static final long serialVersionUID = -7169310645162467417L;
	
	public DRulePairsInColumns() // Have an explicit public constructor so can be created by reflection
	{
		super();
	}
	
	@Override
	boolean findIntersectingRules(List<DRule> listRules)
	{
		for (DRule rule : listRules)
		{
			if (rule instanceof DRuleColumn) m_listRulesCheck.add((DRuleSquare)rule);
			if (rule instanceof DRuleRow) m_listRulesConstrain.add((DRuleSquare)rule);
		}
		return true;
	}	

	@Override
	String setName(int iColumn, int iRow)
	{
		m_scName = "Pairs in columns ";
		return m_scName;
	}	
}

class DRulePairsInRows extends DRulePairs
{
	private static final long serialVersionUID = 3732191274545233092L;

	public DRulePairsInRows() // Public constructor so can be created by reflection
	{
		super();
	}
	
	@Override
	boolean findIntersectingRules(List<DRule> listRules)
	{
		for (DRule rule : listRules)
		{
			if (rule instanceof DRuleRow) m_listRulesCheck.add((DRuleSquare)rule);
			if (rule instanceof DRuleColumn) m_listRulesConstrain.add((DRuleSquare)rule);
		}
		return true;
	}
	
	@Override
	String setName(int iColumn, int iRow)
	{
		m_scName = "Pairs in rows ";
		return m_scName;
	}	
}

