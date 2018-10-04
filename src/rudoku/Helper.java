package rudoku;

import java.awt.Point;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

public class Helper 
{
	static void frameView(final JPanel componentPanel, final String scFrameTitle)
	{
		Runnable startLater = new Runnable()
		{
			@Override
			public void run()
			{
				JFrame frameDice = new JFrame(scFrameTitle);
				frameDice.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
				
				frameDice.add(componentPanel);
				frameDice.pack();
				
				frameDice.setLocation(new Point(200, 200));
				frameDice.setVisible(true);
			}
		};
		
		SwingUtilities.invokeLater(startLater);

		return;
	}
}
