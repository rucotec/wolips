package jp.aonir.fuzzyxml;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jp.aonir.fuzzyxml.event.FuzzyXMLErrorEvent;
import jp.aonir.fuzzyxml.event.FuzzyXMLErrorListener;
import jp.aonir.fuzzyxml.internal.FuzzyXMLAttributeImpl;
import jp.aonir.fuzzyxml.internal.FuzzyXMLCDATAImpl;
import jp.aonir.fuzzyxml.internal.FuzzyXMLCommentImpl;
import jp.aonir.fuzzyxml.internal.FuzzyXMLDocTypeImpl;
import jp.aonir.fuzzyxml.internal.FuzzyXMLDocumentImpl;
import jp.aonir.fuzzyxml.internal.FuzzyXMLElementImpl;
import jp.aonir.fuzzyxml.internal.FuzzyXMLProcessingInstructionImpl;
import jp.aonir.fuzzyxml.internal.FuzzyXMLTextImpl;
import jp.aonir.fuzzyxml.internal.FuzzyXMLUtil;
import jp.aonir.fuzzyxml.resources.Messages;

import org.objectstyle.wolips.wodclipse.core.util.WodHtmlUtils;

public class FuzzyXMLParser {

  private Stack<FuzzyXMLNode> _stack = new Stack<FuzzyXMLNode>();
  private String _originalSource;
  private List<FuzzyXMLNode> _roots;
  private FuzzyXMLDocType _docType;

  private List<FuzzyXMLErrorListener> _listeners = new ArrayList<FuzzyXMLErrorListener>();
  private List<FuzzyXMLElement> _nonCloseElements = new ArrayList<FuzzyXMLElement>();
  private List<String> _looseNamespaces = new ArrayList<String>();
  private List<String> _autocloseTags = new ArrayList<String>();
  private List<String> _looseTags = new ArrayList<String>();

  private boolean _wo54 = false;
  private boolean _isHTML = false;

  // �p�[�X�Ɏg�p���鐳�K�\��
  private Pattern _tag = Pattern.compile("<((|/)([^<>]*))([^<]|>)");
  //	private Pattern attr = Pattern.compile("([\\w:]+?)\\s*=(\"|')([^\"]*?)\\2");
  private Pattern _docTypeName = Pattern.compile("^<!DOCTYPE[ \r\n\t]+([\\w\\-_]*)");
  private Pattern _docTypePublic = Pattern.compile("PUBLIC[ \r\n\t]+\"(.*?)\"[ \r\n\t]*\"*(.*?)\"*");
  private Pattern _docTypeSystem = Pattern.compile("SYSTEM[ \r\n\t]+\"(.*?)\"");
  private Pattern _docTypeSubset = Pattern.compile("\\[([^\\]]*)\\]>");
  private Pattern _invalidStringPattern = Pattern.compile("([<>&'])");

  public FuzzyXMLParser(boolean wo54) {
    this(wo54, false);
  }

  public FuzzyXMLParser(boolean wo54, boolean isHTML) {
    super();
    _wo54 = wo54;
    _roots = new LinkedList<FuzzyXMLNode>();
    _isHTML = isHTML;
    // MS: Hardcoded that "wo" is a loose namespace
    addLooseNamespace("wo");
    addLooseNamespace("webobject");
    addLooseNamespace("webobjects");
    if (!_wo54) {
      addAutocloseTag("img");
      addAutocloseTag("br");
      addAutocloseTag("hr");
      addAutocloseTag("meta");
      addAutocloseTag("link");
      addAutocloseTag("input");
      addAutocloseTag("spacer");
      addAutocloseTag("frame");
      addAutocloseTag("basefont");
      addAutocloseTag("base");
      addAutocloseTag("area");
      addAutocloseTag("col");
      addAutocloseTag("isindex");
      addAutocloseTag("param");
      addLooseTag("p");
      addLooseTag("li");
    }
  }
  
  /**
   * An autoclose tag is like br or link where it commonly does not have a closing tag
   * but it also never has contents.
   * 
   * @param autocloseTag the name of the tag to make loose
   */
  public void addAutocloseTag(String autocloseTag) {
    _autocloseTags.add(autocloseTag);
    addLooseTag(autocloseTag);
  }
  
  /**
   * A "loose" tag is like li or p where people lazily often do not close them properly,
   * but they may have content.
   * 
   * @param looseTag the name of the tag to make loose
   */
  public void addLooseTag(String looseTag) {
    _looseTags.add(looseTag);
  }

  /**
   * A "loose" namespace is like the wo: namespace.  We don't actually require that
   * wo:if have a corresponding wo:if close tag -- it actually just needs a 
   * wo close tag.
   * 
   * @param namespace the name of the namespace to make loose
   */
  public void addLooseNamespace(String namespace) {
    _looseNamespaces.add(namespace);
  }

  /**
   * �G���[�n���h�����O�p�̃��X�i��ǉ����܂��B
   * 
   * @param listener ���X�i
   */
  public void addErrorListener(FuzzyXMLErrorListener listener) {
    _listeners.add(listener);
  }

  private void fireErrorEvent(int offset, int length, String message, FuzzyXMLNode node) {
    FuzzyXMLErrorEvent evt = new FuzzyXMLErrorEvent(offset, length, message, node);
    for (FuzzyXMLErrorListener listener : _listeners) {
      listener.error(evt);
    }
  }

  /**
   * ���̓X�g���[������XML�h�L�������g���p�[�X���܂��B
   * �����R�[�h��XML�錾�ɂ��������Ĕ��ʂ���܂��B
   * 
   * @param in ���̓X�g���[��
   * @return �p�[�X����
   * @throws IOException
   */
  public FuzzyXMLDocument parse(InputStream in) throws IOException {
    byte[] bytes = FuzzyXMLUtil.readStream(in);
    String encode = FuzzyXMLUtil.getEncoding(bytes);
    if (encode == null) {
      return parse(new String(bytes));
    }
    return parse(new String(bytes, encode));
  }

  /**
   * �t�@�C������XML�h�L�������g���p�[�X���܂��B
   * �����R�[�h��XML�錾�ɂ��������Ĕ��ʂ���܂��B
   * 
   * @param file �t�@�C��
   * @return �p�[�X����
   * @throws IOException
   */
  public FuzzyXMLDocument parse(File file) throws IOException {
    byte[] bytes = FuzzyXMLUtil.readStream(new FileInputStream(file));
    String encode = FuzzyXMLUtil.getEncoding(bytes);
    if (encode == null) {
      return parse(new String(bytes));
    }
    return parse(new String(bytes, encode));
  }

  protected int _parse(String source, int initialOffset, boolean woOnly) {
    // �p�[�X���J�n
    Matcher matcher = _tag.matcher(source);
    int lastIndex = initialOffset - 1;
    while (matcher.find()) {
      int start = matcher.start() + initialOffset;
      int end = matcher.end() + initialOffset;
      if (lastIndex == -1 && lastIndex < start) {
        handleText(0, start, true);
      }
      else if (lastIndex != (initialOffset - 1) && lastIndex < start) {
        handleText(lastIndex, start, true);
      }
      String text = matcher.group(1).trim();
      // ���^�O
      if (!woOnly && text.startsWith("%")) {
        // ignore
        handleText(start, end, false);
      }
      else if (!woOnly && text.startsWith("?")) {
        handleDeclaration(start, end);
      }
      else if (!woOnly && (text.startsWith("!DOCTYPE") || text.startsWith("!doctype"))) {
        handleDoctype(start, end, text);
      }
      else if (!woOnly && text.startsWith("![CDATA[")) {
        handleCDATA(start, end, _originalSource.substring(start, end));
      }
      else if (text.startsWith("/") && (!woOnly || WodHtmlUtils.isWOTag(text.substring(1)))) {
        handleCloseTag(start, end, text);
      }
      else if (!woOnly && text.startsWith("!--")) {
        handleComment(start, end, _originalSource.substring(start, end));
      }
      else if (text.endsWith("/") && (!woOnly || WodHtmlUtils.isWOTag(text))) {
        handleEmptyTag(start, end);
      }
      else if (!woOnly || WodHtmlUtils.isWOTag(text)) {
        handleStartTag(start, end);
      }
      lastIndex = end;
    }
    return lastIndex;
  }

  /**
   * �����Ƃ��ēn���ꂽXML�\�[�X���p�[�X����FuzzyXMLDocument�I�u�W�F�N�g��ԋp���܂��B
   * 
   * @param source XML�\�[�X
   * @return �p�[�X���ʂ�FuzzyXMLDocument�I�u�W�F�N�g
   */
  public FuzzyXMLDocument parse(String source) {
    // �I���W�i���̃\�[�X��ۑ����Ă���
    _originalSource = source;
    // �R�����g�ACDATA�ADOCTYPE����������
    source = FuzzyXMLUtil.comment2space(source, true);
    source = FuzzyXMLUtil.escapeScript(source);
    source = FuzzyXMLUtil.scriptlet2space(source, true);
    source = FuzzyXMLUtil.cdata2space(source, true);
    source = FuzzyXMLUtil.doctype2space(source, true);
    source = FuzzyXMLUtil.processing2space(source, true);
    source = FuzzyXMLUtil.escapeString(source);

    int lastIndex = _parse(source, 0, false);

    if (_stack.size() > 0 && _nonCloseElements.size() > 0) {
      FuzzyXMLElementImpl lastElement = (FuzzyXMLElementImpl) _nonCloseElements.get(_nonCloseElements.size() - 1);
      String lowercaseLastElementName = lastElement.getName().toLowerCase();
      if (!_looseTags.contains(lowercaseLastElementName)) {
        fireErrorEvent(lastElement.getOffset(), lastElement.getLength(), Messages.getMessage("error.noCloseTag", lastElement.getName()), null);
      }

      for (FuzzyXMLNode openNode : _stack) {
        if (openNode instanceof FuzzyXMLElementImpl) {
          FuzzyXMLElementImpl openElement = (FuzzyXMLElementImpl) openNode;
          openElement.setLength(lastIndex - openElement.getOffset());
          if (openElement.getParentNode() == null) {
            _roots.add(openElement);
          }
          else {
            ((FuzzyXMLElementImpl) openElement.getParentNode()).appendChildWithNoCheck(openElement);
          }
        }
      }
    }

    FuzzyXMLElement docElement = null;
    if (_roots.size() == 0) {
      docElement = new FuzzyXMLElementImpl(null, "document", 0, _originalSource.length(), 0);
      //docElement.appendChild(root);
    }
    else {
      FuzzyXMLNode firstRoot = _roots.get(0);
      FuzzyXMLNode lastRoot = _roots.get(_roots.size() - 1);
      docElement = new FuzzyXMLElementImpl(null, "document", firstRoot.getOffset(), lastRoot.getOffset() + lastRoot.getLength() - firstRoot.getOffset(), 0);
      for (FuzzyXMLNode root : _roots) {
        ((FuzzyXMLElementImpl) docElement).appendChildWithNoCheck(root);
      }
    }
    FuzzyXMLDocumentImpl doc = new FuzzyXMLDocumentImpl(docElement, _docType);
    doc.setHTML(_isHTML);
    return doc;
  }

  /** CDATA�m�[�h���������܂��B */
  private void handleCDATA(int offset, int end, String text) {
    closeAutocloseTags();
    text = text.replaceFirst("<!\\[CDATA\\[", "");
    text = text.replaceFirst("\\]\\]>", "");
    FuzzyXMLCDATAImpl cdata = new FuzzyXMLCDATAImpl(getParent(), text, offset, end - offset);
    if (getParent() != null) {
      ((FuzzyXMLElement) getParent()).appendChild(cdata);
    }
    else {
      _roots.add(cdata);
    }

    _stack.push(cdata);
    _parse(text, offset + "<![CDATA[".length(), true);
    FuzzyXMLNode poppedNode = _stack.pop();
    if (poppedNode != cdata) {
      _stack.push(poppedNode);
    }
  }

  /** �e�L�X�g�m�[�h���������܂��B */
  private void handleText(int offset, int end, boolean escape) {
    String text = _originalSource.substring(offset, end);
    //System.out.println("FuzzyXMLParser.handleText: '" + text + "'");
    if (text != null && text.trim().length() > 0) {
      closeAutocloseTags();
    }
    FuzzyXMLTextImpl textNode = new FuzzyXMLTextImpl(getParent(), text, offset, end - offset);
    textNode.setEscape(escape);
    if (getParent() != null) {
      //FuzzyXMLUtil.decode(text, _isHTML)
      ((FuzzyXMLElement) getParent()).appendChild(textNode);
    }
    else {
      _roots.add(textNode);
    }
  }

  /** XML�錾�i�������߁j���������܂��B */
  private void handleDeclaration(int offset, int end) {
    closeAutocloseTags();
    String text = _originalSource.substring(offset, end);
    text = text.replaceFirst("^<\\?", "");
    text = text.replaceFirst("\\?>$", "");
    text = text.trim();

    String[] dim = text.split("[ \r\n\t]+");
    String name = dim[0];
    String data = text.substring(name.length()).trim();

    FuzzyXMLProcessingInstructionImpl pi = new FuzzyXMLProcessingInstructionImpl(null, name, data, offset, end - offset);
    if (getParent() != null) {
      // �]�v�ȕ��������
      ((FuzzyXMLElement) getParent()).appendChild(pi);
    }
    else {
      _roots.add(pi);
    }
    
    // XML should not have autoclosing tags 
    if (name.startsWith("xml")) {
      _autocloseTags.clear();
    }
  }

  /** DOCTYPE�錾���������܂��B */
  private void handleDoctype(int offset, int end, String text) {
    closeAutocloseTags();
    if (_docType == null) {
      String name = "";
      String publicId = "";
      String systemId = "";
      String internalSubset = "";

      text = _originalSource.substring(offset, end);
      Matcher matcher = _docTypeName.matcher(text);
      if (matcher.find()) {
        name = matcher.group(1);
      }
      matcher = _docTypePublic.matcher(text);
      if (matcher.find()) {
        publicId = matcher.group(1);
        systemId = matcher.group(2);
      }
      else {
        matcher = _docTypeSystem.matcher(text);
        if (matcher.find()) {
          systemId = matcher.group(1);
        }
      }
      matcher = _docTypeSubset.matcher(text);
      if (matcher.find()) {
        internalSubset = matcher.group(1);
      }
      _docType = new FuzzyXMLDocTypeImpl(null, name, publicId, systemId, internalSubset, offset, end - offset);
    }
  }

  private void closeAutocloseTags() {
    if (_stack.size() > 0) {
      FuzzyXMLElementImpl lastOpenElement = (FuzzyXMLElementImpl)_stack.peek();
      String name = lastOpenElement.getName().toLowerCase();
      if (_autocloseTags.contains(name)) {
        int openTagEndOffset = lastOpenElement.getOffset() + lastOpenElement.getOpenTagLength();
        handleCloseTag(openTagEndOffset, openTagEndOffset, "/" + name, false);
      }
    }
  }
  
  /** ���^�O���������܂��B */
  private void handleCloseTag(int offset, int end, String text) {
    handleCloseTag(offset, end, text, true);
  }

  private void handleCloseTag(int offset, int end, String text, boolean showMismatchError) {
    if (_stack.size() == 0) {
      return;
    }
    String tagName = text.substring(1).trim();

    // MS: Chuck does close tags like </webobject closing something else> 
    int chuckIndex = tagName.indexOf(' ');
    if (chuckIndex != -1) {
      String chuckWord = tagName.substring(0, chuckIndex);
      if (WodHtmlUtils.isWOTag(chuckWord)) {
        tagName = chuckWord;
      }
    }

    FuzzyXMLElementImpl lastOpenElement = (FuzzyXMLElementImpl) _stack.pop();
    String lowercaseLastOpenElementName = lastOpenElement.getName().toLowerCase();
    String lowercaseCloseTagName = tagName.toLowerCase();

    boolean closeTagMatches = lowercaseLastOpenElementName.equals(lowercaseCloseTagName);
    //System.out.println("FuzzyXMLParser.handleCloseTag: lastOpen = " + lowercaseLastOpenElementName + ", close = " + lowercaseCloseTagName);
    if (!closeTagMatches) {
      closeAutocloseTags();

      // Allow </wo> to close </wo:if>
      boolean looseNamespace = false;
      int colonIndex = lowercaseLastOpenElementName.indexOf(':');
      if (colonIndex != -1) {
        String elementNamespace = lowercaseLastOpenElementName.substring(0, colonIndex);
        if (lowercaseCloseTagName.equals(elementNamespace) && _looseNamespaces.contains(elementNamespace)) {
          tagName = lastOpenElement.getName();
          lowercaseCloseTagName = lowercaseLastOpenElementName;
          looseNamespace = true;
        }
      }

      if (!looseNamespace) {
        boolean looseTag = false;
        if (_looseTags.contains(lowercaseLastOpenElementName)) {
          looseTag = true;
        }

        if (looseTag) {
          while (lowercaseLastOpenElementName != null && !lowercaseLastOpenElementName.equals(lowercaseCloseTagName) && _looseTags.contains(lowercaseLastOpenElementName)) {
            int lastOpenElementEndOffset = end;
            //int lastOpenElementEndOffset = lastOpenElement.getOffset() + lastOpenElement.getLength();
            _stack.push(lastOpenElement);
            handleCloseTag(lastOpenElementEndOffset, lastOpenElementEndOffset, "/" + lastOpenElement.getName(), false);

            /*
             FuzzyXMLElement looseElement = lastOpenElement;
             FuzzyXMLNode[] looseElementChildren = lastOpenElement.getChildren();
             FuzzyXMLElement looseElementParent = (FuzzyXMLElement) lastOpenElement.getParentNode();
             for (FuzzyXMLNode looseElementChild : looseElementChildren) {
             looseElement.removeChild(looseElementChild);
             looseElementParent.insertAfter(looseElementChild, looseElement);
             //((AbstractFuzzyXMLNode) looseElementChild).setOffset(looseElementChild.getOffset() + 1);
             }
             */

            if (_stack.size() == 0) {
              lastOpenElement = null;
              lowercaseLastOpenElementName = null;
            }
            else {
              lastOpenElement = (FuzzyXMLElementImpl) _stack.pop();
              lowercaseLastOpenElementName = lastOpenElement.getName().toLowerCase();
            }
          }
        }
        else {
          FuzzyXMLElement matchingOpenElement = null;
          for (FuzzyXMLElement nonCloseElement : _nonCloseElements) {
            if (nonCloseElement.getName().equalsIgnoreCase(lowercaseCloseTagName)) {
              matchingOpenElement = nonCloseElement;
            }
          }
          if (matchingOpenElement == null) {
            if (showMismatchError) {
              fireErrorEvent(offset, end - offset, Messages.getMessage("error.noStartTag", tagName), null);
            }
            _stack.push(lastOpenElement);
            return;
          }

          //System.out.println("FuzzyXMLParser.handleCloseTag: mismatched close " + lastOpenElement.getName());
          if (showMismatchError) {
            fireErrorEvent(offset, end - offset, "</" + tagName + "> occurred before </" + lastOpenElement.getName() + ">", null);
            fireErrorEvent(lastOpenElement.getOffset(), lastOpenElement.getLength(), "</" + tagName + "> occurred before </" + lastOpenElement.getName() + ">", null);
          }
          _stack.push(lastOpenElement);
          handleCloseTag(offset, offset, "/" + lastOpenElement.getName(), false);
          lastOpenElement = (FuzzyXMLElementImpl) _stack.pop();
          lowercaseLastOpenElementName = lastOpenElement.getName().toLowerCase();
        }
        /*
         boolean matchesOpenElement = false;
         if (looseTag) {
         for (FuzzyXMLElement nonCloseElement : nonCloseElements) {
         if (nonCloseElement.getName().equalsIgnoreCase(lowercaseCloseTagName)) {
         matchesOpenElement = true;
         }
         }
         if (matchesOpenElement) {
         nonCloseElements.remove(lastOpenElement);
         }
         }

         if (lastOpenElement.getParentNode() != null) {
         ((FuzzyXMLElementImpl) lastOpenElement.getParentNode()).appendChildWithNoCheck(lastOpenElement);
         FuzzyXMLNode[] nodes = lastOpenElement.getChildren();
         for (int i = 0; i < nodes.length; i++) {
         ((AbstractFuzzyXMLNode) nodes[i]).setParentNode(lastOpenElement.getParentNode());
         lastOpenElement.removeChild(nodes[i]);
         ((FuzzyXMLElementImpl) lastOpenElement.getParentNode()).appendChildWithNoCheck(nodes[i]);
         }
         }
         else {
         //System.out.println(tagName + "�̊J�n�^�O��������܂���B");
         fireErrorEvent(offset, end - offset, Messages.getMessage("error.noStartTag", tagName), null);
         }
         if (matchesOpenElement) {
         handleCloseTag(offset, end, text);
         }
         //			stack.push(element);
         return;
         */
      }
    }

    if (lastOpenElement != null) {
      // ��^�O�̏ꍇ�͋�̃e�L�X�g�m�[�h��ǉ����Ă���
      if (lastOpenElement.getChildren().length == 0) {
        // MS: Hopefully this doesn't break things ... Sure wish I could read
        // Japanese to know what the original author said about this :)
        //lastOpenElement.appendChild(new FuzzyXMLTextImpl(getParent(), "", offset, 0));
      }
      lastOpenElement.setLength(end - lastOpenElement.getOffset());
      if (closeTagMatches) {
        lastOpenElement.setCloseTagOffset(offset);
        lastOpenElement.setCloseTagLength(end - offset - 2);
        lastOpenElement.setCloseNameOffset(text.indexOf(tagName));
      }
      _nonCloseElements.remove(lastOpenElement);
      if (lastOpenElement.getParentNode() == null) {
        _roots.add(lastOpenElement);
        for (FuzzyXMLElement error : _nonCloseElements) {
          //System.out.println(error.getName() + "�͕��Ă��܂���B");
          if (showMismatchError) {
            fireErrorEvent(error.getOffset(), error.getLength(), Messages.getMessage("error.noCloseTag", error.getName()), error);
          }
        }
      }
      else {
        ((FuzzyXMLElementImpl) lastOpenElement.getParentNode()).appendChildWithNoCheck(lastOpenElement);
      }
    }
  }

  private void checkAttributeValue(FuzzyXMLAttribute attr) {
    String str = attr.getValue();
    if (str != null) {
      str = str.replaceAll("&[^&; \"]+;", " ");
      Matcher invalidStringMatcher = _invalidStringPattern.matcher(str);
      while (invalidStringMatcher.find()) {
        String invalidPart = invalidStringMatcher.group();
        fireErrorEvent(attr.getParentNode().getOffset() + attr.getValueDataOffset() + 1, attr.getValueDataLength(), "The character '" + invalidPart + "' must be escaped.", attr);
      }
    }
  }

  /** ��^�O���������܂��B */
  private void handleEmptyTag(int offset, int end) {
    TagInfo info = parseTagContents(_originalSource.substring(offset + 1, end - 1));
    FuzzyXMLNode parent = getParent();
    FuzzyXMLElementImpl element = new FuzzyXMLElementImpl(parent, info.name, offset, end - offset, info.nameOffset);
    if (parent == null) {
      _roots.add(element);
    }
    else {
      ((FuzzyXMLElement) parent).appendChild(element);
    }
    // ������ǉ�
    AttrInfo[] attrs = info.getAttrs();
    for (int i = 0; i < attrs.length; i++) {
      FuzzyXMLAttributeImpl attr = new FuzzyXMLAttributeImpl(element, attrs[i].name, attrs[i].value, attrs[i].offset + offset, attrs[i].end - attrs[i].offset + 1, attrs[i].valueOffset);
      attr.setQuoteCharacter(attrs[i].quote);
      if (attrs[i].value.indexOf('"') >= 0 || attrs[i].value.indexOf('\'') >= 0 || attrs[i].value.indexOf('<') >= 0 || attrs[i].value.indexOf('>') >= 0 || attrs[i].value.indexOf('&') >= 0) {
        attr.setEscape(false);
      }
      element.appendChild(attr);
    }

    for (FuzzyXMLAttribute attr : element.getAttributes()) {
      _stack.push(element);
      if (!_wo54) {
        _parse(attr.getValue(), element.getOffset() + attr.getValueDataOffset() + 1, true);
      }
      else {
        checkAttributeValue(attr);
      }
      FuzzyXMLNode poppedNode = _stack.pop();
      if (poppedNode != element) {
        _stack.push(poppedNode);
      }
    }
  }

  /** �R�����g���������܂��B */
  private void handleComment(int offset, int end, String text) {
    if (getParent() != null) {
      text = text.replaceFirst("<!--", "");
      text = text.replaceFirst("-->", "");
      FuzzyXMLCommentImpl comment = new FuzzyXMLCommentImpl(getParent(), text, offset, end - offset);
      ((FuzzyXMLElement) getParent()).appendChild(comment);
    }
  }

  /** �J�n�^�O���������܂��B */
  private void handleStartTag(int offset, int end) {
    closeAutocloseTags();
    TagInfo info = parseTagContents(_originalSource.substring(offset + 1, end - 1));
    //System.out.println("FuzzyXMLParser.handleStartTag: open " + info.name);
    FuzzyXMLElementImpl element = new FuzzyXMLElementImpl(getParent(), info.name, offset, end - offset, info.nameOffset);
    // ������ǉ�
    AttrInfo[] attrs = info.getAttrs();
    for (int i = 0; i < attrs.length; i++) {
      //			// ���O��Ԃ̃T�|�[�g
      //			if(attrs[i].name.startsWith("xmlns")){
      //				String uri    = attrs[i].value;
      //				String prefix = null;
      //				String[] dim = attrs[i].name.split(":");
      //				if(dim.length > 1){
      //					prefix = dim[1];
      //				}
      //				element.addNamespaceURI(prefix,uri);
      //			}
      FuzzyXMLAttributeImpl attr = new FuzzyXMLAttributeImpl(element, attrs[i].name, attrs[i].value, attrs[i].offset + offset, attrs[i].end - attrs[i].offset + 1, attrs[i].valueOffset);
      attr.setQuoteCharacter(attrs[i].quote);
      if (attrs[i].value.indexOf('"') >= 0 || attrs[i].value.indexOf('\'') >= 0 || attrs[i].value.indexOf('<') >= 0 || attrs[i].value.indexOf('>') >= 0 || attrs[i].value.indexOf('&') >= 0) {
        attr.setEscape(false);
      }
      element.appendChild(attr);
    }
    _stack.push(element);
    _nonCloseElements.add(element);

    for (FuzzyXMLAttribute attr : element.getAttributes()) {
      _stack.push(element);
      if (!_wo54) {
        _parse(attr.getValue(), element.getOffset() + attr.getValueDataOffset() + 1, true);
      }
      else {
        checkAttributeValue(attr);
      }
      FuzzyXMLNode poppedNode = _stack.pop();
      if (poppedNode != element) {
        _stack.push(poppedNode);
      }
    }
  }

  /** �X�^�b�N�̍Ō�̗v�f���擾���܂�(�X�^�b�N����͍폜���܂���)�B */
  private FuzzyXMLNode getParent() {
    if (_stack.size() == 0) {
      return null;
    }
    return _stack.get(_stack.size() - 1);
  }

  /** �^�O�������p�[�X���܂��B */
  private TagInfo parseTagContents(String text) {
    // �g����
    Range trimmedRange = Range.trimmedRange(text);
    text = trimmedRange.trim(text);
    // ���^�O��������Ō�̃X���b�V�����폜
    if (text.endsWith("/")) {
      text = text.substring(0, text.length() - 1);
    }
    // �ŏ��̃X�y�[�X�܂ł��^�O��
    TagInfo info = new TagInfo();
    if (FuzzyXMLUtil.getSpaceIndex(text) != -1) {
      info.name = text.substring(0, FuzzyXMLUtil.getSpaceIndex(text)).trim();
      info.nameOffset = trimmedRange.getOffset();
      parseAttributeContents(info, text);
    }
    else {
      info.name = text;
    }
    return info;
  }

  /** �A�g���r���[�g�������p�[�X���܂��B */
  private void parseAttributeContents(TagInfo info, String text) {

    int state = 0;
    StringBuffer sb = new StringBuffer();
    String name = null;
    char quote = 0;
    int start = -1;
    int valueOffset = -1;
    boolean escape = false;

    for (int i = 0; i < text.length(); i++) {
      char c = text.charAt(i);
      if (state == 0 && FuzzyXMLUtil.isWhitespace(c)) {
        state = 1;
      }
      else if (state == 1 && !FuzzyXMLUtil.isWhitespace(c)) {
        if (start == -1) {
          start = i;
        }
        state = 2;
        sb.append(c);
      }
      else if (state == 2) {
        if (c == '=') {
          state = 3;
          name = sb.toString().trim();
          sb.setLength(0);
          valueOffset = -1;
        }
        else {
          sb.append(c);
        }
      }
      else if (state == 3 && !FuzzyXMLUtil.isWhitespace(c)) {
        if (valueOffset == -1) {
          valueOffset = i;
        }
        if (c == '\'' || c == '\"') {
          quote = c;
        }
        else {
          quote = 0;
          sb.append(c);
        }
        state = 4;
      }
      else if (state == 4) {
        if (c == quote && escape == true) {
          sb.append(c);
          escape = false;
        }
        else if (c == quote || (quote == 0 && FuzzyXMLUtil.isWhitespace(c))) {
          // add an attribute
          AttrInfo attr = new AttrInfo();
          attr.name = name;
          attr.value = sb.toString();//FuzzyXMLUtil.decode(sb.toString(), _isHTML);
          attr.valueOffset = valueOffset;
          attr.offset = start;
          attr.end = i + 1;
          attr.quote = quote;
          info.addAttr(attr);

          // reset
          sb.setLength(0);
          state = 1;
          start = -1;
        }
        else if (c == '\\') {
          if (escape == true) {
            sb.append(c);
          }
          else {
            // MS: I took out escaping .. This is potentially a really sketchy thing to do, but it
            // was breaking attributes like   numberformat = "\$#,##0.00"
            sb.append(c);
            escape = true;
          }
        }
        else {
          sb.append(c);
          escape = false;
        }
      }
    }
    if (state == 4 && quote == 0) {
      AttrInfo attr = new AttrInfo();
      attr.name = name;
      attr.value = sb.toString();//FuzzyXMLUtil.decode(sb.toString(), _isHTML);
      attr.valueOffset = valueOffset;
      attr.offset = start;
      attr.end = text.length();
      attr.quote = quote;
      info.addAttr(attr);
    }
    if (state == 4 && quote != 0) {
      //System.out.println("FuzzyXMLParser.parseAttributeContents: " + info.name);
    }
    //		Matcher matcher = attr.matcher(text);
    //		while(matcher.find()){
    //			AttrInfo attr = new AttrInfo();
    //			attr.name   = matcher.group(1);
    //			attr.value  = FuzzyXMLUtil.decode(matcher.group(3));
    //			attr.offset = matcher.start();
    //			attr.end    = matcher.end();
    //			info.addAttr(attr);
    //		}
  }

  private class TagInfo {
    private String name;
    private int nameOffset;
    private ArrayList<AttrInfo> attrs = new ArrayList<AttrInfo>();

    public void addAttr(AttrInfo attr) {
      // �������̂������Ă��ǉ����Ȃ�
      AttrInfo[] info = getAttrs();
      for (int i = 0; i < info.length; i++) {
        if (info[i].name.equals(attr.name)) {
          return;
        }
      }
      attrs.add(attr);
    }

    public AttrInfo[] getAttrs() {
      return attrs.toArray(new AttrInfo[attrs.size()]);
    }
  }

  private class AttrInfo {
    private String name;
    private String value;
    private int offset;
    private int valueOffset;
    private int end;
    private char quote;
  }

  public static class Range {
    private int _offset;
    private int _length;

    public Range() {
    }

    public int getOffset() {
      return _offset;
    }

    public int getLength() {
      return _length;
    }

    public String trim(String str) {
      return str.substring(_offset, _offset + _length);
    }

    public static Range trimmedRange(String str) {
      int i = 0;
      int length = str.length();
      Range r = new Range();
      for (i = 0; i < length && str.charAt(i) <= ' '; i++) {
        // DO NOTHING
      }
      r._offset = i;

      for (i = length - 1; i > r._offset && str.charAt(i) <= ' '; i--) {
        // DO NOTHING
      }
      r._length = (i - r._offset + 1);
      return r;
    }
  }

}