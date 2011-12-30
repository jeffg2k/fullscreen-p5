/*
  Part of the Processing Fullscreen API

  Copyright (c) 2006-08 Hansi Raber

  This library is free software; you can redistribute it and/or
  modify it under the terms of the GNU General Public
  License as published by the Free Software Foundation; either
  version 3 of the License, or (at your option) any later version.

  This library is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  Lesser General Public License for more details.

  You should have received a copy of the GNU General
  Public License along with this library; if not, write to the
  Free Software Foundation, Inc., 59 Temple Place, Suite 330,
  Boston, MA  02111-1307  USA
*/
package fullscreen;

import japplemenubar.JAppleMenuBar;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;

import processing.core.PApplet;
import processing.core.PConstants;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

/**
 *  Creates a new softfullscreen object. <br>
 *  
 *  This will use undecorated frames to bring your sketch to the screen. <br>
 *  The advantages are: 
 *  
 *  <ul>
 *    <li>You can create a sketch that spans across multiple monitors easily</li>
 *  </ul>
 *  
 *  The drawbacks are: 
 *  <ul>
 *    <li>You cannot change resolution</li>
 *    <li>Screensaver must be disabled manually</li>
 *    <li>Notifications and other kinds of annoying popups might just show up on top of your sketch</li>
 *  </ul>
 */

public class SoftFullScreen extends FullScreenBase{
	// We use this frame to go to fullScreen mode...
	Frame fsFrame; 
	GraphicsDevice fsDevice;
	
	//AWTEventListener fsKeyListener;
	
	// the first time wait until the frame is displayed
	boolean fsIsInitialized; 
	
	// Daddy...
	PApplet dad; 
	
	
	/**
	 * Creates a new softfullscreen object. 
	 * 
	 * @param dad The parent sketch (aka "this")
	 */
	public SoftFullScreen( PApplet dad ){
		this( dad, 0 ); 
	}
	
	/**
	 * Creates a new softfullscreen object on a specific screen 
	 * (numbering starts at 0)
	 * 
	 * @param dad The parent sketch (usually "this")
	 * @param screenNr The screen number. 
	 */
	public SoftFullScreen( PApplet dad, int screenNr ){
		super( dad ); 
		this.dad = dad;
		
		GraphicsDevice[] devices = GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices();
		if( screenNr >= devices.length ){
			System.err.println( "FullScreen API: You requested to use screen nr. " + screenNr + ", " ); 
			System.err.println( "however, there are only " + devices.length + " screens in your environment. " ); 
			System.err.println( "Continuing with screen nr. 0" );
			screenNr = 0; 
		}
		
		fsDevice = devices[screenNr];
		final WindowListener listener = new WindowAdapter() {
			@Override
			public void windowDeiconified(final WindowEvent w) {
				if (isFullScreen()) {
					informDad(w, WindowEvent.WINDOW_DEICONIFIED);
					if (PApplet.platform == PConstants.MACOSX) {
						new JAppleMenuBar().setVisible(false);
					}
				}

			}
			
			@Override
			public void windowClosing(final WindowEvent w) {
				if (isFullScreen()) {
					informDad(w, WindowEvent.WINDOW_CLOSING);
				}
			}

			@Override
			public void windowClosed(final WindowEvent w) {
				if (isFullScreen()) {
					informDad(w, WindowEvent.WINDOW_CLOSED);
				}
			}

			@Override
			public void windowOpened(final WindowEvent w) {
				if (isFullScreen()) {
					informDad(w, WindowEvent.WINDOW_OPENED);
				}
			}

			@Override
			public void windowIconified(final WindowEvent w) {
				if (isFullScreen()) {
					informDad(w, WindowEvent.WINDOW_ICONIFIED);
				}
			}

			@Override
			public void windowActivated(final WindowEvent w) {
				if (isFullScreen()) {
					informDad(w, WindowEvent.WINDOW_ACTIVATED);
				}
			}

			@Override
			public void windowDeactivated(final WindowEvent w) {
				if (isFullScreen()) {
					informDad(w, WindowEvent.WINDOW_DEACTIVATED);
				}
			}
		};


		fsFrame = new Frame( fsDevice.getDefaultConfiguration() );
		fsFrame.addWindowListener(listener);
		fsFrame.setTitle( dad.frame == null? "":dad.frame.getTitle() );
		fsFrame.setIconImage( dad.frame.getIconImage() );
		fsFrame.setUndecorated( true ); 
		fsFrame.setBackground( Color.black ); 
		fsFrame.setLayout( null ); 
		fsFrame.setSize( 
			Math.max( fsDevice.getDisplayMode().getWidth(), dad.width ), 
			Math.max( fsDevice.getDisplayMode().getHeight(), dad.height )
		);
		
		registerFrame( fsFrame ); 
	}
	
	private void informDad(final WindowEvent theEvent, final int theAction) {
		final WindowListener[] dadlistens = dad.frame.getWindowListeners();
		for (int i = 0; i < dadlistens.length; i++) {
			final WindowListener advice = dadlistens[i];
			switch (theAction) {
			case WindowEvent.WINDOW_DEACTIVATED:
				advice.windowDeactivated(theEvent);
				break;
			case WindowEvent.WINDOW_ACTIVATED:
				advice.windowActivated(theEvent);
				break;
			case WindowEvent.WINDOW_ICONIFIED:
				//advice.windowIconified(theEvent);
				break;
			case WindowEvent.WINDOW_DEICONIFIED:
				//advice.windowDeiconified(theEvent);
				break;
			case WindowEvent.WINDOW_CLOSED:
				advice.windowClosed(theEvent);
				break;
			case WindowEvent.WINDOW_CLOSING:
				advice.windowClosing(theEvent);
				break;
			case WindowEvent.WINDOW_OPENED:
				advice.windowOpened(theEvent);
				break;
			}
		}
	}
	
	/**
	 * Are we in FullScreen mode? 
	 *
	 * @return true if so, yes if not
	 */
	public boolean isFullScreen(){
		return fsFrame.isVisible();  
	}
	
	/**
	 * Allow for minimizing the frame
	 */
	public void minimize(){
		if( isFullScreen() ){
			if( PApplet.platform == PConstants.MACOSX ){
				new JAppleMenuBar().setVisible( true );
			}
			fsFrame.setState( Frame.ICONIFIED );
		}
		else{
			dad.frame.setState( Frame.ICONIFIED );
		}
	}
	
	/**
	 * Restores the frame after it has been minimized. 
	 * If it wasn't minimized this doesn't do much! 
	 */
	public void restore(){
		if( isFullScreen() ){
			fsFrame.setState( Frame.NORMAL ); 
			
			if( PApplet.platform == PConstants.MACOSX ){
				new JAppleMenuBar().setVisible( false );
			}
		}
		else{
			dad.frame.setState( Frame.NORMAL ); 
		}
	}

	
	/**
	 * FullScreen is only available is applications, not in applets! 
	 *
	 * @return true if fullScreen mode is available
	 */
	public boolean available(){
		return dad.frame != null;
	}
	
	
	/**
	 * Enters/Leaves fullScreen mode. 
	 *
	 * @param fullScreen true or false
	 */
	public void setFullScreen( final boolean fullScreen ){
		new DelayedAction( 2 ){
			public void action(){
				setFullScreenImpl( fullScreen ); 
			}
		};
	}
	
	public java.awt.Point getDadLocation() {
		java.awt.Point p = new java.awt.Point(0,0);
		boolean usesEntireScreen = fsDevice.getDefaultConfiguration().getBounds().getSize().equals( new Dimension( dad.width, dad.height ) );
		int appleDriversSuck = PApplet.platform == PConstants.MACOSX && usesEntireScreen? 1:0;
		
		if (!dad.frame.isVisible()) {
			p.x = fsFrame.getWidth() - dad.width ) / 2;
			p.y = fsFrame.getHeight() - dad.height ) / 2 - appleDriversSuck;
		} else {
			p.x = frame.getLocation().x + frame.getWidth() - dad.width;
			p.y = frame.getLocation().y + frame.getHeight() - dad.height;
		}
		return p;
	}


	@SuppressWarnings("deprecation")
	private void setFullScreenImpl( boolean fullScreen ){
		if( fullScreen == isFullScreen() ){
			// no change required! 
			return; 
		}
		else if( fullScreen ){
			if( available() ){
				// remove applet from processing frame and attach to fsFrame
				dad.frame.setVisible( false ); 
				fsFrame.add( dad ); 
				
				if( PApplet.platform == PConstants.MACOSX ){
					new JAppleMenuBar().setVisible( false ); 
				}
				
				fsFrame.setVisible( true ); 
				fsFrame.setLocation( fsDevice.getDefaultConfiguration().getBounds().getLocation() );
				
				java.awt.Point p = getDadLocation();
				dad.setLocation(p.x, p.y);

				fsFrame.setExtendedState( Frame.MAXIMIZED_BOTH );		
				
				GLDrawableHelper.reAllocate( this ); 
				GLTextureUpdateHelper.update( this ); 
				
				requestFocus();
				notifySketch( dad );
				
				return; 
			}
			else{
				System.err.println( "FullScreen API: Fullscreen mode not available" ); 
				return; 
			}
		}
		else{
			// remove applet from fsFrame and attach to processing frame
			fsFrame.setVisible( false ); 
			fsFrame.removeAll(); 
			dad.frame.add( dad ); 
			dad.setLocation( dad.frame.insets().left, dad.frame.insets().top );
			
			// processing.core.fullscreen_texturehelper.update( dad );
			if( PApplet.platform == PConstants.MACOSX ){
				new JAppleMenuBar().setVisible( true );
			}
			
			dad.frame.setVisible( true ); 
			
			GLDrawableHelper.reAllocate( this ); 
			GLTextureUpdateHelper.update( this ); 
			
			requestFocus();
			notifySketch( dad ); 
			
			return; 
		}
	}

        public boolean isAlwaysOnTop() {
              return fsFrame.isAlwaysOnTop();
        }

        public void setAlwaysOnTop(final boolean ontop) {
            if (ontop) {
                new DelayedAction( 2 ) {
                    public void action() {
                        fsFrame.setAlwaysOnTop(ontop);
                    }
                };
            } 
            else {
                fsFrame.setAlwaysOnTop(ontop);
            }
        }

	/**
	 * Setting resolution is not possible with the SoftFullScreen object. 
	 */
	@Override
	public void setResolution( int xRes, int yRes ) {
		System.err.println( "Changing resolution is not supported in SoftFullScreen mode. " ); 
		System.err.println( "Use the normal FullScreen mode to make use of that functionality.  " ); 
	}
}
