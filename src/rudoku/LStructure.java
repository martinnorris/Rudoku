package rudoku;

public interface LStructure 
{
	public enum EStructure {_CHANGE_STRUCTURE};
	public boolean eventStructure(EStructure enumAction, DStructure structure);
	
	public enum EPosition {_ADD_POSITION, _FIX_SYMBOL, _CONSTRAIN_SYMBOL, _EXCLUDE_SYMBOL};
	public boolean eventPosition(EPosition enumAction, DPosition position, DSymbol symbol);
	
	public enum ESymbol {_INITIAL_SET};
	public boolean eventSymbol(ESymbol enumAction, DSymbol symbol);
	
	public enum ERule {_ADD_RULE, _APPLY_CONSTRAIN, _APPLY_REMOVE};
	public boolean eventRule(ERule enumAction, DRule rule, DPosition position, DSymbol symbol);
}

class LStructureAdapter implements LStructure
{
	LStructureAdapter()
	{
	}
	
	@Override
	public boolean eventStructure(EStructure enumAction, DStructure structure) 
	{
		return true;
	}

	@Override
	public boolean eventPosition(EPosition enumAction, DPosition position, DSymbol symbol) 
	{
		return true;
	}

	@Override
	public boolean eventSymbol(ESymbol enumAction, DSymbol symbol) 
	{
		return true;
	}

	@Override
	public boolean eventRule(ERule enumAction, DRule rule, DPosition position, DSymbol symbol) 
	{
		return true;
	}
}