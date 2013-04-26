/*  VoteTec
 *  Copyright (C) 2009 Eric Cederstrand, Carsten Schuermann
 *  and Kenneth Sjøholm
 *
 *  This file is part of VoteTec.
 *
 *  VoteTec is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  VoteTec is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with VoteTec.  If not, see <http://www.gnu.org/licenses/>.
 */

package model;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import org.w3c.dom.*;

import java.util.Map;
import java.util.HashMap;

/* Takes an XML file, validates it according to the DTD, and constructs a 
 * tree of Groups and Choices. */
public class XMLRecursiveReader {
	//Global value so it can be ref'd by the tree-adapter
	private Document document;
	private final File file;
	private Group root;
	private Map<String, Election> map;

	public XMLRecursiveReader(File file) {
		this.file = file;
		readXML();
	}

	public Group getRoot() { return root; }
	public Map<String, Election> getMap() { return map; }

	private void readXML() {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setValidating(true);
		factory.setNamespaceAware(true);
		try {
			DocumentBuilder builder = factory.newDocumentBuilder();

			builder.setErrorHandler(new org.xml.sax.ErrorHandler() {
				// ignore fatal errors (an exception is guaranteed)
				public void fatalError(SAXParseException exception) throws SAXException {}
					// treat validation errors as fatal
					public void error(SAXParseException e) throws SAXParseException { throw e; }

					// dump warnings too
					public void warning(SAXParseException err) throws SAXParseException {
						System.out.println("** Warning" + ", line "
								+ err.getLineNumber() + ", uri "
								+ err.getSystemId());
						System.out.println("   " + err.getMessage());
					}
				});

			document = builder.parse(file);

			// Make the election objects
			root = new Group("Root", document.getLastChild().getNodeName(), 0, 1, null);
			map = new HashMap<String, Election>();
			walk(document.getLastChild(), root);

		} catch (SAXException sxe) {
			// Error generated during parsing)
			Exception x = sxe;
			if (sxe.getException() != null)
				x = sxe.getException();
			x.printStackTrace();

		} catch (ParserConfigurationException pce) {
			// Parser with specified options can't be built
			pce.printStackTrace();
		} catch (IOException ioe) { ioe.printStackTrace(); }
	}

	//walk the DOM tree and print as you go    
	private void walk(Node node, Group parentGroup) {
		int min, max, type = node.getNodeType();
		String id, value, nodeName;
		NamedNodeMap nnm;
		Group group = null;

		switch (type) {
			case Node.ELEMENT_NODE: {
				nodeName = node.getNodeName();
				if (!nodeName.equalsIgnoreCase("election")) {
					nnm = node.getAttributes();
					// Collect node attributes
					id = nnm.getNamedItem("id").getNodeValue();
					value = nnm.getNamedItem("value").getNodeValue();
					min = Integer.parseInt(nnm.getNamedItem("min").getNodeValue());
					max = Integer.parseInt(nnm.getNamedItem("max").getNodeValue());
	
					if (nodeName.equalsIgnoreCase("group")) {
						group = new Group(id, value, min, max, parentGroup);
						group.setVotesLeft(max);
						parentGroup.addGroup(group);
						map.put(group.getId(), group);
					} else if (nodeName.equalsIgnoreCase("choice")) {
						Choice choice = new Choice(id, value, min, max, parentGroup);
						parentGroup.addChoice(choice);
						map.put(choice.getId(), choice);
					}
				} else if (nodeName.equalsIgnoreCase("election")) {
					group = parentGroup;
					map.put(group.getId(), group);
				}
				break;
			}
			default: { break; }
		} //end of switch      

		//recurse        
		for (Node child = node.getFirstChild(); child != null; child = child.getNextSibling()) {
			walk(child, group);
		}
	}//end of walk
}
