package rudoku;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class Rudoku 
{
	void writeLine(String scFormat)
	{
		System.out.println(scFormat);
		return;
	}
	
	private String readLine(final BufferedReader readerSystem, final String scPrompt, final int iSeconds)
	{
		Callable<String> callWithTimeout = new Callable<String>()
		{
			@Override
			public String call() throws IOException 
			{
				String scReturn;

				System.out.print(scPrompt);
				
				try 
				{
					while (!readerSystem.ready()) Thread.sleep(100);
					scReturn = readerSystem.readLine();
				} 
				catch (InterruptedException e) 
				{
					return null;
				}
				
				return scReturn;
			}			
		};
		
		ExecutorService threadExecute = Executors.newSingleThreadExecutor();
		Future<String> futureReturn = threadExecute.submit(callWithTimeout);
		
		String scReturn = "Q";
		
		try 
		{
			scReturn = futureReturn.get(iSeconds, TimeUnit.SECONDS);
		} 
		catch (InterruptedException e) 
		{
		} 
		catch (ExecutionException e) 
		{
		} 
		catch (TimeoutException e) 
		{
			scReturn = "W";
		}
		finally
		{
			threadExecute.shutdownNow();
		}
		
		return scReturn;
	}
	
	boolean createConsole(final CStructure controller, String[] ascSetup)
	{
		AStructureSet actionPrevious = new AStructureSet("|");
		
		if (0<ascSetup.length)
		{
			for (int iIndex = 0; iIndex<ascSetup.length; ++iIndex)
				if (controller.interpretAction(null, actionPrevious, ascSetup[iIndex])) return true;
		}
		else
		{
			writeLine("Hit 'W' to display view, 'Q' to quit, or");
			controller.interpretAction(null, null, "HELP");
		}
		
		LStructure listenerStructure = new LStructure()
		{
			@Override
			public boolean eventStructure(EStructure enumAction, DStructure structure) 
			{
				return true;
			}

			@Override
			public boolean eventPosition(EPosition enumAction, DPosition position, DSymbol symbol) 
			{
				if (EPosition._FIX_SYMBOL==enumAction)
				{
					int iColumn = controller.m_structureDefined.getColumn(position);
					int iRow = controller.m_structureDefined.getRow(position);
					writeLine(String.format("Set %d,%d as %s", iColumn+1, iRow+1, symbol));
				}
				if (EPosition._CONSTRAIN_SYMBOL==enumAction)
				{
					int iColumn = controller.m_structureDefined.getColumn(position);
					int iRow = controller.m_structureDefined.getRow(position);
					writeLine(String.format("Now %d,%d has only %c", iColumn+1, iRow+1, symbol.m_cFixedSymbol));
				}
				if (EPosition._EXCLUDE_SYMBOL==enumAction)
				{
					int iColumn = controller.m_structureDefined.getColumn(position);
					int iRow = controller.m_structureDefined.getRow(position);
					writeLine(String.format("Now %d,%d removed %c has %s", iColumn+1, iRow+1, symbol.m_cFixedSymbol, position.m_symbolSet.m_scConstrains));
				}
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
				if (ERule._APPLY_CONSTRAIN==enumAction)
				{
					int iColumn = controller.m_structureDefined.getColumn(position);
					int iRow = controller.m_structureDefined.getRow(position);
					writeLine(rule.toString() + String.format("applied %d,%d must contain %c", iColumn+1, iRow+1, symbol.m_cFixedSymbol, position.getSymbolSet().m_scConstrains));
				}
				if (ERule._APPLY_REMOVE==enumAction)
				{
					int iColumn = controller.m_structureDefined.getColumn(position);
					int iRow = controller.m_structureDefined.getRow(position);
					writeLine(rule.toString() + String.format("applied %d,%d cannot contain %c only has %s", iColumn+1, iRow+1, symbol.m_cFixedSymbol, position.getSymbolSet().m_scConstrains));
				}
				return true;
			}
		};
		
		controller.addStructureListener(listenerStructure);
		
		if (createConsole(controller, actionPrevious))
		{
			// Close console
			writeLine("... quit");
			return true;
		}
		
		// Go interactive
		writeLine("... interactive");
		return false;
	}
	
	boolean createConsole(CStructure controller, AStructureSet actionPrevious)
	{
		// Console does not exist in some cases so use System.in
		BufferedReader readerSystem = new BufferedReader(new InputStreamReader(System.in));

		for (int iTimeout = 5;; iTimeout = 300)
		{
			String scAction = readLine(readerSystem, "> ", iTimeout);
			
			if (scAction.startsWith("W")) return false; // end console - open interactive
			if (scAction.startsWith("Q")) return true; // end console - end app

			if (!controller.interpretAction(null, actionPrevious, scAction)) break;
		}
		
		return true;
	}
	
	public static void main(String[] args) 
	{
		CStructure structureCreated = new CStructure(new DStructure());
		
		Rudoku main = new Rudoku();
		if (main.createConsole(structureCreated, args)) return;
		
		VStructure structurePanel = new VStructure(structureCreated);
		Helper.frameView(structurePanel, "Configure structure");
	}

}

/*
	class StringArrayReader extends Reader
	{
		StringArrayReader(String[] ascStrings)
		{
			m_ascStrings = ascStrings;
			m_readerString = new StringReader(ascStrings[0]);
		}
		
		String[] m_ascStrings;
		int m_iIndex = 0;
		StringReader m_readerString;
		
		@Override
		public void close() throws IOException 
		{
			m_readerString.close();
			return;
		}
		
		@Override
		public void mark(int iLimit) throws IOException
		{
			try
			{
				m_readerString.mark(iLimit);
			}
			catch (IOException x)
			{
				if (m_ascStrings.length>++m_iIndex)
				{
					m_readerString = new StringReader(m_ascStrings[m_iIndex]);
					mark(iLimit);
					return;
				}
				throw x;
			}
			return;
		}
		
		@Override
		public boolean markSupported()
		{
			return m_readerString.markSupported();
		}
		
		@Override
		public int read() throws IOException 
		{
			try
			{
				return m_readerString.read();
			}
			catch (IOException x)
			{
				if (m_ascStrings.length>++m_iIndex)
				{
					m_readerString = new StringReader(m_ascStrings[m_iIndex]);
					return read();
				}
				throw x;
			}			
		}

		@Override
		public int read(char[] cbuf) throws IOException 
		{
			try
			{
				return m_readerString.read(cbuf);
			}
			catch (IOException x)
			{
				if (m_ascStrings.length>++m_iIndex)
				{
					m_readerString = new StringReader(m_ascStrings[m_iIndex]);
					return read(cbuf);
				}
				throw x;
			}
		}
		
		@Override
		public int read(char[] cbuf, int off, int len) throws IOException 
		{
			try
			{
				return m_readerString.read(cbuf, off, len);
			}
			catch (IOException x)
			{
				if (m_ascStrings.length>++m_iIndex)
				{
					m_readerString = new StringReader(m_ascStrings[m_iIndex]);
					return read(cbuf, off, len);
				}
				throw x;
			}
		}
	}
	
 */

