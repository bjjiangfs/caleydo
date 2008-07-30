package org.caleydo.core.parser.xml.sax;

import org.caleydo.core.data.xml.IMementoCallbackXML;
import org.caleydo.core.util.exception.CaleydoRuntimeException;
import org.caleydo.core.util.exception.CaleydoRuntimeExceptionType;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.LocatorImpl;

/**
 * Base class for SAX Parser containing several useful methods.
 * 
 * @author Michael Kalkusch
 */
public abstract class ASaxParserHandler
	extends DefaultHandler
	implements ISaxParserHandler
{

	/**
	 * Buffer for error messages. An error message always sets the flag
	 * org.caleydo.core.net.dwt.swing.DParseBaseSaxHandler#bErrorWhileParsing
	 * true.
	 * 
	 * @see org.caleydo.core.net.dwt.swing.parser.ASaxParserHandler#bErrorWhileParsing
	 * @see org.caleydo.core.net.dwt.swing.parser.ASaxParserHandler#sInfoMessage
	 */
	private String sErrorMessage;

	/**
	 * Reference to the local Locator
	 */
	private LocatorImpl locator;

	/**
	 * Reference to the calling MenmentoXML object.
	 */
	protected IMementoCallbackXML parentMementoCaller = null;

	/**
	 * This variabel defines, if a parsing error shall throw a
	 * org.xml.sax.SAXParseException. If an parsing error occurs the variabel
	 * org.caleydo.core.net.dwt.swing.DParseBaseSaxHandler#bErrorWhileParsing is
	 * set to true.
	 * 
	 * @see org.caleydo.core.net.dwt.swing.parser.ASaxParserHandler#bErrorWhileParsing
	 */
	protected boolean bErrorHaltParsingOnError = true;

	/**
	 * This variable indicates, that an error has occurred. With respect to the
	 * variabel org.caleydo.core.net.dwt.swing.DParseBaseSaxHandler#
	 * bError_HaltParsingOnError the parsing is interrupted.
	 * 
	 * @see org.caleydo.core.net.dwt.swing.parser.ASaxParserHandler#bErrorHaltParsingOnError
	 */
	protected boolean bErrorWhileParsing = false;

	/**
	 * Default Constructor. Sets bEnableHaltOnParsingError = false.
	 */
	public ASaxParserHandler()
	{

		super();
		reset();
		setSaxHandlerLocator(new LocatorImpl());
	}

	/**
	 * Constructor with bEnableHaltOnParsingError.
	 * 
	 * @param bEnableHaltOnParsingError
	 *            enabels or disables halting on errors
	 */
	public ASaxParserHandler(final boolean bEnableHaltOnParsingError)
	{

		super();
		reset();
		setSaxHandlerLocator(new LocatorImpl());

		bErrorHaltParsingOnError = bEnableHaltOnParsingError;
	}

	/**
	 * Tells if a parsing error will cause an abortion of parsing.
	 * 
	 * @see org.caleydo.core.net.dwt.swing.parser.ASaxParserHandler#bErrorHaltParsingOnError
	 * @return true if a parsing error will cause an parsing abortion
	 */
	final protected boolean isHaltOnParsingErrorSet()
	{

		return bErrorHaltParsingOnError;
	}

	/**
	 * Sets the reference to the current Locator
	 * 
	 * @see org.xml.sax.helpers.LocatorImpl
	 * @see org.xml.sax.Locator
	 * @param setLocator
	 */
	final protected void setSaxHandlerLocator(LocatorImpl setLocator)
	{

		assert setLocator != null : "setSaxHandlerLocator() Error due to null-pointer";

		System.out.println("ASaxParserHandler.setSaxHandlerLocator() DUMDIDUM");

		this.locator = setLocator;
		setDocumentLocator(locator);
	}

	/**
	 * Details on current location during parsing.
	 * 
	 * @return current line and column number
	 */
	final protected String detailsOnLocationInXML()
	{

		return " line=" + locator.getLineNumber() + ":" + locator.getColumnNumber();
	}

	/**
	 * Append an error message and set the "error-has-occurred"-flag true. In
	 * comparision
	 * org.caleydo.core.net.dwt.swing.DParseBaseSaxHandler#appandInfoMsg(String)
	 * sets an info message without setting the "error-has-occurred"-flag true.
	 * 
	 * @see org.caleydo.core.net.dwt.swing.parser.ASaxParserHandler#getErrorMessage()
	 * @see org.caleydo.core.net.dwt.swing.parser.ASaxParserHandler#bErrorWhileParsing
	 * @see org.caleydo.core.net.dwt.swing.parser.ASaxParserHandler#appandInfoMsg(String)
	 * @param errorMessage
	 *            new error message
	 */
	final protected void appandErrorMsg(final String errorMessage)
	{

		if (sErrorMessage.length() > 1)
		{
			sErrorMessage += "\n";
		}
		sErrorMessage += errorMessage + detailsOnLocationInXML();

		if (bErrorHaltParsingOnError)
		{
			try
			{
				this.fatalError(new SAXParseException("ParseException due to " + errorMessage,
						locator));
			}
			catch (SAXException s_e)
			{
				throw new CaleydoRuntimeException(s_e.toString(),
						CaleydoRuntimeExceptionType.SAXPARSER);
			}
		}
		if (!bErrorWhileParsing)
			bErrorWhileParsing = true;
	}

	/**
	 * Test if parsing was successful. To get error message call
	 * org.caleydo.core.net.dwt.swing.DButtonSaxHandler#getErrorMessage()
	 * 
	 * @return TRUE if an error occurred on parsing.
	 * @see org.caleydo.core.net.dwt.swing.parser.ASaxParserHandler#getErrorMessage()
	 */
	public final boolean hasErrorWhileParsing()
	{

		return bErrorWhileParsing;
	}

	/**
	 * Important: all derived classes must call super.reset() inside their
	 * reset() call to not cause side effects!
	 * 
	 * @see org.caleydo.core.parser.xml.sax.ISaxParserHandler#reset()
	 */
	public void reset()
	{

		sErrorMessage = "";
		bErrorWhileParsing = false;
	}

	/**
	 * Sets the reference to the calling memento object. Used to tigger a
	 * callback event
	 * 
	 * @see
	 * @param setRefParent
	 *            reference to the parent obejct or the object, that sould be
	 *            triggert in case of a callback action
	 */
	public void setParentMementoCaller(IMementoCallbackXML setRefParent)
	{

		parentMementoCaller = setRefParent;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.caleydo.core.net.dwt.swing.DParseSaxHandler#startElement(Stringt,
	 * Stringt, Stringt, org.xml.sax.Attributes)
	 */
	public abstract void startElement(String uri, String localName, String qName,
			Attributes attributes);

	/*
	 * (non-Javadoc)
	 * @see org.caleydo.core.net.dwt.swing.DParseSaxHandler#endElement(Stringt,
	 * Stringt, Stringt)
	 */
	public abstract void endElement(String uri, String localName, String qName);
}
